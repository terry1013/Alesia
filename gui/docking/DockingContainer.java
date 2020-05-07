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
package gui.docking;

import java.awt.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;

import com.alee.extended.statusbar.*;
import com.alee.extended.transition.*;
import com.alee.laf.button.*;
import com.alee.laf.label.*;
import com.alee.managers.style.*;
import com.alee.utils.*;

import core.*;
import gui.*;

public class DockingContainer extends JPanel {

	private ComponentTransition transitionPanel;
	private JPanel contentPanel;
	private TLeftPanel leftPanel;

	public void setContentPanel(JPanel cpanel) {
		setVisible(false);
		remove(contentPanel);
		contentPanel = null;
		contentPanel = cpanel;
		add(contentPanel, BorderLayout.CENTER);

		// auto select listener for model select property
		List<Container> cnts = SwingUtils.collectAllContainers(contentPanel);
		for (Container cnt : cnts) {
			if (cnt instanceof TUIListPanel)
				((TUIListPanel) cnt).init();
			
			if (cnt instanceof PropertyChangeListener && cnt instanceof TUIListPanel) {
				PropertyChangeListener pcl = (PropertyChangeListener) cnt;
				addChangeListener(TUIListPanel.MODEL_SELECTED, pcl);
			}
		}
		setVisible(true);
	}

	/**
	 * add the listener all containers instances of <code>soruceClazz</code> inside of this component content panel.
	 * 
	 * @param sourceClazz - the class source of the property
	 * @param propertyName - the name
	 * @param listener - the listener interested in recive notification
	 */
	public void addChangeListener(String propertyName, PropertyChangeListener listener) {
		List<Container> cnts = SwingUtils.collectAllContainers(this);
		// avoid to add listerner to the "listener" itselft and other class disticg to TUIListPanel
		cnts.removeIf(cnt -> cnt == listener || !(cnt instanceof TUIListPanel));
		for (Container source : cnts) {
			// avoid mutiple propertyChange invocation on listener
			source.removePropertyChangeListener(propertyName, listener);
			source.addPropertyChangeListener(propertyName, listener);
		}

		// for (String cn : vs) {
		// // check not addPropertyChangeListener to myself
		// if (!dview.getName().equals(cn)) {
		// addPropertyChangeListener(cn, TConstants.RECORD_SELECTED, lst);
		// addPropertyChangeListener(cn, TConstants.FIND_TEXT, lst);
		// addPropertyChangeListener(cn, TConstants.LOG_MESSAGE, lst);
		// }
		// }
		// }

	}

	public TLeftPanel getLeftPanel() {
		return leftPanel;
	}

	public DockingContainer() {
		super(new BorderLayout());
		leftPanel = new TLeftPanel(this);

		contentPanel = new JPanel();
		// contentPanel.setBackground(Color.PINK);

		add(leftPanel, BorderLayout.WEST);
		add(getStatusBar(), BorderLayout.SOUTH);
		add(contentPanel, BorderLayout.CENTER);

		transitionPanel = new ComponentTransition();
		// transitionPanel.setContent(rootWindow);
		// transitionPanel.setContent(backgroundPanel);

		// Transition effect
		// final CurtainTransitionEffect effect = new CurtainTransitionEffect();
		// effect.setDirection(com.alee.extended.transition.effects.Direction.down);
		// effect.setType(CurtainType.fade);
		// effect.setSpeed(9);
		// transitionPanel.setTransitionEffect(effect);
	}

	public static ArrayList<WebButton> createNavButtons(Color toColor, String style, Font font, Action... actions) {
		int size = 20;
		ArrayList<WebButton> list = new ArrayList<>();
		TUIUtils.overRideIcons(size, toColor, actions);
		for (Action action : actions) {
			WebButton wb = new WebButton(StyleId.of(style), action);
			if (font != null) {
				wb.setFont(font);
			}
			// TODO: incorporate security
			list.add(wb);
		}
		return list;
	}

	/**
	 * Return a {@link JLabel} formatted to present warning message about problems with autorization for user
	 * 
	 * @param txt - text to format message
	 * 
	 * @return {@link JLabel}
	 */
	private static JLabel getLockPanel(String txt) {
		// JPanel jp = new JPanel(new BorderLayout());
		String msg = MessageFormat.format(TStringUtils.getString("security.msg01"), txt);
		JLabel jl = new JLabel(msg, TResources.getIcon("lock-panel", 48), JLabel.CENTER);
		jl.setVerticalTextPosition(JLabel.BOTTOM);
		jl.setHorizontalTextPosition(JLabel.CENTER);
		jl.setBorder(new EmptyBorder(4, 4, 4, 4));
		return jl;
	}

	/**
	 * crate and return status bar
	 * 
	 * @return - webstatusbar
	 */
	private WebStatusBar getStatusBar() {
		WebStatusBar bar = new WebStatusBar();
		WebLabel pd = new WebLabel(TStringUtils.getAboutAppShort(), TResources.getSmallIcon("alpha"));
		bar.add(pd);
		bar.addSpacing();
		bar.addToEnd(Alesia.manager.getProgressBar());
		return bar;
	}
}
