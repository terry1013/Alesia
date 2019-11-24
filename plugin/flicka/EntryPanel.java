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
import java.text.*;
import java.util.*;

import javax.swing.*;

import core.datasource.*;

public class EntryPanel extends JPanel implements DockingComponent, ActionListener {

	ServiceRequest request;
	private JTextArea leftTextArea, resultTextArea;
	private JButton testLeft, send;
	private DBAccess dbAccess;

	/**
	 * new instance
	 */
	public EntryPanel() {
		super(new BorderLayout());
		this.dbAccess = ConnectionManager.getAccessTo("reslr");
	}

	@Override
	public void init() {
		Font f = new Font("Courier New", Font.PLAIN, 12);
		this.leftTextArea = new JTextArea(20, 100);
		leftTextArea.setFont(f);
		this.resultTextArea = new JTextArea(20, 100);
		resultTextArea.setFont(f);

		this.testLeft = new JButton("test Left");
		this.testLeft.addActionListener(this);
		this.send = new JButton("Send");
		this.send.addActionListener(this);

		JPanel jp1 = new JPanel(new GridLayout(1, 2, 4, 4));
		jp1.add(new JScrollPane(leftTextArea));
		jp1.add(new JScrollPane(resultTextArea));

		JPanel jpb = new JPanel(new GridLayout(0, 2));
		jpb.add(testLeft);
		jpb.add(send);

		JPanel jp2 = new JPanel(new BorderLayout(4, 4));
		jp2.add(jp1, BorderLayout.CENTER);
		jp2.add(jpb, BorderLayout.SOUTH);

		add(jp2, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		resultTextArea.setText("");

		if (e.getSource() == testLeft) {
			parseLeft();
		}
		if (e.getSource() == send) {
			sendToFile();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

	}

	private void sendToFile() {
		Vector<Record> leftList = parseLeft();
		for (Record rcd : leftList) {
			print(rcd);
			boolean ok = dbAccess.write(rcd);
			if (!ok) {
				resultTextArea.append("Error writing record");
				return;
			}
		}
	}

	/**
	 * previous parse rutine before jockey weight was put on the right side of the line
	 * 
	 * @return
	 */
	private Vector<Record> parseLeft() {
		Vector<Record> list = new Vector<Record>();
		String l = leftTextArea.getText();
		l = l.replaceAll("[']", " ");
		int lincnt = 0;
		String[] lines = l.split("\n");
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		try {
			for (lincnt = 0; lincnt < lines.length; lincnt++) {
				Record mod = dbAccess.getModel();
				// mark header group
				if (lines[lincnt].startsWith("Obs.")) {
					// put the horse gender at rigth of Obs. mark (M or F)
					resultTextArea.append("gender and racedate\n");
					mod.setFieldValue("rehorsegender", lines[lincnt].split(" ")[1]);
					mod.setFieldValue("redate", sdf.parseObject(lines[lincnt + 2]));

					resultTextArea.append("race data line " + lines[lincnt + 3] + "\n");
					String fields[] = lines[lincnt + 3].split(" ");
					mod.setFieldValue("rerace", new Integer(fields[2].substring(1, fields[2].length() - 1)));
					mod.setFieldValue("redistance", new Integer(fields[4]));
					mod.setFieldValue("reracetime", new Double(fields[8]));

					resultTextArea.append("Serie data line " + lines[lincnt + 4] + "\n");
					fields = lines[lincnt + 4].split(" ");
					mod.setFieldValue("reserie", fields[1]);
					mod.setFieldValue("repartial1", new Double(fields[3]));
					mod.setFieldValue("repartial2", new Double(fields[5]));
					mod.setFieldValue("repartial3", new Double(fields[7]));

					resultTextArea.append("race result. do until next Obs mark or end of lines\n");
					int newlincnt = lincnt + 6;
					while (newlincnt < lines.length) {
						String newline = lines[newlincnt];
						newlincnt++;
						if (newline.startsWith("Obs.")) {
							break;
						}
						newline = newline.replaceAll(",", ".");
						fields = newline.split(" ");
						Record rcd = new Record(mod);

						resultTextArea.append("reend_pos\n");
						rcd.setFieldValue("reend_pos", new Integer(fields[0]));
						// check until double parseable field. the gap between is the horse name
						resultTextArea.append("rehorse & rejockey_weight & number\n");
						String tmp = "";
						int fldcnt = 0;
						for (int j = 1; j < 5; j++) {
							try {
								Double d = new Double(fields[j]);
								rcd.setFieldValue("rehorse", tmp.trim());
								rcd.setFieldValue("rejockey_weight", d);
								fldcnt = j;
								break;
							} catch (Exception e) {
								// build horsename
								tmp += fields[j] + " ";
							}
						}
						tmp = "";
						resultTextArea.append("restar_lane\n");
						rcd.setFieldValue("restar_lane", fields[++fldcnt]);
						
						resultTextArea.append("recps\n");
						rcd.setFieldValue("recps", fields[++fldcnt]);
						
						resultTextArea.append("rerating\n");
						rcd.setFieldValue("rerating", new Integer(fields[++fldcnt]));

						resultTextArea.append("rejockey\n");
						tmp = "";
						for (int j = fldcnt + 1; j < fields.length; j++) {
							try {
								Double dv = new Double(fields[j]);
								rcd.setFieldValue("rejockey", tmp.substring(0, tmp.length() - 2));
								rcd.setFieldValue("redividend", dv);
								fldcnt = j;
								break;
							} catch (Exception e) {
								// build rejockey
								tmp += fields[j] + " ";
							}
						}
						list.add(rcd);
						resultTextArea.append("parse ok for " + rcd.getFieldValue("rerace") + ": " +rcd.getFieldValue("rehorse")+ "\n");
					}
					lincnt = newlincnt - 2;
				}
			}

		} catch (Exception e) {
			resultTextArea.append(e.getMessage() + "\n");
			return null;
		}
		resultTextArea.setText("\nParse Left data are Ok!" + "\n");
		return list;
	}

	/**
	 * actual parse routine
	 * 
	 * @return
	 */
	private Vector<Record> parseLeft2() {
		Vector<Record> list = new Vector<Record>();
		String l = leftTextArea.getText();
		l = l.replaceAll("[']", " ");
		int lincnt = 0;
		String[] lines = l.split("\n");
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		try {
			for (lincnt = 0; lincnt < lines.length; lincnt++) {
				Record mod = dbAccess.getModel();
				// mark header group
				if (lines[lincnt].startsWith("Obs.")) {
					// put the horse gender at rigth of Obs. mark (M or F)
					resultTextArea.append("gender and racedate\n");
					mod.setFieldValue("rehorsegender", lines[lincnt].split(" ")[1]);
					mod.setFieldValue("redate", sdf.parseObject(lines[lincnt + 2]));

					resultTextArea.append("race data line " + lines[lincnt + 3] + "\n");
					String fields[] = lines[lincnt + 3].split(" ");
					mod.setFieldValue("rerace", new Integer(fields[2].substring(1, fields[2].length() - 1)));
					mod.setFieldValue("redistance", new Integer(fields[4]));
					mod.setFieldValue("reracetime", new Double(fields[8]));

					resultTextArea.append("Serie data line " + lines[lincnt + 4] + "\n");
					fields = lines[lincnt + 4].split(" ");
					mod.setFieldValue("reserie", fields[1]);
					mod.setFieldValue("repartial1", new Double(fields[3]));
					mod.setFieldValue("repartial2", new Double(fields[5]));
					mod.setFieldValue("repartial3", new Double(fields[7]));

					resultTextArea.append("race result. do until next Obs mark or end of lines\n");
					int newlincnt = lincnt + 6;
					while (newlincnt < lines.length) {
						String newline = lines[newlincnt];
						newlincnt++;
						if (newline.startsWith("Obs.")) {
							break;
						}
						newline = newline.replaceAll(",", ".");
						fields = newline.split(" ");
						Record rcd = new Record(mod);

						resultTextArea.append("reend_pos\n");
						rcd.setFieldValue("reend_pos", new Integer(fields[0]));
						// check until double parseable field. the gap between is the horse name
						resultTextArea.append("rehorse & rejockey_weight\n");
						String tmp = "";
						int fldcnt = 0;
						for (int j = 1; j < 5; j++) {
							fldcnt = j;
							try {
								Integer hn = new Integer(fields[j]);
								rcd.setFieldValue("rehorse", tmp.trim());
								rcd.setFieldValue("rehorsenumber", hn);
								break;
							} catch (Exception e) {
								// build horsename
								tmp += fields[j] + " ";
							}
						}
						tmp = "";
						// cps
						resultTextArea.append("recps\n");
						rcd.setFieldValue("recps", fields[++fldcnt]);

						// rating
						resultTextArea.append("rerating\n");
						rcd.setFieldValue("rerating", new Integer(fields[++fldcnt]));

						// look for the jockey name. the nex double is dividend
						resultTextArea.append("rejockey\n");
						tmp = "";
						for (int j = fldcnt; j < fields.length; j++) {
							if (Character.isLetter(fields[j].charAt(0))) {
								tmp += fields[j] + " ";
								fldcnt++;
							}
						}
						rcd.setFieldValue("rejockey", tmp.substring(0, tmp.length() - 2));

						resultTextArea.append("redividend\n");
						rcd.setFieldValue("redividend", new Double(fields[++fldcnt]));
						resultTextArea.append("restar_lane\n");
						rcd.setFieldValue("restar_lane", new Integer(fields[++fldcnt]));
						resultTextArea.append("rejockey_weight\n");
						rcd.setFieldValue("rejockey_weight", new Double(fields[++fldcnt]));
						list.add(rcd);

						resultTextArea.append("parse ok for " + rcd.getFieldValue("rerace") + ": " +rcd.getFieldValue("rehorse")+ "\n");
					}
					lincnt = newlincnt - 2;
				}
			}

		} catch (Exception e) {
			resultTextArea.append(e.getMessage() + "\n");
			return null;
		}
		resultTextArea.setText("\nParse Left data are Ok!" + "\n");
		return list;
	}
	private void print(Record r) {
		String txt = "";
		for (int c = 0; c < r.getFieldCount(); c++) {
			txt = (r.getFieldName(c) + "               ").substring(0, 20);
			txt += ">" + r.getFieldValue(c) + "<\n";
			resultTextArea.append(txt);
		}
	}
}
