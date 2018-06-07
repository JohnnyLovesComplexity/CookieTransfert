package fr.polytech.arar.cookietransfert;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

public class TransferManager {
	
	@SuppressWarnings("ConstantConditions")
	public static synchronized boolean receiveFile(@NotNull File localFile, @NotNull String distantFilePath, @NotNull InetAddress address) {
		if (localFile == null)
			throw new NullPointerException("localFile must not be null");
		
		if (distantFilePath == null)
			throw new NullPointerException("distantFilePath must not be null");
		
		if (distantFilePath.equals(""))
			throw new IllegalArgumentException("distantFilePath must not be empty");
		
		if (address == null)
			throw new NullPointerException("address must not be null");
		
		try {
			if (!address.isReachable(500))
				throw new IllegalArgumentException("The address \"" + address.getHostAddress() + "\" is not reachable.");
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("The address \"" + address.getHostAddress() + "\" is not reachable.");
		}
	}
	@SuppressWarnings("ConstantConditions")
	public static synchronized boolean receiveFile(@Nullable String localFilePath, @NotNull String distantFilePath, @NotNull InetAddress address) {
		File f = new File(localFilePath);
		
		return receiveFile(f, distantFilePath, address);
	}
	@SuppressWarnings("ConstantConditions")
	public static synchronized boolean receiveFile(@NotNull String distantFilePath, @NotNull InetAddress address) {
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
