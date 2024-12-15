import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

// Trie Node Class
class TrieNode {
    char c;
    boolean isWord;
    HashMap<Character, TrieNode> childMap;

    TrieNode(char c) {
        this.c = c;
        isWord = false;
        childMap = new HashMap<>();
    }
}

// Wordle Solver Class
class WordleSolver {
    TrieNode root;
    HashMap<String, Boolean> wordMap;
    String hiddenWord;
    int numGuesses;
    java.util.List<String> wordList;

    WordleSolver(java.util.List<String> words) {
        root = new TrieNode('\0');
        wordMap = new HashMap<>();
        numGuesses = 0;
        wordList = words;
        buildTrie(words);
        selectHiddenWord();
    }

    void buildTrie(java.util.List<String> words) {
        for (String word : words) {
            TrieNode curr = root;
            for (char c : word.toCharArray()) {
                if (!curr.childMap.containsKey(c)) {
                    curr.childMap.put(c, new TrieNode(c));
                }
                curr = curr.childMap.get(c);
            }
            curr.isWord = true;
            wordMap.put(word, true);
        }
    }

    void selectHiddenWord() {
        java.util.List<String> words = new ArrayList<>(wordMap.keySet());
        hiddenWord = words.get(new Random().nextInt(words.size()));
    }

    boolean isWordInTrie(String word) {
        TrieNode curr = root;
        for (char c : word.toCharArray()) {
            if (!curr.childMap.containsKey(c)) {
                return false;
            }
            curr = curr.childMap.get(c);
        }
        return curr.isWord;
    }

    String guess(String input) {
        if (input.length() != 5) {
            return "Please enter a 5-letter word";
        }
        if (!isWordInTrie(input)) {
            return "Word not found in the list";
        }

        numGuesses++;
        if (numGuesses > 6) {
            return "Failed to guess";
        }

        char[] inputArray = input.toCharArray();
        char[] hiddenArray = hiddenWord.toCharArray();
        StringBuilder feedback = new StringBuilder();

        for (int i = 0; i < 5; i++) {
            if (inputArray[i] == hiddenArray[i]) {
                feedback.append("G");
            } else if (new String(hiddenArray).indexOf(inputArray[i]) != -1) {
                feedback.append("Y");
            } else {
                feedback.append("X");
            }
        }

        if (input.equals(hiddenWord)) {
            return "Well done, correct guess!";
        }

        // Filtration of words
        java.util.List<String> filteredWords = new ArrayList<>();
        for (String word : wordMap.keySet()) {
            boolean isValid = true;
            for (int i = 0; i < 5; i++) {
                if (feedback.charAt(i) == 'X' && word.indexOf(inputArray[i]) != -1) {
                    isValid = false;
                    break;
                }
                if (feedback.charAt(i) == 'Y' && (word.charAt(i) == inputArray[i] || word.indexOf(inputArray[i]) == -1)) {
                    isValid = false;
                    break;
                }
                if (feedback.charAt(i) == 'G' && word.charAt(i) != inputArray[i]) {
                    isValid = false;
                    break;
                }
            }
            if (isValid) {
                filteredWords.add(word);
            }
        }
        wordMap.clear();
        for (String word : filteredWords) {
            wordMap.put(word, true);
        }

        // Suggest top 6 words
        java.util.List<String> suggestions = new ArrayList<>(wordMap.keySet());
        Collections.shuffle(suggestions);
        suggestions = suggestions.subList(0, Math.min(6, suggestions.size()));

        return feedback.toString() + " Suggestions: " + String.join(", ", suggestions);
    }

    void reset() {
        numGuesses = 0;
        wordMap.clear();
        buildTrie(wordList);
        selectHiddenWord();
    }

    boolean hasFailed() {
        return numGuesses >= 6;
    }

    String getHiddenWord() {
        return hiddenWord;
    }
}

// Self-Solver GUI
class WordleGUI extends JFrame {
    private WordleSolver solver;
    private JTextField[][] grid;
    private JLabel suggestionLabel;
    private JButton submitButton;
    private JButton newGameButton;

