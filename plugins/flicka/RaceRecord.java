package plugins.flicka;

import javax.swing.*;

import org.javalite.activejdbc.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import core.*;
import gui.*;

public class RaceRecord extends TUIFormPanel {

	public static int EVENT = 1;
	public static int BASIC = 2;
	public static int FULL = 3;
	boolean newr;
	int mode;
	public RaceRecord(TUIListPanel listPanel, Model model, boolean newr, int mode) {
		this.newr = newr;
		this.mode = mode;
		setModel(model);
		showAditionalInformation(false);
		// EVENT components:
		if (mode == EVENT || mode == FULL) {
			addInputComponent(TUIUtils.getWebDateField(model, listPanel.getColumnMetadata().get("redate")), true, true);
			addInputComponent(TUIUtils.getWebFormattedTextField(model, listPanel.getColumnMetadata().get("rerace")), true,
					true);
			addInputComponent(TUIUtils.getWebFormattedTextField(model, listPanel.getColumnMetadata().get("redistance")),
					true, true);
			addInputComponent(TUIUtils.getWebFormattedTextField(model, listPanel.getColumnMetadata().get("reracetime")),
					true, true);
			addInputComponent(TUIUtils.getJTextField(model, listPanel.getColumnMetadata().get("reserie")), true, true);
			addInputComponent(TUIUtils.getWebFormattedTextField(model, listPanel.getColumnMetadata().get("repartial1")),
					true, true);
			addInputComponent(TUIUtils.getWebFormattedTextField(model, listPanel.getColumnMetadata().get("repartial2")),
					false, true);
			addInputComponent(TUIUtils.getWebFormattedTextField(model, listPanel.getColumnMetadata().get("repartial3")),
					false, true);
			addInputComponent(TUIUtils.getWebFormattedTextField(model, listPanel.getColumnMetadata().get("repartial4")),
					false, true);
			addInputComponent(TUIUtils.getJTextField(model, listPanel.getColumnMetadata().get("rehorsegender")), true,
					true);
		}

		// BASIC components:
		if (mode == BASIC || mode == FULL) {
			addInputComponent(TUIUtils.getWebFormattedTextField(model, listPanel.getColumnMetadata().get("restar_lane")),
					true, true);
			addInputComponent(TUIUtils.getWebFormattedTextField(model, listPanel.getColumnMetadata().get("reend_pos")),
					true, true);
			TEntry[] ele = Flicka.getElemets("rehorse", "tentry.none");
			TWebComboBox jcb = TUIUtils.getTWebComboBox("ttrehorse", ele, model.getString("rehorse"));
			addInputComponent("rehorse", jcb, true, true);
			ele = Flicka.getElemets("rejockey", "tentry.none");
			jcb = TUIUtils.getTWebComboBox("ttrejockey", ele, model.get("rejockey"));
			addInputComponent("rejockey", jcb, true, true);
			addInputComponent(
					TUIUtils.getWebFormattedTextField(model, listPanel.getColumnMetadata().get("rejockey_weight")), false,
					true);
			addInputComponent(TUIUtils.getWebFormattedTextField(model, listPanel.getColumnMetadata().get("rerating")),
					false, true);
			addInputComponent(TUIUtils.getJTextField(model, listPanel.getColumnMetadata().get("reobs")), false, true);
			addInputComponent(TUIUtils.getWebFormattedTextField(model, listPanel.getColumnMetadata().get("recps")), false,
					true);
			addInputComponent(TUIUtils.getWebFormattedTextField(model, listPanel.getColumnMetadata().get("redividend")),
					false, false);
			addInputComponent(TUIUtils.getJTextField(model, listPanel.getColumnMetadata().get("retrainer")), false,
					true);
		}

		JPanel panel = mode == FULL ? getFullInputComponents() : null;
		panel = mode == EVENT ? getEventInputComponents() : panel;
		panel = mode == BASIC ? getElementInputComponents() : panel;
		setBodyComponent(panel);
		setFooterActions("acept", "cancel");
		preValidate();
	}

