package plugins.ratass;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.stream.*;

import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.stat.descriptive.*;
import org.jdesktop.application.*;

import com.javaflair.pokerprophesier.api.adapter.*;
import com.javaflair.pokerprophesier.api.card.*;
import com.jgoodies.common.base.*;

import core.*;

/**
 * this class represent the core of al hero plugins. As a core class, this class dependes of anothers classes in order
 * to build a useful player agent. the followin is a list of class from where the information is retrived, and the
 * actions are performed.
 * <ul>
 * <li>{@link PokerSimulator} - get the numerical values for decition making
 * <li>{@link RobotActuator} - permorm the action sended by this class.
 * <li>{@link SensorsArray} - perform visual operation of the enviorement
 * </ul>
 * <p>
 * <b>RULE 1: hero is here to win money. So in order to do that, hero need to fight and stay in the table. </b>
 * <p>
 * to be agree with this rule, this implementation:
 * <ul>
 * <li>Invest his chips only in calculated 0 or positive EV. When the EV for pot odd return an empty list, for example,
 * pot=0 (initial bet and hero is the dealer) the EV function will return negative espectative even with AAs. in this
 * case, the {@link #setPrefloopActions()} is called as a last resource.
 * <li>Table Position: In this implementation, the tableposition is irrelevant because the normal odd action take the
 * values of the the pot odd actions are imp the table position are implied in the normal odd actions. this mean, the
 * convination of odd actions and preflophand evaluation has the hero table position already implied.
 * <li>Number of villans: The number of villans is also irrelevant in this implementation because that information is
 * already present in {@link PokerProphesierAdapter}.
 * </ul>
 * 
 * @author terry
 *
 */
public class Trooper extends Task {

	private static Trooper instance;
	private static DecimalFormat fourDigitFormat = new DecimalFormat("#0.0000");

	public static String EXPLANATION = "trooper.Explanation";
	public static String STATUS = "trooper.Status";
	private PokerSimulator pokerSimulator;
	private RobotActuator robotActuator;
	private SensorsArray sensorsArray;
	private boolean isTestMode;
	private Vector<TEntry<String, Double>> availableActions;
	private int countdown = 5;
	private File file;
	private long time1;
	private DescriptiveStatistics outGameStats;
	private boolean paused = false;
	/**
	 * the game recorder is created only after the continue action. this avoid record incomplete sesion
	 */
	private GameRecorder gameRecorder;

	private long lastGCCall = -1;
	public Trooper() {
		this(null);
	}
	public Trooper(Trooper clone) {
		super(Alesia.getInstance());
		this.robotActuator = new RobotActuator();
		availableActions = new Vector<>();
		this.outGameStats = new DescriptiveStatistics(10);
		// this.trooperStatus = ACTIVE;
		this.sensorsArray = new SensorsArray();
		this.pokerSimulator = sensorsArray.getPokerSimulator();
		instance = this;
		if (clone != null && clone.file != null) {
			init(clone.file);
		}
	}
	public static Trooper getInstance() {
		return instance;
	}

	public SensorsArray getSensorsArray() {
		return sensorsArray;
	}

	/**
	 * set the enviorement. this method create a new enviorement discarting all previous created objects
	 * 
	 */
	public void init(File file) {
		ShapeAreas sDisp = new ShapeAreas(file);
		sDisp.read();
		this.file = file;
		this.availableActions.clear();
		sensorsArray.createSensorsArray(sDisp);
		robotActuator.setEnviorement(sDisp);
		// gameRecorder = new GameRecorder(sensorsArray);
		Hero.sensorsPanel.setArray(sensorsArray);
	}

	public boolean isPaused() {
		return paused;
	}

	public boolean isTestMode() {
		return isTestMode;
	}

	public void pause(boolean pause) {
		this.paused = pause;
		setVariableAndLog(STATUS, paused ? "Game paused" : "Game resumed");
	}

	public void setTestMode(boolean isTestMode) {
		this.isTestMode = isTestMode;
	}
	/**
	 * check the win probability. The tendency of the trooper to stay in the game some times make him reach higher
	 * street with extreme low win probability (spetialy the river) to avoid error on {@link #getSubOptimalAction()},
	 * this code ensure fold action only for 0 troper probability
	 * 
	 * TODO: this variables MUST be ajusted. maybe dinamicaly or based on gamerecording analisis. for now, pot to 10%
	 * only to aboid drain chips in lost causes
	 * 
	 */
	private boolean checkProbabilities(int currentRound) {
		double prob = pokerSimulator.getBestProbability();
		if (currentRound == PokerSimulator.FLOP_CARDS_DEALT && prob < 0.20) {
			availableActions.clear();
			setVariableAndLog(EXPLANATION, "Probability on flop < 20%");
		}
		if (currentRound > PokerSimulator.FLOP_CARDS_DEALT && prob < 0.15) {
			availableActions.clear();
			setVariableAndLog(EXPLANATION, "Probability on turn or river < 15%");
		}
		return availableActions.size() == 0;
	}

