package plugins.hero;

import java.util.*;

import org.apache.commons.math3.stat.descriptive.*;

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
	private DescriptiveStatistics bettings;
	private DescriptiveStatistics potValues;
	private SensorsArray array;
	private int playerId;
	private String prefix;

	public GamePlayer(int playerId) {
		this.playerId = playerId;
		this.prefix = "villan" + playerId;
		this.name = prefix;
		this.array = Trooper.getInstance().getSensorsArray();
		this.bettings = new DescriptiveStatistics(100);
		this.potValues = new DescriptiveStatistics(100);
	}

	/**
	 * signal by {@link GameRecorder} when is time to update the information about this player. This method will updata
	 * all the available information retrived from the enviorement.
	 * <p>
	 * TODO: when this method detect the name of the villan, it will try to retrive pass information about him form the
	 * data base. propabilistic information about this villan could be retribed afeter that
	 */
	public void update() {

		// update only the active villans. if a villan fold, his last actions was already recorded
		if (!array.isVillanActive(playerId))
			return;

		// update only villans with colected information
		double chips = array.getSensor(prefix + ".chips").getNumericOCR();
		if (chips == -1)
			return;

		// update only after preflop
		if (array.getPokerSimulator().getCurrentRound() == PokerSimulator.HOLE_CARDS_DEALT)
			return;

		// at this point i need to collect the information
		// villan name
		name = array.getSensor(prefix + ".name").getOCR();
		name = name == null ? prefix : name;
		// TODO: is a know villan ??
		if (name != null) {
			// Alesia.openDB();
		}

		double pot = array.getSensor("pot").getNumericOCR();
		potValues.addValue(pot);
		bettings.addValue(chips);
	}
}