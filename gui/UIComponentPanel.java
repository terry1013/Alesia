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

import gui.html.*;
import gui.tree.*;
import gui.wlaf.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import action.*;

import com.alee.extended.layout.*;
import com.alee.extended.menu.*;
import com.alee.extended.panel.*;
import com.alee.extended.transition.*;
import com.alee.extended.transition.effects.fade.*;
import com.alee.laf.button.*;
import com.alee.laf.panel.*;

import core.*;

/**
 * Contenedor principal de la aplicacion. Este es usado para contener y presentar todos los componentes de interfaz de
 * usuario. UIComponentPanel contiene componentes y formas definidas como estandar para la aplicacion. - contiene un
 * area de encabezado que indica al usuario una breve descripcion de lo que se esta haciendo es este momento. -area
 * donde se pueden colocar componentes informativos. - linea de mensajes por la cual la aplicacion se comunica con el
 * usuario indicando errores y advertencias. - barra de herramientas - area para botones de accion en la parte inferior.
 * 
 * si el panes es usado para compoentes de entrada, puede ser confi
 * 
 * 
 */
public class UIComponentPanel extends ComponentTransition {
	private TError aplicationE;
	private Box noListPanel;

	private TWebDynamicMenu dynamicMenu;
	private JLabel msgLabel, blkinfoLabel;
	private javax.swing.Timer timer;
	private TimerAction timer_a;
	private JPanel componentPanel, informationPanel, headerPanel, messagePanel;
	private ComponentTransition transitionPanel;
	private JComponent internalPanel;
	private WaitPanel waitPanel;
	private TAbstractAction[] toolBarActs;
	private WebPanel toolBar;
	private JPopupMenu popupMenu;
	protected UIComponentPanel parentUiComponentPanel;
	private TAbstractAction doubleClicAction;

	/**
	 * crea un nuevo UIComponentPanel con docN como documento de encabezado
	 * 
	 * @param idc - id de constante que contiene el texto para encabezado. Si <code>null</code> no se presenta la barra
	 *        de titulo.
	 * @param eerr - indica si se desea o no el area para mostrar errores.
	 */
	public UIComponentPanel(String idc, boolean eerr) {
		super();
		setLayout(new BorderLayout());
		this.popupMenu = null;
		this.doubleClicAction = null;
		this.parentUiComponentPanel = null;
		this.toolBarActs = null;
		this.toolBar = null;
		this.aplicationE = null;
		this.timer_a = new TimerAction();
		this.timer = new javax.swing.Timer(125, timer_a);
		timer.setRepeats(true);

		toolBar = new WebPanel(true, new ToolbarLayout());
		toolBar.setPaintSides(false, false, false, false);
		toolBar.setShadeWidth(0);
		toolBar.setVisible(false);

		dynamicMenu = new TWebDynamicMenu();
		dynamicMenu.setType(DynamicMenuType.roll);
		dynamicMenu.setHideType(DynamicMenuType.roll);
		dynamicMenu.setRadius(70);
		dynamicMenu.setStepProgress(0.08f);

		// informationPanel is the component that display the information about this component usage.
		this.informationPanel = new JPanel(new BorderLayout());
		informationPanel.setBackground(TUIUtils.brighter(informationPanel.getBackground()));
		Dimension dim = new Dimension(0, 68);
		informationPanel.setPreferredSize(dim);
		JLabel jltd = new JLabel(idc == null ? "" : TStringUtils.getString(idc));
		jltd.setBorder(new EmptyBorder(4, 4, 4, 4));
		jltd.setVerticalAlignment(JLabel.NORTH);
		informationPanel.add(jltd, BorderLayout.CENTER);
		informationPanel.add(new JSeparator(), BorderLayout.SOUTH);
		informationPanel.setVisible(idc != null);

		// messagePanel is where the error are displayed.
		this.msgLabel = new JLabel(" ");
		Dimension dim1 = new Dimension(0, 18);
		msgLabel.setPreferredSize(dim1);
		this.messagePanel = new JPanel(new BorderLayout());
		messagePanel.setBackground(Color.white);
		messagePanel.add(msgLabel, BorderLayout.NORTH);
		messagePanel.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.CENTER);
		msgLabel.setBorder(new EmptyBorder(1, 2, 1, 2));
		setVisibleMessagePanel(eerr);

