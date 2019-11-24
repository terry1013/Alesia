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

import gui.*;

import java.beans.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import action.*;
import core.*;

/*
 *  soporte base para descargas
 */
public class AbstractDownload implements Runnable, PropertyChangeListener {

	protected long speed, totalToDownload, sizeDownloaded, lastTime, lastSize, beginByte;
	protected File targetFile;
	protected InputStream inputStream;
	protected TProgressMonitor progressPanel;
	protected OutputStream outputStream;
	protected int progress;
	private JLabel filespeed;
	private boolean isRunning;
	public int status;
	public Hashtable <String, Object>parameters;
	private boolean fistTime = true;

	/** nueva instancia
	 * 
	 * @param u - url
	 * @param tfi - archivo destino
	 */
	protected AbstractDownload(File tfi) {
		try {
			this.speed = 0l;
			this.lastTime = 0;
			this.targetFile = tfi;
			beginByte = targetFile.length();
			this.sizeDownloaded = 0;
			if (targetFile.exists()) {
				sizeDownloaded = targetFile.length();
			}
			FileOutputStream fos = new FileOutputStream(targetFile, sizeDownloaded > 0);
			this.outputStream = new BufferedOutputStream(fos);
			this.isRunning = true;
			this.parameters = new Hashtable<String, Object>();
		} catch (Exception e) {
			SystemLog.logException(e);
		}
	}

	/** suspende la ejecucion de esta instacia y elimina el archivo q esta instancia estaba 
	 * escribiendo
	 * 
	 */
	public void suspend() {
		try {
			isRunning = false;
			if (progressPanel != null) {
				progressPanel.dispose();
			}
			inputStream.close();
			outputStream.flush();
			outputStream.close();
			targetFile.delete();
		} catch (Exception e) {
			SystemLog.logException(e);
		}
	}

	/** indica si se desea que se presente la barra de progreso del descarga
	 * 
	 */
	protected void showProgressPanel() {
		this.progressPanel = new TProgressMonitor("download32", "pp01", null, true);
		JLabel file = new JLabel(" ");
		filespeed = new JLabel(" ");
		/*
		progressPanel.appendToPanel(Box.createVerticalStrut(4));
		progressPanel.appendToPanel(TUIUtils.getInHoriszontalBox(filespeed));
		progressPanel.appendToPanel(Box.createVerticalStrut(4));
		progressPanel.appendToPanel(TUIUtils.getInHoriszontalBox(file));
		progressPanel.addCancelListener(this);
		*/
		String tx = TStringUtils.getString("pp06") + " " + targetFile;
		file.setText(tx + " " + TStringUtils.formatSize(totalToDownload));
		progressPanel.setVisible(true);
	}

	/*
	 *  (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getNewValue() instanceof CancelOperation) {
			suspend();
		}
	}

	@Override
	public void run() {
		try {
			if (fistTime) {
				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
			}
			int byteRead;
			while (isRunning && (byteRead = inputStream.read()) != -1) {
				++sizeDownloaded;
				outputStream.write(byteRead);
				// speed counter
				long now = System.currentTimeMillis();
				if ((now - lastTime) > 1000) {
					lastTime = now;
					status = DownloadManager.DONWLOADING;
					speed = (sizeDownloaded - lastSize);
					progress = (int) (sizeDownloaded * 100 / totalToDownload);
					if (progressPanel != null) {
						progressPanel.setProgress(progress, null);
						filespeed.setText(TStringUtils.getString("pp08") + " "
							+ TStringUtils.formatSize(speed));
					}
					lastSize = sizeDownloaded;
					DownloadManager.updateStatus(this);
				}
			}
			if (isRunning) {
				status = DownloadManager.COMPLETE;
				DownloadManager.updateStatus(this);
				DownloadManager.done(this);
			}
		} catch (Exception e) {
			SystemLog.logException(e);
		}
	}
}
