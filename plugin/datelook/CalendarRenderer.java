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
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;

/**
 * Renders the calendar on a panel.
 */
public class CalendarRenderer extends Renderer {

	// main colors
	private float red_begin = 80;
	private float green_begin = 150;
	private float blue_begin = 190;
	private Color nowColor = 	new Color(255,125,0);
	private Color month_color = new Color((int) (red_begin + 0.85 * (255 - red_begin)),
			(int) (green_begin + 0.85 * (255 - green_begin)), (int) (blue_begin + 0.85 * (255 - blue_begin)));
	private Color hour_color = new Color((int) (red_begin + 0.5 * (255 - red_begin)),
			(int) (green_begin + 0.5 * (255 - green_begin)), (int) (blue_begin + 0.5 * (255 - blue_begin)));
	private int day_width;
	private int space;
	private Font f;
	private Font small_f;
	private int digit_font_width;
	private int font_height;
	private int font_ascent;
	private int small_digit_font_width;
	private int small_font_ascent;

	/**
	 * Constructor for the CalendarRenderer object
	 * 
	 * @param p Description of the Parameter
	 */
	CalendarRenderer(JPanel p) {
		super(p);
	}

	/**
	 * Gets the font attribute of the CalendarRenderer object
	 * 
	 * @return The font value
	 */
	public Font get_font() {
		return f;
	}

