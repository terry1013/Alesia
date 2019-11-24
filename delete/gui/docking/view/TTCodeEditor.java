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
package delete.gui.docking.view;

import gui.*;
import gui.docking.*;

import java.beans.*;

import javax.xml.bind.*;

import action.*;
import core.*;
import core.datasource.*;

/**
 * componente para edicion de codigo para la generacion de reportes
 * 
 */
public class TTCodeEditor extends UIComponentPanel implements DockingComponent {

	ServiceRequest request;
	TCodeEditor codeEditor;

	/**
	 * nueva instancia
	 * 
	 * 
	 */
	public TTCodeEditor() {
		super(null, false);
		this.codeEditor = new TCodeEditor();
		setToolBar(new SaveAction(this));
		addWithoutBorder(codeEditor);
		addPropertyChangeListener(this);
	}

	@Override
	public void init() {
		DBAccess dba = ConnectionManager.getAccessTo("t_tasks");
		Record r = dba.exist("t_taname = 'mvel report'");
		String cod64 = (String) r.getFieldValue("t_tacode");
		String cod = new String(DatatypeConverter.parseBase64Binary(cod64));
		codeEditor.setExpression(cod);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(TConstants.ACTION_PERFORMED)) {

			// save code
			if (evt.getNewValue() instanceof SaveAction) {
				DBAccess dba = ConnectionManager.getAccessTo("t_report_definition");
				Record r = dba.getModel();
				r.setFieldValue("t_taname", "mvel report");
				r.setFieldValue("t_tadescription", "Report for testing mvel library");
				String cod = codeEditor.getExpression();
				String cod64 = DatatypeConverter.printBase64Binary(cod.getBytes());
				r.setFieldValue("t_tacode", cod64);
				dba.write(r);
				firePropertyChange(TConstants.LOG_MESSAGE, null, "codeeditor.savecode");
			}

			// evaluate
			/*
			 * if (evt.getNewValue() instanceof ExecuteCode) { try { Serializable s =
			 * MVEL.compileExpression(codeEditor.getExpression()); MVEL.executeExpression(s, new HashMap());
			 * 
			 * // verifica si se desea ver datos en visor if (DR.halt != null) { firePropertyChange(TConstants.HALT,
			 * null, DR.halt); } } catch (Exception e) { e.printStackTrace(); firePropertyChange(TConstants.LOG_MESSAGE,
			 * null, e); } }
			 */
		}
	}
}
