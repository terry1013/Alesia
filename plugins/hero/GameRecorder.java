package plugins.hero;

import java.awt.image.*;
import java.util.*;
import java.util.stream.*;

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
	public static Hashtable<String, BufferedImage> actionsTable = ScreenSensor.loadImages(IMAGE_ACTIONS);

	private int currentRound = PokerSimulator.NO_CARDS_DEALT;
	private SensorsArray sensorsArray;
	private int tablePosition;
	private String comunityCards;
	private Vector<GamePlayer> villans;
	private GamePlayer trooper;

	public GameRecorder(SensorsArray sensorsArray) {
		this.sensorsArray = sensorsArray;
		this.trooper = new GamePlayer(0);
		trooper.name = "trooper";
	}

	/**
	 * complete the recording operation and store the result in DB. This method also determine the winnigs.
	 * <p>
	 * Call this method before a new enviorement or clean eviorement invocation.
	 */
	public void flush() {
		takeSnapShot(null);
		String row = tablePosition + "|<sm value>|" + comunityCards + "|";
		row += trooper.toString() + "|";
		row += villans.stream().map(GamePlayer::toString).collect(Collectors.joining("|"));
		Hero.logger.severe(row);
	}

	/**
	 * Take a snapshot of the game status. at this point all elements are available for be processed by this method
	 * because most of the screensensor are up to date. This method is invoked one moment before the trooper perform the
	 * action. if action = <code>null</code> indicate the last recording action previous to {@link #flush()} operation.
	 * 
	 * @param action - the trooper desition before be performed by {@link RobotActuator}
	 */
	public void takeSnapShot(String action) {

		// at firt exec, init the villans list with active seats. if a villan run before is my turn to
		// fight, his action still on the screen at this point
		if (villans == null) {
			int lv = sensorsArray.getVillans();
			this.villans = new Vector<>(lv);
			for (int i = 1; i <= lv; i++) {
				if (sensorsArray.isActiveSeats(i)) {
					villans.add(new GamePlayer(i));
				}
			}
		}
		// header info
		comunityCards = sensorsArray.getPokerSimulator().getMyHandHelper().getCommunityCards().toString();
		tablePosition = sensorsArray.getPokerSimulator().getTablePosition();

		// if the game round change, add a marker inside the actions for eachplayer
		int cr = sensorsArray.getPokerSimulator().getCurrentRound();
		if (cr > currentRound) {
			currentRound = cr;
			trooper.actions.append(">");
			villans.stream().forEach(gp1 -> gp1.actions.append(">"));
		}

		// trooper
		trooper.card1 = sensorsArray.readAndGetOCR("hero.card1");
		trooper.card2 = sensorsArray.readAndGetOCR("hero.card2");
		if (action != null) {
			trooper.actions.append(action.substring(0, 1));
		}

		// villans info
		villans.stream().forEach(v -> v.update());
	}
}
