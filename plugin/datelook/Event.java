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
import gui.*;

import java.awt.*;
import java.util.*;

import core.*;

public class Event implements Comparable {

	private String UID; // Unique Identifier (vCalendar 1.0)
	private long begin_UTC_ms;
	private long end_UTC_ms;
	private long alarm_UTC_ms;
	private long last_mod_UTC_ms;
	private String period = None;

	public final static String None = "ONCE";
	public final static String Daily = "DAILY";
	public final static String Weekly = "WEEKLY";
	public final static String Monthly = "MONTHLY";
	public final static String Yearly = "YEARLY";

	private int period_multiplier = 1; // to provide the possiblility for biweekly or evry other day..

	/**
	 * Events class Public
	 */
	public final static int Public = 0;

	/**
	 * Events class Private
	 */
	public final static int Private = 1;

	private int number_of_periods = 2;
	private int alarm_counter = 0;
	private boolean alarm_active = false;

	public Color rendererColor;
	public String summary;
	public String comment;
	public String status;
	public boolean alowAllDay;
	public String task;

	private int renderer_group; // line in panel where it is rendered
	private EventRenderer my_renderer;
	private EventMemory event_memory;
	private boolean now_imported = false; // to remeber that the event is imported now

	/**
	 * Events class Public or Private.<br>
	 * Public - will be exchanged with remote database if synchronizing.<br>
	 * Private - will NOT be exchanged with remote database if synchronizing.
	 */
	private int vcal_class = Public;

	/**
	 * Constructor for the Event object
	 * 
	 * @param b Begin time UTC in ms.
	 * @param rg Renderer group.<br>
	 *        Determines the row in main window where the events rectangle is drawn.
	 * @param tm Event memory, the object that stores this and other events.
	 */
	public Event(long b, int rg, EventMemory tm) {
		generate_UID();
		last_mod_UTC_ms = new GregorianCalendar().getTime().getTime(); // is set to now
		event_memory = tm;
		begin_UTC_ms = b;
		end_UTC_ms = b + 60L * 60L * 1000L; // one hour duration for default
		alarm_UTC_ms = b;
		renderer_group = rg;
		summary = "New Event";
		status = "CONFIRMED";
		rendererColor = ColorComboBox.getRandomColor();
	}

	/**
	 * Constructor for the Event object
	 * 
	 * @param tm Event memory, the object that stores this and other events.
	 */
	public Event(EventMemory tm) {
		generate_UID();
		last_mod_UTC_ms = new GregorianCalendar().getTime().getTime(); // is set to now
		event_memory = tm;
		renderer_group = 0;
		rendererColor = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
	}

	/**
	 * Compare to another event.<br>
	 * The criteria is the events begin time only.
	 * 
	 * @param o other event.
	 * @return 0 - if both begin time are equal<br>
	 *         1 - if own begin time is later than the other<br>
	 *         -1 - if own begin time is earlier than the other
	 */
	public int compareTo(Object o) {
		if (this.begin_UTC_ms > ((Event) o).begin_UTC_ms) {
			return 1;
		}
		if (this.begin_UTC_ms == ((Event) o).begin_UTC_ms) {
			return 0;
		}
		return -1;
	}

	/**
	 * Set begin time in UTC (ms).
	 * 
	 * @param l begin time in UTC (ms)
	 */
	public synchronized void set_begin_UTC_ms(long l) {
		begin_UTC_ms = l;
		// begin_UTC_ms = l / (5L * 60L * 1000L) * (5L * 60L * 1000L); // set in 5 min steps
	}

	/**
	 * Set UID.
	 * 
	 * @param s UID
	 */
	public synchronized void set_UID(String s) {
		UID = s;
	}

	/**
	 * Get UID.
	 * 
	 * @return UID
	 */
	public synchronized String get_UID() {
		return UID;
	}

