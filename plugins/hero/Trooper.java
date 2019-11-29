package plugins.hero;

import java.io.*;
import java.util.*;

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
 * 
 * @author terry
 *
 */
public class Trooper extends Task {

	private static Trooper instance;
	private PokerSimulator pokerSimulator;
	private RobotActuator robotActuator;
	private SensorsArray sensorsArray;

	private boolean isTestMode;
	private Vector<String> availableActions;
	private int countdown = 5;
	private File enviorement;
	private long time1;
	private DescriptiveStatistics outGameStats;

	public Trooper() {
		this(null);
	}
	public Trooper(Trooper clone) {
		super(Alesia.getInstance());
		this.robotActuator = new RobotActuator();
		this.availableActions = new Vector();
		this.outGameStats = new DescriptiveStatistics(10);

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
	public void clearEnviorement() {
		sensorsArray.init();

		// at first time execution, a standar time of 10 second is used
		long tt = time1 == 0 ? 10000 : System.currentTimeMillis() - time1;
		outGameStats.addValue(tt);
		time1 = System.currentTimeMillis();
		Hero.logger.info("Game play time average=" + TStringUtils.formatSpeed((long) outGameStats.getMean()));
	}

	/**
	 * Return the expectation of the pot odd against the <code>val</code> argument. This method cosider:
	 * <ul>
	 * <li>for negative <code>val</code> argument, this method will return negative expectative in order to ensure fold
	 * action
	 * <li>for pot value = 0 (initial bet) this method return 0 expectative
	 * <li>for val = 0 (check) this method return 0 expectative
	 * </ul>
	 * <h5>MoP page 54</h5>
	 * 
	 * @param pot - current pot amount
	 * @param val - cost of call/bet
	 * 
	 * @return expected pot odd
	 */
	public double getPotOdds(int val, String name) {
		int pot = pokerSimulator.getPotValue() + getVillansCall();

		/*
		 * rule: i came here to win money, and for win money i need to stay in the game. for that i choose the hihgest
		 * available probability. it can be from inprove probability or from the global winning probability
		 */
		MyHandStatsHelper myhsh = pokerSimulator.getMyHandStatsHelper();
		float inprove = myhsh == null ? 0 : myhsh.getTotalProb();
		float actual = pokerSimulator.getMyGameStatsHelper().getWinProb();
		float totp = inprove > actual ? inprove : actual;
		String pnam = inprove > actual ? "improve" : "win";

		// MoP page 54
		double poto = totp * pot - val;
		// for pot=0 (initial bet) return 0 expectaive
		poto = (pot == 0) ? 0 : poto;
		// for val=0 (check) return 0 expectaive
		poto = (val == 0) ? 0 : poto;
		// for val=-1 (posible error) return -1 expectaive
		poto = (val < 0) ? -1 : poto;
		Hero.logger.info(pnam + " prob=" + totp + " pot=" + pot + " value=" + val + " potodd for " + name + "=" + poto);
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

	public void setTestMode(boolean isTestMode) {
		this.isTestMode = isTestMode;
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
		sensorsArray.read("pot", "call", "raise", "hero.chips");
		int call = pokerSimulator.getCallValue();
		int raise = pokerSimulator.getRaiseValue();
		int pot = pokerSimulator.getPotValue() + getVillansCall();
		int chips = pokerSimulator.getHeroChips();

		if (getPotOdds(call, "call") >= 0) {
			addAction("call");
		}
		if (getPotOdds(raise, "raise") >= 0) {
			addAction("raise");
		}
		if (getPotOdds(pot, "pot") >= 0) {
			addAction("raise.pot;raise");
		}
		if (getPotOdds(chips, "hero chips") >= 0) {
			addAction("raise.allin;raise");
		}
	}

	/**
	 * decide de action(s) to perform. This method is called when the {@link Trooper} detect that is my turn to play. At
	 * this point, the game enviorement is waiting for an accion. This method must report all posible actions using the
	 * {@link #addAction(String)} method
	 */
	private void decide() {
		sensorsArray.read("hero.card1", "hero.card2", "flop1", "flop2", "flop3", "turn", "river");
		if (pokerSimulator.getCurrentRound() > PokerSimulator.NO_CARDS_DEALT) {
			addAction("fold");
			addPotOddActions();
		}
	}
	/**
	 * Temporal: return the sum of all villans call. used to retrive the exact amount of pot
	 * 
	 * @return
	 */
	private int getVillansCall() {
		sensorsArray.read("villan1.call", "villan2.call", "villan3.call", "villan4.call");
		int villanscall = 0;
		String val = "";
		try {
			val = sensorsArray.getScreenSensor("villan1.call").getOCR();
			villanscall += Integer.parseInt(val == null ? "0" : val);
			val = sensorsArray.getScreenSensor("villan2.call").getOCR();
			villanscall += Integer.parseInt(val == null ? "0" : val);
			val = sensorsArray.getScreenSensor("villan3.call").getOCR();
			villanscall += Integer.parseInt(val == null ? "0" : val);
			val = sensorsArray.getScreenSensor("villan4.call").getOCR();
			villanscall += Integer.parseInt(val == null ? "0" : val);
		} catch (Exception e) {
			System.err.println("Error setting villans call values.");
		}

		return villanscall;
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
	 * set the enviorement. this method create a new enviorement discarting all previous created objects
	 * 
	 */
	public void setEnviorement(File file) {
		ScreenAreas sDisp = new ScreenAreas(file);
		sDisp.read();
		this.enviorement = file;
		sensorsArray.createSensorsArray(sDisp);
		robotActuator.setEnviorement(sDisp);

		Hero.sensorsPanel.setEnviorement(this);

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
			Hero.logger.info("Available actions to perform: " + actl.substring(0, actl.length() - 2));
			Hero.logger.info("Current hand: " + pokerSimulator.getMyHandHelper().getHand().toString());
			if (availableActions.size() == 1) {
				robotActuator.perform(availableActions.elementAt(0));
			} else {
				double rnd = Math.random();
				int r = (int) (rnd * availableActions.size());
				robotActuator.perform(availableActions.elementAt(r));
			}
		}
	}

	@Override
	protected Object doInBackground() throws Exception {

		while (!isCancelled()) {

			// countdown before start
			if (countdown > 0) {
				countdown--;

				Hero.logger.info("Seconds to start: " + countdown);
				Thread.sleep(1000);
				continue;
			}

			sensorsArray.lookTable();

			// look the continue button and perform the action if available.
			// sensorsArray.lookTable("continue");
			ScreenSensor ss = sensorsArray.getScreenSensor("continue");
			if (ss.isEnabled()) {
				robotActuator.perform("continue");
				clearEnviorement();
				continue;
			}

			// look the standar actions buttons. this standar button indicate that the game is waiting for my play
			// sensorsArray.lookTable("fold", "call", "raise");
			if (isMyTurnToPlay()) {
				Hero.logger.info("Deciding ...");
				decide();
				Hero.logger.info("-------------------");
				Hero.logger.info("Acting ...");
				act();
				Hero.logger.info("-------------------");
			}
			Hero.logger.info("Thinkin ...");
			think();
			Hero.logger.info("-------------------");

			// check simulator status: in case of any error, try to clean the simulator and wait for the next cycle
			if (pokerSimulator.getException() != null) {
				// clearEnviorement();
				// pokerSimulator.init();
				// continue;
			}
		}
		return null;
	}

	/**
	 * This method is invoked during the idle phase (after {@link #act()} and before {@link #decide()}. use this method
	 * to perform large computations.
	 */
	protected void think() {
		// read all the table only for warm up the sensors hoping that many of then will not change in the near future
		// sensorsArray.read();
		// 191020: ayer ya la implementacion por omision jugo una partida completa y estuvo a punto de vencer a la
		// chatarra de Texas poker - poker holdem. A punto de vencer porque jugaba tan lento que me aburri del sueno :D
	}
}
