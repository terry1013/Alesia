package plugin.mailbomber;

import gui.*;
import gui.docking.*;

import java.util.*;
import java.util.concurrent.*;

import javax.swing.*;

import core.*;
import core.datasource.*;
import core.tasks.*;

public class AddressBookUpdater implements TRunnable {

	public static String UPDATER_ACTION = "updateAction";
	public static String UPDATER_CREATE = "create";
	public static String UPDATER_UPDATE = "update";
	public static String UPDATER_WRITE = "write";

	private DBAccess addressBookDBA;
	private TProgressMonitor monitor;
	private Hashtable taskParameters;
	private Properties myProperties;
	private Vector<Record> addrbook;
	private String updaterAction;
	private Record trashCan;

	public AddressBookUpdater() {
		this.addressBookDBA = ConnectionManager.getAccessTo("m_address_book");
		taskParameters = new Hashtable();
		addrbook = new Vector<Record>();
		this.myProperties = PluginManagerIld.getProperties("MailBomber");
	}

	protected void checkParameters() throws Exception {
		TEntry[] nodes = TStringUtils.getTEntryGroup("mail.book.type_");
		// mandatory parameters
		for (TEntry te : nodes) {
			SystemLog.log("mail.msg11", "MailBomber", "", te);
			String prp = "mail.updater." + te.getKey() + ".";
			String tn = myProperties.getProperty(prp + "table");
			String wc = myProperties.getProperty(prp + "where");
			wc = wc.equals("") ? null : wc;
			ConnectionManager.getRowCount(tn, wc);
			
			if (!te.getKey().equals("company")) {
				String p = myProperties.getProperty(prp + "parent", null);
				if (p == null) {
					throw new Exception("Mandatory parameter " + prp + "parent not found.");
				}
			}
		}
	}

	/**
	 * resolve the <code>company</code> tier. This method must be call only on <code>create</code> action.
	 * 
	 */
	protected void resolveCompanyNode() {
		Vector<Record> implist = getRecordList("company");
		for (Record imprcd : implist) {
			Record addrcd = getNewRecordAndMap(imprcd, "company");
			addressBookDBA.write(addrcd);
		}
	}

	protected Vector<Record> getRecordList(String node) {
		String prp = "mail.updater." + node + ".";
		String tn = myProperties.getProperty(prp + "table");
		DBAccess dba = ConnectionManager.getAccessTo(tn);
		String wc = myProperties.getProperty(prp + "where");
		wc = wc.equals("") ? null : wc;
		return dba.search(wc, null);
	}

	/**
	 * resolve the <code>m_abparentid</code> field for the address book record argument. this method must be called only
	 * for <code>personal</code> and <code>ounit</code> record tipes. the <code>m_abparentid</code> is determined by the
	 * natural structure of the incoming data. If no parent is found, the record is send to the trash can
	 * 
	 * @param imprcd - imported record
	 * @param addrcd - address book record
	 * @param curnod - current node. <code>personal</code> or <code>ounit</code>
	 */
	protected void resolveParent(Record imprcd, Record addrcd, String curnod) {
		Record prcd = null;
		String prp = "mail.updater." + curnod + ".";

		String fid = myProperties.getProperty(prp + "parent");
		fid = imprcd.getFieldValue(fid).toString();
		String pnod = "";
		if (curnod.equals("personal"))
			pnod = "ounit";
		else if (curnod.equals("ounit"))
			pnod = "company";
		String abwc = "M_ABTYPE = '" + pnod + "' AND M_ABFORMER_ID = '" + fid + "'";
		prcd = addressBookDBA.exist(abwc);
		// set the parent or store in trascan
		if (prcd != null) {
			addrcd.setFieldValue("m_abparentid", prcd.getFieldValue("m_abid"));
		} else {
			addrcd.setFieldValue("m_abparentid", trashCan.getFieldValue("m_abid"));
		}
	}

