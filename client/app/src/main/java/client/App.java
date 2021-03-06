/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package client;

// Used for making JavaFX work
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.*;
import javafx.scene.text.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.*;

// Needed for the net portion of the code
import java.util.*;
import java.net.*;
import java.io.*;

public class App extends Application {

    // Setting the window height and width as constants
    private final static int HEIGHT = 640;
    private final static int WIDTH = 480;

    // sets the max area of the message display area
    private final static int MAX_HEIGHT = 21;

    // used to help write messages to the display
    private boolean logEnd = false;
    private int lastWritenMessage = 0;

    // padding of the boxes used in the layout
    private final static int HBOX_PADDING = 10;
    private final static int VBOX_PADDING = 10;

    private final static String TITLE = "Local Chat";

    // The username of the client will use, set to the empty string
    private String userName = "";

    // setting up the buttons
        
    // This will send the text data to the server
    private Button send = new Button("send");
        
    // This will apply the changes to the setting to the program
    private Button setUserName = new Button("connect");
        
    // These two will be used to change between the two scenes
    private Button settings = new Button("disconnect");

    // making the text-boxes

    // This is the field that the user will type the text to be sent into
    private TextField messageBox = new TextField();

    // This will hold the new username that the user wants
    private TextField newUserName = new TextField();

    // Displays the messages that the user gets
    private TextFlow messageDisplay = new TextFlow();


    // Setting the two scenes to null

    private Scene mainScene = null;
    private Scene settingsScene = null;

    // Creating networking class
    private static class Channel {

        // The in and out streams that the client and server use
        private static PrintWriter messenger = null;
        public static BufferedReader receiver = null;

        private static  Socket connection = null;

