package plugin.dbtweezer;

import gui.*;
import gui.docking.*;

import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

import javax.management.*;
import javax.script.*;

import core.*;
import core.datasource.*;
import core.tasks.*;

public class DBTweezerTask implements TRunnable {

	private DBAccess scriptListDBA;
	private TProgressMonitor monitor;
	private Hashtable taskParameters;
	private Vector testData = new Vector();
	private Vector<Record> sourceList;
	private ServiceRequest request;
	private boolean isTest;
	private ScriptEngine engine;

	/**
	 * response by this task to point to the servicerresponce when this task is runned for test purpose.
	 */
	public static String TEST_DATA = "testData";
	/**
	 * for test purpose
	 */
	public static String TEST_RECORD = "testRecord";
	/**
	 * indicate the connection profile name to retrive all the task to execute
	 */
	public static String CONNECTION_PROFILE = "connProfile";

	public DBTweezerTask() {
		this.scriptListDBA = ConnectionManager.getAccessTo("s_script");
		taskParameters = new Hashtable();
		testData = new Vector();
		sourceList = new Vector();
		request = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST, "DBTweezerTask", testData);

		ScriptEngineManager factory = new ScriptEngineManager();
		engine = factory.getEngineByName("groovy");
	}

	/**
	 * update the enviorement of this task execution. The envioremet is setted acording tho the stored parameters for
	 * this task
	 */
	private void updateEnviorement() {
		Record srcd = (Record) taskParameters.get(TEST_RECORD);
		isTest = srcd == null ? false : true;
		sourceList.clear();
		if (isTest) {
			testData.clear();
			String tn = (String) srcd.getFieldValue("s_sctable");
			Record mod = ConnectionManager.getAccessTo(tn).getModel();
			request.setParameter(ServiceResponse.RECORD_MODEL, mod);
			// fields description for export
			Hashtable<String, String> f_n = new Hashtable<String, String>();
			for (int c = 0; c < mod.getFieldCount(); c++) {
				f_n.put(mod.getFieldName(c), mod.getFieldName(c));
			}
			request.setParameter(ServiceResponse.RECORD_FIELDS_DESPRIPTION, f_n);
			request.setTableName(tn);
			sourceList.add(srcd);
		} else {
			String cn = (String) taskParameters.get(CONNECTION_PROFILE);
			if (cn == null) {
				throw new NullPointerException("Connnection profile task parameter not found.");
			}
			sourceList = scriptListDBA.search("S_SCCNNAME = '" + cn + "' AND S_SCSTATUS = 'active'", null);
		}
		taskParameters.put(TEST_DATA, request);
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1000);
			monitor.setProgress(0, "task.monitor01");
			long t1 = System.currentTimeMillis();
			updateEnviorement();
			// execute script list
			for (int j = 0; j < sourceList.size(); j++) {
				Record srcd = sourceList.elementAt(j);
				int per = (j + 1) * 100 / sourceList.size();
				String msg = MessageFormat.format(TStringUtils.getString("task.monitor02"),
						srcd.getFieldValue("s_scname"));
				monitor.setProgress(per, msg);
				excecuteStep(srcd);
			}

			monitor.dispose();
			// log total time
			long t2 = System.currentTimeMillis();
			SystemLog.log("dbt.msg10", "DBTweezer", "", TStringUtils.formatSpeed(t2 - t1));

			// update the task parameters
			if (isTest) {
				request.setData(testData);
			}
			Alesia.showNotification("dbt.notification.msg01");
			if (isTest) {
				DockingContainer.signalFreshgen(SourceTableData.class.getName());
			}
			DockingContainer.signalFreshgen(TweezerLog.class.getName());
		} catch (Exception ex) {
			// java.lang.InterruptedException: sleep interrupted not monitoring in case of cancel
			if (!(ex instanceof InterruptedException)) {
				monitor.dispose();
				SystemLog.logException(ex);
				Alesia.showNotification("notification.msg00", ex.getMessage());
			}
		}
	}

	private void excecuteStep(Record srcd) throws Exception {
		Object script = srcd.getFieldValue("s_sctype");
		SystemLog.log("dbt.msg03", "DBTweezer", "", srcd.getFieldValue("s_scstep"), srcd.getFieldValue("s_scname"));
		long t1 = System.currentTimeMillis();
		try {
			if (script.equals("Sql")) {
				executeSQL(srcd);
			}
			if (script.equals("Groovy")) {
				executeGroovy(srcd);
			}
			// log partial time
			long t2 = System.currentTimeMillis();
			SystemLog.log("dbt.msg04", "DBTweezer", "", srcd.getFieldValue("s_scstep"), srcd.getFieldValue("s_scname"),
					(TStringUtils.formatSpeed(t2 - t1)));
		} catch (Exception e) {
			SystemLog.log("dbt.msg05", "DBTweezer", e);
			throw e;
		}
	}

	private void executeSQL(Record scriprcd) throws Exception {
		Statement sts = DBTConnectionManager.getSourceDB().getConnection().createStatement();
		sts.execute((String) scriprcd.getFieldValue("s_scscript"));
	}

	public static void testGroovyScrip(String ttn, String scrip) throws Exception {
		DBAccess targetdba = ConnectionManager.getAccessTo(ttn);
		Record rmod = targetdba.getModel();

		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("groovy");

		// from record to engine vars
		for (int c = 0; c < rmod.getFieldCount(); c++) {
			engine.put(rmod.getFieldName(c), rmod.getFieldValue(c));
		}
		boolean exce = (Boolean) engine.eval(scrip);
		// if script signal ok for update, execute update
		if (exce) {
			// from vars to record
			Bindings bins = engine.getBindings(ScriptContext.ENGINE_SCOPE);
			for (int c = 0; c < rmod.getFieldCount(); c++) {
				String fn = rmod.getFieldName(c);
				rmod.setFieldValue(fn, bins.get(fn));
			}
		}
	}

	private void executeGroovy(Record scriptrcd) throws Exception {
		String ttable = (String) scriptrcd.getFieldValue("s_sctable");
		DBAccess targetdba = ConnectionManager.getAccessTo(ttable);
		String wc = (String) scriptrcd.getFieldValue("s_scwhere");
		wc = wc.trim().equals("") ? null : wc;
		int rc = DBTConnectionManager.getSourceDB().getRowCount(ttable, wc);
		SystemLog.log("dbt.msg07", "DBTweezer", "", rc);
		Vector<Record> rlst = targetdba.search(wc, null);
		for (Record sourcercd : rlst) {
			Record afterrcd = new Record(sourcercd);
			// from record to engine vars
			for (int c = 0; c < sourcercd.getFieldCount(); c++) {
				engine.put(sourcercd.getFieldName(c), sourcercd.getFieldValue(c));
			}
			boolean exce = (Boolean) engine.eval((String) scriptrcd.getFieldValue("s_scscript"));
			// if script signal ok for update, execute update
			if (exce) {
				// from vars to record
				Bindings bins = engine.getBindings(ScriptContext.ENGINE_SCOPE);
				for (int c = 0; c < sourcercd.getFieldCount(); c++) {
					String fn = sourcercd.getFieldName(c);
					afterrcd.setFieldValue(fn, bins.get(fn));
				}

				// isTest? update test vector and do nothing more
				if (isTest) {
					testData.addElement(afterrcd);
				} else {
					targetdba.update(afterrcd);
				}
			}
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
		this.monitor = new TProgressMonitor("DBTweezerAction", "task.monitor.title", f, ab);
		/*
		 * SwingUtilities.invokeLater(new Runnable() {
		 * 
		 * @Override public void run() { monitor.show(Alesia.frame); } });
		 */
		monitor.show(Alesia.mainFrame);
	}
}
