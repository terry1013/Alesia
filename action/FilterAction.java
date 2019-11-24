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
package action;

import gui.*;
import gui.tree.*;

import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.*;

import javax.swing.*;

import org.jdesktop.core.animation.timing.*;
import org.jdesktop.core.animation.timing.Animator.EndBehavior;
import org.jdesktop.core.animation.timing.Animator.RepeatBehavior;
import org.jdesktop.core.animation.timing.interpolators.*;

import com.alee.extended.layout.*;
import com.alee.extended.window.*;
import com.alee.laf.button.*;
import com.alee.laf.label.*;
import com.alee.laf.text.*;

import core.*;

/**
 * this action is responsible for show the input component for filter instances of {@link UIListPanel} or
 * {@link TAbstractTree}. this classes automaticaly append this actions and are available by default.
 * 
 * 180101: 12:15 otrooo año pasa y yo sentado frente a la compu programando !!!!! :'( no me molestaria si estubiera
 * haciendo matematica !! pero nooo :'(
 * 
 * @author terry
 * 
 */
public class FilterAction extends TAbstractAction {

	private WebPopOver popOver;
	private WebTextField filterField;
	private UIComponentPanel panel;
	private WebButton button;

	public FilterAction(UIComponentPanel uip) {
		super(TABLE_SCOPE);
		this.panel = uip;
		putValue(TAbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));


	}

	/**
	 * calculate the {@link Point} required to show a component of dimension <code>des</code> on center of a component
	 * <code>srcc</code>
	 * 
	 * @param srcc - component to show other component in center of
	 * @param des - dimension of minor component
	 * @return {@link Point} to use in setLocation o similar methods
	 */
	public static Point getCenter(Component srcc, Dimension des) {
		Rectangle src = srcc.getBounds();
		Point p = srcc.getLocationOnScreen();
		p.x += src.width / 2;
		p.x -= des.width / 2;
		p.y += src.height / 2;
		p.y -= des.height / 2;
		return p;
	}

	/**
	 * first animation.
	 * 
	 * @param src -
	 */
	public static void pop(WebPopOver src) {
		int z = 4;
		Rectangle r1 = src.getBounds();
		Rectangle r2 = new Rectangle(r1);
		r2.x -= z / 2;
		r2.y -= z / 2;
		r2.width += z;
		r2.height += z;
		// 171231 11:55 Felizz añooo madreeeeeee
		Color c1 = src.getContentBackground();
		TimingTarget ps = PropertySetter.getTarget(src, "ContentBackground", c1, new Color(230, 230, 230));
		Animator animator = new AnimatorBuilder().setInterpolator(new SplineInterpolator(1.00f, 0.00f, 1.00f, 1.00f))
				.addTarget(ps).setRepeatBehavior(RepeatBehavior.REVERSE).setEndBehavior(EndBehavior.HOLD)
				.setRepeatCount(2).setDuration(250, TimeUnit.MILLISECONDS).build();
		animator.start();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object src = arg0.getSource();

		// if source is a button || source is from keybind
		if (src instanceof WebButton) {
			button = (WebButton) src;
			
			// 180116: popover creation here because is necesary know the parent container when action are in jdialog 
			//create only once
			if (popOver == null) {
				this.popOver = new WebPopOver(panel.getRootPane().getParent(), "Filter");
				popOver.setCloseOnFocusLoss(true);
				popOver.setMargin(4);
				popOver.setMovable(false);
				popOver.setLayout(new VerticalFlowLayout());
//				final WebLabel titleLabel = new WebLabel("Filtar");
				//titleLabel.setIcon((ImageIcon) getValue(TAbstractAction.SMALL_ICON));
				//titleLabel.setDrawShade(true);
				//popOver.add(titleLabel);
//				popOver.add(new JSeparator());
				this.filterField = TUIUtils.getWebFindField(this);
				filterField.setBorder(null);
				filterField.addKeyListener(new KeyAdapter() {

					@Override
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
							if (button != null) {
								button.setSelected(!filterField.getText().equals(""));
							}
							popOver.dispose();
						}
					}
				});
				popOver.add(filterField);
			}
			filterField.requestFocus();
			popOver.show(button);
		}

		// filter text
		if (src == filterField) {
			String t = filterField.getText();
			popOver.dispose();
			// do direct method invocation for instnaces of uilistpanel and tabstracttree
			if (panel instanceof UIListPanel) {
				((UIListPanel) panel).filterList(t);
			}
			if (panel instanceof TAbstractTree) {
				((TAbstractTree) panel).filterTree(t);
			}
			// set/unset button to indicate that filter list is active
			button.setSelected(!t.equals(""));
		} else {
			/*
			 * // must come from keybinding, or instance of this action // invokelater to avoid interference between
			 * jpopupmenu and this popup SwingUtilities.invokeLater(new Runnable() {
			 * 
			 * @Override public void run() { Dimension d = popOver.getPreferredSize(); Point p = getCenter(panel, d);
			 * popOver.show(p.x, p.y); pop(popOver); } });
			 */
		}
	}
}
