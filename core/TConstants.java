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

import gui.*;
import gui.tree.*;

import javax.swing.*;
import javax.swing.tree.*;

import core.datasource.*;

/**
 * A collection of constants for general application purpose
 * 
 * @author terry
 * 
 */
public interface TConstants {

	/**
	 * boton por omision para aceptar cambios dentro de un componente de entrada. Clase de la propiedad: propiedad:
	 * {@link JButton}
	 */
	public static final String DEFAULT_BUTTON = "DefaultButton";
	/**
	 * boton por omision para cancelar o cerrar ventanas o dialogos dentro de un componente de entrada. Clase de la
	 * propiedad: {@link JButton}
	 */
	public static final String DEFAULT_CANCEL_BUTTON = "DefaultCancelButton";
	/**
	 * acciones ejecutada. disparada por acciones instancias de <code>TAbstractAction</code>. clase de la propiedad:
	 * <code>TAbstractAction</code>
	 * 
	 */
	public static final String ACTION_PERFORMED = "ActionPerformed";
	/**
	 * elemento dentro de una lista seleccionado. la clse de propiedad: Record
	 * 
	 */
	public static final String RECORD_SELECTED = "RecordSelected";
	/**
	 * path selected form {@link PathSelector}. the property are an array of {@link TEntry}
	 */
	public static final String PATH_SELECTED = "patSelected";

	/**
	 * Database connection has change. Property it the record from T_CONNECTIONS of the target database connection
	 */
	public static final String CONNECTION_CHANGE = "connectionChange";

	
	/**
	 * dentro de una lista de elementos, indica los parametros necesarios para presentar el icono que adorna la celda.
	 * los parametros descritos en forma parm;parm;... clase de pripiedad String
	 * 
	 * @param column - Numero de la columna donde se desea presentar el icono (vista tabla)
	 * @param icon - nombre del archivo icono o prefijo (si se especifica la columna valcol). si este valor es *, el
	 *        nombre especificado en <code>valcol</code> debe ser instancia de byte[] donde esta almacenado el icono.
	 * @param valcol - nombre de la columna donde se obtendra el valor que sera concatenado con el nombre especificado
	 *        en parametro <code>icon</code> para deterinar el nombre del archivo icono (puede no especificarse). si
	 *        <code>icon="*"</code> el campo especificado aqui, contiene los byte[] para crear la imagen
	 *        <p>
	 *        ejemplo:
	 *        <li>0;user_;t_usroll: idica que se desa colocar en la columna 0 el icono cuyo nombre comienza con user_ y
	 *        usar el valor de la columna t_usroll para concaternarlo con el nombre del archivo icono
	 *        <li>3;users4: colocar el icono llamado users4 en la 4ta columna
	 *        <li>0;*;userphoto: crea un icono usando los byte[] almacenados en la columan userphoto y lo coloca en la
	 *        columna 0
	 * 
	 * 
	 */
	public static final String ICON_PARAMETERS = "IconParameters";

	/**
	 * set the JTable view in transpose mode. the {@linkplain RowHeaderJTable} is place as rowHeader in JScrollPane and
	 * setted with {@link #SHOW_COLUMNS}. the column for main table is now a concat bettwen this columname and column
	 * number. Property class String
	 */
	public static final String TRANSPORSE_COLUMN = "TransposeColumn";


	/**
	 * In tree view, set the level of expanded brach of the tree: 0 means expand only root, 1 expand root and the next
	 * brach and so on. -1 means dont expand the tree Property class: int
	 */
	public static final String TREE_EXPANDED = "treeExpanded";

	/**
	 * en vista de arbol, indica el nombre del campo cuyo valor identificara al archivo icono que sera usado para
	 * adornar la celda. Clase de propiedad: String
	 * <p>
	 * 180220: puede ser una lista de valores que indical los nombres de los campos donde se almacenan las instancias de
	 * imageIcon desde donde obtener iconos para elemento del arbol. los 3 campos representan respectivamente los
	 * valores para <code>Tree.closedIcon;Tree.openIcon;Tree.leafIcon</code>
	 * 
	 */
	public static final String TREE_ICON_FIELD = "treeIconField";

	/**
	 * vista de arbol. nombre del campo cuyo valor es usado para determinar el estado seleccionado/no seleccionado de
	 * una celda. esta propiedad automaticamente cambia la instancia a <code>TJCheckBoxTreeCellRenderer</code>. Clase de
	 * propiedad String
	 * 
	 */
	public static final String TREE_BOOLEAN_FIELD = "treeBooleanField";

	/**
	 * elemento dentro de una lista seleccionado. clase de propiedad <code>LTEntry</code>
	 * 
	 */
	public static final String LIST_ITEM_SELECTED = "ListItemSelected";

	/**
	 * dentro de una lista de elementos, indica una columna especia. columnas especiales contienen valores que comienzan
	 * con un "*". esto indica que operaciones como la eliminacion de registro no son permitidas. Clase de propiedad:
	 * <code>String</code>
	 * 
	 */
	public static final String SPECIAL_COLUMN = "SpecialColumn";

	// archivo seleccionado (openFileChooser)
	public static final String FILE_SELECTED = "FileSelected";
	// color seleccionado (openColorChooser)
	public static final String COLOR_SELECTED = "ColorSelected";
	// propiedad que indica que este evento no es generado por instanicas interactivas.
	public static String NO_INTERACTIVE_REPORT = "noInteractiveReport";
	// indica instancias de OutputConsole que existe nuevo mensaje para anotar (elemento a anotar puede ser de distintos
	// tipos)
	public static String LOG_MESSAGE = "logMessage";
	/**
	 * fired when user request filter or find a text inside a list of elements. the main list components
	 * {@link UIListPanel} reprocess the {@link ServiceRequest} showing only rows whose columns contain the test to
	 * find. {@link TAbstractTree} look inside of {@link TreeModel} showin only the brach that contains the string to
	 * find
	 */
	public static String FIND_TEXT = "findText";
	// propiedad emitida por vistacalendar cuando se selecciona una fecha en el el panel de dias (DayPanel)
	public static String CALENDAR_DATE_SELECTED = "DateSelected";
	// propiedad emitida por vistacalendar cuando finaliza calculo de eventos para para el mes presentado (DayPanel)
	public static String CALENDAR_EVENTS_PREPARED = "EventsPepared";

}
