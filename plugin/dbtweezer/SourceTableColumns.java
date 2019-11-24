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
package plugin.dbtweezer;

import gui.*;
import gui.docking.*;
import java.beans.*;
import javax.swing.*;
import com.alee.utils.*;
import action.*;
import core.*;
import core.datasource.*;

public class SourceTableColumns extends UIListPanel implements DockingComponent {

	private EditRecord2 colScrip;
	private EditRecord2 editColumn;

	/**
	 * new instance
	 */
	public SourceTableColumns() {
		super(null);
		// column script
		colScrip = new EditRecord2(this) {
			@Override
			public void actionPerformed2() {
				super.actionPerformed2();
				DockingContainer.signalFreshgen(ScriptList.class.getName());
			}
		};
		colScrip.setDefaultValues("ColumnScript");
		colScrip.setAllowWrite(true);

		// alter columnd
		editColumn = new EditRecord2(this) {
			@Override
			public void actionPerformed2() {
				super.actionPerformed2();
				DockingContainer.signalFreshgen(ScriptList.class.getName());
			}
		};
		editColumn.setDefaultValues("AlterColumn");
		editColumn.setAllowWrite(true);

		setToolBar(editColumn);
//		addToolBarAction(colScrip);
		setColumns("column_name;type_name;column_size;decimal_digits;column_def;is_nullable;remarks");
		setIconParameters("-1; ");
	}

	@Override
	protected void enableRecordScopeActions(boolean ena) {
		super.enableRecordScopeActions(ena);
		// columns script only for numeric fields
		Record rcol = getRecord();
		if (rcol != null) {
			String dds = rcol.getFieldValue("decimal_digits").toString();
			int dd = dds.equals("") ? 0 : Integer.valueOf(dds);
			colScrip.setEnabled(dd > 0);
		}
	}

	@Override
	public UIComponentPanel getUIFor(AbstractAction aa) {
		UIComponentPanel pane = null;

		if (aa == colScrip) {
			Record srcd = getRecord();
			String patt = FileUtils.readToString(TResources.getFile("columnScript.groovy"));
			Record scmod = DBTweezer.getNewScriptRecord("ColumnScript", "Sql", srcd, patt);
			scmod.setFieldValue("s_sctype", "Groovy");
			pane = new GroovyScriptUI(scmod);
		}

		if (aa == editColumn) {
			Record colrcd = getRecord();
			Record scmod = DBTweezer.getNewScriptRecord("AlterColumn", "Sql", colrcd, "");
			pane = new AlterColumn(colrcd, scmod);
		}
		return pane;
	}

	@Override
	public void init() {
		setMessage("dbt.ui.msg01");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object src = evt.getSource();
		Object newv = evt.getNewValue();
		if (src instanceof SourceDBTables) {
			Record rcd = (Record) newv;
			if (rcd != null) {
				DBTConnectionManager sdb = DBTConnectionManager.getSourceDB();
				setServiceRequest(sdb.getServiceRequestFor(DBTConnectionManager.TABLE_COLUMNS,
						rcd.getFieldValue("table_name").toString()));
			} else {
				setMessage("dbt.ui.msg01");
			}
		}
	}
}
