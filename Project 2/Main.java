import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.net.Socket;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Scanner;

/**
 * Please Read! I was not able to find a good URL or Parameter parser for Java!
 */
public class Main {
  static Socket controlChannel;
  static DataInputStream controlIn;
  static PrintStream controlOut;
  static Socket dataChannel;
  static DataInputStream dataIn;
  static DataOutputStream dataOut;

  public Main() {}
  public static void main(String[] args) throws IOException {
    // connecting to the FTP server
    controlChannel = new Socket("ftp.3700.network", 21);
    controlIn = new DataInputStream(controlChannel.getInputStream());
    controlOut = new PrintStream(controlChannel.getOutputStream(), true, StandardCharsets.UTF_8);

    // welcome message
    FTPResponse();
    // logging in
    setUpNamePass(args);

    // setup
    controlOut.println("TYPE I\r\n");
    FTPResponse();
    controlOut.println("MODE S\r\n");
    FTPResponse();
    controlOut.println("STRU F\r\n");
    FTPResponse();

    // getting the ftp path from args
    String cmd = args[0];
    String ftpPath;
    // handles the 1 param commands and cp (to local machine)
    if(args.length == 2) {
      ftpPath = parseArgs(args, 1);
    // handles mv and cp (to ftp server)
    } else {
      if(args[1].contains("ftp.3700.network")) {
        ftpPath = parseArgs(args, 1);
      } else {
        ftpPath = parseArgs(args, 2);
      }
    }

    // if the command needs the data channel
    if(cmd.equals("ls") || cmd.equals("cp") || cmd.equals("mv")) {
      // connecting to the data channel
      controlOut.println("PASV\r\n");
      String response = controlIn.readLine();
      ConnectToDataChannel(response);

      if (dataChannel.isConnected()) {
        sendControlDataCmd(ftpPath, args);
      }
      FTPResponse();

    // else
    } else {
      controlOut.println(sendControlOnlyCmd(cmd, ftpPath));
      FTPResponse();
    }

    // end program by ending the connection
    controlOut.println("QUIT\r\n");
    FTPResponse();
    controlChannel.close();
  }

  /**
   * Parses the url/path of the file after the ftp.3700.network string/segment.
   * @param arg which item in the args array that needs to be parsed
   * @return
   */
  public static String parseArgs(String[] args, int arg) {
    String ftpPath;
    int index = args[arg].indexOf("ftp.3700.network");
    // if path is root
    if(args[arg].length() <= index + 16) {
      ftpPath = "/";
    } else {
      ftpPath = args[arg].substring(index + 16);
    }
    return ftpPath;
  }

  /**
   * Returns the correct command that needs to be sent to the control panel.
   * This method only handles to cases that the data channel is not needed.
   */
  public static String sendControlOnlyCmd(String command, String path) {
    String input = "";
    if(command.equals("mkdir")) {
      input += "MKD " + path + "\r\n";
    } else if (command.equals("rmdir")) {
      input += "RMD " + path + "\r\n";
    } else {
      input += "DELE " + path + "\r\n";
    }
    return input;
  }

  /**
   * Executes the correct command that needs to be sent to the control panel and handles the
   * writing and reading of files.
   * This method only handles to cases that the data channel is needed.
   */
  public static void sendControlDataCmd(String ftp,String[] local) throws IOException {
    String cmd = local[0];
    if(cmd.equals("cp")) {
      if(local[2].contains("ftp.3700.network")) {
        // from Local Machine to FTP
        readFile(ftp, local);
      } else {
        // from FTP to Local Machine
        writeFile(ftp, local);
      }
    } else if (cmd.equals("mv")) {
      if(local[2].contains("ftp.3700.network")) {
        // from Local Machine to FTP
        readFile(ftp, local);
      } else {
        // from FTP to Local Machine
        writeFile(ftp, local);
      }
    } else {
      controlOut.println("LIST " + ftp + "\r\n");
      DataResponse();
    }
    dataChannel.close();
  }

  /**
   * Reads the contents of one file after uploading it to the ftp server.
   * @param ftp the ftp file name
   * @param local the local file name
   */
  public static void readFile(String ftp, String[] local) throws IOException {
    controlOut.println("STOR " + ftp + "\r\n");
    FTPResponse();

    byte[] fileContent = Files.readAllBytes(Path.of(local[1]));
    dataOut.write(fileContent);
  }

