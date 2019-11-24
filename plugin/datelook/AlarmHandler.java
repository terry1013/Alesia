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
import core.*;

public class AlarmHandler implements Runnable {

	private DateLook dateLook;
	private String me;

	public AlarmHandler(DateLook dl) {
		this.dateLook = dl;
		this.me = getClass().getSimpleName();
	}

	@Override
	public void run() {
		long now_ms = System.currentTimeMillis();
		boolean saving_needed = false;
		EventMemory event_memory = dateLook.getEventMemory();
		for (int i = event_memory.get_size() - 1; i > -1; i--) {
			Event t = event_memory.get_event(i);
			// System.out.println(t.summary + ": " + Converter.ms2hm(t.get_alarm_UTC_ms()));
			/*
			 * if (t.get_alarm_active()) { // System.out.println(t.summary + ": " +
			 * Converter.ms2hm(t.get_begin_UTC_ms())); long l = (Converter.UTCplusPeriod2UTC(t.get_alarm_UTC_ms(),
			 * t.getPeriod(), t.get_alarm_counter(), t.get_period_multiplier())); System.out.println(t.summary + "  " +
			 * Converter.ms2hm(l)); System.out.println("Now  " + Converter.ms2hm(now_ms));
			 * System.out.println("t.get_number_of_periods() > t.get_alarm_counter() " + (t.get_number_of_periods() >
			 * t.get_alarm_counter())); }
			 */
			while (t.get_alarm_active()
					& !(Converter.UTCplusPeriod2UTC(t.get_alarm_UTC_ms(), t.getPeriod(), t.get_alarm_counter(),
							t.get_period_multiplier()) > now_ms) && t.get_number_of_periods() > t.get_alarm_counter()) {

				// process only confirmed events
				if (t.status.equals("CONFIRMED")) {
					Alesia.showNotification(t.summary);
					// sumit task
					String task = t.task;
					if (task.equals("NONE")) {
//						TTaskManager.submitTask(task);
					}
				} else {
					SystemLog.warning(me + " reach " + t.summary + " event. no action performed because status = "
							+ t.status);
				}

				t.inc_alarm_counter();
				saving_needed = true;
			}
		}
		if (saving_needed) { // needed to save increased alarm counters
			event_memory.save();
		}

		// move now line
		dateLook.getDateLookPanel().repaint();
	}
}
