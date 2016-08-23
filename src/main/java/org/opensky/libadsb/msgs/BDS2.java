package org.opensky.libadsb.msgs;

import java.io.Serializable;

import org.opensky.libadsb.exceptions.BadFormatException;

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
 * Decoder for ADS-B velocity messages
 * @author Matthias Schäfer (schaefer@opensky-network.org)
 */
public class BDS2 extends EnhancedSurveillance implements Serializable {
	
	private static final long serialVersionUID = -7397309420290359454L;
	private byte[] callsign;
	
	/**
	 * Maps ADS-B encoded to readable characters
	 * @param digit encoded digit
	 * @return readable character
	 */
	private static char mapChar (byte digit) {
		if (digit>0 && digit<27) return (char) ('A'+digit-1);
		else if (digit>47 && digit<58) return (char) ('0'+digit-48);
		else return ' ';
	}
	
	/**
	 * Maps ADS-B encoded to readable characters
	 * @param digits array of encoded digits
	 * @return array of decoded characters
	 */
	private static char[] mapChar (byte[] digits) {
		char[] result = new char[digits.length];
		
		for (int i=0; i<digits.length; i++)
			result[i] = mapChar(digits[i]);
		
		return result;
	}
	
	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected BDS2() { }

	/**
	 * @param raw_message raw ADS-B velocity-over-ground message as hex string
	 * @throws BadFormatException if message has wrong format
	 */
	public BDS2(String raw_message) throws BadFormatException {
		this(new EnhancedSurveillance(raw_message));
	}
	
	/**
	 * @param squitter extended squitter which contains this velocity over ground msg
	 * @throws BadFormatException if message has wrong format
	 */
	public BDS2(EnhancedSurveillance squitter) throws BadFormatException {
		super(squitter);
		setType(subtype.BDS2);
		
		if (this.getB1_4() != 2 || this.getB5_8() != 0) {
			throw new BadFormatException("BDS2 messages must have bits 1-4 == 2 and 5-8 == 0.");
		}
		
		byte[] msg = this.getMessage();
		
		// extract identity
		callsign = new byte[8];
		int byte_off, bit_off;
		for (int i=8; i>=1; i--) {
			// calculate offsets
			byte_off = (i*6)/8; bit_off = (i*6)%8;
			
			// char aligned with byte?
			if (bit_off == 0) callsign[i-1] = (byte) (msg[byte_off]&0x3F);
			else {
				++byte_off;
				callsign[i-1] = (byte) (msg[byte_off]>>>(8-bit_off)&(0x3F>>>(6-bit_off)));
				// should we add bits from the next byte?
				if (bit_off < 6) callsign[i-1] |= msg[byte_off-1]<<bit_off&0x3F;
			}
		}
	}
	
	/**
	 * @return the call sign as 8 characters array
	 */
	public char[] getCallsign() {
		return mapChar(callsign);
	}
	
	public String toString() {
		return super.toString()+"\n"+
				"BDS2:\n"+
				"\tCallsign:\t\t"+new String(getIdentity());
	}
}
