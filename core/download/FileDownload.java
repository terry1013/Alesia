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
package core.download;

import java.io.*;

/** clase encargada de descargar archivo copiandolos directamente desde una ubicacion local 
 * 
 */
public class FileDownload extends AbstractDownload {

	/** nueva instancia
	 * 
	 * @param of - archivo origen
	 * @param tf - archivo destino
	 */
	public FileDownload(File of, File tf) throws Exception {
		super(tf);
		FileInputStream fis = new FileInputStream(of);
		fis.skip(beginByte);
		inputStream = new BufferedInputStream(fis, 2048);
		totalToDownload = of.length();
	}
}
