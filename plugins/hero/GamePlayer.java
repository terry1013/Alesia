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
	private int playerId;
	private String prefix;
	public GamePlayer(int playerId) {
		this.playerId = playerId;
		this.prefix = "villan" + playerId;
		this.name = prefix;
	}

	/**
	 * signal by {@link GameRecorder} when is time to update the information about this player. This method will updata
	 * all the available information retrived from the enviorement.
	 * <p>
	 * TODO: when this method detect the name of the villan, it will try to retrive pass information about him form the
	 * data base. propabilistic information about this villan could be retribed afeter that
	 */
	public void update() {
		SensorsArray array = Trooper.getInstance().getSensorsArray();
		ScreenSensor nameSensor = array.getSensor(prefix + ".name");

		// update only the active villans. if a villan fold, his last actions was already recorded
		if (!array.isVillanActive(playerId))
			return;

		// TODO: temporal for th app. retrive the name only once when the background is black
		if (name.equals(prefix) && nameSensor.getMaxColor().equals("000000")) {
			nameSensor.capture(true);
			name = nameSensor.getOCR();
			name = name == null ? prefix : name;
		}
		if (card1.equals(unkData)) {
			String ct = array.getSensor(prefix + ".card1").getOCR();
			card1 = ct == null ? unkData : ct;
		}
		if (card2.equals(unkData)) {
			String ct = array.getSensor(prefix + ".card2").getOCR();
			card2 = ct == null ? unkData : ct;
		}

		// action perform by the by villan. at this point the villan still alive. if i can get the villan action it is
		// because the villan is not spoked yet, hero is in a early table position. So he still alive at this point he
		// must be call/check the hero last action
		// BufferedImage imagea = nameSensor.getPreparedImage();
		BufferedImage imagea = nameSensor.getCapturedImage();
		String ocr = ScreenSensor.getOCRFromImage(imagea, GameRecorder.actionsTable);
		// values. the unknow or error spetial value -1 is appended too
		int val = array.getSensor(prefix + ".call").getIntOCR();
		actions.append(ocr == null ? "c" : ocr.substring(0, 1));
		actions.append(val);
	}
	@Override
	public String toString() {
		return name + "," + card1 + "," + card2 + "," + actions;
	}
}