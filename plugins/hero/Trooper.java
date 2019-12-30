package plugins.hero;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.stream.*;

import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.stat.descriptive.*;
import org.jdesktop.application.*;

import com.javaflair.pokerprophesier.api.card.*;
import com.javaflair.pokerprophesier.api.helper.*;
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
 * RULE 1: hero is here to win money. So in order to do that, hero need to fight and stay in the table.
 * <ol>
 * <li>This trooper implementation invest his chips only in calculated positive EV. the word FIGHT here means that in
 * some cases, like pot=0 (initial bet and hero is the dealer) the EV function will return negative espectative even
 * with pair of aces. For this particular case, the trooper will try to select the less worst negative EV.
 * <li>to compute pot odd function, there is a metod who try to select the best probability for it
 * 
 * 
 * 
 * @author terry
 *
 */
public class Trooper extends Task {

	// public static String WAITING = "waiting";
	// public static String ACTIVE = "active";
	private static Trooper instance;

	private static DecimalFormat fourDigitFormat = new DecimalFormat("#0.0000");
	private PokerSimulator pokerSimulator;
	private RobotActuator robotActuator;
	private SensorsArray sensorsArray;
	private String trooperStatus;
	private boolean isTestMode;
	private Vector<TEntry<String, Double>> availableActions;
	private int countdown = 5;
	private File enviorement;
	private long time1;
	private DescriptiveStatistics outGameStats;
	private boolean paused = false;
	private double lastProbableValue = -1;

