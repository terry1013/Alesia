package gui.jgoodies;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.alee.laf.label.*;
import com.jgoodies.common.base.*;
import com.jgoodies.common.display.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

/**
 * class for display a generic item. this class came in 3 flavors. the construccion of this component dont build the
 * panel. clases must call {@link #buildView()} before use or present this class
 * 
 * @author terry
 *
 */
public class ListItemView extends JPanel {

	public static Color ERROR = Color.RED;
	public static Color NONE = Color.BLACK;
	public static Color SUCCESS = Color.GREEN;
	public static Color WARNING = Color.ORANGE;

	public static String SINGLE_LINE = "single";
	public static String DOUBLE_LINE = "double";
	public static String TRIPLE_LINE = "triple";

	JLabel graphicLabel;
	JLabel overlineLabel;
	JLabel primaryLabel;
	JLabel secondaryLabel;
	JLabel tertiaryLabel;
	JLabel numberLabel;
	JLabel numberUnitLabel;
	JLabel statusLabel;
	JLabel metaLabel;
	protected Color primaryForeground;
	protected Color secondaryForeground;
	protected Color metaForeground;
	protected Color state;
	private String flavor;

	public ListItemView() {
		this(SINGLE_LINE);
	}

	public ListItemView(String flavor) {
		this.flavor = flavor;
		graphicLabel = new WebLabel();
		overlineLabel = new WebLabel();
		primaryLabel = new WebLabel();
		secondaryLabel = new WebLabel();
		tertiaryLabel = new WebLabel();
		numberLabel = new WebLabel();
		numberUnitLabel = new WebLabel();
		statusLabel = new WebLabel();
		metaLabel = new WebLabel();

		// colors
		Color stdc = primaryLabel.getForeground();
		setPrimaryForeground(stdc);
		setSecondaryForeground(stdc);
		setMetaForeground(stdc);

		// primary font
		Font pf = primaryLabel.getFont();
		int si = pf.getSize();
		setPrimaryFont(pf.deriveFont(si + 2f));
		setOverlineFont(pf);
		setSecondaryFont(pf);
		setGraphicPadding(new Insets(0, 0, 0, 16));
		graphicLabel.setVisible(false);
		setMetaPadding(new Insets(4, 16, 0, 0));
		metaLabel.setVisible(false);

		setPadding(new Insets(4, 4, 4, 4));
	}

