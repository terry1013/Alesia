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
package delete;

import java.util.*;

import core.datasource.*;

public class RecordList extends Vector<Record> {
	
	private Record model = null;
	
	public RecordList() {
		super();
	}
	public RecordList(int siz) {
		super(siz);
	}
	
	public RecordList(Record m) {
		this();
		this.model = m;
	}
	
	public void setModel(Record mod) {
		this.model = mod;
	}
	public Record getModel() {
		if (model == null) {
			throw new NullPointerException("Recod model not established");
		}
		return model;
	}
}
