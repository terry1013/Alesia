package gui.wlaf;

import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;



import com.alee.extended.image.*;
import com.alee.utils.*;

import core.*;

/**
 * extend {@link WebImageDrop} to support set and get {@link ImageIcon} and {@link MouseListener} to clear component
 * 
 * @author terry
 * 
 */
public class TWebImageDrop extends WebImageDrop {
	private Vector<ImageIcon> avatars;
	private int next;
	/**
	 * new instance
	 * 
	 * @param w - component with
	 * @param h - component heght
	 * @param ii - ImageIcon
	 */
	public TWebImageDrop(int w, int h, ImageIcon ii) {
		super(w, h, null);

		// load avatars
		this.avatars = new Vector<ImageIcon>();
		for (int i = 1; i < 17; i++) {
			avatars.add(TResources.getIcon("avatar (" + i + ")"));
		}
		// set avatar or icon in field
		if (ii == null) {
			setImageIcon(avatars.elementAt((int) (Math.random() * avatars.size() - 1)));
		} else {
			setImageIcon(ii);
		}
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int mod = e.getModifiers();
				// clear image 2 clic button 1
				if (e.getClickCount() == 2 && mod == InputEvent.BUTTON1_MASK) {
					((TWebImageDrop) e.getComponent()).setImage(null);
				}
				// chage avatar 2 clic button 1
				if (e.getClickCount() == 2 && mod == InputEvent.META_MASK) {
					next = (next == avatars.size() - 1) ? 0 : next + 1;
					((TWebImageDrop) e.getComponent()).setImageIcon(avatars.elementAt(next));
				}
			}
		});
		setToolTipText("<html>Drag component. Drag a image to load into. <p>Double Clic on mouse <b>Button 1</b> "
				+ "to clear actual image. <p>Double Clic on mouse <b>Button 2</b> set any of pre installed avatars.</html>");
	}
	/**
	 * return actual display image as byte[]
	 * 
	 * @return byte[]
	 */
	public byte[] getImageIcon() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		BufferedImage bi = getThumbnail();
		try {
			if (bi != null) {
				ImageIO.write(bi, "jpg", bos);
			}
		} catch (Exception e) {
			SystemLog.logException1(e);
		}
		return bi == null ? new byte[0] : bos.toByteArray();
	}

	/**
	 * set the image
	 * 
	 * @param ii ImageIcon
	 */
	public void setImageIcon(ImageIcon ii) {
		setImage(ImageUtils.getBufferedImage(ii));
	}
}
