package gui.prueckl.draw;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;

import javax.swing.*;

import action.*;

public class SetImageToClipboard extends TAbstractAction {

	private DrawingEditor editor;
	public SetImageToClipboard(DrawingEditor de) {
		this.editor = de;
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		ImageIcon ii = editor.getDrawingPanel().getBackgroundImage();
		if (ii != null) {
			// BufferedImage bi = ImageUtils.getBufferedImage(ii);
			ImageTransferable transferable = new ImageTransferable(ii.getImage());
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
		}
	}

	static class ImageTransferable implements Transferable {
		private Image image;

		public ImageTransferable(Image image) {
			this.image = image;
		}

		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
			if (isDataFlavorSupported(flavor)) {
				return image;
			} else {
				throw new UnsupportedFlavorException(flavor);
			}
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor == DataFlavor.imageFlavor;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{DataFlavor.imageFlavor};
		}
	}
}
