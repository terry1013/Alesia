package gui.console;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.text.*;

import org.jdesktop.application.Action;

import com.alee.extended.layout.*;
import com.alee.laf.combobox.*;
import com.alee.laf.panel.*;
import com.alee.laf.text.*;
import com.alee.managers.settings.*;

import core.*;

/**
 * Component to show messages from the {@link Logger} system. this class is used to see the logg messages control the
 * log level an filter menssages. The instance of {@link Logger} passed in the constructor argument is modified to acept
 * new {@link Handler} to allow this component see the messages. This class can modify the level of the recived logger
 * (and the internal handler )to allow whach diferect levels. The handler can be configured in the logging.properties
 * file under the TextHandler section
 * 
 * @author terry
 *
 */
public class ConsolePanel extends WebPanel {

	private WebEditorPane editorPane;
	private ActionMap actionMap;
	private Logger logger;
	private TextAreaHandler handler;
	private String blanck = "                                                                                                    ";

	public ConsolePanel(Logger logger) {
		setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP));
		this.logger = logger;
		actionMap = Alesia.getInstance().getContext().getActionMap(this);
		this.editorPane = new WebEditorPane();
		editorPane.setEditable(false);
		editorPane.setEditorKit(new StyledEditorKit());
		Font f = new Font("courier new", Font.PLAIN, 12);
		editorPane.setFont(f);

		OutputStream os = new TextAreaOutputStream(editorPane);
		handler = new TextAreaHandler(os);
		logger.addHandler(handler);

		// temporal
		WebPanel toolbar = new WebPanel(new LineLayout(LineLayout.HORIZONTAL));

		Vector<Level> lvl = new Vector<>();
		lvl.add(Level.OFF);
		lvl.add(Level.SEVERE);
		lvl.add(Level.WARNING);
		lvl.add(Level.INFO);
		lvl.add(Level.CONFIG);
		lvl.add(Level.FINE);
		lvl.add(Level.FINER);
		lvl.add(Level.FINEST);
		lvl.add(Level.ALL);
		WebComboBox comboBox = new WebComboBox(lvl);
		comboBox.addActionListener(evt -> setLoggerLever((Level) comboBox.getSelectedItem()));
		comboBox.registerSettings(new Configuration<ComboBoxState>("logging.level"));
		setLoggerLever((Level) comboBox.getSelectedItem());
		// GroupPane gp = new GroupPane();
		// WebToggleButton wtb = TUIUtils.getWebToggleButton(actionMap.get("showAllMessage"));
		// wtb.registerSettings(new Configuration<ButtonState>("Console.showAllMessage"));
		// gp.add(wtb);
		// wtb = TUIUtils.getWebToggleButton(actionMap.get("showFinerMessage"));
		// wtb.registerSettings(new Configuration<ButtonState>("Console.showFinerMessage"));
		// gp.add(wtb);
		// wtb = TUIUtils.getWebToggleButton(actionMap.get("showInfoMessage"));
		// wtb.registerSettings(new Configuration<ButtonState>("Console.showInfoMessage"));
		// gp.add(wtb);
		// wtb = TUIUtils.getWebToggleButton(actionMap.get("showWarnMessage"));
		// wtb.registerSettings(new Configuration<ButtonState>("Console.showWarnMessage"));
		// gp.add(wtb);

		toolbar.add(comboBox, TUIUtils.getWebButtonForToolBar(actionMap.get("cleanConsole")));

		editorPane.setSize(Short.MAX_VALUE, 150);
		editorPane.setText(blanck);
		JScrollPane jsp = new JScrollPane(editorPane);
		add(toolbar);
		add(jsp);

		Dimension prefd = editorPane.getPreferredSize();
		jsp.getViewport().setPreferredSize(new Dimension(prefd.width, 150));
		jsp.getViewport().setMinimumSize(new Dimension(prefd.width, 150));
		// editorPane.setText("");

	}

	public void cleanConsole() {
		editorPane.setText(blanck);
	}
	@Action
	public void cleanConsole(ActionEvent event) {
		cleanConsole();
	}

	private void setLoggerLever(Level level) {
		logger.setLevel(level);
		handler.setLevel(level);
		logger.log(level, "Loggin level set to " + level);
	}
}
