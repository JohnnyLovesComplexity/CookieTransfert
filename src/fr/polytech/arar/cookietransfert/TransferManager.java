package fr.polytech.arar.cookietransfert;

import fr.berger.enhancedlist.Couple;
import fr.berger.enhancedlist.lexicon.Lexicon;
import fr.berger.enhancedlist.lexicon.LexiconBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.naming.OperationNotSupportedException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.file.Files;
import java.util.Arrays;

public class TransferManager {
	
	public static final int TFTP_PORT = 69;
	public static final int TFTP_MAX_DATA_LENGTH = 512;
	public static final int ERROR_LIMIT = 3;
	
	/**
	 * Send a READ REQUEST (RRQ) to the server at the address given in argument to get the file
	 * @param localFile
	 * @param distantFilePath
	 * @param address
	 * @return
	 */
	@SuppressWarnings("ConstantConditions")
	public static ErrorCode receiveFile(@NotNull File localFile, @NotNull String distantFilePath, @NotNull InetAddress address) {
		// Test parameters
		if (localFile == null)
			throw new NullPointerException("localFile must not be null");
		
		if (localFile.exists()) {
			if (!localFile.delete())
				return ErrorCode.CANNOT_DELETE_LOCAL_FILE;
		}
		
		if (distantFilePath == null)
			throw new NullPointerException("distantFilePath must not be null");
		
		if (distantFilePath.equals(""))
			throw new IllegalArgumentException("distantFilePath must not be empty");
		
		if (address == null)
			throw new NullPointerException("address must not be null");
		
		// Test if the address is reachable
		/*try {
			if (!address.isReachable(500))
				throw new IllegalArgumentException("The address " + address.getHostAddress() + " is not reachable.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("The address " + address.getHostAddress() + " is not reachable.");
		}*/
		
		// Create a list of block number
		Lexicon<Integer> blockNumbers = new LexiconBuilder<>(Integer.class)
				.setAcceptNullValues(false)
				.setAcceptDuplicates(false)
				.createLexicon();
		
		// Prepare the request
		String mode = "octet";
		byte[] RRQ = createRequest(OPCode.RRQ.getCode(), distantFilePath, mode);

		String request = Arrays.toString(RRQ);

		Log.println("TransferManager.receiveFile> Sending \"" + request + "\" to server...");
		
		// Create the UDP communication
		Connection co = new Connection(address, TFTP_PORT);
		
		// Send the request
		co.send(RRQ);
		Log.println("TransferManager.receiveFile> Sent.");
		
		// Wait for an answer
		Couple<String, DatagramPacket> result = co.receive();
		Log.println("TransferManager.receiveFile> Answer received: \"" + result.getX() + "\"");
		
		// 'repetition' count the number of ACK-fail
		int repetition = 0;
		while (result != null && result.getY() != null && !isLastPacket(result.getY())) {
			repetition = 0;
			
			// Send ACK
			byte[] blockNumbersBytes = { (byte) (blockNumbers.last() & 0xFF), (byte) ((blockNumbers.last() >> 8) & 0xFF) };
			byte[] ACK = { 0, OPCode.ACK.getCode(), blockNumbersBytes[0], blockNumbersBytes[1] };
			Log.println("TransferManager.receiveFile> ACK = " + Arrays.toString(ACK));
			try {
				co.answer(/*OPCode.ACK.getRepresentation()*/ACK);
			} catch (OperationNotSupportedException e) {
				System.err.println("FATAL ERROR: CANNOT SEND ACK TO SERVER " + address.getHostAddress());
				e.printStackTrace();
				repetition++;
				
				if (repetition >= ERROR_LIMIT)
					return ErrorCode.TRANSFER_ERROR;
			}
			
			// Receive a new block of data
			result = co.receive();
			
			if (result != null && result.getY() != null) {
				byte[] data = result.getY().getData();
				
				if (data != null && data.length >= 2) {
					byte[] header = {data[0], data[1]};
					
					if (header[0] != OPCode.DATA.getCode())
						throw new RuntimeException("Receive request " + header[0] + " instead of " + OPCode.DATA.getCode() + " (" + OPCode.DATA.getRepresentation() + ") to receive the file.");


					/*if (header[1] == OPCode.ERROR.getCode()) {
						String errorCode = new String(data, 3, 1);
						//TODO : treat errors ErrorCode
					} else if (header[1] == OPCode.DATA.getCode()) {

					}*/
					
					byte blockNumber = header[1];
					
					// If the blockNumber is new, add the data block. If not, resend a ACK
					if (!blockNumbers.contains((int) blockNumber)) {
						blockNumbers.add((int) blockNumber);
						
						if (data.length > 2) {
							byte[] fileData = new byte[data.length - 2];
							System.arraycopy(data, 2, fileData, 0, data.length - 2);
							
							try {
								Files.write(localFile.toPath(), fileData);
							} catch (IOException e) {
								e.printStackTrace();
								return ErrorCode.CANNOT_WRITE_LOCAL_FILE;
							}
						} else
							result = null; // if result = null, then stop while-loop
					}
				}
				else
					result = null;
			}
			else
				result = null;
		}
		
		return ErrorCode.OK;
	}
	@SuppressWarnings("ConstantConditions")
	public static ErrorCode receiveFile(@Nullable String localFilePath, @NotNull String distantFilePath, @NotNull InetAddress address) {
		File f = new File(localFilePath);
		
		return receiveFile(f, distantFilePath, address);
	}
	@SuppressWarnings("ConstantConditions")
	public static ErrorCode receiveFile(@NotNull String distantFilePath, @NotNull InetAddress address) {
		if (distantFilePath == null || "".equals(distantFilePath))
			throw new IllegalArgumentException("Distant file path not valid.");
		
		// Split the path into piece according to the file separator character ("/" for UNIX, "\" for Windows)
		String[] sp_path = distantFilePath.split(File.separator);
		
		if (sp_path.length <= 0)
			throw new IllegalArgumentException("Distant file path not valid.");
		
		// Get the filename (with extension)
		String filename = sp_path[sp_path.length - 1];
		
		return receiveFile(filename, distantFilePath, address);
	}

	private static byte[] createRequest(final byte opCode, final String fileName,
								 final String mode) {
		byte opCodes[] = new byte[] {0, opCode};
		
		byte zeroByte = 0;
		int rrqByteLength = 2 + fileName.length() + 1 + mode.length() + 1;
		byte[] rrqByteArray = new byte[rrqByteLength];

		int position = 0;
		rrqByteArray[position] = opCodes[0];
		position++;
		rrqByteArray[position] = opCodes[1];
		position++;
		for (int i = 0; i < fileName.length(); i++) {
			rrqByteArray[position] = (byte) fileName.charAt(i);
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

	@SuppressWarnings("ConstantConditions")
	private static boolean isLastPacket(@NotNull DatagramPacket datagramPacket) {
		if (datagramPacket == null)
			throw new NullPointerException();
		
		return datagramPacket.getLength() < TFTP_MAX_DATA_LENGTH + 4;
	}
}
