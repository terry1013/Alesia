package gui.wlaf;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.*;

import org.jdesktop.core.animation.timing.*;
import org.jdesktop.swing.animation.timing.sources.*;

import com.alee.api.jdk.*;
import com.alee.extended.transition.*;
import com.alee.extended.transition.effects.fade.*;
import com.alee.laf.rootpane.*;
import com.alee.laf.window.*;
import com.alee.managers.settings.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import action.*;
import core.*;

public class TWebFrame extends WebFrame {

	private static Animator frameAnimator;
	private ComponentTransition transitionPanel;
	private JComponent splashPanel, contentPanel;
	private JLabel splashIncrementLabel;

	public TWebFrame() {
		super();
		setBackground(Color.WHITE);
		transitionPanel = new ComponentTransition();
		transitionPanel.setTransitionEffect(new FadeTransitionEffect());
		setSplashTitleText(Alesia.getResourceMap().getString("name"));
		setSplashSubtitleText(Alesia.getResourceMap().getString("description"));
		splashIncrementLabel = new JLabel(" ");
		// waitComponent = new WaitPanel();
		splashPanel = buildSplash();
		transitionPanel.setContent(splashPanel);
		setContentPane(transitionPanel);

		setTitle(TStringUtils.getString("title"));
		Vector v = new Vector();
		v.add(TResources.getIcon("appicon", 16).getImage());
		v.add(TResources.getIcon("appicon", 32).getImage());
		setIconImages(v);

		registerSettings(new Configuration<WindowState>("application", new SerializableSupplier<WindowState>() {
			@Override
			public WindowState get() {
				// initial size
				return new WindowState(new Dimension(800, 600));
			}
		}));

		WindowAdapter ad = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				Alesia.getInstance().exit();
			}
		};
		addWindowListener(ad);

		SwingTimerTimingSource ts = new SwingTimerTimingSource();
		AnimatorBuilder.setDefaultTimingSource(ts);
		frameAnimator = new AnimatorBuilder().setDuration(250, TimeUnit.MILLISECONDS).build();
		// ts.init();
	}

	/**
	 * Return a {@link Rectangle} center on this frame {@link GraphicsConfiguration} and size based on factor f
	 * 
	 * @param factor - size factor
	 * @return bounds
	 */
	public Rectangle getBoundByFactor(double factor) {
		return getBoundByFactor(factor, factor);
	}

	/**
	 * Return a {@link Rectangle} center on this frame {@link GraphicsConfiguration} and size based on the sith and
	 * height arguments.
	 * 
	 * @param withFactor - width factor
	 * @param heightFactor - height factor
	 * @return center rectangle
	 */
	public Rectangle getBoundByFactor(double withFactor, double heightFactor) {
		Rectangle gcr = getGraphicsConfiguration().getBounds();
		Rectangle rr = new Rectangle(0, 0, (int) (gcr.width * withFactor), (int) (gcr.height * heightFactor));
		center(gcr, rr);
		return rr;
	}

	/**
	 * center the r2 rectangle based on r1 coordenates
	 * 
	 * @param r1 - base rectangle
	 * @param r2 - rectangle to center
	 */
	private void center(Rectangle r1, Rectangle r2) {
		r2.x = (r1.width - r2.width) / 2;
		r2.y = (r1.height - r2.height) / 2;
	}

	public void setContent(JComponent c) {
		this.contentPanel = c;
		splashPanel.setPreferredSize(contentPanel.getPreferredSize());
		transitionPanel.setContent(contentPanel);
	}

	/**
	 * Calc new rectangle resulting that is centered based on src rectangle
	 * 
	 * @param src - orginial rectangle
	 * @param dim - new size of orginal rectangle
	 * 
	 * @return center rectangle
	 */
	public static Rectangle zoomIn(Rectangle src, Dimension dim) {
		Rectangle dest = new Rectangle(src);
		dest.setSize(dim);
		int hx = (dim.width - src.width) / 2;
		int hy = (dim.height - src.height) / 2;
		dest.x -= hx;
		dest.y -= hy;
		return dest;
	}

	/**
	 * @return the transitionPanel
	 */
	public ComponentTransition getTransitionPanel() {
		return transitionPanel;
	}
	private Icon splashIcon;
	private String splashTitleText;
	private Font splashTitleFont;
	private String splashSubtitleText;
	private Font splashSubtitleFont;

	private JComponent buildSplash() {
		JLabel splashTitleLabel;
		if (this.splashTitleText != null) {
			splashTitleLabel = new JLabel(this.splashTitleText);
			splashTitleLabel.setFont(this.getSplashTitleFont());
			splashTitleLabel.setForeground(this.getSplashTitleForeground());
		} else if (this.splashIcon != null) {
			splashTitleLabel = new JLabel(this.splashIcon);
		} else {
			splashTitleLabel = null;
		}
		JLabel splashSubtitleLabel = null;
		if (this.splashSubtitleText != null) {
			splashSubtitleLabel = new JLabel(this.splashSubtitleText);
			splashSubtitleLabel.setFont(this.getSplashSubitleFont());
			splashSubtitleLabel.setForeground(this.isDarkBackground() ? Color.WHITE : Color.DARK_GRAY);
		}

		FormLayout lay = new FormLayout("0:grow, center:pref, 0:grow", "0:g, p, 0, p, 0, p, 0:g");
		PanelBuilder bui = new PanelBuilder(lay);
		CellConstraints cc = new CellConstraints();
		bui.add(splashTitleLabel, cc.xy(2, 2));
		bui.add(splashSubtitleLabel, cc.xy(2, 4));
		bui.add(splashIncrementLabel, cc.xy(2, 6));
		return bui.getPanel();
	}
	public Icon getSplashIcon() {
		return splashIcon;
	}
	public void setSplashIncrementText(String text) {
		splashIncrementLabel.setText(text);
		Alesia.logger.info(text);
	}

	public void setSplashIcon(Icon splashIcon) {
		this.splashIcon = splashIcon;
	}

	public String getSplashTitleText() {
		return splashTitleText;
	}

	public void setSplashTitleText(String splashTitleText) {
		this.splashTitleText = splashTitleText;
	}

	public String getSplashSubtitleText() {
		return splashSubtitleText;
	}

	public void setSplashSubtitleText(String splashSubtitleText) {
		this.splashSubtitleText = splashSubtitleText;
	}

	private Font getSplashTitleFont() {
		return (this.splashTitleFont != null) ? this.splashTitleFont : getFont(Font.PLAIN, 72);
	}

	private Font getSplashSubitleFont() {
		return (this.splashSubtitleFont != null) ? this.splashSubtitleFont : getFont(Font.PLAIN, 24);
	}

	private static Font getFont(final int style, final float size) {
		return (new JLabel()).getFont().deriveFont(style, size);
	}

	private Color getSplashTitleForeground() {
		return this.isDarkBackground() ? new Color(241, 241, 241) : new Color(100, 100, 100);
	}
	/**
	 * TODO: move to colorutils
	 * 
	 * @return
	 */
	private boolean isDarkBackground() {
		final Color bg = getBackground();
		final float[] hsbVals = new float[3];
		Color.RGBtoHSB(bg.getRed(), bg.getGreen(), bg.getBlue(), hsbVals);
		return hsbVals[2] < 0.8f;
	}

}