	/**
	 * Set last modified time in UTC (ms).
	 * 
	 * @param l last modified time in UTC (ms)
	 */
	public synchronized void set_last_mod_UTC_ms(long l) {
		last_mod_UTC_ms = l / 1000L * 1000L; // in 1 sec steps
	}

	/**
	 * Get last modified time in UTC (ms).
	 * 
	 * @return last modified time in UTC (ms)
	 */
	public synchronized long get_last_mod_UTC_ms() {
		return last_mod_UTC_ms;
	}

	/**
	 * Get begin time in UTC (ms).
	 * 
	 * @return begin time in UTC (ms)
	 */
	public synchronized long get_begin_UTC_ms() {
		return begin_UTC_ms;
	}

	/**
	 * Set end time in UTC (ms).
	 * 
	 * @param l Description of the Parameter
	 */
	public synchronized void set_end_UTC_ms(long l) {
		end_UTC_ms = l;
		// end_UTC_ms = l / (5 * 60 * 1000) * (5 * 60 * 1000); // set in 5 min steps
	}

	/**
	 * Get end time in UTC (ms).
	 * 
	 * @return end time in UTC (ms)
	 */
	public synchronized long get_end_UTC_ms() {
		return end_UTC_ms;
	}

	/**
	 * Set alarm time in UTC (ms).
	 * 
	 * @param l alarm time in UTC (ms)
	 */
	public synchronized void set_alarm_UTC_ms(long l) {
		alarm_UTC_ms = l;
		// alarm_UTC_ms = l / (5L * 60L * 1000L) * (5L * 60L * 1000L); // set in 5 min steps
	}

	/**
	 * Get alarm time in UTC (ms).
	 * 
	 * @return alarm time in UTC (ms)
	 */
	public synchronized long get_alarm_UTC_ms() {
		return alarm_UTC_ms;
	}

	/**
	 * Set vcal class
	 * 
	 * @param i Public or Private
	 */
	public synchronized void set_vcal_class(int i) {
		vcal_class = Math.min(Math.max(i, Public), Private);
	}

	/**
	 * Get vcal class
	 * 
	 * @return Public or Private
	 */
	public synchronized int get_vcal_class() {
		return vcal_class;
	}

	public synchronized void setPeriod(String p) {
		if (p.equals(None) || p.equals(Daily) || p.equals(Weekly) || p.equals(Monthly) || p.equals(Yearly)) {
			period = p;
		} else {
			period = None;
		}
	}

	public synchronized String getPeriod() {
		return period;
	}

	/**
	 * Set number of periods.
	 * 
	 * @param i number of periods
	 */
	public synchronized void set_number_of_periods(int i) {
		number_of_periods = Math.min(Math.max(i, 2), 999);
	}

	/**
	 * Get number of periods.
	 * 
	 * @return number of periods
	 */
	public synchronized int get_number_of_periods() {
		return number_of_periods;
	}

	/**
	 * Set period multiplier.
	 * 
	 * @param i period multiplier
	 */
	public synchronized void set_period_multiplier(int i) {
		period_multiplier = Math.min(Math.max(i, 1), 9);
	}

	/**
	 * Get period multiplier.
	 * 
	 * @return period multiplier
	 */
	public synchronized int get_period_multiplier() {
		return period_multiplier;
	}

	/**
	 * Set alarm counter.
	 * 
	 * @param i alarm counter
	 */
	public synchronized void set_alarm_counter(int i) {
		alarm_counter = Math.max(i, 0);
	}

	/**
	 * Get alarm counter.
	 * 
	 * @return Description of the Return Value
	 */
	public synchronized int get_alarm_counter() {
		return alarm_counter;
	}

	/**
	 * Increase alarm counter.
	 */
	public synchronized void inc_alarm_counter() {
		alarm_counter++;
	}

	/**
	 * Set renderer group.<br>
	 * Determines the rows where the event is drawn in the main window.
	 * 
	 * @param i renderer group (range 0 - 4)
	 */
	public synchronized void set_renderer_group(int i) {
		renderer_group = Math.min(Math.max(i, 0), 4);
	}

