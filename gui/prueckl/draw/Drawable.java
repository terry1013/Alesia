package gui.prueckl.draw;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import action.*;

public abstract class Drawable implements Serializable {

	public static class DrawableAction extends TAbstractAction {
		public Class drawableclass;
		public DrawableAction(Class dc) {
			super(TABLE_SCOPE);
			drawableclass = dc;
			setIcon(dc.getSimpleName());
		}
		public void actionPerformed(ActionEvent e) {
			Drawable.activeDrawableClass = drawableclass;
		}
	}

	public static class NoActiveDrawableException extends Exception {

	}

	protected class MouseListener extends MouseInputAdapter {
		public void mouseMoved(MouseEvent e) {
			// System.out.println("mouseMoved");
			if (e.getButton() == MouseEvent.NOBUTTON) {
				if (isInside(e.getPoint())) {
					highlighted = true;
					parent.repaint();
				} else {
					if (highlighted == true) {
						highlighted = false;
						parent.repaint();
					}
				}
			}
		}
	}
	protected static Class activeDrawableClass = null;
	static final long serialVersionUID = -921871210619840442L;
	public static final int HIGHLIGHT_SELECT_ANCHOR_SIZE = 8;
	public static ButtonGroup buttonGroup = new ButtonGroup();
	transient protected Component parent;
	protected boolean selected = false;
	protected boolean highlighted = false;
	private Properties properties = new Properties();

	private long selectAt = 0;
	public Drawable(Component parent) {

		this.parent = parent;
	}
	public static Drawable createDrawable(Object[] args) throws NoActiveDrawableException {
		if (activeDrawableClass == null)
			throw new NoActiveDrawableException();
		try {
			Class[] classArgs = new Class[args.length];
			for (int i = 0; i < args.length; i++) {
				classArgs[i] = args[i].getClass();
			}
			Constructor ct = activeDrawableClass.getConstructor(classArgs);
			return (Drawable) ct.newInstance(args);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static TAbstractAction getAction(Class fc) {
		return new DrawableAction(fc);
	}
	public static AbstractButton getButton(Class fc) {
		JToggleButton b = new JToggleButton(getAction(fc));
		b.setText(fc.getName());
		buttonGroup.add(b);
		return b;
	}

	public static JMenuItem getMenuItem(Class dc) {
		final Class drawableclass = dc;
		JMenuItem m = new JMenuItem(getAction(dc));
		m.setText(dc.getSimpleName());
		m.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (((JMenuItem) e.getSource()).isSelected()
						&& ((DrawableAction) ((JMenuItem) e.getSource()).getAction()).drawableclass == drawableclass) {
					activeDrawableClass = drawableclass;
				}
			}
		});
		return m;
	}
	public static boolean isConnSelected() {
		return activeDrawableClass != null && Conn.class.isAssignableFrom(activeDrawableClass);
	}
	public static boolean isDrawableSelected() {
		return activeDrawableClass != null;
	}
	public static boolean isFigureSelected() {
		return activeDrawableClass != null && Figure.class.isAssignableFrom(activeDrawableClass);
	}
	public abstract void delete();
	public abstract java.awt.Rectangle getBounds();
	public Properties getProperties() {
		return properties;
	}
	/**
	 * Return the selection time in milis or 0 if the figure is not selected
	 * 
	 * @return selection time
	 */
	public long getSelectedAt() {
		return selectAt;
	}

	public void highlight(boolean b) {
		highlighted = b;
	}

	public boolean isHighlighted() {
		return highlighted;
	}
	public abstract boolean isInside(Point p);
	public boolean isSelected() {
		return selected;
	}
	public abstract void paint(Graphics g);
	public void paintHighlightSelectPoint(Graphics g, int x, int y) {
		g.fillRect(x - HIGHLIGHT_SELECT_ANCHOR_SIZE / 2, y - HIGHLIGHT_SELECT_ANCHOR_SIZE / 2,
				HIGHLIGHT_SELECT_ANCHOR_SIZE, HIGHLIGHT_SELECT_ANCHOR_SIZE);
	}
	public void select(boolean s) {
		selected = s;
		selectAt = selected ? System.currentTimeMillis() : 0;
	}
}
