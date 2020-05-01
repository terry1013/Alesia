package plugins.hero;

import java.util.*;

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
 * @author terry
 *
 */
public class GameRecorder {

	private Vector<GamePlayer> villans;

	public GameRecorder(int vills) {
		// 0 index is the troper
		this.villans = new Vector<>(vills);
		for (int i = 0; i <= vills; i++) {
			villans.add(new GamePlayer(i));
		}
	}

	public ArrayList<String> getMeans() {
		ArrayList<String> means = new ArrayList<>(villans.size());
		villans.forEach(gp -> means.add(gp.toString()));
		return means;
	}
	public String getAssest(int playerId) {
		return villans.elementAt(playerId).toString();
	}

	/**
	 * Store the result in DB. This method is called afther the troper perform the action. at this moment, is enough
	 * time to update the database and perform an assesment over the villans
	 * 
	 */
	public void updateDB() {
		villans.forEach(gr -> gr.updateDB());
	}

	private ArrayList<TEntry<String, Double>> tempList = new ArrayList<>();
	public String getBoss() {
		tempList.clear();
		for (int i = 1; i < villans.size(); i++) {
			GamePlayer gp = villans.elementAt(i);
			double mean = gp.getMean() * Trooper.getInstance().getSensorsArray().getPokerSimulator().getBuyIn();
			double var = gp.getVariance();
			tempList.add(new TEntry<>(gp.toString(), mean + var));
		}
		tempList.sort(null);
		String boss = tempList.size() > 0 ? tempList.get(tempList.size() - 1).getKey() : "No boss detected.";
		return boss;
	}
	/**
	 * Take a snapshot of the game status. At this point all elements are available for be processed because this method
	 * is called one step before the tropper perform his action.
	 * 
	 */
	public void takeSnapShot(int round) {
		villans.stream().forEach(v -> v.update(round));
	}
}