	private void clearEnviorement() {
		sensorsArray.init();
		availableActions.clear();
		// at first time execution, a standar time of 10 second is used
		long tt = time1 == 0 ? 10000 : System.currentTimeMillis() - time1;
		outGameStats.addValue(tt);
		time1 = System.currentTimeMillis();
		Hero.logger.fine("Game play time average=" + TStringUtils.formatSpeed((long) outGameStats.getMean()));
	}
	/**
	 * decide de action(s) to perform. This method is called when the {@link Trooper} detect that is my turn to play. At
	 * this point, the game enviorement is waiting for an accion.
	 * 
	 */
	private void decide() {
		setVariableAndLog(STATUS, "Deciding ...");
		// read first the numbers to update the dashboard whit the numerical values. this allow me some time to inspect.
		// only for visula purporse
		sensorsArray.read(SensorsArray.TYPE_NUMBERS);
		sensorsArray.read(SensorsArray.TYPE_CARDS);
		availableActions.clear();

		// chek the status of the simulator in case of error. if an error is detected, fold
		if (!pokerSimulator.getVariables().get(PokerSimulator.STATUS).equals(PokerSimulator.STATUS_OK)) {
			availableActions.add(new TEntry<String, Double>("fold", 1.0));
			setVariableAndLog(EXPLANATION, "Exception detected in poker simulator.");
			return;
		}

		int currentRound = pokerSimulator.getCurrentRound();
		int pot = pokerSimulator.getPotValue();
		// int chips = pokerSimulator.getHeroChips();

		setVariableAndLog(EXPLANATION, "normal pot odds actions");
		setOddActions("Pot " + pot, pot);

		// check the odd probabilities
		checkProbabilities(currentRound);

		// Exterme cases: Preflop
		if (availableActions.size() == 0 && currentRound == PokerSimulator.HOLE_CARDS_DEALT) {
			setVariableAndLog(EXPLANATION, "pre flop action");
			setPrefloopActions();
		}

		// if the list of available actions are empty, the only posible action todo now is fold
		if (availableActions.size() == 0)
			availableActions.add(new TEntry<String, Double>("fold", 1.0));
	}

	/**
	 * this metod will try to return to the game table if he detect the current enviorement is in another place. This
	 * method will try for an specific amount of time to return to the main gametable. if it not succede return
	 * <code>false</code>
	 * 
	 * @return <code>true</code> if the enviorement is in the main gametable.
	 */
	private boolean ensureGameTable() throws Exception {
		setVariableAndLog(STATUS, "Looking the table ...");
		// try during 10 seg
		long tottime = 30 * 1000;
		long t1 = System.currentTimeMillis();
		while (System.currentTimeMillis() - t1 < tottime) {
			sensorsArray.lookTable();
			// enviorement is already in the gametable
			if (isMyTurnToPlay()) {
				return true;
			}

			// continue active and fold inactive. the match is end. clear the internal variables
			if (sensorsArray.isSensorEnabled("continue") && !sensorsArray.isSensorEnabled("fold")) {
				if (gameRecorder != null) {
					gameRecorder.flush();
				}
				gameRecorder = new GameRecorder(sensorsArray);
				robotActuator.perform("continue");
				clearEnviorement();
				continue;
			}

			// enviorement is in main menu? click on play button
			if (sensorsArray.isSensorEnabled("sensor0")
					&& !(sensorsArray.isSensorEnabled("fold") && sensorsArray.isSensorEnabled("continue"))) {
				robotActuator.perform("sensor0");
				continue;
			}

			// enviorement is in continue after lost focus? click on continue button
			if (sensorsArray.isSensorEnabled("sensor2") && sensorsArray.isSensorEnabled("sensor1")) {
				robotActuator.perform("sensor2");
				continue;
			}

			// trooper win the match. clic on ok
			if (sensorsArray.isSensorEnabled("sensor5") && sensorsArray.isSensorEnabled("continue")) {
				robotActuator.perform("continue");
				continue;
			}
		}
		// System.out.println(System.currentTimeMillis() - t1);
		return false;
	}
	/**
	 * Return the expectation of the <code>base</code> argument against the <code>cost</code> argument. to comply with
	 * rule 1, this method retrive his probability from {@link #getProbability()}
	 * <h5>MoP page 54</h5>
	 * 
	 * @param base - amount to retrive the odds from
	 * @param cost - cost of call/bet/raise/...
	 * 
	 * @return expected utility for the passed argument
	 */
	private double getOdds(int base, int cost) {
		Preconditions.checkArgument(base >= 0 && cost >= 0, "Odd function accept only 0 or positive values.");
		double prob = pokerSimulator.getBestProbability();
		// MoP page 54
		double ev = (prob * base) - cost;
		return ev;
	}

