package fr.polytech.arar.cookietransfert;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class CookieTransfert extends Application {

    private static final byte OP_RRQ = 1;
    private static final byte OP_WRQ = 2;
    private static final byte OP_DATA = 3;
    private static final byte OP_ACK = 4;
    private static final byte OP_ERROR = 5;

    private final static int PACKET_SIZE = 516; //512 + 2 for the op_code + 2 for the number of the bloc

    @Override
    public void start(Stage primaryStage) throws Exception {
        String file = "cookie.txt";

        // STEP 1 : connexion to server
        InetAddress inetAddress = InetAddress.getByName(ConnectionData.serverIP);
        DatagramSocket datagramSocket = new DatagramSocket();
        byte[] requestByteArray = createRequest(OP_RRQ, file, "octet");
        DatagramPacket datagramPacket = new DatagramPacket(requestByteArray,
                requestByteArray.length, inetAddress, ConnectionData.serverPort);

        //connection

        // STEP 2: sending request RRQ to pumpkin
        datagramSocket.send(datagramPacket);

        /*// STEP 3: receive file
         cr_rv = receiveFile();

        // STEP 4: write file to local disc
        writeFile(byteOutOS, fileName);*/
    }

    private byte[] createRequest(final byte op_code, final String file,
                                 final String mode) {
        byte zeroByte = 0;
        int rrqByteLength = 2 + file.length() + 1 + mode.length() + 1;
        byte[] rrqByteArray = new byte[rrqByteLength];

        int position = 0;
        rrqByteArray[position] = zeroByte;
        position++;
        rrqByteArray[position] = op_code;
        position++;
        for (int i = 0; i < file.length(); i++) {
            rrqByteArray[position] = (byte) file.charAt(i);
            position++;
        }
        rrqByteArray[position] = zeroByte;
        position++;
        for (int i = 0; i < mode.length(); i++) {
            rrqByteArray[position] = (byte) mode.charAt(i);
            position++;
        }
        rrqByteArray[position] = zeroByte;
        return rrqByteArray;
    }

    public static void main(String[] args){
        launch(args);
    }
}
