/*******************************************************************************
 * Copyright (C) 2017 terry.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     terry - initial API and implementation
 ******************************************************************************/
package plugin.flicka;

import gui.docking.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;

import core.datasource.*;

public class EntryPanelFromMagazine extends JPanel implements DockingComponent, ActionListener {

	ServiceRequest request;
	private JTextArea leftTextArea, rightTextArea, resultTextArea;
	private JTextField maedate, maedistance, maerace, maesex;
	private String gaedate, gaedistance, gaerace, gaesex;
	private JButton testLeft, testRight, send;
	private DBAccess dbAccess;
	private GregorianCalendar calendar;

	/**
	 * new instance
	 */
	public EntryPanelFromMagazine() {
		super(new BorderLayout());
		this.dbAccess = ConnectionManager.getAccessTo("magazine");
	}

	@Override
	public void init() {
		this.calendar = new GregorianCalendar();
		Font f = new Font("Courier New", Font.PLAIN, 12);
		this.leftTextArea = new JTextArea(20, 100);
		leftTextArea.setFont(f);
		this.rightTextArea = new JTextArea(20, 100);
		rightTextArea.setFont(f);
		this.resultTextArea = new JTextArea(20, 100);
		resultTextArea.setFont(f);

		this.maedate = new JTextField();
		this.maedistance = new JTextField();
		this.maerace = new JTextField();
		this.maesex = new JTextField();

		this.testLeft = new JButton("test Left");
		this.testLeft.addActionListener(this);
		this.testRight = new JButton("test Right");
		this.testRight.addActionListener(this);
		this.send = new JButton("Send");
		this.send.addActionListener(this);

		JPanel jp1 = new JPanel(new GridLayout(1, 2, 4, 4));
		jp1.add(new JScrollPane(leftTextArea));
		jp1.add(new JScrollPane(rightTextArea));

		JPanel jpb = new JPanel(new GridLayout(0, 11));
		jpb.add(new JLabel("Date: ", JLabel.RIGHT));
		jpb.add(maedate);
		jpb.add(new JLabel("Distance: ", JLabel.RIGHT));
		jpb.add(maedistance);
		jpb.add(new JLabel("Race: ", JLabel.RIGHT));
		jpb.add(maerace);
		jpb.add(new JLabel("Sex: ", JLabel.RIGHT));
		jpb.add(maesex);
		jpb.add(testLeft);
		jpb.add(testRight);
		jpb.add(send);

		JPanel jp2 = new JPanel(new BorderLayout(4, 4));
		jp2.add(jp1, BorderLayout.CENTER);
		jp2.add(jpb, BorderLayout.SOUTH);

		JPanel jp3 = new JPanel(new GridLayout(2, 1, 4, 4));
		jp3.add(jp2);
		jp3.add(new JScrollPane(resultTextArea));

		add(jp3, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		resultTextArea.setText("");

		// check mandatory fields
		gaedate = maedate.getText();
		gaedistance = maedistance.getText();
		gaerace = maerace.getText();
		gaesex = maesex.getText();
		if (gaedate.equals("") || gaedistance.equals("") || gaerace.equals("") || gaesex.equals("")) {
			resultTextArea.append("Mandatory entry fields are required !!!\n");
			return;
		}

		if (e.getSource() == testLeft) {
			parseLeft();
		}
		if (e.getSource() == testRight) {
			parseRight();
		}
		if (e.getSource() == send) {
			parseBoth();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

	}

	private void parseBoth() {
		Vector<Record> leftList = parseLeft();
		Vector<Vector> rightList = parseRight();
		int lcnt = -1;
		for (Vector rrcds : rightList) {
			lcnt++;
			for (Object obj : rrcds) {
				Record rrcd = (Record) obj;
				Record lrcd = leftList.elementAt(lcnt);
				for (int c = 0; c < 13; c++) {
					rrcd.setFieldValue(c, lrcd.getFieldValue(c));
				}

				// write only if, for this horse, ther is no historical race already in file
				String w = "maehorsename = '" + rrcd.getFieldValue("maehorsename") + "' AND mahrace = '"
						+ rrcd.getFieldValue("mahrace") + "'";
				if (dbAccess.exist(w) == null) {
					print(rrcd);
					dbAccess.write(rrcd);
				}
			}
		}
	}

	private Vector<Vector> parseRight() {
		Vector<Vector> list = new Vector<Vector>();
		Vector<Record> records = new Vector<Record>();
		String line = "";
		int ilin = 0, newcnt = 0;
		String fields[] = null;
		try {
			String l = rightTextArea.getText();
			l = l.replaceAll("[']", " ");
			String[] lines = l.split("\n");
			newcnt = -1;
			for (ilin = 0; ilin < lines.length; ilin++) {
				line = lines[ilin].trim();
				// mark of group ends
				if (line.startsWith("De  ")) {
					records = new Vector<Record>();
					list.add(records);
					for (int j = ilin - 1; (j > (ilin - 5) && j > -1); j--) {
						Record mod = dbAccess.getModel();
						line = lines[j].trim();
						if (line.equals("D E B U T A N T E")) {
							// all historical group has at least 1 element (even debutant)
							records.add(mod);
							break;
						}
						if (line.startsWith("De  ")) {
							break;
						}
						fields = line.split(" ");
						mod.setFieldValue("mahrace", fields[0]);
						String hrace = fields[0].substring(1);
						// fix date
						String ddmm[] = fields[1].split("/");
						int y = calendar.get(GregorianCalendar.YEAR);
						java.sql.Date tmpd = java.sql.Date.valueOf(y + "-" + ddmm[1] + "-" + ddmm[0]);
						if (Integer.valueOf(hrace) > Integer.valueOf(gaerace)) {
							tmpd = java.sql.Date.valueOf((y - 1) + "-" + ddmm[1] + "-" + ddmm[0]);
						}
						mod.setFieldValue("mahracedate", tmpd);

						mod.setFieldValue("mahhorseweight", fields[2]);
						mod.setFieldValue("mahdistance", fields[3]);
						mod.setFieldValue("mahlane", fields[4]);
						mod.setFieldValue("mahend_pos", fields[5]);
						mod.setFieldValue("mahpartials", fields[6]);
						mod.setFieldValue("mahins", fields[7]);

						// jockey name Initials + lastname
						String jn = fields[8] + " " + fields[9] + " " + fields[10];
						jn = jn.substring(0, jn.indexOf(","));
						mod.setFieldValue("mahjockeyname", jn);

						// counter position based on comma detection (depending of jockey name)
						newcnt = (fields[9].indexOf(",") > 1) ? 10 : 11;

						// winner name may be 1 2 or 3 words
						String wn = fields[newcnt++];
						try {
							Integer.parseInt(fields[newcnt]);
						} catch (Exception e) {
							wn += " " + fields[newcnt];
							newcnt++;
							try {
								Integer.parseInt(fields[newcnt]);
							} catch (Exception e1) {
								wn += " " + fields[newcnt];
								newcnt++;
							}
						}
						mod.setFieldValue("mahwinner", wn);
						mod.setFieldValue("mahjockeyweight", fields[newcnt++]);
						mod.setFieldValue("mahcps", fields[newcnt++]);
						mod.setFieldValue("mahserie", fields[newcnt++]);
						mod.setFieldValue("mahrating", fields[newcnt++]);
						mod.setFieldValue("mahwinnertime", fields[newcnt++]);
						mod.setFieldValue("mahhorsetime", fields[newcnt++]);
						records.add(mod);
						print(mod);
					}
				}
			}

		} catch (Exception e) {
			resultTextArea.append(e.getMessage() + "\n");
			for (String string : fields) {
				resultTextArea.append(string + "\n");
			}
			e.printStackTrace();
			return null;
		}
		resultTextArea.append("parseRight Ok" + "\n");
		return list;
	}
	private Vector<Record> parseLeft() {

		Vector<Record> list = new Vector<Record>();
		String line = "";

		try {
			String l = leftTextArea.getText();
			l = l.replaceAll("[']", " ");

			// System.out.println(l.split(System.getProperty("line.separator")).length);
			// System.out.println(l.split("\\r?\\n").length);
			// System.out.println(l.split("\n").length);

			String[] lines = l.split("\n");

			int lineM = 1;
			boolean twolst = false;
			for (int i = 0; i < lines.length; i++) {
				line = lines[i].trim();
				Record mod = dbAccess.getModel();
				if (line.equals(String.valueOf(lineM))) {
					lineM++;
					// mandatory fields
					String ddmmyy[] = gaedate.split("/");
					mod.setFieldValue("maedate", "20" + ddmmyy[2] + "-" + ddmmyy[1] + "-" + ddmmyy[0]);
					mod.setFieldValue("maedistance", gaedistance);
					mod.setFieldValue("maerace", gaerace);
					mod.setFieldValue("maesex", gaesex);

					mod.setFieldValue("maeparents", lines[i - 1]);
					mod.setFieldValue("maelane", lines[i]);
					mod.setFieldValue("maebirthdate", lines[i + 1]);
					mod.setFieldValue("maehorsename", lines[i + 2].replaceAll("[']", ""));

					line = lines[i + 3].substring(6);
					String st = lines[i + 4].trim();
					twolst = false;
					try {
						String s = lines[i + 6].replaceAll(",", ".");
						Double.parseDouble(s);
					} catch (Exception e) {
						line += " " + st;
						twolst = true;
					}
					mod.setFieldValue("maestud", line);

					int nc = twolst ? i + 5 : i + 4;

					mod.setFieldValue("maecoach", lines[nc]);
					mod.setFieldValue("maeunk01", lines[++nc]);
					mod.setFieldValue("maejockeyweight", lines[++nc]);
					mod.setFieldValue("maejockeyname", lines[++nc]);
					list.add(mod);
					print(mod);
				}
			}
		} catch (Exception e) {
			resultTextArea.append(e.getMessage() + "\n");
			return null;
		}
		resultTextArea.append("parseLeft Ok" + "\n");
		return list;
	}

	private void print(Record r) {
		String txt = "";
		for (int c = 0; c < r.getFieldCount(); c++) {
			txt += r.getFieldName(c) + ": " + r.getFieldValue(c) + "<- \t";
		}
		resultTextArea.append(txt + "\n");
	}
}
