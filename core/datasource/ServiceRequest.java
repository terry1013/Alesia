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
package core.datasource;

import java.io.*;
import java.util.*;

/**
 * esta clase contiene una solicitud enviada a travez de la coneccion entre el client y el servidor. esta solicitud
 * contiene toda la informacion necesaria para iniciar un servicio en el servidor. esta solicitud viaja entre el cliente
 * y el seridor y esta dice;ada para unificar un paquete complento de informacion.
 * 
 */
public class ServiceRequest implements Serializable {

	public static final String DB_QUERY = "DBQuery";
	public static final String DB_QUERY_SIZE = "DBQuerySize";
	public static final String DB_DELETE = "DeleteRecord";
	public static final String DB_ADD = "AddRecord";
	public static final String DB_UPDATE = "UpdateRecord";
	public static final String DB_WRITE = "WriteRecord";
	public static final String DB_EXIST = "ExistRecord";
	public static final String DB_REQUEST_MODEL = "RequestModel";
	public static final String DB_COMPLEX_QUERY = "DBComplexQuery";
	public static final String DB_EXECUTE_UPDATE = "ExecuteUpdate";
	public static final String DB_JOIN_QUERY = "DBJoinQuery";

	/**
	 * nombre de archivo de base de datos
	 * 
	 */
	public static final String TABLE_NAME = "tableName";

	/**
	 * parametro para ordenamiento por columna
	 * 
	 */
	public static final String ORDER_BY = "orderBy";
	
	/**
	 * parametro para ignorar seguridad para servicios. ver documentacion de {@link DBAccess#ignoreSecurity()}
	 * 
	 */
	public static final String IGNORE_SECURITY = "ignoreSecurity";

	/**
	 * parametro para servicio que retornen Vector<Record> para filtrar lista resultante. se debe encontrar el valor
	 * descrito en FILTER_VALUE dentro de la lista de campos (separados por ;) descritos en FILTER_FIELDS
	 */
	public static final String FILTER_FIELDS = "FilterFields";
	public static final String FILTER_VALUE = "FilterValue";

	/**
	 * Parametros para {@link ServiceRequest#DB_JOIN_QUERY}
	 */
	public static final String RIGHT_TABLE_NAME = "RightTableName";
	public static final String LEFT_KEY_FILED = "LeftKeyField";
	public static final String RIGHT_KEY_FIELD = "RightKeyField";
	public static final String RIGHT_FIELDS = "RightFields";
	
	/**
	 * parametros para {@link TreeViewFromParentChildrenTables}
	 */
	public static final String CHILDREN_TABLE_NAME = "childrentablename";
	public static final String PARENT_STRING = "parentstring";
	public static final String CHILDREN_STRING = "childrenstring";
	public static final String PARENT_JOIN_BY = "parentjoin";
	public static final String CHILDREN_JOIN_BY = "childrenjoin";
	public static final String NODE_FIELD = "nodefieldname";
	public static final String SUB_NODE_FIELD = "subnodefieldname";
	
	/**
	 * este servicio no esta definido como instancia de <code>AbstractTransaction</code>. use cuando una clase genera
	 * sus propios datos y desea usarla en otra clase a traves de metodos como
	 * <code>setServiceReques(ServiceRequest)</code> Ej: los reportes pueden generar su propia lista de elementos. como
	 * resutado deben crear una instancia de <code>TAbstractTableModel</code>
	 * 
	 */
	public static final String CLIENT_GENERATED_LIST = "ClientGeneratedList";

	// lista de compañias autorizadas segun usuario en sesion
	public static final String PARTNERS_LIST = "PartnersList";

	private String rName;
	private Object rData;
	private HashMap parameters;

	/**
	 * nueva instancia
	 * 
	 * @param nam nombre de solicitud
	 * @param tn - nombre de archivo de base de datos (si aplica) o <code>null</code>
	 * @param dta - datos iniciales segun solicitud de servicio (si aplica) o <code>null</code>
	 */
	public ServiceRequest(String nam, String tn, Object dta) {
		this.rName = nam;
		this.rData = dta;
		this.parameters = new HashMap();
		parameters.put(TABLE_NAME, tn);
	}

	/**
	 * retorna el nombre de la tabla a la cual se desea accesar. este parametro puede ser null si el servicion no es de
	 * base de datos
	 * 
	 * @return nombre de tabla
	 */
	public String getTableName() {
		return (String) parameters.get(TABLE_NAME);
	}

	/**
	 * nombre de tabla para esta solicitud de servicio
	 * 
	 * @param tn - nombre de table
	 */
	public void setTableName(String tn) {
		parameters.put(TABLE_NAME, tn);
	}

	/**
	 * retorna el nombre del servicio a ejecutar
	 * 
	 * @return nombre de la transaccion
	 */
	public String getName() {
		return rName;
	}

	/**
	 * Retorna los datos requeridos por el servicio. estos datos pueden variar y es resposabilidad del programador la
	 * coordinacion de estos entre el cliente y el servidor
	 * 
	 * @return datos
	 */
	public Object getData() {
		return rData;
	}

	/**
	 * establece los datos para esta solicitud de servicio
	 * 
	 * @param dta - datos
	 */
	public void setData(Object dta) {
		this.rData = dta;
	}

	/**
	 * Retorna el valor para el nombre del parametro pasado como argumento
	 * 
	 * @param pn - nombre del parametro
	 * @return Objeto valor
	 */
	public Object getParameter(String pn) {
		return parameters.get(pn);
	}

	public HashMap getParameters() {
		return parameters;
	}

	/**
	 * establece parametros para esta transaccion
	 * 
	 * @param pn - Nombre del parametro
	 * @param val - Valor del parametro
	 */
	public void setParameter(String pn, Object val) {
		this.parameters.put(pn, val);
	}
}
