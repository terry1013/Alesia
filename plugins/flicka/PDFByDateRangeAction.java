package plugins.flicka;

import java.awt.event.*;
import java.text.*;

import javax.swing.*;

import action.*;
import core.*;

public class PDFByDateRangeAction extends ConfirmAction {

	private String lowDate, upDate;
	private String ctField;
	private String tTable;

	public PDFByDateRangeAction(String ctf, String tt) {
		super(NO_SCOPE);
		this.ctField = ctf;
		this.tTable = tt;
		setIcon(ctField);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String idate = (String) TPreferences.getPreference("FieldReviewAction", "Dates", "");
		String date = JOptionPane.showInputDialog(Alesia.mainFrame, "Lower and Uper date range for: \n\nTable: "
				+ tTable + " Field: " + ctField + "\n\nin yyyy-MM-dd format Separeted by ;", idate);
		if (date != null) {
			try {
				String[] ld_up = date.split(";");
				lowDate = ld_up[0];
				upDate = ld_up[1];
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				sdf.parse(lowDate);
				sdf.parse(upDate);
				TPreferences.setPreference("FieldReviewAction", "Dates", date);
				actionPerformed2();
			} catch (ParseException e) {
				JOptionPane.showMessageDialog(Alesia.mainFrame, "Error in date format. The format must be yyyy-MM-dd",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	public void actionPerformed2() {
		if (tTable.equals("ctable")) {
//			Flicka.buildCTable(lowDate, upDate, ctField);
		} else {
//			Flicka.buildPDistribution(lowDate, upDate, ctField);
		}
	}
}
