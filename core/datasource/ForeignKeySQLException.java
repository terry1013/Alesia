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
/*			
 * Copyright (c) QQ - All right reserved

 */
package core.datasource;

import java.sql.*;

/** exception que transmite la informacion detallada de una falla en la operacion de supresion 
 * debido a claves foraneas
 *  
 */
public class ForeignKeySQLException extends SQLException {
	
	/** nueva instancia
	 * 
	 * @param e - <code>SQLException</code> original
	 * @param inf - informacion ampliada 
	 */
	public ForeignKeySQLException(SQLException e, String inf) {
		super(inf, e.getSQLState(), e.getErrorCode());
	}
}