	private double getOddsWhioutRegret(int base, int cost) {
		Preconditions.checkArgument(base >= 0 && cost >= 0, "Odd function accept only 0 or positive values.");
		double prob = pokerSimulator.getBestProbability();
		// normal EV
		double ev = (prob * base) - cost;
		if (ev < 0)
			return ev;
		// regret
		double reg = (prob - 0.5) * -1;
		double ev2 = (prob * base) - (cost * reg);
		return ev2;
	}

	/**
	 * chck for all sensor and return the current active stronger villan. The stronger villan in this functions is the
	 * villan with more chips.
	 */
	private int getStrongerVillan() {
		ArrayList<Integer> chips = new ArrayList<>();
		for (int id = 1; id <= sensorsArray.getVillans(); id++) {
			if (sensorsArray.isVillanActive(id))
				chips.add(sensorsArray.getSensor("villan" + id + ".chips").getIntOCR());
		}
		chips.removeIf(i -> i.intValue() < 0);
		chips.sort(null);

		// TODO: temporal for TH, the sensor some times get error and this function cant detect the stronger villan. in
		// this case, return 0 to allow the fight continue
		if (chips.isEmpty())
			return 0;

		int wv = chips.get(chips.size() - 1);
		return wv;
	}
	/**
	 * perform a random selection of the available actions. this method build a probability distribution and select
	 * randmly a variate of that distribution. this is suboptimal because the objetive function already has the optimal
	 * action to perfomr. but this behavior make the trooper visible to the villans that can use this informatiion for
	 * trap or fool the troper.
	 * <p>
	 * the available actions and his values must be previous evaluated. The action value express the hihest expected
	 * return.
	 * 
	 */
	private String getSubOptimalAction() {
		Vector<TEntry<String, Double>> tmp = new Vector<>();
		int elements = availableActions.size();
		double denom = availableActions.stream().mapToDouble(te -> te.getValue().doubleValue()).sum();
		int[] singletos = new int[elements];
		double[] probabilities = new double[elements];
		for (int i = 0; i < elements; i++) {
			singletos[i] = i;
			probabilities[i] = availableActions.get(i).getValue() / denom;
			tmp.add(new TEntry<>(availableActions.get(i).getKey(), probabilities[i]));
		}
		EnumeratedIntegerDistribution dist = new EnumeratedIntegerDistribution(singletos, probabilities);
		String selact = availableActions.get(dist.sample()).getKey();
		pokerSimulator.setActionsData(selact, tmp);
		return selact;
	}

	/**
	 * invert the list of {@link #availableActions} if the list contain all negative expected values.
	 * <p>
	 * Under some conditions, for example, when pot=0 or when, in pre flop street, the cost of a single call can not be
	 * afforded even if hero has a pair of AAs, the normal potodd equation return negative espectative, in this case,
	 * the trooper will fold his hand. to avoid this, translate all negatives values to the positive contepart changing
	 * the sigh and shift the values to reflect the inverse of the retrived value. this allow
	 * {@link #getSubOptimalAction()} select the less worst action for this espetial case.
	 * 
	 * <p>
	 * Example: calculating the potodd for pot=0 will return: <code>
	 * <TABLE>
	 * <TR>
	 * <TD>fold=-1</TD>
	 * <TD>fold=1000</TD>
	 * </TR>
	 * <TR>
	 * <TD>call(100)=-100</TD>
	 * <TD>call(100)=200</TD>
	 * </TR>
	 * <TR>
	 * <TD>raise(200)=-200</TD>
	 * <TD>raise(200)=200</TD>
	 * </TR>
	 * <TR>
	 * <TD>...</TD>
	 * <TD>...</TD>
	 * </TR>
	 * <TR>
	 * <TD>allin(1000)=-1000</TD>
	 * <TD>allin(1000)=1</TD>
	 * </TR>
	 * </TABLE>
	 * </code>
	 * 
	 */
	private void invertNegativeEV() {
		// remove positive values. just in case
		availableActions.removeIf(te -> te.getValue().doubleValue() > 0);
		if (availableActions.isEmpty())
			return;

		// availableActions.add(new TEntry<String, Double>("fold", -1.0));
		// availableActions.sort(null);
		double max = availableActions.get(0).getValue() * -1.0;
		availableActions.stream().forEach(te -> te.setValue(max + te.getValue()));
		// pokerSimulator.setActionsData("Inverted", null, availableActions);
	}