	/**
	 * Get renderer group.<br>
	 * Indicates the rows where the event is drawn in the main window.
	 * 
	 * @return renderer group (range 0 - 4)
	 */
	public synchronized int get_renderer_group() {
		return renderer_group;
	}

	/**
	 * Set "alarm active"-flag
	 * 
	 * @param b true - set alarm active.<br>
	 *        false - set alarm inactive.
	 */
	public synchronized void set_alarm_active(boolean b) {
		alarm_active = b;
	}

	/**
	 * Get "alarm active"-flag
	 * 
	 * @return "alarm active"-flag
	 */
	public synchronized boolean get_alarm_active() {
		return alarm_active;
	}

	public synchronized void setEventRenderer(EventRenderer tr) {
		my_renderer = tr;
	}

	public synchronized EventRenderer getEventRenderer() {
		return my_renderer;
	}

	/**
	 * Set "now imorted"-flag.
	 * 
	 * @param i true - is now imorted.<br>
	 *        false - is not now imorted.
	 */
	public synchronized void set_now_imported(boolean i) {
		now_imported = i;
	}

	/**
	 * Get "now imorted"-flag.
	 * 
	 * @return now imorted
	 */
	public synchronized boolean get_now_imported() {
		return now_imported;
	}

	/**
	 * Get event memory
	 * 
	 * @return event memory
	 */
	public synchronized EventMemory getEventMemory() {
		return event_memory;
	}

	/**
	 * Changed.<br>
	 * Sets "last modified time" to now and informs its renderer and memory.<br>
	 * Must be called always after changing data of the event.
	 */
	public synchronized void changed() {
		set_last_mod_UTC_ms(new GregorianCalendar().getTime().getTime());
		if (my_renderer != null) {
			my_renderer.changed();
		}
		event_memory.changed();
	}

	/**
	 * Delete event itself, its renderer and delete it from memory.<br>
	 * Dispose all attached windows (Editor...).
	 */
	public synchronized void delete() {
		set_last_mod_UTC_ms(new GregorianCalendar().getTime().getTime());
		event_memory.delete_event(this);
		my_renderer.delete();
	}

	/**
	 * Clone event.<br>
	 * But UID is a new one in the cloned event.
	 * 
	 * @return Description of the Return Value
	 */
	public synchronized Event clone2() {
		Event t = new Event(event_memory);
		t.set_alarm_active(alarm_active);
		t.set_alarm_counter(alarm_counter);
		t.set_alarm_UTC_ms(alarm_UTC_ms);
		t.set_begin_UTC_ms(begin_UTC_ms);
		t.set_end_UTC_ms(end_UTC_ms);
		t.setPeriod(period);
		t.set_period_multiplier(period_multiplier);
		t.set_number_of_periods(number_of_periods);
		t.rendererColor = rendererColor;
		t.set_renderer_group(renderer_group);
		t.summary = summary;
		t.comment = comment;
		t.set_last_mod_UTC_ms(last_mod_UTC_ms);
		return t;
	}

	/**
	 * Set alarm counter to next after now
	 */
	public synchronized void set_alarm_counter_to_next_after_now() {
		long now_ms = new GregorianCalendar().getTime().getTime();
		alarm_counter = (int) Math.max(((now_ms - alarm_UTC_ms) / Converter.period2ms(period, period_multiplier)) - 1,
				0);
		while (!(Converter.UTCplusPeriod2UTC(alarm_UTC_ms, period, alarm_counter, period_multiplier) > now_ms)) {
			alarm_counter++;
		}
	}

	/**
	 * UID is a random number + creating time UTC in ms
	 */
	private void generate_UID() {

		UID = "T-" + TStringUtils.getUniqueID() + "-" + System.currentTimeMillis();
	}
}
