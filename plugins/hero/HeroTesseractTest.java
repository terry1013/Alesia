package plugins.hero;

import java.io.*;

import net.sourceforge.tess4j.*;

public class HeroTesseractTest {

	public static void main(String[] args) {
		File imageFile = new File("plugin/hero/test.png");
		ITesseract instance = new Tesseract(); // JNA Interface Mapping
		// ITesseract instance = new Tesseract1(); // JNA Direct Mapping
		instance.setDatapath("plugin/hero/tessdata"); // path to tessdata directory

		try {
			String result = instance.doOCR(imageFile);
			System.out.println(result);
		} catch (TesseractException e) {
			System.err.println(e.getMessage());
		}
	}
}