package org.opensky.libadsb;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 *  This file is part of org.opensky.libadsb.
 *
 *  org.opensky.libadsb is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  org.opensky.libadsb is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with org.opensky.libadsb.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Some useful functions when working with libadsb. Mostly we need these
 * functions since the library often works with arrays of bytes which are
 * not really readable for humans or basic operations are missing.
 * @author Matthias Schäfer (schaefer@opensky-network.org)
 */
public class tools {
	private static final char[] hexDigits =
		{'0', '1', '2', '3', '4', '5', '6', '7',
		 '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	/**
	 * Converts a byte into a hex string (e.g. 164 -&gt; "a4")
	 * @param b input byte
	 * @return hex representation of input byte
	 */
	public static String toHexString(byte b) {
		final char[] out = new char[2];
		out[0] = hexDigits[(0xF0 & b) >>> 4];
		out[1] = hexDigits[0x0F & b];
		return new String(out);
	}


	/**
	 * Converts an array of bytes in a hex string; Taken from 
     * org.apache.commons.codec.binary.Hex.
	 * @param bytes array of bytes
	 * @return concatenated hex representation of input byte array
	 */
	public static String toHexString(byte[] bytes) {
		final int l = bytes.length;
		final char[] out = new char[l << 1];

		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = hexDigits[(0xF0 & bytes[i]) >>> 4];
			out[j++] = hexDigits[0x0F & bytes[i]];
		}
		return new String(out);
	}

	/**
	 * Compares two byte arrays element by element
	 * @param array1 first array
	 * @param array2 second array
	 * @return array1 == array2
	 */
	public static boolean areEqual(byte[] array1, byte[] array2) {
		if (array1.length != array2.length) return false;

		for (int i=0; i<array1.length; ++i)
			if (array1[i] != array2[i]) return false;

		return true;
	}

	/**
	 * Compares two byte arrays element by element
	 * @param array1 first array
	 * @param array2 second array
	 * @return array1 == array2
	 */
	public static boolean areEqual(char[] array1, char[] array2) {
		if (array1.length != array2.length) return false;

		for (int i=0; i<array1.length; ++i)
			if (array1[i] != array2[i]) return false;

		return true;
	}

	/**
	 * @param byte1 first byte
	 * @param byte2 second byte
	 * @return byte1 xor byte2 (bitwise)
	 */
	public static byte xor(byte byte1, byte byte2) {
		return (byte)(0xff&(byte1^byte2));
	}

	/**
	 * @param array1 first array
	 * @param array2 second array
	 * @return array1 xor array2 (bitwise)
	 */
	public static byte[] xor(byte[] array1, byte[] array2) {
		assert(array1.length != array2.length);

		byte[] res = new byte[array1.length];
		for (int i=0; i<array1.length; ++i)
			res[i] = xor(array1[i], array2[i]);

		return res;
	}

	/**
	 * Checks whether a byte array just contains elements equal to zero
	 * @param  bytes input byte array
	 * @return true if all bytes of the array are 0
	 */
	public static boolean isZero(byte[] bytes) {
		int x = 0;
		for (int i = 0; i < bytes.length; i++) {
			x |= bytes[i];
		}
		return x == 0;
	}
	
	/**
	 * Counts all lines in a file
	 * @param inputHexx
	 * @return
	 * @throws IOException
	 */
	public static int countLines(File inputHexx) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(inputHexx));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        boolean empty = true;
	        while ((readChars = is.read(c)) != -1) {
	            empty = false;
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	        }
	        return (count == 0 && !empty) ? 1 : count;
	    } finally {
	        is.close();
	    }
	}
	
	/**
	 * Rounds a double to any decimals
	 * @param toRound
	 * @param decimals
	 * @return
	 */
	public static double round(Double toRound, int decimals){
		double rounded = (double) Math.round(toRound * Math.pow(10, decimals)) / Math.pow(10, decimals);
		return rounded;
	}
	
	
	/**
	 * @param epoch
	 * @return String for the time format HH:MM:SS
	 */
	public static String epoch2time(Long epoch){
			LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneId.of("Z"));
			return String.format("%02d", date.getHour()) + ":" + String.format("%02d", date.getMinute()) + ":" + String.format("%02d", date.getSecond());
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
