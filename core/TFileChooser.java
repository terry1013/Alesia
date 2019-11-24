package core;

import javax.swing.*;
import javax.swing.plaf.*;

public class TFileChooser extends JFileChooser{

	public FileChooserUI myui;
	@Override
	public FileChooserUI getUI() {
		return myui;
	}
}
