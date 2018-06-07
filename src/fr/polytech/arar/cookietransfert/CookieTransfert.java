package fr.polytech.arar.cookietransfert;

import javafx.application.Application;
import javafx.stage.Stage;

public class CookieTransfert extends Application {

    private static final byte OP_RRQ = 1;
    private static final byte OP_WRQ = 2;
    private static final byte OP_DATA = 3;
    private static final byte OP_ACK = 4;
    private static final byte OP_ERROR = 5;

    private final static int PACKET_SIZE = 516; //512 + 2 for the op_code + 2 for the number of the bloc

    @Override
    public void start(Stage primaryStage) throws Exception {

    }

    public static void main(String[] args){
        launch(args);
    }
}
