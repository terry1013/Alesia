package plugins.hero;

import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;

import com.alee.utils.*;
import com.alee.utils.filefilter.*;

import core.*;

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
 * 
 * @author terry
 *
 */
public class GameRecorder implements Runnable {

	public static String IMAGE_ACTIONS = "plugins/hero/image_actions/";

	private SensorsArray sensorsArray;
	private int tablePosition;
	private String comunityCards;
	private Vector<GamePlayer> gamePlayers;
	private GamePlayer trooper;

	public GameRecorder(SensorsArray sensorsArray) {
		this.sensorsArray = sensorsArray;
		int lv = sensorsArray.getVillans();
		this.gamePlayers = new Vector<>(lv);
		this.trooper = new GamePlayer();
		trooper.name = "trooper";
	}

	@Override
	public void run() {

		// at first execution, init the players list
		for (int i = 0; i < gamePlayers.size(); i++) {
			GamePlayer p = new GamePlayer();
			p.name = "villan" + (i + 1);
			gamePlayers.add(p);
		}

		// trooper info
		trooper.cards = sensorsArray.getPokerSimulator().getMyHandHelper().getHoleCards().toString();

		comunityCards = sensorsArray.getPokerSimulator().getMyHandHelper().getCommunityCards().toString();
		tablePosition = sensorsArray.getPokerSimulator().getTablePosition();

		tablePosition = sensorsArray.getPokerSimulator().getTablePosition();

		// villans info
		for (int i = 0; i < gamePlayers.size(); i++) {
			String tmp1 = sensorsArray.readAndGetOCR("villan" + i + ".card1");
			String tmp2 = sensorsArray.readAndGetOCR("villan" + i + ".card2");
			if (tmp1 == null || tmp2 == null) {
				gamePlayers.elementAt(i).cards = (tmp1 + tmp2);
			}
			// TODO: evaluate the posibility of separate the text ocr areas form the numeric ocr ares to improve the
			// reability of the ocr operation
			int val = sensorsArray.getScreenSensor("villan" + i + ".call").getIntOCR();

		}
	}

	private String getAction(ScreenSensor screenSensor) {
		BufferedImage imagea = screenSensor.getCapturedImage();

		String act = null;
		double dif = 100.0;
		File dir = new File(IMAGE_ACTIONS);

		// String[] imgs = dir.list((dir1, name) -> name.contains("action."));
		String[] imgs = dir.list();

		for (String img : imgs) {
			File f = new File(ScreenSensor.IMAGE_CARDS + img);
			BufferedImage imageb = ImageIO.read(f);
			double s = ScreenSensor.getImageDiferences(imagea, imageb, 100);
			if (s < dif) {
				dif = s;
				act = f.getName().split("[.]")[0];
			}
		}
		return act;
	}
}
