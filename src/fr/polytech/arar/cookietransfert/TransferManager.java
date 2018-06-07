package fr.polytech.arar.cookietransfert;

import fr.berger.enhancedlist.Couple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.naming.OperationNotSupportedException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.file.Files;

public class TransferManager {
	
	public static final int TFTP_PORT = 69;
	public static final int TFTP_MAX_DATA_LENGTH = 512;
	public static final int ERROR_LIMIT = 3;
	
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
		
		// Prepare the request
		String request = OPCode.RRQ.getRepresentation() + " " + distantFilePath;
		
		// Create the UDP communication
		Connection co = new Connection(address, TFTP_PORT);
		
		// Send the request
		co.send(request);
		
		// Wait for an answer
		Couple<String, DatagramPacket> result = co.receive();
		
		// 'repetition' count the number of ACK-fail
		int repetition = 0;
		while (result != null && result.getY() != null && result.getY().getLength() >= TFTP_MAX_DATA_LENGTH) {
			repetition = 0;
			
			// Send ACK
			try {
				co.answer(OPCode.ACK.getRepresentation());
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
					
					if (data.length > 2) {
						byte[] fileData = new byte[data.length - 2];
						System.arraycopy(data, 2, fileData, 0, data.length - 2);
						
						try {
							Files.write(localFile.toPath(), fileData);
						} catch (IOException e) {
							e.printStackTrace();
							return ErrorCode.CANNOT_WRITE_LOCAL_FILE;
						}
					}
					else
						result = null; // if result = null, then all blocks have been received
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
}