	/**
	 * the game recorder is created only after the continue action. this avoid record incomplete sesion
	 */
	private GameRecorder gameRecorder;

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
		if (clone != null && clone.enviorement != null) {
			setEnviorement(clone.enviorement);
		}
	}

	public static Trooper getInstance() {
		return instance;
	}
	/**
	 * Return the expectation of the <code>base</code> argument against the <code>cost</code> argument. to comply with
	 * rule 2, this method retrive his probability from {@link #getProbability()}
	 * <h5>MoP page 54</h5>
	 * <p>
	 * 
	 * @param base - amount to retrive the odds from
	 * @param cost - cost of call/bet/raise/...
	 * 
	 * @return expected utility for the passed argument
	 */
	public double getOdds(int base, int cost) {
		Preconditions.checkArgument(base >= 0 && cost >= 0, "Odd function accept only 0 or positive values.");

		double totp = getProbability();

		// MoP page 54
		double poto = totp * base - cost;

		return poto;
	}
	public RobotActuator getRobotActuator() {
		return robotActuator;
	}
	public SensorsArray getSensorsArray() {
		return sensorsArray;
	}

	public boolean isCallEnabled() {
		return sensorsArray.getScreenSensor("call").isEnabled();
	}

	public boolean isPaused() {
		return paused;
	}

	public boolean isRaiseDownEnabled() {
		return sensorsArray.getScreenSensor("raise.down").isEnabled();
	}

	public boolean isRaiseEnabled() {
		return sensorsArray.getScreenSensor("raise").isEnabled();
	}

	public boolean isRaiseUpEnabled() {
		return sensorsArray.getScreenSensor("raise.up").isEnabled();
	}

	public boolean isTestMode() {
		return isTestMode;
	}

	public void pause(boolean pause) {
		this.paused = pause;
		Hero.logger.info(paused ? "Game paused..." : "Game resumed");
	}

	/**
	 * set the enviorement. this method create a new enviorement discarting all previous created objects
	 * 
	 */
	public void setEnviorement(File file) {
		ScreenAreas sDisp = new ScreenAreas(file);
		sDisp.read();
		this.enviorement = file;
		lastProbableValue = -1;
		this.availableActions.clear();
		sensorsArray.createSensorsArray(sDisp);
		robotActuator.setEnviorement(sDisp);
		// gameRecorder = new GameRecorder(sensorsArray);
		Hero.sensorsPanel.setEnviorement(this);
	}

	public void setTestMode(boolean isTestMode) {
		this.isTestMode = isTestMode;
	}

	public void setTrooperStatus(String trooperStatus) {
		this.trooperStatus = trooperStatus;
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
	private void addOddActions(String sourceName, int sourceValue) {
		availableActions.clear();
		int call = pokerSimulator.getCallValue();
		int raise = pokerSimulator.getRaiseValue();
		int chips = pokerSimulator.getHeroChips();

		if (call >= 0)
			availableActions.add(new TEntry<String, Double>("call", getOdds(sourceValue, call)));

		if (raise >= 0)
			availableActions.add(new TEntry<String, Double>("raise", getOdds(sourceValue, raise)));

		// TODO: temporal removed for TH because the raise.slider is in the same area
		// if (getPotOdds(pot, "pot") >= 0) {
		// addAction("raise.pot;raise");
		// }

		// TODO: temporal for TH: simulate allin
//		if (chips >= 0) {
//			availableActions.add(new TEntry<String, Double>("raise.allin,c=10;raise", getOdds(sourceValue, chips)));
//		}

		// TODO: until now i.m goin to implement the slider performing click over the right side of the compoent.
		// TODO: complete implementation of writhe the ammount for Poker star
		int sb = pokerSimulator.getSmallBlind();
		int bb = pokerSimulator.getBigBlind();

		// check the variable. if the house rule is for call variable (remember call has -1 for invalid, 0 for check and
		// positive for call) check the slider only for positive value
		if (call > 0) {
			for (int c = 1; c < 11; c++) {
				// TODO: temporal for TH. every up button increament the call value
				// TODO: move to house rules
				int tick = call + (call * c);
				availableActions
						.add(new TEntry<String, Double>("raise.slider,c=" + c + ";raise", getOdds(sourceValue, tick)));
			}
		}

		clearNegativeEV();
		// 191228: Hero win his first game against TH app !!!!!!!!!!!!!!!! :D
		String key = "Odds actions for";
		// String val = sourceName + "(" + sourceValue + ") " + availableActions.stream()
		String val = " " + sourceName + " " + availableActions.stream()
				.map(te -> te.getKey() + "=" + fourDigitFormat.format(te.getValue())).collect(Collectors.joining(", "));
		Hero.logger.info(key + " " + val);
		pokerSimulator.setActionsData(sourceName, null, availableActions);
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
		long zeroOrPos = availableActions.stream().mapToDouble(te -> te.getValue().doubleValue()).filter(d -> d >= 0)
				.count();
		long negatives = availableActions.size() - zeroOrPos;

		// perform algorith when the ev all are negatives.
		if (negatives == availableActions.size()) {
			availableActions.add(new TEntry<String, Double>("fold", -1.0));
			availableActions.sort(null);
			// test remove extreme values
			availableActions.remove(0);
			double max = availableActions.get(0).getValue() * -1.0;
			availableActions.stream().forEach(te -> te.setValue(max + te.getValue()));
		}
	}

	/**
	 * remove all negative EV form the list of {@link #availableActions} leaving the list only with positive or 0 EV
	 * actions. If the list contains only negative values, after this method, {@link #availableActions} will be empty
	 */
	private void clearNegativeEV() {
		Vector<TEntry<String, Double>> neglist = new Vector<>();
		availableActions.forEach(te -> {
			if (te.getValue() < 0)
				neglist.add(te);
		});
		availableActions.removeAll(neglist);
	}

	private void clearEnviorement() {
		lastProbableValue = -1;
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
		Hero.logger.info("Deciding...");
		sensorsArray.read(SensorsArray.TYPE_CARDS);
		sensorsArray.read(SensorsArray.TYPE_NUMBERS);
		availableActions.clear();

		String key = "Current hand";
		String value = pokerSimulator.getMyHandHelper().getHand().toString();
		pokerSimulator.setVariable(key, value);
		Hero.logger.info(key + " " + value);

		key = "Hero hand";
		value = pokerSimulator.getMyHoleCards().getFirstCard() + ", " + pokerSimulator.getMyHoleCards().getSecondCard();
		pokerSimulator.setVariable(key, value);
		Hero.logger.info(key + " " + value);

		key = "comunity cards";
		value = pokerSimulator.getCommunityCards().toString();
		pokerSimulator.setVariable(key, value);
		Hero.logger.info(key + " " + value);

		int currentRound = pokerSimulator.getCurrentRound();
		int pot = pokerSimulator.getPotValue();
		int chips = pokerSimulator.getHeroChips();
		addOddActions("Pot", pot);

		// check the invest posibility based on heros chips. ONLY IN PREFLOP. AND ONLY ONCE.
		if (availableActions.size() == 0 && currentRound == PokerSimulator.HOLE_CARDS_DEALT)
			addOddActions("Hero chips", chips);

		// check the win probability. The tendency of the trooper to stay in the game some times make him reach higher
		// street with 0 win probability (spetialy the river) to avoid error on getSubObtimalaction, this code ensure
		// fold action only for 0 troper probability

		// TODO: this variables must be ajusted. maybe dinamicaly or based on gamerecording analisis. for now, pot to
		// 10% only to aboid drain chips in lost causes
		if (getProbability() < 0.10)
			availableActions.clear();

		// if the list of available actions are empty, the only posible action todo now is fold
		if (availableActions.size() == 0)
			availableActions.add(new TEntry<String, Double>("fold", -1.0));
	}
	/**
	 * consult the {@link PokerSimulator} and retrive the hihest probability between gobal win probability or inprove
	 * probability. When the turn card is dealed, this metod only return the win probability
	 * 
	 * @see #getOdds(int, String)
	 * @return the hights probability: win or inmprove hand
	 */
	private double getProbability() {
		MyHandStatsHelper myhsh = pokerSimulator.getMyHandStatsHelper();
		float inprove = myhsh == null ? 0 : myhsh.getTotalProb();
		float actual = pokerSimulator.getMyGameStatsHelper().getWinProb();
		float totp;
		if (pokerSimulator.getCurrentRound() == PokerSimulator.RIVER_CARD_DEALT)
			totp = actual;
		else
			totp = inprove > actual ? inprove : actual;

		if (totp != lastProbableValue) {
			String pnam = inprove > actual ? "Improve" : "Win";
			String key = "Trooper selected probability";
			String val = pnam + " " + fourDigitFormat.format(totp);
			pokerSimulator.setVariable(key, val);
			Hero.logger.info(key + "=" + val);
			lastProbableValue = totp;
		}
		return totp;
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
	 * TODO: take into account table position??
	 * 
	 * TODO: take into account numbers of active villans ??
	 * 
	 */
	private String getSubOptimalAction() {
		int elements = availableActions.size();
		double denom = availableActions.stream().mapToDouble(te -> te.getValue().doubleValue()).sum();
		int[] singletos = new int[elements];
		double[] probabilities = new double[elements];
		for (int i = 0; i < elements; i++) {
			singletos[i] = i;
			probabilities[i] = availableActions.get(i).getValue() / denom;
		}
		EnumeratedIntegerDistribution dist = new EnumeratedIntegerDistribution(singletos, probabilities);
		String selact = availableActions.get(dist.sample()).getKey();
		return selact;
	}

	/**
	 * return <code>true</code> if the herro cards are inside of the predefinde hand distributions for pre-flop
	 * 
	 */
	private boolean isGoodHand() {
		boolean ok = false;

		// suited hand
		if (pokerSimulator.getMyHoleCards().isSuited()) {
			Hero.logger.info("Hero hand: is Suited");
			ok = true;
		}

		// 10 or higher
		Card[] heroc = pokerSimulator.getMyHoleCards().getCards();
		if (heroc[0].getRank() > Card.TEN && heroc[1].getRank() > Card.TEN) {
			Hero.logger.info("Hero hand: 10 or higher");
			ok = true;
		}

		// posible straight: cernters cards separated only by 1 or 2 cards provides de best probabilities (>=6%)
		if (pokerSimulator.getMyHandStatsHelper().getStraightProb() >= 6) {
			Hero.logger.info("Hero hand: Posible straight");
			ok = true;
		}
		// is already a pair
		if (pokerSimulator.getMyHandHelper().isPocketPair()) {
			Hero.logger.info("Hero hand: is pocket pair");
			ok = true;
		}

		if (ok) {
			Hero.logger.info("the current hand is a bad hand");
		}

		return ok;
	}

	private boolean isMyTurnToPlay() {
		return sensorsArray.getScreenSensor("fold").isEnabled() || sensorsArray.getScreenSensor("call").isEnabled()
				|| sensorsArray.getScreenSensor("raise").isEnabled();
	}

	/**
	 * perform the action. At this point, the game table is waiting for the hero action.
	 * 
	 * TODO: complete documentation
	 */
	protected void act() {
		String ha = getSubOptimalAction();
//		pokerSimulator.setActionsData("Sub obtimal selection", ha, availableActions);
		if (gameRecorder != null) {
			gameRecorder.takeSnapShot(ha);
		}
		String key = "Action performed";
		pokerSimulator.setVariable(key, ha);
		// robot actuator perform the log
		robotActuator.perform(ha);
		// if my last act was fold
		if (ha.equals("fold")) {
			// setTrooperStatus(WAITING);
		}
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
				Hero.logger.info("Seconds to start: " + countdown);
				Thread.sleep(1000);
				continue;
			}

			sensorsArray.lookTable();
			// Hero.logger.info("---");

			// look the continue button and perform the action if available.
			ScreenSensor ss = sensorsArray.getScreenSensor("continue");
			if (ss.isEnabled()) {
				if (gameRecorder != null) {
					gameRecorder.flush();
				}
				gameRecorder = new GameRecorder(sensorsArray);
				robotActuator.perform("continue");
				clearEnviorement();
				// setTrooperStatus(ACTIVE);
				continue;
			}

			// // i have nothig to do expect wait to the game round complete and press the continue button because i
			// folded
			// // the cards
			// if (trooperStatus.equals(WAITING)) {
			// Thread.sleep(100);
			// continue;
			// }

			// look the standar actions buttons. this standar button indicate that the game is waiting for my move
			if (isMyTurnToPlay()) {
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
		// update the villans db table

		// 191020: ayer ya la implementacion por omision jugo una partida completa y estuvo a punto de vencer a la
		// chatarra de Texas poker - poker holdem. A punto de vencer porque jugaba tan lento que me aburri del sueno :D
	}
}
