package plugins.hero;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.stream.*;

import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.stat.descriptive.*;
import org.jdesktop.application.*;

import com.javaflair.pokerprophesier.api.adapter.*;
import com.javaflair.pokerprophesier.api.card.*;

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
	private static DecimalFormat twoDigitFormat = new DecimalFormat("#0.00");

	public static String EXPLANATION = "trooper.Explanation";
	public static String STATUS = "trooper.Status";
	private static String ODDS_EV = "Expected value odds";
	private static String ODDS_MREV = "Minimun regret odds";
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

	private GameRecorder gameRecorder;
	private Properties preflopHands;
	long stepMillis;

	// This variable is ONLY used and cleaned by ensuregametable method
	private String lastHoleCards = "";

	private double preflopReconAmunition;

	public Trooper() {
		this(null);
	}

	public Trooper(Trooper clone) {
		super(Alesia.getInstance());
		this.robotActuator = new RobotActuator();
		availableActions = new Vector<>();
		this.outGameStats = new DescriptiveStatistics(10);
		this.sensorsArray = new SensorsArray();
		this.pokerSimulator = sensorsArray.getPokerSimulator();
		instance = this;
		this.preflopHands = new Properties();
		try {
			preflopHands.load(new FileInputStream("preflop.properties"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (clone != null && clone.file != null) {
			init(clone.file);
		}
	}

	public static Trooper getInstance() {
		return instance;
	}
	public void cancelTrooper(boolean interrupt) {
		setVariableAndLog(STATUS, "Trooper Canceled.");
		super.cancel(interrupt);
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
		gameRecorder = new GameRecorder(sensorsArray);
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
		setVariableAndLog(STATUS, paused ? "Trooper paused" : "Trooper resumed");
	}
	public void setTestMode(boolean isTestMode) {
		this.isTestMode = isTestMode;
	}

	private void clearEnviorement() {
		sensorsArray.init();
		preflopReconAmunition = 0;
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
			setVariableAndLog(EXPLANATION, "Error detected in simulator.");
			return;
		}

		double number = getAmmunitions();
		setOddActions(ODDS_MREV, "from ammunition value", number);

		// ammunitionControl();

		// preflop: if normal pot odd action has no action todo, check the preflopcards.
		if (availableActions.size() == 0 && pokerSimulator.getCurrentRound() == PokerSimulator.HOLE_CARDS_DEALT) {
			setVariableAndLog(EXPLANATION, "pre flop action");
			setPrefloopActions();
		}

		// if the list of available actions are empty, i habe no option but fold/check
		// in concordance whit rule1: if i can keep checking until i get a luck card. i will do. this behabior also
		// allow for example getpreflop action continue because some times, the enviorement is too fast and the trooper
		// is unable to retribe all information
		if (availableActions.size() == 0 && sensorsArray.getSensor("call").isEnabled()
				&& pokerSimulator.getCallValue() <= 0) {
			setVariableAndLog(STATUS, "Empty list. Checking");
			availableActions.add(new TEntry<String, Double>("call", 1.0));
		}

		// if the list of available actions are empty, the only posible action todo now is fold
		if (availableActions.size() == 0) {
			setVariableAndLog(STATUS, "Empty list. Folding");
			availableActions.add(new TEntry<String, Double>("fold", 1.0));
		}
	}
	/**
	 * compute and return the amount of chips available for actions. The amunitons come form 2 sides: the pot and the
	 * chips from hero. Both values are fractions acording to the current hand ranck. this allow the troper invest
	 * ammunitons acording to a real chance of winning. The previos estimation based on probabilities sen the trooper to
	 * invest a lot on amunitions in low value hands an is easy anbush by villans.
	 * 
	 * TODO: control the number of amunitions per street. the trooper most dangeros disadvantege is when all the villas
	 * put small amount of chips during a lager period of time in a single street (generaly preflop) hero must avoid
	 * this situation because in subsecuent street, the pot will be so hight that hero will be available to go allin
	 * whit poor hands
	 * 
	 * @return amunitions
	 */
	private double getAmmunitions() {
		// Hand hand = pokerSimulator.getMyHandHelper().getHand();
		// double factor = (hand.getHandRank() - 1) * 0.10;
		double factor = pokerSimulator.getHandFactor();
		double prob = pokerSimulator.getBestProbability();
		// double chips = pokerSimulator.getHeroChips() * factor;
		double chips = pokerSimulator.getHeroChips();
		// double pot = pokerSimulator.getPotValue() * factor;
		double buyIn = pokerSimulator.getBuyIn();
		// the minimul is the buyin.
		double number = chips > buyIn ? buyIn * factor : chips * factor;

		 String txt = "Amunition amount ";
		// if (invest > pot) {
		// number = invest;
		// txt = "Hero chips ";
		// } else {
		// number = pot;
		// txt = "Pot ";
		// }
		// only allow allin posibility for prob ??????????? o better or the hand is the nuts
		if (pokerSimulator.getMyHandHelper().isTheNuts() || prob > 0.9) {
			txt = pokerSimulator.getMyHandHelper().isTheNuts()
					? "Current hand is the nuts "
					: "Hight Probability (" + fourDigitFormat.format(prob) + ") ";
			number = chips;
		}
		// // bugget control.
		// if (number > buyIn) {
		// number = buyIn;
		// txt = "Max ";
		// }
		setVariableAndLog(EXPLANATION, txt + twoDigitFormat.format(number));
		return number;
	}

	/**
	 * chck for all sensor and return the current active stronger villan. The stronger villan in this functions is the
	 * villan with more chips.
	 */
	private double getBoss() {
		ArrayList<Double> chips = new ArrayList<>();
		for (int id = 1; id <= sensorsArray.getVillans(); id++) {
			if (sensorsArray.isActive(id))
				chips.add(sensorsArray.getSensor("villan" + id + ".chips").getNumericOCR());
		}
		chips.removeIf(i -> i.doubleValue() < 0);
		chips.sort(null);

		if (chips.isEmpty())
			return -1;

		double wv = chips.get(chips.size() - 1);
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
		int elements = availableActions.size();
		double denom = availableActions.stream().mapToDouble(te -> te.getValue().doubleValue()).sum();
		int[] singletos = new int[elements];
		double[] probabilities = new double[elements];
		for (int i = 0; i < elements; i++) {
			singletos[i] = i;
			double evVal = availableActions.get(i).getValue();
			probabilities[i] = evVal / denom;
		}
		EnumeratedIntegerDistribution dist = new EnumeratedIntegerDistribution(singletos, probabilities);
		String selact = availableActions.get(dist.sample()).getKey();
		pokerSimulator.setActionsData(selact, availableActions);
		return selact;
	}
	/**
	 * return <code>true</code> if the hero cards are inside of the predefinde hand distributions for pre-flop. This
	 * implementation only contain a roky based card selection. this selecction is only here because has a "common
	 * sense" card distribution for preflop decision.
	 * 
	 * TODO: Check Loky from UoA. there is a table with a complete hand distribution for preflop
	 * 
	 */
	private String isGoodPreflopHand() {
		String txt = null;
		HoleCards hc = pokerSimulator.getMyHoleCards();
		String s = hc.isSuited() ? "s" : "";
		String cr = hc.getFirstCard().toString().substring(0, 1) + hc.getSecondCard().toString().substring(0, 1);
		String cards = cr + s;
		boolean good = preflopHands.containsValue(cards);
		if (good) {
			txt = "preflop hand is in positive EV list";
		}
		// pocket pair
		if (pokerSimulator.getMyHandHelper().isPocketPair()) {
			txt = "preflop hand is a pocket pair";
		}

		// suited
		if (pokerSimulator.getMyHoleCards().isSuited())
			txt = "preflop hand is suited";

		// connected: cernters cards separated only by 1 or 2 cards provides de best probabilities (>6%)
		double sp = pokerSimulator.getMyHandStatsHelper().getStraightProb();
		if (sp >= 0.060)
			txt = "preflop hand is posible straight";

		// 10 or higher
		Card[] heroc = pokerSimulator.getMyHoleCards().getCards();
		if (heroc[0].getRank() > Card.NINE && heroc[1].getRank() > Card.NINE)
			txt = "preflop hand are 10 or higher";

		return txt;
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
	 * TODO: maybe implement some kind of threshold to alow 1 more action (bluff)
	 * <p>
	 * TODO: the extreme values: fold=-1 and allin=x must be agree whit mathematical poker model to allow bluff.
	 * 
	 * @param sourceName - the name of the source
	 * @param sourceValue - the value
	 */
	private void setOddActions(String computationType, String sourceName, double sourceValue) {
		availableActions.clear();
		// no calculation for 0 value
		if (sourceValue == 0) {
			Hero.logger.info("No posible positive EV for " + sourceValue);
			return;
		}
		double call = pokerSimulator.getCallValue();
		double raise = pokerSimulator.getRaiseValue();
		double chips = pokerSimulator.getHeroChips();
		double pot = pokerSimulator.getPotValue();

		if (call >= 0)
			availableActions.add(new TEntry<String, Double>("call", call));

		if (raise >= 0)
			availableActions.add(new TEntry<String, Double>("raise", raise));

		if (pot >= 0 && sensorsArray.getSensor("raise.pot").isEnabled())
			availableActions.add(new TEntry<String, Double>("raise.pot;raise", pot));

		if (chips >= 0 && sensorsArray.getSensor("raise.allin").isEnabled())
			availableActions.add(new TEntry<String, Double>("raise.allin;raise", chips));

		double sb = pokerSimulator.getSmallBlind();
		double bb = pokerSimulator.getBigBlind();
		if (raise > 0 && sensorsArray.getSensor("raise.slider").isEnabled()) {
			// check for int or double values for blinds
			boolean isInt = (new Double(bb)).intValue() == bb && (new Double(sb)).intValue() == sb;
			double tick = (chips - raise) / 10;
			for (int c = 1; c < 11; c++) {
				double tickVal = raise + (tick * c);
				String txt = isInt ? "" + (int) tickVal : twoDigitFormat.format(tickVal);
				availableActions
						.add(new TEntry<String, Double>("raise.text,dc;raise.text,k=" + txt + ";raise", tickVal));
			}
		}
		if (computationType == ODDS_EV) {
			calculateOdds(sourceValue, availableActions);
			availableActions.sort(Collections.reverseOrder());
		} else {
			calculateRegretMinOdds(sourceValue, availableActions);
			availableActions.sort(null);
		}
		// 191228: Hero win his first game against TH app !!!!!!!!!!!!!!!! :D
		String val = availableActions.stream().map(te -> te.getKey() + "=" + fourDigitFormat.format(te.getValue()))
				.collect(Collectors.joining(", "));
		val = val.trim().isEmpty() ? "No positive EV" : val;
		Hero.logger.info(computationType + " for " + sourceName + " " + val);
	}

	/**
	 * Set the action based on the starting hand distribution. If the starting hand is inside on the predefined hands
	 * distribution, this method evaluate if a predefinde max amount of chips has ben reached (due to an allin or
	 * repeated villans.s rise/call) . if this is the case, the metodh clear the action list and the standar final
	 * action will be selected (check/fold).
	 * <p>
	 * the general idea here is try to put the trooper in folp, so the normal odds operation has chance to decide, at
	 * lower posible cost
	 */
	private void setPrefloopActions() {
		availableActions.clear();
		String prehand = isGoodPreflopHand();
		if (prehand == null) {
			setVariableAndLog(EXPLANATION, "preflop hand not good.");
			return;
		}
		// 5% of the buyin bus be enougth
		double max = pokerSimulator.getBuyIn() * 0.05;
		double call = pokerSimulator.getCallValue();
		// can i check ??
		if (call == 0) {
			availableActions.add(new TEntry<String, Double>("call", 1.0));
			setVariableAndLog(EXPLANATION, "Checking.");
			return;
		}
		// enought ammunitions wasted ??
		if (call > max || preflopReconAmunition > max) {
			// TODO: test the case when i already a hight par (AA)
			setVariableAndLog(EXPLANATION, "No more ammunition available for preflop actions.");
			return;
		}
//		last check: can call (the sensor is active)??
		if(call != -1) {
			preflopReconAmunition += call;
			availableActions.add(new TEntry<String, Double>("call", call));
			setVariableAndLog(EXPLANATION, prehand + ". Calling " + call + " of " + max);
		}

		// TEST: if the cost is marginal, i go. trying to get luck in folp street. this allow trooper to look for hiden
		// oportunities
		// double call = pokerSimulator.getCallValue();
		// double temMax = pokerSimulator.getBigBlind() * 2;
		// if (call <= 0) {
		// setVariableAndLog(EXPLANATION, "empty list. Letting check.");
		// return;
		// }
		//
		// if (call > temMax || preflopReconAmunition > temMax) {
		// setVariableAndLog(EXPLANATION, "No more ammunition available for Recon mode.");
		// return;
		// }
		// preflopReconAmunition += call;
		// availableActions.add(new TEntry<String, Double>("call", call));
		// setVariableAndLog(EXPLANATION, "Recon mode. Calling " + call);
	}

	private void setVariableAndLog(String key, Object value) {
		String value1 = value.toString();
		if (value instanceof Double)
			value1 = fourDigitFormat.format(((Double) value).doubleValue());
		pokerSimulator.setVariable(key, value);
		// don.t log the status, only the explanatio
		if (!STATUS.equals(key)) {
			String key1 = key.replace(EXPLANATION, "");
			// 200210: Hero play his first 2 hours with REAL +EV. Convert 10000 chips in 64000
			Hero.logger.info(key1 + value1);
		}
	}
	/**
	 * This metod check all the sensor areas and perform the corrections to get the troper into the fight. The
	 * conbination of enabled/disabled status of the sensor determine the action to perform. If the enviorement request
	 * the trooper to play, this method return <code>true</code>, else this method will try to reach the gametable until
	 * an fix amount of time is reached. In that case, this method return <code>false</code>
	 * 
	 * @return <code>true</code> if the enviorement is waiting for the troper to {@link #decide()} and {@link #act()}.
	 */
	private boolean watchEnviorement() throws Exception {
		setVariableAndLog(STATUS, "Looking the table ...");
		// try during x seg. Some round on PS long like foreeeeveeerr
		long tottime = 300 * 1000;
		long t1 = System.currentTimeMillis();
		boolean restarChips = true;
		while (System.currentTimeMillis() - t1 < tottime) {
			// pause ?
			if (paused) {
				Thread.sleep(100);
				continue;
			}
			// canceled ?
			if (isCancelled())
				return false;

			sensorsArray.readVillans(restarChips);
			restarChips = false;

			sensorsArray.read(SensorsArray.TYPE_ACTIONS);

			// NEW ROUND: if the hero current hand is diferent to the last measure, clear the enviorement.
			String hc1 = sensorsArray.getSensor("hero.card1").getOCR();
			String hc2 = sensorsArray.getSensor("hero.card2").getOCR();
			String hch = hc1 == null ? "" : hc1;
			hch += hc2 == null ? "" : hc2;
			if (!("".equals(hch) || lastHoleCards.equals(hch))) {
				lastHoleCards = hch;
				setVariableAndLog(STATUS, "Cleanin the enviorement ...");
				setVariableAndLog(EXPLANATION, "new round.");
				clearEnviorement();
				setVariableAndLog(STATUS, "Looking the table ...");
				continue;
			}

			// enviorement is in the gametable
			if (isMyTurnToPlay()) {
				// repeat the look of the sensors. this is because some times the capture is during a animation
				// transition. to avoid error reading sensors, perform the lecture once more time. after the second
				// lecutre, this return return normaly
				sensorsArray.read(SensorsArray.TYPE_ACTIONS);
				return true;
			}

			// if any of this are active, do nothig. raise.text in this case, is wachit a chackbok for check
			if (sensorsArray.isSensorEnabled("raise.text") || sensorsArray.isSensorEnabled("sensor1")
					|| sensorsArray.isSensorEnabled("sensor2")) {
				continue;
			}

			// the i.m back button is active (at this point, the enviorement must only being showing the i.m back
			// button)
			if (sensorsArray.isSensorEnabled("imBack")) {
				// robotActuator.perform("imBack");
				continue;
			}
		}
		setVariableAndLog(EXPLANATION, "Can.t reach the main gametable. Trooper return.");
		return false;
	}

	/**
	 * perform the action. At this point, the game table is waiting for the hero action.
	 * 
	 */
	protected void act() {
		setVariableAndLog(STATUS, "Acting ...");
		String ha = getSubOptimalAction();
		gameRecorder.takeSnapShot();
		String key = "trooper.Action performed";
		pokerSimulator.setVariable(key, ha);
		// robot actuator perform the log
		robotActuator.perform(ha);
		gameRecorder.updateDB();
		setVariableAndLog("trooper.Assesment ", gameRecorder.getAssest(0));
	}
	/**
	 * Return the expectation of the <code>base</code> argument against the <code>cost</code> argument. to comply with
	 * rule 1, this method retrive his probability from {@link #getProbability()}
	 * <h5>MoP page 54</h5>
	 * 
	 * @param base - amount to retrive the odds from
	 * @param cost - cost of call/bet/raise/...
	 * 
	 */
	protected void calculateOdds(double base, Vector<TEntry<String, Double>> list) {
		// Preconditions.checkArgument(base >= 0 && cost >= 0, "Odd function accept only 0 or positive values.");
		double prob = pokerSimulator.getBestProbability();
		if (prob == 0) {
			list.clear();
			setVariableAndLog(EXPLANATION, "no posible +EV with probability = " + prob);
		}
		for (TEntry<String, Double> tEntry : list) {
			double cost = tEntry.getValue();
			// MoP page 54
			double ev = (prob * base) - cost;
			tEntry.setValue(ev);
		}
		// remove the action with negative EV
		list.removeIf(te -> te.getValue() < 0);
	}

	protected void calculateRegretMinOdds(double base, Vector<TEntry<String, Double>> list) {
		double prob = pokerSimulator.getBestProbability();
		if (prob == 0) {
			list.clear();
			setVariableAndLog(EXPLANATION, "no posible +EV with probability = " + prob);
		}

		// step (by observation, 1/20 of the bb)
		double step = pokerSimulator.getBigBlind() / 20.0;
		// regret
		double reg = (prob - PokerSimulator.probabilityThreshold) * -1 * step;
		for (TEntry<String, Double> tEntry : list) {
			double cost = tEntry.getValue();
			// 1 calculate normal EV
			double ev = (prob * base) - cost;
			// 2 ONLY if normal EV is positive, calcula RMEV.
			if (ev > 0)
				ev = (prob * base) - (cost * reg);
			tEntry.setValue(ev);
		}
		// 3 remove all negative values
		list.removeIf(te -> te.getValue() < 0);
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

		clearEnviorement();

		while (!isCancelled()) {
			if (paused) {
				Thread.sleep(100);
				continue;
			}
			// countdown before start
			if (countdown > 0) {
				countdown--;
				setVariableAndLog(STATUS, "start in " + countdown);
				Thread.sleep(1000);
				continue;
			}

			boolean ingt = watchEnviorement();

			// if i can reach the gametable, dismiss the troper
			if (!ingt) {
				return null;
			}

			// at this point i must decide and act
			decide();
			act();
		}
		return null;
	}
	/**
	 * This method is invoked during the idle phase (after {@link #act()} and before {@link #decide()}. use this method
	 * to perform large computations.
	 */
	protected void think() {
		// setVariableAndLog(STATUS, "Reading villans ...");
		// sensorsArray.readVillan();
		// 191020: ayer ya la implementacion por omision jugo una partida completa y estuvo a punto de vencer a la
		// chatarra de Texas poker - poker holdem. A punto de vencer porque jugaba tan lento que me aburri del sueno :D
	}
}
