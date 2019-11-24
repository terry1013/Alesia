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
/*
 * 03/21/2010
 *
 * Copyright (C) 2010 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is distributed under a modified BSD license.  See the included
 * RSTALanguageSupport.License.txt file for details.
 */
package dev.rstala;

import java.io.File;
import javax.swing.filechooser.FileFilter;


/**
 * A file filter for opening files with a specific extension.
 *
 * @author Robert Futrell
 * @version 1.0
 */
class ExtensionFileFilter extends FileFilter {

	private String desc;
	private String ext;


	/**
	 * Constructor.
	 *
	 * @param desc A description of the file type.
	 * @param ext The extension of the file type.
	 */
	public ExtensionFileFilter(String desc, String ext) {
		this.desc = desc;
		this.ext = ext;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean accept(File f) {
		return f.isDirectory() || f.getName().endsWith(ext);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return desc + " (*." + ext + ")";
	}


}