	/**
	 * Perform the fields map values sustitution setted in updater parameters. This mehtod determine the relation
	 * addressbook record <-> impor record fields and get the field value form import record and store in addressbook
	 * record.
	 * 
	 * @param imprcd - imported record
	 * @param addrcd - mail remitent
	 * @param curnod - current node
	 */
	void mapFields(Record imprcd, Record addrcd, String curnod) {
		String prp = "mail.updater." + curnod + ".";
		for (int c = 0; c < addrcd.getFieldCount(); c++) {
			String tfn = prp + addrcd.getFieldName(c).toLowerCase();
			tfn = myProperties.getProperty(tfn);
			if (tfn != null) {
				String val = imprcd.getFieldValue(tfn).toString();
				addrcd.setFieldValue(c, val);
			}
		}
	}

	/**
	 * resolve the <code>ounit</code> tier. This method must be call only on <code>create</code> action.
	 * 
	 */
	protected void resolveOUnitNode() {
		Vector<Record> implist = getRecordList("ounit");
		for (Record imprcd : implist) {
			Record addrcd = getNewRecordAndMap(imprcd, "ounit");
			resolveParent(imprcd, addrcd, "ounit");
			addressBookDBA.write(addrcd);
		}
	}

	/**
	 * Check the <code>m_abemail</code> field value of the argument. if this field has no mail, log the news to the
	 * user.
	 * 
	 * @param addrcd - address record to check and log
	 */
	private void logNoMail(Record addrcd) {
		if (addrcd.getFieldValue("m_abemail").equals("")) {
			SystemLog.log("mail.msg10", "MailBomber", "", addrcd.getFieldValue("m_abname"),
					addrcd.getFieldValue("m_abformer_id"));
			// addrcd.setFieldValue("m_abemail", addrcd.getFieldValue(0) + "@server.com");
		}
	}

	/**
	 * Create and return a new address book record whit the standar parameters set and the
	 * {@link #mapFields(Record, Record, String)} setted too.
	 * 
	 * @param imprcd - incoming record
	 * @param node - node
	 * @return new record ready to process
	 */
	private Record getNewRecordAndMap(Record imprcd, String node) {
		Record addrcd = addressBookDBA.getModel();
		addrcd.setFieldValue(0, TStringUtils.getRecordId());
		addrcd.setFieldValue("m_ablastupd", System.currentTimeMillis());
		addrcd.setFieldValue("m_abtype", node);
		mapFields(imprcd, addrcd, node);
		return addrcd;
	}

	protected void createPersonalNode() {
		Vector<Record> implist = getRecordList("personal");
		for (Record imprcd : implist) {
			Record addrcd = getNewRecordAndMap(imprcd, "personal");
			resolveParent(imprcd, addrcd, "personal");
			logNoMail(addrcd);
			addressBookDBA.write(addrcd);
		}
	}

	protected void writePersonalNode() {
		long mark = System.currentTimeMillis();
		String prp = "mail.updater.personal.";
		Vector<Record> implist = getRecordList("personal");
		for (Record imprcd : implist) {
			// check addr book for existent list
			String fid = myProperties.getProperty(prp + "m_abformer_id");
			fid = imprcd.getFieldValue(fid).toString();
			String abwc = "M_ABTYPE = 'personal' AND M_ABFORMER_ID = '" + fid + "'";
			Vector<Record> addrlst = addressBookDBA.search(abwc, null);

			// if addrlst is empty, incoming record is a new record, insert
			if (addrlst.isEmpty()) {
				Record addrcd = getNewRecordAndMap(imprcd, "personal");
				resolveParent(imprcd, addrcd, "personal");
				logNoMail(addrcd);
				addressBookDBA.add(addrcd);
			} else {
				// if list is not empty, update m_ablastupd field to mark as view and dont delete
				for (Record addrcd : addrlst) {
					addrcd.setFieldValue("m_ablastupd", System.currentTimeMillis());
					addressBookDBA.update(addrcd);
				}
			}
		}
		// write action second pass: delete all personal record not update
		String abwc = "M_ABTYPE = 'personal' AND m_ablastupd < " + mark;
		Vector<Record> addrlst = addressBookDBA.search(abwc, null);
		for (Record addr : addrlst) {
			addressBookDBA.delete(addr);
		}
	}

