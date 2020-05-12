package plugins.hero;

import java.util.*;

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
	private DescriptiveStatistics bettingPattern;
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
		this.bettingPattern = new DescriptiveStatistics(100);
	}
	/**
	 * signal by {@link GameRecorder} when is time to update the information about this player. This method will updata
	 * all the available information retrived from {@link PokerSimulator}
	 * <p>
	 * When this method detect a know villan, it will try to retrive pass information about him form the data base.
	 * propabilistic information about this villan could be retribed afeter that
	 */
	public void update() {
		// record only if the player is active
		if (!array.isActive(playerId))
			return;

		List<ScreenSensor> list = new ArrayList<>();
		if (playerId == 0) {
			list.add(array.getSensor("hero.chips"));
		} else {
			list = array.getSensors("villan" + playerId);
		}
		array.readSensors(true, list);

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
			}
		}

		// store the curren street. starting hans can be calculated just retrivin the values > 0;
		// startingHands.addValue(array.getPokerSimulator().getCurrentRound());
		// startingHands.addValue(round);
		// negative for betting, positive for winnigs (don.t record 0 value because affect statistical values)
		if (chips - prevValue != 0)
			bettingPattern.addValue(chips - prevValue);
		prevValue = chips;
	}

	public double getMean() {
		double mean = bettingPattern.getMean();
		mean = ((int) (mean * 100)) / 100.0;
		return mean;
	}
	public long getN() {
		return bettingPattern.getN();
	}

	public double getVariance() {
		double var = bettingPattern.getStandardDeviation();
		var = ((int) (var * 100)) / 100.0;
		return var;
	}

	@Override
	public String toString() {
		return getMean() + " (" + getN() + ")";
	}

	public void updateDB() {
		if (!name.equals(prefix)) {
			GamesHistory gh = GamesHistory.findOrInit("time", Hero.startDate, "tableparams",
					array.getPokerSimulator().getTableParameters(), "name", name);
			gh.set("ASSESMENT", toString());
			gh.set("BEATTIN_PATTERN", TPreferences.getByteArrayFromObject(bettingPattern));
			gh.save();
		}
	}
}