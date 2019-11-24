package gui.prueckl.draw;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

public class DrawingPanel extends JPanel implements Externalizable {
	protected class MouseListener extends MouseInputAdapter {
		private boolean transform = false;
		private boolean move = false;
		private boolean create = false;
		private boolean connecting = false;
		private Point lastPoint = null;
		private Point startpoint = null, endpoint = null;
		private int itp;
		Figure figure = null;
		Conn conn = null;
		private DrawingPanel drawingPanel;

		public MouseListener(DrawingPanel dp) {
			this.drawingPanel = dp;
		}
		public void mouseDragged(MouseEvent e) {
			endpoint = e.getPoint();

			// System.out.println("mouseMoved");
			DrawingPanel f = (DrawingPanel) e.getSource();
			if (tracker) {
				trackerRect.width = e.getPoint().x - trackerRect.x;
				trackerRect.height = e.getPoint().y - trackerRect.y;
				repaint();
				return;
			}
			if (connecting) {
				// System.out.println("mouseDragged() conn");
				Figure figure = f.getFigure(e.getPoint());
				if (figure != null)
					conn.setToFigure(figure);
				else {
					conn.setToFigure(null);
					conn.setDragPoint(e.getPoint());
				}
				return;
			}
			if (figure == null)
				return;
			if (create) {
				// System.out.println("transform lastPoint is " + lastPoint);
				figure.setBoundPoints(startpoint, endpoint);
			}
			if (lastPoint != null && move) {
				// System.out.println("move lastPoint is " + lastPoint);
				Point p = e.getPoint();
				figure.moveBy(p.x - lastPoint.x, p.y - lastPoint.y);
				lastPoint = p;
			}
			if (lastPoint != null && transform) {
				// System.out.println("transform lastPoint is " + lastPoint);
				Point p = e.getPoint();
				figure.transform(itp, lastPoint, p);
				lastPoint = p;
			}
		}
		public void mouseMoved(MouseEvent e) {
			// System.out.println("mouseMoved");
			DrawingPanel f = (DrawingPanel) e.getSource();
			Figure newf = f.getFigure(e.getPoint());
			boolean repaint = false;
			if (figure != null && (newf == null || figure != newf)) {
				figure.highlight(false);
				repaint = true;
			}
			figure = newf;
			Conn newc = f.getConn(e.getPoint());
			if (conn != null && (newc == null || conn != newc)) {
				conn.highlight(false);
				repaint = true;
			}
			conn = newc;
			if (e.getButton() == MouseEvent.NOBUTTON) {
				if (figure != null) {
					figure.highlight(true);
					repaint = true;
				} else if (figure != null && figure.isHighlighted()) {
					figure.highlight(false);
					repaint = true;
				}
				if (conn != null) {
					conn.highlight(true);
					repaint = true;
				} else if (conn != null && conn.isHighlighted()) {
					conn.highlight(false);
					repaint = true;
				}
			}
			if (repaint)
				repaint();
		}
		public void mousePressed(MouseEvent e) {
			startpoint = e.getPoint();

			drawingPanel.requestFocus();

			DrawingPanel f = (DrawingPanel) e.getSource();
			figure = f.getFigure(e.getPoint());
			conn = f.getConn(e.getPoint());
			if (e.getButton() == MouseEvent.BUTTON3) {
				JPopupMenu menu = new JPopupMenu();
				final Figure fig = figure;
				final Conn c = conn;
				if (f != null || c != null) {
					menu.add("Delete").addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (fig != null) {
								DrawingPanel.this.figures.remove(fig);
								Conn conn;
								for (Iterator it = DrawingPanel.this.conns.iterator(); it.hasNext();) {
									conn = (Conn) it.next();
									if (conn.getFromFigure() == fig || conn.getToFigure() == fig) {
										it.remove();
									}
								}
							} else if (c != null) {
								DrawingPanel.this.conns.remove(c);
							}
							repaint();
						}
					});
					menu.show(DrawingPanel.this, e.getPoint().x, e.getPoint().y);
				}
				return;
			}
			if (select) {
				if (conn != null) {
					conn.select(!conn.isSelected());
				}
				if (figure != null) {
					figure.select(!figure.isSelected());
				}
			} else {
				f.selectAll(false);
			}
			if (figure == null && conn == null && DrawingPanel.select) {
				f.selectAll(false);
				trackerRect.x = e.getPoint().x;
				trackerRect.y = e.getPoint().y;
				tracker = true;
				return;
			}
			if (Conn.isConnSelected() && figure != null) {
				try {
					// System.out.println("mousePressed() conn");
					conn = Conn.createConn(f, figure);
					lastPoint = e.getPoint();
					conn.setDragPoint(lastPoint);
					f.addConn(conn);
					connecting = true;
					// repaint();
				} catch (Conn.NoActiveDrawableException ex) {
					JOptionPane.showMessageDialog(DrawingPanel.this,
							"Before you can connect figures, you must select one");
				}
				return;
			}
			if (e.getButton() == MouseEvent.BUTTON1 && figure != null) {
				itp = figure.isOnTransformPoint(e.getPoint());
				if (itp != -1) {
					// System.out.println("Start of transforming");
					lastPoint = e.getPoint();
					transform = true;
				} else {
					// System.out.println("Start of moving");
					lastPoint = e.getPoint();
					move = true;
				}
				return;
			}
			if (e.getButton() == MouseEvent.BUTTON1 && figure == null && Drawable.isFigureSelected()) {
				try {
					f.addFigure(figure = Figure.createFigure(f, startpoint));
					create = true;
				} catch (Figure.NoActiveDrawableException ex) {
					JOptionPane.showMessageDialog(DrawingPanel.this,
							"Before you can draw something, you must select one");
				}
			}
		}
		public void mouseReleased(MouseEvent e) {
			endpoint = e.getPoint();

			DrawingPanel f = (DrawingPanel) e.getSource();
			// System.out.println("mouse button was released");
			if (connecting) {
				Figure figure = f.getFigure(e.getPoint());
				if (figure != null) {
					conn.setToFigure(figure);
				} else if (conn.getToFigure() == null) {
					conns.remove(conn);
				}
			}
			startpoint = null;
			endpoint = null;
			lastPoint = null;
			transform = false;
			move = false;
			create = false;
			tracker = false;
			connecting = false;
			conn = null;
			figure = null;
			repaint();
		}
	}

	public static boolean select = false;
	static final long serialVersionUID = 1475630615089553070L;
	private java.awt.Rectangle trackerRect = new java.awt.Rectangle();
	private boolean tracker = false;
	private Vector figures = new Vector();
	private Vector conns = new Vector();
	protected MouseListener mi;

	private ImageIcon backgroundImage = null;
	public DrawingPanel() {
		super();
		this.mi = new MouseListener(this);
		setFocusable(true);
		setLayout(new GridLayout());
		addMouseListener(mi);
		addMouseMotionListener(mi);
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int k = e.getKeyCode();
				int m = e.getModifiersEx();

				// coordenates
				if ((m | k) == (KeyEvent.CTRL_DOWN_MASK | KeyEvent.VK_LEFT)) {
					changeShapeBound(-1, 0, 0, 0);
				}
				if ((m | k) == (KeyEvent.CTRL_DOWN_MASK | KeyEvent.VK_RIGHT)) {
					changeShapeBound(1, 0, 0, 0);
				}
				if ((m | k) == (KeyEvent.CTRL_DOWN_MASK | KeyEvent.VK_UP)) {
					changeShapeBound(0, -1, 0, 0);
				}
				if ((m | k) == (KeyEvent.CTRL_DOWN_MASK | KeyEvent.VK_DOWN)) {
					changeShapeBound(0, 1, 0, 0);
				}
				// width/height
				if ((m | k) == ((KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK) | KeyEvent.VK_LEFT)) {
					changeShapeBound(0, 0, -1, 0);
				}
				if ((m | k) == ((KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK) | KeyEvent.VK_RIGHT)) {
					changeShapeBound(0, 0, 1, 0);
				}
				if ((m | k) == ((KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK) | KeyEvent.VK_UP)) {
					changeShapeBound(0, 0, 0, -1);
				}
				if ((m | k) == ((KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK) | KeyEvent.VK_DOWN)) {
					changeShapeBound(0, 0, 0, 1);
				}

			}
			public void keyReleased(KeyEvent e) {

				// delete
				if (e.getKeyCode() != KeyEvent.VK_DELETE)
					return;
				Iterator it;
				Figure f;
				Conn c;
				Vector deletedFigures = new Vector();
				for (it = DrawingPanel.this.figures.iterator(); it.hasNext();) {
					f = (Figure) it.next();
					if (f.isSelected()) {
						deletedFigures.add(f);
						it.remove();
					}
				}
				for (it = DrawingPanel.this.conns.iterator(); it.hasNext();) {
					c = (Conn) it.next();
					if (c.isSelected()) {
						it.remove();
						continue;
					}
					for (Enumeration en = deletedFigures.elements(); en.hasMoreElements();) {
						f = (Figure) en.nextElement();
						if (c.getFromFigure() == f || c.getToFigure() == f) {
							it.remove();
							break;
						}
					}
				}
			}
		});
	}

	public static JMenu getConnectionsJMenu() {
		JMenu menuConns = new JMenu("Connections");
		menuConns.add(Conn.getMenuItem(LineConn.class));
		menuConns.add(Conn.getMenuItem(CurveConn.class));
		return menuConns;
	}
	public void addConn(Conn f) {
		conns.add(f);
		addDrawable(f);
	}
	public void addDrawable(Drawable f) {
		Dimension d = getPreferredSize();
		java.awt.Rectangle rf = f.getBounds();
		if (d.getWidth() < rf.getX() + rf.getWidth()) {
			d.width = (int) (rf.getX() + rf.getWidth());
		}
		if (d.getHeight() < rf.getY() + rf.getHeight()) {
			d.height = (int) (rf.getY() + rf.getHeight());
		}
		setPreferredSize(d);
		this.repaint();
	}

	public void addFigure(Figure f) {
		figures.add(f);
		addDrawable(f);
	}

	public JMenu getActionsMenu() {
		JMenu menu = new JMenu();
		JMenuItem mi;
		(mi = menu.add("Delete")).addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Figure f;
				Conn c;
				for (Enumeration en = DrawingPanel.this.figures.elements(); en.hasMoreElements();) {
					f = (Figure) en.nextElement();
					if (f.isSelected()) {
						DrawingPanel.this.figures.remove(f);
						f.delete();
					}
				}
				for (Enumeration en = DrawingPanel.this.conns.elements(); en.hasMoreElements();) {
					c = (Conn) en.nextElement();
					if (c.isSelected()) {
						DrawingPanel.this.conns.remove(c);
						c.delete();
					}
				}
				repaint();
			}
		});
		mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		return menu;
	}
	public ImageIcon getBackgroundImage() {
		return backgroundImage;
	}
	public Conn getConn(Point p) {
		Enumeration e;
		Conn c;
		for (e = conns.elements(); e.hasMoreElements();) {
			c = (Conn) e.nextElement();
			if (c.isInside(p))
				return c;
		}
		return null;
	}

	public Vector getConnectors() {
		return conns;
	}
	
	@org.jdesktop.application.Action
	public void deleteFigure() {
				Figure f;
				Conn c;
				for (Enumeration en = DrawingPanel.this.figures.elements(); en.hasMoreElements();) {
					f = (Figure) en.nextElement();
					if (f.isSelected()) {
						DrawingPanel.this.figures.remove(f);
						f.delete();
					}
				}
				for (Enumeration en = DrawingPanel.this.conns.elements(); en.hasMoreElements();) {
					c = (Conn) en.nextElement();
					if (c.isSelected()) {
						DrawingPanel.this.conns.remove(c);
						c.delete();
					}
				}
				repaint();
	}
	public Figure getFigure(Point p) {
		Enumeration e;
		Figure f;
		for (e = figures.elements(); e.hasMoreElements();) {
			f = (Figure) e.nextElement();
			if (f.isInside(p))
				return f;
		}
		return null;
	}
	public Vector getFigures() {
		return figures;
	}
	public Drawable insideAnyDrawable(Point p) {
		Enumeration e;
		Figure f;
		Conn c;
		for (e = figures.elements(); e.hasMoreElements();) {
			f = (Figure) e.nextElement();
			if (f.isInside(p))
				return f;
		}
		for (e = conns.elements(); e.hasMoreElements();) {
			c = (Conn) e.nextElement();
			if (c.isInside(p))
				return c;
		}
		return null;
	}

	/**
	 * Return the first selected figure or <code>null</code> if no figure are selected
	 * 
	 * @return first selected figure
	 */
	public Figure getFirstSelectedFigure() {
		long now = System.currentTimeMillis();
		Figure firstf = null;
		for (Object obj : figures) {
			Figure f = (Figure) obj;
			if (f.getSelectedAt() > 0 && now > f.getSelectedAt()) {
				now = f.getSelectedAt();
				firstf = f;
			}
		}
		return firstf;
	}

	public void paint(Graphics g) {
		super.paint(g);

		// terry: draw image background if exist
		if (backgroundImage != null) {
			g.drawImage(backgroundImage.getImage(), 0, 0, null);
		}

		if (tracker) {
			g.drawRect(trackerRect.x, trackerRect.y, trackerRect.width, trackerRect.height);
		}
		Enumeration e;
		Figure f;
		Conn c;
		for (e = figures.elements(); e.hasMoreElements();) {
			f = (Figure) e.nextElement();
			if (tracker) {
				if (trackerRect.contains(f.getBounds())) {
					f.select(true);
				} else
					f.select(false);
			}
			f.paint(g);
		}
		for (e = conns.elements(); e.hasMoreElements();) {
			c = (Conn) e.nextElement();
			if (tracker) {
				if (trackerRect.contains(c.getBounds()))
					c.select(true);
				else
					c.select(false);
			}
			c.paint(g);
		}
	}
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.figures = (Vector) in.readObject();
		for (Figure figure : (Vector<Figure>) figures) {
			figure.parent = this;
		}
		this.conns = (Vector) in.readObject();
		for (Conn conn : (Vector<Conn>) conns) {
			conn.parent = this;
		}
		this.backgroundImage = (ImageIcon) in.readObject();
	}
	public void selectAll(boolean s) {
		Enumeration e;
		Figure f;
		Conn c;
		for (e = figures.elements(); e.hasMoreElements();) {
			f = (Figure) e.nextElement();
			f.select(s);
		}
		for (e = conns.elements(); e.hasMoreElements();) {
			c = (Conn) e.nextElement();
			c.select(s);
		}
	}
	public void setBackgroundImage(BufferedImage bgimg) {
		this.backgroundImage = new ImageIcon(bgimg);
		repaint();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(this.figures);
		out.writeObject(this.conns);
		out.writeObject(backgroundImage);
	}

	/**
	 * Change the x, y coordenates and/or the width/height of the selected shapes. For every non zero parameter, this
	 * method add or substract the amount of the input argument to the correspondent parameter in the shape. E.G:
	 * <code>changeShapeBound(-1,1,0,0)</code> will move all selected shapes -1 point to the left, and 1 point down
	 * leaving the width/height unchanged
	 * 
	 * @param x - x coordenate
	 * @param y- y coordenate
	 * @param w - width
	 * @param h - height
	 */
	private void changeShapeBound(int x, int y, int w, int h) {
		for (Iterator it = DrawingPanel.this.figures.iterator(); it.hasNext();) {
			Figure f = (Figure) it.next();
			if (f.isSelected()) {
				Rectangle r = f.getBounds();
				r.x = x != 0 ? r.x = r.x + x : r.x;
				r.y = y != 0 ? r.y = r.y + y : r.y;
				r.width = w != 0 ? r.width = r.width + w : r.width;
				r.height = h != 0 ? r.height = r.height + h : r.height;
				f.setBounds(r);
			}
		}
		repaint();
	}
}
