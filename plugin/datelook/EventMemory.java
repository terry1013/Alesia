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
import java.io.*;
import java.text.*;
import java.util.*;

import javax.swing.*;

import core.*;

/**
 * Description of the Class
 */
public class EventMemory {

	/**
	 * encoding of the used operating system e.g. "ISO-8859-15"
	 */
	public final String default_encoding = new OutputStreamWriter(new ByteArrayOutputStream()).getEncoding();

	private DateLookPanel date_look_panel;
	private ArrayList event_list = new ArrayList();
	private ArrayList deleted_event_list = new ArrayList(); // stores the deleted events for sync

	private String data_file_name; // file to store dates in home directory of user (vCalendar 1.0)
	private String backup_file_name; // file to backup dates in home directory of user (vCalendar 1.0)

	/**
	 * Constructor for the EventMemory object.
	 * 
	 * @param dp Description of the Parameter
	 */
	public EventMemory(DateLookPanel dp) {
		date_look_panel = dp;
		data_file_name = TResources.USER_DIR + "/plugin/datelook/dates.vcs";
		backup_file_name = TResources.USER_DIR + "/plugin/datelook/dates.bak";

	}

	/**
	 * Read the database from predefined file and make a backup of that file (.bak).
	 */
	public synchronized void read_data_file() {
		try {
			import_vCalendar(new File(data_file_name));

			// write a copy to backup file
			File backup_file = new File(backup_file_name);
			int[] i = new int[event_list.size()];
			for (int k = 0; k < i.length; k++) {
				i[k] = k; // array for all events built
			}
			export_vCalendar(backup_file, i, null, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// reset imported now for all
		for (int i = 0; i < this.get_size(); i++) {
			this.get_event(i).set_now_imported(false);
		}
	}

	/**
	 * Save all events to predefined file.
	 */
	public synchronized void save() {
		try {
			File tmp_file = File.createTempFile("datelook", null);
			int[] i = new int[event_list.size()];

			for (int k = 0; k < i.length; k++) {
				i[k] = k; // array for all events built
			}

			export_vCalendar(tmp_file, i, null, false);

			if (tmp_file.canWrite()) {
				File data_file = new File(data_file_name);
				data_file.delete();
				tmp_file.renameTo(data_file);
			}
		} catch (Exception b) {
			b.printStackTrace();
		}
	}

	/**
	 * Revert to the backup database (*.bak) and rename file *.bak to *.vcs and reads it.
	 */
	public synchronized void revert() {
		File backup_file = new File(backup_file_name);
		if (backup_file.canWrite()) {
			File data_file = new File(data_file_name);
			data_file.delete();
			backup_file.renameTo(data_file);
			read_data_file(); // read old data base
		}
	}

	/**
	 * Import events from an input stream reader.
	 * 
	 * @param isr input stream reader
	 * @param fl length of data in byte
	 * @param pb progress bar
	 * @exception Exception -
	 */
	public synchronized void import_vCalendar(InputStreamReader isr) throws Exception {
		boolean begin_found = false;
		boolean vcalender_version_ok = false;

		BufferedReader b_file_reader = new BufferedReader(isr) {
			// readLine() is overidden with new readLine() that unfolds lines
			// according to two different methods: remove CRLF + LWSP or
			// if it is a property value coded with quoted-printable: remove '=' + CRLF
			long read_bytes = 0;

			public String readLine() throws IOException {
				String s = super.readLine();
				if (s == null) {
					return null;
				}
				boolean line_unfolding_quoted_printable = false;
				if (s.toUpperCase().indexOf("QUOTED-PRINTABLE") < s.indexOf(":")
						& s.toUpperCase().indexOf("QUOTED-PRINTABLE") > 0) {
					line_unfolding_quoted_printable = true;
				}

				mark(100);
				while (true) {
					String s2 = super.readLine();
					if (s2 == null || s2.length() == 0) {
						return s;
					}
					if (s2.charAt(0) == ' ' & !line_unfolding_quoted_printable | s.endsWith("=")
							& line_unfolding_quoted_printable) {
						mark(100);
						if (line_unfolding_quoted_printable) {
							s = s.substring(0, s.length() - 1) + s2;
						} else {
							s = s + s2.substring(1);
						}
						read_bytes = read_bytes + 3;
					} else {
						reset();
						break;
					}
				}
				return s;
			}
		};

		// read line by line.
		while (true) {
			String line = b_file_reader.readLine();
			if (line == null) {
				break;
			} else if (!begin_found && line.toUpperCase().compareTo("BEGIN:VCALENDAR") == 0) {
				begin_found = true;
			} else if (begin_found && !vcalender_version_ok && line.toUpperCase().startsWith("VERSION:")) {
				if (line.substring(8).trim().compareTo("1.0") != 0) {
					break;
				} else {
					vcalender_version_ok = true;
				}
			} else if (vcalender_version_ok & line.trim().toUpperCase().compareTo("BEGIN:VEVENT") == 0
					| line.trim().toUpperCase().compareTo("BEGIN:VTODO") == 0) {
				// handle VEVENT
				Event t = new Event(this);
				t.setPeriod(Event.None);
				t.set_vcal_class(Event.Public);
				t.summary = "";
				t.status = "CONFIRMED";
				t.task = "NONE";
				t.comment = "";
				boolean summary_found = false;
				boolean dtstart_found = false;
				boolean dtend_found = false;
				boolean x_alarmcounter_found = false;
				boolean last_modified_found = false;
				while (line.trim().toUpperCase().compareTo("END:VEVENT") != 0
						& line.trim().toUpperCase().compareTo("END:VTODO") != 0) {
					line = b_file_reader.readLine();
					if (line == null) {
						break;
					} else if (line.toUpperCase().startsWith("DTSTART")) {
						Long l = Converter.dtstart2UTC(line.substring(line.indexOf(":") + 1));
						if (l != null) {
							t.set_begin_UTC_ms(l.longValue());
							t.set_alarm_UTC_ms(l.longValue());
							dtstart_found = true;
						}
					} else if (line.toUpperCase().startsWith("DTEND")) {
						Long l = Converter.dtstart2UTC(line.substring(line.indexOf(":") + 1));
						if (l != null) {
							t.set_end_UTC_ms(l.longValue());
							dtend_found = true;
						}
					} else if (line.toUpperCase().startsWith("DALARM")) {
						Long l = Converter.dtstart2UTC(line.substring(line.indexOf(":") + 1));
						if (l != null) {
							t.set_alarm_UTC_ms(l.longValue());
							t.set_alarm_active(true);
						}
					} else if (line.toUpperCase().startsWith("RRULE:")) {
						// RRULE:xx;MULTIPLIER:xx;NUMBERS:xx
						String rmn[] = line.trim().split(";");

						String tmp[] = rmn[0].split("[:]");
						t.setPeriod((tmp.length == 2) ? tmp[1] : Event.None);

						tmp = rmn[1].split("[:]");
						t.set_period_multiplier(Integer.valueOf((tmp.length == 2) ? tmp[1] : "1"));

						tmp = rmn[2].split("[:]");
						int p1 = Integer.valueOf((tmp.length == 2) ? tmp[1] : "1");
						if (p1 == 0) {
							p1 = 999; // 999 is used internally for unlimited
						}
						t.set_number_of_periods(p1);

					} else if (line.toUpperCase().startsWith("UID")) {
						t.set_UID(line.substring(line.indexOf(":") + 1).trim());
					} else if (line.toUpperCase().startsWith("STATUS")) {
						t.status = line.substring(line.indexOf(":") + 1).trim();
					} else if (line.toUpperCase().startsWith("LAST-MODIFIED")) {
						Long l = Converter.dtstart2UTC(line.substring(line.indexOf(":") + 1));
						if (l != null) {
							t.set_last_mod_UTC_ms(l.longValue());
						}
						last_modified_found = true;
					} else if (line.toUpperCase().startsWith("CLASS")) {
						if (line.substring(line.indexOf(":") + 1).toUpperCase().trim().equals("PUBLIC")) {
							t.set_vcal_class(Event.Public);
						} else {
							t.set_vcal_class(Event.Private);
						}
					} else if (line.toUpperCase().startsWith("SUMMARY:")) {
						t.summary = line.substring(8).trim();
						summary_found = true;
					} else if (line.toUpperCase().startsWith("COMMENT:")) {
						t.comment = line.substring(8).trim();
					} else if (line.toUpperCase().startsWith("COLOR:#")) {
						t.rendererColor = Color.decode(line.substring(6).trim());
					} else if (line.toUpperCase().startsWith("TASK:")) {
						t.task = line.substring(5).trim();
					} else if (line.toUpperCase().startsWith("T-ALLDAY:")) {
						t.alowAllDay = Boolean.getBoolean(line.substring(9).trim());
					} else if (line.toUpperCase().startsWith("T-GROUP:")) {
						t.set_renderer_group(Integer.parseInt(line.substring(8).trim()));
					} else if (line.toUpperCase().startsWith("T-ALARMCOUNTER:")) {
						t.set_alarm_counter(Integer.parseInt(line.substring(15).trim()));
						x_alarmcounter_found = true;
					}
				}

				// add new date list
				if (summary_found & dtstart_found & dtend_found) {
					// if an alarmcounter found in event then use this, otherwise
					// calculate a value by own so that all alarms in past are marked as performed
					if (!x_alarmcounter_found) {
						t.set_alarm_counter_to_next_after_now();
					}

					// test for already existing UID and compare Last Modification Time
					boolean store_imported_event = true;
					boolean stop_searching = false;
					for (int i = 0; i < event_list.size(); i++) {
						if (stop_searching) {
							break;
						}
						if (((Event) event_list.get(i)).get_UID().compareTo(t.get_UID()) == 0) {
							stop_searching = true;
							if (((Event) event_list.get(i)).get_last_mod_UTC_ms() < t.get_last_mod_UTC_ms()
									& last_modified_found) {
								// event is already in memory and older than the imported one
								// delete stored event
								((Event) event_list.get(i)).delete();
							} else {
								// event is already in memory and younger than the imported one
								store_imported_event = false;
							}
						}
					}
					// test whether this event is to be deleted
					for (int i = 0; i < deleted_event_list.size(); i++) {
						if (stop_searching) {
							break;
						}
						if (((Event) deleted_event_list.get(i)).get_UID().compareTo(t.get_UID()) == 0) {
							stop_searching = true;
							if (!(((Event) deleted_event_list.get(i)).get_last_mod_UTC_ms() < t.get_last_mod_UTC_ms())) {
								// event is deleted later than modified -> don't store
								store_imported_event = false;
							}
						}
					}
					if (store_imported_event) {
						event_list.add(t);
						t.set_now_imported(true);
					}
				}
			}
		}
		b_file_reader.close();
		Collections.sort(event_list);
		date_look_panel.changed();
	}

	/**
	 * Import events from a file in vCalendar-format
	 * 
	 * @param file file (object)
	 * @param pb progress bar
	 * @exception Exception -
	 */
	public synchronized void import_vCalendar(File file) throws Exception {
		if (file.canRead()) {
			this.import_vCalendar(new MyInputStreamReader(new FileInputStream(file)));
		}
	}

	/**
	 * Export events to a OutputStreamWriter
	 * 
	 * @param osw OutputStreamWriter object
	 * @param i array with indexes of the events to be exported
	 * @param pb progress bar that shows the progress
	 * @param public_only true - export only events of class public<br>
	 *        false - export all selected events
	 * @return number of exported events
	 * @exception Exception -
	 */
	public synchronized int export_vCalendar(OutputStreamWriter osw, int[] i, JProgressBar pb, boolean public_only)
			throws Exception {
		int number_of_exported_events = 0;
		JProgressBar progress_bar = pb;
		BufferedWriter file_writer = new BufferedWriter(osw);
		file_writer.write("BEGIN:VCALENDAR\r\n");
		file_writer.write("PRODID:Alesia DateLook plugins "
				+ PluginManagerIld.getPluginProperty("DateLook", "plugin.version") + "\r\n");
		file_writer.write("VERSION:1.0\r\n");
		if (progress_bar != null) {
			progress_bar.setValue(0);
			progress_bar.setMaximum(i.length);
		}
		for (int n = 0; n < i.length; n++) {
			if (progress_bar != null) {
				progress_bar.setValue(n);
			}
			if (!(public_only & ((Event) event_list.get(i[n])).get_vcal_class() == Event.Private)) {
				write_vevent(file_writer, (Event) event_list.get(i[n]));
			}
			number_of_exported_events++;
		}
		file_writer.write("END:VCALENDAR\r\n");
		file_writer.close();
		return number_of_exported_events;
	}

	/**
	 * Export events to file
	 * 
	 * @param file database file
	 * @param i array with indexes of events to be exported
	 * @param pb progress bar that shows the progress
	 * @param public_only true - only events of class public will be exported,<br>
	 *        false - all selected events will be exported
	 * @return number of exported events
	 * @exception Exception
	 */
	public synchronized int export_vCalendar(File file, int[] i, JProgressBar pb, boolean public_only) throws Exception {
		// charset to export is always US-ASCII, that is sure because there are no other characters inside the file
		return this
				.export_vCalendar(new OutputStreamWriter(new FileOutputStream(file), "US-ASCII"), i, pb, public_only);
	}

	/**
	 * Delete event from database
	 * 
	 * @param t event object
	 */
	public synchronized void delete_event(Event t) {
		deleted_event_list.add(t);
		event_list.remove(t);
		Collections.sort(event_list);
	}

	public void changed() {
		Collections.sort(event_list);
	}

	/**
	 * Add event to database
	 * 
	 * @param t event object
	 */
	public synchronized void add_event(Event t) {
		deleted_event_list.remove(t); // if an event only temporary deleted during drag
		event_list.add(t);
		Collections.sort(event_list);
	}

	/**
	 * Gets the size attribute of the EventMemory object
	 * 
	 * @return The size value
	 */
	public synchronized int get_size() {
		return event_list.size();
	}

	/**
	 * Get event
	 * 
	 * @param i index of the event
	 * @return event object
	 */
	public synchronized Event get_event(int i) {
		return (Event) event_list.get(i);
	}

	/**
	 * Write a event (vEvent) to a BufferedWriter object.
	 * 
	 * @param bw BufferedWriter object
	 * @param t event
	 * @exception IOException -
	 */
	public void write_vevent(BufferedWriter bw, Event t) throws IOException {

		NumberFormat formatter = NumberFormat.getNumberInstance();
		formatter.setMinimumIntegerDigits(2);
		formatter.setGroupingUsed(false);
		NumberFormat formatter4 = NumberFormat.getNumberInstance();
		formatter4.setMinimumIntegerDigits(4);
		formatter4.setGroupingUsed(false);

		Date d = new Date();
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeZone(new SimpleTimeZone(0, "UTC"));
		bw.write("BEGIN:VEVENT\r\n");

		// write STATUS
		bw.write("STATUS:" + t.status + "\r\n");

		// write DTSTART
		d.setTime(t.get_begin_UTC_ms());
		gc.setTime(d);
		bw.write("DTSTART:" + formatter4.format(gc.get(GregorianCalendar.YEAR))
				+ formatter.format(gc.get(GregorianCalendar.MONTH) + 1)
				+ formatter.format(gc.get(GregorianCalendar.DAY_OF_MONTH)) + "T"
				+ formatter.format(gc.get(GregorianCalendar.HOUR_OF_DAY))
				+ formatter.format(gc.get(GregorianCalendar.MINUTE)) + "00Z\r\n");

		// write DTEND
		d.setTime(t.get_end_UTC_ms());
		gc.setTime(d);
		bw.write("DTEND:" + formatter4.format(gc.get(GregorianCalendar.YEAR))
				+ formatter.format(gc.get(GregorianCalendar.MONTH) + 1)
				+ formatter.format(gc.get(GregorianCalendar.DAY_OF_MONTH)) + "T"
				+ formatter.format(gc.get(GregorianCalendar.HOUR_OF_DAY))
				+ formatter.format(gc.get(GregorianCalendar.MINUTE)) + "00Z\r\n");

		// write DALARM
		if (t.get_alarm_active()) {
			d.setTime(t.get_alarm_UTC_ms());
			gc.setTime(d);
			bw.write("DALARM:" + formatter4.format(gc.get(GregorianCalendar.YEAR))
					+ formatter.format(gc.get(GregorianCalendar.MONTH) + 1)
					+ formatter.format(gc.get(GregorianCalendar.DAY_OF_MONTH)) + "T"
					+ formatter.format(gc.get(GregorianCalendar.HOUR_OF_DAY))
					+ formatter.format(gc.get(GregorianCalendar.MINUTE)) + "00Z\r\n");
		}

		// write RRULE
		d.setTime(t.get_begin_UTC_ms());
		gc.setTime(d);
		String period = t.getPeriod();
		if (period != Event.None) {
			int num = t.get_number_of_periods();
			if (num == 999) {
				num = 0; // 999 is internally used for unlimited
			}
			String s = period + ";MULTIPLIER:" + t.get_period_multiplier() + ";NUMBERS:" + num;
			bw.write("RRULE:" + s + "\r\n");
		}

		// write UID
		String uid = "UID:" + t.get_UID();
		int i = 65;
		// fold line
		while (i < uid.length()) {
			uid = uid.substring(0, i) + "\r\n " + uid.substring(i);
			i = i + 65;
		}
		bw.write(uid + "\r\n");

		// write LAST-MODIFIED
		d.setTime(t.get_last_mod_UTC_ms());
		gc.setTime(d);
		bw.write("LAST-MODIFIED:" + formatter4.format(gc.get(GregorianCalendar.YEAR))
				+ formatter.format(gc.get(GregorianCalendar.MONTH) + 1)
				+ formatter.format(gc.get(GregorianCalendar.DAY_OF_MONTH)) + "T"
				+ formatter.format(gc.get(GregorianCalendar.HOUR_OF_DAY))
				+ formatter.format(gc.get(GregorianCalendar.MINUTE))
				+ formatter.format(gc.get(GregorianCalendar.SECOND)) + "Z\r\n");

		// write CLASS
		String class_string = "PUBLIC";
		if (t.get_vcal_class() == Event.Private) {
			class_string = "PRIVATE";
		}
		bw.write("CLASS:" + class_string + "\r\n");

		// write SUMMARY
		bw.write("SUMMARY:" + t.summary + "\r\n");

		// write DESCRIPTION
		bw.write("COMMENT:" + t.comment + "\r\n");

		// write COLOR
		bw.write("COLOR:" + ColorComboRenderer.getHexColor(t.rendererColor) + "\r\n");

		// write TASK
		bw.write("TASK:" + t.task + "\r\n");

		// write internal tags
		formatter.setMinimumIntegerDigits(3);
		bw.write("T-ALLDAY:" + t.alowAllDay + "\r\n");
		bw.write("T-GROUP:" + Integer.toString(t.get_renderer_group()) + "\r\n");
		bw.write("T-ALARMCOUNTER:" + Integer.toString(t.get_alarm_counter()) + "\r\n");
		bw.write("END:VEVENT" + "\r\n");
	}

	/**
	 * Read bytes from FileInputStream and extends each byte to a character imply by adding 0x00 at MSB.
	 */
	public static class MyInputStreamReader extends InputStreamReader {
		InputStream my_input_stream;

		/**
		 * Constructor for the MyInputStreamReader object
		 * 
		 * @param in Description of the Parameter
		 */
		public MyInputStreamReader(InputStream in) {
			super(in);
			my_input_stream = in;
		}

		/**
		 * Read a character from predefined input stream.
		 * 
		 * @return character
		 */
		public int read() {
			try {
				return (char) my_input_stream.read();
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}
		}

		/**
		 * Read characters from my_input_stream and write the character to a character buffer<br>
		 * to a given offset.
		 * 
		 * @param cbuf character buffer
		 * @param offset offset in cbuf where read characters is written to
		 * @param length number of characters to be read
		 * @return number of read characters
		 */
		public int read(char[] cbuf, int offset, int length) {
			byte[] my_byte_array = new byte[length];
			int retVal;

			try {
				retVal = my_input_stream.read(my_byte_array, 0, length);
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}

			for (int k = 0; k < retVal; k++) {
				cbuf[k + offset] = (char) my_byte_array[k];
			}

			return retVal;
		}
	}
}
