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

/** interface que deben implementar las clases que desean usar las acciones
 * <code>PreviousAction, NextAction</code>
 * 
 */
public interface Navigator {
	
	/** ir a inicial
	 * 
	 *
	 */
	public void home();
	
	/** anterior
	 *
	 */
	public void previous();

	/** siguiente
	 *
	 */	
	public void next();
}
