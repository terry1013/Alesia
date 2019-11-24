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
import java.awt.geom.*;

/*
 *  Title:        DateLook
 *  Copyright:    Copyright (c) 2005
 *  Author:       Rene Ewald
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details. You should have
 *  received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
/**
 * renders events on the DateLookPanel
 */
public class EventRenderer extends Renderer {

	private Event event;

	private int x_pos; // x-coordinate of rectangle on panel
	private int width; // width of rectangle on panel
	private boolean visible = true;
	private boolean focus = false;

	private boolean summary_already_drawn;
	private int x_summary; // coordinates of drawn description
	private int y_summary;
	private int height; // height of description

	private String s; // String of description
	private Font f; // used font for description
	private int space; // x-space between description text and description border
	private int text_width; // description text width
	private int text_height; // description text height
	private int ascent; // font ascent

	private int period_counter = 0; // stores number the period where the mouse is

	/**
	 * Constructor for the EventRenderer object
	 * 
	 * @param t event
	 * @param p the DateLookPanel, on it the event is to be rendered
	 */
	public EventRenderer(Event t, DateLookPanel p) {
		super(p);
		event = t;
	}

	/**
	 * Draw the event on the DateLookPanel.
	 * 
	 * @param g2 graphics object
	 * @param paint_rect if true draw the rectangle to the events display
	 * @param paint_con_line if true draw the connection line between the rectangle and the description
	 * @param paint_summary if true draw the summary to the summaries display
	 * @return indicates whether the event is in shown space of time and is set to visible
	 */
	public boolean draw(Graphics2D g2, boolean paint_rect, boolean paint_con_line, boolean paint_summary) {
		boolean ret_val = false;

		if (!visible) {
			return true;
		}
		int drawn_period_counter = (int) Math.min(Math.max((( // preset value to prevent many while-loops
				(((DateLookPanel) panel).get_first_rendered_hour_UTC_ms() + ((DateLookPanel) panel)
						.get_number_of_rendered_hours() * 60L * 60L * 1000L) - event.get_begin_UTC_ms()) / Converter
						.period2ms(event.getPeriod(), event.get_period_multiplier())) + 2, 0), (long) event
				.get_number_of_periods() - 1);
		g2.setColor(event.rendererColor);
		summary_already_drawn = false;
		while (true) {
			this.set_rect(drawn_period_counter);
			if (drawn_period_counter < 0 || x_pos + width < 0) {
				return ret_val; // all visible cyclic occurrences painted
			}

			else if (x_pos < ((DateLookPanel) panel).getWidth()) {
				ret_val = true;
				if (paint_rect) {
					g2.fillRect(x_pos, (5 + event.get_renderer_group()) * DateLookPanel.slot_height, width,
							DateLookPanel.slot_height);
					if (focus) {
						g2.drawRect(x_pos - 2, (5 + event.get_renderer_group()) * DateLookPanel.slot_height - 2,
								width + 3, DateLookPanel.slot_height + 4);
					}
				}

				// paint summary and connection line
				if (paint_summary || paint_con_line) {

					// determine place of description and font
					if (!summary_already_drawn) {
						space = DateLookPanel.slot_height / 5;
						height = DateLookPanel.slot_height;
						f = new Font("SansSerif", Font.PLAIN, height * 2 / 3);
						s = "";
						String hm = Converter.ms2hm(event.get_begin_UTC_ms());
						// if event starts at 00:00 or a renderer day is smaller then 16 pix then
						// show begin date instead of begin time
						if (!(panel.getWidth() * 24 / ((DateLookPanel) panel).get_number_of_rendered_hours() > 16)
								| hm.equals("00:00")) {
							if (event.getPeriod() == Event.None) {
								s = Converter.ms2dmy(Converter.UTCplusPeriod2UTC(event.get_begin_UTC_ms(),
										event.getPeriod(), drawn_period_counter, event.get_period_multiplier()));
							}
							// if periodic event show e.g "2-weekly since 23.Mar 2001"
							else {
								s = event.getPeriod() + " since " + Converter.ms2dmy(event.get_begin_UTC_ms());
							}
						} else {
							s = hm;
						}
						s = s + " - " + event.summary;
						Rectangle2D bounds = f.getStringBounds(s, g2.getFontRenderContext());
						text_width = (int) bounds.getWidth();
						text_height = (int) bounds.getHeight();
						ascent = (int) -bounds.getY();

						// get location on panel, where summary has to be drawn
						x_summary = ((DateLookPanel) super.panel).get_free_space_X(Math.max(x_pos, 5), text_width
								+ space * 2);
						y_summary = ((DateLookPanel) super.panel).get_free_space_Y();
					}

					// paint summary to main window below Calendar
					if (paint_summary && !summary_already_drawn) {
						g2.setColor(event.rendererColor);
						if (focus) {
							g2.fillRect(x_summary - 2, y_summary - 2, text_width + space * 2 + 4, height + 4);
							g2.setColor(Color.white);
							g2.fillRect(x_summary - 1, y_summary - 1, text_width + space * 2 + 2, height + 2);
							g2.setColor(event.rendererColor);
						}
						g2.fillRect(x_summary, y_summary, text_width + space * 2, height);
						g2.setColor(Color.white);
						g2.fillRect(x_summary + 2, y_summary + 2, text_width + space * 2 - 4, height - 4);
						g2.setFont(f);
						g2.setColor(Color.black);
						g2.drawString(s, x_summary + space, y_summary + height / 2 + ascent - text_height / 2);
					}

					// paint connection lines between rectangles and descriptions
					if (paint_con_line) {
						g2.setColor(event.rendererColor);
						g2.drawLine(Math.max(x_pos, 0), (6 + event.get_renderer_group()) * height, x_summary, y_summary);
					}
					summary_already_drawn = true;
				} else {
					x_summary = 0; // to prevent mouse hit of not new determined summary coordinates
					y_summary = 0;
				}
			}
			drawn_period_counter--;
		}
	}

