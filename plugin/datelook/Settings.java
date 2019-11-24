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
import java.io.*;
import javax.swing.filechooser.*;


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
 *  Stores GUI-settings such as position and size of main window,<br>
 *  extended view or simple view and the number of renderers hours<br>
 *  in a file and read this file back if needed.
 */
public class Settings {

  private Rectangle position_and_size = new Rectangle(50, 50, 700, 240);
  private long number_of_rendered_hours = 7 * 24;
  private String settings_file_name;
  private boolean ext_view;


  /**
   *  Constructor for the Settings object
   */
  public Settings() {
    try {
      settings_file_name = FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath()
           + File.separatorChar + ".datelook" + File.separatorChar + "rc";
      File settings_file = new File(settings_file_name);

      if (settings_file.canRead() & !settings_file.isDirectory()) {
        // read file
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(settings_file_name));
        position_and_size = ((Rectangle) in.readObject());
        number_of_rendered_hours = in.readLong();
        ext_view = in.readBoolean();
        in.close();
      }
    }
    catch (Exception b) {
      b.printStackTrace();
    }
  }


  /**
   *  Get position and size of main window on screen
   *
   * @return    position and size
   */
  public Rectangle get_position_and_size() {
    return position_and_size;
  }


  /**
   *  Get number of rendererd hours in main window
   *
   * @return    number of rendererd hours
   */
  public long get_number_of_rendered_hours() {
    return number_of_rendered_hours;
  }


  /**
   *  Get view mode
   *
   * @return    false - simple view<br>
   *      true - extended view
   */
  public boolean get_ext_view() {
    return ext_view;
  }


  /**
   *  Save GUI settings
   *
   * @param  rect       position and size of main window on screen
   * @param  nor_hours  number of rendered hours
   * @param  eview      false - simple view<br>
   *      true - extended view
   */
  public void save(Rectangle rect, long nor_hours, boolean eview) {
    try {
      new File(settings_file_name).delete();
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(settings_file_name));
      out.writeObject(rect);
      out.writeLong(nor_hours);
      out.writeBoolean(eview);
      out.close();
    }
    catch (Exception b) {
      b.printStackTrace();
    }
  }
}