	/**
	 * Draw the calendar on th parent panel
	 * 
	 * @param g2 Description of the Parameter
	 */
	public void draw(Graphics2D g2) {
		this.resized(); // set font sizes ...

		g2.setFont(f);
		g2.setColor(Color.black);
		GregorianCalendar gc = Converter.ms2gc(((DateLookPanel) panel).get_first_rendered_hour_UTC_ms());
		gc.set(GregorianCalendar.HOUR_OF_DAY, 0);

		// render each day
		int i = 0;
		// counter of renderer days
		while (true) {
			int x_pos_real = UTC2x_pos(gc.getTime().getTime());
			int x_pos;
			// x where day rendering begins (for first day not equal to x_pos_real!)
			if (i != 0) {
				x_pos = x_pos_real;
			} else {
				x_pos = 0;
			}
			if (x_pos > panel.getWidth()) {
				break;
			}

			// draw grid lines, rectangles and write text
			// year
			if (i == 0 | gc.get(GregorianCalendar.DAY_OF_YEAR) == 1) {
				g2.setColor(Color.white);
				g2.fillRect(x_pos, 0, panel.getWidth(), DateLookPanel.slot_height);
				g2.setColor(Color.GRAY);
				if (i != 0) {
					g2.drawLine(x_pos, 0, x_pos, DateLookPanel.slot_height);
				}
				g2.drawString(new Integer(gc.get(GregorianCalendar.YEAR)).toString(), x_pos + space,
						DateLookPanel.slot_height / 2 + font_ascent / 2);
			}

			// month
			g2.setColor(Color.black);
			if (i == 0 | gc.get(GregorianCalendar.DAY_OF_MONTH) == 1) {
				// g2.setColor(month_color);
				g2.setColor(Color.white);
				g2.fillRect(x_pos, DateLookPanel.slot_height, panel.getWidth(), DateLookPanel.slot_height);
				g2.setColor(Color.GRAY);
				if (i != 0) {
					g2.drawLine(x_pos, DateLookPanel.slot_height, x_pos, 2 * DateLookPanel.slot_height);
				}
				g2.drawString(Converter.gc2monthl(gc), x_pos + space, (3 * DateLookPanel.slot_height) / 2 + font_ascent
						- font_height / 2);
			}

			int day_width_ext = day_width * 25 / 24 + 2; // to render DST-switch-day exact too!

			// day
			// g2.setColor(this.getDayColor(gc));
			g2.setColor(Color.WHITE);
			g2.fillRect(x_pos, 2 * DateLookPanel.slot_height, day_width_ext, DateLookPanel.slot_height);
			if (day_width > 16) {
				g2.setColor(Color.GRAY);
				g2.drawString(gc.get(GregorianCalendar.DAY_OF_MONTH) + "", x_pos + space,
						(5 * DateLookPanel.slot_height) / 2 + font_ascent / 2);
				if (gc.get(GregorianCalendar.DAY_OF_WEEK) == Calendar.MONDAY
						& (day_width > 2 * (digit_font_width + small_digit_font_width + space))) {
					g2.setFont(small_f);
					g2.setColor(Color.red);
					g2.drawString(gc.get(GregorianCalendar.WEEK_OF_YEAR) + "", x_pos + day_width - space
							- small_digit_font_width * 2, (5 * DateLookPanel.slot_height) / 2 + font_ascent / 2);
					g2.setFont(f);
					g2.setColor(Color.black);
				}
			} else if ((gc.get(GregorianCalendar.DAY_OF_WEEK) == Calendar.SUNDAY)
					& (7 * day_width > space + 2 * digit_font_width)) {
				g2.setColor(Color.GRAY);
				g2.drawString(gc.get(GregorianCalendar.WEEK_OF_YEAR) + "", x_pos - 6 * day_width + space / 2,
						(7 * DateLookPanel.slot_height) / 2 + font_ascent / 2);
				g2.setColor(Color.black);
			}

			// day of week
			g2.setColor(this.getDayOfWeekColor(gc, false));
			g2.fillRect(x_pos, 3 * DateLookPanel.slot_height, day_width_ext, DateLookPanel.slot_height);
			if (day_width > 16) {
				g2.setColor(Color.WHITE);
				g2.fillRect(x_pos, 3 * DateLookPanel.slot_height, day_width_ext, DateLookPanel.slot_height);
				g2.setColor(this.getDayOfWeekColor(gc, true));
				g2.drawString(Converter.getDayOfWeekWString(gc), x_pos + space, (7 * DateLookPanel.slot_height) / 2
						+ font_ascent - font_height / 2);
				// g2.setColor(Color.black);
			}

			// hours
			// g2.setColor(hour_color);
			// g2.fillRect(x_pos_real, 4 * DateLookPanel.slot_height, day_width_ext, DateLookPanel.slot_height);

			// determine number of hours of that day (23/24/25)
			int day_hours = 24;
			day_hours = day_hours + gc.get(Calendar.DST_OFFSET) / (1000 * 60 * 60);

			// align digits to right edge of a day (necessary for DST-switch!)
			gc.add(GregorianCalendar.DAY_OF_YEAR, 1); // increase day
			day_hours = day_hours - gc.get(Calendar.DST_OFFSET) / (1000 * 60 * 60);
			x_pos_real = UTC2x_pos(gc.getTime().getTime()) - day_width;

			if (day_width > 30) {
				g2.setColor(Color.GRAY);
				g2.setFont(small_f);
				g2.drawString("6", x_pos_real + day_width / 4 - small_digit_font_width / 2,
						(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
				g2.drawString("12", x_pos_real + day_width / 2 - small_digit_font_width,
						(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
				g2.drawString("18", x_pos_real + day_width * 3 / 4 - small_digit_font_width,
						(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
				if (day_width / 16 > small_digit_font_width) {
					g2.drawString("3", x_pos_real + day_width / 8 - small_digit_font_width / 2,
							(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
					g2.drawString("9", x_pos_real + day_width * 3 / 8 - small_digit_font_width / 2,
							(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
					g2.drawString("15", x_pos_real + day_width * 5 / 8 - small_digit_font_width,
							(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
					g2.drawString("21", x_pos_real + day_width * 7 / 8 - small_digit_font_width,
							(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
					if (day_width / 48 > small_digit_font_width) {
						// draw hour-lines too
						g2.drawLine(x_pos_real + day_width * 6 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 6 / 24, 10 * DateLookPanel.slot_height);
						g2.drawLine(x_pos_real + day_width * 12 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 12 / 24, 10 * DateLookPanel.slot_height);
						g2.drawLine(x_pos_real + day_width * 18 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 18 / 24, 10 * DateLookPanel.slot_height);
						g2.drawLine(x_pos_real + day_width * 3 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 3 / 24, 10 * DateLookPanel.slot_height);
						g2.drawLine(x_pos_real + day_width * 9 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 9 / 24, 10 * DateLookPanel.slot_height);
						g2.drawLine(x_pos_real + day_width * 15 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 15 / 24, 10 * DateLookPanel.slot_height);
						g2.drawLine(x_pos_real + day_width * 21 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 21 / 24, 10 * DateLookPanel.slot_height);
						g2.drawString("4", x_pos_real + day_width * 4 / 24 - small_digit_font_width / 2,
								(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
						g2.drawLine(x_pos_real + day_width * 4 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 4 / 24, 10 * DateLookPanel.slot_height);
						g2.drawString("5", x_pos_real + day_width * 5 / 24 - small_digit_font_width / 2,
								(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
						g2.drawLine(x_pos_real + day_width * 5 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 5 / 24, 10 * DateLookPanel.slot_height);
						g2.drawString("7", x_pos_real + day_width * 7 / 24 - small_digit_font_width / 2,
								(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
						g2.drawLine(x_pos_real + day_width * 7 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 7 / 24, 10 * DateLookPanel.slot_height);
						g2.drawString("8", x_pos_real + day_width * 8 / 24 - small_digit_font_width / 2,
								(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
						g2.drawLine(x_pos_real + day_width * 8 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 8 / 24, 10 * DateLookPanel.slot_height);
						g2.drawString("10", x_pos_real + day_width * 10 / 24 - small_digit_font_width,
								(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
						g2.drawLine(x_pos_real + day_width * 10 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 10 / 24, 10 * DateLookPanel.slot_height);
						g2.drawString("11", x_pos_real + day_width * 11 / 24 - small_digit_font_width,
								(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
						g2.drawLine(x_pos_real + day_width * 11 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 11 / 24, 10 * DateLookPanel.slot_height);
						g2.drawString("13", x_pos_real + day_width * 13 / 24 - small_digit_font_width,
								(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
						g2.drawLine(x_pos_real + day_width * 13 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 13 / 24, 10 * DateLookPanel.slot_height);
						g2.drawString("14", x_pos_real + day_width * 14 / 24 - small_digit_font_width,
								(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
						g2.drawLine(x_pos_real + day_width * 14 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 14 / 24, 10 * DateLookPanel.slot_height);
						g2.drawString("16", x_pos_real + day_width * 16 / 24 - small_digit_font_width,
								(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
						g2.drawLine(x_pos_real + day_width * 16 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 16 / 24, 10 * DateLookPanel.slot_height);
						g2.drawString("17", x_pos_real + day_width * 17 / 24 - small_digit_font_width,
								(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
						g2.drawLine(x_pos_real + day_width * 17 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 17 / 24, 10 * DateLookPanel.slot_height);
						g2.drawString("19", x_pos_real + day_width * 19 / 24 - small_digit_font_width,
								(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
						g2.drawLine(x_pos_real + day_width * 19 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 19 / 24, 10 * DateLookPanel.slot_height);
						g2.drawString("20", x_pos_real + day_width * 20 / 24 - small_digit_font_width,
								(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
						g2.drawLine(x_pos_real + day_width * 20 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 20 / 24, 10 * DateLookPanel.slot_height);
						g2.drawString("22", x_pos_real + day_width * 22 / 24 - small_digit_font_width,
								(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
						g2.drawLine(x_pos_real + day_width * 22 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 22 / 24, 10 * DateLookPanel.slot_height);
						g2.drawString("23", x_pos_real + day_width * 23 / 24 - small_digit_font_width,
								(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
						g2.drawLine(x_pos_real + day_width * 23 / 24, 5 * DateLookPanel.slot_height, x_pos_real
								+ day_width * 23 / 24, 10 * DateLookPanel.slot_height);
						// check for DST-switch, if true "1" and "2" must be shifted
						if (day_hours == 24) {
							// 24 h day
							g2.drawString("1", x_pos_real + day_width / 24 - small_digit_font_width / 2,
									(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
							g2.drawLine(x_pos_real + day_width / 24, 5 * DateLookPanel.slot_height, x_pos_real
									+ day_width / 24, 10 * DateLookPanel.slot_height);
							g2.drawString("2", x_pos_real + day_width * 2 / 24 - small_digit_font_width / 2,
									(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
							g2.drawLine(x_pos_real + day_width * 2 / 24, 5 * DateLookPanel.slot_height, x_pos_real
									+ day_width * 2 / 24, 10 * DateLookPanel.slot_height);
						} else if (day_hours == 25) {
							// 25 h day
							g2.drawString("1", x_pos_real - small_digit_font_width / 2, (9 * DateLookPanel.slot_height)
									/ 2 + small_font_ascent / 2);
							g2.drawLine(x_pos_real, 5 * DateLookPanel.slot_height, x_pos_real,
									10 * DateLookPanel.slot_height);
							g2.drawString("2", x_pos_real + day_width / 24 - small_digit_font_width / 2,
									(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
							g2.drawLine(x_pos_real + day_width / 24, 5 * DateLookPanel.slot_height, x_pos_real
									+ day_width / 24, 10 * DateLookPanel.slot_height);
							g2.drawString("2", x_pos_real + day_width * 2 / 24 - small_digit_font_width / 2,
									(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
							g2.drawLine(x_pos_real + day_width * 2 / 24, 5 * DateLookPanel.slot_height, x_pos_real
									+ day_width * 2 / 24, 10 * DateLookPanel.slot_height);
						} else {
							// 23 h day
							g2.drawString("1", x_pos_real + day_width * 2 / 24 - small_digit_font_width / 2,
									(9 * DateLookPanel.slot_height) / 2 + small_font_ascent / 2);
							g2.drawLine(x_pos_real + day_width * 2 / 24, 5 * DateLookPanel.slot_height, x_pos_real
									+ day_width * 2 / 24, 10 * DateLookPanel.slot_height);
						}
					}
				}
				g2.setFont(f);
			}

			// lines between days
			g2.setColor(Color.GRAY);
			if (i != 0 & day_width > 16) {
				g2.drawLine(x_pos, 2 * DateLookPanel.slot_height, x_pos, 10 * DateLookPanel.slot_height);
			}
			// first line always drawed
			if (i != 0 & gc.get(GregorianCalendar.DAY_OF_MONTH) == 2) {
				g2.drawLine(x_pos, 2 * DateLookPanel.slot_height, x_pos, 10 * DateLookPanel.slot_height);
			}
			
			// now line
			int xp = UTC2x_pos(System.currentTimeMillis());
			g2.setColor(nowColor);
			g2.drawLine(xp, 0, xp, 10 * DateLookPanel.slot_height);

			i++;
		}

		// render horizontal lines
		g2.setColor(Color.black);
		for (int k = 1; k < 11; k++) {
			// g2.drawLine(0, DateLookPanel.slot_height * k, panel.getWidth(), DateLookPanel.slot_height * k);
		}

	}

	/**
	 * Determines fonts and their seizes after zoom or resize of panel
	 */
	private void resized() {
		Graphics g = panel.getGraphics();
		Graphics2D g2 = (Graphics2D) g;
		day_width = panel.getWidth() * 24 / (int) ((DateLookPanel) panel).get_number_of_rendered_hours(); // not exact
																											// for
																											// DST-switches!
		space = DateLookPanel.slot_height / 4;

		// f = UIManager.getFont("Label.font").deriveFont(Font.PLAIN).deriveFont(DateLookPanel.slot_height * 2 / 3);
		f = new Font("SansSerif", Font.PLAIN, DateLookPanel.slot_height * 2 / 3);
		// small_f =
		// UIManager.getFont("Label.font").deriveFont(Font.PLAIN).deriveFont(Math.min(DateLookPanel.slot_height * 2 / 3,
		// day_width / 8));
		small_f = new Font("SansSerif", Font.PLAIN, Math.min(DateLookPanel.slot_height * 2 / 3, day_width / 8));
		FontRenderContext context = g2.getFontRenderContext();
		Rectangle2D bounds = f.getStringBounds("0", context);
		digit_font_width = (int) bounds.getWidth();
		font_height = (int) bounds.getHeight();
		font_ascent = (int) -bounds.getY();
		bounds = small_f.getStringBounds("0", context);
		small_digit_font_width = (int) bounds.getWidth();
		small_font_ascent = (int) -bounds.getY();
	}

	/**
	 * Gets the dayColor attribute of the CalendarRenderer object
	 * 
	 * @param g Description of the Parameter
	 * @return The dayColor value
	 */
	private Color getDayColor(GregorianCalendar g) {
		float c = (float) g.get(GregorianCalendar.DAY_OF_MONTH) / 31F;
		return new Color((int) (red_begin + c * (255 - red_begin)), (int) (green_begin + c * (255 - green_begin)),
				(int) (blue_begin + c * (255 - blue_begin)));
	}

	/**
	 * Gets the dayOfWeekColor attribute of the CalendarRenderer object
	 * 
	 * @param g Description of the Parameter
	 * @return The dayOfWeekColor value
	 */
	private Color getDayOfWeekColor(GregorianCalendar g, boolean fc) {
		if (g.get(GregorianCalendar.DAY_OF_WEEK) == Calendar.SATURDAY
				| g.get(GregorianCalendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			return fc ? Color.RED : new Color(255, 230, 230);
		}
		return fc ? Color.GRAY : Color.WHITE;
	}

	/**
	 * Description of the Method
	 * 
	 * @param l Description of the Parameter
	 * @return Description of the Return Value
	 */
	private int UTC2x_pos(long l) {
		return (int) ((l - ((DateLookPanel) panel).get_first_rendered_hour_UTC_ms()) * panel.getWidth() / (((DateLookPanel) panel)
				.get_number_of_rendered_hours() * 60 * 60 * 1000));
	}
}
