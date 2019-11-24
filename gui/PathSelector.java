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
package gui;

import gui.docking.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import action.*;

import com.alee.extended.breadcrumb.*;
import com.alee.laf.menu.*;

import core.*;
import core.datasource.*;

/**
 * this class build a instance of {@link WebBreadcrumb} that handle all envioremental variables for the application. to
 * implement this class, the programmer must program the {@link #appendNode(String)} method. this method control the
 * secuense of elements in breadcrum component.
 * 
 * @author terry
 * 
 */
public class PathSelector {

	private final WebBreadcrumb classPath;
	private String actualPath;
	private String initialPath;
	private boolean isAjusting;

	private static PathSelector instance;

	public static String ROOT = "ROOT";
	public static String USER = "USER";
	public static String COMPANY = "COMPANY";
	public static String SCENARIO = "SCENARIO";

	public PathSelector() {
		classPath = new WebBreadcrumb(false);
		classPath.setEncloseLastElement(true);
		classPath.setElementMargin(4, 6, 4, 6);
		classPath.setOpaque(false);
		actualPath = "";
		initialPath = (String) TPreferences.getPreference(TPreferences.LAST_SELECTED_PATH, "", "");
		isAjusting = true;
		appendNode(ROOT);
		TPreferences.setPreference(TPreferences.LAST_SELECTED_PATH, "", actualPath);
		initialPath = "";
		isAjusting = false;
		instance = this;
		// the property for stored path selected is fired in a swingUtilites.invokelater
	}

	private static boolean isNodeSelected(String path, String... nodes) {
		boolean alls = true;
		for (String n : nodes) {
			alls = getNodeValue(path, n) == null ? false : alls;
		}
		return alls;
	}

	/**
	 * check if all required nodes are setted in path
	 * 
	 * @param nodes array of nodes to check
	 * 
	 * @return <code>true</code> iff all nodes are setted inside path
	 */
	public static boolean isNodeSelected(String... nodes) {
		return isNodeSelected(instance.actualPath, nodes);
	}

	/**
	 * return the actual path selected in the active instance of {@link PathSelector}.
	 * 
	 * @return path selected
	 */
	public static String getActualPath() {
		if (instance == null) {
			throw new NullPointerException("There is no active instance of " + PathSelector.class.getName());
		}
		return instance.actualPath;
	}

	/**
	 * find and return the node value of <code>node</code> stored in <code>path</code>
	 * 
	 * @param path - where to look
	 * @param node - value to retrive
	 * @return value of node or <code>null</code>if node is not in path
	 */
	public static String getNodeValue(String path, String node) {
		String[] ns = path.split(";");
		String nv = null;
		for (String s : ns) {
			nv = s.startsWith(node) ? s.split("[=]")[1] : nv;
		}
		return nv;
	}

	/**
	 * Return the node value of <code>node</code> stored in the actual path in active instance
	 * 
	 * @param node - value to retrive
	 * @return value of node or <code>null</code>if node is not in path
	 */
	public static String getNodeValue(String node) {
		return getNodeValue(getActualPath(), node);
	}

	/**
	 * append the node <code>nodeid</code> to the end of breadcrum. This method determnie the correct secuense of
	 * elements at the breadcrum and may require values of previus selected nodes.
	 * 
	 * @param nodeid - node to update. (may contain value also: e.i: user=12
	 */
	public void appendNode(String nodeid) {
		Vector<Record> rlist = new Vector<Record>();

		// root: build user list
		if (nodeid.equals(ROOT)) {
			rlist.add(Session.getUser());
			// String id = (String) Session.getUserFieldValue("id");
			createMenu(rlist, "username", "fullname", USER, TResources.getSmallIcon("user_user"));
		}

		// user=?: build company list
		if (nodeid.startsWith(USER)) {
			rlist = ConnectionManager.getAccessTo("sle_company").search(null, null);
			// createMenu(rlist, "company_id", "name", COMPANY, TResourceUtils.getSmallIcon("sle_company"));
			createMenu(rlist, "id", "name", COMPANY, TResources.getSmallIcon("sle_company"));
		}
		// company=?: build scenario list
		if (nodeid.startsWith(COMPANY)) {
			String cid = (String) getNodeValue(actualPath, COMPANY);
			rlist = ConnectionManager.getAccessTo("sle_scenario").search(
					"COMPANY_ID = '" + cid + "' AND status != 'C'", null);
			createMenu(rlist, "id", "name", SCENARIO, TResources.getSmallIcon("sle_scenario"));
		}
	}

