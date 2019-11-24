package plugin.mailbomber;

import java.awt.*;
import java.io.*;
import java.net.*;

import javafx.application.*;
import javafx.embed.swing.*;
import javafx.scene.*;
import javafx.scene.web.*;

import javax.swing.*;

import org.w3c.dom.*;

import core.*;

public class TinyMCEEditor2 extends JPanel {

	private final JFXPanel jfxPanel = new JFXPanel();
	private WebEngine engine;

	public TinyMCEEditor2() {
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
				engine.load(getTinyMCEPage());
				jfxPanel.setScene(new Scene(view));
			}
		});
	}

	public String getContent() {
		Document doc = engine.getDocument();
		return doc.getTextContent();
	}
	
	public String getTextArea(String html) {
		int fi = html.indexOf("<textarea>");
		int ei = html.indexOf("</textarea>");
		String tmp = html.substring(fi, ei);
		return tmp;
	}

	public String getTinyMCEPage() {
		File nf = TResources.getFile("/plugin/mailbomber/tinyMCE/index.html");
		String url = null;
		try {
			url = nf.toURI().toURL().toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		SystemLog.info("tinymce temporal page: "+url);
		return url;
	}

	public void setContent(String txt) {

	}
}