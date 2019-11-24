package gui.prueckl.draw;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.*;

import com.alee.utils.*;

import action.*;

public class TrigerPoint extends TAbstractAction implements MouseListener{

	private DrawingEditor editor;
	public TrigerPoint(DrawingEditor de) {
		this.editor = de;
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		editor.getDrawingPanel().addMouseListener(this);
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		System.out.println("TrigerPoint.mouseClicked()");
	}
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
