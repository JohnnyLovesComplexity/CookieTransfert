package fr.polytech.arar.cookietransfert;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class ByteConversion {
	
	@NotNull
	public static byte[] convertIntToBytes(int value, int numberOfBytes) {
		if (numberOfBytes < 1)
			throw new NullPointerException();
		
		ByteBuffer b = ByteBuffer.allocate(numberOfBytes);
		
		b.putInt(value);
		
		byte[] bytes = b.array();
		
		if (bytes.length == numberOfBytes)
			return bytes;
		else if (bytes.length > numberOfBytes) {
			throw new IllegalArgumentException("numberOfBytes is too big for the conversion of " + value + ".");
		}
		else /* if (bytes.length < numberOfBytes) */ {
			// Add zero before
			byte[] new_array = new byte[numberOfBytes];
			
			// Fill with 0
			for (int i = 0, maxi = bytes.length; i < maxi; i++) {
				new_array[i] = 0;
			}
			
			System.arraycopy(bytes, 0, new_array, numberOfBytes - bytes.length, bytes.length);
			
			return new_array;
			/*byte[] ret = new byte[4];
			ret[3] = (byte) (a & 0xFF);
			ret[2] = (byte) ((a >> 8) & 0xFF);
			ret[1] = (byte) ((a >> 16) & 0xFF);
			ret[0] = (byte) ((a >> 24) & 0xFF);
			return ret;*/
		}
	}
	public static byte[] convertIntToBytes(int value) {
		return convertIntToBytes(value, 4);
	}
	
	public static int convertBytesToInt(@NotNull byte[] value) {
		if (value == null)
			throw new NullPointerException();
		
		if (value.length == 0)
			throw new IllegalArgumentException("value cannot be empty");
		
		/*ByteBuffer b = ByteBuffer.wrap(value);
		
		//b.order(ByteOrder.LITTLE_ENDIAN);
		
		return b.getInt()*/;
		
		//return ((value[0] & 0xff) << 8) | (value[1] & 0xFF);
		int a = 0;
		for (int i = 0; i < value.length; i++) {
			int shift = (value.length - 1 - i) * 8;
			a += (value[i] & 0x000000FF) << shift;
		}
		return a;
	}
}
