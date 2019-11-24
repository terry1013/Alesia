package core;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;
import org.jdesktop.application.Action;

import com.alee.utils.*;
import com.jgoodies.common.base.*;

import action.*;
import core.datasource.model.*;
import gui.*;
import gui.docking.*;

public class TActionsFactory {

	private static TActionsFactory instance;
	/**
	 * general key for notification whien the information has been loaded inside of the action.
	 */
	public static String DATA_LOADED = "dataLoaded";

	/**
	 * contant for data sended outside of the aplication
	 */
	public static String DATA_SAVED = "dataSaved";

	/**
	 * gereal key for send data inside of the action to be processed by it
	 */
	public static String INPUT_DATA = "inpuData";
	private ActionMap actionMap;

	public TActionsFactory() {
		this.actionMap = Alesia.getInstance().getContext().getActionMap(this);
		instance = this;
	}

	public static javax.swing.Action getAction(String key) {
		return instance.actionMap.get(key);
	}

	private static void disposeDialog(JComponent cmp, boolean valid) {
		cmp.putClientProperty("validation", valid);
		Window root = SwingUtilities.getWindowAncestor(cmp);
		if (root instanceof JDialog) {
			((JDialog) root).dispose();
		}
	}

	/**
	 * retrive the instance of {@link ApplicationAction} inside of the {@link ActionEvent} pass as argument. this method
	 * throw and exeption if the source of the event is not an {@link AbstractButton}
	 * 
	 * @param event - event
	 * @return {@link ApplicationAction}
	 * @since 2.3
	 */
	private static ApplicationAction getMe(ActionEvent event) {
		Object obj = event.getSource();
		Preconditions.checkArgument(obj instanceof AbstractButton,
				"The source of the ActionEvent is not an instance of abstractButton");
		AbstractButton src = (AbstractButton) obj;
		return (ApplicationAction) src.getAction();

	}

	@Action
	public void about(ActionEvent event) {
		DockingContainer dc = SwingUtils.getFirstParent((JComponent) event.getSource(), DockingContainer.class);
		dc.setContentPanel(new AboutPanel());
	}
	@Action
	public void fileChooserOpen(ActionEvent event) {
		ApplicationAction me = getMe(event);
		FileDialog dialog = new FileDialog(Alesia.getMainFrame(), null, FileDialog.LOAD);
		dialog.setMultipleMode(false);
		dialog.setVisible(true);
		String sf = dialog.getFile();
		String sd = dialog.getDirectory();
		if (sf != null || sf != null) {
			File nf = new File(sd + sf);
			me.putValue(DATA_LOADED, nf);
		}
	}
	@Action
	public void fileChooserSave(ActionEvent event) {
		ApplicationAction me = getMe(event);
		FileDialog dialog = new FileDialog(Alesia.getMainFrame(), null, FileDialog.SAVE);
		dialog.setMultipleMode(false);
		dialog.setVisible(true);
		String sf = dialog.getFile();
		String sd = dialog.getDirectory();
		if (sf != null || sf != null) {
			File nf = new File(sd + sf);
			me.putValue(DATA_SAVED, nf);
		}
	}
	
	/**
	 * standar comit action. this action:
	 * <ul>
	 * <li>if the containr of the action is an instance of {@link TUIFormPanel} this acction perform
	 * {@link TUIFormPanel#validateFields()} and perform {@link JDialog#dispose()} if the validation is ok
	 * 
	 * </ul>
	 * 
	 * @param event
	 */
	@Action
	public void acept(ActionEvent event) {
		Object obj = event.getSource();
		if (obj instanceof JComponent) {
			JComponent cnt = SwingUtils.getFirstParent((JComponent) obj, TUIFormPanel.class);
			if (cnt != null) {
				boolean val = ((TUIFormPanel) cnt).validateFields();
				if (val) {
					disposeDialog(cnt, true);
				}
			}
		}
	}

