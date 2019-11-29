package gui.console;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.text.*;

import org.jdesktop.application.Action;

import com.alee.extended.layout.*;
import com.alee.laf.button.*;
import com.alee.laf.grouping.*;
import com.alee.laf.panel.*;
import com.alee.laf.text.*;
import com.alee.managers.settings.*;

import core.*;

/**
 * component to display the alesia console. This component must be retrived using
 * {@link TUIUtils#getMessageConsolePanel(String)}
 * 
 * TODO: incorporate security
 * 
 * @param grouping
 * @return
 * 
 * @author terry
 *
 */
public class MessageConsolePanel extends WebPanel {

	private WebEditorPane editorPane;
	private Logger logger;
	private ActionMap actionMap;
	private MessageConsole console;

	public MessageConsolePanel(Logger logger) {
		setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP));
		actionMap = Alesia.getInstance().getContext().getActionMap(this);
		this.editorPane = new WebEditorPane();
		editorPane.setEditable(false);
		editorPane.setEditorKit(new StyledEditorKit());
		Font f = new Font("courier new", Font.PLAIN, 12);
		editorPane.setFont(f);
		this.logger = logger;
		editorPane.setSize(Short.MAX_VALUE, 150);
		editorPane.setText(
				"012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");
		Dimension prefd = editorPane.getPreferredSize();
		editorPane.setText("");

		this.console = new MessageConsole(editorPane);
		console.redirectOut(Color.BLACK, System.out);
		console.redirectErr(Color.RED, System.err);

		// temporal with
		WebPanel toolbar = new WebPanel(new LineLayout(LineLayout.HORIZONTAL));

		GroupPane gp = new GroupPane();
		WebToggleButton wtb = TUIUtils.getWebToggleButton(actionMap.get("showAllMessage"));
		wtb.registerSettings(new Configuration<ButtonState>("Console.showAllMessage"));
		gp.add(wtb);
		wtb = TUIUtils.getWebToggleButton(actionMap.get("showInfoMessage"));
		wtb.registerSettings(new Configuration<ButtonState>("Console.showInfoMessage"));
		gp.add(wtb);
		wtb = TUIUtils.getWebToggleButton(actionMap.get("showWarnMessage"));
		wtb.registerSettings(new Configuration<ButtonState>("Console.showWarnMessage"));
		gp.add(wtb);

		toolbar.add(gp, TUIUtils.getWebButtonForToolBar(actionMap.get("cleanConsole")));

		JScrollPane jsp = new JScrollPane(editorPane);
		jsp.getViewport().setPreferredSize(new Dimension(prefd.width, 150));
		// jsp.setPreferredSize(new Dimension(prefd.width, 150));
		// jsp.setSize(new Dimension(prefd.width, 150));
		// jsp.setMinimumSize(new Dimension(prefd.width, 150));
		// editorPane.setPreferredSize(new Dimension(prefd.width, 150));
		// editorPane.setSize(new Dimension(prefd.width, 150));
		// editorPane.setMinimumSize(new Dimension(prefd.width, 150));

		add(toolbar);
		add(jsp);
		setLoggerLever(SettingsManager.get("LoggerLevel", Level.ALL));
	}

	public void cleanConsole() {
		editorPane.setText("");
	}
	@Action
	public void cleanConsole(ActionEvent event) {
		cleanConsole();
	}

	@Action
	public void showAllMessage(ActionEvent event) {
		setLoggerLever(Level.ALL);
	}

	@Action
	public void showInfoMessage(ActionEvent event) {
		setLoggerLever(Level.INFO);
	}

	@Action
	public void showWarnMessage(ActionEvent event) {
		setLoggerLever(Level.WARNING);
	}

	private void setLoggerLever(Level level) {
		logger.setLevel(level);
		Arrays.asList(logger.getHandlers()).stream().forEach(h->h.setLevel(level));
		Level ll = level.equals(Level.ALL) ? Level.INFO : level;
		logger.log(ll, "Logger level changed to " + level.getName());
		SettingsManager.set("LoggerLevel", level);
	}
}
