package plugins.hero;

import java.awt.*;
import java.text.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import java.util.stream.*;

import javax.swing.*;

import com.alee.laf.combobox.*;
import com.alee.managers.settings.*;
import com.javaflair.pokerprophesier.api.adapter.*;
import com.javaflair.pokerprophesier.api.card.*;
import com.javaflair.pokerprophesier.api.exception.*;
import com.javaflair.pokerprophesier.api.helper.*;
import com.jgoodies.common.base.*;

import core.*;
import gui.*;

/**
 * 
 * Link betwen hero and PokerProthsis library. This Class perform th simulation and store all result for futher use.
 * this is for <I>Me vs Opponents</I> simulator.
 * <p>
 * this its the class that contain all nesesary information for desicion making and is populated bby the class
 * {@link SensorsArray}
 * 
 */
public class PokerSimulator {

	// same values as PokerProphesierAdapter + no_card_deal status
	public static final int NO_CARDS_DEALT = 0;
	public static final int HOLE_CARDS_DEALT = 1;
	public static final int FLOP_CARDS_DEALT = 2;
	public static final int TURN_CARD_DEALT = 3;
	public static final int RIVER_CARD_DEALT = 4;

	public static int SMALL_BLIND = 1;
	public static int BIG_BLIND = 2;
	public static int MIDLE = 3;
	public static int DEALER = 10;
	public static String STATUS = "Simulator Status";
	public static String STATUS_OK = "Ok";
	private static DecimalFormat fourDigitFormat = new DecimalFormat("#0.0000");

	public static double probabilityThreshold = 0.50;
	// Number of simulations, total players
	private int numSimulations = 100000;
	// temporal storage for incoming cards
	private Hashtable<String, String> cardsBuffer;
	// number of players
	private int numSimPlayers;
	private TreeMap<String, Object> variableList;
	private PokerProphesierAdapter adapter;
	private double callValue, raiseValue, potValue;
	private CommunityCards communityCards;
	private JLabel reportJLabel;
	private TUIPanel reportPanel;
	private int currentRound;
	private HoleCards holeCards;
	private int tablePosition;
	private WebComboBox helperFilterComboBox;
	private MyHandHelper myHandHelper;
	private MyHandStatsHelper myHandStatsHelper;
	private MyGameStatsHelper myGameStatsHelper;

	private double heroChips;
	private double smallBlind;
	private double bigBlind;

	private ActionsBarChart actionsBarChart;
	private long lastStepMillis;
	private double bestProbability;
	public PokerSimulator() {
		this.cardsBuffer = new Hashtable<String, String>();
		// Create an adapter to communicate with the simulator
		this.adapter = new PokerProphesierAdapter();
		adapter.setNumSimulations(numSimulations);
		variableList = new TreeMap<>();

		// information components
		helperFilterComboBox = new WebComboBox();
		helperFilterComboBox.addItem(new TEntry("MyHandHelper", "My hand"));
		helperFilterComboBox.addItem(new TEntry("MyHandStatsHelper", "My hand statistics"));
		helperFilterComboBox.addItem(new TEntry("MyOutsHelper", "My outs"));
		helperFilterComboBox.addItem(new TEntry("OppHandStatsHelper", "Oponent Hand"));
		helperFilterComboBox.addItem(new TEntry("MyGameStatsHelper", "Game statistic"));
		helperFilterComboBox.addItem(new TEntry("trooperVariables", "Trooper variables"));
		// helperFilterComboBox.addActionListener(evt -> filterSensors());
		helperFilterComboBox.registerSettings(new Configuration<ComboBoxState>("PokerSimulator.HelperFilter"));

		this.reportPanel = new TUIPanel();
		reportPanel.showAditionalInformation(false);
		reportPanel.getToolBarPanel().add(helperFilterComboBox);
		this.reportJLabel = new JLabel();
		reportJLabel.setVerticalAlignment(JLabel.TOP);
		reportJLabel.setFont(new Font("courier new", Font.PLAIN, 12));
		this.actionsBarChart = new ActionsBarChart();
		JPanel jp = new JPanel(new BorderLayout());
		jp.add(reportJLabel, BorderLayout.CENTER);
		jp.add(actionsBarChart.getChartPanel(), BorderLayout.SOUTH);
		// reportPanel.setBodyComponent(new JScrollPane(reportJLabel));
		reportPanel.setBodyComponent(jp);
		setBlinds(50, 100);

		init();
	}

