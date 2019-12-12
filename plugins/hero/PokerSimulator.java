package plugins.hero;

import java.util.*;

import javax.swing.*;

import com.alee.laf.combobox.*;
import com.alee.managers.settings.*;
import com.javaflair.pokerprophesier.api.adapter.*;
import com.javaflair.pokerprophesier.api.card.*;
import com.javaflair.pokerprophesier.api.helper.*;

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
	// Number of simulations, total players
	private int numSimulations = 100000;
	// temporal storage for incoming cards
	private Hashtable<String, String> cardsBuffer;
	// number of players
	private int numSimPlayers = 5;

	private PokerProphesierAdapter adapter;
	private int callValue, raiseValue, potValue;
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
	private int heroChips = 0;
	private int smallBlind;
	private int bigBlind;

	public PokerSimulator() {
		this.cardsBuffer = new Hashtable<String, String>();
		// Create an adapter to communicate with the simulator
		this.adapter = new PokerProphesierAdapter();
		adapter.setNumSimulations(numSimulations);

		// information components
		helperFilterComboBox = new WebComboBox();
		helperFilterComboBox.addItem(new TEntry("MyHandHelper", "My hand"));
		helperFilterComboBox.addItem(new TEntry("MyHandStatsHelper", "My hand statistics"));
		helperFilterComboBox.addItem(new TEntry("MyOutsHelper", "My outs"));
		helperFilterComboBox.addItem(new TEntry("OppHandStatsHelper", "Oponent Hand"));
		helperFilterComboBox.addItem(new TEntry("MyGameStatsHelper", "Game statistic"));
		// helperFilterComboBox.addActionListener(evt -> filterSensors());
		helperFilterComboBox.registerSettings(new Configuration<ComboBoxState>("PokerSimulator.HelperFilter"));

		this.reportPanel = new TUIPanel();
		reportPanel.showAditionalInformation(false);
		reportPanel.getToolBarPanel().add(helperFilterComboBox);
		this.reportJLabel = new JLabel();
		reportJLabel.setVerticalAlignment(JLabel.TOP);
		reportPanel.setBodyComponent(new JScrollPane(reportJLabel));

		init();
	}

	/**
	 * this mathod act like a buffer betwen {@link SensorsArray} and this class to set the cards based on the name/value
	 * of the {@link ScreenSensor} component while the cards arrive at the game table. For example durin a reading
	 * operation the card are retrived without order. this method store the incomming cards and the run simulation
	 * method will create the correct game status based on the card stored
	 * 
	 * @param sName - name of the {@link ScreenSensor}
	 * @param card - ocr retrived from the sensor.
	 */
	public void addCard(String sName, String card) {
		cardsBuffer.put(sName, card);
//		System.out.println(sName + " " + card);
	}
	public PokerProphesierAdapter getAdapter() {
		return adapter;
	}

	public int getBigBlind() {
		return bigBlind;
	}
	public int getCallValue() {
		return callValue;
	}
	public CommunityCards getCommunityCards() {
		return communityCards;
	}
	public int getCurrentRound() {
		return currentRound;
	}

	public int getHeroChips() {
		return heroChips;
	}

	/**
	 * Return the information component whit all values computesd form simulations and game status
	 * 
	 * @return information component
	 */
	public JComponent getReportPanel() {
		return reportPanel;
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

	public int getPotValue() {
		return potValue;
	}

	public int getRaiseValue() {
		return raiseValue;
	}
	public int getSmallBlind() {
		return smallBlind;
	}

	public int getTablePosition() {
		return tablePosition;
	}
	/**
	 * Init the simulation eviorement. Use this metod to clear al component in case of error or start/stop event
	 * 
	 */
	public void init() {
		this.currentRound = NO_CARDS_DEALT;
		holeCards = null;
		communityCards = null;
		// 190831: ya el sistema se esta moviendo. por lo menos hace fold !!!! :D estoy en el salon de clases del campo
		// de refujiados en dresden !!!! ya van 2 meses
		cardsBuffer.clear();
		potValue = -1;
		tablePosition = -1;
		smallBlind = -1;
		bigBlind=-1;
		callValue = -1;
		raiseValue=-1;
		heroChips = -1;
		reportJLabel.setText("Poker simulator clean.");
	}
	
	public void setBlinds(int sb, int bb) {
		this.smallBlind = sb;
		this.bigBlind = bb;
		updateReport();

	}
	public void setCallValue(int callValue) {
		this.callValue = callValue;
		updateReport();

	}
	public void setHeroChips(int heroChips) {
		this.heroChips = heroChips;
		updateReport();

	}
	public void setNunOfPlayers(int p) {
		this.numSimPlayers = p;
		updateReport();

	}
	public void setPotValue(int potValue) {
		this.potValue = potValue;
		updateReport();
	}
	public void setRaiseValue(int raiseValue) {
		this.raiseValue = raiseValue;
		updateReport();
	}
	public void setTablePosition(int tp) {
		this.tablePosition = tp;
		updateReport();
	}
	public void updateReport() {
		// long t1 = System.currentTimeMillis();
		String selectedHelper = ((TEntry) helperFilterComboBox.getSelectedItem()).getKey().toString();
		String text = "<HTML>";
		myHandHelper = adapter.getMyHandHelper();
		if (myHandHelper != null && selectedHelper.equals("MyHandHelper")) {
			text += "<h3>My hand:" + getFormateTable(myHandHelper.toString(), "hand=", "communityCards=");
		}
		myHandStatsHelper = adapter.getMyHandStatsHelper();
		if (myHandStatsHelper != null && selectedHelper.equals("MyHandStatsHelper")) {
			text += "<h3>My hand Statatistis:" + getFormateTable(myHandStatsHelper.toString());
		}
		MyOutsHelper myOutsHelper = adapter.getMyOutsHelper();
		if (myOutsHelper != null && selectedHelper.equals("MyOutsHelper")) {
			text += "<h3>My Outs:" + getFormateTable(myOutsHelper.toString());
		}
		OppHandStatsHelper oppHandStatsHelper = adapter.getOppHandStatsHelper();
		if (oppHandStatsHelper != null && selectedHelper.equals("OppHandStatsHelper")) {
			text += "<h3>Oponents hands:" + getFormateTable(oppHandStatsHelper.toString());
		}
		myGameStatsHelper = adapter.getMyGameStatsHelper();
		if (myGameStatsHelper != null && selectedHelper.equals("MyGameStatsHelper")) {
			String addinfo = "\nTable Position: " + getTablePosition() + "\n";
			addinfo += "Current round: " + getCurrentRound() + "\n";
			addinfo += "Call amount: " + getCallValue() + "\n";
			addinfo += "Pot: " + getPotValue() + "\n";
			addinfo += "Small blind: " + getSmallBlind() + "\n";
			addinfo += "Big blind: " + getBigBlind() + "\n";
			String allinfo = getFormateTable(myGameStatsHelper.toString() + addinfo);
			text += "<h3>Game Statistics:" + allinfo;
		}

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
	private String getFormateTable(String helperString, String... only) {
		String[] hslines = helperString.split("\n");
		String res = "";
		List<String> onlys = Arrays.asList(only);
		for (String lin : hslines) {
			final String lin1 = lin;
			long c = onlys.size() == 0 ? 1 : onlys.stream().filter(onl -> lin1.startsWith(onl)).count();
			if (c > 0) {
				lin = "<TR><TD>" + lin + "</TD></TR>";
				res += lin.replaceAll(": ", "</TD><TD>");
			}
		}
		return "<TABLE>" + res + "</TABLE>";
	}

	/**
	 * perform the PokerProphesier simulation. Call this method when all the cards on the table has been setted using
	 * {@link #addCard(String, String)} this method will create the {@link HoleCards} and the {@link CommunityCards} (if
	 * is available). After the simulation, the adapters are updated and can be consulted and the report are up to date
	 * 
	 */
	public void runSimulation() {
		try {

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

			createHoleCards();
			createComunityCards();

			adapter.runMySimulations(holeCards, communityCards, numSimPlayers, currentRound);
			updateReport();
		} catch (Exception e) {
			Hero.logger.warning(e.getMessage() + "\n\tCurrent round: " + currentRound + "\n\tHole cards: " + holeCards
					+ "\n\tComunity cards: " + communityCards);
			// Hero.logger.warning(e.getMessage());
			// System.err.println("hole cards " + ((myHoleCards == null) ? "(null)" : myHoleCards.toString())
			// + " communityCards " + ((communityCards == null) ? "(null)" : communityCards.toString()));
		}
	}
}
