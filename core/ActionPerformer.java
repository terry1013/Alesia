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

import javax.swing.*;

import action.*;


/** esta clase define un protocolo de comunicaciones entre acciones y clases que provee 
 * componentes visuales de entrada. Una accion es creada con una clase que implementa 
 * este interface, al momento de la ejecucion de la accion, se solicita el componente 
 * visual. luago, al ejecutarse otra accion (instancia de <code>RedirecAction</code>
 * se redirige la solicitud hacia el metodo que agrupa las acciones segun las acciones
 * 
 */
public interface ActionPerformer {

	/** Retorna <code>UIComponentPanel</code> diceñado para la accion solicitada.
	 * clases implementan este medoto para retornar los paneles correspondientes a las
	 * acciones que se pasan como argumento. 
	 * 
	 * @param aa - accion solicitante
	 * @return el <code>UIComponentPanel</code> diceñado para la accion
	 */
	public abstract UIComponentPanel getUIFor(AbstractAction aa);
	
	/** invocado por las acciones para indicar que se desea procesar la accion. 
	 * clases que implementan este metodo efectuan las operaciones para que la accion se efectue. 
	 * El valor de retorno indica al la cadena de invocaciones si la ejecicion del comando fue 
	 * efectuada de manera exitosa.
	 * 
	 * @param event - Evento
	 * 
	 * 
	 * @return <code>true</code> si la ejecicion fue exitosa
	 */
	public boolean executeAction(TActionEvent event);
		
}
