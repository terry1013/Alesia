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
package core;

import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import javax.swing.*;

import core.datasource.*;
import core.download.*;

/**
 * esta clase centraliza la verificacion, respaldo e instalacion de actuaizaciones de aplicacion. el sistema de
 * actualizaciones esta compuesto pricipalmente por 2 archivos. el manifiesto (constante <code>update_manifest</code>)
 * contiene informacion de los arreglos fechas de generacion etc. el archivo indicado en la constante
 * <code>update_file</code> contiene el codigo para la actualizacion. todo esto ublicado en el url especificado en
 * <code>update_URL</code>
 */
public class UpdateManager {

	public static boolean isDownload;
	private static String newID, newVer;
	private static File tmpManifest, spackzip;
	private static String updateURL, updateManifest;

	/**
	 * verifica si es necesario instalar nuevas actualizaciones de software descargando una copia del manifiesto y
	 * verificando la fecha de generacion. si es procedente, se solicita autorizacion para la instalacion. el archivo de
	 * manifiesto se graba en la carpeta de recursos
	 */
	public static void checkForPTF() {
		try {
			updateManifest = SystemVariables.getStringVar("updateManifest");
			updateURL = (String) SystemVariables.getStringVar("updateURL");
			if (updateURL.equals("*NO_UPDATE")) {
				return;
			}
			tmpManifest = File.createTempFile(updateManifest, null);
			AbstractDownload ad = DownloadManager.getDownload(updateURL, updateManifest, tmpManifest.toString());
			if (ad != null) {
				ActionListener al = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						UpdateManager.checkManifest();
					};
				};
				DownloadManager.download(ad, al, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void checkManifest() {
		try {
			Properties prp = new Properties();
			prp.load(new FileInputStream(tmpManifest));

			// busca identificador
			String luid = SystemVariables.getStringVar("updateID");
			newID = prp.getProperty("updateID");
			if (!newID.equals(luid)) {
				newVer = prp.getProperty("versionID");
				String fn = (String) TPreferences.getPreference("updateZipFile", "", "update.zip");
				spackzip = File.createTempFile(fn, null);
				AbstractDownload ad = DownloadManager.getDownload(updateURL, fn, spackzip.toString());
				if (ad != null) {
					ActionListener al = new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							UpdateManager.installPTF();
						};
					};
					DownloadManager.download(ad, al, true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ejecuta un script de base de datos contenido dentro de las actualizaciones.
	 * 
	 * @param zf - zipfile
	 * @param ze - entrada con el contenido del script
	 * @throws IOException
	 */
	private static void executeDBScript(ZipFile zf, ZipEntry ze) throws IOException {
		File f = getZipEntry(zf, ze);
		Properties prp = new Properties();
		prp.load(new FileInputStream(f));
		Enumeration enume = prp.keys();
		while (enume.hasMoreElements()) {
			String nam = (String) enume.nextElement();
			String t_n[] = prp.getProperty(nam).split(";");
			System.out.println("UpdateManager.executeDBScript()");
//			ServiceConnection.sendTransaction(ServiceRequest.DB_EXECUTE_UPDATE, t_n[0], t_n[1]);
		}
		f.delete();
	}

	/**
	 * extrae el archivo identificado en <code>ze</code> dentro del archivo empaquetado pasado como argumento
	 * 
	 * @param zf - archivo
	 * @param ze - entrada
	 * @return archivo temporal con entrada descomprimida
	 */
	private static File getZipEntry(ZipFile zf, ZipEntry ze) throws IOException {
		File f = File.createTempFile("tmp", null);
		BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(f));
		BufferedInputStream is = new BufferedInputStream(zf.getInputStream(ze));
		int byted;
		while ((byted = is.read()) != -1) {
			fos.write(byted);
		}
		is.close();
		fos.close();
		return f;
	}

	/**
	 * para cada elemento de la lista(archivos), extrae los datos y los coloca en sus ubicaciones
	 * 
	 * @param jf - paquete origen de datos
	 * @param v - lista de elementos a instalar
	 */
	private static void install(ZipFile jf, Vector v) throws IOException {
		for (int l = 0; l < v.size(); l++) {
			ZipEntry ze = (ZipEntry) v.elementAt(l);
			String sfn = System.getProperty("user.dir") + "/" + ze.getName();
			File fn = new File(sfn);
			if (ze.isDirectory()) {
				fn.mkdirs();
				continue;
			}
			if (fn.exists()) {
				fn.delete();
				fn = new File(sfn);
			} else {
				fn.getParentFile().mkdirs();
				fn = new File(sfn);
				fn.createNewFile();
			}
			File fe = getZipEntry(jf, ze);
			fe.renameTo(fn);
		}
	}

	/**
	 * instalacion de actualizaciones. se hace una lista de los archivos contenidos dentro de este paquete (todos menos
	 * ptfaaaammdd) se respaldan y se instalan los nuevos
	 */
	private static void installPTF() {
		Vector v = new Vector();
		boolean reset = false;
		try {
			ZipFile jf = new ZipFile(spackzip);
			Enumeration e = jf.entries();
			while (e.hasMoreElements()) {
				ZipEntry ze = (ZipEntry) e.nextElement();
				String zefn = ze.getName();
				// script de base de datos
				if (zefn.endsWith("sql")) {
					executeDBScript(jf, ze);
					reset = true;
				} else {
					v.addElement(ze);
				}
			}
			if (v.size() > 0) {
				// backup(v);
				install(jf, v);
				reset = true;
			}
			// reiniciar??
			if (reset) {
				// actualiza datos, borrar y reiniciar aplicacion
				SystemVariables.updateVar("updateID", newID);
				SystemVariables.updateVar("versionID", newVer);
				// redenomina para updatemanifestViewer
				File f = new File(updateManifest);
				f.delete();
				tmpManifest.renameTo(f);
				spackzip.delete();
				// consola muestra mensaje y reinicia
				JOptionPane.showMessageDialog(null, TStringUtils.getString("update.ms01"),
						TStringUtils.getString("r02"), JOptionPane.INFORMATION_MESSAGE);
				System.out.println("UpdateManager.installPTF()");
				//				Exit.restarApplication();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
