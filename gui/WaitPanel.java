package gui;

import java.awt.*;
import java.text.*;

import javax.swing.*;
import javax.swing.border.*;

import com.alee.laf.panel.*;

import core.*;

public class WaitPanel extends WebPanel {

	private JLabel jLabel;
	private String messageId, waitIcon;

	public WaitPanel() {
		this("waitMsg", "clock-loading");
	}
	public WaitPanel(String msgid, String ii) {
		super();
		messageId = msgid;
		waitIcon = ii;
		jLabel = new JLabel(TStringUtils.getString(messageId), TResources.getIcon(waitIcon), JLabel.CENTER);
		// jLabel.setHorizontalAlignment(JLabel.CENTER);
		jLabel.setVerticalTextPosition(JLabel.BOTTOM);
		jLabel.setHorizontalTextPosition(JLabel.CENTER);
		jLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
		add(jLabel, BorderLayout.CENTER);
	}

	public void setIcon(String wi) {
		jLabel.setIcon(TResources.getIcon(waitIcon));
	}

	public void setMessageId(String id) {
		this.messageId = id;
	}

	/**
	 * Update the progress for this wait panel. this method formatt the message whit all arguments
	 * 
	 * @param dta - message data
	 */
	public void setProgress(Object... dta) {
		jLabel.setText(MessageFormat.format(TStringUtils.getString(messageId), dta));
	}
}
