import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

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

class WordleSolver {
    TrieNode root;
    HashMap<String, Boolean> wordMap;
    String hiddenWord;
    int numGuesses;
    Gson gson;

    WordleSolver() {
        root = new TrieNode('\0');
        wordMap = new HashMap<>();
        numGuesses = 0;
        gson = new Gson();
    }

    void buildTrie(List<String> words) {
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
        List<String> words = new ArrayList<>(wordMap.keySet());
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
        if (!isWordInTrie(input)) {
            numGuesses--;
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
                feedback.append("<span style='color:green'>").append(inputArray[i]).append("</span>");
            } else if (new String(hiddenArray).indexOf(inputArray[i]) != -1) {
                feedback.append("<span style='color:yellow'>").append(inputArray[i]).append("</span>");
            } else {
                feedback.append("<span style='color:gray'>").append(inputArray[i]).append("</span>");
            }
        }

        if (input.equals(hiddenWord)) {
            return "Well done, correct guess!";
        }

        // Filtration of words
        List<String> filteredWords = new ArrayList<>();
        for (String word : wordMap.keySet()) {
            boolean isValid = true;
            for (int i = 0; i < 5; i++) {
                if (inputArray[i] == hiddenArray[i] && word.charAt(i) != inputArray[i]) {
                    isValid = false;
                    break;
                }
                if (inputArray[i] != hiddenArray[i] && word.charAt(i) == inputArray[i]) {
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

        // Suggest top 5 words
        List<String> suggestions = new ArrayList<>(wordMap.keySet());
        Collections.shuffle(suggestions);
        suggestions = suggestions.subList(0, Math.min(5, suggestions.size()));

        return feedback.toString() + "<br>Suggestions: " + suggestions;
    }

    void reset() {
        numGuesses = 0;
        wordMap.clear();
        buildTrie(getWordList());
        selectHiddenWord();
    }

    List<String> getWordList() {
        List<String> words = new ArrayList<>();
        try {
            String url = "https://api.datamuse.com/words?sp=?????&max=1000";
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            JsonArray jsonArray = gson.fromJson(reader, JsonArray.class);
            reader.close();

            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();
                String word = jsonObject.get("word").getAsString();
                if (word.length() == 5) {
                    words.add(word);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return words;
    }
}

public class Main {
    public static void main(String[] args) {
        WordleSolver solver = new WordleSolver();
        solver.buildTrie(solver.getWordList());
        solver.selectHiddenWord();

        Scanner scanner = new Scanner(System.in);
        boolean continuePlaying = true;

        while (continuePlaying) {
            while (true) {
                System.out.print("Enter your guess: ");
                String input = scanner.nextLine();
                String result = solver.guess(input);
                System.out.println(result);
                if (result.equals("Well done, correct guess!") || result.equals("Failed to guess")) {
                    break;
                }
            }
            System.out.print("Start a new game? (yes/no): ");
            String userChoice = scanner.nextLine();
            if (userChoice.equalsIgnoreCase("yes")) {
                solver.reset();
            } else {
                continuePlaying = false;
                System.out.println("Game ended.");
            }
        }
    }
}

