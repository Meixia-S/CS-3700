
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonStreamParser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import javax.net.ssl.SSLSocketFactory;

public class Main {

  /**
   * Main method the runs the client side of the word guessing game.
   */
  public static void main(String[] args) throws Exception {
    // initializing sockets
    Socket socket = null;
    SSLSocketFactory ssl = (SSLSocketFactory) SSLSocketFactory.getDefault();

    // getting information form args
    String hostName = args[args.length-2]; // proj1.3700.network
    String userName = args[args.length-1]; // sindelar.m

    // setting up the correct socket based on the flags
    if (args[0].contains("-s") || args[1].contains("-s")) {
      socket = ssl.createSocket(hostName, 27994);
    } else {
      socket = new Socket(hostName, 27993); }

    // setting up the in and out
    DataInputStream in = new DataInputStream(socket.getInputStream());
    InputStreamReader reader = new InputStreamReader(in);
    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
    // setting up the in with a JSON stream parser
    JsonStreamParser jsonStreamParser = new JsonStreamParser(reader);

    // play through the word guessing game
    playGame(out, in, jsonStreamParser, userName);
    socket.close();
  }

  /**
   * Plays through the whole word guessing game starting with the hello message.
   * @param out the output stream
   * @param in the input stream
   * @param jsonStreamParser the stream that parses the json input
   * @param userName the player's username
   */
  public static void playGame(DataOutputStream out, DataInputStream in, JsonStreamParser jsonStreamParser, String userName) throws Exception {
    // send hello message
    out.write((helloMessage(userName) + "\n").getBytes(StandardCharsets.UTF_8));
    client c = new client();
    String id = "";

    // going through the game one response at a time
    while (jsonStreamParser.hasNext()) {
      // paring the input given
      JsonObject responseFromServer = jsonStreamParser.next().getAsJsonObject();
      String type = responseFromServer.getAsJsonPrimitive("type").getAsString();

      // if it is the start of the game, guess a random number from the word_list
      if (type.equals("start")) {
        Random rand = new Random();
        id = String.valueOf(responseFromServer.getAsJsonPrimitive("id"));
        String randomWord = c.wordList.get(rand.nextInt(c.wordList.size()));
        out.write((buildResponse(id, randomWord) + "\n").getBytes(StandardCharsets.UTF_8));
        // if guessing regularly use the guessWord method in the client class
      } else if (type.equals("retry")) {
        JsonArray guesses = responseFromServer.getAsJsonArray("guesses");
        JsonObject feedback = (JsonObject) guesses.get(guesses.size() - 1);
        String guessedWord = c.guessAWord(feedback.getAsJsonPrimitive("word").getAsString(),
                feedback.getAsJsonArray("marks"));
        out.write((buildResponse(id, guessedWord) + "\n").getBytes(StandardCharsets.UTF_8));
        // close the socket and then break from the loop
      } else {
        System.out.println(responseFromServer.getAsJsonPrimitive("flag").getAsString());
        in.close();
        out.close();
        break;
      }
    }
  }

  /**
   * Formats the JsonObject response that the client sends to the server.
   * @param id the player's id
   * @param word the word the player is guessing
   */
  public static JsonObject buildResponse(String id, String word) {
    JsonObject responseFromClient = new JsonObject();
    responseFromClient.add("type", new JsonPrimitive("guess"));
    responseFromClient.add("id", new JsonPrimitive(id));
    responseFromClient.add("word", new JsonPrimitive(word));
    return responseFromClient;
  }

  /**
   * Prints out the initial hello message with user information.
   * @param userName the name the user goes by (taken from user's email)
   */
  public static JsonObject helloMessage(String userName) {
    JsonObject helloMsg = new JsonObject();
    JsonPrimitive hello = new JsonPrimitive("hello");
    helloMsg.add("type", hello);
    JsonPrimitive username = new JsonPrimitive(userName);
    helloMsg.add("northeastern_username", username);

    return helloMsg;
  }
}