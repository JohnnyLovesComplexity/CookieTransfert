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

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class CookieTransfert extends Application {
    public String serverIP = "127.0.0.1";
    public String filename = "";

    private TextFlow echange = new TextFlow();
    private TextField inputAdresse = new TextField();
    private TextField inputFilename = new TextField();
    Button submit = new Button("Submit");

    private final static int PACKET_SIZE = 516; //512 + 2 for the op_code + 2 for the number of the bloc

    @Override
    public void start(Stage primaryStage) throws Exception {
        inputAdresse.setText("Adress");
        inputFilename.setText("Filename");
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
                }else{
                    inputAdresse.setText("");
                    inputFilename.setText("");
                    //TODO : echange
                    // STEP 1 : connexion to server
                    try{
                        InetAddress inetAddress = InetAddress.getByName(serverIP);
                    }catch(UnknownHostException e){
                        Text t3 = new Text();
                        t3.setStyle("-fx-fill: #b70d1b;-fx-font-weight:bold;");
                        t3.setText(">>ERROR : Invalid adress " + "\n");
                        echange.getChildren().add(t3);
                    }
                    // STEP 2: sending request RRQ to pumpkin

                        /*// STEP 3: receive file
                         cr_rv = receiveFile();

                        // STEP 4: write file to local disc
                        writeFile(byteOutOS, fileName);*/

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