	protected void updatePersonalNode() {
		String prp = "mail.updater.personal.";
		Vector<Record> implist = getRecordList("personal");
		for (Record imprcd : implist) {
			// check addr book for existent list
			String fid = myProperties.getProperty(prp + "m_abformer_id");
			fid = imprcd.getFieldValue(fid).toString();
			String abwc = "M_ABTYPE = 'personal' AND M_ABFORMER_ID = '" + fid + "'";
			Vector<Record> addrlst = addressBookDBA.search(abwc, null);
			for (Record addrcd : addrlst) {
				mapFields(imprcd, addrcd, "personal");
				logNoMail(addrcd);
				addressBookDBA.update(addrcd);
			}
		}
	}

	/**
	 * Ensure that trash can recipient is on address book. if exist, leave as it, else, create new 
	 */
	private void ensureTrashCan() {
		trashCan = addressBookDBA.exist("m_abtype = 'trashcan'");
		if (trashCan == null) {
			trashCan = addressBookDBA.getModel();
			trashCan.setFieldValue(0, TStringUtils.getRecordId());
			TEntry te = TStringUtils.getTEntry("mail.book.trashcan");
			trashCan.setFieldValue("m_abtype", "trashcan");
			trashCan.setFieldValue("m_abname", te.getValue());
//			trashCan.setFieldValue("m_ablastupd", 1);
			addressBookDBA.add(trashCan);
		}
	}

	public void createAddressBook() {
		addrbook = addressBookDBA.search(null, null);
		for (Record r : addrbook) {
			addressBookDBA.delete(r);
		}
		ensureTrashCan();
		resolveCompanyNode();
		resolveOUnitNode();
		createPersonalNode();

	}
	@Override
	public void run() {
		try {
			Thread.sleep(250);

			// connection to jdbc
			monitor.setProgress(0, "updater.conn");
			String conp = myProperties.getProperty("mail.connectionProfile", null);
			Record cf = ConnectionManager.getAccessTo("t_connections").exist("T_CNNAME = '" + conp + "'");
			ConnectionManager.connect(cf);

			checkParameters();

			// task parameters
			updaterAction = (String) taskParameters.get(AddressBookUpdater.UPDATER_ACTION);
			ensureTrashCan();

			monitor.setProgress(0, "updater.importing");

			// create from scrach!
			if (updaterAction.equals("create")) {
				createAddressBook();
			}
			// add/delete new/old mail recipient
			if (updaterAction.equals("write")) {
				writePersonalNode();
			}
			// update address book
			if (updaterAction.equals("update")) {
				updatePersonalNode();
			}

			DockingContainer.signalFreshgen(AddressBookTree.class.getName());
			DockingContainer.signalFreshgen(MailLog.class.getName());
			monitor.dispose();
			Alesia.showNotification("mail.notification.msg02", "");
		} catch (Exception ex) {
			monitor.dispose();
			SystemLog.logException(ex);
			Alesia.showNotification("notification.msg00", ex.getMessage());
		}
	}

	@Override
	public Hashtable getTaskParameters() {
		return taskParameters;
	}
	@Override
	public void setTaskParameters(Hashtable parms) {
		taskParameters.putAll(parms);
	}

	@Override
	public void setFuture(Future f, boolean ab) {
		this.monitor = new TProgressMonitor("AddressBookUpdateAction", "AddressBookUpdater", null, true);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				monitor.show(Alesia.mainFrame);
			}
		});
	}
}
