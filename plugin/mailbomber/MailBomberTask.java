package plugin.mailbomber;

import gui.*;
import gui.docking.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.*;

import org.apache.commons.mail.*;
import org.apache.commons.mail.resolver.*;

import com.alee.utils.*;

import core.*;
import core.datasource.*;
import core.tasks.*;

public class MailBomberTask implements TRunnable {

	public static String INTERACTIVE = "interactive";
	private DBAccess addressBookDBA;
	private Record messageRcd;
	private String inlineDir;
	private TProgressMonitor monitor;
	private Hashtable taskParameters;

	public MailBomberTask() {
		this.addressBookDBA = ConnectionManager.getAccessTo("m_address_book");
		this.inlineDir = null;
		taskParameters = new Hashtable();
	}
	public static File extractMailTemplate(byte[] tpl) throws Exception {
		File tmpd = TResources.createTemporalDirectory("mailBomber");
		File tmpf = File.createTempFile("template", ".zip", tmpd);
		FileOutputStream fos = new FileOutputStream(tmpf);
		fos.write(tpl);
		fos.close();
		TResources.extractZipFile2(tmpf, tmpd.getPath() + "/");
		return tmpf;
	}

	private Vector<Record> validatePreconditions() throws Exception, TError{
		Vector<Record> addrlist = new Vector<Record>();

		// check for template parameter. if selected, prepare enviorement for inline images
		inlineDir = null;
		String tid = (String) messageRcd.getFieldValue("m_metemplate_id");
		if (!tid.equals("")) {
			Record ter = ConnectionManager.getAccessTo("M_TEMPLATES").exist("M_TEID = '" + tid + "'");
			if (ter == null) {
				SystemLog.log("mail.msg06", "MailBomber", "", "Template not found.");
				throw new TError("mail.msg06", "Template not found.");
			}
			// prepara for inline
			try {
				File tmpf = extractMailTemplate((byte[]) ter.getFieldValue("m_tetemplate"));
				inlineDir = tmpf.getParent();
			} catch (Exception e) {
				SystemLog.log("mail.msg06", "MailBomber", "", e.getMessage());
				throw e;
			}
		}

		// resolving all personal record type
		String mto = (String) messageRcd.getFieldValue("m_meto");
		Record addr = addressBookDBA.exist("m_abid = '" + mto + "'");
		if (addr == null) {
			SystemLog.log("mail.msg02", "MailBomber", "", mto);
			throw new TError("mail.msg02", mto);
		}
		addrlist.add(addr);

		// for any entry in addrlist, look up for its chlidrens nodes
		SystemLog.log("mail.msg01", "MailBomber", "", MailBomber.formatMail(addr));
		for (int i = 0; i < addrlist.size(); i++) {
			addr = addrlist.get(i);
			if (!addr.getFieldValue("m_abtype").equals("personal")) {
				addrlist.remove(i);
				i--;
				String pid = (String) addr.getFieldValue("m_abid");
				Vector<Record> v = addressBookDBA.search("m_abparentid = '" + pid + "'", null);
				addrlist.addAll(v);
			}
		}
		return addrlist;
	}
	@Override
	public void run() {
		try {
			Thread.sleep(250);
			monitor.setProgress(0, "mailtask.precon");
			Vector<Record> addrlist = validatePreconditions();
			int senderr = 0;
				// send mail to all recipient
				for (int j = 0; j < addrlist.size(); j++) {
					Record addr = addrlist.elementAt(j);
					int per = j + 1 * 100 / addrlist.size();
					String msg = MessageFormat.format(TStringUtils.getString("mailtask.sendingto"),
							addr.getFieldValue("m_abname"));
					monitor.setProgress(per, msg);
					boolean ok = sendMail(messageRcd, addr);
					senderr = (ok) ? senderr : senderr + 1;
					// sleep for ten seconds : TODO: add to systemvars
					Thread.sleep(10 * 1000);
				}
				monitor.dispose();
				Alesia.showNotification("mail.notification.msg01", messageRcd.getFieldValue("m_mesubject"),
						addrlist.size(), senderr);
				DockingContainer.signalFreshgen(MailLog.class.getName());
		} catch (Exception ex) {
			// java.lang.InterruptedException: sleep interrupted not monitoring in case of cancel
			if (!(ex instanceof InterruptedException)) {
				monitor.dispose();
				SystemLog.logException(ex);
				Alesia.showNotification("notification.msg00", ex.getMessage());
			}
		}
	}

