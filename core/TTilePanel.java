package core;

import gui.*;
import gui.datasource.*;

import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.*;

import javax.swing.*;
import javax.swing.border.*;

import org.jdesktop.core.animation.timing.*;

import com.alee.laf.button.*;

public class TTilePanel extends JPanel implements ActionListener {

	private Animator animator;
	private JPanel buttonPanel, contentPanel;

	public TTilePanel() {
		super(new BorderLayout());
		setBorder(new EmptyBorder(5, 5, 5, 5));
		buttonPanel = getButtonPanel();
		contentPanel = new JPanel();
		add(buttonPanel, BorderLayout.CENTER);
		animator = new AnimatorBuilder().setDuration(250, TimeUnit.MILLISECONDS).build();
	}
	
	private JPanel getButtonPanel() {
		JPanel jp = new JPanel(new GridLayout(2, 2, 10, 10));
		jp.add(new TTile(SystemVars.class, this));
		jp.add(new TTile(ConnectionProfile.class, this));
		jp.add(new TTile(TDriverDialog.class, this));
		return jp;
	}

	TTile activeTile = null;

	@Override
	public void actionPerformed(ActionEvent e) {
		WebButton wb = (WebButton) e.getSource();
		if (wb.getName().equals("CloseContent")) {
			closeContent();
		} else {
			showContent(wb);
		}
	}
	
	private void closeContent() {
		
	}
	
	private void showContent(WebButton swb) {
		Animator lasta = null;
		Component[] cmps = buttonPanel.getComponents();
		for (Component cmp : cmps) {
			TTile tt = (TTile) cmp;
			tt.arm();
			if (tt.getButton() != swb) {
				lasta = tt.tileOutTransition();
			} else {
				activeTile = tt;
				tt.setBackground(Color.pink);
			}
		}
		TimingTargetAdapter tta = new TimingTargetAdapter() {
			@Override
			public void end(Animator source) {
				TimingTargetAdapter tta = new TimingTargetAdapter() {
					@Override
					public void end(Animator source) {
						setVisible(false);
						remove(buttonPanel);
						JComponent jc = activeTile.getContent();
//						Dimension d = PlanC.frame.getContentPane().getSize();
//						jc.setSize(d);
						add(jc, BorderLayout.CENTER);
						setVisible(true);
					}
					
				};
				Animator a = activeTile.tileBoundTransition();
				a.addTarget(tta);
			}
		};
		lasta.addTarget(tta);
	}
}
