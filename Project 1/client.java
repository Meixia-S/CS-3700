import com.google.gson.JsonArray;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * client class that represents a player playing the word guessing game.
 */
public class client {
  // The array that contains all the words that could possibly be "the word"
  List<String> wordList = creatingWordList();
  // A list of all the words that have already been guessed
  ArrayList<String> guessedWords = new ArrayList<>();

  public client () throws Exception { }

  /**
   * @return the contents of the word_list text file as a list of strings.
   */
  public ArrayList<String> creatingWordList() throws Exception {
    ArrayList<String> wordList = new ArrayList<>();
    // load data from file
    BufferedReader bf = new BufferedReader(new FileReader("word_list.txt"));
    // read each line as string
    String line = bf.readLine();

    // checking for end of file
    while (line != null) {
      wordList.add(line);
      line = bf.readLine();
    }

    bf.close();
    return wordList;

    // source: https://www.geeksforgeeks.org/read-file-into-an-array-in-java/#
  }

  /**
   * Makes another guess by picking out a word that matches the "marks" and have not been guessed before.
   * @param prevGuessedWord the previous word that was guessed
   * @param jA the JsonArray that contains the marks (indicator of which letters appear in the word and where)
   */
  public String guessAWord(String prevGuessedWord, JsonArray jA) {
    HashMap<Character, Integer> newGuessRequirement = new HashMap<>();
    String currGuessedWord = "";

    for (String word : this.wordList) {
      currGuessedWord = word;
      if (this.guessedWords.contains(currGuessedWord)) {
        continue;
      }
      // adds new guidelines
      for (int i = 0; i < 5; i++) {
        if (jA.get(i).getAsInt() == 2) {
          newGuessRequirement.put(prevGuessedWord.charAt(i), i);
        } else if (jA.get(i).getAsInt() == 1) {
          newGuessRequirement.put(prevGuessedWord.charAt(i), 5);
        } else if (jA.get(i).getAsInt() == 0) {
          newGuessRequirement.put(prevGuessedWord.charAt(i), 6);
        }
      }
      if (isFollowingGuideline(newGuessRequirement, currGuessedWord)) {
        guessedWords.add(currGuessedWord);
        break;
      }
    }
    return currGuessedWord;
  }

  /**
   * Helper method that checks at the current word follows at the guidelines indicated by the marks
   * @param letterGuider a hashmap that keeps track of all the letters that where marked with 0 or 1
   * @param currGuessedWord the current word we are looking at
   */
  public boolean isFollowingGuideline(HashMap<Character, Integer> letterGuider, String currGuessedWord) {
    for (Map.Entry<Character, Integer> entry : letterGuider.entrySet()) {
      if (entry.getValue() < 5 && currGuessedWord.charAt(entry.getValue()) != entry.getKey()) {
          return false;
      } else if (entry.getValue() == 5 && !currGuessedWord.contains(String.valueOf(entry.getKey()))) {
          return false;
      } else if (entry.getValue() == 6 && currGuessedWord.contains(String.valueOf(entry.getKey()))) {
          return false;
      }
    }
    return true;
  }
}