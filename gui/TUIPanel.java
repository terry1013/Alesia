package gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.Action;
import javax.swing.border.*;
import javax.swing.event.*;

import org.jdesktop.application.*;

import com.alee.extended.layout.*;
import com.alee.extended.panel.*;
import com.alee.laf.button.*;
import com.alee.laf.label.*;
import com.alee.laf.panel.*;
import com.alee.laf.window.*;
import com.alee.managers.style.*;
import com.jgoodies.common.base.*;
import com.jgoodies.forms.layout.*;

import core.*;

/**
 * base class for application ui manage. this class is divided in tritle component, body component and footer component.
 * the base implementation create a title component that consist in a title label, a 3 dot button and a aditional
 * information component. the behabior of the 3dot buttons can bi setted via {@link #set3DotBehavior(int)} method. the
 * title of this component (title component and 3dot button) can be set visible/invisible leaving the aditional
 * information alone. Aditional information can be set visible or not.
 * 
 * @author terry
 *
 */
public class TUIPanel extends WebPanel {

	public static double ASPECT_RATION_NONE = 0.0;
	public static double ASPECT_RATION_NARROW = 1.3333;
	public static double ASPECT_RATION_DEFAULT = 1.6666;
	public static double ASPECT_RATION_WIDE = 1.7777;
	protected Vector<Action> allActions;
	private JComponent bodyJComponent, footerJComponent;
	private WebLabel titleLabel;
	private ActionMap actionMap;
	private WebButton treeDotButton;
	private Box bodyMessageJComponent;
	private JLabel blkinfoLabel;

	private JEditorPane additionalInfo;

	private JPanel titlePanel;
	private WebDialog dialog;
	double aspectRatio = ASPECT_RATION_DEFAULT;

	private JPopupMenu popupMenu;
	private Action doubleClickAction;
	private WebPanel toolBarPanel;

	public WebPanel getToolBarPanel() {
		return toolBarPanel;
	}

	/**
	 * Enable/Disable all the actions present in this component acordint to parametars pass as arguments.
	 * <p>
	 * For example. the class {@link TUIFormPanel} has the <code>Acept </code> action. this action has a paremeter
	 * <code>acept.Action.isCommint = true</code> that mark this action as an action for commit changes to the sistem.
	 * <p>
	 * call this metodo <code>enableInternalActions("isCommint", "true", false)</code> means that all actions whit
	 * property <code>.isCommit = true</code> will be disabled
	 * 
	 * @param property - indicate the property of the action to look for
	 * @param value - the value of the param property must be equal tho this value
	 * @param enable - boolean value to enable or disable de action.
	 */
	protected void setEnableActions(String property, String value, boolean enable) {
		for (Action a : allActions) {
			ApplicationAction aa = (ApplicationAction) a;
			String isc = aa.getResourceMap().getString(aa.getName() + ".Action." + property);
			if (isc != null && isc.equals(value))
				aa.setEnabled(enable);
		}
	}

	public TUIPanel(boolean showAI) {
		this();
		showAditionalInformation(showAI);
	}
	public TUIPanel() {
		super(new BorderLayout());
		this.allActions = new Vector<>();
		this.titleLabel = new WebLabel(" ");
		titleLabel.setFont(Alesia.title1);
		actionMap = Alesia.getInstance().getContext().getActionMap((TUIPanel) this);
		this.treeDotButton = getTreeDotButton();

		// temporal
		this.toolBarPanel = new WebPanel(StyleId.panelTransparent);
		toolBarPanel.setLayout(new LineLayout(LineLayout.HORIZONTAL, 0));

		// tilte label + 3dot button
		this.titlePanel = new WebPanel(StyleId.panelTransparent);
		titlePanel.setLayout(new BorderLayout());
		// titlePanel.add(titleLabel, BorderLayout.CENTER);
		titlePanel.add(toolBarPanel, BorderLayout.CENTER);
		// titlePanel.add(treeDotButton, BorderLayout.EAST);

		this.additionalInfo = createReadOnlyEditorPane(null, null);
		additionalInfo.setPreferredSize(new Dimension(0, 48));

		// noListPanel are used to display a message when instances of this component show a list of elements and
		// such list has no elements to display.
		this.bodyMessageJComponent = Box.createVerticalBox();
		this.blkinfoLabel = new JLabel();
		blkinfoLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
		bodyMessageJComponent.add(Box.createVerticalStrut(8));
		bodyMessageJComponent.add(blkinfoLabel);
		bodyMessageJComponent.add(Box.createVerticalGlue());

		WebPanel north = new WebPanel(StyleId.panelTransparent);
		north.setLayout(new BorderLayout());
		north.add(titlePanel, BorderLayout.NORTH);
		north.add(additionalInfo, BorderLayout.CENTER);

		add(north, BorderLayout.NORTH);
	}

