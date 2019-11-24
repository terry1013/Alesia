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
 * Copyright (c) 2003 Arnaldo Fuentes. Todos los derechos reservados.
 */

package gui;

import gui.html.*;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import action.*;
import core.*;

/** Un <code>WizardContainer</code> contiene y controla una lista de objetos instancia de 
 * <code>WizardPanel1</code> en forma secuencial para asi presentar una secuencia de paneles 
 * de entrada al estilo "asistente para..." esta clase controla la secuencia de presentacion y 
 * las posibles relaciones entre los paneles comunicandose con cada uno de los objetos
 * dentro de la lista a presentar. el funcionamiento es:
 * 
 * * se crea una instancia de este componente
 * * se adicionan los paneles que conformaran el asistente usando el metodo 
 * <code>addWizardPanel</code>
 * * se invoca <code>next()</code> par iniciar la secuencia
 * 
 * si alguno de los pasos dentro del asistente es una instancia de <code>AbstractDataInput</code>
 * el contenedor tendra especial cuidado en tratar este paso como si fuese un dialogo de entrada
 * 
 * durante la presentancion del asistente, cualquiera de los paneles puede modificar la
 * secuencia de la presentacion a voluntad. para ello, puede usar los metodos 
 * insertWizardPaneAt removeWizardPanelAt. es conveniente que estos metodos sean invocados durante
 * la validacion de los campos dentro del panel. (<code>validateWizardPanel</code>)
 * 
 */
public class WizardContainer extends UIComponentPanel implements Navigator {

	public static int PREVIOUS = -1;
	public static int WELLCOME = 0;
	public static int GOODBYE = 1;

	private Vector v_visualC, v_panels, v_dname;
	private Hashtable h_panels;
	private int p_counter;
	private JPanel container;
	private TAbstractAction prevAct, cancelAct;
	private NextAction nextAct;
	private FinalizeAction finalizeAct;
	private JButton jb_default;
//	private Box innerC;

	/** nueva instancia.
	 * 
	 * @param ca - instancia de <code>ConfirmationAction</code> 
	 */
	public WizardContainer() {
		super(null, true);
		this.container = new JPanel(new BorderLayout());
		this.p_counter = -1;
		this.prevAct = new PreviousAction(this);
		this.nextAct = new NextAction(this);
		this.finalizeAct = new FinalizeAction(this);
		this.cancelAct = new CancelAction(this);
		this.v_visualC = new Vector();
		this.v_panels = new Vector();		
		this.v_dname = new Vector();
		this.h_panels = new Hashtable();
		addWithoutBorder(container);
		setActionBar(new AbstractAction[] {prevAct, nextAct, finalizeAct, cancelAct});
	}
	
	/** inserta un panel dentro de la secuencia de paneles. este metodo permite agregar paneles
	 * durante la ejecucion del asistente. ver descripcion de clase para mas info.
	 * 
	 * @param j - instancia de <code>WizardPanel1</code>
	 * @param dn - nombre del documento que sera presentado en la barra de informacion
	 * @param pan - nombre que identifica al panel dentro de la lista. 
	 * @param pos - posicion dentro de la lista. los paneles siguientes seran movidas 
	 * hacia adelante 1 posicion.
	 */
	public void insertWizardPanelAt(WizardPanel1 j, String dn, String pan, int pos) {
		insertWizardPanel(j, dn, pan, pos);
	}
	
	/** remueve un panel determinado dentro de la secuencia del asistente. el resto de los 
	 * paneles se movera una posision hacia la izquierda. Ver descripcion de clase 
	 * 
	 * @param pid - identificador de panel a eliminar.
	 */
	public void removeWizardPanel(String pid) {
		Object robj = h_panels.get(pid);
		if (robj != null) {
			h_panels.remove(pid);
			int idx = v_panels.indexOf(robj);
			v_panels.remove(idx);
			v_visualC.remove(idx);
			v_dname.remove(idx);
		}
	}
	

