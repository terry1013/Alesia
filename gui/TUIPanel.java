package gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.Action;
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
public class TUIPanel extends JPanel {

	public static double ASPECT_RATION_NONE = 0.0;
	public static double ASPECT_RATION_NARROW = 1.3333;
	public static double ASPECT_RATION_DEFAULT = 1.6666;
	public static double ASPECT_RATION_WIDE = 1.7777;
	protected Vector<Action> actions;
	private JComponent bodyJComponent, footerJComponent;
	private WebLabel titleLabel;
	private ActionMap actionMap;
	private WebButton treeDotButton;

	private JEditorPane additionalInfo;

	private JPanel titlePanel;
	private WebDialog dialog;
	double aspectRatio = ASPECT_RATION_DEFAULT;

	private JPopupMenu popupMenu;
	private Action doblecClickAction;
	private WebPanel toolBarPanel;
	
	public WebPanel getToolBarPanel() {
		return toolBarPanel;
	}
	
	public TUIPanel() {
		super(new BorderLayout());
		this.actions = new Vector<>();
		this.titleLabel = new WebLabel(" ");
		titleLabel.setFont(Alesia.title1);
		actionMap = Alesia.getInstance().getContext().getActionMap((TUIPanel) this);
		this.treeDotButton = getTreeDotButton();

//	temporal 
		this.toolBarPanel = new WebPanel(StyleId.panelTransparent);
		toolBarPanel.setLayout(new LineLayout(LineLayout.HORIZONTAL, 0));
		
		// tilte label + 3dot button
		this.titlePanel = new WebPanel(StyleId.panelTransparent);
		titlePanel.setLayout(new BorderLayout());
//		titlePanel.add(titleLabel, BorderLayout.CENTER);
		titlePanel.add(toolBarPanel, BorderLayout.CENTER);
		titlePanel.add(treeDotButton, BorderLayout.EAST);

		this.additionalInfo = createReadOnlyEditorPane(null, null);
		additionalInfo.setPreferredSize(new Dimension(0, 48));

		WebPanel north = new WebPanel(StyleId.panelTransparent);
		north.setLayout(new BorderLayout());
		north.add(titlePanel, BorderLayout.NORTH);
		north.add(additionalInfo, BorderLayout.CENTER);

		add(north, BorderLayout.NORTH);
	}

	@Override
	@Deprecated
	public Component add(Component comp) {
		// do nothig. force use set### methods
		return comp;
	}

	public final WebDialog createDialog() {
		// Preconditions.checkState(EventQueue.isDispatchThread(), "You must create and show dialogs from the
		// Event-Dispatch-Thread (EDT).");
		// checkWindowTitle(title);
		if (dialog != null) {
			// dialog.setTitle(" ");
			return dialog;
		}
		dialog = new WebDialog(StyleId.dialogDecorated, Alesia.getMainFrame());
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
		setDialogAspectRatio();
		dialog.setLocationRelativeTo(Alesia.getMainFrame());
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

	public void setAditionalInformationVisible(boolean aFlag) {
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

	public void setDescription(String tId) {
		additionalInfo.setText(Alesia.getResourceMap().getString(tId));
	}

	/**
	 * set an standar footer area for components intendet to input data. this component is a list of {@link JButton}
	 * ordered as the input argument styled as standar alesia button bar.
	 * <p>
	 * NOTE: the actions bust be located in {@link TActionsFactory} class
	 * 
	 * @param actions list of actions
	 */
	public void setFooterActions(String... actions) {
		this.actions = new Vector<>();
		ActionMap amap = Alesia.getActionMap();
		Vector<JComponent> lst = new Vector<>();
		lst.add(new JLabel());
		// lst.addAll(createWebButtons(actions));
		GroupPanel groupPane = new GroupPanel(GroupingType.fillFirst, true,
				(JComponent[]) lst.toArray(new JComponent[lst.size()]));

		// GroupPane groupPane = new GroupPane(StyleId.grouppane, (WebButton[]) lst.toArray(new WebButton[lst.size()]));
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
	
	public void setToolBar(Action...actions) {
		setToolBar(Arrays.asList(actions));
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
			WebButton wb = TUIUtils.getWebButtonForToolBar(act);
			// componets.add(wb);
			// action scope
			ApplicationAction aa = (ApplicationAction) act;
			String sco = aa.getResourceMap().getString(aa.getName() + ".Action.scope");
			if (sco != null && sco.equals("element")) {
				JMenuItem jmi = new JMenuItem(act);
				jmi.setIcon(null);
				// doble click para la primera que encuentre
				this.doblecClickAction = doblecClickAction == null ? act : doblecClickAction;
				jmi.setFont(jmi.getFont().deriveFont(Font.BOLD));
				popupMenu.add(jmi);
			}
			toolBarPanel.add(wb);
		}

		// 171231: append some standar actions for list sublcases
//		toolBarPanel.add(TUIUtils.getWebButtonForToolBar(actionMap.get("filterList")), LineLayout.END);
//		toolBarPanel.add(TUIUtils.getWebButtonForToolBar(actionMap.get("refreshList")), LineLayout.END);
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
}
