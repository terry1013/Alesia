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
 * QQ C.A.
 * Created on 17/08/2005
 *
 */
package gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import core.*;


/**
 * Utilice este Contenedor para colocar componentes enmarcados en un borde que similar a
 * <code>TitledBorder</code> pero con un componente como elemento descriptivo. este elemento
 * (generalmente una instancia de <code>JCheckBox o JRadioButton</code>) determinara el estado
 * (habilidato o desabilitado) del contenido de este panel. 
 */
public class ComponentTitledPane extends JPanel {
	private CompTitledBorder border1;
	private JComponent title;
	private JPanel panel1;
	private boolean inverse;

	/**
	 * nueva instancia
	 * 
	 * @param tit - componente que sera establecido como elemetno descriptivo dentro del borde
	 * @param cmp - contenedor con componentes incorporados dentro de este borde
	 */
	public ComponentTitledPane(JComponent tit, JComponent cnt) {
		this.title = tit;
		border1 = new CompTitledBorder(title);
		this.setBorder(border1);
		this.panel1 = new JPanel(new BorderLayout());
		if (cnt != null) {
			panel1.add(cnt, BorderLayout.NORTH);
		}
		// FIXME: correccion temporal de espaciado sobrante hacia la derecha, falta espcio
		// en le parete posterior
		setLayout(new BorderLayout());
		add(title, BorderLayout.NORTH);
		add(panel1, BorderLayout.CENTER);
		checkTitledComponent();
	}
	
	/** Inverso habilita el componente interno cuanto el componente titulo no esta marcado
	 * 
	 *
	 */
	public void setInverse() {
		inverse = true;
		checkTitledComponent();
	}
	
	/** verifica si es componente titulo es una instancia de <code>JToogleButton</code>
	 * si lo es, se asigna un listener que habilitara o deshabilitara el contenedor asignado
	 * para contener los componentes enmarcados por este "borde"
	 *
	 */
	private void checkTitledComponent() {
		if (title instanceof JToggleButton) {
			JToggleButton togle = (JToggleButton) title;
			togle.addChangeListener(new ChangeListener() {
				/* (non-Javadoc)
				 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
				 */
				public void stateChanged(ChangeEvent e) {
					JToggleButton jtb = (JToggleButton) e.getSource();
					TUIUtils.setEnabled(getContentPane(), inverse ? !jtb.isSelected() : jtb.isSelected());
				}
			});
			TUIUtils.setEnabled(getContentPane(), inverse ? !togle.isSelected() : togle.isSelected());
		}
	}

	/** retorna el compomponente usado como titulo
	 * 
	 * @return - componente
	 */
	public JComponent getTitleComponent() {
		return title;
	}

	/** establece el componente usado como titulo
	 * 
	 * @param newComponent - componente
	 */
	public void setTitleComponent(JComponent newComponent) {
		remove(title);
		add(newComponent);
		border1.setTitleComponent(newComponent);
		title = newComponent;
		checkTitledComponent();
	}

	/** retorna el panel que debe contener los elemento a enmarcar dentro de este panel con
	 * borde. este panel tiene establecido BorderLayout por defecto
	 * 
	 * @return - contenedor
	 */
	public JPanel getContentPane() {
		return panel1;
	}

	/*
	 *  (non-Javadoc)
	 * @see java.awt.Component#doLayout()
	 */
	public void doLayout() {
		Insets insets = getInsets();
		Rectangle rect = getBounds();
		rect.x = 0;
		rect.y = 0;

		Rectangle compR = border1.getComponentRect(rect, insets);
		title.setBounds(compR);
		rect.x += insets.left;
		rect.y += insets.top;
		rect.width -= insets.left + insets.right;
		rect.height -= insets.top + insets.bottom;
		panel1.setBounds(rect);
	}

	/**
	 * extiende <code>TitledBorder</code> para permitir un componente como elemento descriptivo
	 */
	public class CompTitledBorder extends TitledBorder {

		private  JComponent bordercmp;

		/**
		 * nueva instancia
		 * 
		 * @param cmp - componente
		 */
		public CompTitledBorder(JComponent cmp) {
			this(null, cmp, LEFT, TOP);
		}

		/**
		 * nueva instancia
		 * 
		 * @param border - borde
		 */
		public CompTitledBorder(Border border) {
			this(border, null, LEFT, TOP);
		}

		/**
		 * nueva instania
		 * 
		 * @param border - borde
		 * @param component - componente
		 */
		public CompTitledBorder(Border border, JComponent cmp) {
			this(border, cmp, LEFT, TOP);
		}

