package plugins.hero;

import java.awt.image.*;
import java.util.*;

/**
 * encapsulate all player information.
 * <p>
 * TODO:this class is an extension of {@link SensorsArray} active instance and constantly using
 * {@link Collection#parallelStream()} TODO: check if i really can do that !!
 * 
 * @author terry
 *
 */
public class GamePlayer {
	public static final String unkData = "?";
	public String name = "";
	public String card1 = unkData;
	public String card2 = unkData;
	public StringBuffer actions = new StringBuffer();
	public StringBuffer values = new StringBuffer();

	private String prefix;
	public GamePlayer(int playerId) {
		this.prefix = "villan" + playerId;
		this.name = prefix;
	}

	/**
	 * signal by ??? when is time to update the information about this player. This method will updata all the available
	 * information retrived from the enviorement.
	 * <p>
	 * when this method detect the name of the villan, it will try to retrive pass information about him form the data
	 * base. propabilistic information about this villan could be retribed afeter that
	 */
	public void update() {
		SensorsArray array = Trooper.getInstance().getSensorsArray();
		ScreenSensor nameSensor = array.getScreenSensor(prefix + ".name");

		// TODO: temporal for th app. retrive the name only once when the background is black

		if (name.equals(prefix) && nameSensor.getMaxColor().equals("000000")) {
			nameSensor.capture(true);
			name = nameSensor.getOCR();
			name = name == null ? prefix : name;
		}
		if (card1.equals(unkData)) {
			String ct = array.readAndGetOCR(prefix + ".card1");
			card1 = ct == null ? unkData : ct;
		}
		if (card2.equals(unkData)) {
			String ct = array.readAndGetOCR(prefix + ".card2");
			card2 = ct == null ? unkData : ct;
		}

		// action perform by the by villan
		BufferedImage imagea = nameSensor.getCapturedImage();
		String ocr = ScreenSensor.getOCRFromImage(imagea, GameRecorder.actionsTable);
		actions.append(ocr == null ? unkData : ocr.substring(0, 1));
		// values. the unknow or error spetial value -1 is appended too
		int val = array.getScreenSensor(prefix + ".call").getIntOCR();
		values.append(val + ",");
	}
	@Override
	public String toString() {
		// String rval = values.length() > 0 ? values.substring(0, values.length() - 1) : "-1";
//		return name + "," + card1 + card2 + "," + actions + "," + rval;
		return name + "," + card1 + ","+card2 +","+ actions;
	}
}