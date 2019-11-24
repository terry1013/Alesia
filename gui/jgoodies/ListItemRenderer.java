package gui.jgoodies;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.*;

public final class ListItemRenderer<E> extends ListItemView implements ListCellRenderer<E>, UIResource {

	private final ListCellRenderer<Object> prototypeRenderer = new DefaultListCellRenderer();

	public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected,
			boolean cellHasFocus) {
		Component prototype = prototypeRenderer.getListCellRendererComponent(list, null, index, isSelected,
				cellHasFocus);
		Color foreground = prototype.getForeground();
		Color activePrimaryForeground = !isSelected ? primaryForeground : foreground;
		Color activeStatusForeground = (!isSelected && state != null) ? state : foreground;
		Color activeMetaForeground = (!isSelected && metaForeground != null) ? metaForeground : foreground;
		graphicLabel.setForeground(foreground);
		overlineLabel.setForeground(foreground);
		primaryLabel.setForeground(activePrimaryForeground);
		numberLabel.setForeground(activePrimaryForeground);
		secondaryLabel.setForeground(foreground);
		tertiaryLabel.setForeground(foreground);
		numberUnitLabel.setForeground(foreground);
		statusLabel.setForeground(activeStatusForeground);
		metaLabel.setForeground(activeMetaForeground);
		setBackground(prototype.getBackground());
		return this;
	}

	public boolean isOpaque() {
		Color back = getBackground();
		Component p = getParent();
		if (p != null)
			p = p.getParent();
		boolean colorMatch = (back != null && p != null && back.equals(p.getBackground()) && p.isOpaque());
		return (!colorMatch && super.isOpaque());
	}

	public void revalidate() {
	}

	public void repaint() {
	}

	public void repaint(long tm, int x, int y, int width, int height) {
	}

	public void repaint(Rectangle r) {
	}

	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		if (propertyName == "text"
				|| ((propertyName == "font" || propertyName == "foreground") && oldValue != newValue &&

						getClientProperty("html") != null))
			super.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
	}

	public void firePropertyChange(String propertyName, char oldValue, char newValue) {
	}

	public void firePropertyChange(String propertyName, short oldValue, short newValue) {
	}

	public void firePropertyChange(String propertyName, int oldValue, int newValue) {
	}

	public void firePropertyChange(String propertyName, long oldValue, long newValue) {
	}

	public void firePropertyChange(String propertyName, float oldValue, float newValue) {
	}

	public void firePropertyChange(String propertyName, double oldValue, double newValue) {
	}

	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
	}
}