    public WordleGUI(java.util.List<String> wordList) {
        this.solver = new WordleSolver(wordList);

        setTitle("Wordle Self-Solver");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(6, 5));
        grid = new JTextField[6][5];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                grid[i][j] = new JTextField();
                grid[i][j].setHorizontalAlignment(JTextField.CENTER);
                grid[i][j].setBackground(Color.LIGHT_GRAY);
                grid[i][j].setEditable(true);
                grid[i][j].setFont(new Font("Arial", Font.PLAIN, 20));
                grid[i][j].setPreferredSize(new Dimension(40, 40));
                grid[i][j].addKeyListener(new KeyListener() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        if (e.getKeyChar() < 'a' || e.getKeyChar() > 'z') {
                            e.consume();
                        }
                    }

                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            JTextField currentField = (JTextField) e.getSource();
                            for (int i = 0; i < 6; i++) {
                                for (int j = 0; j < 5; j++) {
                                    if (grid[i][j] == currentField) {
                                        if (j < 4) {
                                            grid[i][j + 1].requestFocus();
                                        } else if (i < 5) {
                                            grid[i + 1][0].requestFocus();
                                        }
                                        return;
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {}
                });
                gridPanel.add(grid[i][j]);
            }
        }
        add(gridPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        submitButton = new JButton("Submit Guess");
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleGuess();
            }
        });
        controlPanel.add(submitButton, BorderLayout.CENTER);

        newGameButton = new JButton("Start New Game");
        newGameButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                solver.reset();
                clearGrid();
                suggestionLabel.setText("Suggestions: ");
                grid[0][0].requestFocus(); // Ensure focus is set to the first input field
            }
        });
        controlPanel.add(newGameButton, BorderLayout.WEST);

        JButton quitButton = new JButton("Quit Game");
        quitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        controlPanel.add(quitButton, BorderLayout.EAST);

        suggestionLabel = new JLabel("Suggestions: ");
        controlPanel.add(suggestionLabel, BorderLayout.NORTH);

        add(controlPanel, BorderLayout.SOUTH);
        grid[0][0].requestFocus(); // Ensure focus is set to the first input field initially
    }

    private void handleGuess() {
        StringBuilder guessBuilder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            guessBuilder.append(grid[solver.numGuesses][i].getText().toLowerCase());
        }
        String guess = guessBuilder.toString().trim();

        if (guess.length() != 5) {
            JOptionPane.showMessageDialog(this, "Please enter exactly 5 letters.");
            return;
        }

        String result = solver.guess(guess);
        updateGrid(guess, result);

        if (solver.hasFailed() && !result.startsWith("Well done")) {
            JOptionPane.showMessageDialog(this, "Better luck next time, the hidden word was '" + solver.getHiddenWord() + "'");
            int option = JOptionPane.showConfirmDialog(this, "Do you want to start a new game?", "Game Over", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                solver.reset();
                clearGrid();
                suggestionLabel.setText("Suggestions: ");
                grid[0][0].requestFocus(); // Ensure focus is set to the first input field after restarting
            } else {
                System.exit(0);
            }
        } else if (result.startsWith("Well done")) {
            JOptionPane.showMessageDialog(this, result);
            int option = JOptionPane.showConfirmDialog(this, "Do you want to start a new game?", "Game Over", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                solver.reset();
                clearGrid();
                suggestionLabel.setText("Suggestions: ");
                grid[0][0].requestFocus(); // Ensure focus is set to the first input field after restarting
            } else {
                System.exit(0);
            }
        } else {
            String[] parts = result.split(" Suggestions: ");
            if (parts.length > 1) {
                suggestionLabel.setText("Suggestions: " + parts[1]);
            }
        }
    }

    private void updateGrid(String guess, String result) {
        char[] feedback = result.substring(0, 5).toCharArray();
        for (int i = 0; i < 5; i++) {
            char c = feedback[i];
            Color color;
            switch (c) {
                case 'G':
                    color = Color.GREEN;
                    break;
                case 'Y':
                    color = Color.YELLOW;
                    break;
                default:
                    color = Color.GRAY;
                    break;
            }
            grid[solver.numGuesses - 1][i].setBackground(color);
            grid[solver.numGuesses - 1][i].setText(String.valueOf(guess.charAt(i)).toUpperCase());
        }
    }

    private void clearGrid() {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                grid[i][j].setText("");
                grid[i][j].setBackground(Color.LIGHT_GRAY);
            }
        }
    }
}

// Main Menu GUI
class MainMenu extends JFrame {
    public MainMenu(java.util.List<String> wordList) {
        setTitle("Wordle Game Menu");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 1));

        JButton solverButton = new JButton("Start Self-Solver");
        solverButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                new WordleGUI(wordList).setVisible(true);
            }
        });
        add(solverButton);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        add(exitButton);
    }
}

// Main class to run the application
public class WordleGame {
    public static void main(String[] args) {
        final java.util.List<String> wordList;
        try {
            wordList = Files.readAllLines(Paths.get("wordlist.txt"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        SwingUtilities.invokeLater(() -> {
            new MainMenu(wordList).setVisible(true);
        });
    }
}