		/**
		 * nueva instancia
		 * 
		 * @param border - estilo
		 * @param cmp - componente
		 * @param titleJustification - justificacion
		 * @param titlePosition - titulo
		 */
		public CompTitledBorder(Border border, JComponent cmp, int titleJustification,
			int titlePosition) {
			super(border, null, titleJustification, titlePosition, null, null);
			this.bordercmp = cmp;
			if (border == null) {
				this.border = super.getBorder();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.border.Border#paintBorder(java.awt.Component, java.awt.Graphics, int,
		 *      int, int, int)
		 */
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			Rectangle borderR = new Rectangle(x + EDGE_SPACING, y + EDGE_SPACING, width
				- (EDGE_SPACING * 2), height - (EDGE_SPACING * 2));
			Insets borderInsets;
			if (border != null) {
				borderInsets = border.getBorderInsets(c);
			} else {
				borderInsets = new Insets(0, 0, 0, 0);
			}

			Rectangle rect = new Rectangle(x, y, width, height);
			Insets insets = getBorderInsets(c);
			Rectangle compR = getComponentRect(rect, insets);
			int diff;
			switch (titlePosition) {
			case ABOVE_TOP:
				diff = compR.height + TEXT_SPACING;
				borderR.y += diff;
				borderR.height -= diff;
				break;
			case TOP:
			case DEFAULT_POSITION:
				diff = insets.top / 2 - borderInsets.top - EDGE_SPACING;
				borderR.y += diff;
				borderR.height -= diff;
				break;
			case BELOW_TOP:
			case ABOVE_BOTTOM:
				break;
			case BOTTOM:
				diff = insets.bottom / 2 - borderInsets.bottom - EDGE_SPACING;
				borderR.height -= diff;
				break;
			case BELOW_BOTTOM:
				diff = compR.height + TEXT_SPACING;
				borderR.height -= diff;
				break;
			}
			border.paintBorder(c, g, borderR.x, borderR.y, borderR.width, borderR.height);
			Color col = g.getColor();
			g.setColor(c.getBackground());
			g.fillRect(compR.x, compR.y, compR.width, compR.height);
			g.setColor(col);
			bordercmp.repaint();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component,
		 *      java.awt.Insets)
		 */
		public Insets getBorderInsets(Component c, Insets insets) {
			Insets borderInsets;
			if (border != null) {
				borderInsets = border.getBorderInsets(c);
			} else {
				borderInsets = new Insets(0, 0, 0, 0);
			}
			insets.top = EDGE_SPACING + TEXT_SPACING + borderInsets.top;
			insets.right = EDGE_SPACING + TEXT_SPACING + borderInsets.right;
			insets.bottom = EDGE_SPACING + TEXT_SPACING + borderInsets.bottom;
			insets.left = EDGE_SPACING + TEXT_SPACING + borderInsets.left;

			if (c == null || bordercmp == null) {
				return insets;
			}

			int compHeight = 0;
			if (bordercmp != null) {
				compHeight = bordercmp.getPreferredSize().height;
			}

			switch (titlePosition) {
			case ABOVE_TOP:
				insets.top += compHeight + TEXT_SPACING;
				break;
			case TOP:
			case DEFAULT_POSITION:
				insets.top += Math.max(compHeight, borderInsets.top) - borderInsets.top;
				break;
			case BELOW_TOP:
				insets.top += compHeight + TEXT_SPACING;
				break;
			case ABOVE_BOTTOM:
				insets.bottom += compHeight + TEXT_SPACING;
				break;
			case BOTTOM:
				insets.bottom += Math.max(compHeight, borderInsets.bottom) - borderInsets.bottom;
				break;
			case BELOW_BOTTOM:
				insets.bottom += compHeight + TEXT_SPACING;
				break;
			}
			return insets;
		}

		/**
		 * establece el componente a presentar
		 * 
		 * @param component - componente
		 */
		private void setTitleComponent(JComponent cmp) {
			this.bordercmp = cmp;
		}

		/**
		 * retorn el componente usado como titulo
		 * 
		 * @return
		 */
		public JComponent getTitleComponent() {
			return bordercmp;
		}

		/**
		 * calcula y retorna el espacion total que ocupa el borde junto con los componente.
		 * 
		 * @param rect - valor previo calculado
		 * @param borderInsets - insets
		 * @return - rectangulo
		 */
		public Rectangle getComponentRect(Rectangle rect, Insets borderInsets) {
			Dimension compD = bordercmp.getPreferredSize();
			Rectangle compR = new Rectangle(0, 0, compD.width, compD.height);
			switch (titlePosition) {
			case ABOVE_TOP:
				compR.y = EDGE_SPACING;
				break;
			case TOP:
			case DEFAULT_POSITION:
				compR.y = EDGE_SPACING
					+ (borderInsets.top - EDGE_SPACING - TEXT_SPACING - compD.height) / 2;
				break;
			case BELOW_TOP:
				compR.y = borderInsets.top - compD.height - TEXT_SPACING;
				break;
			case ABOVE_BOTTOM:
				compR.y = rect.height - borderInsets.bottom + TEXT_SPACING;
				break;
			case BOTTOM:
				compR.y = rect.height - borderInsets.bottom + TEXT_SPACING
					+ (borderInsets.bottom - EDGE_SPACING - TEXT_SPACING - compD.height) / 2;
				break;
			case BELOW_BOTTOM:
				compR.y = rect.height - compD.height - EDGE_SPACING;
				break;
			}
			switch (titleJustification) {
			case LEFT:
			case DEFAULT_JUSTIFICATION:
				compR.x = TEXT_INSET_H + borderInsets.left;
				break;
			case RIGHT:
				compR.x = rect.width - borderInsets.right - TEXT_INSET_H - compR.width;
				break;
			case CENTER:
				compR.x = (rect.width - compR.width) / 2;
				break;
			}
			return compR;
		}
	}

}
