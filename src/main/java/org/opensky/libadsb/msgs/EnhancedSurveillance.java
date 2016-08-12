package org.opensky.libadsb.msgs;

import java.io.Serializable;

import org.opensky.libadsb.tools;
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
 * Decoder for Mode S EHS
 * @author Jorge Martin (jm@innaxis.org)
 */
public class EnhancedSurveillance extends ModeSReply implements Serializable {

	private static final long serialVersionUID = -7877955448285410779L;
	
	private byte[] message;
	private boolean b1;
	private int b1_4;
	private int b5_8;
	private boolean b12;
	private boolean b13;
	private boolean b14;
	private boolean b24;
	private boolean b27;
	private boolean b35;
	private boolean b46;
	private int b40_47;
	private int b52_53;

	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected EnhancedSurveillance() { }

	/**
	 * @param raw_message raw extended squitter as hex string
	 * @throws BadFormatException if message is not extended squitter or 
	 * contains wrong values.
	 */
	public EnhancedSurveillance(String raw_message) throws BadFormatException {
		this(new ModeSReply(raw_message));
	}
	
	/**
	 * @param reply Mode S reply containing this extended squitter
	 * @throws BadFormatException if message is not extended squitter or 
	 * contains wrong values.
	 */
	public EnhancedSurveillance(ModeSReply reply) throws BadFormatException {
		super(reply);
		setType(subtype.ENHANCED_SURVEILLANCE);
		
		if (getDownlinkFormat() != 20 && getDownlinkFormat() != 21) {
			throw new BadFormatException("Message is not an enhanced surveillance!");
		}
		
		byte[] payload = getPayload();
		
		// extract ADS-B message
		message = new byte[7];
		for (int i=0; i<7; i++)
			message[i] = payload[i+3];
		
		b1 = ((message[0]>>>7)&0x1) == 1;
		b1_4 = ((message[0]>>>4));
		b5_8 = ((message[0]&0x4));
		b12 = ((message[1]>>>4)&0x1) == 1;
		b13 = ((message[1]>>>3)&0x1) == 1;
		b14 = ((message[1]>>>2)&0x1) == 1;
		b24 = (message[2]&0x1) == 1;
		b27 = ((message[3]>>>5)&0x1) == 1;
		b35 = ((message[4]>>>5)&0x1) == 1;
		b46 = ((message[5]>>>2)&0x1) == 1;
		b40_47 = (((message[4]&0x1)<<7) | ((message[5]&0xFE)>>1));
		b52_53 = ((message[6]>>>3)&0x11);
	}
	
	
	
	/**
	 * Copy constructor for subclasses
	 * 
	 * @param ehs instance of ExtendedSquitter to copy from
	 */
	public EnhancedSurveillance(EnhancedSurveillance ehs) {
		super(ehs);
		
		message = ehs.getMessage();
		
		b1 = ehs.isB1();
		b1_4 = ehs.getB1_4();
		b5_8 = ehs.getB5_8();
		b12 = ehs.isB12();
		b13 = ehs.isB13();
		b14 = ehs.isB14();
		b24 = ehs.isB24();
		b27 = ehs.isB27();
		b35 = ehs.isB35();
		b46 = ehs.isB46();
		b40_47 = ehs.getB40_47();
		b52_53 = ehs.getB52_53();
	}

	/**
	 * @return The message as 7-byte array
	 */
	public byte[] getMessage() {
		return message;
	}
	
	/**
	 * @return the b1
	 */
	public boolean isB1() {
		return b1;
	}

	/**
	 * @return the b1_4
	 */
	public int getB1_4() {
		return b1_4;
	}
	
	/**
	 * @return the b5_8
	 */
	public int getB5_8() {
		return b5_8;
	}

	/**
	 * @return the b12
	 */
	public boolean isB12() {
		return b12;
	}

	/**
	 * @return the b13
	 */
	public boolean isB13() {
		return b13;
	}

	/**
	 * @return the b14
	 */
	public boolean isB14() {
		return b14;
	}

	/**
	 * @return the b24
	 */
	public boolean isB24() {
		return b24;
	}

	/**
	 * @return the b27
	 */
	public boolean isB27() {
		return b27;
	}

	/**
	 * @return the b35
	 */
	public boolean isB35() {
		return b35;
	}

	/**
	 * @return the b46
	 */
	public boolean isB46() {
		return b46;
	}

	/**
	 * @return the b40_47
	 */
	public int getB40_47() {
		return b40_47;
	}

	/**
	 * @return the b52_53
	 */
	public int getB52_53() {
		return b52_53;
	}

	public String toString() {
		return super.toString()+"\n"+
				"Enhanced Surveillance:\n"+
				"\tB1:\t"+isB1()+"\n"+
				"\tB1_4:\t"+getB1_4()+"\n"+
				"\tB5_8:\t"+getB5_8()+"\n"+
				"\tB12:\t"+isB12()+"\n"+
				"\tB13:\t"+isB13()+"\n"+
				"\tB14:\t"+isB14()+"\n"+
				"\tB24:\t"+isB24()+"\n"+
				"\tB27:\t"+isB27()+"\n"+
				"\tB35:\t"+isB35()+"\n"+
				"\tB46:\t"+isB46()+"\n"+
				"\tB40_47:\t"+getB40_47()+"\n"+
				"\tB52_53:\t"+getB52_53()+"\n"+
				"\tMessage field:\t\t"+tools.toHexString(getMessage());
	}

}
