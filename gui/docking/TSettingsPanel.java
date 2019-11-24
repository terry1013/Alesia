package gui.docking;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.alee.laf.button.*;
import com.alee.laf.label.*;
import com.alee.laf.panel.*;
import com.alee.managers.style.*;

import core.*;

public class TSettingsPanel extends WebPanel {

	private JLabel titleLable;
	private WebPanel center;

	public TSettingsPanel() {
		super(StyleId.of("RightPanel"));
		setLayout(new BorderLayout());
		this.titleLable = new WebLabel("asdfasdf ");
		titleLable.setFont(Alesia.title1);
		titleLable.setBorder(new EmptyBorder(10, 10, 10, 10));

		center = new WebPanel();
		center.setLayout(new GridLayout(4, 3));

		add(titleLable, BorderLayout.NORTH);
		add(center, BorderLayout.CENTER);
	}

	public void setActions(Action invoker, Action... actions) {
		titleLable.setText((String) invoker.getValue(Action.NAME));
		ArrayList<WebButton> lst = DockingContainer.createNavButtons(null, "RightPanelButton", null, actions);
		for (WebButton webButton : lst) {
			webButton.setVerticalAlignment(SwingConstants.TOP);
			Action a = webButton.getAction();
			String nt = "<html>" + (String) a.getValue(Action.NAME) + "<br>"
					+ (String) a.getValue(Action.SHORT_DESCRIPTION) + "</html>";
			webButton.setText(nt);
			center.add(webButton);
		}
	}
}
