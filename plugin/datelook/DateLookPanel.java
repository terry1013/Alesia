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
package plugin.datelook;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;

import action.*;
import core.*;

/**
 * Description of the Class
 */
public class DateLookPanel extends RPanel implements PropertyChangeListener {

	private ArrayList visible_event_renderer_list = new ArrayList(); // contains only events visible in calendar
	private CalendarRenderer my_calendar = new CalendarRenderer(this);
	private ZoomPointer zoom_pointer = new ZoomPointer(this);
	private ShiftPointer shift_pointer = new ShiftPointer(this);
	private Cursor default_cursor = Toolkit.getDefaultToolkit().createCustomCursor(
			Toolkit.getDefaultToolkit().createImage(DateLook.class.getResource("default.gif")), new Point(0, 0),
			"default");
	private Cursor default_day_cursor = Toolkit.getDefaultToolkit().createCustomCursor(
			Toolkit.getDefaultToolkit().createImage(DateLook.class.getResource("default_day.gif")), new Point(0, 0),
			"default_day");
	private Cursor move_cursor = Toolkit.getDefaultToolkit().createCustomCursor(
			Toolkit.getDefaultToolkit().createImage(DateLook.class.getResource("move.gif")), new Point(16, 16), "move");
	private Cursor move_day_cursor = Toolkit.getDefaultToolkit().createCustomCursor(
			Toolkit.getDefaultToolkit().createImage(DateLook.class.getResource("move_day.gif")), new Point(16, 16),
			"move_day");
	private Cursor move_day_copy_cursor = Toolkit.getDefaultToolkit().createCustomCursor(
			Toolkit.getDefaultToolkit().createImage(DateLook.class.getResource("move_day_copy.gif")),
			new Point(16, 16), "move_day_copy");
	private Cursor move_copy_cursor = Toolkit.getDefaultToolkit().createCustomCursor(
			Toolkit.getDefaultToolkit().createImage(DateLook.class.getResource("move_copy.gif")), new Point(16, 16),
			"move_copy");
	private EventMemory event_memory;
	private Settings settings = new Settings();

	private JPopupMenu jPopupMenu_main = new JPopupMenu();
	private JMenuItem jMenuItem_revert = new JMenuItem();
	private JMenuItem jMenuItem_mode = new JMenuItem();
	private JMenuItem jMenuItem_settings = new JMenuItem();

	private int last_x; // position of mouse pointer when pressed
	private int last_y;
	private int mouse_x; // coordinates of mouse pointer
	private int mouse_y;

	private long first_rendered_hour_UTC_ms;

	private final long first_rendered_hour_UTC_ms_min = (new GregorianCalendar(1, 0, 1, 0, 0)).getTime().getTime(); // 01.01.01
																													// 00:00
	private final long last_rendered_UTC_ms_max = (new GregorianCalendar(2501, 0, 1, 0, 0)).getTime().getTime(); // 01.01.2501
																													// 00:00
	private final static long rendered_hours_min = 24;
	private final static long rendered_hours_max = 365 * 24;
	private boolean rebuilt_visible_event_renderer_list = true; // indicates that visible events have changed

	private long first_rendered_hour_UTC_ms_before_shift;
	private long number_of_rendered_hours;
	private long number_of_rendered_hours_before_zoom;
	private boolean extended_view = false;
	private boolean start_ext_view = false;

	private EventRenderer mouse_over_event_renderer; // "event_renderer" where the mouse is over
	private Event dragging_event; // event that is dragging
	private Event dragging_event_before_dragging; // stores the original event when dragging start
	private EventEdit eventEdit;
	private JDialog dialog;
	private boolean newevent;
	private boolean dragging_event_resize = false; // true if the dragging event is a new one not copied
	private long last_begin_UTC_ms; // values of dragging event when dragging starts
	private long last_end_UTC_ms;
	private long last_alarm_UTC_ms;

	/**
	 * Height of a line for year/date/day of week.<br>
	 * very important! controls all sizes of other windows and fonts
	 */
	protected static int slot_height = 0;

	/**
	 * Description of the Field
	 */
	protected static int frame_decor_height = 0;
	/**
	 * Description of the Field
	 */
	protected static int frame_decor_width = 0;

	// variables to control the descriptons renderer
	private int[] free_x = new int[16]; // array to remember x_coordinate of free space in row before
	private int free_space_y; // temporarily store calculated y-coordinate for a description
	private int space_between_date_descriptions;
	private int descriptions_slot_height;
	private int required_description_renderer_height = 0; // to show all dates descriptions in extended view
	private int y_description_slot0; // y-coordinate of first row in descriptons renderer
	private int _height_before_ext_view; // used to switch back to simple view

