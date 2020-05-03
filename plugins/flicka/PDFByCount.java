package plugins.flicka;

import java.awt.event.*;
import java.text.*;

import javax.swing.*;

import action.*;
import core.*;

public class PDFByCount extends ConfirmAction {

	private String upDate;
	private int count;
	private String ctField;

	public PDFByCount(String ctf) {
		super(NO_SCOPE);
		this.ctField = ctf;
		setIcon(ctField);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String par = (String) TPreferences.getPreference("PDFByCount", "Parms", "");
		par = JOptionPane.showInputDialog(Alesia.mainFrame, "Record count and uper date for: " + "\n\nField: " + ctField
				+ "\n\nsepareted by ;", par);
		if (par != null) {
			try {
				String[] ld_up = par.split(";");
				count = Integer.parseInt(ld_up[0]);
				upDate = ld_up[1];
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				sdf.parse(upDate);
				TPreferences.setPreference("PDFByCount", "Parms", par);
				actionPerformed2();
			} catch (ParseException e) {
				JOptionPane.showMessageDialog(Alesia.mainFrame, "Error in input parameters", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	public void actionPerformed2() {
//		Flicka.buildPDistribution(count, upDate, ctField);
	}
}
