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

import java.util.*;

import core.datasource.*;

/**
 * An object whose its internal list of data can be expoerted to external format. this object has a
 * {@link ServiceRequest} that feed its internal data structure and this data wish to be expoerted.
 * <p>
 * This implementation expect a {@link Vector} of Records when the asociated {@link ServiceResponse} is send to process.
 * generaly {@link ServiceRequest#DB_QUERY}, {@link ServiceRequest#CLIENT_GENERATED_LIST} or similar
 * 
 */
public interface Exportable {

	/**
	 * Return the {@link ServiceRequest} that is the datasource for this list of elements. the espected data of
	 * asociated {@link ServiceResponse} must be a vector of records
	 * 
	 * @return ServiceRequest
	 */
	public ServiceRequest getServiceRequest();

}
