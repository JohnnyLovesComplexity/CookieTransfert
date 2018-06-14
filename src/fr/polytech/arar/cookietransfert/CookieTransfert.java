package fr.polytech.arar.cookietransfert;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
		
		/* /DEBUG\ *
		inputAdresse.setText("192.168.0.19");
		inputFilename.setText("blackhole.jpg");
		* \DEBUG/ */
		
		setCurrentContent("");
		loggerView = new WebView();
		loggerEngine = loggerView.getEngine();
	
		Log.register(CookieTransfert.this::log);

		submit.setOnAction(event -> {
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
					String[] split = filename.split(File.separator.replace("\\", "\\\\"));
					
					if (split.length <= 0)
						localFilePath += "filename.txt";
					else
						localFilePath += split[split.length - 1];
					
					final String f_localFilePath = localFilePath;

					// STEP 2: sending request RRQ to pumpkin
					new Thread(() -> {
						ValueCode code = null;
						try {
							code = TransferManager.receiveFile(f_localFilePath, filename, inetAddress);
							
							if (code == ValueCode.OK) {
								SoundManager.play(SoundManager.BELL);
								Log.println("CookieTransfert> Receive file returned " + code.getCode() + " (" + code.name() + ")");
							}
						} catch (SocketTimeoutException e) {
							Log.println("CookieTransfert> Connection lost.");
							//e.printStackTrace();
						}
					}).start();
				}
				catch(UnknownHostException e) {
					e.printStackTrace();
					log("Error: Invalid address");
				}
			}
		});

		HBox hbox = new HBox();
		Image image = new Image(CookieTransfert.class.getResourceAsStream("/images/logo64.png"));
		Label label1 = new Label();
		label1.setGraphic(new ImageView(image));
		hbox.setSpacing(10);
		hbox.getChildren().add((label1));

		HBox hbox2 = new HBox();
		Image volumeOn = new Image(CookieTransfert.class.getResourceAsStream("/images/volume_on.png"));
		Image volumeOff = new Image(CookieTransfert.class.getResourceAsStream("/images/volume_off.png"));
		Button mute = new Button();
		mute.setGraphic(new ImageView(volumeOn));
		mute.setId("mute");
		hbox2.setSpacing(10);
		hbox2.getChildren().add((mute));

		mute.setOnAction(event -> {
			if (SoundManager.toggleSoundActivated())
				mute.setGraphic(new ImageView(volumeOn));
			else
				mute.setGraphic(new ImageView(volumeOff));
		});

		GridPane gridPane = new GridPane();
		gridPane.add(hbox,0,0);
		gridPane.add(inputAdresse,1,0);
		gridPane.add(inputFilename,2,0);
		gridPane.add(submit,3,0);
		gridPane.add(mute,4,0);

		gridPane.setHgap(10); //horizontal gap in pixels => that's what you are asking for
		gridPane.setVgap(10); //vertical gap in pixels
		gridPane.setPadding(new Insets(10, 10, 10, 10));

		VBox root = new VBox(20, gridPane, loggerView);
		root.setStyle("-fx-background-color: #295396");

		submit.setId("submit");

		root.setPrefSize(600,600);
		Scene scene = new Scene(root);
		scene.getStylesheets().add(CookieTransfert.class.getResource("/css/style.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.setTitle("[CLIENT STF] CookieTransfert");
		primaryStage.getIcons().add(new Image(CookieTransfert.class.getResourceAsStream("/images/logo64.png")));
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
