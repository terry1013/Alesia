package plugin.flicka;

import gui.*;
import gui.table.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;

import core.datasource.*;

public class RaceRecordFromTable extends AbstractRecordDataInput {

	boolean newr;
	private JTable jTable;
	private Record rModel;
	private String[] columns;

	public RaceRecordFromTable(Record rcd, boolean newr) {
		super(null, rcd);
		this.newr = newr;
		this.rModel = rcd;
		// table columns
		columns = new String[]{"restar_lane", "rehorse", "rejockey", "rejockey_weight", "reend_pos", "recps",
				"reobs", "retrainer"};

		// table data
		Object[][] data = new Object[14][columns.length];
		for (int r = 0; r < 14; r++) {
			Record trcd = new Record(rcd);
			for (int c = 0; c < columns.length; c++) {
				data[r][c] = trcd.getFieldValue(columns[c]);
			}
		}

		this.jTable = new JTable(data, columns);
		ExcelAdapter ea = new ExcelAdapter(jTable);
		ea.setPasteActionListener(this);
		jTable.setPreferredScrollableViewportSize(new Dimension(950, 250));
		// jTable.getModel().addTableModelListener(this);
		jTable.setCellSelectionEnabled(true);
		// jTable.setColumnSelectionAllowed(true);
		setDefaultActionBar();
		add(new JScrollPane(jTable));
		preValidate(null);
	}

	public void updateRecords() {
		TableModel model = jTable.getModel();
		for (int r = 0; r < model.getRowCount(); r++) {
			if (model.getValueAt(r, 1).toString().length() == 0) {
				continue;
			}
			Record rcd = new Record(rModel);
			rcd.setFieldValue("restar_lane", new Integer(model.getValueAt(r, 0).toString()));
			rcd.setFieldValue("rehorse", model.getValueAt(r, 1).toString());
			rcd.setFieldValue("rejockey", model.getValueAt(r, 2).toString());
			rcd.setFieldValue("rejockey_weight", new Integer(model.getValueAt(r, 3).toString()));
			rcd.setFieldValue("reend_pos", new Integer(model.getValueAt(r, 4).toString()));
			rcd.setFieldValue("recps", new Double(model.getValueAt(r, 5).toString()));
			rcd.setFieldValue("reobs", model.getValueAt(r, 6).toString());
			rcd.setFieldValue("retrainer", model.getValueAt(r, 7).toString());

			ConnectionManager.getAccessTo("reslr").write(rcd);
		}
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		super.actionPerformed(ae);
		DBAccess reslr = ConnectionManager.getAccessTo("reslr");

		TableModel model = jTable.getModel();
		for (int r = 0; r < model.getRowCount(); r++) {
			if (model.getValueAt(r, 1).toString().length() == 0) {
				continue;
			}

			// uppercase for horse names
			String ho = model.getValueAt(r, 1).toString().toUpperCase();
			// check if horse exist. if not, mark
			if (reslr.exist("rehorse = '" + ho + "'") == null) {
				ho = ">>" + ho;
			}
			model.setValueAt(ho, r, 1);

			// Check and format jockey
			String[] jos = model.getValueAt(r, 2).toString().split("[ ]");
			String jo = jos[0]+ " ";
			for (int c = 1; c < jos.length; c++) {
				String j = jos[c];
				jo += (j.length() > 2) ? j+" " : "";
			}
			if (reslr.exist("rejockey = '" + jo + "'") == null) {
				jo = ">>" + jo;
			}
			model.setValueAt(jo, r, 2);

			// change fractions by decimal representation
			String[] cp = model.getValueAt(r, 5).toString().split("[ ]");
			String cps = cp[0];
			if (cp.length > 1) {
				cps += (cp[1].equals("1/4") ? ".25" : "");
				cps += (cp[1].equals("1/2") ? ".50" : "");
				cps += (cp[1].equals("3/4") ? ".75" : "");
				model.setValueAt(cps, r, 5);
			}
		}
	}
}