	public void cleanReport() {
		variableList.keySet().forEach(key -> variableList.put(key, ""));
		variableList.put(STATUS, STATUS_OK);
		actionsBarChart.setDataSet(null);
		updateReport();
	}

	/**
	 * this mathod act like a buffer betwen {@link SensorsArray} and this class to set the cards based on the name/value
	 * of the {@link ScreenSensor} component while the cards arrive at the game table. For example durin a reading
	 * operation the card are retrived without order. this method store the incomming cards and the run simulation
	 * method will create the correct game status based on the card stored
	 * 
	 * @param sName - name of the {@link ScreenSensor}
	 * @param card - ocr retrived from the sensor. public void addCard(String sName, String card) {
	 *        cardsBuffer.put(sName, card); // System.out.println(sName + " " + card); }
	 */
	public PokerProphesierAdapter getAdapter() {
		return adapter;
	}

	public double getBestProbability() {
		return bestProbability;
	}

	public double getBigBlind() {
		return bigBlind;
	}

	public double getCallValue() {
		return callValue;
	}

	public Hashtable<String, String> getCardsBuffer() {
		return cardsBuffer;
	}

	public CommunityCards getCommunityCards() {
		return communityCards;
	}

	public int getCurrentRound() {
		return currentRound;
	}

	public double getHeroChips() {
		return heroChips;
	}
	public MyGameStatsHelper getMyGameStatsHelper() {
		return myGameStatsHelper;
	}

	public MyHandHelper getMyHandHelper() {
		return myHandHelper;
	}
	public MyHandStatsHelper getMyHandStatsHelper() {
		return myHandStatsHelper;
	}

	public HoleCards getMyHoleCards() {
		return holeCards;
	}
	public int getNumSimPlayers() {
		return numSimPlayers;
	}

	public double getPotValue() {
		return potValue;
	}

	public double getRaiseValue() {
		return raiseValue;
	}

	/**
	 * Return the information component whit all values computesd form simulations and game status
	 * 
	 * @return information component
	 */
	public JComponent getReportPanel() {
		return reportPanel;
	}

	public double getSmallBlind() {
		return smallBlind;
	}
	public int getTablePosition() {
		return tablePosition;
	}