	/** adiciona elementos a la lista de paneles a presentar. Los elementos debes ser 
	 * añadidos en el orden que se desean presentar. el parametro <code>pan</code> es ingnorado
	 * se el panel adicionado dentro de la secuencia es un panel especial. (creado usando
	 * <code>getEspecialPanel(String, int)</code>). 
	 * 
	 * 
	 * @param j - instancia de <code>WizardPanel1</code>
	 * @param dn - nombre del documento que sera presentado en la barra de informacion
	 * @param pan - nombre que identifica al panel dentro de la lista. o null si es una 
	 * panel especial
	 * 
	 */
	public void addWizardPanel(WizardPanel1 j, String dn, String pan) {
		insertWizardPanel(j, dn, pan, 9999);
	}
	
	/** metodo que soporta metodos publicos add e insert. este metodo verifica si existe un panel
	 * identificado con el nombre <code>pan</code>. si existe, no se adiciona el panel pasado
	 * como argumento
	 * 
	 * 
	 * @param j - instancia de <code>WizardPanel1</code>
	 * @param dn - nombre del documento que sera presentado en la barra de informacion
	 * @param pan - nombre que identifica al panel dentro de la lista. o null si es una 
	 * panel especial
	 * @param pos - posicion dentro de la lista. "Insertar"
	 * 
	 */
	private void insertWizardPanel(WizardPanel1 j, String dn, String pan, int pos) {
		if (!(j instanceof EspecialWizardPanel)) {
			
			// solo añade si no existe ya uno con el mismo nombre
			if (h_panels.get(pan) != null) {
				return;
			}
			this.h_panels.put(pan, j);
		}

		JComponent jc = j.getComponent();
		if (pos > v_visualC.size()) {
			this.v_visualC.addElement(jc);
			this.v_panels.addElement(j);
			this.v_dname.addElement(dn);
		} else {
			this.v_visualC.insertElementAt(jc, pos);
			this.v_panels.insertElementAt(j, pos);
			this.v_dname.insertElementAt(dn, pos);
		}
	}

	/** evalua si hay mas paneles dentro de la lista.
	 * 
	 * @return <code>true</code>si los hay
	 */	
	private boolean hasNext() {
		return (p_counter + 1) < v_visualC.size();
	}
	
	/** retorna verdadero si actualmente se esta presentado el primer panel de entrada
	 * 
	 * @return <code>truo o false</code>
	 */
	private boolean isFirst() {
		return p_counter == 0;
	}
	
