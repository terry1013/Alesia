package gui.console;

import java.awt.*;

import javax.swing.*;
import javax.swing.text.*;

import com.alee.extended.layout.*;
import com.alee.laf.panel.*;
import com.alee.laf.text.*;

import core.*;

/**
 * component to display the alesia console. This component must be retrived using
 * {@link TUIUtils#getMessageConsolePanel(String)}
 * 
 *  TODO: incorporate security 
 * 
 * @param grouping
 * @return
 * 
 * @author terry
 *
 */
public class MessageConsolePanel extends WebPanel {

	private WebEditorPane editorPane;
	private MessageConsole console;

	public MessageConsolePanel() {
//		TODO: present only the messages asociated whit the grouping argument.
		setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP));
		this.editorPane = new WebEditorPane();
		editorPane.setEditable(false);
		editorPane.setEditorKit(new StyledEditorKit());
		Font f = new Font("courier new", Font.PLAIN, 12);
		editorPane.setFont(f);
		editorPane.setText(
				"                                                                                                                        ");

		this.console = new MessageConsole(editorPane);
		console.redirectOut(Color.BLACK, System.out);
		console.redirectErr(Color.RED, System.err);
		// temporal with
		WebPanel toolbar = new WebPanel(new LineLayout(LineLayout.HORIZONTAL));
		toolbar.add(TUIUtils.getWebButtonForToolBar(console.getActionMap().get("showAllMessage")));
		toolbar.add(TUIUtils.getWebButtonForToolBar(console.getActionMap().get("showInfoMessage")));
		toolbar.add(TUIUtils.getWebButtonForToolBar(console.getActionMap().get("showWarnMessage")));

		JScrollPane jsp = new JScrollPane(editorPane);
		jsp.setPreferredSize(new Dimension(100, 150));

		add(toolbar);
		add(jsp);
	}

	public void clearConsole() {
		editorPane.setText("");
	}
}
