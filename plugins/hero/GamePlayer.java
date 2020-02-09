package plugins.hero;

import org.apache.commons.math3.stat.descriptive.*;

import core.*;
import core.datasource.model.*;

/**
 * encapsulate all player information. this class collect the necesary information to make a wild guess over the
 * villans.
 * <p>
 * A beginner villan must play around 1-2 hours dayly. a profesional poker player around 2-8 . this class only take the
 * last 100 hands (must be around 1 1/2 hours)
 * 
 * TODO. 100 is an estimated to avoid pass behaviors interfir whit the new villans moods and skills. chick this value. a
 * better aproach cound be keep track of long data and remove the old ones by date.
 * 
 * @author terry
 *
 */
public class GamePlayer {
	private String name;
	private String oldName = "";
	private DescriptiveStatistics bettingPattern, startingHands, potValues, trooperProbabilities;
	private SensorsArray array;
	private int playerId;
	private String prefix;
	private boolean newRound;

	public GamePlayer(int playerId) {
		this.playerId = playerId;
		this.prefix = "villan" + playerId;
		this.name = prefix;
		this.array = Trooper.getInstance().getSensorsArray();
		initStatistics();
	}
	private void initStatistics() {
		this.bettingPattern = new DescriptiveStatistics(100);
		this.startingHands = new DescriptiveStatistics(100);
		this.potValues = new DescriptiveStatistics(100);
		this.trooperProbabilities = new DescriptiveStatistics(100);

	}
	/**
	 * signal by {@link GameRecorder} when is time to update the information about this player. This method will updata
	 * all the available information retrived from {@link PokerSimulator}
	 * <p>
	 * When this method detect a know villan, it will try to retrive pass information about him form the data base.
	 * propabilistic information about this villan could be retribed afeter that
	 */
	public void update() {
		// update only after preflop
		if (array.getPokerSimulator().getCurrentRound() == PokerSimulator.HOLE_CARDS_DEALT) {
			newRound = true;
			return;
		}

		// villans chips value meaning:
		// chips = -1 (posible error but the villan is active. collect the info to keep tracking
		// chips = 0 means the villan fold his card (is not active)

		// hero o villan
		double chips = 0.0;
		if (playerId == 0) {
			chips = array.getPokerSimulator().getHeroChips();
		} else {
			chips = array.getSensor(prefix + ".chips").getNumericOCR();
			if (!array.isVillanActive(playerId) && chips > 0)
				chips = 0;
		}

		// villan name
		// spetial treatment for troper
		if (playerId == 0)
			name = "Hero";
		else
			name = array.getSensor(prefix + ".name").getOCR();

		name = name == null ? prefix : name;
		if (!(name.equals(prefix) || name.equals(oldName))) {
			oldName = name;
			GamesHistory gh = GamesHistory.findFirst("NAME = ?", name);
			if (gh == null) {
				initStatistics();
			} else {
				bettingPattern = (DescriptiveStatistics) TPreferences
						.getObjectFromByteArray(gh.getBytes("BEATTIN_PATTERN"));
				startingHands = (DescriptiveStatistics) TPreferences
						.getObjectFromByteArray(gh.getBytes("STARTING_HANDS"));
				potValues = (DescriptiveStatistics) TPreferences.getObjectFromByteArray(gh.getBytes("POT_VALUES"));
				trooperProbabilities = (DescriptiveStatistics) TPreferences
						.getObjectFromByteArray(gh.getBytes("TROOPER_PROBS"));
			}
		}

		potValues.addValue(array.getPokerSimulator().getPotValue());
		startingHands.addValue(newRound ? 1.0 : 0.0);
		bettingPattern.addValue(chips);
		trooperProbabilities.addValue(array.getPokerSimulator().getBestProbability());
		newRound = false;
	}
	public void assest() {
		// TODO
	}
	public void updateDB() {
		if (!name.equals(prefix)) {
			GamesHistory gh = GamesHistory.findOrCreateIt("NAME", name);
			gh.set("BEATTIN_PATTERN", TPreferences.getByteArrayFromObject(bettingPattern));
			gh.set("STARTING_HANDS", TPreferences.getByteArrayFromObject(startingHands));
			gh.set("POT_VALUES", TPreferences.getByteArrayFromObject(potValues));
			gh.set("TROOPER_PROBS", TPreferences.getByteArrayFromObject(trooperProbabilities));
			gh.save();
		}
	}
}