	public final WebDialog createDialog(boolean setAspectRatio) {
		// Preconditions.checkState(EventQueue.isDispatchThread(), "You must create and show dialogs from the
		// Event-Dispatch-Thread (EDT).");
		// checkWindowTitle(title);
		if (dialog != null) {
			// dialog.setTitle(" ");
			return dialog;
		}
		dialog = new WebDialog(StyleId.dialogDecorated, Alesia.mainFrame);
		// standar behavior: if the title of the tuipanel is visible, this method remove the string and put in as this
		// dialog title
		if (isTitleVisible()) {
			dialog.setTitle(getTitleText());
			setTitleVisible(false);
		}

		dialog.setModal(true);
		dialog.setResizable(false);
		dialog.setContentPane(this);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		computeAndSetInitialDialogSize();
		if (setAspectRatio)
			setDialogAspectRatio();
		else
			dialog.pack();
		dialog.setLocationRelativeTo(Alesia.mainFrame);
		return dialog;
	}
	/**
	 * return and {@link JEditorPane} for information read only.
	 * 
	 * @param textId - text id. may be <code>null</code>
	 * @param hyperlinkListener - may be <code>null</code>
	 * 
	 * @return eidtor pane for read only
	 */
	public JEditorPane createReadOnlyEditorPane(String textId, HyperlinkListener hyperlinkListener) {
		String txt = textId == null ? null : TStringUtils.getString(textId);
		JEditorPane editorPane = new JEditorPane("text/html", txt);
		editorPane.setEditable(false);
		editorPane.setOpaque(false);
		editorPane.setFocusable(false);
		HTMLUtils.addDefaultStyleSheetRule(editorPane);
		if (hyperlinkListener != null) {
			editorPane.addHyperlinkListener(hyperlinkListener);
		}
		return editorPane;
	}

	@org.jdesktop.application.Action
	public void filterList(ActionEvent event) {

	}

	public double getAspectRatio() {
		return aspectRatio;
	}

	public String getTitleText() {
		return titleLabel.getText();
	}

	public boolean isTitleVisible() {
		return titleLabel.isVisible();
	}

	@org.jdesktop.application.Action
	public void refreshList(ActionEvent event) {

	}

	public void set3DotBehavior(int behavior) {

	}

	public void showAditionalInformation(boolean aFlag) {
		this.additionalInfo.setVisible(aFlag);
	}

	public final void setAspectRatio(double customValue) {
		Preconditions.checkArgument((customValue >= 0.0D),
				"The aspect ratio must positive, or ASPECT_RATION_NONE to disable the feature.");
		this.aspectRatio = customValue;
	}
	public void setBodyComponent(JComponent body) {
		if (bodyJComponent != null) {
			remove(bodyJComponent);
		}
		this.bodyJComponent = body;
		add(body, BorderLayout.CENTER);
	}

	/**
	 * replace the {@link JComponent} set using the metod {@link #setBodyComponent(JComponent)} and present a new
	 * componet to display the selected mensaje. If the msgId parameter is <code>null</code>, hide the message componet
	 * and present the body component
	 * 
	 * @param msgId - message id for text
	 * @param visibleTB - indicati if the toolbarpanel will be visible or not visible
	 * @param msgData - Sustitution data
	 * 
	 * @see UIComponentPanel#getToolBar()
	 */
	public void setMessage(String msgId, boolean visibleTB, Object... msgData) {
		if (msgId == null) {
			toolBarPanel.setVisible(true);
			remove(bodyMessageJComponent);
			add(bodyJComponent, BorderLayout.CENTER);
		} else {
			TError te = new TError(msgId, msgData);
			blkinfoLabel.setIcon(te.getExceptionIcon());
			blkinfoLabel.setText(te.getMessage());
			// blkinfoLabel.setVerticalTextPosition(JLabel.TOP);

			toolBarPanel.setVisible(visibleTB);
			remove(bodyJComponent);
			add(bodyMessageJComponent, BorderLayout.CENTER);
		}
		repaint();
	}

	/**
	 * Same as {@link #setMessage(String, boolean, Object...)} but set the toolbar no visible
	 * 
	 * @param msgId - message id for text
	 * @param msgData - Sustitution data
	 */
	public void setMessage(String msgId, Object... msgData) {
		setMessage(msgId, false, msgData);
	}

	public void setDescription(String tId) {
		additionalInfo.setText(Alesia.getResourceMap().getString(tId));
	}

	/**
	 * set an standar footer area for components intendet to input data.
	 * <p>
	 * NOTE: the actions bust be located in {@link TActionsFactory} class
	 * 
	 * @param actions list of actions
	 */
	public void setFooterActions(String... actions) {
		List<Action> alist = TActionsFactory.getActions(actions);
		Vector<JComponent> lst = new Vector<>();
		lst.add(new JLabel());
		for (Action act : alist) {
			allActions.add(act);
			TUIUtils.overRideIcons(16, null, act);
			WebButton wb = new WebButton(act);
			// ApplicationAction aa = (ApplicationAction) act;
			// String sco = aa.getResourceMap().getString(aa.getName() + ".Action.scope");
			lst.add(wb);
		}

		GroupPanel groupPane = new GroupPanel(GroupingType.fillFirst, true,
				(JComponent[]) lst.toArray(new JComponent[lst.size()]));

		// GroupPanel groupPane = new GroupPane(StyleId.grouppane, (WebButton[]) lst.toArray(new
		// WebButton[lst.size()]));
		// groupPane.setOrientation(SwingConstants.LEADING);
		// SwingUtils.equalizeComponentsWidth(groupPane.getComponents());

		setFooterComponent(groupPane);
	}
	public void setFooterComponent(JComponent footer) {
		if (footerJComponent != null) {
			remove(footerJComponent);
		}
		this.footerJComponent = footer;
		add(footer, BorderLayout.SOUTH);
	}

