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

import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import core.*;
import core.datasource.*;

/*
 * Punto de entrada para el sistema de descargas
 */
public class DownloadManager {

	public static final int COMPLETE = 2;
	public static final int DONWLOADING = 1;
	private static Hashtable<Object, ActionListener> listener;
	private static Vector<AbstractDownload> readers;
	private static ExecutorService service;

	/**
	 * inicializa entordo de descarga
	 */
	public static void init() {
		try {
			readers = new Vector<AbstractDownload>();
			listener = new Hashtable<Object, ActionListener>();
			// configurado con variable relacionada con peliculas x conveniencia. y dejo 1 para
			// otras descargas
			service = Executors.newFixedThreadPool(1);

		} catch (Exception e) {
			SystemLog.logException(e);
		}
	}

	/**
	 * inicia descarga de la instancia del <code>AbstractDownload</code> pasado como argumento. una
	 * vez termiada la descarga, se invocara <code>al.actionPerformed(ActionEvent)</code>
	 * 
	 * @param ad - instancia a iniciar
	 * @param al - instancia a notificar cuando finalice la descarga
	 * @param sd - <code>true</code> si se desea mostrar barra de progreso
	 */
	public static void download(AbstractDownload ad, ActionListener al, boolean sd) {
		if (sd) {
			ad.showProgressPanel();
		}
		listener.put(ad, al);
		readers.add(ad);
		service.submit(ad);
	}


	/**
	 * inicia o continua descarga de pelicula pasada como argumento desde el URL url
	 * 
	 * @param newm - registro de pelicula
	 * @param url - origen de datos
	 */
	public static void downloadMovie(Record newm, String url) {
		try {
			DBAccess mdba = ConnectionManager.getAccessTo("movie");
			String f[] = ((String) newm.getFieldValue("rutamovie")).split("[/]");
			String fnam = f[f.length - 1];
			String tarf = SystemVariables.getStringVar("movieDir") + fnam;
			// si no esta el registro y existe el archivo, lo suprimo
			if (mdba.exist(newm) == null) {
				File f1 = new File(tarf);
				if (f1.exists()) {
					f1.delete();
				}
			}
			newm.setFieldValue("status", "down");
			newm.setFieldValue("rutamovie", tarf);
			newm.setFieldValue("srcurl", url);
			mdba.write(newm);
			AbstractDownload ad = getDownload(url, fnam, tarf);
			ad.parameters.put("MovieRecord", newm);
			Thread.sleep(100);
			readers.add(ad);
			service.submit(ad);
		} catch (Exception e) {
			SystemLog.logException(e);
		}
	}

	/**
	 * retorna instancia de <code>AbstractDownload</code> acorde con los parametros pasados
	 * 
	 * @param url - url
	 * @param fnam - archivo a descargar
	 * @param tarf - archivo destino
	 * @return <code>AbstractDownload</code>
	 */
	public static AbstractDownload getDownload(String url, String fnam, String tarf) {
		AbstractDownload ad = null;
		try {
			if (url.startsWith("http")) {
				ad = new HttpDownload(new URL(url + fnam), new File(tarf));
			} else if (url.startsWith("ftp")) {
//				ad = new FTPDownload(new URL(url + fnam), new File(tarf));
			} else if (url.startsWith("file")) {
				URL u = new URL(url + fnam);
				File f = new File("//" + u.getHost() + u.getFile());
				ad = new FileDownload(f, new File(tarf));
			}
		} catch (Exception e) {
			SystemLog.logException(e);
		}
		return ad;
	}

	/**
	 * pasos finales de descarga y finalizacion
	 * 
	 * @param ad - instancia a finalizar
	 */
	protected static void done(AbstractDownload ad) {
		try {
			ad.outputStream.flush();
			ad.outputStream.close();
			ad.inputStream.close();
			if (ad.progressPanel != null) {
				ad.progressPanel.dispose();
			}
			ActionListener al = listener.get(ad);
			if (al != null) {
				al.actionPerformed(new ActionEvent(ad, ActionEvent.ACTION_PERFORMED, ""));
				listener.remove(al);
			}
			readers.remove(ad);
		} catch (Exception e) {
			SystemLog.logException(e);
		}
	}


	/**
	 * suspenda la instancia de <code>AbstractDownload</code> que esta descargando el registro
	 * pelicual pasado como argumento.
	 * 
	 * @param mv - registro peli a suspender
	 */
	public static void suspendMovie(Record mv) {
		for (AbstractDownload ad : readers) {
			Record mr = (Record) ad.parameters.get("MovieRecord");
			if (mr != null && mr.equals(mv)) {
				ad.suspend();
				readers.remove(ad);
			}
		}
	}


	/**
	 * acutaliza estado relacionados con la instacia de <code>AbstractDosnload</code>
	 * 
	 * @param ad - instancia a obtener acutalizacion
	 */
	protected static void updateStatus(AbstractDownload ad) {
		Record mr = (Record) ad.parameters.get("MovieRecord");
		if (mr != null) {
			mr.setFieldValue("progress", ad.progress);
			if (ad.status == COMPLETE) {
				// instalar descarga
			}
		}
	}
}
