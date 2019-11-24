package gui.wlaf;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.beans.*;

import javax.swing.*;

import action.*;

import com.alee.extended.image.*;
import com.alee.extended.menu.*;
import com.alee.utils.*;

import core.*;

public class TWebDynamicMenu extends WebDynamicMenu {

	// TODO: this color must be extrated from l&F uimanager
	protected Color borderColor = new Color(89, 122, 222);
	protected Color disabledBorderColor = new Color(149, 151, 170);

	public void addItem(TAbstractAction taa) {
		TWebDynamicMenuItem item = new TWebDynamicMenuItem(taa);
		items.add(item);
		TWWebImage menuItem = new TWWebImage(taa);
		// menuItem.setEnabled(item.getTAbstractAction().isEnabled());
		menuItem.setMargin(item.getMargin());
		menuItem.addMouseListener(new TWMouseAdapter(taa, menuItem));
		add(menuItem);
	}

	protected class TWWebImage extends WebImage implements PropertyChangeListener {
		private TAbstractAction action;

		TWWebImage(TAbstractAction taa) {
			super(TResources.getIcon((String) taa.getValue(TAbstractAction.ICON_ID), 24));
			this.action = taa;
			setEnabled(action.isEnabled());
			action.addPropertyChangeListener(this);
		}
		@Override
		protected void paintComponent(final Graphics g) {
			final Graphics2D g2d = (Graphics2D) g;
			final Object aa = GraphicsUtils.setupAntialias(g2d);

			final Area outer = new Area(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
			final Ellipse2D.Double inner = new Ellipse2D.Double(2, 2, getWidth() - 4, getHeight() - 4);
			outer.exclusiveOr(new Area(inner));
			// ImageUtils.createDisabledCopy(img)
			g2d.setPaint(isEnabled() ? borderColor : disabledBorderColor);
			g2d.fill(outer);

			g2d.setColor(Color.WHITE);
			g2d.fill(inner);

			GraphicsUtils.restoreAntialias(g2d, aa);

			final BufferedImage currentImage = getCurrentImage();
			final Insets insets = getInsets();
			g2d.drawImage(currentImage, insets.left, insets.top, null);

//			super.paintComponent(g);
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource() == action) {
				setEnabled((Boolean) evt.getNewValue());
			}

		}
	}

	protected class TWMouseAdapter extends MouseAdapter {
		private TAbstractAction action;
		private WebImage menuItem;
		TWMouseAdapter(TAbstractAction taa, WebImage wi) {
			this.action = taa;
			this.menuItem = wi;
		}
		public void mousePressed(final MouseEvent e) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					if (action.isEnabled()) {
						ActionEvent ae = new ActionEvent("", 0, "");
						action.actionPerformed(ae);
					}
				}
			});
			hideMenu(getComponentZOrder(menuItem));
		}

	}
}
