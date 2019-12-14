package plugins.hero;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.stat.descriptive.*;
import org.jdesktop.application.*;

import com.javaflair.pokerprophesier.api.card.*;
import com.javaflair.pokerprophesier.api.helper.*;

import core.*;

/**
 * this class represent the core of al hero plugins. As a core class, this class dependes of anothers classes in order
 * to build a useful player agent. the followin is a list of class from where the information is retrived, and the
 * actions are performed.
 * <ul>
 * <li>{@link PokerSimulator} - get the numerical values for decition making
 * <li>{@link RobotActuator} - permorm the action sended by this class.
 * 
 * *
 * <li>{@link SensorsArray} - perform visual operation of the enviorement
 * 
 * <p>
 * RULES:
 * <ol>
 * <li>I came here to win money, and for win money i need to stay in the game. for that i choose the hihgest available
 * probability. it can be from inprove probability or from the global winning probability
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

	private PokerSimulator pokerSimulator;
	private RobotActuator robotActuator;
	private SensorsArray sensorsArray;
	private String trooperStatus;
	private boolean isTestMode;
	private Hashtable<String, Double> availableActions;
	private int countdown = 5;
	private File enviorement;
	private long time1;
	private DescriptiveStatistics outGameStats;
	private boolean paused = false;
	private double lastProbableValue;
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
		availableActions = new Hashtable<>();
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
		ArrayList<Double> vals = new ArrayList<>(availableActions.values());
		int elements = vals.size();
		double denom = vals.stream().mapToDouble(Double::doubleValue).sum();
		int[] singletos = new int[elements];
		double[] probabilities = new double[elements];
		for (int i = 0; i < elements; i++) {
			singletos[i] = i;
			probabilities[i] = vals.get(i) / denom;
		}
		EnumeratedIntegerDistribution dist = new EnumeratedIntegerDistribution(singletos, probabilities);
		ArrayList<String> keys = new ArrayList<>(availableActions.keySet());
		String selact = keys.get(dist.sample());
		return selact;
	}
	/**
	 * Return the expectation of the pot odd against the <code>val</code> argument. to comply with rule 1, this method
	 * perform:
	 * <ul>
	 * <li>for negative <code>val</code> argument, this method will return negative expectative in order to ensure fold
	 * action. this is a safe insurance agains posible read errors.
	 * <li>for val = 0 (check) this method return 0 expectative
	 * </ul>
	 * <h5>MoP page 54</h5>
	 * 
	 * @param val - cost of call/bet/raise/...
	 * @param name - name os the action. Only for logging
	 * 
	 * @return expected utility for the passed argument
	 */
	public double getPotOdds(int val, String name) {
		int pot = pokerSimulator.getPotValue();
		double totp = getProbability();

		// MoP page 54
		double poto = totp * pot - val;
		// for pot=0 (initial bet) return 0 expectaive
		// poto = (pot == 0) ? 0 : poto;
		// for val=0 (check) return 0 expectaive
		poto = (val == 0) ? 0 : poto;
		// for val=-1 (posible error) return -1 expectaive
		poto = (val < 0) ? -1 : poto;

		// only log 0 or positive expectative
		if (poto >= 0)
			Hero.logger.info("Pot odd pot=" + pot + " " + name + "(" + val + ")=" + fourDigitFormat.format(poto));
		return poto;
	}
	private static DecimalFormat fourDigitFormat = new DecimalFormat("#0.0000");
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
	 * add the action to the list of available actions. This metodh ensure:
	 * <ul>
	 * <li>the <code>fold</code> action is the only action on the list.
	 * <li>only distict actions are present in the list.</li>
	 * 
	 * @param act - action
	 */
	private void addAction(String act, double utility) {
		// if the action is fold or the list already contain a fold action
		if (act.equals("fold") || availableActions.contains("fold")) {
			availableActions.clear();
		}
		availableActions.remove(act);
		availableActions.put(act, utility);
	}
	/**
	 * Compute the actions available according to {@link #getPotOdds(int, String)} evaluations. The resulting
	 * computation will be reflected in a single (fold) or multiples (check/call, raise, ...) actions available to be
	 * randomy perform.
	 * <p>
	 * for initial bet (pot = 0), the normal potodd equation return negative espectative, in this case, the trooper will
	 * fold his hand. to avoid this, for initial bet, translate all negatives values to the positive contepart changing
	 * the sigh and shift the values to reflect the inverse of the retrived value. this allow
	 * {@link #getSubOptimalAction()} select the action for this espetial case.
	 * 
	 * TODO: the extreme values: fold=-1 and allin=x must be agree whit mathematical poker model to allow bluff.
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
	 */
	private void addPotOddActions() {
		sensorsArray.read(SensorsArray.TYPE_NUMBERS);
		int call = pokerSimulator.getCallValue();
		int raise = pokerSimulator.getRaiseValue();
		int pot = pokerSimulator.getPotValue();
		int chips = pokerSimulator.getHeroChips();
		double utility = 0.0;

		utility = getPotOdds(call, "call");
		if (utility >= 0) {
			addAction("call", utility);
		}
		utility = getPotOdds(raise, "raise");
		if (utility >= 0) {
			addAction("raise", utility);
		}

		// TODO: temporal removed for TH because the raise.slider is in the same area
		// if (getPotOdds(pot, "pot") >= 0) {
		// addAction("raise.pot;raise");
		// }

		// TODO: temporal for TH: simulate allin
		utility = getPotOdds(chips, "all in");
		if (utility >= 0) {
			addAction("raise.allin,c=10;raise", utility);
		}

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
				utility = getPotOdds(call + (call * c), "raise.slider" + c);
				if (utility >= 0) {
					addAction("raise.slider,c=" + c + ";raise", utility);
				}
			}
		}

		// perform algorith for pot = 0. see method documentation
		if (pot == 0) {
			addAction("fold", -1);
			// check all values must be negative values
			Collection<Double> orgVals = availableActions.values();
			long positives = orgVals.stream().mapToDouble(Double::doubleValue).filter(d -> d > 0).count();
			if (positives > 0)
				Hero.logger.severe("Positive EV found for pot =0. this is a error !?!?!?!");

			ArrayList<Double> newlst = new ArrayList<>();
			orgVals.stream().forEach(dv -> newlst.add(new Double(dv.doubleValue() * -1.0)));
			Collections.rotate(newlst, newlst.size());
			orgVals.clear();
			newlst.forEach(dv -> orgVals.add(dv));
		}

		// if the list of available actions are empty, the only posible action todo now is fold
		if (availableActions.size() == 0)
			addAction("fold", -1);

	}
	private void clearEnviorement() {
		sensorsArray.init();
		// at first time execution, a standar time of 10 second is used
		long tt = time1 == 0 ? 10000 : System.currentTimeMillis() - time1;
		outGameStats.addValue(tt);
		time1 = System.currentTimeMillis();
		Hero.logger.fine("Game play time average=" + TStringUtils.formatSpeed((long) outGameStats.getMean()));
	}
	/**
	 * decide de action(s) to perform. This method is called when the {@link Trooper} detect that is my turn to play. At
	 * this point, the game enviorement is waiting for an accion. This method must report all posible actions using the
	 * {@link #addAction(String)} method
	 */
	private void decide() {
		Hero.logger.info("Deciding...");
		sensorsArray.read(SensorsArray.TYPE_CARDS);
		int currentRound = pokerSimulator.getCurrentRound();
		if (currentRound > PokerSimulator.NO_CARDS_DEALT) {
			addPotOddActions();
		}
	}

	/**
	 * consult the {@link PokerSimulator} and retrive the hihest probability between gobal win probability or inprove
	 * probability. This is a direct application of Rule 1. This probability is used in {@link #getPotOdds(int, String)}
	 * function
	 * 
	 * @return the hights probability: win or inmprove hand
	 */
	private double getProbability() {
		MyHandStatsHelper myhsh = pokerSimulator.getMyHandStatsHelper();
		float inprove = myhsh == null ? 0 : myhsh.getTotalProb();
		float actual = pokerSimulator.getMyGameStatsHelper().getWinProb();
		float totp = inprove > actual ? inprove : actual;
		String pnam = inprove > actual ? "Improve" : "Win";

		if (totp != lastProbableValue) {
			Hero.logger.info(pnam + " probabiliyt=" + totp);
			lastProbableValue = totp;
		}
		return totp;
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
		if (isMyTurnToPlay() && availableActions.size() > 0) {
			Hero.logger.fine("Available actions to perform: " + availableActions.keySet().toString());
			Hero.logger.info("Current hand: " + pokerSimulator.getMyHandHelper().getHand().toString());
			String ha = getSubOptimalAction();
			if (gameRecorder != null) {
				gameRecorder.takeSnapShot(ha);
			}
			robotActuator.perform(ha);
			// if my last act was fold
			if (ha.equals("fold")) {
				// setTrooperStatus(WAITING);
			}
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

			// look the standar actions buttons. this standar button indicate that the game is waiting for my play
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