        public Channel() {
            try {
                // Sets up the connection destination and port
                connection = new Socket("localhost", 6666);

                // Creates both I/O streams
                messenger = new PrintWriter(connection.getOutputStream(), true);
                receiver = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        public static String getMessage() {
            try {
                // If there is anything in the stream it will read
                if (receiver.ready()) {

                    // Gets the message from the stream
                    String message = receiver.readLine();
                    if ("" != message) {

                        // Prints to the ternimnal and returns it
                        System.out.println(message);
                        return message;
                    }
                }
                // an empty String is sent if there is no message
                return "";
            }
            catch (Exception e) {
                e.printStackTrace();
                return "Error server offline";
            }
        }

        public static void sendMessage(String message) {
            try {
                // Simply writes the message to the out-stream
                messenger.println(message);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // creates a instance of the Channel code
    Channel serverConnection = new Channel();
    
    @Override
    public void stop() {
        // When the window is closed it will send a disconnect message
        serverConnection.sendMessage(userName + " disconnected");

        // sends magic number
        disconnect();
        System.out.println("Goodbye, World!");
    }

    public void disconnect() {
        // Sends the magic number that the thread uses to terminate itself
        serverConnection.sendMessage("eRykMdxkNnBqI1cI8SH8X8SQNFCtxzDd");
    }

    @Override
    public void start(Stage stage) {

        // setting up the layout of the main scene
        VBox mainShelf = new VBox(VBOX_PADDING);

        mainShelf.getChildren().addAll(messageDisplay, messageBox, send);

        HBox mainHolder = new HBox(HBOX_PADDING);

        mainHolder.getChildren().addAll(settings, mainShelf);

        // setting up the layout of the settings scene
        VBox settingsShelf = new VBox(VBOX_PADDING);

        settingsShelf.getChildren().addAll(newUserName, setUserName);

        HBox settingsHolder = new HBox(HBOX_PADDING);

        settingsHolder.getChildren().addAll(new Label("Username:"), settingsShelf);

        // Creating the two scenes
        this.mainScene = new Scene(new StackPane(mainHolder), HEIGHT, WIDTH);
        this.settingsScene =
            new Scene(new StackPane(settingsHolder), HEIGHT, WIDTH);

        // Adding the button actions to the buttons
        send.setOnAction(this::sendText);
        messageBox.setOnAction(this::sendText);

        // The method used to start the chat-room after getting a user name
        EventHandler<ActionEvent> mkConnection =
            new EventHandler<ActionEvent>() {
                public void handle(ActionEvent e) {

                    // sets the username
                    applySetting();
                    System.out.println("Pressed connect...");

                    // changes to the main scene
                    stage.setScene(mainScene);
                }
            };

        // The method used to leave room and take user back to connect screen
        EventHandler<ActionEvent> leave =
            new EventHandler<ActionEvent>() {
                public void handle(ActionEvent e) {
                    serverConnection.sendMessage(userName + " disconnected");
                    System.out.println("Pressed disconnect...");

                    // changes to the connection scene
                    stage.setScene(settingsScene);
                }
            };

        // adds methods to various objects
        setUserName.setOnAction(mkConnection);
        newUserName.setOnAction(mkConnection);
        settings.setOnAction(leave);

        // sets the text box to a set of newline chars
        for (int ii = 0; ii < MAX_HEIGHT; ii++) {
            messageDisplay.getChildren().add(new Text("\n"));
        }

        // A timer that will read from the buffer when it's got a message and
        // write it
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                String message = Channel.getMessage();
                if ("" != message) {
                    write(message + "\n");
                }
            }
        };

        // setting up the stage
        stage.setScene(settingsScene);
        stage.setTitle(TITLE);
        stage.show();

        // stating the message reader
        timer.start();
    }

    private void sendText(ActionEvent e) {
        System.out.println("Sending...");

        // makes sure that their is a message and a username before sending
        // the message
        if (!userNameEmpty() && !messageBoxEmpty()) {

            // creates the message in its format
            String message = this.userName + ": " + this.messageBox.getText();

            // clears the message box 
            this.messageBox.clear();

            // writes to the terminal
            System.out.println(message);

            // sends the message to the server and writes it to the text area
            serverConnection.sendMessage(message);
            write(message + "\n");
        }
        // Error that happen if no username or message is given
        else if (userNameEmpty()) {
            System.out.println("you do not have a username," +
                    " go to settings to set one");
        }
        else {
            System.out.println("Please enter text" +
                    " to the text box to send messages");
        }
    }

    private void applySetting() {
        System.out.println("Applying Settings...");

        // if username given sends it with the connection message
        if (!newUserNameEmpty()) {
            this.userName = newUserName.getText();
            serverConnection.sendMessage(userName + " joined");
            System.out.println("New Username: \"" + this.userName + "\"");
            this.newUserName.clear();
        }
        else {
            // user must give a new username to move forward
            System.out.println("No Username Given");
        }
    }

    public static void main(String[] args) {
        launch();
    }

    private boolean userNameEmpty() {
        return "" == this.userName ? true:false;
    }

    private boolean messageBoxEmpty() {
        return "" == this.messageBox.getText() ? true:false;
    }

    private boolean newUserNameEmpty() {
        return "" == this.newUserName.getText() ? true:false;
    }

    private void write(String message) {
        // if the messages board is not full yet
        if (!logEnd) {
            // writes to the text-box
            this.messageDisplay.getChildren().set(lastWritenMessage++,
                    new Text(message));
            
            // checks if the messages have reached their limit
            if (MAX_HEIGHT <= lastWritenMessage) { logEnd = true; }
        }
        else {
            // if the next message will over flow the text area clears them all
            this.messageDisplay.getChildren().clear();

            // writes the new message as the first entry in the text area
            this.messageDisplay.getChildren().add(new Text(message));

            // sets the rest of the chars to empty
            for (int ii = 1; ii < MAX_HEIGHT; ii++) {
                this.messageDisplay.getChildren().add(new Text("\n"));
            }

            // resets these variables to their new values
            logEnd = false;
            lastWritenMessage = 1;
        }
    }
}

