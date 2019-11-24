package gui.wlaf;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;

import com.alee.extended.filechooser.*;
import com.alee.laf.button.*;

import core.*;

/**
 * modify the standar {@link WebFileChooserField} Override the chooser button to display native file dialog
 * 
 * @author terry
 * 
 */
public class TWebFileChooserField extends WebFileChooserField implements FilesSelectionListener {

	private String file;
	private FileDialog fileDialog;
	private WebButton chooseButton;
	private String suffix = null;

	public TWebFileChooserField(String f) {
		super();
		this.file = f;
		this.fileDialog = new FileDialog(Alesia.mainFrame, "Save", FileDialog.SAVE);
		fileDialog.setLocationRelativeTo(Alesia.mainFrame);
		addSelectedFilesListener(this);
		// override button
		chooseButton = getChooseButton();
		chooseButton.removeActionListener(chooseButton.getActionListeners()[0]);
		chooseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				fileDialog.setVisible(true);
				String f = fileDialog.getFile();
				String d = fileDialog.getDirectory();
				File fi = null;
				if (f != null && d != null) {
					file = d + f;
					// try ensure suffix ONLY if suffix was not explicitly writted by user
					if (!f.contains(".") && suffix != null) {
						file = d + f + suffix;
					}
					fi = new File(file);
				}
				setSelectedFile(fi);

				// Requesting focus back to this component after file chooser close
				chooseButton.requestFocusInWindow();
			}
		});
		// no file selected is saved as *none by AbstractDataInput
		if (file != null && !(file.equals("*none") || file.equals(""))) {
			setSelectedFile(new File(file));
		}
	}

	/**
	 * return the selected file or <code>null</code> if no file was selected
	 * 
	 * @return file selected
	 */
	public String getSelectedFile() {
		return file;
	}

	/**
	 * the the suffix for the file name (extension). The value is used during user file selection to try guaratee the
	 * correct file name. If there are a previous selected file, this method does nothing unless the user select a new
	 * file.
	 * 
	 * @param fs - the suffix ('.' char included)
	 */

	public void setSuffix(String fs) {
		this.suffix = fs;
	}

	@Override
	public void selectionChanged(List<File> files) {
		file = files.isEmpty() ? null : files.get(0).toString();
	}
}
