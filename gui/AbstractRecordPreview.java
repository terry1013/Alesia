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
 * Created on 22/03/2005
 * (c) QQ  
 */
package gui;

import gui.docking.*;

import java.awt.*;
import java.beans.*;

import javax.swing.*;



import core.*;
import core.datasource.*;





/** este componete presenta el contenido del una registro en un formato estandar que permite
 * una vista rapida de los valores de un registro de base de datos. subclases deben 
 * implementar el resto de los metodos para pasar argumentos personalizado segun la clase
 * a la que se desea asignar como modificador.
 * 
 */
public abstract class AbstractRecordPreview extends JPanel implements DockingComponent {
	private JPanel secondConainer;
	protected Record record;
	private Box box;
	
	/** nueva instacia
	 * 
	 * @param rcd - registro de base de datos
	 */
	public AbstractRecordPreview() {
		super(new BorderLayout());
		this.secondConainer = new JPanel(new BorderLayout());
		this.box = null;
		secondConainer.setBackground(Color.WHITE);
		setBackground(Color.WHITE);
		add(secondConainer, BorderLayout.NORTH);
		TUIUtils.setEmptyBorder(this);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see client.core.DockingComponent#getAction()
	 */
	public Action getAction() {
		return null;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see client.core.DockingComponent#getIcon()
	 */
	public String getIconName() {
		return "AbstractRecordPreview";
	}
		
	/*
	 *  (non-Javadoc)
	 * @see client.core.DockingComponent#getTitle()
	 */
	public String getTitleID() {
		return "r01";
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		setRecord((Record) evt.getNewValue());
	}
	
	/** establece el registro cuyos datos se presentaran en este panel. 
	 * 
	 * @param rcd
	 */
	public void setRecord(Record rcd) {
		this.record = rcd;
		configurePanel();
	}
	
	/** Sub clases implementan este metodo para indicar si el campo fn debe ser presentado
	 * o no 
	 * 
	 * @param fn - nombre del campo
	 * @return true = mostrar
	 */
	public abstract boolean isFieldVisible(String fn);
	
	/** configura panel con el contenido del registro	
	 *
	 */
	private void configurePanel(){
		setVisible(false);
		if (box != null) {
			secondConainer.remove(box);
		}
		this.box = Box.createHorizontalBox();
		
		// seleccion nula
		if (record != null) {
			JPanel fnam = new JPanel(new GridLayout(0, 1, 4, 4));
			fnam.setOpaque(false);
			JPanel fval = new JPanel(new GridLayout(0, 1, 4, 4));
			fval.setOpaque(false);
			
			for (int f = 0; f < record.getFieldCount(); f++) {
				String fn = record.getFieldName(f);
				if (!isFieldVisible(fn)) {
					continue;
				}
				JLabel jl = TUIUtils.getJLabel(fn, false, true);
				jl.setFont(jl.getFont().deriveFont(Font.BOLD));
//				jl.setHorizontalAlignment(JLabel.RIGHT);
				fnam.add(jl);
				ExtendedJLabel ejl = 
					TUIUtils.getExtendedJLabel(record, record.getFieldName(f), false);
				ejl.setHorizontalAlignment(JLabel.LEFT);
				fval.add(ejl);
			}
			box.add(fnam);
			box.add(Box.createHorizontalStrut(4));
			box.add(fval);			
	//		box.add(Box.createVerticalGlue());
		}
		secondConainer.add(box, BorderLayout.WEST);
		setVisible(true);
	}	
}
