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
package delete;

import java.io.*;
import java.net.*;
import java.util.logging.*;

import core.*;

public class ApplicationInstanceManager {

	private static ApplicationInstanceListener subListener;

	/** Randomly chosen, but static, high socket number */
	public static final int SINGLE_INSTANCE_NETWORK_SOCKET = 44331;

	/** Must end with newline */
	public static final String SINGLE_INSTANCE_SHARED_KEY = "$$NewInstance$$\n";

	/**
	 * Registers this instance of the application. 
	 * 
	 * @return true if first instance, false if not.
	 */
	public static boolean registerInstance() {
		// returnValueOnError should be true if lenient (allows app to run on network error) or false if strict.
		boolean returnValueOnError = true;
		// try to open network socket
		// if success, listen to socket for new instance message, return true
		// if unable to open, connect to existing and send new instance message, return false
		try {
			InetAddress ia = InetAddress.getByAddress(new byte[]{(byte) 127, (byte) 0, (byte) 0, (byte) 1});
			final ServerSocket socket = new ServerSocket(SINGLE_INSTANCE_NETWORK_SOCKET, 10, ia);
			Alesia.logger.info("Listening for application instances on socket " + SINGLE_INSTANCE_NETWORK_SOCKET);
			Thread instanceListenerThread = new Thread(new Runnable() {
				public void run() {
					boolean socketClosed = false;
					while (!socketClosed) {
						if (socket.isClosed()) {
							socketClosed = true;
						} else {
							try {
								Socket client = socket.accept();
								BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
								String message = in.readLine();
								if (SINGLE_INSTANCE_SHARED_KEY.trim().equals(message.trim())) {
									Alesia.logger.info("Shared key matched - new application instance found");
									fireNewInstance();
								}
								in.close();
								client.close();
							} catch (IOException e) {
								socketClosed = true;
							}
						}
					}
				}
			});
			instanceListenerThread.start();
			// listen
		} catch (UnknownHostException e) {
			Alesia.logger.log(Level.SEVERE, e.getMessage(), e);
			return returnValueOnError;
		} catch (IOException e) {
			Alesia.logger.info("Port is already taken.  Notifying first instance.");
			try {
				Socket clientSocket = new Socket(InetAddress.getLocalHost(), SINGLE_INSTANCE_NETWORK_SOCKET);
				OutputStream out = clientSocket.getOutputStream();
				out.write(SINGLE_INSTANCE_SHARED_KEY.getBytes());
				out.close();
				clientSocket.close();
				Alesia.logger.info("Successfully notified first instance.");
				return false;
			} catch (UnknownHostException e1) {
				Alesia.logger.log(Level.SEVERE, e.getMessage(), e);
				return returnValueOnError;
			} catch (IOException e1) {
				Alesia.logger.severe("Error connecting to local port for single instance notification");
				Alesia.logger.log(Level.SEVERE, e.getMessage(), e);
				return returnValueOnError;
			}

		}
		return true;
	}
	public static void setApplicationInstanceListener(ApplicationInstanceListener listener) {
		subListener = listener;
	}

	private static void fireNewInstance() {
		if (subListener != null) {
			subListener.newInstanceCreated();
		}
	}

	public interface ApplicationInstanceListener {
		public void newInstanceCreated();
	}
/*
	public class MyApplication {
		public static void main(String[] args) {
			if (!ApplicationInstanceManager.registerInstance()) {
				// instance already running.
				System.out.println("Another instance of this application is already running.  Exiting.");
				System.exit(0);
			}
			ApplicationInstanceManager.setApplicationInstanceListener(new ApplicationInstanceListener() {
				public void newInstanceCreated() {
					System.out.println("New instance detected...");
					// this is where your handler code goes...
				}
			});
		}
	}
	*/
}