	private JPanel getFullInputComponents() {
		FormLayout lay = new FormLayout("left:pref, 3dlu, left:pref, 7dlu, left:pref, 3dlu, left:pref", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p,3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu,p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabel("redate"), cc.xy(1, 1));
		build.add(getInputComponent("redate"), cc.xy(3, 1));
		build.add(getLabel("rerace"), cc.xy(5, 1));
		build.add(getInputComponent("rerace"), cc.xy(7, 1));
		build.add(getLabel("redistance"), cc.xy(1, 3));
		build.add(getInputComponent("redistance"), cc.xy(3, 3));
		build.add(getLabel("reracetime"), cc.xy(5, 3));
		build.add(getInputComponent("reracetime"), cc.xy(7, 3));
		build.add(getLabel("reserie"), cc.xy(1, 5));
		build.add(getInputComponent("reserie"), cc.xy(3, 5));
		build.add(getLabel("repartial1"), cc.xy(5, 5));
		build.add(getInputComponent("repartial1"), cc.xy(7, 5));
		build.add(getLabel("repartial2"), cc.xy(1, 7));
		build.add(getInputComponent("repartial2"), cc.xy(3, 7));
		build.add(getLabel("repartial3"), cc.xy(5, 7));
		build.add(getInputComponent("repartial3"), cc.xy(7, 7));
		build.add(getLabel("reend_pos"), cc.xy(1, 9));
		build.add(getInputComponent("reend_pos"), cc.xy(3, 9));
		build.add(getLabel("rehorsenumber"), cc.xy(5, 9));
		build.add(getInputComponent("rehorsenumber"), cc.xy(7, 9));
		build.add(getLabel("rehorsegender"), cc.xy(1, 11));
		build.add(getInputComponent("rehorsegender"), cc.xy(3, 11));

		build.add(getLabel("rehorse"), cc.xy(1, 13));
		build.add(getInputComponent("rehorse"), cc.xyw(3, 13, 5));

		build.add(getLabel("rejockey"), cc.xy(1, 15));
		build.add(getInputComponent("rejockey"), cc.xyw(3, 15, 5));

		build.add(getLabel("rejockey_weight"), cc.xy(1, 17));
		build.add(getInputComponent("rejockey_weight"), cc.xy(3, 17));
		build.add(getLabel("restar_lane"), cc.xy(5, 17));
		build.add(getInputComponent("restar_lane"), cc.xy(7, 17));

		build.add(getLabel("reunk"), cc.xy(1, 19));
		build.add(getInputComponent("reunk"), cc.xyw(3, 19, 5));

		build.add(getLabel("rerating"), cc.xy(1, 21));
		build.add(getInputComponent("rerating"), cc.xy(3, 21));
		build.add(getLabel("recps"), cc.xy(5, 21));
		build.add(getInputComponent("recps"), cc.xy(7, 21));
		build.add(getLabel("redividend"), cc.xy(1, 23));
		build.add(getInputComponent("redividend"), cc.xy(3, 23));

		build.add(getLabel("reobs"), cc.xy(1, 25));
		build.add(getInputComponent("reobs"), cc.xyw(3, 25, 5));
		return build.getPanel();
	}

