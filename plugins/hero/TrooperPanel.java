package plugins.hero;

import java.awt.*;

import javax.swing.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
import gui.*;

public class TrooperPanel extends TUIFormPanel {
	private TUIPanel reportPanel;

	public TrooperPanel() {
		this.reportPanel = new TUIPanel();

		addInputComponent(TUIUtils.getWebFormattedTextField("table.buyIn", 10000.0, 5));
		addInputComponent(TUIUtils.getWebFormattedTextField("table.bigBlid", 100.0, 5));
		addInputComponent(TUIUtils.getWebFormattedTextField("table.smallBlid", 50.0, 5));
		addInputComponent(TUIUtils.getTWebComboBox("preflopStrategy", "table.strategy"));

		FormLayout layout = new FormLayout("right:pref, 3dlu, left:pref, 3dlu, left:pref, 3dlu, left:pref", // 1st major colum
				""); // add rows dynamically
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DIALOG);

		builder.append(TStringUtils.getString("table.parameters"));
		builder.append(getInputComponent("table.buyIn"));
		builder.append(getInputComponent("table.bigBlid"));
		builder.append(getInputComponent("table.smallBlid"));
		builder.nextLine();
		builder.append(TStringUtils.getString("preflopStrategy"));
		builder.append(getInputComponent("preflopStrategy"), 5);

		 JPanel jp = new JPanel(new BorderLayout());
		 jp.add(builder.getPanel(), BorderLayout.NORTH);
		 jp.add(reportPanel, BorderLayout.CENTER);

//		builder.append(reportPanel, 4);

		setBodyComponent(jp);
		registreSettings();
	}

	public void updatePockerSimulator(SensorsArray sensorsArray) {
		reportPanel.removeAll();
		PokerSimulator simulator = sensorsArray.getPokerSimulator();
		reportPanel.add(simulator.getReportPanel(), BorderLayout.CENTER);
	}
}