	/**
	 * Constructor for the DateLookPanel object
	 * 
	 * @param mf Description of the Parameter
	 */
	public DateLookPanel(DateLook mf) {
		super(mf, false);
		this.setBackground(Color.white);
		this.setCursor(default_cursor);

		// initialize first_rendered_hour_UTC_ms with 00:00:00 of today
		GregorianCalendar gc = new GregorianCalendar();
		gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
		gc.set(GregorianCalendar.MINUTE, 0);
		gc.set(GregorianCalendar.SECOND, 0);
		gc.set(GregorianCalendar.MILLISECOND, 0);
		first_rendered_hour_UTC_ms = gc.getTime().getTime();

		number_of_rendered_hours = settings.get_number_of_rendered_hours();
		if (settings.get_ext_view()) {
			start_ext_view = true;
		}

		jMenuItem_revert.setText("Revert");
		jMenuItem_mode.setText("Extended View");
		jMenuItem_settings.setText("Save Zoom");

		jPopupMenu_main.add(jMenuItem_mode);
		jPopupMenu_main.add(jMenuItem_settings);
		// jPopupMenu_main.add(jMenuItem_revert);

		jMenuItem_revert.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jPopupMenu_main.setVisible(false);
				event_memory.revert();
				// TODO: implement: reload entire calendar and repaint
			}
		});
		jMenuItem_mode.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jPopupMenu_main.setVisible(false);
				toggle_view_mode();
			}
		});
		jMenuItem_settings.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				settings.save(new Rectangle(getX(), getY(), getWidth(), slot_height * 10 + frame_decor_height),
						number_of_rendered_hours, extended_view);
				jPopupMenu_main.setVisible(false);
			}
		});
	}
	/**
	 * Paint component
	 * 
	 * @param g Graphics object
	 */
	public void paintComponent(Graphics g) {
		frame_decor_height = getHeight() - this.getHeight();
		frame_decor_width = super.getWidth() - this.getWidth();

		required_description_renderer_height = 0;
		if (!extended_view) {
			slot_height = this.getHeight() / 10; // in extended mode slot_height is frozen
			descriptions_slot_height = (slot_height * 20) / 15;
		}
		super.paintComponent(g);

		if (dragging_event == null) {
			if (rebuilt_visible_event_renderer_list) {
				// rebuilt visible_event_renderer_list here
				rebuilt_visible_event_renderer_list = false;
				visible_event_renderer_list.clear();
				for (int i = event_memory.get_size() - 1; i > -1; i--) {
					EventRenderer tmp_renderer = event_memory.get_event(i).getEventRenderer();
					if (tmp_renderer == null) {
						// create a renderer for this event
						tmp_renderer = new EventRenderer(event_memory.get_event(i), this);
						event_memory.get_event(i).setEventRenderer(tmp_renderer);
					}
					if (tmp_renderer.draw(g2, true, false, false)) {
						// event is visible in calendar
						visible_event_renderer_list.add(tmp_renderer); // add to visible_event_renderer_list
					}
				}
			} else {
				// reuse visible_event_renderer_list
				for (int i = 0; i < visible_event_renderer_list.size(); i++) {
					((EventRenderer) visible_event_renderer_list.get(i)).draw(g2, true, false, false);
				}
			}
		} else {
			// if dragging is in progress the "old" list of visible events can be used -> faster rendering!
			if (!visible_event_renderer_list.contains(dragging_event.getEventRenderer())) {
				rebuilt_visible_event_renderer_list = true;
				visible_event_renderer_list.add(dragging_event.getEventRenderer());
			} else {
				// dragging event to foreground if not already there
				if (visible_event_renderer_list.get(visible_event_renderer_list.size() - 1) != dragging_event
						.getEventRenderer()) {
					visible_event_renderer_list.add(visible_event_renderer_list.remove(
					// dragging event to foreground
							visible_event_renderer_list.indexOf(dragging_event.getEventRenderer())));
				}
			}
			if (dragging_event_before_dragging != null
					&& !visible_event_renderer_list.contains(dragging_event_before_dragging.getEventRenderer())) {
				visible_event_renderer_list.add(dragging_event_before_dragging.getEventRenderer());
			}
			for (int i = 0; i < visible_event_renderer_list.size(); i++) {
				((EventRenderer) visible_event_renderer_list.get(i)).draw(g2, true, false, false);
			}
		}

		if (extended_view) {
			// draw connection lines over rectangles
			reset_space_map(); // reset occupied space for date description on page
			for (int i = 0; i < visible_event_renderer_list.size(); i++) {
				((EventRenderer) visible_event_renderer_list.get(i)).draw(g2, false, true, false);
			}
			// draw descriptions over the connection lines
			reset_space_map(); // reset occupied space for date description on page
			for (int i = 0; i < visible_event_renderer_list.size(); i++) {
				((EventRenderer) visible_event_renderer_list.get(i)).draw(g2, false, false, true);
			}
		}

		my_calendar.draw(g2);
		zoom_pointer.draw(g2);
		shift_pointer.draw(g2);

		if (mouse_over_event_renderer != null) {
			mouse_over_event_renderer.draw_mouse_over_description(g2, mouse_x, mouse_y);
		}

		if (extended_view && required_description_renderer_height > this.getHeight() - 10 * slot_height) {
			// enlarge if extended view and there are descriptions invisible

			EventQueue.invokeLater(new Runnable() {
				// invoke later because it isn't a good idea to start new paint within a paint
				public void run() {
					setSize(getWidth(), required_description_renderer_height + _height_before_ext_view);
					repaint();
					// .paintAll(.getGraphics());
				}
			});
		}
		if (start_ext_view) {
			// or DateLook should start with extended view
			start_ext_view = false;
			EventQueue.invokeLater(new Runnable() {
				// invoke later because it isn't a good idea to start new paint within a paint
				public void run() {
					toggle_view_mode();
				}
			});
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @param e Description of the Parameter
	 */
	public void mousePressed(MouseEvent e) {
		jPopupMenu_main.setVisible(false);
		this.set_font_antialiasing(false);
		if (zoom_pointer.get_visible() || shift_pointer.get_visible() || dragging_event != null) {
			return; // zoom/shift or dragging in progress -> do nothing
		}

		last_x = e.getX(); // remember position for dragging or zoom/shift
		last_y = e.getY();

		if (last_y < slot_height * 5) {
			// start zoom or shift
			number_of_rendered_hours_before_zoom = number_of_rendered_hours;
			first_rendered_hour_UTC_ms_before_shift = first_rendered_hour_UTC_ms;
			if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
				// start zoom
				zoom_pointer.modify(0, last_x, last_y, true);
			} else if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
				// start shift
				shift_pointer.modify(0, last_x, last_y, true);
			}
			this.repaint();
		} else if (last_y < slot_height * 10) {
			// mouse points into dates renderer
			if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && (e.getModifiers() & InputEvent.BUTTON3_MASK) == 0) {
				// start dragging of an event
				for (int i = visible_event_renderer_list.size() - 1; i > -1; i--) {
					if (((EventRenderer) visible_event_renderer_list.get(i)).clicked(last_x, last_y)) {
						// existing event matched
						Event t = ((EventRenderer) visible_event_renderer_list.get(i)).get_event();
						dragging_event_before_dragging = t.clone2(); // remark: new UID!
						dragging_event = t;
						event_memory.add_event(dragging_event_before_dragging);
						dragging_event.getEventRenderer().set_focus(true);
						event_memory.delete_event(dragging_event_before_dragging);
						// should be invisible in Event Manager
						EventRenderer tmp_renderer = dragging_event_before_dragging.getEventRenderer();
						if (tmp_renderer == null) {
							// create a renderer for this event
							tmp_renderer = new EventRenderer(dragging_event_before_dragging, this);
							dragging_event_before_dragging.setEventRenderer(tmp_renderer);
						}
						dragging_event_before_dragging.getEventRenderer().set_visible(false);
						mouse_over_event_renderer = dragging_event.getEventRenderer();
						last_begin_UTC_ms = dragging_event.get_begin_UTC_ms();
						last_end_UTC_ms = dragging_event.get_end_UTC_ms();
						last_alarm_UTC_ms = dragging_event.get_alarm_UTC_ms();
						dragging_event_resize = false;

						cursor_control(e);
						return;
					}
				}
				// no event matched therefore create a new one
				int group = (last_y - slot_height * 5) / slot_height;
				long begin_UTC_ms = ((long) last_x * number_of_rendered_hours * 60L * 60L * 1000L)
						/ ((long) this.getWidth()) + first_rendered_hour_UTC_ms;
				dragging_event = new Event(begin_UTC_ms, group, event_memory);
				dragging_event.set_end_UTC_ms(begin_UTC_ms);
				dragging_event_resize = true;
				last_begin_UTC_ms = begin_UTC_ms;
				last_end_UTC_ms = begin_UTC_ms;
				last_alarm_UTC_ms = begin_UTC_ms;
				event_memory.add_event(dragging_event);
				EventRenderer tmp_renderer = dragging_event.getEventRenderer();
				if (tmp_renderer == null) {
					// create a renderer for this event
					tmp_renderer = new EventRenderer(dragging_event, this);
					dragging_event.setEventRenderer(tmp_renderer);
				}
				tmp_renderer.set_focus(true);

				cursor_control(e);
			}
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @param e Description of the Parameter
	 */
	public void mouseMoved(MouseEvent e) {
		mouse_x = e.getX();
		mouse_y = e.getY();
		if (mouse_y > slot_height * 5) { // 5 * slot_height
			for (int i = visible_event_renderer_list.size() - 1; i > -1; i--) {
				if (((EventRenderer) visible_event_renderer_list.get(i)).clicked(mouse_x, mouse_y)
						&& mouse_y < slot_height * 10) {
					mouse_over_event_renderer = (EventRenderer) visible_event_renderer_list.get(i);
					this.setCursor(default_cursor);
					this.repaint();
					return;
				}
			}
			this.setCursor(default_cursor);
		} else {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
		mouse_over_event_renderer = null;
		this.repaint();
	}

	/**
	 * Description of the Method
	 * 
	 * @param e Description of the Parameter
	 */
	public void mouseDragged(MouseEvent e) {
		mouse_x = e.getX();
		mouse_y = e.getY();
		if (zoom_pointer.get_visible()) { // zoom
			set_number_of_rendered_hours((int) (last_x * number_of_rendered_hours_before_zoom / Math.max(e.getX(), 1)));
			zoom_pointer.modify(0, e.getX(), e.getY(), true);
			this.repaint();
		} else if (shift_pointer.get_visible()) { // shift
			int delta_x = last_x - e.getX();
			long delta_first_hour = (number_of_rendered_hours_before_zoom * (long) delta_x / (long) this.getWidth());
			GregorianCalendar gc = Converter.ms2gc(first_rendered_hour_UTC_ms_before_shift);
			gc.add(GregorianCalendar.HOUR_OF_DAY, (int) delta_first_hour);
			set_first_rendered_hour_UTC_ms(gc.getTime().getTime());
			shift_pointer.modify(0, e.getX(), e.getY(), true);
			this.repaint();
		} else if (dragging_event != null) { // drag an event
			dragging_event.set_renderer_group(Math.max(Math.min((e.getY() - slot_height * 5) / slot_height, 4), 0));

			long delta = (long) (e.getX() - last_x) * (long) number_of_rendered_hours * 60L * 60L * 1000L
					/ (long) this.getWidth();
			long begin_delta;
			long end_delta;
			long alarm_delta;
			boolean shift_pressed = false;
			if ((e.getModifiers() & InputEvent.SHIFT_MASK) == 1) {
				shift_pressed = true;
			}
			if (!shift_pressed || (dragging_event_resize == true)) {
				// shift in five-minutes-steps
				delta = (delta / (5L * 60L * 1000L)) * (5L * 60L * 1000L); // set to 5 min steps
				begin_delta = delta;
				end_delta = delta;
				alarm_delta = delta;
			} else {
				// shift in one-day-steps
				// set delta to full days and consider DST-switches (23h or 25h-days).
				// because of alarm or/and begin can be shifted over a DST-switch and end not
				// all delta times must considered separately
				int shifted_days = (int) (delta / (24L * 60L * 60L * 1000L));
				begin_delta = Converter.UTCplusPeriod2UTC(last_begin_UTC_ms, Event.Daily, shifted_days, 1)
						- last_begin_UTC_ms;
				end_delta = Converter.UTCplusPeriod2UTC(last_end_UTC_ms, Event.Daily, shifted_days, 1)
						- last_end_UTC_ms;
				alarm_delta = Converter.UTCplusPeriod2UTC(last_alarm_UTC_ms, Event.Daily, shifted_days, 1)
						- last_alarm_UTC_ms;
			}

			long new_alarm_UTC_ms = last_alarm_UTC_ms + alarm_delta;
			long new_begin_UTC_ms = last_begin_UTC_ms + begin_delta;
			long new_end_UTC_ms = last_end_UTC_ms + end_delta;
			if (new_alarm_UTC_ms > first_rendered_hour_UTC_ms_min & new_end_UTC_ms < last_rendered_UTC_ms_max) {
				// do not shift over absolute time limits!
				if (dragging_event_resize == false) {
					// move event
					dragging_event.set_begin_UTC_ms(new_begin_UTC_ms);
					dragging_event.set_alarm_UTC_ms(new_alarm_UTC_ms);
					dragging_event.set_end_UTC_ms(new_end_UTC_ms);
					dragging_event.set_alarm_counter_to_next_after_now();
				} else {
					// new event has been created
					if (shift_pressed) {
						// if shift is pressed round begin/end/alarm-time to day-boundary
						if (end_delta > 0) {
							// shift end time
							dragging_event.set_end_UTC_ms(Converter.ms2msdayboundary(new_end_UTC_ms));
							dragging_event.set_begin_UTC_ms(Converter.ms2msdayboundary(last_begin_UTC_ms));
							dragging_event.set_alarm_UTC_ms(Converter.ms2msdayboundary(last_alarm_UTC_ms));
						} else {
							// shift begin time and alarm time
							dragging_event.set_begin_UTC_ms(Converter.ms2msdayboundary(new_begin_UTC_ms));
							dragging_event.set_alarm_UTC_ms(Converter.ms2msdayboundary(new_alarm_UTC_ms));
							dragging_event.set_end_UTC_ms(Converter.ms2msdayboundary(last_end_UTC_ms));
						}
					} else {
						if (end_delta > 0) {
							// shift end time
							dragging_event.set_end_UTC_ms(new_end_UTC_ms);
							dragging_event.set_begin_UTC_ms(last_begin_UTC_ms);
							dragging_event.set_alarm_UTC_ms(last_alarm_UTC_ms);
						} else {
							// shift begin time and alarm time
							dragging_event.set_begin_UTC_ms(new_begin_UTC_ms);
							dragging_event.set_alarm_UTC_ms(new_alarm_UTC_ms);
							dragging_event.set_end_UTC_ms(last_end_UTC_ms);
						}
					}
				}
				dragging_event.changed();
			}
			cursor_control(e);
		}
	}

	/**
	 * Description of the Method
	 * 
	 * @param e Description of the Parameter
	 */
	public void mouseReleased(MouseEvent e) {
		this.set_font_antialiasing(true);
		if (mouse_y > slot_height * 5) {
			this.setCursor(default_cursor);
		} else {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}

		int x = e.getX();
		int y = e.getY();

		if (zoom_pointer.get_visible() | shift_pointer.get_visible()) {
			// zoom or shift took place
			zoom_pointer.modify(false);
			shift_pointer.modify(false);
			this.repaint();
			return;
		}

		if (dragging_event != null) {
			// dragging took place
			if (dragging_event_resize) {
				// dragging of new event
				dragging_event.getEventRenderer().set_focus(true);
				showEventEditorDialg(true);
				dragging_event_resize = false;
			} else if ((e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
				// copy of event requested
				// Change UIDs of original and copy
				String UID = dragging_event_before_dragging.get_UID();
				dragging_event_before_dragging.set_UID(dragging_event.get_UID());
				dragging_event.set_UID(UID);
				if (!dragging_event_before_dragging.getEventRenderer().get_visible()) {
					event_memory.add_event(dragging_event_before_dragging);
				}
				dragging_event_before_dragging.getEventRenderer().set_visible(true);
				dragging_event.getEventRenderer().set_focus(false);
				showEventEditorDialg(false);
			} else {
				// only shift of old event
				dragging_event.getEventRenderer().set_focus(false);
				showEventEditorDialg(false);
				dragging_event_before_dragging.delete();
			}
			dragging_event.changed();
			rebuilt_visible_event_renderer_list = true;
			dragging_event_before_dragging = null;
			dragging_event = null;
			return;
		}

		// no dragging, no zoom, no shift took place
		if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
			for (int i = visible_event_renderer_list.size() - 1; i > -1; i--) {
				if (((EventRenderer) visible_event_renderer_list.get(i)).clicked(x, y)) {
					// meet an event
					/*
					 * if (((EventRenderer) visible_event_renderer_list.get(i)).get_event().get_my_editor_frame() !=
					 * null) { return; // meet event is still edited }
					 */
					mouse_over_event_renderer = null;
					this.repaint();
					/*
					 * EditorFrame ed = new EditorFrame(((EventRenderer)
					 * visible_event_renderer_list.get(i)).get_event(), null, false, false); ((EventRenderer)
					 * visible_event_renderer_list.get(i)).get_event().set_my_editor_frame(ed); ed.setLocation((int)
					 * this.getLocationOnScreen().getX() + x - 10, (int) this.getLocationOnScreen() .getY() + y - 10);
					 * ed.setVisible(true);
					 */
					return;
				}
			}
			jPopupMenu_main.show(this, x, y);
		}
	}

	/**
	 * Check whether the mouse wheel rotates and handle the actions to be done: <br>
	 * mouse wheel rotates - shift visible space of time<br>
	 * mouse wheel rotates + shift - zoom.
	 * 
	 * @param e mouse wheel event
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (!zoom_pointer.get_visible() & !shift_pointer.get_visible() & dragging_event == null) {
			GregorianCalendar gc = Converter.ms2gc(first_rendered_hour_UTC_ms);

			if ((e.getModifiers() & InputEvent.SHIFT_MASK) == 0) {
				// shift visible space of time

				int delta = Math.max((int) ((long) Math.abs(e.getWheelRotation()) * number_of_rendered_hours / 25), 1);
				int y = e.getY();
				// srcoll speed depends on where the mouse is year, month or day area of the calendar
				if (y < slot_height) {
					delta = delta * 365;
				} else if (y < 2 * slot_height) {
					delta = delta * 30;
				}
				if (e.getWheelRotation() < 0) {
					delta = -delta;
				}
				gc.add(GregorianCalendar.HOUR_OF_DAY, delta);
				set_first_rendered_hour_UTC_ms(gc.getTime().getTime());
			} else {
				// change zoom state

				int factor = 50;
				int y = e.getY();
				// zoom speed depends on where the mouse is year, month or day area of the calendar
				if (y < slot_height) {
					factor = 10;
				} else if (y < 2 * slot_height) {
					factor = 20;
				}
				set_number_of_rendered_hours((int) (get_number_of_rendered_hours() + (get_number_of_rendered_hours() / factor)
						* e.getWheelRotation()));
			}
			mouse_over_event_renderer = null;
			this.repaint();
		}
	}

	/**
	 * Method is called if an event is dragging or if a key is pressed or released.<br>
	 * It controls the cursor and the rendering of the original event
	 * 
	 * @param e input event
	 */
	private void cursor_control(InputEvent e) {
		if (dragging_event == null) {
			return;
		}
		if (dragging_event_resize) {
			// new event is being created
			if ((e.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
				this.setCursor(default_day_cursor);
			} else {
				this.setCursor(default_cursor);
			}
		}
		// dragging an old or copied event
		else if ((e.getModifiers() & InputEvent.CTRL_MASK) != 0) {
			// dragging the copy
			if ((e.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
				this.setCursor(move_day_copy_cursor);
			} else {
				this.setCursor(move_copy_cursor);
			}
			if (!dragging_event_before_dragging.getEventRenderer().get_visible()) {
				event_memory.add_event(dragging_event_before_dragging);
			}
			dragging_event_before_dragging.getEventRenderer().set_visible(true);
			dragging_event.changed();
		} else {
			// dragging the original
			if ((e.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
				this.setCursor(move_day_cursor);
			} else {
				this.setCursor(move_cursor);
			}
			if (dragging_event_before_dragging.getEventRenderer().get_visible()) {
				event_memory.delete_event(dragging_event_before_dragging);
			}
			dragging_event_before_dragging.getEventRenderer().set_visible(false);
			dragging_event.changed();
			// not dragging_event_before_dragging.changed()
			// because last_mod of it would be changed and this would be wrong
		}
	}

	/**
	 * Description of the Method
	 */
	public void rebuilt_visible_event_renderer_list() {
		rebuilt_visible_event_renderer_list = true;
	}

	/**
	 * Gets the first_rendered_hour_UTC_ms attribute of the DateLookPanel object
	 * 
	 * @return The first_rendered_hour_UTC_ms value
	 */
	public long get_first_rendered_hour_UTC_ms() {
		return first_rendered_hour_UTC_ms;
	}

	/**
	 * Description of the Method
	 * 
	 * @param d Description of the Parameter
	 */
	public void set_first_rendered_hour_UTC_ms(long d) {
		rebuilt_visible_event_renderer_list = true;

		// prevent that time is visible which is out of range 01.01.01 00:00 to 01.01.2500 00:00
		first_rendered_hour_UTC_ms = Math.min(Math.max(first_rendered_hour_UTC_ms_min, d), last_rendered_UTC_ms_max
				- number_of_rendered_hours * 60L * 60L * 1000L);
	}

	/**
	 * Gets the number_of_rendered_hours attribute of the DateLookPanel object
	 * 
	 * @return The number_of_rendered_hours value
	 */
	public long get_number_of_rendered_hours() {
		return number_of_rendered_hours;
	}

	/**
	 * Set number of renderer hours.
	 * 
	 * @param i number of renderer hours
	 */
	public void set_number_of_rendered_hours(int i) {
		rebuilt_visible_event_renderer_list = true;

		// prevent that time is visible which is out of range 01.01.01 00:00 to 01.01.2500 00:00
		number_of_rendered_hours = Math.min(Math.min(Math.max(rendered_hours_min, i), rendered_hours_max),
				(last_rendered_UTC_ms_max - first_rendered_hour_UTC_ms) / (60L * 60L * 1000L));
	}

	/**
	 * Sets the event_memory attribute of the DateLookPanel object
	 * 
	 * @param tm The new event_memory value
	 */
	public void set_event_memory(EventMemory tm) {
		event_memory = tm;
	}

	/**
	 * Toggle view mode between simple and extended
	 */
	public void toggle_view_mode() {
		if (extended_view) {
			jMenuItem_mode.setText("Extended View");
			setSize(getWidth(), _height_before_ext_view);
		} else {
			jMenuItem_mode.setText("Simple View");
			_height_before_ext_view = getHeight();
			setSize(getWidth(), _height_before_ext_view + 8 * descriptions_slot_height);
		}
		extended_view = !extended_view;
		repaint();
	}
	/**
	 * Indicate to the DateLookPanel that at least one event has been changed.<br>
	 * The panel will be repainted and the visible_event_renderer_list will be rebuilt.
	 */
	public void changed() {
		// called if an event has been changed
		if (dragging_event == null) {
			rebuilt_visible_event_renderer_list = true;
		}
		this.repaint();
	}

	/**
	 * Give coordinates of free space to render the event's description on descriptions renderer
	 * 
	 * @param x_rectangle Description of the Parameter
	 * @param width Description of the Parameter
	 * @return Description of the Return Value
	 */
	public int get_free_space_X(int x_rectangle, int width) {

		int description_slot = 0;
		int panel_width = this.getWidth();
		while (description_slot < 15 & free_x[description_slot] < x_rectangle + width + space_between_date_descriptions
				& free_x[description_slot] < panel_width) {
			description_slot++;
		}
		required_description_renderer_height = Math.max(required_description_renderer_height,
				Math.min(15, (description_slot + 1)) * descriptions_slot_height);
		if (description_slot == 15) {
			// search for slot with the most free space
			int best_description_slot = 0;
			for (int i = 1; i < 15; i++) {
				if (free_x[best_description_slot] < free_x[i]) {
					best_description_slot = i;
				}
			}
			free_space_y = y_description_slot0 + best_description_slot * descriptions_slot_height;
			x_rectangle = free_x[best_description_slot] - width - space_between_date_descriptions;
			free_x[best_description_slot] = x_rectangle;
			return x_rectangle;
		} else {
			if (x_rectangle + width + space_between_date_descriptions > panel_width) {
				x_rectangle = panel_width - width - space_between_date_descriptions;
			}
			x_rectangle = Math.max(x_rectangle, space_between_date_descriptions);
			free_x[description_slot] = x_rectangle - space_between_date_descriptions;
			free_space_y = y_description_slot0 + description_slot * descriptions_slot_height;
			return x_rectangle;
		}
	}

	/**
	 * Gets the free_space_Y attribute of the DateLookPanel object
	 * 
	 * @return The free_space_Y value
	 */
	public int get_free_space_Y() {
		// must be called immediately after get_free_space_X()
		return free_space_y;
	}

	/**
	 * Set all space of descriptions renderer to empty
	 */
	private void reset_space_map() {
		int panel_width = this.getWidth();
		for (int i = 0; i < 15; i++) {
			free_x[i] = panel_width;
		}
		space_between_date_descriptions = this.getWidth() / 100;
		y_description_slot0 = (slot_height * 10) + descriptions_slot_height / 5;
	}

	/**
	 * Description of the Class
	 */
	public static class ZoomPointer extends Renderer {

		private int x_1;
		private int x_2;
		private int y;
		private boolean visible;

		/**
		 * Constructor for the ZoomPointer object
		 * 
		 * @param p Description of the Parameter
		 */
		public ZoomPointer(DateLookPanel p) {
			super(p);
		}

		/**
		 * Description of the Method
		 * 
		 * @param g2 Description of the Parameter
		 */
		public void draw(Graphics2D g2) {
			if (visible) {
				g2.setColor(Color.GREEN);
				g2.fill3DRect(x_1, y, x_2 - x_1, 1, true);
				g2.fill3DRect(x_1, 0, 1, panel.getHeight(), true);
				g2.fill3DRect(x_2, 0, 1, panel.getHeight(), true);
				Polygon arrow = new Polygon();
				arrow.addPoint(x_1, y);
				arrow.addPoint(x_1 + 8, y - 5);
				arrow.addPoint(x_1 + 8, y + 5);
				g2.fillPolygon(arrow);
				arrow = new Polygon();
				arrow.addPoint(x_2, y);
				arrow.addPoint(x_2 - 8, y - 5);
				arrow.addPoint(x_2 - 8, y + 5);
				g2.fillPolygon(arrow);
			}
		}

		/**
		 * Description of the Method
		 * 
		 * @param x1 Description of the Parameter
		 * @param x2 Description of the Parameter
		 * @param y1 Description of the Parameter
		 * @param v Description of the Parameter
		 */
		public void modify(int x1, int x2, int y1, boolean v) {
			x_1 = x1;
			x_2 = x2;
			y = y1;
			visible = v;
		}

		/**
		 * Description of the Method
		 * 
		 * @param v Description of the Parameter
		 */
		public void modify(boolean v) {
			visible = v;
		}

		/**
		 * Gets the visible attribute of the ZoomPointer object
		 * 
		 * @return The visible value
		 */
		public boolean get_visible() {
			return visible;
		}
	}

	/**
	 * Description of the Class
	 */
	public static class ShiftPointer extends Renderer {
		private int x_2;
		private int y;
		boolean visible;

		/**
		 * Constructor for the ShiftPointer object
		 * 
		 * @param p Description of the Parameter
		 */
		public ShiftPointer(DateLookPanel p) {
			super(p);
		}

		/**
		 * Description of the Method
		 * 
		 * @param g2 Description of the Parameter
		 */
		public void draw(Graphics2D g2) {
			if (visible) {
				g2.setColor(Color.orange);
				g2.fill3DRect(x_2 - 17, y, 34, 2, true);
				g2.fill3DRect(x_2, 0, 2, panel.getHeight(), true);
				Polygon arrow = new Polygon();
				arrow.addPoint(x_2 - 20, y);
				arrow.addPoint(x_2 - 10, y - 7);
				arrow.addPoint(x_2 - 10, y + 7);
				g2.fillPolygon(arrow);
				arrow = new Polygon();
				arrow.addPoint(x_2 + 20, y);
				arrow.addPoint(x_2 + 10, y - 7);
				arrow.addPoint(x_2 + 10, y + 7);
				g2.fillPolygon(arrow);
			}
		}

		/**
		 * Description of the Method
		 * 
		 * @param x1 Description of the Parameter
		 * @param x2 Description of the Parameter
		 * @param y1 Description of the Parameter
		 * @param v Description of the Parameter
		 */
		public void modify(int x1, int x2, int y1, boolean v) {
			x_2 = x2;
			y = y1;
			visible = v;
		}

		/**
		 * Description of the Method
		 * 
		 * @param v Description of the Parameter
		 */
		public void modify(boolean v) {
			visible = v;
		}

		/**
		 * Gets the visible attribute of the ShiftPointer object
		 * 
		 * @return The visible value
		 */
		public boolean get_visible() {
			return visible;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		Object prp = evt.getPropertyName();
		Object newv = evt.getNewValue();

		if (prp.equals(TConstants.ACTION_PERFORMED)) {
			if (newv instanceof CancelAction) {
				if (dragging_event_before_dragging != null) {
					// restore original
					dragging_event_before_dragging.set_UID(dragging_event.get_UID()); // write orig UID to original
																						// event
																						// dragging_event.delete();
					dragging_event_before_dragging.getEventMemory().add_event(dragging_event_before_dragging);
					if (dragging_event_before_dragging.getEventRenderer() != null) {
						dragging_event_before_dragging.getEventRenderer().set_visible(true);
					}
				}
				if (newevent) {
					dragging_event.delete();
				}
				dialog.dispose();
			}
			if (newv instanceof AceptAction) {
				Event e = eventEdit.getEvent();
				e.getEventMemory().add_event(e);
				e.getEventMemory().save();
				dialog.dispose();
			}

			if (newv instanceof DeleteRecord) {
				dragging_event.delete();
				dragging_event.getEventMemory().save();
				dialog.dispose();
			}

			boolean new_event = false;

			if (new_event && (dragging_event != null)) {
				dragging_event.delete();
			}
		}
	}
	public void showEventEditorDialg(boolean ne) {
		eventEdit = new EventEdit(dragging_event, ne);
		newevent = ne;

		// FIXME: 141120: the main frame cant be directly referred. app name can change
		dialog = new JDialog(Alesia.mainFrame, true);

		dialog.setContentPane(eventEdit);
		eventEdit.addPropertyChangeListener(TConstants.ACTION_PERFORMED, this);
		JButton jb = (JButton) eventEdit.getClientProperty(TConstants.DEFAULT_BUTTON);
		if (jb != null) {
			dialog.getRootPane().setDefaultButton(jb);
		}
		TAbstractAction jb1 = (TAbstractAction) ((JButton) eventEdit
				.getClientProperty(TConstants.DEFAULT_CANCEL_BUTTON)).getAction();
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.addWindowListener(new DialogListener(jb1, eventEdit));
		if (jb1 != null) {
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		}

		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setResizable(false);
		// dialog.setTitle(ConstantUtilities.getBundleString(tit));
		dialog.setVisible(true);
	}
}