	public void home() {
		
	}
	/*
	 *  (non-Javadoc)
	 * @see com.ae.gui.wizard.Navigator#next()
	 */
	public void next() {
		boolean err = false;
		
		if (p_counter > -1) {
			WizardPanel1 wp = (WizardPanel1) v_panels.elementAt(p_counter);

			// si es una instancia de ltpanel, primero verifica posible error
			JComponent jc = (JComponent) v_visualC.elementAt(p_counter);
			if (jc instanceof UIComponentPanel) {
				UIComponentPanel lt = (UIComponentPanel) jc;
				err = lt.isShowingError();
			}
			
			// si no hay error, verfico panel. esto permite sicronia con ltpanel
			if (!err) {
				err = wp.validateWizardPanel(this);
			}
		}
		
		// si todo bien, deplazar
		if (!err) {
			update(p_counter, ++p_counter);
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see com.ae.gui.wizard.Navigator#previous()
	 */
	public void previous() {
		if (!isFirst()) {
			update(p_counter, --p_counter);
			prevAct.setEnabled(!isFirst());
		}
	}
	
	/** retorna instancia del panel que se encuentra en la posision solicitada. comenzando 
	 * desde 0.
	 * 
	 * @param pos - Posicion. o <code>WizardContainer.PREVIOUS</code> 
	 * @return panel
	public WizardPanel1 getPanel(int pos) {
		if (pos == PREVIOUS) {
			pos = p_counter - 1;
		}
		return (WizardPanel1) v_panels.elementAt(pos);
	}
	 */

	/** retorna el panel cuyo nombre que lo identifica es <code>pid</code> 
	 * 
	 * @param pid - nombre del panel
	 * @return panel
	 */
	public WizardPanel1 getPanel(String pid) {
		return (WizardPanel1) h_panels.get(pid);
	}
	
	/** hacia adelante o atras segun la combinacion de argumento. este metodo elimina el panel
	 * actual, inicializa panel a presentar, adiciona y presenta
	 * 
	 * @param ol - posicion de panel a remover
	 * @param ne - posicion de panel a presentar 
	 */
	private void update(int ol, int ne) {
		setVisible(false);
		if (ol > -1) {
//			container.remove(innerC);
			container.remove((JComponent) v_visualC.elementAt(ol));
		}
		WizardPanel1 wp = (WizardPanel1) v_panels.elementAt(ne);
		wp.initializePanel(this);
		JComponent jc = (JComponent) v_visualC.elementAt(ne);
		if (jc instanceof UIComponentPanel) {
			UIComponentPanel lt = (UIComponentPanel) jc;
//			lt.setDocPanel((String) v_dname.elementAt(ne));
		}
		boolean na = true;
		if (jc instanceof AbstractDataInput) {
			AbstractDataInput adi = (AbstractDataInput) jc;
			// posiblemente ya este establecido 
			// TODO: verificar
	//		adi.setDefaultButton(jb_default);
			na = adi.checkInputVerifier();
		}
		
//		innerC = UIUtilities.getInHorizontalBox(new JComponent[] {jc}, SwingConstants.CENTER);
//		container.add(innerC, BorderLayout.NORTH);
		container.add(jc, BorderLayout.NORTH);
		updateButtons(na);
		setVisible(true);
	}
	
	/** actualiza situacion de los botones de accion segun el estado de la secuencia
	 * 
	 * @param na - indica si el boton siguiente podria estar habilitado
	 */
	private void updateButtons(boolean na) {
		prevAct.setEnabled(!isFirst());
		jb_default.setEnabled(na && hasNext());
		finalizeAct.setEnabled(!hasNext());
	}	
	
	/** retorna panel con formato de panel inicial de bienvenida al asistente.
	 * 
	 * @param rid - mensaje
	 * @param ty - Tipo de panel. puede ser WELLCOME o GOODBYE
	 * @return WizardPanel1
	 */
	public WizardPanel1 getEspecialPanel(String rid, int ty) {
		return new EspecialWizardPanel(rid, ty);
	}
	
	public JButton getDefaultButton() {
		return jb_default;
	}
	
	/** implementacion de <code>WizardPanel1</code> para panel de bienvenida o panel final
	 * 
	 */
	private class EspecialWizardPanel implements WizardPanel1 {
		
		private String msg;
		private int type;
		
		/** nueva panel especial
		 * 
		 * @param rid - id para mensaje
		 * @param t - tipo de panel WELLCOME o GOODBYE
		 */
		public EspecialWizardPanel(String rid, int t) {
			this.msg = rid;
			this.type = t;
		}
		
		public JComponent getComponent() {
			JPanel jp = new JPanel(new BorderLayout());
			JLabel jl1 = new JLabel(TStringUtils.getString(msg));
			jl1.setVerticalAlignment(JLabel.TOP);
			TUIUtils.setEmptyBorder(jl1);
			// bienvenida
			if (type == WizardContainer.WELLCOME) {
				jp.setBackground(Color.WHITE);
				ImageIcon ii = TResources.getIcon("Import32");
				JLabel jl = new JLabel(ii);
				jl.setVerticalAlignment(JLabel.TOP);
				jp.add(jl, BorderLayout.WEST);
			}
			jp.add(jl1, BorderLayout.CENTER);
			return jp;
		}
		public Object getData() {
			return null;
		}
		public void initializePanel(WizardContainer arg0) {

		}
		/* (non-Javadoc)
		 * @see client.ui.wizard.WizardPanel1#validateWizardPanel()
		 */
		public boolean validateWizardPanel(WizardContainer newParam) {
			return false;
		}
	}	
}