	private boolean sendMail(Record msg, Record addr) {
		String mto = (String) addr.getFieldValue("m_abemail");
		boolean ok = false;
		try {
			SystemLog.log("mail.msg03", "MailBomber", "", MailBomber.formatMail(addr));
			String toname = addr.getFieldValue("m_abtitle") + " " + addr.getFieldValue("m_abname");

			// config email parameters
			ImageHtmlEmail imageHtmlEmail = new ImageHtmlEmail();
			imageHtmlEmail.setHostName(PluginManagerIld.getPluginProperty("MailBomber", "mail.config.server"));
			String val = PluginManagerIld.getPluginProperty("MailBomber", "mail.config.smptport");
			imageHtmlEmail.setSmtpPort(Integer.valueOf(val));
			String us = PluginManagerIld.getPluginProperty("MailBomber", "mail.config.autenticator.user");
			String pass = PluginManagerIld.getPluginProperty("MailBomber", "mail.config.autenticator.password");
			imageHtmlEmail.setAuthenticator(new DefaultAuthenticator(us, pass));
			String ssl = PluginManagerIld.getPluginProperty("MailBomber", "mail.config.ssl");
			imageHtmlEmail.setSSLOnConnect(ssl.equals("true"));

			// header
			imageHtmlEmail.setFrom(us, PluginManagerIld.getPluginProperty("MailBomber", "mail.config.user.name"));
			imageHtmlEmail.addTo(mto, toname);
			imageHtmlEmail.setSubject((String) messageRcd.getFieldValue("m_mesubject"));

			// text message or template? (when template, inlinedir != null)
			String oldbody = (String) msg.getFieldValue("m_mebody");
			if (inlineDir != null) {
				File f = new File(inlineDir + "/index.html");
				oldbody = FileUtils.readToString(f);
			}
			String newbody = replaceVars(oldbody, addr);
			imageHtmlEmail.setHtmlMsg(newbody);

			// inline images
			if (inlineDir != null) {
				DataSourceFileResolver dsfr = new DataSourceFileResolver(new File(inlineDir));
				imageHtmlEmail.setDataSourceResolver(dsfr);
			}

			// static attachment
			ArrayList<File> files = (ArrayList<File>) TPreferences.getObjectFromByteArray((byte[]) messageRcd
					.getFieldValue("m_mestatic_attch"));
			String act = (String) messageRcd.getFieldValue("m_mestatic_attch_err");
			if (act.equals("abort")) {
				for (File f : files) {
					if (!f.exists()) {
						SystemLog.log("mail.msg07", "MailBomber", "", f);
						return false;
					}
				}
			}

			// dinamyc attachment
			String sdir = (String) messageRcd.getFieldValue("m_medynamic_attch_dir");
			String patt = (String) messageRcd.getFieldValue("m_medynamic_attch");
			if (!(sdir.equals("") || patt.equals(""))) {
				String attfn = replaceVars(patt, addr);
				File df = new File(sdir + "/" + attfn);
				act = (String) messageRcd.getFieldValue("m_medynamic_attch_err");
				if (act.equals("abort") && !df.exists()) {
					SystemLog.log("mail.msg07", "MailBomber", "", df);
					return false;
				}
				if (df.exists()) {
					files.add(df);
				}
			}

			for (File f : files) {
				SystemLog.log("mail.msg08", "MailBomber", "", f);
				EmailAttachment attach = new EmailAttachment();
				attach.setPath(f.getAbsolutePath());
				attach.setDisposition(EmailAttachment.ATTACHMENT);
				attach.setName(f.getName());
				imageHtmlEmail.attach(attach);
			}
			imageHtmlEmail.send();
			SystemLog.log("mail.msg04", "MailBomber", "", MailBomber.formatMail(addr));
			ok = true;
		} catch (Exception e) {
			SystemLog.log("mail.msg05", "MailBomber", e);
		}
		return ok;
	}

	/**
	 * replace every variables found in body text to their values accourding with given parameters. this method navigate
	 * upward to find all variables, in case that a mail recipient have multiples parent recors of OUnit type, the first
	 * (the inmediate parent in up direction) is used to replace variables inside the pattern
	 * 
	 * @param patt - pattern
	 * @param addr - contact from address book
	 * 
	 * @return personalized mody message
	 */
	private String replaceVars(String patt, Record addr) {
		String tmpmsg = patt;
		Record current = new Record(addr);
		while (current != null) {
			String prefix = (String) current.getFieldValue("m_abtype") + ".";
			tmpmsg = TStringUtils.format(tmpmsg, prefix, current);
			// get the parent node
			String parid = (String) current.getFieldValue("m_abparentid");
			// stop at no parent id
			current = addressBookDBA.exist("M_ABID = '" + parid + "'");
		}
		return tmpmsg;
	}

	@Override
	public Hashtable getTaskParameters() {
		return taskParameters;
	}
	@Override
	public void setTaskParameters(Hashtable parms) {
		taskParameters.putAll(parms);
		this.messageRcd = (Record) parms.get("Record");
	}
	@Override
	public void setFuture(Future f, boolean ab) {
		this.monitor = new TProgressMonitor("SendMail", "Enviar e-Mail", f, ab);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				monitor.show(Alesia.mainFrame);
			}
		});
	}
}
