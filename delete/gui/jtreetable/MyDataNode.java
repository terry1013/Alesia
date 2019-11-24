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
package delete.gui.jtreetable;

import java.util.*;

public class MyDataNode {

	private String name;
	private String capital;
	private Date declared;
	private Integer area;

	private List<MyDataNode> children;

	public MyDataNode(String name, String capital, Date declared, Integer area, List<MyDataNode> children) {
		this.name = name;
		this.capital = capital;
		this.declared = declared;
		this.area = area;
		this.children = children;

		if (this.children == null) {
			this.children = Collections.emptyList();
		}
	}

	public String getName() {
		return name;
	}

	public String getCapital() {
		return capital;
	}

	public Date getDeclared() {
		return declared;
	}

	public Integer getArea() {
		return area;
	}

	public List<MyDataNode> getChildren() {
		return children;
	}

	/**
	 * Knotentext vom JTree.
	 */
	public String toString() {
		return name;
	}
}
