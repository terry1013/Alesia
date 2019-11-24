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
 * Created on 13/04/2005
 *
 */
package gui;

import javax.swing.*;

/** Clases que implementan este interface, puede ser presentadas en secuencia en forma de 
 * asistente para ... La comunicacion con este interface es controlada por <code>WizardContainer</code> 
 * el contrato general es:
 * 
 * o se inicializa el contenedor invocando <code>getComponent()</code> para todos los 
 * paneles dentro de la lista a presentar.
 * 
 * para cada panel:
 * 
 * <code>initializePanel</code> antes de presentar el componente visual
 * <code>validateFields</code> al momento de presionar Siguiente >> para validacion de
 * datos
 * 
 * 
 */
public interface WizardPanel1 {
	
	/** retorna los datos de entrada seleccionados o introducidos por el usuario
	 * dentro de este panel de entrada. La clase del objeto depende de la implementacion
	 * de la instancia. por tanto, es responsabilidad del solicitante 
	 * conocer de antemano la clase del objeto que retorna este metodo
	 * para el panel especifico del cual solicida datos.
	 * 
	 * @return Datos
	 */
	public Object getData() ;
	
	/** este metodo es invocado antes de que <code>WizardsContainer</code>
	 * haga visibles este panel. se debe implementar este metodo
	 * cuando es necesario conocer la informacion descrita en(los) panel(es)
	 * anteriores a este. Use el metodo 
	 * <code>WizardContainer.getPanel(int).getData()</code>. el valor de retorno indica al 
	 * contenedor si este panel desea ser presentado dentro de la secuencia del asistente. 
	 *  
	 *
	 */
	public void initializePanel(WizardContainer arg0) ;

	/** Este metodo es invocado por <code>WizardContainer</code> 
	 * antes de presentar el siguiete panel dentro del asistente o de finalizar
	 * este. clases deben implementar este metodo si desean que el contenedor detenga la 
	 * secuencia. 
	 * 
	 * Este metodo puede usarse cuando un panel determina que es necesario alterar 
	 * la secuencia del asistente, llamando a los metodos 
	 * <code>arg0.insertWizardPanel o arg0.removeWizardPanel</code>
	 * 
	 * @param newParam - Contenedor
	 * @return <code>true</code> si se desea detener la secuencia
	 */	
	public boolean validateWizardPanel(WizardContainer newParam) ;
	
	/** invocado por el contenedo cuando se desea conocer el componente visual que sera presentado
	 * en dentro de la secuencia. 
	 * 
	 * @return componente visual 
	 */
	public JComponent getComponent(); 

}
