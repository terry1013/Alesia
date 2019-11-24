package plugin.mailbomber;

import java.awt.*;
import java.io.*;

import javafx.application.*;
import javafx.embed.swing.*;
import javafx.scene.*;
import javafx.scene.web.*;

import javax.swing.*;

import core.datasource.*;

public class ComposeMailViewer extends JPanel {

	private final JFXPanel jfxPanel = new JFXPanel();
	private WebEngine engine;
	private String pattid;

	public ComposeMailViewer() {
		super(new BorderLayout());
		createScene();
		add(jfxPanel, BorderLayout.CENTER);
	}

	private void createScene() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				WebView view = new WebView();
				engine = view.getEngine();
				jfxPanel.setScene(new Scene(view));
			}
		});
	}

	public void load(String tid) {
		this.pattid = tid;
		System.out.println("ComposeMailViewer.load()");
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.println("ComposeMailViewer.load(...).new Runnable() {...}.run()");
					Record ter = ConnectionManager.getAccessTo("M_TEMPLATES").exist("M_TEID = '" + pattid + "'");
					File mt = MailBomberTask.extractMailTemplate((byte[]) ter.getFieldValue("m_tetemplate"));
					String d = mt.getParent();
					File f1 = new File(d + "/index.html");
					String url = f1.toURI().toURL().toString();
					engine.load(url);
					// engine.reload();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}