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
package action;

import core.*;
import core.datasource.*;

/**
 * perform delete a table row. as part of {@link ConfirmAction} family, this action display a confirmation message
 * before execute the action. if user acept, delete al selected record from the {@link EditableList}
 * 
 * TODO: customize the message for 1 or multiples records
 */
public class DeleteRecord2 extends ConfirmAction implements NoActionForSpecialRecord {

	public DeleteRecord2() {
		super(TAbstractAction.RECORD_SCOPE);
		setIcon("DeleteRecord");
	}
	public DeleteRecord2(EditableList el) {
		this();
		editableList = el;
		messagePrefix = "action.delete.";
	}

	@Override
	public void actionPerformed2() {
		Record[] rcd = editableList.getRecords();
		for (Record r : rcd) {
			boolean ok = ConnectionManager.getAccessTo(r.getTableName()).delete(r);
			// if any error, the dbacces show the exeption. meanwhile, a suspend the delete task
			if (!ok) {
				break;
			}
		}
		editableList.freshen();
	}
}
