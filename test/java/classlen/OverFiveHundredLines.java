package java.classlen;

//Imports are listed in full to show what's being used
//could just import javax.swing.* and java.awt.* etc..
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class OverFiveHundredLines {

	// Note: Typically the main method will be in a
	// separate class. As this is a simple one class
	// example it's all in the one class.
	public static void main(String[] args) {

		new OverFiveHundredLines();
	}

	public OverFiveHundredLines() {
		JFrame guiFrame = new JFrame();

		// make sure the program exits when the frame closes
		guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		guiFrame.setTitle("Example GUI");
		guiFrame.setSize(300, 250);

		// This will center the JFrame in the middle of the screen
		guiFrame.setLocationRelativeTo(null);

		// Options for the JComboBox
		String[] fruitOptions = { "Apple", "Apricot", "Banana", "Cherry",
				"Date", "Kiwi", "Orange", "Pear", "Strawberry" };

		// Options for the JList
		String[] vegOptions = { "Asparagus", "Beans", "Broccoli", "Cabbage",
				"Carrot", "Celery", "Cucumber", "Leek", "Mushroom", "Pepper",
				"Radish", "Shallot", "Spinach", "Swede", "Turnip" };

		// The first JPanel contains a JLabel and JCombobox
		final JPanel comboPanel = new JPanel();
		JLabel comboLbl = new JLabel("Fruits:");
		JComboBox fruits = new JComboBox(fruitOptions);

		comboPanel.add(comboLbl);
		comboPanel.add(fruits);

		// Create the second JPanel. Add a JLabel and JList and
		// make use the JPanel is not visible.
		final JPanel listPanel = new JPanel();
		listPanel.setVisible(false);
		JLabel listLbl = new JLabel("Vegetables:");
		JList vegs = new JList(vegOptions);
		vegs.setLayoutOrientation(JList.HORIZONTAL_WRAP);

		listPanel.add(listLbl);
		listPanel.add(vegs);

		JButton vegFruitBut = new JButton("Fruit or Veg");

		// The ActionListener class is used to handle the
		// event that happens when the user clicks the button.
		// As there is not a lot that needs to happen we can
		// define an anonymous inner class to make the code simpler.
		vegFruitBut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				// When the fruit of veg button is pressed
				// the setVisible value of the listPanel and
				// comboPanel is switched from true to
				// value or vice versa.
				listPanel.setVisible(!listPanel.isVisible());
				comboPanel.setVisible(!comboPanel.isVisible());

			}
		});

		// The JFrame uses the BorderLayout layout manager.
		// Put the two JPanels and JButton in different areas.
		guiFrame.add(comboPanel, BorderLayout.NORTH);
		guiFrame.add(listPanel, BorderLayout.CENTER);
		guiFrame.add(vegFruitBut, BorderLayout.SOUTH);

		// make sure the JFrame is visible
		guiFrame.setVisible(true);
	}

	/*
	 * pre: none post: returns a String with base10Num in base 2
	 */
	public static String toBinary(int base10Num) {
		boolean isNeg = base10Num < 0;
		base10Num = Math.abs(base10Num);
		String result = "";

		while (base10Num > 1) {
			result = (base10Num % 2) + result;
			base10Num /= 2;
		}
		assert base10Num == 0 || base10Num == 1 : "value is not <= 1: "
				+ base10Num;

		result = base10Num + result;
		assert all0sAnd1s(result);

		if (isNeg)
			result = "-" + result;
		return result;
	}

	/*
	 * pre: cal != null post: return true if val consists only of characters 1
	 * and 0, false otherwise
	 */
	public static boolean all0sAnd1s(String val) {
		assert val != null : "Failed precondition all0sAnd1s. parameter cannot be null";
		boolean all = true;
		int i = 0;
		char c;

		while (all && i < val.length()) {
			c = val.charAt(i);
			all = c == '0' || c == '1';
			i++;
		}
		return all;
	}

	public static void show(boolean[][] grid) {
		String s = "";
		for (boolean[] row : grid) {
			for (boolean val : row)
				if (val)
					s += "*";
				else
					s += ".";
			s += "\n";
		}
		System.out.println(s);
	}

	public static boolean[][] gen() {
		boolean[][] grid = new boolean[10][10];
		for (int r = 0; r < 10; r++)
			for (int c = 0; c < 10; c++)
				if (Math.random() > 0.7)
					grid[r][c] = true;
		return grid;
	}

	public static void dosomething(String[] args) {
		boolean[][] world = gen();
		show(world);
		System.out.println();
		world = nextGen(world);
		show(world);
		Scanner s = new Scanner(System.in);
		while (s.nextLine().length() == 0) {
			System.out.println();
			world = nextGen(world);
			show(world);

		}
	}

	public static boolean[][] nextGen(boolean[][] world) {
		boolean[][] newWorld = new boolean[world.length][world[0].length];
		int num;
		for (int r = 0; r < world.length; r++) {
			for (int c = 0; c < world[0].length; c++) {
				num = numNeighbors(world, r, c);
				if (occupiedNext(num, world[r][c]))
					newWorld[r][c] = true;
			}
		}
		return newWorld;
	}

	public static boolean occupiedNext(int numNeighbors, boolean occupied) {
		if (occupied && (numNeighbors == 2 || numNeighbors == 3))
			return true;
		else if (!occupied && numNeighbors == 3)
			return true;
		else
			return false;
	}

	private static int numNeighbors(boolean[][] world, int row, int col) {
		int num = world[row][col] ? -1 : 0;
		for (int r = row - 1; r <= row + 1; r++)
			for (int c = col - 1; c <= col + 1; c++)
				if (inbounds(world, r, c) && world[r][c])
					num++;

		return num;
	}

	private static boolean inbounds(boolean[][] world, int r, int c) {
		return r >= 0 && r < world.length && c >= 0 && c < world[0].length;
	}

	// pre: list != null, list.length > 0
	// post: return index of minimum element of array
	public static int findMin(int[] list) {
		assert list != null && list.length > 0 : "failed precondition";

		int indexOfMin = 0;
		for (int i = 1; i < list.length; i++) {
			if (list[i] < list[indexOfMin]) {
				indexOfMin = i;
			}
		}

		return indexOfMin;
	}

	/*
	 * pre: list != null, newSize >= 0post: nothing. the method does not succeed
	 * it resizing the argument
	 */
	public static void badResize(int[] list, int newSize) {
		assert list != null && newSize >= 0 : "failed precondition";

		int[] temp = new int[newSize];
		int limit = Math.min(list.length, newSize);

		for (int i = 0; i < limit; i++) {
			temp[i] = list[i];
		}

		// uh oh!! Changing pointer, not pointee. This breaks the
		// relationship between the parameter and argument
		list = temp;
	}

	/*
	 * pre: list != null, newSize >= 0post: returns an array of size newSize.
	 * Elements from 0 to newSize - 1 will be copied into the new array
	 */
	public static int[] goodResize(int[] list, int newSize) {
		assert list != null && newSize >= 0 : "failed precondition";

		int[] result = new int[newSize];
		int limit = Math.min(list.length, newSize);

		for (int i = 0; i < limit; i++) {
			result[i] = list[i];
		}

		return result;
	}

	/*
	 * pre: list != nullpost: prints out the indices and values of all pairs of
	 * numbersin list such that list[a] + list[b] = target
	 */
	public static void findAndPrintPairs(int[] list, int target) {
		assert list != null : "failed precondition";

		for (int i = 0; i < list.length; i++) {
			for (int j = i + 1; j < list.length; j++) {
				if (list[i] + list[j] == target) {
					System.out.println("The two elements at indices " + i
							+ " and " + j + " are " + list[i] + " and "
							+ list[j] + " add up to " + target);
				}
			}
		}
	}

	/*
	 * pre: list != null;post: sort the elements of list so that they are in
	 * ascending order
	 */
	public static void bubblesort(int[] list) {
		assert list != null : "failed precondition";

		int temp;
		boolean changed = true;
		for (int i = 0; i < list.length && changed; i++) {
			changed = false;
			for (int j = 0; j < list.length - i - 1; j++) {
				assert (j > 0) && (j + 1 < list.length) : "loop counter j " + j
						+ "is out of bounds.";
				if (list[j] > list[j + 1]) {
					changed = true;
					temp = list[j + 1];
					list[j + 1] = list[j];
					list[j] = temp;
				}
			}
		}

		assert isAscending(list);
	}

	public static void showList(int[] list) {
		for (int i = 0; i < list.length; i++)
			System.out.print(list[i] + " ");
		System.out.println();
	}

	/*
	 * pre: list != null post: return true if list is sorted in ascedning order,
	 * false otherwise
	 */
	public static boolean isAscending(int[] list) {
		boolean ascending = true;
		int index = 1;
		while (ascending && index < list.length) {
			assert index >= 0 && index < list.length;

			ascending = (list[index - 1] <= list[index]);
			index++;
		}

		return ascending;
	}

	public static void go() {
		Rectangle r1 = new Rectangle(0, 0, 5, 5);
		System.out.println("In method go. r1 " + r1 + "\n");
		// could have been
		// System.out.prinltn("r1" + r1.toString());
		r1.setSize(10, 15);
		System.out.println("In method go. r1 " + r1 + "\n");
		alterPointee(r1);
		System.out.println("In method go. r1 " + r1 + "\n");

		alterPointer(r1);
		System.out.println("In method go. r1 " + r1 + "\n");
	}

	public static void alterPointee(Rectangle r) {
		System.out.println("In method alterPointee. r " + r + "\n");
		r.setSize(20, 30);
		System.out.println("In method alterPointee. r " + r + "\n");
	}

	public static void alterPointer(Rectangle r) {
		System.out.println("In method alterPointer. r " + r + "\n");
		r = new Rectangle(5, 10, 30, 35);
		System.out.println("In method alterPointer. r " + r + "\n");
	}

	// pre: list != null
	// post: return sum of elements
	// uses enhanced for loop
	public static int sumListEnhanced(int[] list) {
		int total = 0;
		for (int val : list) {
			total += val;
		}
		return total;
	}

	// pre: list != null
	// post: return sum of elements
	// use traditional for loop
	public static int sumListOld(int[] list) {
		int total = 0;
		for (int i = 0; i < list.length; i++) {
			total += list[i];
			System.out.println(list[i]);
		}
		return total;
	}

	// pre: list != null
	// post: none.
	// The code appears to add one to every element in the list, but does not
	public static void addOneError(int[] list) {
		for (int val : list) {
			val = val + 1;
		}
	}

	// pre: list != null
	// post: adds one to every element of list
	public static void addOne(int[] list) {
		for (int i = 0; i < list.length; i++) {
			list[i]++;
		}
	}

	public static void printList(int[] list) {
		System.out.println("index, value");
		for (int i = 0; i < list.length; i++) {
			System.out.println(i + ", " + list[i]);
		}
	}

	public static boolean[] getPrimes(int max) {
		boolean[] result = new boolean[max + 1];
		for (int i = 2; i < result.length; i++)
			result[i] = true;
		final double LIMIT = Math.sqrt(max);
		for (int i = 2; i <= LIMIT; i++) {
			if (result[i]) {
				// cross out all multiples;
				int index = 2 * i;
				while (index < result.length) {
					result[index] = false;
					index += i;
				}
			}
		}
		return result;
	}

	// pre: num >= 2
	public static boolean isPrime(int num) {
		assert num >= 2 : "failed precondition. num must be >= 2. num: " + num;
		final double LIMIT = Math.sqrt(num);
		boolean isPrime = (num == 2) ? true : num % 2 != 0;
		int div = 3;
		while (div <= LIMIT && isPrime) {
			isPrime = num % div != 0;
			div += 2;
		}
		return isPrime;
	}

	// pre: num >= 2
	public static int numFactors(int num) {
		assert num >= 2 : "failed precondition. num must be >= 2. num: " + num;
		int result = 0;
		final double SQRT = Math.sqrt(num);
		for (int i = 1; i < SQRT; i++) {
			if (num % i == 0) {
				result += 2;
			}
		}
		if (num % SQRT == 0)
			result++;
		return result;
	}

	public static void do2(String[] args) {
		try {
			File f = new File("ciaFactBook2008.txt");
			Scanner sc;
			sc = new Scanner(f);
			// sc.useDelimiter("[^a-zA-Z']+");
			Map<String, Integer> wordCount = new TreeMap<String, Integer>();
			while (sc.hasNext()) {
				String word = sc.next();
				if (!wordCount.containsKey(word))
					wordCount.put(word, 1);
				else
					wordCount.put(word, wordCount.get(word) + 1);
			}

			// show results
			for (String word : wordCount.keySet())
				System.out.println(word + " " + wordCount.get(word));
			System.out.println(wordCount.size());
		} catch (IOException e) {
			System.out.println("Unable to read from file.");
		}
	}

	// allow user to pick file to exam via GUI.
	// allow multiple picks
	public static void countWordsViaGUI() {
		setLookAndFeel();
		try {
			Scanner key = new Scanner(System.in);
			do {
				System.out.println("Opening GUI to choose file.");
				Scanner fileScanner = new Scanner(getFile());
				ArrayList<String> words = countWordsWithArrayList(fileScanner);
				System.out.print("Enter number of words to display: ");
				int numWordsToShow = Integer.parseInt(key.nextLine());
				showWords(words, numWordsToShow);
				fileScanner.close();
				System.out.print("Perform another count? ");
			} while (key.nextLine().toLowerCase().charAt(0) == 'y');
			key.close();
		} catch (FileNotFoundException e) {
			System.out
					.println("Problem reading the data file. Exiting the program."
							+ e);
		}
	}

	// determine distinct words in a file using an array list
	private static ArrayList<String> countWordsWithArrayList(Scanner fileScanner) {

		String numWords = null;
		System.out.println("Total number of words: " + numWords);
		ArrayList<String> result = null;
		System.out.println("number of distincy words: " + result.size());
		return result;
	}

	// determine distinct words in a file and frequency of each word with a Map
	private static Map<String, Integer> countWordsWithMap(Scanner fileScanner) {

		String numWords = null;
		System.out.println("Total number of words: " + numWords);
		Map<String, Integer> result = null;
		System.out.println("number of distincy words: " + result.size());
		return result;
	}

	private static void showWords(ArrayList<String> words, int numWordsToShow) {
		for (int i = 0; i < words.size() && i < numWordsToShow; i++)
			System.out.println(words.get(i));
	}

	private static void showWords(Map<String, Integer> words, int numWordsToShow) {

	}

	// perform a series of experiments on files. Determine average time to
	// count words in files of various sizes
	private static void performExp() {
		String[] smallerWorks = { "smallWords.txt", "2BR02B.txt", "Alice.txt",
				"SherlockHolmes.txt" };
		;
		String[] bigFile = { "ciaFactBook2008.txt" };
		timingExpWithArrayList(smallerWorks, 50);
		timingExpWithArrayList(bigFile, 3);
		timingExpWithMap(smallerWorks, 50);
		timingExpWithMap(bigFile, 3);
	}

	// pre: titles != null, elements of titles refer to files in the
	// same path as this program, numExp >= 0
	// read words from files and print average time to cound words.
	private static void timingExpWithMap(String[] titles, int numExp) {
		try {
			double[] times = new double[titles.length];
			final int NUM_EXP = 50;
			for (int i = 0; i < NUM_EXP; i++) {
				for (int j = 0; j < titles.length; j++) {
					Scanner fileScanner = new Scanner(new File(titles[j]));
					Map<String, Integer> words = countWordsWithMap(fileScanner);
					System.out.println(words.size());
					fileScanner.close();
				}
			}
			for (double a : times)
				System.out.println(a / NUM_EXP);
		} catch (FileNotFoundException e) {
			System.out
					.println("Problem reading the data file. Exiting the program."
							+ e);
		}
	}

	// pre: titles != null, elements of titles refer to files in the
	// same path as this program, numExp >= 0
	// read words from files and print average time to cound words.
	private static void timingExpWithArrayList(String[] titles, int numExp) {
		try {
			double[] times = new double[titles.length];
			for (int i = 0; i < numExp; i++) {
				for (int j = 0; j < titles.length; j++) {
					Scanner fileScanner = new Scanner(new File(titles[j]));
					ArrayList<String> words = countWordsWithArrayList(fileScanner);
					fileScanner.close();
				}
			}
			for (int i = 0; i < titles.length; i++)
				System.out.println("Average time for " + titles[i] + ": "
						+ (times[i] / numExp));
		} catch (FileNotFoundException e) {
			System.out
					.println("Problem reading the data file. Exiting the program."
							+ e);
		}
	}

	// try to set look and feel to same as system
	private static void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println("Unable to set look at feel to local settings. "
					+ "Continuing with default Java look and feel.");
		}
	}

	/**
	 * Method to choose a file using a window.
	 * 
	 * @return the file chosen by the user. Returns null if no file picked.
	 */
	private static File getFile() {
		// create a GUI window to pick the text to evaluate
		JFileChooser chooser = new JFileChooser(".");
		chooser.setDialogTitle("Select File To Count Words:");
		int retval = chooser.showOpenDialog(null);
		File f = null;
		chooser.grabFocus();
		if (retval == JFileChooser.APPROVE_OPTION)
			f = chooser.getSelectedFile();
		return f;
	}
	
    private class Counter {
        public Counter(int counter) { this.counter=counter; }
        public void inc() { counter++; };
        public int getValue() { return counter; }
        private int counter = 0;
    }


}