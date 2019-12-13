package plugins.hero;

import java.io.*;
import java.util.*;

import org.apache.commons.math3.stat.descriptive.*;
import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import com.javaflair.pokerprophesier.api.card.*;
import com.javaflair.pokerprophesier.api.helper.*;

import core.*;
import core.datasource.model.*;

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
	private Vector<String> availableActions;
	private int countdown = 5;
	private File enviorement;
	private long time1;
	private DescriptiveStatistics outGameStats;
	private boolean paused = false;
	private double preflopProb = 0.20;
	private long lastGetProbabilityLog;
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
		this.availableActions = new Vector();
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
	 * Return the expectation of the pot odd against the <code>val</code> argument. This method cosider:
	 * <ul>
	 * <li>for negative <code>val</code> argument, this method will return negative expectative in order to ensure fold
	 * action
	 * <li>for pot value = 0 (initial bet) this method return 0 expectative
	 * <li>for val = 0 (check) this method return 0 expectative
	 * <li>for val < 0 (posible error) this method return negative expectative
	 * </ul>
	 * <h5>MoP page 54</h5>
	 * 
	 * @param val - cost of call/bet/raise/...
	 * @param name - name os the action. Only for logging
	 * 
	 * @return expected pot odd
	 */
	public double getPotOdds(int val, String name) {
		int pot = pokerSimulator.getPotValue();
		double totp = getProbability();

		// MoP page 54
		double poto = totp * pot - val;
		// for pot=0 (initial bet) return 0 expectaive
		poto = (pot == 0) ? 0 : poto;
		// for val=0 (check) return 0 expectaive
		poto = (val == 0) ? 0 : poto;
		// for val=-1 (posible error) return -1 expectaive
		poto = (val < 0) ? -1 : poto;

		// only log 0 or positive expectative
		if (poto >= 0)
			Hero.logger.info("Pot odd pot=" + pot + " " + name + "(" + val + ")=" + poto);
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
	private void addAction(String act) {
		// if the action is fold or the list already contain a fold action
		if (act.equals("fold") || availableActions.contains("fold")) {
			availableActions.clear();
		}
		availableActions.remove(act);
		availableActions.add(act);
	}
	/**
	 * Compute the actions available according to {@link #getPotOdds(int)} evaluations. The resulting computation will
	 * be reflected in a single (fold) or multiples (check/call, raise, ...) actions available to be randomy perform.
	 * 
	 */
	private void addPotOddActions() {
		sensorsArray.read(SensorsArray.TYPE_NUMBERS);
		int call = pokerSimulator.getCallValue();
		int raise = pokerSimulator.getRaiseValue();
		int pot = pokerSimulator.getPotValue();
		int chips = pokerSimulator.getHeroChips();

		if (getPotOdds(call, "call") >= 0) {
			addAction("call");
		}
		if (getPotOdds(raise, "raise") >= 0) {
			addAction("raise");
		}

		// TODO: temporal removed for TH because the raise.slider is in the same area
		// if (getPotOdds(pot, "pot") >= 0) {
		// addAction("raise.pot;raise");
		// }

		// TODO: temporal for TH: simulate allin
		if (getPotOdds(chips, "hero chips") >= 0) {
			addAction("raise.allin,c=10;raise");
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
				if (getPotOdds(call + (call * c), "raise.slider" + c) >= 0) {
					addAction("raise.slider,c=" + c + ";raise");
				}
			}
		}
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
		Hero.logger.info("--- Deciding... --------------------");
		sensorsArray.read(SensorsArray.TYPE_CARDS);
		int currentRound = pokerSimulator.getCurrentRound();
		if (currentRound > PokerSimulator.NO_CARDS_DEALT) {
			addAction("fold");
			addPotOddActions();
		}
	}

	/**
	 * consult the {@link PokerSimulator} and retrive the hihest probability between gobal win probability or inprove
	 * probability. This is a direct application of Rule 1. This method also override the probability acording to
	 * diferent game round.
	 * <p>
	 * in preflop actions, this method sum {@link #preflopProb} to the selected probability to allow potodd room to
	 * manouber
	 * 
	 * @return the hights probability: win or inmprove hand
	 */
	private double getProbability() {
		MyHandStatsHelper myhsh = pokerSimulator.getMyHandStatsHelper();
		float inprove = myhsh == null ? 0 : myhsh.getTotalProb();
		float actual = pokerSimulator.getMyGameStatsHelper().getWinProb();
		float totp = inprove > actual ? inprove : actual;
		String pnam = inprove > actual ? "Improve" : "Win";

		// int villns = pokerSimulator.getNumSimPlayers() - 1;
		if (pokerSimulator.getCurrentRound() == PokerSimulator.HOLE_CARDS_DEALT) {
			totp += preflopProb;
			pnam = pnam + " + preflop increase " + preflopProb;
		}
		if (System.currentTimeMillis() - lastGetProbabilityLog > 1000) {
			Hero.logger.info(pnam + " probabiliyt=" + totp);
			lastGetProbabilityLog = System.currentTimeMillis();
		}
		return totp;
	}

	/**
	 * Action have agresiveness. fold has 0 agresiveness, check 1, call 2 and so on, this allow a numeric value for the
	 * agresion. Some methods use this agresion to press the charge agains the oter players
	 */
	/**
	 * this method increase the probability of chose an agresive action. This method take into accoun the diference
	 * between the numbers of villans current active.
	 * <p>
	 * For all active villans (at the battle start) the probability of random select an action is uniform to all. At the
	 * Head usp, the probaility of chose the mosts agresive options icrease
	 * 
	 * @param options
	 * @return
	 */
	private String getRandomSelection() {
		// int vills = sensorsArray.getVillans();
		// int av = sensorsArray.getActivePlayers() - 1;
		//
		// double fact = Math.abs((av / vills) - 1) + 1;
		// EnumeratedIntegerDistribution eid = new EnumeratedIntegerDistribution(data)
		return null;
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
	 * use de actions stored in {@link #availableActions} list. At this point, the game table is waiting for the herro
	 * action.
	 * <p>
	 * this implenentation randomly select an action from the list and perfom it. if <code>fold</code> action is in the
	 * list, this bust be the only action.
	 */
	protected void act() {
		if (isMyTurnToPlay() && availableActions.size() > 0) {
			final StringBuffer actl = new StringBuffer();
			availableActions.stream().forEach((act) -> actl.append(act + ", "));
			Hero.logger.fine("Available actions to perform: " + actl.substring(0, actl.length() - 2));
			Hero.logger.info("Current hand: " + pokerSimulator.getMyHandHelper().getHand().toString());
			String ha = "";
			if (availableActions.size() == 1) {
				ha = availableActions.elementAt(0);
			} else {
				double rnd = Math.random();
				int r = (int) (rnd * availableActions.size());
				ha = availableActions.elementAt(r);
			}
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
		Alesia.openDB();

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
			Hero.logger.info("---");

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
