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
	
	private byte flight_status;
	private byte downlink_request;
	private byte utility_msg;
	private short code;
	
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
		flight_status = getFirstField();
		downlink_request = (byte) ((payload[0]>>>3) & 0x1F);
		utility_msg = (byte) ((payload[0]&0x7)<<3 | (payload[1]>>>5)&0x7);
		code = (short) ((payload[1]<<8 | payload[2]&0xFF)&0x1FFF);
		
		// extract ADS-B message
		message = new byte[7];
		for (int i=0; i<7; i++)
			message[i] = payload[i+3];
		
		b1 = ((message[0]>>>7)&0x1) == 1;
		b1_4 = ((message[0]>>>4));
		b5_8 = ((message[0]&0xF));
		b12 = ((message[1]>>>4)&0x1) == 1;
		b13 = ((message[1]>>>3)&0x1) == 1;
		b14 = ((message[1]>>>2)&0x1) == 1;
		b24 = (message[2]&0x1) == 1;
		b27 = ((message[3]>>>5)&0x1) == 1;
		b35 = ((message[4]>>>5)&0x1) == 1;
		b46 = ((message[5]>>>2)&0x1) == 1;
		b40_47 = (((message[4]&0x1)<<7) | ((message[5]&0xFE)>>1));
		b52_53 = ((message[6]>>>3)&0x3);
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
	 * Indicates alerts, whether SPI is enabled, and if the plane is on ground.
	 * @return The 3 bits flight status. The coding is:<br>
	 * <ul>
	 * <li>0 signifies no alert and no SPI, aircraft is airborne</li>
	 * <li>1 signifies no alert and no SPI, aircraft is on the ground</li>
	 * <li>2 signifies alert, no SPI, aircraft is airborne</li>
	 * <li>3 signifies alert, no SPI, aircraft is on the ground</li>
	 * <li>4 signifies alert and SPI, aircraft is airborne or on the ground</li>
	 * <li>5 signifies no alert and SPI, aircraft is airborne or on the ground</li>
	 * <li>6 reserved</li>
	 * <li>7 not assigned</li>
	 * </ul>
	 * @see #hasAlert()
	 * @see #hasSPI()
	 * @see #isOnGround()
	 */
	public byte getFlightStatus() {
		return flight_status;
	}

	/**
	 * @return whether flight status indicates alert
	 */
	public boolean hasAlert() {
		return flight_status>=2 && flight_status<=4;
	}

	/**
	 * @return whether flight status indicates special purpose indicator
	 */
	public boolean hasSPI() {
		return flight_status==4 || flight_status==5;
	}

	/**
	 * @return whether flight status indicates that aircraft is
	 * airborne or on the ground; For flight status &gt;= 4, this flag is unknown
	 */
	public boolean isOnGround() {
		return flight_status==1 || flight_status==3;
	}

	/**
	 * indicator for downlink requests
	 * @return the 5 bits downlink request. The coding is:<br>
     * <ul>
     * <li>0 signifies no downlink request</li>
	 * <li>1 signifies request to send Comm-B message</li>
	 * <li>2 reserved for ACAS</li>
	 * <li>3 reserved for ACAS</li>
	 * <li>4 signifies Comm-B broadcast message 1 available</li>
	 * <li>5 signifies Comm-B broadcast message 2 available</li>
	 * <li>6 reserved for ACAS</li>
	 * <li>7 reserved for ACAS</li>
	 * <li>8-15 not assigned</li>
	 * <li>16-31 see downlink ELM protocol (3.1.2.7.7.1)</li>
     * </ul>
	 */
	public byte getDownlinkRequest() {
		return downlink_request;
	}

	/**
	 * @return The 6 bits utility message (see ICAO Annex 10 V4)
	 */
	public byte getUtilityMsg() {
		return utility_msg;
	}
	
	/**
	 * Note that this is not the same identifier as the one contained in all-call replies.
	 * 
	 * @return the 4-bit interrogator identifier subfield of the
	 * utility message which reports the identifier of the
	 * interrogator that is reserved for multisite communications.
	 */
	public byte getInterrogatorIdentifier() {
		return (byte) ((utility_msg>>>2)&0xF);
	}
	
	/**
	 * Assigned coding is:<br>
	 * 0 signifies no information<br>
	 * 1 signifies IIS contains Comm-B II code<br>
	 * 2 signifies IIS contains Comm-C II code<br>
	 * 3 signifies IIS contains Comm-D II code<br>
	 * @return the 2-bit identifier designator subfield of the
	 * utility message which reports the type of reservation made
	 * by the interrogator identified in
	 * {@link #getInterrogatorIdentifier() getInterrogatorIdentifier}.
	 */
	public byte getIdentifierDesignator() {
		return (byte) (utility_msg&0x3);
	}
	
	/**
	 * @return The 13 bits altitude/identity code (see ICAO Annex 10 V4)
	 */
	public short getCode() {
		return code;
	}
	
	/**
	 * This method converts a gray code encoded int to a standard decimal int
	 * @param gray gray code encoded int of length bitlength
	 *        bitlength bitlength of gray code
	 * @return radix 2 encoded integer
	 */
	private static int grayToBin(int gray, int bitlength) {
		int result = 0;
		for (int i = bitlength-1; i >= 0; --i)
			result = result|((((0x1<<(i+1))&result)>>>1)^((1<<i)&gray));
		return result;
	}
	
	/**
	 * @return the decoded altitude in meters
	 */
	public Integer getAltitude() {
		// identity instead of altitude
		if (getDownlinkFormat() != 20) return null;
		// altitude unavailable
		if (code == 0) return null;

		boolean Mbit = (code&0x40)!=0;
		if (!Mbit) {
			boolean Qbit = (code&0x10)!=0;
			if (Qbit) { // altitude reported in 25ft increments
				int N = (code&0x0F) | ((code&0x20)>>>1) | ((code&0x1F80)>>>2);
				return (25*N-1000);
			}
			else { // altitude is above 50175ft, so we use 100ft increments

				// it's decoded using the Gillham code
				int C1 = (0x1000&code)>>>12;
				int A1 = (0x0800&code)>>>11;
				int C2 = (0x0400&code)>>>10;
				int A2 = (0x0200&code)>>>9;
				int C4 = (0x0100&code)>>>8;
				int A4 = (0x0080&code)>>>7;
				int B1 = (0x0020&code)>>>5;
				int B2 = (0x0008&code)>>>3;
				int D2 = (0x0004&code)>>>2;
				int B4 = (0x0002&code)>>>1;
				int D4 = (0x0001&code);

				// this is standard gray code
				int N500 = grayToBin(D2<<7|D4<<6|A1<<5|A2<<4|A4<<3|B1<<2|B2<<1|B4, 8);

				// 100-ft steps must be converted
				int N100 = grayToBin(C1<<2|C2<<1|C4, 3)-1;
				if (N100 == 6) N100=4;
				if (N500%2 != 0) N100=4-N100; // invert it

				return (-1200+N500*500+N100*100);
			}
		}
		else return null; // unspecified metric encoding
	}
	
	/**
	 * @return The identity/Mode A code (see ICAO Annex 10 V4).
	 * Special codes are<br>
	 * <ul>
	 * <li> 7700 indicates emergency<br>
	 * <li> 7600 indicates radiocommunication failure</li>
	 * <li> 7500 indicates unlawful interference</li>
	 * <li> 2000 indicates that transponder is not yet operated</li>
	 * </ul>
	 */
	public String getIdentity() {
		// Altitude instead of identity
		if (getDownlinkFormat() != 21) return null;
		
		int C1 = (0x1000&code)>>>12;
		int A1 = (0x800&code)>>>11;
		int C2 = (0x400&code)>>>10;
		int A2 = (0x200&code)>>>9;
		int C4 = (0x100&code)>>>8;
		int A4 = (0x080&code)>>>7;
		int B1 = (0x020&code)>>>5;
		int D1 = (0x010&code)>>>4;
		int B2 = (0x008&code)>>>3;
		int D2 = (0x004&code)>>>2;
		int B4 = (0x002&code)>>>1;
		int D4 = (0x001&code);

		String A = Integer.toString((A4<<2)+(A2<<1)+A1);
		String B = Integer.toString((B4<<2)+(B2<<1)+B1);
		String C = Integer.toString((C4<<2)+(C2<<1)+C1);
		String D = Integer.toString((D4<<2)+(D2<<1)+D1);
		
		return A+B+C+D;
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
				"\tFlight status:\t\t"+getFlightStatus()+"\n"+
				"\tDownlink request:\t\t"+getDownlinkRequest()+"\n"+
				"\tUtility Message:\t\t"+getUtilityMsg()+"\n"+
				"\tAltitude/Identity:\t"+(getDownlinkFormat() == 21 ? getIdentity() : getDownlinkFormat() == 20 ? String.valueOf(getAltitude()) : null)+"\n"+
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
