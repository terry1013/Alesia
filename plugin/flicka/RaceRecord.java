package plugin.flicka;

import gui.*;

import javax.swing.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.*;

public class RaceRecord extends AbstractRecordDataInput {

	public static int EVENT = 1;
	public static int BASIC = 2;
	public static int FULL = 3;
	boolean newr;
	int mode;

	public RaceRecord(Record rcd, boolean newr, int mode) {
		super(null, rcd);
		this.newr = newr;
		this.mode = mode;
		// EVENT components:
		if (mode == EVENT || mode == FULL) {
			addInputComponent("redate", TUIUtils.getWebDateField(rcd, "redate"), true, true);
			addInputComponent("rerace", TUIUtils.getJFormattedTextField(rcd, "rerace"), true, true);
			addInputComponent("redistance", TUIUtils.getJFormattedTextField(rcd, "redistance"), true, true);
			addInputComponent("reracetime", TUIUtils.getJFormattedTextField(rcd, "reracetime"), true, true);
			addInputComponent("reserie", TUIUtils.getJTextField(rcd, "reserie"), true, true);
			addInputComponent("repartial1", TUIUtils.getJFormattedTextField(rcd, "repartial1"), true, true);
			addInputComponent("repartial2", TUIUtils.getJFormattedTextField(rcd, "repartial2"), false, true);
			addInputComponent("repartial3", TUIUtils.getJFormattedTextField(rcd, "repartial3"), false, true);
			addInputComponent("repartial4", TUIUtils.getJFormattedTextField(rcd, "repartial4"), false, true);
			addInputComponent("rehorsegender", TUIUtils.getJTextField(rcd, "rehorsegender"), true, true);
		}

		// BASIC components:
		if (mode == BASIC || mode == FULL) {
			addInputComponent("restar_lane", TUIUtils.getJFormattedTextField(rcd, "restar_lane"), true, true);
			addInputComponent("reend_pos", TUIUtils.getJFormattedTextField(rcd, "reend_pos"), true, true);
			TEntry[] ele = Flicka.getElemets("rehorse", "tentry.none");
			TJComboBox jcb = TUIUtils.getJComboBox("ttrehorse", ele, rcd.getFieldValue("rehorse"));
			addInputComponent("rehorse", jcb, true, true);
			ele = Flicka.getElemets("rejockey", "tentry.none");
			jcb = TUIUtils.getJComboBox("ttrejockey", ele, rcd.getFieldValue("rejockey"));
			addInputComponent("rejockey", jcb, true, true);
			addInputComponent("rejockey_weight", TUIUtils.getJFormattedTextField(rcd, "rejockey_weight"), false, true);
			addInputComponent("rerating", TUIUtils.getJFormattedTextField(rcd, "rerating"), false, true);
			addInputComponent("reobs", TUIUtils.getJTextField(rcd, "reobs"), false, true);
			addInputComponent("recps", TUIUtils.getJFormattedTextField(rcd, "recps"), false, true);
			addInputComponent("redividend", TUIUtils.getJFormattedTextField(rcd, "redividend"), false, false);
			addInputComponent("retrainer", TUIUtils.getJTextField(rcd, "retrainer"), false, true);
		}

		JPanel panel = mode == FULL ? getFullInputComponents() : null;
		panel = mode == EVENT ? getEventInputComponents() : panel;
		panel = mode == BASIC ? getElementInputComponents() : panel;
		setDefaultActionBar();
		add(panel);
		preValidate(null);
	}

	private JPanel getFullInputComponents() {
		FormLayout lay = new FormLayout("left:pref, 3dlu, left:pref, 7dlu, left:pref, 3dlu, left:pref", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p,3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu,p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("redate"), cc.xy(1, 1));
		build.add(getInputComponent("redate"), cc.xy(3, 1));
		build.add(getLabelFor("rerace"), cc.xy(5, 1));
		build.add(getInputComponent("rerace"), cc.xy(7, 1));
		build.add(getLabelFor("redistance"), cc.xy(1, 3));
		build.add(getInputComponent("redistance"), cc.xy(3, 3));
		build.add(getLabelFor("reracetime"), cc.xy(5, 3));
		build.add(getInputComponent("reracetime"), cc.xy(7, 3));
		build.add(getLabelFor("reserie"), cc.xy(1, 5));
		build.add(getInputComponent("reserie"), cc.xy(3, 5));
		build.add(getLabelFor("repartial1"), cc.xy(5, 5));
		build.add(getInputComponent("repartial1"), cc.xy(7, 5));
		build.add(getLabelFor("repartial2"), cc.xy(1, 7));
		build.add(getInputComponent("repartial2"), cc.xy(3, 7));
		build.add(getLabelFor("repartial3"), cc.xy(5, 7));
		build.add(getInputComponent("repartial3"), cc.xy(7, 7));
		build.add(getLabelFor("reend_pos"), cc.xy(1, 9));
		build.add(getInputComponent("reend_pos"), cc.xy(3, 9));
		build.add(getLabelFor("rehorsenumber"), cc.xy(5, 9));
		build.add(getInputComponent("rehorsenumber"), cc.xy(7, 9));
		build.add(getLabelFor("rehorsegender"), cc.xy(1, 11));
		build.add(getInputComponent("rehorsegender"), cc.xy(3, 11));

		build.add(getLabelFor("rehorse"), cc.xy(1, 13));
		build.add(getInputComponent("rehorse"), cc.xyw(3, 13, 5));

		build.add(getLabelFor("rejockey"), cc.xy(1, 15));
		build.add(getInputComponent("rejockey"), cc.xyw(3, 15, 5));

		build.add(getLabelFor("rejockey_weight"), cc.xy(1, 17));
		build.add(getInputComponent("rejockey_weight"), cc.xy(3, 17));
		build.add(getLabelFor("restar_lane"), cc.xy(5, 17));
		build.add(getInputComponent("restar_lane"), cc.xy(7, 17));

		build.add(getLabelFor("reunk"), cc.xy(1, 19));
		build.add(getInputComponent("reunk"), cc.xyw(3, 19, 5));

		build.add(getLabelFor("rerating"), cc.xy(1, 21));
		build.add(getInputComponent("rerating"), cc.xy(3, 21));
		build.add(getLabelFor("recps"), cc.xy(5, 21));
		build.add(getInputComponent("recps"), cc.xy(7, 21));
		build.add(getLabelFor("redividend"), cc.xy(1, 23));
		build.add(getInputComponent("redividend"), cc.xy(3, 23));

		build.add(getLabelFor("reobs"), cc.xy(1, 25));
		build.add(getInputComponent("reobs"), cc.xyw(3, 25, 5));
		return build.getPanel();
	}