	/**
	 * Draw the "mouse over description"
	 * 
	 * @param g2 graphics object
	 * @param xi x coordinate of mouse pointer
	 * @param yi y coordinate of mouse pointer
	 */
	public void draw_mouse_over_description(Graphics2D g2, int xi, int yi) {
		space = DateLookPanel.slot_height / 5;
		height = DateLookPanel.slot_height;
		f = new Font("SansSerif", Font.PLAIN, height * 2 / 3);
		String s = "";
		// if event starts at 00:00 or a renderer day is smaller then 16 pix then
		// show begin date instead of begin time
		String hm = Converter.ms2hm(event.get_begin_UTC_ms());
		if (!(panel.getWidth() * 24 / ((DateLookPanel) panel).get_number_of_rendered_hours() > 16) | hm.equals("00:00")) {
			s = Converter.ms2dmy(Converter.UTCplusPeriod2UTC(event.get_begin_UTC_ms(), event.getPeriod(),
					period_counter, event.get_period_multiplier()));
		} else {
			s = hm;
		}
		s = s + " - " + event.summary;
		Rectangle2D bounds = f.getStringBounds(s, g2.getFontRenderContext());
		text_width = (int) bounds.getWidth();
		text_height = (int) bounds.getHeight();
		ascent = (int) -bounds.getY();

		int y = Math.max(Math.min(yi - height, 9 * height), 4 * height);
		int x = Math.max(Math.min(xi, ((DateLookPanel) super.panel).getWidth() - text_width - 2 * space), 0);

		g2.setColor(event.rendererColor);
		g2.fillRect(x, y, text_width + space * 2, height);
		g2.setColor(Color.white);
		g2.fillRect(x + 2, y + 2, text_width + space * 2 - 4, height - 4);
		g2.setFont(f);
		g2.setColor(Color.black);
		g2.drawString(s, x + space, y + height / 2 + ascent - text_height / 2);
	}

