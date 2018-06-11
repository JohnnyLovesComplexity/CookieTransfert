package fr.polytech.arar.cookietransfert;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Function;

public class CookieTransfert extends Application {
    public String serverIP = "127.0.0.1";
    public String filename = "";

    private TextFlow echange = new TextFlow();
    private TextField inputAdresse = new TextField();
    private TextField inputFilename = new TextField();
    Button submit = new Button("Submit");
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        inputAdresse.setText(/*"Adress"*/"192.168.43.32");
        inputFilename.setText(/*"Filename"*/"cookie.txt");
        primaryStage.setTitle("CLIENT STF");
        Text t1 = new Text();
        t1.setStyle("-fx-fill: #4F8A10;-fx-font-weight:bold;");
        t1.setText(">>STF Application : Welcome\n");
        echange.getChildren().add(t1);
        echange.setLineSpacing(2);
        echange.setMinWidth(600);
        echange.setMinHeight(500);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(echange);
        scrollPane.setPrefSize(600,500);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                serverIP = inputAdresse.getText();
                filename = inputFilename.getText();
                if(serverIP.equals("") || filename.equals("")){
                    Text t3 = new Text();
                    t3.setStyle("-fx-fill: #b70d1b;-fx-font-weight:bold;");
                    t3.setText(">>ERROR : Data not valid " + "\n");
                    echange.getChildren().add(t3);
                }
                else {
                    inputAdresse.setText("");
                    inputFilename.setText("");
                    //TODO : echange
                    Log.register(new Function<String, Void>() {
	                    @Override
	                    public Void apply(String s) {
		                    Text text = new Text();
		                    text.setStyle("-fx-fill: #b70d1b;-fx-font-weight:bold;");
		                    text.setText(s);
	                    	echange.getChildren().add(new Text(s));
		                    return null;
	                    }
                    });
                    // STEP 1 : connexion to server
                    try {
                        InetAddress inetAddress = InetAddress.getByName(serverIP);

		                // STEP 2: sending request RRQ to pumpkin
		                ErrorCode code = TransferManager.receiveFile("filename.txt", filename, inetAddress);
		                Log.println("CookieTransfert> Receive file returned " + code.getCode() + " (" + code.name() + ")");
                    }
                    catch(UnknownHostException e) {
                        Text t3 = new Text();
                        t3.setStyle("-fx-fill: #b70d1b;-fx-font-weight:bold;");
                        t3.setText(">>ERROR : Invalid adress " + "\n");
                        echange.getChildren().add(t3);
	                }
                    

                    Text t3 = new Text();
                    t3.setStyle("-fx-fill: #874ab7;-fx-font-weight:bold;");
                    t3.setText(">>PUMPKIN : " + "\n");
                    echange.getChildren().add(t3);

                }
            }
        });
        GridPane gridPane = new GridPane();
        gridPane.add(inputAdresse,0,0);
        gridPane.add(inputFilename,1,0);
        gridPane.add(submit,2,0);
        VBox root = new VBox(20,gridPane,scrollPane);
        root.setPrefSize(600,600);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();


    }
    public static void main(String[] args){
        launch(args);
    }
}
