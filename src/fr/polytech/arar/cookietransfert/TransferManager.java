package fr.polytech.arar.cookietransfert;

import fr.berger.enhancedlist.Couple;
import fr.berger.enhancedlist.lexicon.Lexicon;
import fr.berger.enhancedlist.lexicon.LexiconBuilder;
import fr.polytech.arar.cookietransfert.exceptions.TFTPException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.OperationNotSupportedException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
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
	public static ValueCode receiveFile(@NotNull File localFile, @NotNull String distantFilePath, @NotNull InetAddress address) throws SocketTimeoutException {
		// Test parameters
		if (localFile == null)
			throw new NullPointerException("localFile must not be null");
		
		if (localFile.exists()) {
			if (!localFile.delete())
				return ValueCode.CANNOT_DELETE_LOCAL_FILE;
		}
		
		if (distantFilePath == null)
			throw new NullPointerException("distantFilePath must not be null");
		
		if (distantFilePath.equals(""))
			throw new IllegalArgumentException("distantFilePath must not be empty");
		
		if (address == null)
			throw new NullPointerException("address must not be null");
		
		// Create a list of block number
		Lexicon<Integer> blockNumbers = new LexiconBuilder<>(Integer.class)
				.setAcceptNullValues(false)
				.setAcceptDuplicates(false)
				.createLexicon();
		
		// Prepare the request
		String mode = "octet";
		byte[] RRQ = createRequest(OPCode.RRQ.getCode(), distantFilePath, mode);

		String request = Arrays.toString(RRQ);

		Log.println("TransferManager.receiveFile> Sending \"" + request + "\" to server " + address + ":" + TFTP_PORT + "...");
		
		// Create the UDP communication
		Connection co = new Connection(address, TFTP_PORT);
		
		// Send the request
		co.send(RRQ);
		Log.println("TransferManager.receiveFile> Sent.");
		
		// Wait for an answer
		Couple<String, DatagramPacket> result;
		
		// 'repetition' count the number of ACK-fail
		int repetition = 0;
		do {
			repetition = 0;
			
			// Receive a new block of data
			result = co.receive();
			
			if (result == null) {
				Log.println("TransferManager.receiveFile> Connection lost");
				return ValueCode.TRANSFER_ERROR;
			}
			
			Log.println("TransferManager.receiveFile> Received from " + result.getY().getAddress().getHostAddress() + " \"" + result.getY().getAddress().getCanonicalHostName() + "\" with port " + result.getY().getPort() + ". The size of the answer is " + result.getY().getLength() + " byte(s).");
			
			if (result != null && result.getY() != null) {
				byte[] data = result.getY().getData();
				
				if (data != null && data.length >= 4) {
					int opCodeReceived = getOpCode(data);
					int blockNumberReceived = getBlockNumber(data);
					
					if (opCodeReceived != OPCode.DATA.getCode()) {
						if (opCodeReceived == OPCode.ERROR.getCode()){
							int error = getErrorCode(data);
							Log.println("TransferManager.receiveFile> ERROR : The server return an error code. Please make sure that the filename you typed is correct.");
							Log.println("TransferManager.receiveFile> ERROR : " + ErrorCode.errorMessage(error));
							throw new TFTPException("The server return an error code. Please make sure that the filename you typed is correct.");
						}
						else
							throw new RuntimeException("Receive request " + opCodeReceived + " instead of " + OPCode.DATA.getCode() + " (" + OPCode.DATA.getRepresentation() + ") to receive the file.");
					}

					// If the blockNumber is new, add the data block. If not, resend a ACK
					if (!blockNumbers.contains(blockNumberReceived)) {
						blockNumbers.add(blockNumberReceived);
						
						if (data.length > 4) {
							byte[] fileData = getFileData(data, isLastPacket(result.getY()));
							
							String answer_str = new String(fileData, StandardCharsets.UTF_8).replaceAll("\r\n|\n", " ").replaceAll(new String(new char[] {'\0'}), "");
							boolean isWellEncoded = true;
							
							if (answer_str.contains("�"))
								isWellEncoded = false;
							else {
								try {
									answer_str.getBytes("UTF-8");
								} catch (UnsupportedEncodingException ex) {
									isWellEncoded = false;
								}
							}
							
							Log.println("TransferManager.receiveFile> Answer received: [opCode=" + opCodeReceived + " (" + OPCode.from((byte) opCodeReceived) + ")] [blockNumber=" + blockNumberReceived + "]" + (isWellEncoded ? " \"" + answer_str + "\"" : ""));
							
							FileOutputStream fos = null;
							
							try {
								// If the file does not exist, so don't append the data. Otherwise, append mode is enable
								fos = new FileOutputStream(localFile, localFile.exists());
								fos.write(fileData);
							} catch (IOException e) {
								e.printStackTrace();
								return ValueCode.CANNOT_WRITE_LOCAL_FILE;
							}
							// Finally, close the file output stream
							finally {
								if (fos != null) {
									try {
										fos.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
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
			
			
			if (result != null) {
				// Send ACK
				byte[] blockNumbersBytes = {(byte) ((blockNumbers.last() >> 8) & 0xFF), (byte) (blockNumbers.last() & 0xFF)};
				byte[] ACK = {0, OPCode.ACK.getCode(), blockNumbersBytes[0], blockNumbersBytes[1]};
				Log.println("TransferManager.receiveFile> Send ACK = " + Arrays.toString(ACK));
				try {
					co.answer(/*OPCode.ACK.getRepresentation()*/ACK);
				} catch (OperationNotSupportedException e) {
					System.err.println("FATAL ERROR: CANNOT SEND ACK TO SERVER " + address.getHostAddress());
					e.printStackTrace();
					repetition++;
					
					if (repetition >= ERROR_LIMIT)
						return ValueCode.TRANSFER_ERROR;
				}
			}
		} while (result != null && !isLastPacket(result.getY()));
		
		return ValueCode.OK;
	}
	@SuppressWarnings("ConstantConditions")
	public static ValueCode receiveFile(@Nullable String localFilePath, @NotNull String distantFilePath, @NotNull InetAddress address) throws SocketTimeoutException {
		File f = new File(localFilePath);
		
		return receiveFile(f, distantFilePath, address);
	}
	@SuppressWarnings("ConstantConditions")
	public static ValueCode receiveFile(@NotNull String distantFilePath, @NotNull InetAddress address) throws SocketTimeoutException {
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

	private static byte[] createRequest(final byte opCode, final String fileName, final String mode) {
		byte opCodeFormatted[] = new byte[] {0, opCode};
		
		byte zeroByte = 0;
		int rrqByteLength = 2 + fileName.length() + 1 + mode.length() + 1;
		byte[] rrqByteArray = new byte[rrqByteLength];

		int position = 0;
		rrqByteArray[position] = opCodeFormatted[0];
		position++;
		rrqByteArray[position] = opCodeFormatted[1];
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
	
	/**
	 * Give the OpCode from {@code data}
	 * @param data The received data to analyse
	 * @return Return the OpCode found in {@code data}
	 */
	@SuppressWarnings("ConstantConditions")
	private static int getOpCode(@NotNull byte[] data) {
		if (data == null)
			throw new NullPointerException();
		
		if (data.length < 2)
			throw new IllegalArgumentException("data must be greater or equal to 2 bytes at least.");
		
		byte[] opCodesFormatted = { data[0], data[1] };
		return ByteConversion.convertBytesToInt(opCodesFormatted) /*((opCodesFormatted[0] & 0xff) << 8) | (opCodesFormatted[1] & 0xFF)*/;
	}

	private static int getErrorCode(@NotNull byte[] data) {
		if (data == null)
			throw new NullPointerException();

		if (data.length < 2)
			throw new IllegalArgumentException("data must be greater or equal to 2 bytes at least.");

		byte[] errorCodesFormatted = { data[2], data[3] };
		return ByteConversion.convertBytesToInt(errorCodesFormatted);
	}
	
	/**
	 * Give the block number from {@code data}
	 * @param data The received data to analyse
	 * @return Return the block number found in {@code data}
	 */
	@SuppressWarnings("ConstantConditions")
	private static int getBlockNumber(@NotNull byte[] data) {
		if (data == null)
			throw new NullPointerException();
		
		if (data.length < 4)
			throw new IllegalArgumentException("data must be greater or equal to 4 bytes at least.");
		
		byte[] blockNumberFormatted = { data[2], data[3] };
		return ByteConversion.convertBytesToInt(blockNumberFormatted)/*((blockNumberFormatted[0] & 0xff) << 8) | (blockNumberFormatted[1] & 0xFF)*/;
	}
	
	/**
	 * Give the file data from {@code data}
	 * @param data The received data to analyse
	 * @return Return the file data as a byte array found in {@code data}
	 */
	@SuppressWarnings("ConstantConditions")
	@NotNull
	private static byte[] getFileData(@NotNull byte[] data, boolean isLast) {
		if (data == null)
			throw new NullPointerException();
		
		if (data.length < 4)
			throw new IllegalArgumentException("data must be greater or equal to 4 bytes at least.");
		
		if (data.length == 4)
			return new byte[0];
		
		Lexicon<Byte> fileData = new LexiconBuilder<>(Byte.class)
				.setAcceptNullValues(false)
				.createLexicon();
		
		for (int i = 4; i < data.length; i++)
			fileData.add(data[i]);
		
		// Delete all zero from the end if it is the last package
		if (isLast) {
			boolean reachedContent = false;
			for (int i = fileData.size() - 1; i >= 0 && !reachedContent; i--) {
				if (fileData.get(i) == 0)
					fileData.remove(i);
				else
					reachedContent = true;
			}
		}
		
		// Return the byte array
		byte[] array = new byte[fileData.size()];
		for (int i = 0, maxi = fileData.size(); i < maxi; i++)
			array[i] = fileData.get(i);
		
		return array;
	}
	
	/**
	 * Tell if the given datagram packet is the last
	 * @param datagramPacket The packet to analyse
	 * @return Return {@code true} if it is the last packet, {@code false} otherwise
	 */
	@SuppressWarnings("ConstantConditions")
	private static boolean isLastPacket(@NotNull DatagramPacket datagramPacket) {
		if (datagramPacket == null)
			throw new NullPointerException();
		
		return datagramPacket.getLength() < TFTP_MAX_DATA_LENGTH + 4;
	}
}
