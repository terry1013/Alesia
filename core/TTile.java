package core;

import gui.*;

import java.awt.*;
import java.util.concurrent.*;

import javax.swing.*;

import org.jdesktop.core.animation.timing.*;

import com.alee.extended.image.*;
import com.alee.extended.panel.*;
import com.alee.laf.button.*;
import com.alee.laf.label.*;
import com.alee.laf.panel.*;

public class TTile extends JPanel  {

	private Class class1;
	private Animator animator;
	private UIComponentPanel uipanel;
	private WebPanel popOver;
	private WebButton button;
	private Rectangle r1, r2;
	private Point p1, p2;
	private Dimension framedim;
	private CardLayout layout;
	private TTilePanel tilePanel;

	public TTile(Class cls, TTilePanel tp) {
		this.class1 = cls;
		this.tilePanel = tp;
		this.layout = new CardLayout();
		setLayout(layout);
		String sn = class1.getSimpleName();
		this.button = new WebButton(TStringUtils.getString(sn), TResources.getIcon(sn, 48));
		button.setName(sn);
//		button.setUndecorated(true);
		button.setVerticalTextPosition(JLabel.BOTTOM);
		button.setHorizontalTextPosition(JLabel.CENTER);
		button.addActionListener(tilePanel);
		try {
			uipanel = (UIComponentPanel) class1.newInstance();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (uipanel instanceof UIListPanel) {
			((UIListPanel) uipanel).init();
		}
		add(button, "Button");
		buildInputComponent();
//		add(popOver, "Content");
		animator = new AnimatorBuilder().setDuration(250, TimeUnit.MILLISECONDS).build();
	}

	public void buildInputComponent() {
		popOver = new WebPanel(false);
		popOver.setLayout(new BorderLayout());
		popOver.setRound(4);
		popOver.setBackground(Color.WHITE);
		ImageIcon ii = TResources.getSmallIcon(class1.getSimpleName());
		WebImage icon = ii == null ? new WebImage() : new WebImage(ii);
		WebLabel titleLabel = new WebLabel("Pop-over dialog", WebLabel.CENTER);
		WebButton closeButton = new WebButton(TResources.getSmallIcon("CloseTile"), tilePanel);
		closeButton.setName("CloseContent");
		closeButton.setUndecorated(true);
		GroupPanel gp = new GroupPanel(GroupingType.fillMiddle, 4, icon, titleLabel, closeButton);
		gp.setMargin(0, 0, 10, 0);
		popOver.add(gp, BorderLayout.NORTH);
		popOver.add(uipanel, BorderLayout.CENTER);
	}
	
	public WebButton getButton() {
		return button;
	}
	public JComponent getContent() {
		return popOver;
	}

	public void arm() {
		this.r1 = getBounds();
		this.framedim = Alesia.mainFrame.getContentPane().getSize();
	}
	
	public Animator tileOutTransition() {
		p2 = new Point(framedim.width, framedim.height);
		TimingTarget tt = PropertySetter.getTargetTo(this, "Location", p2);
		animator.clearTargets();
		animator.addTarget(tt);
		animator.start();
		return animator;
	}
	
	public Animator tileInTransition() {
		setLocation(p2);
		Point p = new Point(r1.x, r1.y);
		TimingTarget tt = PropertySetter.getTargetTo(this, "Location", p);
		animator.clearTargets();
		animator.addTarget(tt);
		animator.start();
		return animator;
	}
	
	
	public Animator tileBoundTransition() {
		r2 = new Rectangle(0,0, framedim.width, framedim.height);
		TimingTarget tt = PropertySetter.getTargetTo(this, "Bounds", r2);
		animator.clearTargets();
		animator.addTarget(tt);
		animator.start();
		return animator;
	}

	public void performInTransition() {
		setLayout(null);
		add(popOver);
		Point tp = new Point(10,10);
		TimingTarget tt = PropertySetter.getTargetTo(this, "Location", tp);
		animator.clearTargets();
		
		TimingTargetAdapter tta = new TimingTargetAdapter() {
			@Override
			public void end(Animator source) {
//				remove(button);
//				add(popOver, BorderLayout.CENTER);
			}
		};
		animator.addTarget(tta);
		animator.addTarget(tt);
		animator.start();
	}
}