	@Action
	public void cancel(ActionEvent event) {
		Object obj = event.getSource();
		if (obj instanceof JComponent) {

		}
		disposeDialog((JComponent) obj, false);
	}
	/**
	 * retrive an image store in the system clipboard. This action store the image retrived from the system clipboard in
	 * {@link #DATA_LOADED} value for this action
	 * 
	 * @param event
	 * @since 2.3
	 */
	@Action
	public void getImageFromClipBoard(ActionEvent event) {
		ApplicationAction me = getMe(event);
		Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
		if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
			try {
				BufferedImage img = (BufferedImage) transferable.getTransferData(DataFlavor.imageFlavor);
				me.putValue(DATA_LOADED, img);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * generic constant to store information about grouping or group
	 */
	public static String GROUP = "group";
	/**
	 * Allow use the internal file tvariables to load custom user variables. this action work in pair with
	 * {@link #saveProperty(ActionEvent)}. This action use the following values retrive from
	 * {@link javax.swing.Action#getValue(String)} method:
	 * <ul>
	 * <li>grouping - indicate the grouping of the variable to store
	 * </ul>
	 * this action display a list of available values. after user selection, the value is store as
	 * <code>loadPerformed</code> value {@link javax.swing.Action#putValue(String, Object)}
	 * 
	 * @param event - {@link ActionEvent}
	 * @since 2.3
	 * @see #saveProperty(ActionEvent)
	 */
	@Action
	public void loadVariable(ActionEvent event) {
		AbstractButton src = (AbstractButton) event.getSource();
		ApplicationAction me = (ApplicationAction) src.getAction();
		String group = (String) me.getValue(GROUP);
		LazyList<Model> list = LocalProperty.where("grouping = ?", group);
		ArrayList<TEntry> te = TStringUtils.getTEntryGroupFrom(list, "ID", "NAME");

		LocalProperty lsel = getLastSelected(group);
		TEntry tels = lsel == null ? null : new TEntry(lsel.getId(), lsel.get("name"));
		TEntry sel = (TEntry) JOptionPane.showInputDialog(Alesia.getMainFrame(), "Select the elements to load", "Load",
				JOptionPane.PLAIN_MESSAGE, null, te.toArray(), tels);

		// save the last selected and store the value from db in the loadPerformed property
		if (sel != null) {
			// save last selected spetial row
			LocalProperty tmp = LocalProperty.findOrCreateIt("grouping", group + ".LastSelected");
			tmp.set("name", sel.getValue());
			tmp.save();

			// find the variable
			lsel = LocalProperty.findById(sel.getKey());
			Object obj = TPreferences.getObjectFromByteArray(lsel.getBytes("value"));
			me.putValue(DATA_LOADED, obj);
		}
	}
	/**
	 * Allow use the internal file TVARIABLE to store custom user variables. this action work in pair with
	 * {@link #loadProperty(ActionEvent)}. This action use the following values retrive from
	 * {@link javax.swing.Action#getValue(String)} method:
	 * <ul>
	 * <li>{@link #GROUP} - indicate the grouping of the variable to store
	 * <li>{@link #INPUT_DATA} - indicate the data to store in
	 * </ul>
	 * the variable name is request as input
	 * 
	 * @param event
	 * @since 2.3
	 * @see #loadVariable(ActionEvent)
	 * @see #getLastSelected(String)
	 */
	@Action
	public void saveVariable(ActionEvent event) {
		AbstractButton src = (AbstractButton) event.getSource();
		ApplicationAction me = (ApplicationAction) src.getAction();
		String group = (String) me.getValue(GROUP);
		Object value = me.getValue(INPUT_DATA);

		// last selected
		LocalProperty lss = getLastSelected(group);
		String ls = lss == null ? null : lss.getString("name");

		String savn = (String) JOptionPane.showInputDialog(Alesia.getMainFrame(), "Write the name", "Save",
				JOptionPane.PLAIN_MESSAGE, null, null, ls);

		// save & update last selected
		if (savn != null) {
			LocalProperty tmp = LocalProperty.findOrCreateIt("grouping = ? AND nmae = ?", group, savn);
			tmp.set("value", value);
			tmp.save();

			tmp = LocalProperty.findOrCreateIt("grouping = ? ", group + ".LastSelected");
			tmp.set("name", savn);
			tmp.save();
			me.putValue(DATA_SAVED, value);
		}
	}

	/**
	 * send data of type {@link ImageIcon} to the sytem clipboard. This method retrive the image to send using the value
	 * stored in {@link #INPUT_DATA} property. When the operation is succeded, this action set the property
	 * {@link #DATA_SAVED} whit the same image recived.
	 * 
	 * @param event
	 * @since 2.3
	 */
	@Action
	public void sendImageToClipboard(ActionEvent event) {
		ApplicationAction me = getMe(event);
		ImageIcon ii = (ImageIcon) me.getValue(INPUT_DATA);
		if (ii != null) {
			// BufferedImage bi = ImageUtils.getBufferedImage(ii);
			ImageTransferable transferable = new ImageTransferable(ii.getImage());
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
			me.putValue(DATA_SAVED, ii);
		}
	}

	/**
	 * return the last load or saved variable selected using the {@link LoadProperty} and {@link SaveProperty} actiones.
	 * The last load or saved variables are stored in an special row in the variables file. this element contain the
	 * name (variable name) of the las edited element. the row do not store the variable value. this only point to
	 * another element in the same group with the variable name
	 * 
	 * @param grouping - the variable group
	 * @return instace of {@link LocalProperty} that was load/saved for last time or <code>null</code> if the method
	 *         can.t find anything
	 * @since 2.3
	 * @see #loadVariable(ActionEvent)
	 * @see #saveVariable(ActionEvent)
	 * 
	 */
	private LocalProperty getLastSelected(String grouping) {
		LocalProperty tmp = LocalProperty.findOrCreateIt("grouping", grouping + ".LastSelected");
		LocalProperty lsel = null;
		String name = tmp.getString("name");
		LazyList<LocalProperty> lst = LocalProperty.find("grouping = ?  AND name = ?", grouping, name);
		lsel = lst.isEmpty() ? null : lst.get(0);
		return lsel;
	}
}
