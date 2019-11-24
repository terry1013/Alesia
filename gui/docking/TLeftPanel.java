package gui.docking;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.alee.extended.layout.*;
import com.alee.laf.button.*;
import com.alee.laf.panel.*;
import com.alee.managers.style.*;

import core.*;

public class TLeftPanel extends WebPanel {

	public static Dimension colapsedDim = new Dimension(48, -1);
	public static Dimension expandedDim = new Dimension(142, -1);
	ActionMap actionMap;
	private DockingContainer container;
	private ArrayList<WebButton> buttons;

	public TLeftPanel(DockingContainer container) {
		super(StyleId.of("LeftPanel"));
		this.container = container;
		// set preferred size first to allow compute golden ration for icons
		setPreferredSize(colapsedDim);
		setLayout(new VerticalFlowLayout());

		actionMap = Alesia.getInstance().getContext().getActionMap(this);

		// init the buttons and set the basic buttons
		buttons = getLeftPanelButtons(Color.WHITE, actionMap.get("expand"), actionMap.get("settings"));
		appendActions(actionMap.get("expand"), actionMap.get("settings"));
	}

	public void appendActions(Action... actions) {
		WebButton fa = buttons.get(0);
		WebButton la = buttons.get(buttons.size() - 1);
		buttons.clear();
		removeAll();
		// if (actions == null) {
		// actions = new Action[]{actionMap.get("expand"), actionMap.get("settings")};
		// }
		buttons = getLeftPanelButtons(Color.WHITE, actions);
		add(fa);
		for (WebButton wb : buttons) {
			add(wb);
		}
		add(Box.createVerticalBox());
		add(la);
		buttons.add(fa);
		buttons.add(la);
	}
	public static ArrayList<WebButton> getLeftPanelButtons(Color toColor, Action... actions) {
		int size = 20;
		ArrayList<WebButton> list = new ArrayList<>();
		TUIUtils.overRideIcons(size, toColor, actions);
		int gap = 2 + (colapsedDim.width / 4);
		for (Action action : actions) {
			WebButton webButton = new WebButton(StyleId.of("LeftPanelButton"), action);
			webButton.setHorizontalAlignment(SwingConstants.LEFT);
			webButton.setFont(Alesia.title1);
			webButton.setForeground(Color.WHITE);
			webButton.setBorder(null);
			webButton.setIconTextGap(gap);
			webButton.setBorder(new EmptyBorder(0, gap, 0, 0));
			webButton.setPreferredSize(expandedDim.width, colapsedDim.width);
			// TODO: incorporate security
			list.add(webButton);
		}
		return list;
	}

	@org.jdesktop.application.Action
	public void settings(ActionEvent event) {
		TSettingsPanel ts = new TSettingsPanel();
		ts.setActions(actionMap.get("settings"), TActionsFactory.getAction("about"));
		container.setContentPanel(ts);
	}

	@org.jdesktop.application.Action
	public void expand(ActionEvent event) {
		Dimension nd = (getPreferredSize().width == expandedDim.width) ? colapsedDim : expandedDim;
		for (WebButton webButton : buttons) {
			webButton.setText((String) webButton.getAction().getValue(javax.swing.Action.NAME));
		}
		setPreferredSize(nd);
	}
}