	public void setTitle(String txtId) {
		titleLabel.setText(TStringUtils.getString(txtId));
	}
	public void setTitleComponent(JComponent title) {
		add(title, BorderLayout.NORTH);
	}
	public void setTitleVisible(boolean aFlag) {
		this.titlePanel.setVisible(aFlag);
	}

	public void setToolBar(Action... actions) {
		setToolBar(Arrays.asList(actions));
	}

	public void addToolBarAction(Action action) {
		allActions.add(action);
		WebButton wb = TUIUtils.getWebButtonForToolBar(action);
		ApplicationAction aa = (ApplicationAction) action;
		String sco = aa.getResourceMap().getString(aa.getName() + ".Action.scope");

		// auto add the property TActionsFactory.TUILISTPANEL
		if (sco != null && (sco.equals("element") || sco.equals("list"))) {
			aa.putValue(TActionsFactory.TUILISTPANEL, this);
		}

		// action for popup menu
		if (sco != null && sco.equals("element")) {
			JMenuItem jmi = new JMenuItem(action);
			jmi.setIcon(null);
			// temp: doble click for editModel
			if (aa.getName().equals("editModel")) {
				this.doubleClickAction = action;
				jmi.setFont(jmi.getFont().deriveFont(Font.BOLD));
			}
			popupMenu.add(jmi);
		}
		toolBarPanel.add(wb);

	}

	/**
	 * set the toolbar for this component. This toolbar will replace the title label of this component. Use thid method
	 * when you need a full toolbar available for component that requirer many actions (like editors). other whise, use
	 * the 3dot bar.
	 * 
	 * @param actions actions to set inside the bar.
	 */
	public void setToolBar(List<Action> actions) {
		toolBarPanel.removeAll();
		popupMenu = new JPopupMenu();
		// ArrayList<JComponent> componets = new ArrayList<>();
		for (Action act : actions) {
			addToolBarAction(act);
		}

		// 171231: append some standar actions for list sublcases
		// toolBarPanel.add(TUIUtils.getWebButtonForToolBar(actionMap.get("filterList")), LineLayout.END);
		// toolBarPanel.add(TUIUtils.getWebButtonForToolBar(actionMap.get("refreshList")), LineLayout.END);
	}

	@org.jdesktop.application.Action
	public void treeDot(ActionEvent event) {

	}

	/**
	 * TODO: this buton bust be mutable acordin if there are mor element inside the toolbar or not
	 * 
	 * @return
	 */
	private WebButton getTreeDotButton() {
		WebButton tdb = new WebButton(StyleId.buttonHover, actionMap.get("treeDot"));
		return tdb;
	}

	private void invalidateComponentTree(Component c) {
		invalidate();
		// if (c instanceof Container) {
		// Container container = (Container) c;
		// for (Component child : container.getComponents())
		// invalidateComponentTree(child);
		// container.invalidate();
		// }
	}
	protected void computeAndSetInitialDialogSize() {
		if (getPreferredSize().width <= 0) {
			dialog.pack();
			return;
		}
		// dialog.addNotify();
		int targetWidth = Sizes.dialogUnitXAsPixel(getPreferredSize().width, dialog);
		dialog.setSize(targetWidth, 2147483647);
		dialog.validate();
		invalidateComponentTree(this);
		Dimension dialogPrefSize = dialog.getPreferredSize();
		int targetHeight = dialogPrefSize.height;
		dialog.setSize(targetWidth, targetHeight);
	}

	protected void setDialogAspectRatio() {
		int targetHeight;
		Dimension size;
		if (getAspectRatio() == ASPECT_RATION_NONE)
			return;
		do {
			size = dialog.getSize();
			targetHeight = (int) Math.round(size.width / getAspectRatio());
			if (size.height == targetHeight)
				return;
			if (size.height < targetHeight) {
				dialog.setSize(size.width, targetHeight);
				return;
			}
			dialog.setSize(size.width + 10, size.height);
			dialog.validate();
			invalidateComponentTree(this);
			Dimension dialogPrefSize = dialog.getPreferredSize();
			int newPrefHeight = dialogPrefSize.height;
			dialog.setSize((dialog.getSize()).width, newPrefHeight);
		} while (size.height > targetHeight);
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
				if (doubleClickAction != null && doubleClickAction.isEnabled()) {
					doubleClickAction.actionPerformed(null);
				}
			}
		}
	}

}