  /**
   * Reads the contents of one file and writes it to another file.
   * @param ftp the ftp file name
   * @param local the local file name
   */
  public static void writeFile(String ftp, String[] local) throws IOException {
    controlOut.println("RETR " + ftp + "\r\n");
    FTPResponse();

    FileOutputStream writer = new FileOutputStream(local[2]);
    byte[] fileContent = dataIn.readAllBytes();
    writer.write(fileContent);
    writer.close();

    controlOut.println("DELE " + ftp + "\r\n");
  }

  /**
   * @return the parsed url that only contains the username
   */
  public static String parseUsername(String url) {
    String username = "";
    if(url.contains("@")) {
      int indexOfColon = url.indexOf(":", url.indexOf(":") + 1);
      username = url.substring(6, indexOfColon);
     }
    return username;
  }

  /**
   * @return the parsed url that only contains the password
   */
  public static String parsePassword(String url) {
    String password = "";
    if(url.contains("@")) {
      int indexOfColon = url.indexOf(":", url.indexOf(":") + 1);
      int indexOfAt = url.indexOf("@");
      password = url.substring(indexOfColon + 1, indexOfAt);
    }
    return password;
  }

  /**
   * Parses the name and password from the url given and then sends the control channel the
   * appropriate command.
   * If a name and password is not given we simply send the " "USER\r\n" " command.
   */
  public static void setUpNamePass(String[] args) throws IOException {
    // parse to get the username and password if there are any
    String userName;
    String password;
    if (args.length == 2 || args[1].contains("ftp.3700.network")) {
      userName = parseUsername(args[1]);
      password = parsePassword(args[1]);
    } else {
      userName = parseUsername(args[2]);
      password = parsePassword(args[2]);
    }

    if (!userName.isEmpty() && !password.isEmpty()) {
      controlOut.println("USER " + userName + "\r\n");
      FTPResponse();
      controlOut.println("PASS " + password + "\r\n");
      FTPResponse();
    } else {
      controlOut.println("USER\r\n");
      FTPResponse();
    }
  }

  /**
   * Prints out the FTP response.
   */
  public static void FTPResponse() throws IOException {
    String ftpResponse = controlIn.readLine();
    System.out.println(ftpResponse);
    // ends the program if a 4xx, 5xx, or 6xx error message is sent
    if(String.valueOf(ftpResponse.charAt(0)).equals("4") ||
       String.valueOf(ftpResponse.charAt(0)).equals("5") ||
       String.valueOf(ftpResponse.charAt(0)).equals("6")) {
      controlOut.println("QUIT\r\n");
      FTPResponse();
      controlChannel.close();
    }
  }

  /**
   * Gets the response from the data channel.
   */
  public static void DataResponse() {
    if (!dataChannel.isConnected()) {
      System.out.println("Sorry for the inconvenience, please try again.");
    } else {
      Scanner s = new Scanner(dataIn);
      while(s.hasNext()) {
        System.out.println(s.next());
      }
    }
  }

  /**
   * Parses the string to get the IP address.
   * Changes all ',' to '.' .
   */
  public static String getIPAddress(String ftpResponse) {
    String IP = parseResponse(ftpResponse, 1);
    String address = "";
    for (int i =0; i <IP.length(); i ++) {
      if(IP.charAt(i) == ',') {
        address += ".";
      } else
        address += String.valueOf(IP.charAt(i));
    }
    return address;
  }

  /**
   * Parses the response to get the port.
   * Removes the ',' and splits the string into two integers.
   */
  public static int getPortNum(String ftpResponse) {
    String IP = parseResponse(ftpResponse, 2);
    int first = Integer.parseInt(IP.substring(0, IP.indexOf(",")));
    int second = Integer.parseInt(IP.substring(IP.indexOf(",") + 1));
    int port = (first << 8) + second;
    return port;
  }


  /**
   * Parses the FTP response to the get address or port
   * @param response the FTP response when asked to connect to the data channel
   * @param type indicates whether-or-not the address or port is needed
   */
  public static String parseResponse(String response, int type) {
    int endIndex = 0;
    int counter = 0;
    for (int i = 0; i < response.length(); i ++) {
      if(response.charAt(i) == ',') {
        counter ++;
      }
      if (counter == 4) {
        endIndex = i;
        break;
      }
    }
    if(type == 1) {
      return response.substring(response.indexOf("(") + 1, endIndex);
    } else {
      return response.substring(endIndex + 1, response.length() - 2);
    }
  }

  /**
   * Connects to the data channel and setups the input and output streams.
   */
  public static void ConnectToDataChannel(String response) throws IOException {
    // getting username and port of data channel then connect to it
    dataChannel = new Socket(getIPAddress(response), getPortNum(response));
    dataIn = new DataInputStream(dataChannel.getInputStream());
    dataOut = new DataOutputStream(dataChannel.getOutputStream());
  }
}