	/**
	 * return <code>true</code> if the hero cards are inside of the predefinde hand distributions for pre-flop. This
	 * implementation only contain a roky based card selection. this selecction is only here because has a "common
	 * sense" card distribution for preflop decision.
	 * 
	 * TODO: Check Loky from UoA. there is a table with a complete hand distribution for preflop
	 * 
	 */
	private boolean isGoodPreflopHand() {
		String value = null;
		// pocket pair
		if (pokerSimulator.getMyHandHelper().isPocketPair())
			value = "preflop hand is a pocket pair";

		// suited
		if (pokerSimulator.getMyHoleCards().isSuited())
			value = "preflop hand is suited";

		// connected: cernters cards separated only by 1 or 2 cards provides de best probabilities (>6%)
		double sp = pokerSimulator.getMyHandStatsHelper().getStraightProb();
		if (sp > 0.059)
			value = "preflop hand is posible straight";

		// 10 or higher
		Card[] heroc = pokerSimulator.getMyHoleCards().getCards();
		if (heroc[0].getRank() > Card.NINE && heroc[1].getRank() > Card.NINE)
			value = "preflop hand are 10 or higher";

		boolean ok = true;
		if (value == null) {
			value = "preflop hand are not good";
			ok = false;
		}

		setVariableAndLog(EXPLANATION, value);
		return ok;
	}

	private boolean isMyTurnToPlay() {
		return sensorsArray.isSensorEnabled("fold") || sensorsArray.isSensorEnabled("call")
				|| sensorsArray.isSensorEnabled("raise");
	}

	/**
	 * Compute the actions available according to {@link #getOdds(int, String)} evaluations. The resulting computation
	 * will be reflected in a list of actions with his expected values.
	 * <p>
	 * this method also remove negative ev if there exist positive ev. this is because most of the times, there are more
	 * negative EV than positives ones, if i translate the function to the positive cuadrant to allow posible agressive
	 * actions violate the main purporse of this implementation. so, until now, i just remove them.
	 * 
	 * 
	 * TODO: maybe implement some kind of threshold to alow 1 more action (bluff) TODO: the extreme values: fold=-1 and
	 * allin=x must be agree whit mathematical poker model to allow bluff.
	 * 
	 * @param sourceName - the name of the source
	 * @param sourceValue - the value
	 */
	private void setOddActions(String sourceName, int sourceValue) {
		availableActions.clear();
		int call = pokerSimulator.getCallValue();
		int raise = pokerSimulator.getRaiseValue();
		int chips = pokerSimulator.getHeroChips();

		if (call >= 0)
			availableActions.add(new TEntry<String, Double>("call", getOddsWhioutRegret(sourceValue, call)));

		if (raise >= 0)
			availableActions.add(new TEntry<String, Double>("raise", getOddsWhioutRegret(sourceValue, raise)));

		// TODO: temporal removed for TH because the raise.slider is in the same area
		// if (getPotOdds(pot, "pot") >= 0) {
		// addAction("raise.pot;raise");
		// }

		// TODO: temporal for TH: simulate allin
		// if (chips >= 0) {
		// availableActions.add(new TEntry<String, Double>("raise.allin,c=10;raise", getOddsWhioutRegret(sourceValue, chips)));
		// }

		// TODO: until now i.m goin to implement the slider performing click over the right side of the compoent.
		// TODO: complete implementation of writhe the ammount for Poker star
		int sb = pokerSimulator.getSmallBlind();
		int bb = pokerSimulator.getBigBlind();

		// TODO: temporal for TH: the tick is the raise value ?
		// int tick = raise > 0 ? raise : call;
		int tick = raise;
		if (tick > 0) {
			for (int c = 1; c < 11; c++) {
				int val = tick + (tick * c);
				availableActions
						.add(new TEntry<String, Double>("raise.slider,c=" + c + ";raise", getOddsWhioutRegret(sourceValue, val)));
			}
		}

		// remove negative ev
		availableActions.removeIf(te -> te.getValue() < 0);

		// 191228: Hero win his first game against TH app !!!!!!!!!!!!!!!! :D
		String key = "Odds actions for";
		String val = " " + sourceName + " " + availableActions.stream()
				.map(te -> te.getKey() + "=" + fourDigitFormat.format(te.getValue())).collect(Collectors.joining(", "));
		Hero.logger.info(key + " " + val);
		// pokerSimulator.setActionsData(availableActions);
	}

