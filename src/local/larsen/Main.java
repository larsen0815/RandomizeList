package local.larsen;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;

/**
 * Create a randomized list of players while allowing children to select a fixed
 * partner
 */
public class Main {

	private static final String CHILD_MARKER_IN_PLAYER_NAME = "#";
	private static final String CHILD_LINE = "Platzhalter Kind";
	private static final String DASHED_LINE = "--------------------------------";
	private static final String FIELD_LINE = "******* Spielfeld: ";
	private static JTextArea textarea = null;
	private static HashSet<String> controlSet = new HashSet<String>();
	private static int field;

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {

		// Create and set up the window.
		JFrame frame = new JFrame("Randomizer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBackground(Color.GREEN);

		// Button to start the randomizing
		JButton randomizeButton = new JButton("Würfeln");
		randomizeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> arl = randomizeList();
				if (arl != null) {
					putListIntoTextarea(arl);
				}
			}
		});

		// Reset button
		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textarea.setText(getTextFromFile());
			}
		});

		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
		listPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Lay out the buttons from left to right.
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(randomizeButton);
		buttonPane.add(resetButton);

		// textarea to list players
		textarea = new JTextArea(20, 1);
		textarea.setEditable(true);
		textarea.setPreferredSize(new Dimension(300, 400));
		textarea.setText(getDebuggingSampleText());
		textarea.setText(getTextFromFile());
		textarea.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		listPane.add(textarea);

		// Put everything together, using the content pane's BorderLayout.
		frame.add(listPane, BorderLayout.CENTER);
		frame.add(buttonPane, BorderLayout.PAGE_END);

		// Display the window.
		frame.pack();

		// maximize window
		// frame.setExtendedState(frame.getExtendedState() |
		// JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
	}

	/**
	 * Load a file to display in the textarea
	 * 
	 * @ return
	 */
	private static String getTextFromFile() {
		StringBuilder result = new StringBuilder();

		File input = new File("appen.txt");
		log("Datei lesen: " + input.getAbsolutePath());

		FileReader reader;
		BufferedReader br;
		try {
			reader = new FileReader(input);
			br = new BufferedReader(reader);

			while (br.ready()) {
				result.append(br.readLine() + System.lineSeparator());
			}

			br.close();
			reader.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result.toString();
	}

	/**
	 * @return
	 */
	private static String getDebuggingSampleText() {
		StringBuilder sb = new StringBuilder();
		sb.append(" " + System.lineSeparator());
		sb.append("1 leo" + System.lineSeparator());
		sb.append("2 harry #+kind             " + System.lineSeparator());
		sb.append("3 klaus                   " + System.lineSeparator());
		sb.append("4 Andreas                " + System.lineSeparator());
		sb.append("5 Christian    " + System.lineSeparator());
		sb.append("6 Andrea Ri     # +kind" + System.lineSeparator());
		sb.append("7 Olaf Ha           #+kind" + System.lineSeparator());
		sb.append("8 Jörg               " + System.lineSeparator());
		sb.append("9 Silke Petzel             " + System.lineSeparator());
		// sb.append("10 Thomas #+kind            " + System.lineSeparator());
		sb.append("  " + System.lineSeparator());
		return sb.toString();
	}

	protected static ArrayList<String> randomizeList() {

		log("*** Start randomizing ***");

		// get all entries from the textarea and put it into a set
		ArrayList<String> allPlayers = getListFromTextArea(textarea.getText());

		// get number of players having child marker. these will increase the
		// player count
		int childrenCount = 0;
		for (String part : allPlayers) {
			if (part.contains(CHILD_MARKER_IN_PLAYER_NAME)) {
				childrenCount++;
			}
		}

		// FIXME warte auf antwort leo, was hier letztes mal gemacht wurde
		int total = allPlayers.size() + childrenCount;
		if (total % 2 != 0) {
			JOptionPane.showMessageDialog(null, "Ungerade Anzahl an Spielern!", "", JOptionPane.ERROR_MESSAGE);
			return null;
		} else if (total % 4 != 0) {
			JOptionPane.showMessageDialog(null, "Ungerade Anzahl an Paarungen!", "", JOptionPane.ERROR_MESSAGE);
			return null;
		}

		// randomized files
		Collections.shuffle(allPlayers);

		log("arraylist nach shuffle: " + allPlayers);

		return allPlayers;
	}

	/**
	 * Put list back into textarea
	 * 
	 * @param allPlayers
	 */
	protected static void putListIntoTextarea(ArrayList<String> allPlayers) {
		textarea.setText("");
		controlSet.clear();

		int i = 0;
		field = 0;

		for (String part : allPlayers) {
			i++;
			checkHeader(i);
			log("Current part: " + part);

			// debug: eintrag schon vorhanden?
			if (controlSet.contains(part)) {
				JOptionPane.showMessageDialog(null, "Spieler ist doppelt vorhanden!", "", JOptionPane.ERROR_MESSAGE);
				return;
			}

			// wenn ein kind (raute im namen) dabei ist, wird dies anders
			// gehandhabt
			if (part.contains(CHILD_MARKER_IN_PLAYER_NAME)) {
				// falls wir diese person als ersten einfügen, kommt das kind
				// dazu
				if (i % 2 != 0) {
					addEntry(part);
					addEntry(CHILD_LINE);

					// entsprechend muss der zähler eins extra erhöht werden
					i++;

				} else {
					// falls die person als zweites hinzugefügt werden würde,
					// tauschen wir den eintrag mit dem nächsten aus, der kein
					// kind dabei hat

					part = getReplacement(part, allPlayers);
					addEntry(part);

				}
			} else {
				// kein kind dabei. einfach hinzufügen
				addEntry(part);
			}

			checkFooter(i);
		}
	}

	/**
	 * @param entry
	 */
	private static void addEntry(String entry) {
		log("Adding entry: " + entry);
		textarea.append(entry + System.lineSeparator());
		controlSet.add(entry);
	}

	/**
	 * @param i
	 * @param spielfeld
	 * @return
	 */
	private static void checkHeader(int i) {
		// spielfeld ausgeben
		if ((i - 1) % 4 == 0) {
			String spielfeldstring = FIELD_LINE + ++field + System.lineSeparator();
			log(spielfeldstring);
			textarea.append(spielfeldstring);
		}
	}

	/**
	 * @param i
	 */
	private static void checkFooter(int i) {

		// add a dashed line after each pairing
		if (i % 4 == 0) {
			log("gestrichelte linie");
			textarea.append(DASHED_LINE + System.lineSeparator());
		} else if (i % 2 == 0) {
			// add an empty line after each second entry (if not already
			// dashed)
			log("leerzeile");
			textarea.append(System.lineSeparator());
		}
	}

	/**
	 * @param part
	 * @param textArray
	 * @return
	 */
	private static String getReplacement(String part, ArrayList<String> allPlayers) {

		log("Checking replacement");

		// part im array suchen
		int currentPos = allPlayers.indexOf(part);

		// von der gefundenen position aus den nächsten ohne kind suchen
		int newPos = 0;
		for (int i = currentPos + 1; i < allPlayers.size(); i++) {
			if (!allPlayers.get(i).contains(CHILD_MARKER_IN_PLAYER_NAME)) {
				newPos = i;
				break;
			}
		}

		if (newPos == 0) {
			JOptionPane.showMessageDialog(null, "Kein Austausch-Spieler gefunden!", "", JOptionPane.ERROR_MESSAGE);

		}
		// beide gegeneinander tauschen
		Collections.swap(allPlayers, currentPos, newPos);

		return allPlayers.get(currentPos);
	}

	private static ArrayList<String> getListFromTextArea(String textAreaValue) {

		ArrayList<String> list = new ArrayList<String>();
		StringTokenizer tokens = new StringTokenizer(textAreaValue, System.lineSeparator());
		while (tokens.hasMoreTokens()) {
			String elem = (String) tokens.nextElement();
			elem = elem.trim();

			// bestimmte zeilen ignorieren
			if (!elem.isEmpty() && !elem.contains(CHILD_LINE) && !elem.contains(DASHED_LINE) && !elem.startsWith(FIELD_LINE)) {
				log("adding to list: " + elem);
				list.add(elem);
			}
		}
		return list;
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	private static void log(String text) {
		System.out.println(text);
	}
}
