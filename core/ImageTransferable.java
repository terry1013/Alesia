package core;

import java.awt.*;
import java.awt.datatransfer.*;

public class ImageTransferable implements Transferable {
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