		// headerPanel is compound by information panel + messagePanel + toolbar (if are present)
		// ---------------------------------------------|
		// |informationPanel
		// |--------------------------------------------|
		// |messagePanel
		// |--------------------------------------------|
		// |ToolBar (if present)

		this.headerPanel = new JPanel(new BorderLayout());
		headerPanel.add(informationPanel, BorderLayout.NORTH);
		Box b1 = Box.createVerticalBox();
		b1.add(messagePanel);
		headerPanel.add(b1, BorderLayout.CENTER);
		headerPanel.add(toolBar, BorderLayout.SOUTH);

		// noListPanel are used to display a message when instances of this component show a list of elements and
		// such list has no elements to display.
		this.noListPanel = Box.createVerticalBox();
		this.blkinfoLabel = new JLabel();
		blkinfoLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
		noListPanel.add(Box.createVerticalStrut(8));
		noListPanel.add(blkinfoLabel);
		noListPanel.add(Box.createVerticalGlue());

		transitionPanel = new ComponentTransition();
		transitionPanel.setContent(noListPanel);
		// center and south component add after
		// transitionPanel.setContent(internalPanel);
		FadeTransitionEffect effect = new FadeTransitionEffect();
		transitionPanel.setTransitionEffect(effect);

		// componentPanel is where all componet layout.
		// ---------------------------------------------|
		// |titlePanel
		// |--------------------------------------------|
		// |messagePanel
		// |--------------------------------------------|
		// |ToolBar (if present)
		// |center component added by subclases
		// |--------------------------------------------|
		// |actionBar (add by subclass)
		// |--------------------------------------------|
		this.componentPanel = new ComponentTransition();
		componentPanel.setLayout(new BorderLayout());
		componentPanel.add(headerPanel, BorderLayout.NORTH);
		componentPanel.add(transitionPanel, BorderLayout.CENTER);

