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
import java.net.*;

import core.*;

/** clase encargada de descargar archivo usando protocolo http. 
 * NOTA: no instanciar esta clase directamente usar <code>DownloadManager.download(URL)</code> 
 * para que pueda entrar dentro de la lista de trabajos ejecutables gestionada por esta. 
 * 
 */
public class HttpDownload extends AbstractDownload {

	private URL url;
	private HttpURLConnection urlConnection;

	/** nueva instancia
	 * 
	 * @param u - url
	 * @param tf - archivo destino
	 */
	public HttpDownload(URL u, File tf) throws Exception {
		super(tf);
		String su = u.toString().replaceAll("\\s", "%20");
		this.url = new URL(su);
		this.urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setRequestProperty("Range", "bytes=" + beginByte + "-");
		inputStream = new BufferedInputStream(urlConnection.getInputStream(), 2048);
		inputStream.skip(beginByte);
		String cr[] = urlConnection.getHeaderField("Content-Range").split("[/]");
		totalToDownload = Long.parseLong(cr[1]);
	}

	public static boolean checkDownload(URL u, File f) {
		long beg = f.length();
		long td = -1;
		try {
			HttpURLConnection con = (HttpURLConnection) u.openConnection();
			//		for(int i = 0; i < 100 ;i++) {
			//			System.out.println(con.getHeaderFieldKey(i) + ": " + con.getHeaderField(i));
			//		}
			td = Long.parseLong(con.getHeaderField("Content-Length"));
			con.disconnect();
		} catch (Exception e) {
			SystemLog.logException(e);
		}
		return td > beg;
	}

}
