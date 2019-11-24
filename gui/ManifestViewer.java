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
/*			
 * Copyright (c) QQ - All right reserved

 */
package gui;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.*;


import action.*;
import core.*;
import core.datasource.*;

/** este componente presenta el contenido del manifiesto para una actualizacion de la aplicacion.
 * 
 */
public class ManifestViewer extends UIComponentPanel implements ListSelectionListener {
	
	private JList jli_list;
	private JEditorPane jepane;
	private TreeMap description;
	
	/** nueva instancia
	 * 
	 *
	 */
	public ManifestViewer(String dn) {
		super(dn, false);
		Dimension dim = new Dimension(450, 100);
		this.jli_list = new JList();
		jli_list.addListSelectionListener(this);
		this.jepane = new JEditorPane();
		jepane.setEditable(false);
		StyleSheet shee = new StyleSheet();
		try {
			shee.loadRules(new FileReader(TResources.getFile("HtmlEditor.css")), null);
		} catch (Exception e) {
			
		}
		HTMLEditorKit kit = new HTMLEditorKit();
		kit.setStyleSheet(shee);
		jepane.setEditorKit(kit);

		JPanel jp = new JPanel();
		jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
		JScrollPane jsp = new JScrollPane(jli_list); 
		jsp.setPreferredSize(dim);

		JScrollPane jsp1 = new JScrollPane(jepane); 
		jsp1.setPreferredSize(dim);
		
		jp.add(TUIUtils.getInHorizontalBox(
			new Component[] {new JLabel(TStringUtils.getString("update.update"))}, 
			FlowLayout.LEFT));
		jp.add(jsp);
		jp.add(Box.createVerticalStrut(8));
		jp.add(TUIUtils.getInHorizontalBox(
			new Component[] {new JLabel(TStringUtils.getString("update.detail"))}, 
			FlowLayout.LEFT));
		jp.add(jsp1);
		
		add(jp);
		setActionBar(new TAbstractAction[] {new OkAction(this)});
		loadManifest();
	}

	/** carga el archivo de manifiesto foramatea elementos y alamcena en buffer
	 * 
	 */
	private void loadManifest() {
		try {
			BufferedInputStream bis = new BufferedInputStream(
				new FileInputStream(SystemVariables.getStringVar("updateManifest")));
			PropertyResourceBundle lrb = new PropertyResourceBundle(bis);
			bis.close();
			this.description = new TreeMap();
			TreeMap tm = new TreeMap();
			Enumeration kls = lrb.getKeys();
			while(kls.hasMoreElements()) {
				String k = (String) kls.nextElement();
				if (k.startsWith("item")) {
					String[] kv = lrb.getString(k).split(";");
					if (kv.length > 1) {
						kv[0] = "<html>" + kv[0] + "</html>";
						kv[1] = "<html>" + kv[1] + "</html>";
						tm.put(k, new TEntry(k, kv[0]));
						description.put(k, kv[1]);
					}
				}
			}
			DefaultListModel dlm = new DefaultListModel();
			Vector vec = new Vector(tm.keySet());
			for (int i = 0; i < vec.size(); i++) {
				dlm.addElement(tm.get(vec.elementAt(i)));
			}
			this.jli_list.setModel(dlm);
		} catch (Exception e) {
			SystemLog.logException(e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		TEntry sele = (TEntry) jli_list.getSelectedValue();
		if (sele != null) {
			jepane.setText((String) description.get(sele.getKey()));
		} else {
			jepane.setText("");
		}
	}
}
