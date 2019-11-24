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
/**
 * Copyright (c) Terry - All right reserved. PROPRIETARY/CONFIDENTIAL.
 * Use is subject to license terms.
 *
 * @author Terry
 *
 */
package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;

import javax.swing.*;

import org.fife.rsta.ac.*;
import org.fife.rsta.ac.java.*;
import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.*;


import core.*;
import delete.*;

/**
 * clase que permite la edicion para evaluacion de expresiones. este panel esta constituido por una editor de
 * expreisones y una barra de menu con 2 menues: Lista de archivos de base de datos y lists de constantes. ver
 * constructor para mas info
 * 
 */
public class TCodeEditor extends JPanel {

	private String dbFiles, cgFields, NodbFileds;
	private RSyntaxTextArea textArea;
	DefaultCompletionProvider provider;
	private JMenuBar menuBar;

	/**
	 * nueva instancia.
	 * 
	 * @param dbf - lista de archivos de base de datos para contruccion de menu. se aceptan multiples archivos separados
	 *        x ;
	 * @param ndbf - lista separadas por ; de los campos que no se añadiran a la lista de campos disponibles de DB
	 * @param cgf - lista de indentificadores de listas de constantes. se aceptan multiples identificadores de listas de
	 *        constantes separadas x ;
	 * @param scroll <code>true para RTextScrollPane</code> permite multiples lineas
	 */
	public TCodeEditor() {
		this.provider = new DefaultCompletionProvider();
		// languajeCopletionPrvider();
		setLayout(new BorderLayout());
		this.textArea = new RSyntaxTextArea();
		
		LanguageSupportFactory lsf = LanguageSupportFactory.get();
		LanguageSupport support = lsf.getSupportFor(SyntaxConstants.SYNTAX_STYLE_JAVA);
		JavaLanguageSupport jls = (JavaLanguageSupport)support;

		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		textArea.setMarkOccurrences(true);
		textArea.setCodeFoldingEnabled(true);

		lsf.register(textArea);
		ToolTipManager.sharedInstance().registerComponent(textArea);

		// textArea.setAntiAliasingEnabled(true);
		try {
			FileInputStream fis = new FileInputStream(TResources.getFile("eclipse.xml"));
			Theme theme = Theme.load(fis);
			theme.apply(textArea);
		} catch (IOException ioe) {
			SystemLog.logException(ioe);
		}

		AutoCompletion ac = new AutoCompletion(provider);
		ac.install(textArea);

		this.menuBar = new JMenuBar();
		insertFunctionMenu();
		add(menuBar, BorderLayout.NORTH);
		add(new RTextScrollPane(textArea), BorderLayout.CENTER);
		// add(new JScrollPane(outConsole), BorderLayout.SOUTH);
	}

	/**
	 * inserta menu de funciones para spreadsheet
	 * 
	 */
	public void insertFunctionMenu() {
		JMenu jm = new JMenu(TStringUtils.getString("sp01"));
		TEntry[] funs = TStringUtils.getTEntryGroup("sp.men");
		for (TEntry fun : funs) {
			String fn = fun.getKey().toString();
			String ke = TStringUtils.getString("pl" + fun.getValue().toString());
			TCodeEditorAA a = new TCodeEditorAA(ke, fn, "String");
			JMenuItem jmi = new JMenuItem(a);
			jm.add(jmi);

			TUIUtils.setToolTip("tt" + fun.getValue().toString(), jmi);
		}
		menuBar.add(jm);
	}

	/**
	 * establece una expresion en el area de texto
	 * 
	 * @param ex - expresion
	 */
	public void setExpression(String ex) {
		textArea.setText(ex);
	}

	/**
	 * retorna la expresion tipeada
	 * 
	 * @return expresion
	 */
	public String getExpression() {
		return textArea.getText();
	}

