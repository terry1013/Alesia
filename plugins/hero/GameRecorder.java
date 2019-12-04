package plugins.hero;

import java.awt.image.*;
import java.util.*;

/**
 * This class record the game secuence and store the result in the games db file. instance of this class are dispached
 * when is the trooper turns to fight. all sensor information are stored inside of this class and this class silent
 * perform the record operation retrivin all necesari information.
 * <p>
 * the information retrived for this game recorded will be available at the next game session
 * <p>
 * TODO: maybe exted the funcionality to supply imediate info. a villans in this game sesion may alter his behabior and
 * the curren hero status is not aware of this.
 * 
 * TODO: evaluate the posibility of separate the text ocr areas form the numeric ocr ares to improve the reability of
 * the ocr operation
 * 
 * 
 * @author terry
 *
 */
public class GameRecorder {

	public static String IMAGE_ACTIONS = "plugins/hero/image_actions/";
	private static Hashtable<String, BufferedImage> actionsTable = ScreenSensor.loadImages(IMAGE_ACTIONS);

	private SensorsArray sensorsArray;
	private int tablePosition;
	private String comunityCards;
	private Vector<GamePlayer> gamePlayers;
	private GamePlayer trooper;

	public GameRecorder(SensorsArray sensorsArray) {
		this.sensorsArray = sensorsArray;
		this.trooper = new GamePlayer();
		trooper.name = "trooper";
		int lv = sensorsArray.getVillans();
		this.gamePlayers = new Vector<>(lv);
		for (int i = 0; i < lv; i++) {
			GamePlayer p = new GamePlayer();
			p.name = "villan" + (i + 1);
			gamePlayers.add(p);
		}
	}

	/**
	 * complete the recording operation and store the result in DB. This method also determine the winnigs.
	 * <p>
	 * Call this method before a new enviorement or clean eviorement invocation.
	 */
	public void flush() {

	}

	/**
	 * Take a snapshot of the game status. at this point all elements are available for be processed by this method
	 * because most of the screensensor are up to date. This method is invoked one moment before the trooper perform the
	 * action.
	 * 
	 * @param action - the trooper desition before be performed by {@link RobotActuator}
	 */
	public void takeSnapShot(String action) {

		// trooper info
		trooper.cards = sensorsArray.getPokerSimulator().getMyHandHelper().getHoleCards().toString();
		trooper.actions.add(action);
		comunityCards = sensorsArray.getPokerSimulator().getMyHandHelper().getCommunityCards().toString();
		tablePosition = sensorsArray.getPokerSimulator().getTablePosition();

		// villans info
		for (int i = 1; i <= gamePlayers.size(); i++) {
			GamePlayer gp = gamePlayers.elementAt(i-1);
			String tmp1 = sensorsArray.readAndGetOCR("villan" + i + ".card1");
			String tmp2 = sensorsArray.readAndGetOCR("villan" + i + ".card2");
			if (tmp1 == null || tmp2 == null) {
				gp.cards = (tmp1 + tmp2);
			}

			ScreenSensor ss = sensorsArray.getScreenSensor("villan" + i + ".name");
			BufferedImage imagea = ss.getCapturedImage();
			String ocr = ScreenSensor.getOCRFromImage(imagea, actionsTable);
			gp.actions.add(ocr == null ? "" : ocr);

			int val = sensorsArray.getScreenSensor("villan" + i + ".call").getIntOCR();
			if (val > -1) {
				gp.val.add(val);
			}
		}
	}
}
