package gui.docking;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import com.alee.extended.layout.*;
import com.alee.laf.button.*;
import com.alee.laf.label.*;
import com.alee.laf.panel.*;
import com.alee.managers.style.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;
import com.jgoodies.forms.layout.FormLayout;

import core.*;
import gui.*;
import gui.jgoodies.*;

public class TRightPanel extends WebPanel implements FocusListener {

	private JButton backButton;
	private JLabel titleLable;
	private Vector<WebPanel> panels;
	private Dimension size = new Dimension(200, -1);
	// private Vector<JComponent>

	public TRightPanel() {
		super(StyleId.of("RightPanel"), new BorderLayout());
		add(createNavPanel(), BorderLayout.NORTH);
		setSize(size);
		setPreferredSize(size);
		setVisible(false);
		panels = new Vector<>();
		addFocusListener(this);
		// setActions(Alesia.getActionMap().get("about"));
	}

	private void checkGoBackVisibility() {
		backButton.setVisible(panels.size() > 1);
	}
	public void setActions(Action invoker, Action... actions) {
		titleLable.setText((String) invoker.getValue(Action.NAME));
		ArrayList<WebButton> lst = DockingContainer.createNavButtons(null, "RightPanelButton", Alesia.title2, actions);
		WebPanel jp = new WebPanel(StyleId.of("RightPanel"));
		jp.setLayout(new VerticalFlowLayout());
		JComponent gf = null;
		for (WebButton webButton : lst) {
			webButton.setPreferredSize(size);
			jp.add(webButton);
			gf = gf == null ? webButton : gf;
		}
		panels.add(jp);
		add(jp, BorderLayout.CENTER);
		checkGoBackVisibility();
		setVisible(true);
		gf.grabFocus();
		// for (Action action : actions) {
		// ListItemView i = new ListItemView(ListItemView.DOUBLE_LINE);
		// i.setPrimaryText((String) action.getValue(Action.NAME));
		// i.setGraphic((Icon) action.getValue(Action.SMALL_ICON));
		// i.setSecondaryText((String) action.getValue(Action.SHORT_DESCRIPTION));
		// i.buildView();
		// actionList.add(i);
		// }
	}

	/**
	 * present the previous element. the action remove the actual visible component form this container.
	 */
	@org.jdesktop.application.Action
	public void goBack(ActionEvent event) {
		WebPanel p = panels.remove(panels.size() - 1);
		remove(p);
		checkGoBackVisibility();
		// revalidate();
	}
	private JPanel createNavPanel() {
		ActionMap amap = Alesia.getInstance().getContext().getActionMap(this);
		backButton = new WebButton(StyleId.of("RightPanelButton"), amap.get("goBack"));
		TUIUtils.overRideIcons(Alesia.title1.getSize(), null, backButton.getAction());
		backButton.setText(null);
		this.titleLable = new JLabel(" ");
		WebPanel p = new WebPanel(StyleId.panelTransparent);
		FormLayout layout = new FormLayout("pref, left:0:grow", "p");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout, p);
		CellConstraints cc = new CellConstraints();
		builder.add(this.backButton, cc.xy(1, 1));
		builder.add(this.titleLable, cc.xy(2, 1));
		return builder.getPanel();
	}

	@Override
	public void focusGained(FocusEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void focusLost(FocusEvent e) {
		System.out.println("TRightPanel.focusLost()");
		panels.clear();
		setVisible(false);
	}

}