	private JPanel getElementInputComponents() {
		FormLayout lay = new FormLayout("left:pref, 3dlu, left:pref, 7dlu, left:pref, 3dlu, left:pref", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p,3dlu, p, 3dlu, p, 3dlu, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabel("restar_lane"), cc.xy(1, 1));
		build.add(getInputComponent("restar_lane"), cc.xy(3, 1));
		build.add(getLabel("reend_pos"), cc.xy(5, 1));
		build.add(getInputComponent("reend_pos"), cc.xy(7, 1));

		build.add(getLabel("rehorse"), cc.xy(1, 5));
		build.add(getInputComponent("rehorse"), cc.xyw(3, 5, 5));

		build.add(getLabel("rejockey"), cc.xy(1, 7));
		build.add(getInputComponent("rejockey"), cc.xyw(3, 7, 5));

		build.add(getLabel("rejockey_weight"), cc.xy(1, 9));
		build.add(getInputComponent("rejockey_weight"), cc.xy(3, 9));

		build.add(getLabel("rerating"), cc.xy(1, 11));
		build.add(getInputComponent("rerating"), cc.xy(3, 11));
		build.add(getLabel("recps"), cc.xy(5, 11));
		build.add(getInputComponent("recps"), cc.xy(7, 11));
		build.add(getLabel("redividend"), cc.xy(1, 13));
		build.add(getInputComponent("redividend"), cc.xy(3, 13));

		build.add(getLabel("reobs"), cc.xy(1, 15));
		build.add(getInputComponent("reobs"), cc.xyw(3, 15, 5));

		build.add(getLabel("retrainer"), cc.xy(1, 17));
		build.add(getInputComponent("retrainer"), cc.xyw(3, 17, 5));
		return build.getPanel();
	}

	private JPanel getEventInputComponents() {
		FormLayout lay = new FormLayout("left:pref, 3dlu, left:pref, 7dlu, left:pref, 3dlu, left:pref", // columns
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p"); // rows
		CellConstraints cc = new CellConstraints();
		PanelBuilder build = new PanelBuilder(lay);

		build.add(getLabel("redate"), cc.xy(1, 1));
		build.add(getInputComponent("redate"), cc.xy(3, 1));
		build.add(getLabel("rerace"), cc.xy(5, 1));
		build.add(getInputComponent("rerace"), cc.xy(7, 1));
		build.add(getLabel("redistance"), cc.xy(1, 3));
		build.add(getInputComponent("redistance"), cc.xy(3, 3));
		build.add(getLabel("reracetime"), cc.xy(5, 3));
		build.add(getInputComponent("reracetime"), cc.xy(7, 3));
		build.add(getLabel("reserie"), cc.xy(1, 5));
		build.add(getInputComponent("reserie"), cc.xy(3, 5));
		build.add(getLabel("rehorsegender"), cc.xy(5, 5));
		build.add(getInputComponent("rehorsegender"), cc.xy(7, 5));

		build.add(getLabel("repartial1"), cc.xy(1, 7));
		build.add(getInputComponent("repartial1"), cc.xy(3, 7));
		build.add(getLabel("repartial2"), cc.xy(5, 7));
		build.add(getInputComponent("repartial2"), cc.xy(7, 7));
		build.add(getLabel("repartial3"), cc.xy(1, 9));
		build.add(getInputComponent("repartial3"), cc.xy(3, 9));
		build.add(getLabel("repartial4"), cc.xy(5, 9));
		build.add(getInputComponent("repartial4"), cc.xy(7, 9));
		return build.getPanel();
	}

	@Override
	public Model getModel() {
		Model model = super.getModel();
		// for new record on EVENT mode, create a dummy record
		if (mode == EVENT && newr) {
			model.set("rehorse", "terry");
			// r.setFieldValue("rejockey", "terry");
		}
		// to show simulation icon in list
		// if (isSimulation) {
		// r.setFieldValue("rehorsegender", "S");
		// }
		return model;
	}

	/**
	 * Copy the fileds value from source record to target record. the fields that are copied depend of the ftype
	 * argument {@link #EVENT} or {@link #BASIC} type
	 * 
	 * @param srcd - source record to obtains the fields values
	 * @param targrcd - target record
	 * @param ftype fields to copy
	 */
	public static void copyFields(Model srcM, Model tarM, int ftype) {
		// TODO: Checck later
		// int scol = ftype == EVENT ? 0 : 11;
		// int ecol = ftype == EVENT ? 11 : srcd.getFieldCount();
		// for (int c = scol; c < ecol; c++) {
		// targrcd.setFieldValue(c, srcd.getFieldValue(c));
		// }
	}
}