	/**
	 * Check whether event's rectangle or summary is under the mouse pointer
	 * 
	 * @param x x coordinate of mouse pointer
	 * @param y y coordinate of mouse pointer
	 * @return true - mouse is over the event's rectangle,<br>
	 *         false - mouse is not over
	 */
	public boolean clicked(int x, int y) {
		if (y > (5 + event.get_renderer_group()) * DateLookPanel.slot_height
				&& y < (6 + event.get_renderer_group()) * DateLookPanel.slot_height) {
			period_counter = (int) Math.max(( // preset value to prevent many while-loops
					((DateLookPanel) panel).get_first_rendered_hour_UTC_ms() - event.get_end_UTC_ms())
					/ Converter.period2ms(event.getPeriod(), event.get_period_multiplier()) - 1, 0);
			while (true) {
				if (period_counter + 1 > event.get_number_of_periods()) {
					break;
				}
				this.set_rect(period_counter);
				if (x_pos > panel.getWidth()) {
					break;
				}
				if (x > x_pos && x < x_pos + width) {
					return true; // mouse over event's rectangle
				}
				period_counter++;
			}
		}

		if (x > x_summary && x < x_summary + text_width + space * 2 - 4 && y > y_summary && y < y_summary + height) {
			return true; // mouse over summary
		}
		return false;
	}

	/**
	 * Gets the event attribute of the EventRenderer object
	 * 
	 * @return The event value
	 */
	public Event get_event() {
		return event;
	}

	/**
	 * Sets the visible attribute of the EventRenderer object
	 * 
	 * @param b The new visible value
	 */
	public void set_visible(boolean b) {
		visible = b;
		panel.repaint();
	}

	/**
	 * Gets the visible attribute of the EventRenderer object
	 * 
	 * @return The visible value
	 */
	public boolean get_visible() {
		return visible;
	}

	/**
	 * Gets the period_counter attribute of the EventRenderer object
	 * 
	 * @return The period_counter value
	 */
	public int get_period_counter() {
		return period_counter;
	}

	/**
	 * Sets the focus attribute of the EventRenderer object
	 * 
	 * @param b The new focus value
	 */
	public void set_focus(boolean b) {
		focus = b;
		((DateLookPanel) panel).repaint();
	}

	/**
	 * Gets the focus attribute of the EventRenderer object
	 * 
	 * @return The focus value
	 */
	public boolean get_focus() {
		return focus;
	}

	/**
	 * Set rectangle (x_pos, y_pos) of the events rectangle in the events display.
	 * 
	 * @param period number of period
	 */
	private void set_rect(int period) {
		float panel_width = (float) panel.getWidth();
		float panel_time_width = (float) ((DateLookPanel) panel).get_number_of_rendered_hours() * 60 * 60 * 1000;
		long begin_UTC_of_period = Converter.UTCplusPeriod2UTC(event.get_begin_UTC_ms(), event.getPeriod(), period,
				event.get_period_multiplier());
		width = (int) Math.round((float) ((Converter.UTCplusPeriod2UTC(event.get_end_UTC_ms(), event.getPeriod(),
				period, event.get_period_multiplier()) - begin_UTC_of_period) * panel_width / panel_time_width));
		width = Math.max(width, 5);
		x_pos = (int) Math.round((float) (begin_UTC_of_period - ((DateLookPanel) panel)
				.get_first_rendered_hour_UTC_ms()) * panel_width / panel_time_width);
		return;
	}

	/**
	 * Delete itself
	 */
	public void delete() {
		((DateLookPanel) panel).rebuilt_visible_event_renderer_list();
		((DateLookPanel) panel).repaint();
	}

	/**
	 * Changed, called to indicate that the event has been changed.
	 */
	public void changed() {
		((DateLookPanel) panel).changed();
	}
}