	private JPanel getElementInputComponents() {
		FormLayout lay = new FormLayout("left:pref, 3dlu, left:pref, 7dlu, left:pref, 3dlu, left:pref", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p,3dlu, p, 3dlu, p, 3dlu, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("restar_lane"), cc.xy(1, 1));
		build.add(getInputComponent("restar_lane"), cc.xy(3, 1));
		build.add(getLabelFor("reend_pos"), cc.xy(5, 1));
		build.add(getInputComponent("reend_pos"), cc.xy(7, 1));

		build.add(getLabelFor("rehorse"), cc.xy(1, 5));
		build.add(getInputComponent("rehorse"), cc.xyw(3, 5, 5));

		build.add(getLabelFor("rejockey"), cc.xy(1, 7));
		build.add(getInputComponent("rejockey"), cc.xyw(3, 7, 5));

		build.add(getLabelFor("rejockey_weight"), cc.xy(1, 9));
		build.add(getInputComponent("rejockey_weight"), cc.xy(3, 9));

		build.add(getLabelFor("rerating"), cc.xy(1, 11));
		build.add(getInputComponent("rerating"), cc.xy(3, 11));
		build.add(getLabelFor("recps"), cc.xy(5, 11));
		build.add(getInputComponent("recps"), cc.xy(7, 11));
		build.add(getLabelFor("redividend"), cc.xy(1, 13));
		build.add(getInputComponent("redividend"), cc.xy(3, 13));

		build.add(getLabelFor("reobs"), cc.xy(1, 15));
		build.add(getInputComponent("reobs"), cc.xyw(3, 15, 5));
		
		build.add(getLabelFor("retrainer"), cc.xy(1, 17));
		build.add(getInputComponent("retrainer"), cc.xyw(3, 17, 5));
		return build.getPanel();
	}

	private JPanel getEventInputComponents() {
		FormLayout lay = new FormLayout("left:pref, 3dlu, left:pref, 7dlu, left:pref, 3dlu, left:pref", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabelFor("redate"), cc.xy(1, 1));
		build.add(getInputComponent("redate"), cc.xy(3, 1));
		build.add(getLabelFor("rerace"), cc.xy(5, 1));
		build.add(getInputComponent("rerace"), cc.xy(7, 1));
		build.add(getLabelFor("redistance"), cc.xy(1, 3));
		build.add(getInputComponent("redistance"), cc.xy(3, 3));
		build.add(getLabelFor("reracetime"), cc.xy(5, 3));
		build.add(getInputComponent("reracetime"), cc.xy(7, 3));
		build.add(getLabelFor("reserie"), cc.xy(1, 5));
		build.add(getInputComponent("reserie"), cc.xy(3, 5));
		build.add(getLabelFor("rehorsegender"), cc.xy(5, 5));
		build.add(getInputComponent("rehorsegender"), cc.xy(7, 5));
		
		build.add(getLabelFor("repartial1"), cc.xy(1, 7));
		build.add(getInputComponent("repartial1"), cc.xy(3, 7));
		build.add(getLabelFor("repartial2"), cc.xy(5, 7));
		build.add(getInputComponent("repartial2"), cc.xy(7, 7));
		build.add(getLabelFor("repartial3"), cc.xy(1, 9));
		build.add(getInputComponent("repartial3"), cc.xy(3, 9));
		build.add(getLabelFor("repartial4"), cc.xy(5, 9));
		build.add(getInputComponent("repartial4"), cc.xy(7, 9));		
		return build.getPanel();
	}

	@Override
	public Record getRecord() {
		Record r = super.getRecord();
		// for new record on EVENT mode, create a dummy record
		if (mode == EVENT && newr) {
			r.setFieldValue("rehorse", "terry");
			// r.setFieldValue("rejockey", "terry");
		}
		// to show simulation icon in list
		// if (isSimulation) {
		// r.setFieldValue("rehorsegender", "S");
		// }
		return r;
	}

	/**
	 * Copy the fileds value from source record to target record. the fields that are copied depend of the ftype
	 * argument {@link #EVENT} or {@link #BASIC} type
	 * 
	 * @param srcd - source record to obtains the fields values
	 * @param targrcd - target record
	 * @param ftype fields to copy
	 */
	public static void copyFields(Record srcd, Record targrcd, int ftype) {
		int scol = ftype == EVENT ? 0 : 11;
		int ecol = ftype == EVENT ? 11 : srcd.getFieldCount();
		for (int c = scol; c < ecol; c++) {
			targrcd.setFieldValue(c, srcd.getFieldValue(c));
		}
	}
}