	public TreeMap<String, Object> getVariables() {
		return variableList;
	}
	/**
	 * Init the simulation eviorement. Use this metod to clear al component in case of error or start/stop event
	 * 
	 */
	public void init() {
		this.currentRound = NO_CARDS_DEALT;
		this.numSimPlayers = -1;
		holeCards = null;
		communityCards = null;
		// variableList.clear();
		// 190831: ya el sistema se esta moviendo. por lo menos hace fold !!!! :D estoy en el salon de clases del campo
		// de refujiados en dresden !!!! ya van 2 meses
		cardsBuffer.clear();
		potValue = -1;
		tablePosition = -1;
		// smallBlind = -1;
		// bigBlind = -1;
		callValue = -1;
		raiseValue = -1;
		heroChips = -1;
		cleanReport();
	}
	/**
	 * perform the PokerProphesier simulation. Call this method when all the cards on the table has been setted using
	 * {@link #addCard(String, String)} this method will create the {@link HoleCards} and the {@link CommunityCards} (if
	 * is available). After the simulation, the adapters are updated and can be consulted and the report are up to date
	 * 
	 */
	public void runSimulation() {
		try {
			variableList.put(STATUS, "Runing ...");
			updateReport();
			// Set the simulator parameters

			// TODO: check this parameters. maybe is better set off or change it during the game play because not all
			// the
			// time are true. for example, in a 6 villans pre flop game, i can.t assume set opp hole card realiytic is
			// false, but in th turn, if a villan still in the battle, is set to true because maybe he got something
			//
			// or use this info comparing with the gameplayer history !!!!!!!!!!!!!
			adapter.setMyOutsHoleCardSensitive(true);
			adapter.setOppHoleCardsRealistic(true);
			adapter.setOppProbMyHandSensitive(true);

			// String c1 = cardsBuffer.get("hero.card1");
			// String c2 = cardsBuffer.get("hero.card2");
			// if (c1 == null || c2 == null) {
			// JOptionPane.showMessageDialog(Alesia.mainFrame, "error");
			// Trooper.getInstance().cancel(true);
			// }
			createHoleCards();
			createComunityCards();

			adapter.runMySimulations(holeCards, communityCards, numSimPlayers, currentRound);
			myGameStatsHelper = adapter.getMyGameStatsHelper();
			myHandStatsHelper = adapter.getMyHandStatsHelper();
			myHandHelper = adapter.getMyHandHelper();
			updateSimulationResults();
			variableList.put(STATUS, STATUS_OK);
			updateReport();
		} catch (SimulatorException e) {
			setVariable(STATUS, e.getClass().getSimpleName() + e.getMessage());
			Hero.logger.warning(e.getMessage() + "\n\tCurrent round: " + currentRound + "\n\tHole cards: " + holeCards
					+ "\n\tComunity cards: " + communityCards);
		} catch (Exception e) {
			setVariable(STATUS, e.getClass().getSimpleName() + " " + e.getMessage());
			Hero.logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	public void setActionsData(String aperformed, Vector<TEntry<String, Double>> actions) {
		actionsBarChart.setCategoryMarker(aperformed);
		actionsBarChart.setDataSet(actions);
		updateReport();
	}

	public void setActionsData(Vector<TEntry<String, Double>> actions) {
		actionsBarChart.setDataSet(actions);
		updateReport();
	}
	public void setBlinds(int sb, int bb) {
		this.smallBlind = sb;
		this.bigBlind = bb;
	}
	public void setCallValue(double callValue) {
		this.callValue = callValue;
	}
	public void setHeroChips(double heroChips) {
		this.heroChips = heroChips;
	}

	public void setNunOfPlayers(int p) {
		this.numSimPlayers = p;
	}

	public void setPotValue(double potValue) {
		this.potValue = potValue;
	}

	public void setRaiseValue(double raiseValue) {
		this.raiseValue = raiseValue;
	}
	public void setTablePosition(int tp) {
		this.tablePosition = tp;
	}
	public void setVariable(String key, Object value) {
		// format double values
		Object value1 = value;
		if (value instanceof Double)
			value1 = fourDigitFormat.format(((Double) value).doubleValue());
		variableList.put(key, value1);
		if (Trooper.STATUS.equals(key)) {
			// variableList.put("trooper.Performance Step time", (System.currentTimeMillis() - lastStepMillis));
			lastStepMillis = System.currentTimeMillis();
		}
		// mandatori. i nedd to see what is happening
		updateReport();
	}
	public void updateReport() {
		Predicate<String> valgt0 = new Predicate<String>() {
			@Override
			public boolean test(String t) {
				double d = new Double(t);
				return d > 0;
			}
		};
		// long t1 = System.currentTimeMillis();
		String selectedHelper = ((TEntry) helperFilterComboBox.getSelectedItem()).getKey().toString();
		String text = "<html>";
		if (myHandHelper != null && selectedHelper.equals("MyHandHelper")) {
			text += getFormateTable(myHandHelper.toString());
		}
		if (myHandStatsHelper != null && selectedHelper.equals("MyHandStatsHelper")) {
			String tmp = myHandStatsHelper.toString();
			tmp = tmp.replaceFirst("[=]", ":");
			text += getFormateTable(tmp, valgt0);
		}
		MyOutsHelper myOutsHelper = adapter.getMyOutsHelper();
		if (myOutsHelper != null && selectedHelper.equals("MyOutsHelper")) {
			text += getFormateTable(myOutsHelper.toString(), str -> !Strings.isBlank(str));
		}
		OppHandStatsHelper oppHandStatsHelper = adapter.getOppHandStatsHelper();
		if (oppHandStatsHelper != null && selectedHelper.equals("OppHandStatsHelper")) {
			text += getFormateTable(oppHandStatsHelper.toString(), valgt0);
		}
		if (myGameStatsHelper != null && selectedHelper.equals("MyGameStatsHelper")) {
			text += getFormateTable(myGameStatsHelper.toString());
		}
		if (selectedHelper.equals("trooperVariables")) {
			String tmp = variableList.keySet().stream().map(key -> key + ": " + variableList.get(key))
					.collect(Collectors.joining("\n"));

			// remove the group heather. just for visual purporse
			tmp = tmp.replace("sensorArray.", "");
			tmp = tmp.replace("simulator.ammount.", "");
			tmp = tmp.replace("simulator.", "");
			tmp = tmp.replace("trooper.", "");
			text += getFormateTable(tmp);
		}

		text += "</html>";
		reportJLabel.setText(text);
		reportJLabel.repaint();
		// Hero.logger.severe("updateMyOutsHelperInfo(): " + (System.currentTimeMillis() - t1));
	}

	/**
	 * Create and return an {@link Card} based on the string representation. this method return <code>null</code> if the
	 * string representation is not correct.
	 * 
	 * @param scard - Standar string representation of a card
	 * @return Card
	 */
	private Card createCardFromString(String card) {
		Card car = null;
		int suit = -1;
		int rank = -1;
		String scard = new String(card);

		String srank = scard.substring(0, 1).toUpperCase();
		rank = srank.equals("A") ? Card.ACE : rank;
		rank = srank.equals("K") ? Card.KING : rank;
		rank = srank.equals("Q") ? Card.QUEEN : rank;
		rank = srank.equals("J") ? Card.JACK : rank;
		rank = srank.equals("T") ? Card.TEN : rank;
		if (scard.startsWith("10")) {
			rank = Card.TEN;
			scard = scard.substring(1);
		}
		rank = scard.startsWith("9") ? Card.NINE : rank;
		rank = scard.startsWith("8") ? Card.EIGHT : rank;
		rank = scard.startsWith("7") ? Card.SEVEN : rank;
		rank = scard.startsWith("6") ? Card.SIX : rank;
		rank = scard.startsWith("5") ? Card.FIVE : rank;
		rank = scard.startsWith("4") ? Card.FOUR : rank;
		rank = scard.startsWith("3") ? Card.THREE : rank;
		rank = scard.startsWith("2") ? Card.TWO : rank;

		// remove rank
		scard = scard.substring(1).toLowerCase();

		suit = scard.startsWith("s") ? Card.SPADES : suit;
		suit = scard.startsWith("c") ? Card.CLUBS : suit;
		suit = scard.startsWith("d") ? Card.DIAMONDS : suit;
		suit = scard.startsWith("h") ? Card.HEARTS : suit;

		if (rank > 0 && suit > 0)
			car = new Card(rank, suit);
		else
			Hero.logger.warning("String " + card + " for card representation incorrect. Card not created");

		return car;
	}

	/**
	 * create the comunity cards. This method also set the currnet round of the game based on length of the
	 * <code>cards</code> parameter.
	 * <ul>
	 * <li>3 for {@link PokerSimulator#FLOP_CARDS_DEALT}
	 * <li>4 for {@link PokerSimulator#TURN}
	 * <li>5 for {@link PokerSimulator#RIVER}
	 * </ul>
	 */
	private void createComunityCards() {
		ArrayList<String> list = new ArrayList<>();
		if (cardsBuffer.containsKey("flop1"))
			list.add(cardsBuffer.get("flop1"));
		if (cardsBuffer.containsKey("flop2"))
			list.add(cardsBuffer.get("flop2"));
		if (cardsBuffer.containsKey("flop3"))
			list.add(cardsBuffer.get("flop3"));
		if (cardsBuffer.containsKey("turn"))
			list.add(cardsBuffer.get("turn"));
		if (cardsBuffer.containsKey("river"))
			list.add(cardsBuffer.get("river"));

		Card[] ccars = new Card[list.size()];
		for (int i = 0; i < ccars.length; i++) {
			ccars[i] = createCardFromString(list.get(i));
		}
		communityCards = new CommunityCards(ccars);

		// set current round
		currentRound = ccars.length == 3 ? FLOP_CARDS_DEALT : currentRound;
		currentRound = ccars.length == 4 ? TURN_CARD_DEALT : currentRound;
		currentRound = ccars.length == 5 ? RIVER_CARD_DEALT : currentRound;
	}
	/**
	 * Create my cards
	 * 
	 * @param c1 - String representation of card 1
	 * @param c2 - String representation of card 2
	 */
	private void createHoleCards() {
		String c1 = cardsBuffer.get("hero.card1");
		String c2 = cardsBuffer.get("hero.card2");
		Card ca1 = createCardFromString(c1);
		Card ca2 = createCardFromString(c2);
		holeCards = new HoleCards(ca1, ca2);
		currentRound = HOLE_CARDS_DEALT;
	}

	private String getFormateTable(String helperString) {
		return getFormateTable(helperString, s -> true);
	}
	/**
	 * return a HTML table based on the <code>helperString</code> argument. the <code>only</code> paratemeter indicate a
	 * filter of elemenst. If any line form helperstring argument star with a word form this list, the line is include
	 * in the result. an empty list for this parametr means all elemenst
	 * 
	 * @param helperString - string come form any {@link PokerProphesierAdapter} helper class
	 * @param only - list of filter words or empty list
	 * 
	 * @return HTML table
	 */
	private String getFormateTable(String helperString, Predicate<String> valueFilter) {
		String[] hslines = helperString.split("\n");
		String res = "";
		for (String lin : hslines) {
			final String[] k_v = lin.split("[:]");
			if (valueFilter.test(k_v[1])) {
				lin = "<tr><td>" + lin + "</td></tr>";
				res += lin.replaceAll(": ", "</td><td>");
			}
		}
		return "<table>" + res + "</table>";
	}

	private void log() {
		Hero.logger.info("Num of players: " + getNumSimPlayers() + " prob for each player: "
				+ fourDigitFormat.format(1.0 / getNumSimPlayers()));
		Hero.logger.info("Troper probability: " + variableList.get("simulator.Troper probability"));
		Hero.logger.info("Trooper Current hand: " + variableList.get("simulator.Trooper Current hand"));
	}

	/**
	 * update the simulation result to the console
	 */
	private void updateSimulationResults() {
		// TODO: temporal check working only whit win probability. inprove probability was before to the preflopaction
		// metods
		// float inprove = myHandStatsHelper == null ? 0 : myHandStatsHelper.getTotalProb();
		float inprove = -1;
		float actual = myGameStatsHelper == null ? 0 : myGameStatsHelper.getWinProb() + myGameStatsHelper.getTieProb();

		// float actual = getMyGameStatsHelper().getWinProb();
		if (getCurrentRound() == PokerSimulator.RIVER_CARD_DEALT)
			bestProbability = actual;
		else
			bestProbability = inprove > actual ? inprove : actual;

		String pnam = inprove > actual ? "Improve" : "Win";
		variableList.put("simulator.Troper probability", pnam + " " + fourDigitFormat.format(bestProbability));
		variableList.put("simulator.Trooper Current hand", getMyHandHelper().getHand().toString());
		variableList.put("simulator.Trooper Hole hand",
				getMyHoleCards().getFirstCard() + ", " + getMyHoleCards().getSecondCard());
		variableList.put("simulator.Troper Comunity cards", getCommunityCards().toString());
		// variableList.put("Table Position", getTablePosition());
		variableList.put("simulator.Current round", getCurrentRound());
		variableList.put("simulator.ammount.Call", getCallValue());
		variableList.put("simulator.ammount.Raise", getRaiseValue());
		variableList.put("simulator.ammount.Pot", getPotValue());
		variableList.put("simulator.Num of players", getNumSimPlayers());
		variableList.put("simulator.ammount.blinds", getSmallBlind() + "/" + getBigBlind());
		log();
	}
}
