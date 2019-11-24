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
import java.awt.*;
import java.text.*;
import java.util.*;

import javax.swing.*;

/**
 * Encapsulate erros in the framework. Any instance of this class represent an error message whit correspondient visual
 * attributes like color of the error, type and icons
 * 
 * @author terry
 *
 */
public class TError {
	public static String ACTION = "action";
	public static String ERROR = "error";
	public static String INFORMATION = "information";
	public static String WARNING = "warning";

	public static Color ACTION_COLOR = new Color(190, 220, 250);
	public static Color ERROR_COLOR = new Color(255, 190, 230);
	public static Color INFORMATION_COLOR = new Color(230, 230, 255);
	public static Color WARNING_COLOR = new Color(255, 255, 220);

	public static int SHORT = 2000;
	public static int A_WHILE = 10000;
	public static int FOR_EVER = 999000;

	private Color exceptionColor;
	private int milis;
	private ImageIcon exceptionIcon;
	private String message;
	private String eType;

	/**
	 * nueva instancia
	 * 
	 * @param mid - identificador de mensaje
	 */
	public TError(String mid) {
		super();
		String[] a_m = TStringUtils.getString(mid).split(";");
		if (a_m.length < 2) {
			throw new NoSuchElementException("Error trying get exception ID " + mid);
		}
		if (a_m[0].equals("a")) {
			eType = ACTION;
			this.exceptionColor = ACTION_COLOR;
			this.milis = FOR_EVER;
		}
		if (a_m[0].equals("e")) {
			eType = ERROR;
			this.exceptionColor = ERROR_COLOR;
			this.milis = FOR_EVER;
		}
		if (a_m[0].equals("i")) {
			eType = INFORMATION;
			this.exceptionColor = INFORMATION_COLOR;
			this.milis = SHORT;
		}
		if (a_m[0].equals("w")) {
			eType = WARNING;
			this.exceptionColor = WARNING_COLOR;
			this.milis = A_WHILE;

		}
		this.message = a_m[1];
		this.exceptionIcon = TResources.getIcon(eType);
	}

	/**
	 * nueva instancia
	 * 
	 * @param mid - identificador de mensaje.
	 * @param dta - datos para ejecutar
	 */
	public TError(String mid, Object... dta) {
		this(mid);
		message = MessageFormat.format(message, dta);
	}

	/**
	 * Retorna el color asociado al tipo de excepcion
	 * 
	 * @return color
	 */
	public Color getExceptionColor() {
		return exceptionColor;
	}

	/**
	 * retorna icono para esta excepcion
	 * 
	 * @return icono
	 */
	public ImageIcon getExceptionIcon() {
		return exceptionIcon;
	}

	/**
	 * retorna el tipo de esta exepcion.
	 * 
	 * @return tipo de exepcion
	 */
	public String getExceptionType() {
		return eType;
	}

	public String getMessage() {
		return message;
	}

	/**
	 * Establece un nuevo mensaje de texto para esta exception
	 * 
	 * @param msg - mensaje
	 */
	public void setMessage(String msg) {
		this.message = msg;
	}

	/**
	 * retorna el tiempo que se espera se muestre esta exepcion.
	 * 
	 * @return tiempo en seg.
	 */
	public int getMiliSeconds() {
		return milis;
	}
}