		waitPanel = new WaitPanel();
		setContent(waitPanel);
		setContent(componentPanel);
//		FadeTransitionEffect effect1 = new FadeTransitionEffect();
//		setTransitionEffect(effect1);
	}

	@Override
	public Dimension getPreferredSize() {
		return componentPanel.getPreferredSize();
	}

	/**
	 * return the wait component. The Wait component is used to cover all object inside this component during long
	 * component initialization. Use {@link #performTransition(Component)} to set the visivility for this component
	 * 
	 * @return - standar wait component
	 */
	public WaitPanel getWaitPanel() {
		return waitPanel;
	}

	public WebPanel getToolBar() {
		return toolBar;
	}
	public JComponent getComponentPanel() {
		return componentPanel;
	}

	/**
	 * set the parent for this component. when a parent is setted, this component detour some functions to parent
	 * component suchs as execption mesaje display
	 * 
	 * @param par - parent component
	 */
	public void setParent(UIComponentPanel par) {
		this.parentUiComponentPanel = par;
	}

	/**
	 * remplace the user input components or list with a black panel with {@link TError} text and icon. if
	 * a subclass use toolbar, the toolbar remain visible.
	 * 
	 * @param ri - AplicationException id in .property file
	 * @param dta - dta for AplicationException sustitution data
	 * 
	 * @see UIComponentPanel#getToolBar()
	 */
	public void setMessage(String ri, Object... dta) {
		// setVisible(false);
		if (ri == null) {
			if (internalPanel != null) {
				// componentPanel.remove(noListPanel);
				// componentPanel.add(internalPanel, BorderLayout.CENTER);
				toolBar.setVisible(true);
				transitionPanel.performTransition(internalPanel);
			}
		} else {
			noListPanel.setVisible(false);
			// 17.04.05: ahora el panel se presenta en el area que contiene la forma o lista de elementos
			// dejando la barra de herramientas visible
			if (internalPanel != null) {
				// componentPanel.remove(internalPanel);
			}
			aplicationE = new TError(ri, dta);
			blkinfoLabel.setIcon(aplicationE.getExceptionIcon());
			String fmsg = aplicationE.getMessage();
			blkinfoLabel.setText(fmsg);

			// 171127: Try to detect multiline msg looking for any <br>, <p> etc. tag. if that tag are present,
			// aling icon to top
			boolean mlm = false;
			mlm = TStringUtils.getString(fmsg).contains("<br>") ? true : false;
			mlm = TStringUtils.getString(fmsg).contains("<ol>") ? true : mlm;
			mlm = TStringUtils.getString(fmsg).contains("<p>") ? true : mlm;
			if (mlm) {
				blkinfoLabel.setVerticalTextPosition(JLabel.TOP);
			}
			// componentPanel.add(noListPanel, BorderLayout.CENTER);
			noListPanel.setVisible(true);
			toolBar.setVisible(false);
			transitionPanel.performTransition(noListPanel);
		}
		// setVisible(true);
	}

	/**
	 * Adicion el compoenente. Antes de adicionar, Coloca un borde con formata preestablecido.
	 * 
	 * @param comp - Compoenente a adicionar
	 */
	public void add(JComponent comp) {
		TUIUtils.setEmptyBorder(comp);
		addWithoutBorder(comp);
	}

	/**
	 * igual que <code>add(JComponent)</code> pero sin borde
	 * 
	 * @param comp - componente
	 */
	public void addWithoutBorder(JComponent comp) {
		this.internalPanel = comp;
		// componentPanel.add(comp, BorderLayout.CENTER);
		transitionPanel.setContent(internalPanel);
	}

	/**
	 * retorna la instancia de <code>AplicationException</code>que actualmente se esta presentando.
	 * 
	 * @return instancia de <code>AplicationException o null</code> si no hay mensajes actuales
	 */
	public TError getException() {
		return aplicationE;
	}

	/**
	 * retorna arreglo con los botones que componen la barra de herraminetas. puede retornar <code>null</code> si no
	 * existen botones dentro en la barra
	 * 
	 * 
	 * @return botones dentro de la barra de herramientas
	 */
	public TAbstractAction[] getToolBarActions() {
		return toolBarActs;
	}

	/**
	 * Retorna true si este se esta mostrando un mensaje de error. Un mesaje de error todos menos instancias de
	 * <code>InformationException</code>
	 * 
	 * @return true si se esta presentando un error
	 */
	public boolean isShowingError() {
		return !(aplicationE == null || aplicationE.getExceptionType().equals(TError.INFORMATION));
	}

	/**
	 * crea y posiciona una barra de botones estandar para dialogos de entrada. esta coloca los botones en la parte
	 * inferior alienados hacia la derecha. en el orden con que son pasadas las acciones. adicionalmente: - si alguno de
	 * las acciones es instancia de <code>DefaultAceptAction</code> se establece este como propiedad
	 * <code>PropertyNames.DEFAULT_BUTTON</code> - si alguno de las acciones es instancia de
	 * <code>DefaultCancelAction</code> se establece este como <code>PropertyNames.DEFAULT_CANCEL_ACTION</code>
	 * 
	 * @param btns - lista acciones a añadir
	 */
	public void setActionBar(AbstractAction... btns) {
		setActionBar(true, btns);
	}

	public JComponent[] setActionBar(boolean add, AbstractAction... btns) {
		JComponent[] rtnb = new JComponent[btns.length];
		Box b = Box.createHorizontalBox();
		b.add(Box.createHorizontalGlue());
		for (int j = 0; j < btns.length; j++) {
			b.add(Box.createHorizontalStrut(TUIUtils.H_GAP));
			JButton jb = new JButton(btns[j]);
			rtnb[j] = jb;
			if (btns[j] instanceof NextAction) {
				jb.setHorizontalTextPosition(JLabel.LEFT);
			}

			// boton por omision para aceptar
			if (btns[j] instanceof DefaultAceptAction) {
				putClientProperty(TConstants.DEFAULT_BUTTON, jb);
			}

			// accion por omision para cancelar
			if (btns[j] instanceof DefaultCancelAction) {
				putClientProperty(TConstants.DEFAULT_CANCEL_BUTTON, jb);
			}
			b.add(jb);
		}
		if (add) {
			componentPanel.add(TUIUtils.getBoxForButtons(b, true), BorderLayout.SOUTH);
		}
		return rtnb;
	}

	public void setActionBar(JComponent[] cmps) {
		Box b = Box.createHorizontalBox();
		b.add(Box.createHorizontalGlue());
		for (int j = 0; j < cmps.length; j++) {
			b.add(Box.createHorizontalStrut(TUIUtils.H_GAP));
			b.add(cmps[j]);
		}
		componentPanel.add(TUIUtils.getBoxForButtons(b, true), BorderLayout.SOUTH);
	}

	/**
	 * establece si el componente de mensajes sera visible o no. este componente es el que presenta los mensajes
	 * informativos y de error de los dialogos de entrada
	 * 
	 * @param v - true si desea presentar los mensajes de error
	 */
	public void setVisibleMessagePanel(boolean v) {
		messagePanel.setVisible(v);
	}

	public void addToolBarAction(TAbstractAction... taas) {
		int i = toolBarActs.length;
		toolBarActs = Arrays.copyOf(toolBarActs, toolBarActs.length + taas.length);
		WebButtonGroup g = TUIUtils.getButtonGroup();
		for (TAbstractAction taa : taas) {
			WebButton jb = TUIUtils.getWebButtonForToolBar(taa);
			toolBarActs[i++] = taa;
			g.add(jb);
		}
		toolBar.add(g);
		// toolBar.add(g, ToolbarLayout.MIDDLE);
		// toolBar.add(ffGroup, ToolbarLayout.END);

	}

	/**
	 * Set tha actions <code>taas</code> in this component tool bar area plus standar actions
	 * 
	 * @param taas - array of actions to set.
	 */
	public void setToolBar(TAbstractAction... taas) {
		setToolBar(true, taas);
	}

	/**
	 * Set tha actions <code>taas</code> in this component tool bar area.
	 * 
	 * TODO: add listview and tableview
	 * 
	 * @param sea - <code>true</code> to add standar actions {@link FilterAction}, {@link RefreshAction}
	 * @param taas - array of actions to set.
	 */
	public void setToolBar(boolean sea, TAbstractAction... taas) {
		toolBarActs = new TAbstractAction[taas.length + 2];
		WebButtonGroup mainGroup = TUIUtils.getButtonGroup();
		popupMenu = new JPopupMenu();
		int i = 0;
		for (TAbstractAction taa : taas) {
			WebButton jb = TUIUtils.getWebButtonForToolBar(taa);
			toolBarActs[i++] = taa;
			mainGroup.add(jb);
			// dynamicMenu.addItem(ta);
			// toolBar.add(cmps[i]);

			// 171221: commented changed due planc security implementation. override of isEnabled() method in
			// tabstractaction is better aproach !!!
			// ta.setEnabled(ta.isAvailable(this));
			if (taa.getScope() == TAbstractAction.RECORD_SCOPE) {
				JMenuItem jmi = new JMenuItem(taa);
				jmi.setIcon(null);
				// instacia de dobleclic. pero solo para la primera que se encuentre
				if (taa instanceof DefaultDobleClicAction && doubleClicAction == null) {
					this.doubleClicAction = taa;
					jmi.setFont(jmi.getFont().deriveFont(Font.BOLD));
				}
				popupMenu.add(jmi);
			}
		}

		// 171231: append some standar actions to popup when this instance of uilistpanel
		WebButtonGroup ffGroup = TUIUtils.getButtonGroup();
		if (popupMenu != null && (this instanceof UIListPanel || this instanceof TAbstractTree)) {
			TAbstractAction[] taass = new TAbstractAction[]{new RefreshAction(this), new FilterAction(this)};
			for (TAbstractAction taa : taass) {
				getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
						(KeyStroke) taa.getValue(TAbstractAction.ACCELERATOR_KEY), taa.getClass().getName());
				getActionMap().put(taa.getClass().getName(), taa);
				// popupMenu.add(taa);
				ffGroup.add(TUIUtils.getWebButtonForToolBar(taa));
				toolBarActs[i++] = taa;
			}
		}
		toolBar.add(mainGroup);
		if (sea) {
			toolBar.add(ffGroup, ToolbarLayout.END);
		}

		toolBar.setVisible(true);

		mainGroup.setButtonsDrawFocus(false);
		mainGroup.setButtonsRolloverDarkBorderOnly(true);
	}
	/**
	 * presenta el texto de la excepcion den el area de mensajes. si el argumento de entrada es <code>null</code> se
	 * limpia esta area. este metodo redirecciona el mensaje a mostrar si se estabecio una instancia de
	 * <code>UIComponentParent</code> padre para esta instancia. si es asi, (suando el metodo
	 * <code>setParent(UIComponentPanel)(</code> se invocara este mismo metodo a la instancia superior que contiene este
	 * componente
	 * 
	 * @param appe - instancia de la exepcion
	 */
	public void showAplicationException(TError appe) {
		this.aplicationE = appe;
		// redireccion a contenedor superior
		if (parentUiComponentPanel != null) {
			parentUiComponentPanel.showAplicationException(appe);
			return;
		}
		if (aplicationE != null) {
			ImageIcon ii = new ImageIcon(aplicationE.getExceptionIcon().getImage()
					.getScaledInstance(14, 14, Image.SCALE_DEFAULT));
			msgLabel.setIcon(ii);
			msgLabel.setText(aplicationE.getMessage());
			// insertedbr for long messages
			msgLabel.setToolTipText(TStringUtils.getInsertedBR(aplicationE.getMessage(), 100));
			msgLabel.setOpaque(true);
			msgLabel.setBackground(aplicationE.getErrorColor());
			timer_a.initialize(aplicationE);
			timer.start();
		} else {
			timer.stop();
			msgLabel.setOpaque(false);
			msgLabel.setIcon(null);
			msgLabel.setText(" ");
		}
	}

	/*
	 * public void showAplicationException(AplicationException appe, boolean edb) { showAplicationException(appe); if
	 * (appe.getExceptionType()) }
	 */

	/**
	 * Localiza la exepcion identificada con el argumento de entrada y ejecuta
	 * <code>showExceptionMessage(AplicationException)</code>
	 * 
	 * @param msgid - identificador de mensaje.
	 */
	public void showAplicationExceptionMsg(String msgid) {
		TError ae = null;
		if (msgid != null) {
			ae = new TError(msgid);
		}
		showAplicationException(ae);
	}

	/**
	 * establece el area de titulo si es visible o no. este metodo no verifica si el id de mensaje a presentar es null
	 * al momento de co configuracion
	 * 
	 * @param v - visible o no
	 */
	public void setVisibleInformationPanel(boolean v) {
		informationPanel.setVisible(v);
	}

	/**
	 * solo para procesar temporizador. esta clase controla el tiempo de presentacion de la exepcion. Si el tiempo ya ha
	 * sido alcanzado se ejecuta <code>UIComponentPanel.showMessage(null)</code>
	 * 
	 */
	public class TimerAction implements ActionListener, Serializable {
		private int clr_c;

		private Color[] colors;
		private long mark;
		private int offset;

		public TimerAction() {
			this.clr_c = -1;
			this.offset = -1;
			this.mark = 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent ae) {
			if (mark + offset > ae.getWhen()) {
				clr_c = (clr_c == colors.length - 1) ? 0 : ++clr_c;
				msgLabel.setBackground(colors[clr_c]);
				msgLabel.repaint();
			} else {
				showAplicationExceptionMsg(null);
			}
		}

		/**
		 * inicializa valores para iniciar entrega de colores.
		 * 
		 * @param ae
		 */
		public void initialize(TError ae) {
			Color bas = ae.getErrorColor();
			colors = new Color[6];
			colors[0] = bas;
			colors[1] = TUIUtils.brighter(bas);
			colors[2] = TUIUtils.brighter(colors[1]);
			colors[3] = TUIUtils.brighter(colors[2]);
			colors[4] = TUIUtils.brighter(colors[1]);
			colors[5] = TUIUtils.brighter(bas);
			this.clr_c = -1;
			this.mark = new Date().getTime();
			this.offset = ae.getMiliSeconds();
		}
	}
	/**
	 * clase que presenta la instancia de <code>JPopupMenu</code> creada para la table que presenta los datos dentro de
	 * esta clase
	 * 
	 */
	public class ListMouseProcessor extends MouseAdapter {

		private JComponent invoker;

		public ListMouseProcessor(JComponent in) {
			this.invoker = in;
		}

		/**
		 * presetna menu
		 * 
		 * @param e - evento
		 */
		private void showPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				// verifica null porque x autorizciones, pueden no haber elementos
				if (popupMenu != null) {
					popupMenu.show(invoker, e.getX(), e.getY());
					// dynamicMenu.showMenu(invoker, e.getX(), e.getY());
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			showPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			showPopup(e);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				if (doubleClicAction != null && doubleClicAction.isEnabled()) {
					doubleClicAction.actionPerformed(null);
				}
			}
		}
	}
}