	/**
	 * return the {@link WebBreadcrumb} controled by this instance
	 * 
	 * @return breadrum to path selection
	 */
	public WebBreadcrumb getBreadcrumb() {
		return classPath;
	}

	/**
	 * create the {@link WebPopupMenu} that will be displayed when the user press the {@link WebBreadcrumbButton}
	 * asociated with. every MenuItem execute:
	 * <ul>
	 * <li>update the asociated breadcrumbutton with text and parameters
	 * <li>update the main breadcrum removin the rigth elements to keep up to date the secuense.
	 * <li>fire the {@link TConstants#COMPANY_SELECTED} property.
	 * <li>invoke {@link #appendNode(String)} to load the next sequence.
	 * </ul>
	 * 
	 * @param rlist - list of Record source for menu list
	 * @param keyid - key field name
	 * @param name - description field name
	 * @param node - node identifier
	 * @param ii - decorate icon
	 * 
	 * @return {@link WebMenuItem} in previos selected path or null if no node id was found
	 */
	private void createMenu(Vector<Record> rlist, String keyid, String name, String node, ImageIcon ii) {
		final WebPopupMenu rootMenu = new WebPopupMenu();
		final WebBreadcrumbButton wbbutton = new WebBreadcrumbButton();
		wbbutton.setIcon(ii);
		wbbutton.setText("Seleccione ...");
		wbbutton.setName("");
		wbbutton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				rootMenu.showBelowMiddle(wbbutton);
			}
		});
		// TooltipManager.setTooltip(wbbutton, ii, "Compañias");
		classPath.add(wbbutton);

		String lsnv = getNodeValue(initialPath, node);
		lsnv = (lsnv == null) ? "" : lsnv;
		WebMenuItem lswmi = null;
		for (Record r : rlist) {
			Object key = r.getFieldValue(keyid);
			TEntry te = new TEntry(key, r.getFieldValue(name));

			// build menuitem
			String tek = te.getKey().toString();
			WebMenuItem wmi = new WebMenuItem(tek + ": " + te.getValue().toString());
			// store the node=value
			wmi.setName(node + "=" + tek);
			wmi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					WebMenuItem src = (WebMenuItem) e.getSource();
					String nn = src.getName();
					wbbutton.setText(src.getText());
					wbbutton.setName(nn);
					removeRigthOf(wbbutton);
					// during component initialization, dont fire property nor save property
					if (!isAjusting) {
						TPreferences.setPreference(TPreferences.LAST_SELECTED_PATH, "", actualPath);
						DockingContainer.fireProperty(src, TConstants.PATH_SELECTED, actualPath);
					}
					appendNode(nn);
				}
			});
			// find previous selected
			lswmi = tek.equals(lsnv) ? wmi : lswmi;
			rootMenu.add(wmi);
		}

		// if no item added, add a empty menu to show empty list
		if (rootMenu.getComponentCount() == 0) {
			WebMenuItem e = new WebMenuItem("Vacio");
			e.setEnabled(false);
			rootMenu.add(e);
		}
		// for users menulist, append former user menu actions from main class
		if (node.equals(PathSelector.USER)) {
			rootMenu.add(new JSeparator(JSeparator.HORIZONTAL));
			// rootMenu.add(new PChangePasswordAction());
			rootMenu.add(new SignOut());
			rootMenu.add(new Exit());
		}
		// if any menuitem whose node=value are present in initialPath, execute doClic to load the next breadcrum
		// button. otherwise, execute doclic to the button and the recurseve call is stopped
		if (lswmi == null) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					wbbutton.doClick();
				}
			});
		} else {
			lswmi.doClick();
		}
	}

	/**
	 * remove all elements at the rigth of <code>wbcb</code>. this method also update the actualPath.
	 * 
	 * @param wbcb - new last rigth element
	 */
	private void removeRigthOf(WebBreadcrumbButton wbcb) {
		int sd = 100;
		actualPath = "";
		Component[] cmps = classPath.getComponents();
		for (int j = 0; j < cmps.length; j++) {
			// if button name is found, remove the rest of component at right on breadcrum
			sd = cmps[j].getName().equals(wbcb.getName()) ? j : sd;
			if (sd < j) {
				classPath.remove(cmps[j]);
			} else {
				actualPath += cmps[j].getName() + ";";
			}
		}
	}
}
