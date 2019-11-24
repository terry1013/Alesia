package gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.text.html.*;

public class HTMLUtils {
	private static Font cachedTextFont;
	private static String cachedDefaultRule;

	public static void addDefaultStyleSheetRule(JEditorPane editorPane) {
		addStyleSheetRule(editorPane, getDefaultRule());
	}

	public static void addDefaultStyleSheetRule(JEditorPane editorPane, Font textFont) {
		addStyleSheetRule(editorPane, createDefaultRule(textFont));
	}

	public static void addStyleSheetRule(JEditorPane editorPane, String rule) {
		HTMLEditorKit kit = (HTMLEditorKit) editorPane.getEditorKit();
		StyleSheet styleSheet = kit.getStyleSheet();
		styleSheet.addRule(rule);
	}

	public static Font getDefaultTextFont() {
		String fontKey = "Label.font";
		return UIManager.getFont(fontKey);
	}

	private static String getDefaultRule() {
		Font textFont = getDefaultTextFont();
		if (textFont != cachedTextFont) {
			cachedDefaultRule = createDefaultRule(textFont);
			cachedTextFont = textFont;
		}
		return cachedDefaultRule;
	}

	private static String createDefaultRule(Font font) {
		return "body, p, td, a, li { font-family: " + font.getName() + "; font-size: " + font.getSize()
				+ "pt;  }a                  { color: #0066CC; }";
	}
}