	/**
	 * Set the action based on the starting hand distribution. if the starting hand is inside on the predefined
	 * distribution, then compute the amount of chips able to invest in this situation.
	 * <p>
	 * This method is used when the standar pot odds function return an empty action list. this happen when hero is in
	 * an early position in preflo game. To avoid hero fold his hand with a pocket AA, this function compare the villans
	 * with more chips against hero chips. the lowers of them is selected to calculate the normal odd actions.
	 * <ul>
	 * <li>hero chips > stronger villan: normal odds will select all available actions but all of them are inside of the
	 * range of villans actions. This avoid hero put in the table an amount superior to the villan chips.
	 * <li>hero chips < stronger villan: normal odd will select the available acctions, includin all in.
	 * </ul>
	 */
	private void setPrefloopActions() {
		availableActions.clear();
		if (!isGoodPreflopHand())
			return;

		int herochips = pokerSimulator.getHeroChips();
		int boss = getStrongerVillan();
		int base = herochips > boss ? boss : herochips;
		// in case of error getting hero or boss chips, the hand is still good. ??? what to do?
		if (base == -1) {
			setVariableAndLog(EXPLANATION, "Error getting boss chips or hero chip.");
			return;
		}
		setOddActions("Starting hand", base);
	}
	private void setVariableAndLog(String key, Object value) {
		String value1 = value.toString();
		if (value instanceof Double)
			value1 = fourDigitFormat.format(((Double) value).doubleValue());
		pokerSimulator.setVariable(key, value);
		Hero.logger.info(key + " " + value1);
	}

	private void upperBound() {
		// double max = availableActions.stream().mapToDouble(te -> te.getValue().doubleValue()).max().orElse(0);
		// double min = availableActions.stream().mapToDouble(te -> te.getValue().doubleValue()).min().orElse(0);
		if (availableActions.size() < 3)
			return;

		availableActions.sort(null);
		Double ub = availableActions.get(availableActions.size() - 2).getValue();
		availableActions.removeIf(te -> te.getValue() > ub);
	}
	/**
	 * perform the action. At this point, the game table is waiting for the hero action.
	 * 
	 * TODO: complete documentation
	 */
	protected void act() {
		setVariableAndLog(STATUS, "Acting ...");
		String ha = getSubOptimalAction();
		// pokerSimulator.setActionsData(ha, availableActions);
		if (gameRecorder != null) {
			gameRecorder.takeSnapShot(ha);
		}
		String key = "trooper.Action performed";
		pokerSimulator.setVariable(key, ha);
		// robot actuator perform the log
		robotActuator.perform(ha);
	}
	@Override
	protected Object doInBackground() throws Exception {

		// ensure db connection on the current thread.
		try {
			Alesia.openDB();
		} catch (Exception e) {
			// just a warning log because reiterated pause/stop/play can generate error re opening the connection
			Hero.logger.warning(e.getMessage());
		}

		while (!isCancelled()) {
			if (paused) {
				Thread.sleep(1000);
				continue;
			}
			// countdown before start
			if (countdown > 0) {
				countdown--;
				setVariableAndLog(STATUS, "start in " + countdown);
				Thread.sleep(1000);
				continue;
			}

			boolean ingt = ensureGameTable();
			if (!ingt) {
				// if after a few attemps, the enviorement is not in the gametable, signal error
				setVariableAndLog(EXPLANATION, "Can.t reach the main gametable. Trooper return.");
				return null;
			}

			// look the standar actions buttons. this standar button indicate that the game is waiting for my move
			if (isMyTurnToPlay()) {
				// TODO: temporal for TH. some times the piece of shit app show the action bar before complete deal the
				// cards. wait a couple of millis until start reading the table.
				Thread.sleep(200);
				decide();
				act();
			}
			think();
		}
		return null;
	}
	/**
	 * This method is invoked during the idle phase (after {@link #act()} and before {@link #decide()}. use this method
	 * to perform large computations.
	 */
	protected void think() {
		setVariableAndLog(STATUS, "Thinking ...");

		// test: call gc to check performance improvement every 10 minutes
		long time = 10 * 60 * 1000;
		long t1 = System.currentTimeMillis();
		if (t1 - lastGCCall > time) {
			System.gc();
			lastGCCall = t1;
			System.out.println("Trooper.think() " + (System.currentTimeMillis() - t1));
		}

		// 191020: ayer ya la implementacion por omision jugo una partida completa y estuvo a punto de vencer a la
		// chatarra de Texas poker - poker holdem. A punto de vencer porque jugaba tan lento que me aburri del sueno :D
	}
}
