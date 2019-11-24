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

package gui;

import java.awt.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.JFormattedTextField.*;
import javax.swing.text.*;

import core.*;

/**
 * especializacion de <code>InputVerifier</code> para la verificacion de los datos de entrada. Adicionalmente, esta
 * atento si el componente no puede contener 0 o blanco (es requerido). Este verificador emite mensajes directamente al
 * panel que contiene al componente segun algunos de los eventos que puedan ocurrir. tambien cambia el color del fonde
 * del compoente para indicar donde se esta cometiendo la falta (color segun la excepcion). Segun el tipo de constructor
 * que se emplee, se evaluaran si el contenido de los campos numericos es acorde con la conbinacion de argumentos. esta
 * clase sabe que tipo de validacion aplicar segun el contructur que sea usado.
 */
public class TInputVerifier extends InputVerifier {

	private boolean required;
	private Color orgColor;
	private long lowRange, highRange;
	private boolean advance;
	private TError reason;

	/**
	 * nuevo verificador de entrada
	 * 
	 * @param rp - <code>RightPanel</code> donde enviar los mensajes
	 * @param req =true, este componente no puede estar en blanco
	 */
	public TInputVerifier(boolean req) {
		this.required = req;
		this.orgColor = null;
		this.lowRange = 0;
		this.highRange = 0;
		this.advance = false;
		this.reason = null;
	}

	/**
	 * nuevo verificador de entrada. segun la conbinacion de parametro <code>lr, tr</code> este verificador tambien
	 * emitira mensajes para los campos numerios. las conbinaciones son: lr == 0, tr == Long.MAX_VALUE debe ser positivo
	 * lr == Long.MIN_VALUE, tr == 0 debe ser negativo TODO: lr != 0, tr != 0 y ninguno es minimo o maximo se verifica
	 * que el valor para el campo este dentro del rango. lr != 0, tr == 0 el valor para el campo debe ser mayor al valor
	 * de <code>lr</code> lr == 0, tr != 0 el valor para el campo debe ser menor al valor de <code>tr</code>
	 * 
	 * 
	 * @param rp - <code>RightPanel</code> donde enviar los mensajes
	 * @param req - =true, este componente no puede estar en blanco
	 * @param lr - rango inferior
	 * @param tr - rango superior
	 */
	public TInputVerifier(boolean req, long lr, long tr) {
		this(req);
		this.lowRange = lr;
		this.highRange = tr;
		this.advance = true;
	}

	/**
	 * determina si hay un valor en un campo requerido. Si el campo no es requerido retorna true;
	 * 
	 * @param input - componente de entrada
	 * @return =true si el campo componente de entrada contiene un valor distinto de 0 o blanco o este no es de entrada
	 *         obligatoria
	 */
	private boolean requiredValueSet(JComponent input) {
		boolean req = true;
		String t = (((JTextComponent) input).getText()).trim();
		if (required) {
			// jformattedtextfield para fecha y numeros
			if (input instanceof JFormattedTextField) {
				JFormattedTextField ftf = (JFormattedTextField) input;
				AbstractFormatter formatter = ftf.getFormatter();
				try {
					Object val = formatter.stringToValue(t);
					req = (val instanceof Date);
					if (val instanceof Number) {
						req = (((Number) val).doubleValue() != 0.0);
					}
				} catch (Exception e) {
					req = false;
				}

				// jtextField para texto
			} else {
				req = !(t.trim().equals(""));
			}
		}
		return req;
	}

	public boolean shouldYieldFocus(JComponent input) {
		verify(input);

		// color de fondo del componente
		if (orgColor == null) {
			orgColor = input.getBackground();
		}

		// presenta resultado de validacion
		input.setBackground(orgColor);
		if (reason != null) {
			input.setBackground(reason.getExceptionColor());
		}

		return reason == null;
	}

	public boolean verify(JComponent input) {
		reason = null;
		if (input instanceof JFormattedTextField) {
			JFormattedTextField ftf = (JFormattedTextField) input;
			AbstractFormatter formatter = ftf.getFormatter();
			if (formatter != null) {
				try {
					String t = ftf.getText();
					formatter.stringToValue(t);
					reason = requiredValueSet(input) ? null : new TError("msg01");
					if (reason == null) {
						reason = advanceVerification(input);
					}
				} catch (ParseException pe) {
					reason = new TError("msg02");
				}
			}
		} else {
			reason = requiredValueSet(input) ? null : new TError("msg02");
		}
		return reason == null;
	}

	/**
	 * return the reason for validation fail or <code>null</code>if the input value is valid
	 * 
	 * @return reason
	 */
	public TError getReason() {
		/*
		 * 190611: Esta clase la hice cuando estaba aprendiendo java hace 10 anos. hoy, solo tengo que quitar unas cosas
		 * y agregar otras !!! :D gratamente sorprendido. porque incluso hoy, estas viejas tecnicas todavia funcionan
		 * !!!!!!
		 */
		return reason;
	}

	/**
	 * inicia la validacion avanzada de datos. solo disponible para campos numeriocos y cuando esta clase sea construida
	 * usando el segundo constructor.
	 * 
	 * @param input - componente de entrada
	 * @return expepcion indicando la razon o <code>null</code> si no exite error
	 */
	private TError advanceVerification(JComponent input) {
		TError ape = null;
		if (advance) {
			double value = 0;
			if (input instanceof JFormattedTextField) {
				JFormattedTextField ftf = (JFormattedTextField) input;
				AbstractFormatter formatter = ftf.getFormatter();
				try {
					String t = (((JTextComponent) input).getText()).trim();
					value = ((Number) formatter.stringToValue(t)).doubleValue();
				} catch (Exception e) {

				}

				// datos
				Object[] mdta = new Object[]{"", "", "", ""};

				// rango
				if (lowRange != Long.MIN_VALUE && highRange != Long.MAX_VALUE
						&& (value < lowRange || value > highRange)) {
					mdta[0] = new Double(lowRange);
					mdta[1] = new Double(highRange);
					ape = new TError("ui.msg13");
				}

				// solo positivo
				if (lowRange == 0 && highRange == Long.MAX_VALUE && value < 0) {
					ape = new TError("ui.msg15");
				}

				// solo negativos
				if (lowRange == Long.MIN_VALUE && highRange == 0 && value > 0) {
					ape = new TError("ui.msg16");
				}

				// mayor
				if (lowRange != 0 && highRange == 0 && lowRange != Long.MIN_VALUE && value < lowRange) {
					mdta[2] = ">";
					mdta[3] = new Double(lowRange);
					ape = new TError("ui.msg14");
				}

				// menor
				if (lowRange == 0 && highRange != 0 && highRange != Long.MAX_VALUE && (value > highRange)) {
					mdta[2] = "<";
					mdta[3] = new Double(highRange);
					ape = new TError("ui.msg14");
				}

				// si exite algun error. da formato a mensaje
				if (ape != null) {
					ape.setMessage(MessageFormat.format(ape.getMessage(), mdta));
				}
			}
		}
		return ape;
	}
}