	private void buildDoubleLine() {
		FormLayout layout = new FormLayout("pref, 96dlu:grow, right:pref", "p, 2dlu, p");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);
		builder.opaque(true);
		CellConstraints cc = new CellConstraints();
		builder.add(graphicLabel, cc.xywh(1, 1, 1, 3));
		builder.add(primaryLabel, cc.xy(2, 1));
		builder.add(secondaryLabel, cc.xy(2, 3));
		builder.add(metaLabel, cc.xywh(3, 1, 1, 3, "center, top"));
		builder.build();
	}

	private void buildSingleLine() {
		FormLayout layout = new FormLayout("pref, 96dlu:grow, p, 3dlu, p, right:pref", "p");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);
		builder.opaque(true);
		CellConstraints cc = new CellConstraints();
		builder.add(graphicLabel, cc.xy(1, 1));
		builder.add(primaryLabel, cc.xy(2, 1));
		builder.add(numberLabel, cc.xy(3, 1));
		builder.add(numberUnitLabel, cc.xy(5, 1));
		builder.add(metaLabel, cc.xy(6, 1));
		builder.build();
	}

	private void buildTripleLine() {
		FormLayout layout = new FormLayout("p, 96dlu:grow, p, 8dlu, right:pref", "p, p, p, p");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);
		builder.opaque(true);
		CellConstraints cc = new CellConstraints();
		builder.add(graphicLabel, cc.xywh(1, 2, 1, 2));
		builder.add(overlineLabel, cc.xy(2, 1));
		builder.add(primaryLabel, cc.xy(2, 2));
		builder.add(secondaryLabel, cc.xy(2, 3));
		builder.add(tertiaryLabel, cc.xy(2, 4));
		builder.add(numberLabel, cc.xy(3, 2, "right, center"));
		builder.add(numberUnitLabel, cc.xy(3, 3, "right, center"));
		builder.add(statusLabel, cc.xy(3, 4, "right, center"));
		builder.add(metaLabel, cc.xywh(5, 2, 1, 2, "right, top"));
		builder.build();
	}

	/**
	 * build this view according to the construction flavor. call this method after settings al necesary values for this
	 * component.
	 */
	public void buildView() {
		if (flavor.equals(SINGLE_LINE)) {
			buildSingleLine();
		}
		if (flavor.equals(DOUBLE_LINE)) {
			buildDoubleLine();
		}
		if (flavor.equals(TRIPLE_LINE)) {
			buildTripleLine();
		}
	}

	public void setBreadcrumbs(String... pathNodes) {
		StringBuffer bc = new StringBuffer();
		Arrays.asList(pathNodes).stream().forEach(pn -> bc.append(" / "));
		setOverline(bc.substring(0, bc.length() - 3));
	}

	public void setGraphic(Icon icon) {
		graphicLabel.setIcon(icon);
		graphicLabel.setVisible((icon != null));
	}

	public void setGraphicPadding(Insets padding) {
		graphicLabel.setBorder(new EmptyBorder(padding));
	}

	public void setMeta(Icon icon) {
		metaLabel.setIcon(icon);
		metaLabel.setVisible((icon != null));
	}

	public void setMeta(String str, Object... args) {
		String text = Strings.get(str, args);
		metaLabel.setIcon(null);
		metaLabel.setText(text);
		metaLabel.setVisible(Strings.isNotBlank(text));
	}

	public void setMetaForeground(Color foreground) {
		metaForeground = foreground;
		metaLabel.setForeground(foreground);
	}

	public void setMetaPadding(Insets padding) {
		metaLabel.setBorder(new EmptyBorder(padding));
	}

	public void setMetaState(Color state) {
		setMetaForeground(state);
	}

	public void setNumber(String value) {
		numberLabel.setText(value);
	}

	public void setNumberUnit(String unit) {
		numberUnitLabel.setText(unit);
	}

	public void setOverline(String str, Object... args) {
		boolean blank = Strings.isBlank(str);
		overlineLabel.setVisible(!blank);
		if (blank)
			return;
		overlineLabel.setText(Strings.get(str, args).toUpperCase());
	}

	public void setOverlineFont(Font font) {
		overlineLabel.setFont(font);
	}

	public void setPadding(Insets padding) {
		setBorder(new EmptyBorder(padding));
	}

	public void setPrimaryFont(Font font) {
		primaryLabel.setFont(font);
		numberLabel.setFont(font);
	}

	public void setPrimaryForeground(Color foreground) {
		primaryForeground = foreground;
		primaryLabel.setForeground(foreground);
		numberUnitLabel.setForeground(foreground);
	}

	public void setPrimaryText(String str, Object... args) {
		primaryLabel.setText(Strings.get(str, args));
	}

	public void setSecondaryFont(Font font) {
		secondaryLabel.setFont(font);
		tertiaryLabel.setFont(font);
		numberUnitLabel.setFont(font);
		statusLabel.setFont(font);
	}

	public void setSecondaryForeground(Color foreground) {
		secondaryForeground = foreground;
		secondaryLabel.setForeground(foreground);
		tertiaryLabel.setForeground(foreground);
		numberUnitLabel.setForeground(foreground);
	}

	public void setSecondaryText(String str, Object... args) {
		secondaryLabel.setText(Strings.get(str, args));
	}

	public void setStatus(Color state, Displayable value) {
		setStatus(state, value.getDisplayString(), new Object[0]);
	}

	public void setStatus(Color state, String str, Object... args) {
		this.state = state;
		statusLabel.setText(Strings.get(str, args));
	}

	public void setTertiaryText(String str, Object... args) {
		tertiaryLabel.setText(Strings.get(str, args));
	}
}
