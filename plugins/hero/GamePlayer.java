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
	private DescriptiveStatistics bettingPattern, startingHands;
	private SensorsArray array;
	private int playerId;
	private String prefix;
	private double prevValue;

	public GamePlayer(int playerId) {
		this.playerId = playerId;
		this.prefix = "villan" + playerId;
		this.name = prefix;
		this.prevValue = -1;
		this.array = Trooper.getInstance().getSensorsArray();
		initStatistics();
	}
	
	public int getId() {
		return playerId;
	}
	public String getName() {
		return name;
	}
	private void initStatistics() {
		this.bettingPattern = new DescriptiveStatistics(300);
		this.startingHands = new DescriptiveStatistics(300);
	}
	/**
	 * signal by {@link GameRecorder} when is time to update the information about this player. This method will updata
	 * all the available information retrived from {@link PokerSimulator}
	 * <p>
	 * When this method detect a know villan, it will try to retrive pass information about him form the data base.
	 * propabilistic information about this villan could be retribed afeter that
	 */
	public void update(int round) {

		// record only if the player is active
		if (!array.isActive(playerId))
			return;

		// amunitions
		double chips = 0.0;
		if (playerId == 0) {
			chips = array.getPokerSimulator().getHeroChips();
		} else {
			chips = array.getSensor("villan" + playerId + ".chips").getNumericOCR();
		}

		// chips = -1 because this seat is inactive or the player fold
		if (chips == -1)
			return;

		// at the beginnin of the record process, i just set the initial values. after that, i start the record process.
		if (prevValue == -1) {
			prevValue = chips;
			return;
		}

		// at this point, all is set to start the record process
		// name
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
			}
		}

		// store the curren street. starting hans can be calculated just retrivin the values > 0;
//		startingHands.addValue(array.getPokerSimulator().getCurrentRound());
		startingHands.addValue(round);
		// negative for betting, positive for winnigs 0 for checks (or split pot)
		bettingPattern.addValue(chips - prevValue);
		prevValue = chips;
	}

	public double getMean() {
		double mean = bettingPattern.getMean();
		mean = ((int) (mean * 100)) / 100.0;
		return mean;
	}
	
	public double getVariance() {
		double var = bettingPattern.getStandardDeviation();
		var = ((int) (var * 100)) / 100.0;
		return var;
	}

	public int getHands() {
		int hands = 0;
		for (int i = 1; i < startingHands.getN(); i++) {
			if (startingHands.getElement(i) == PokerSimulator.FLOP_CARDS_DEALT
					&& startingHands.getElement(i - 1) == PokerSimulator.HOLE_CARDS_DEALT) {
				hands++;
			}
		}
		return hands;
	}

	@Override
	public String toString() {
		return getMean() + "/";// + getVariance();
	}

	public void updateDB() {
		if (!name.equals(prefix)) {
			GamesHistory gh = GamesHistory.findOrCreateIt("NAME", name);
			gh.set("BUY_IN", array.getPokerSimulator().getBuyIn());
			gh.set("SMALL_BLID", array.getPokerSimulator().getSmallBlind());
			gh.set("BIG_BLID", array.getPokerSimulator().getBigBlind());
			gh.set("ASSESMENT", toString());
			gh.set("BEATTIN_PATTERN", TPreferences.getByteArrayFromObject(bettingPattern));
			gh.set("STARTING_HANDS", TPreferences.getByteArrayFromObject(startingHands));
			gh.save();
		}
	}
}