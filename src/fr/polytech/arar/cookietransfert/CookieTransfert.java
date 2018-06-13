package fr.polytech.arar.cookietransfert;

import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;

public class CookieTransfert extends Application {
	
	private WebView loggerView;
	private WebEngine loggerEngine;
	private final String headContent =
			"<html>" +
				"<head>" +
					"<meta charset=\"utf-8\"/>" +
					"<style>" +
						"p {" +
							"font-family: Calibri, Verdana, Arial, serif;" +
						"}" +
					"</style>" +
				"</head>" +
				"<body>";
	private String currentContent;
	private final String footContent =
				"</body>" +
			"</html>";
	
	private TextField inputAdresse = new TextField();
	private TextField inputFilename = new TextField();
	Button submit = new Button("Submit");
	
	public static void main(String[] args){
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		inputAdresse.setPromptText("Address");
		inputFilename.setPromptText("File name");
		
		/* /DEBUG\ */
		inputAdresse.setText("192.168.0.19");
		inputFilename.setText("cookie.txt");
		/* \DEBUG/ */
		
		setCurrentContent("");
		loggerView = new WebView();
		loggerEngine = loggerView.getEngine();
	
		Log.register(CookieTransfert.this::log);

		submit.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				final String serverIP = inputAdresse.getText();
				final String filename = inputFilename.getText();
				
				if(serverIP.equals("") || filename.equals(""))
					log("Error: Please fill all fields before trying to submit any information");
				else {
					// STEP 1 : connexion to server
					try {
						InetAddress inetAddress = InetAddress.getByName(serverIP);
						
						// Get the localFilePath
						String localFilePath = "file/client/";
						String[] split = filename.split(File.separator);
						
						if (split.length <= 0)
							localFilePath += "filename.txt";
						else
							localFilePath += split[split.length - 1];
						
						final String f_localFilePath = localFilePath;

						// STEP 2: sending request RRQ to pumpkin
						new Thread(() -> {
							ErrorCode code = TransferManager.receiveFile(f_localFilePath, filename, inetAddress);
							Log.println("CookieTransfert> Receive file returned " + code.getCode() + " (" + code.name() + ")");
						}).start();
					}
					catch(UnknownHostException e) {
						e.printStackTrace();
						log("Error: Invalid address");
					}
				}
			}
		});
		
		GridPane gridPane = new GridPane();
		gridPane.add(inputAdresse,0,0);
		gridPane.add(inputFilename,1,0);
		gridPane.add(submit,2,0);
		
		VBox root = new VBox(20, gridPane, loggerView);
		
		root.setPrefSize(600,600);
		primaryStage.setScene(new Scene(root));
		primaryStage.setTitle("[CLIENT STF] CookieTransfert");
		primaryStage.show();

		Log.println("Client ready to process!");
	}
	
	@SuppressWarnings("ConstantConditions")
	public synchronized void log(@NotNull String log) {
		if (log == null)
			throw new NullPointerException();
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd'/'MM'/'yyyy 'at' HH:mm:ss");
		String message = "<b><span style=\"color: #874ab7;\">[" + sdf.format(new Date()) + "]</span></b> " + log;
		
		addCurrentContent("<p>" + message + "</p>");
		
		if (loggerEngine != null) {
			Platform.runLater(() -> loggerEngine.loadContent(
					headContent +
					getCurrentContent() +
					footContent
			));
		}
	}
	
	/* GETTERS & SETTERS */
	
	public synchronized String getCurrentContent() {
		return currentContent;
	}
	
	public synchronized void setCurrentContent(String currentContent) {
		this.currentContent = currentContent;
	}
	
	public synchronized void addCurrentContent(String currentContent) {
		setCurrentContent(getCurrentContent() + currentContent);
	}
}