	/**
	 * establece CompletionProvider con instrucciones para java y MVEL
	 * 
	 */
	private void languajeCopletionPrvider() {
		// java
		provider.addCompletion(new BasicCompletion(provider, "abstract"));
		provider.addCompletion(new BasicCompletion(provider, "assert"));
		provider.addCompletion(new BasicCompletion(provider, "break"));
		provider.addCompletion(new BasicCompletion(provider, "case"));
		provider.addCompletion(new BasicCompletion(provider, "catch"));
		provider.addCompletion(new BasicCompletion(provider, "class"));
		provider.addCompletion(new BasicCompletion(provider, "const"));
		provider.addCompletion(new BasicCompletion(provider, "continue"));
		provider.addCompletion(new BasicCompletion(provider, "default"));
		provider.addCompletion(new BasicCompletion(provider, "do"));
		provider.addCompletion(new BasicCompletion(provider, "else"));
		provider.addCompletion(new BasicCompletion(provider, "enum"));
		provider.addCompletion(new BasicCompletion(provider, "extends"));
		provider.addCompletion(new BasicCompletion(provider, "final"));
		provider.addCompletion(new BasicCompletion(provider, "finally"));
		provider.addCompletion(new BasicCompletion(provider, "for"));
		provider.addCompletion(new BasicCompletion(provider, "goto"));
		provider.addCompletion(new BasicCompletion(provider, "if"));
		provider.addCompletion(new BasicCompletion(provider, "implements"));
		provider.addCompletion(new BasicCompletion(provider, "import"));
		provider.addCompletion(new BasicCompletion(provider, "instanceof"));
		provider.addCompletion(new BasicCompletion(provider, "interface"));
		provider.addCompletion(new BasicCompletion(provider, "native"));
		provider.addCompletion(new BasicCompletion(provider, "new"));
		provider.addCompletion(new BasicCompletion(provider, "package"));
		provider.addCompletion(new BasicCompletion(provider, "private"));
		provider.addCompletion(new BasicCompletion(provider, "protected"));
		provider.addCompletion(new BasicCompletion(provider, "public"));
		provider.addCompletion(new BasicCompletion(provider, "return"));
		provider.addCompletion(new BasicCompletion(provider, "static"));
		provider.addCompletion(new BasicCompletion(provider, "strictfp"));
		provider.addCompletion(new BasicCompletion(provider, "super"));
		provider.addCompletion(new BasicCompletion(provider, "switch"));
		provider.addCompletion(new BasicCompletion(provider, "synchronized"));
		provider.addCompletion(new BasicCompletion(provider, "this"));
		provider.addCompletion(new BasicCompletion(provider, "throw"));
		provider.addCompletion(new BasicCompletion(provider, "throws"));
		provider.addCompletion(new BasicCompletion(provider, "transient"));
		provider.addCompletion(new BasicCompletion(provider, "try"));
		provider.addCompletion(new BasicCompletion(provider, "void"));
		provider.addCompletion(new BasicCompletion(provider, "volatile"));
		provider.addCompletion(new BasicCompletion(provider, "while"));
		provider.addCompletion(new BasicCompletion(provider, "null"));

		// MVEL
		provider.addCompletion(new BasicCompletion(provider, "with"));
		provider.addCompletion(new BasicCompletion(provider, "assert"));
		provider.addCompletion(new BasicCompletion(provider, "isdef"));
		provider.addCompletion(new BasicCompletion(provider, "contains"));
		provider.addCompletion(new BasicCompletion(provider, "is"));
		provider.addCompletion(new BasicCompletion(provider, "instanceof"));
		provider.addCompletion(new BasicCompletion(provider, "strsim"));
		provider.addCompletion(new BasicCompletion(provider, "soundslike"));
		provider.addCompletion(new BasicCompletion(provider, "in"));
		provider.addCompletion(new BasicCompletion(provider, "empty"));
		provider.addCompletion(new BasicCompletion(provider, "foreach"));
		provider.addCompletion(new BasicCompletion(provider, "def"));
		provider.addCompletion(new BasicCompletion(provider, "var"));
		provider.addCompletion(new BasicCompletion(provider, "macro"));

		// dinamicreport metods
		try {
			Method[] m = DR.class.getDeclaredMethods();
			// Method[] m = Class.forName("plugin.dinamicreport.datasource.DR").getClass().getMethods();
			for (Method method : m) {
				String s = method.getName();
				Class[] pt = method.getParameterTypes();
				String par = "(";
				if (pt.length > 1) {
					for (Class class1 : pt) {
						par += class1.getSimpleName() + ", ";
					}
					// System.out.println(">"+par+"<");
					par = par.substring(0, par.length() - 2);
				}
				provider.addCompletion(new BasicCompletion(provider, s + par + ")"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * acion para colocar en la barra de muenu
	 * 
	 */
	public class TCodeEditorAA extends AbstractAction {

		/**
		 * nueva instancia
		 * 
		 * @param key - clave. es el nombre de campo de base de datos o valor para constante
		 * @param val - nombre a mostrar
		 * @param ty - tipo de datos
		 */
		public TCodeEditorAA(String key, String val, String ty) {
			super(val);
			putValue("key", key);
			putValue(AbstractAction.SMALL_ICON, TResources.getSmallIcon(ty));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			textArea.insert((String) getValue("key"), textArea.getCaretPosition());
		}
	}
}
