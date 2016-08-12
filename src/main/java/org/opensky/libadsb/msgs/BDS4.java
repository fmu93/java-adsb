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
public class BDS4 extends EnhancedSurveillance implements Serializable {
	
	private static final long serialVersionUID = -7397309420290359454L;
	private short selaltMCP;
	private short selaltFMS;
	private short koll;
	
	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected BDS4() { }

	/**
	 * @param raw_message raw ADS-B velocity-over-ground message as hex string
	 * @throws BadFormatException if message has wrong format
	 */
	public BDS4(String raw_message) throws BadFormatException {
		this(new EnhancedSurveillance(raw_message));
	}
	
	/**
	 * @param squitter extended squitter which contains this velocity over ground msg
	 * @throws BadFormatException if message has wrong format
	 */
	public BDS4(EnhancedSurveillance squitter) throws BadFormatException {
		super(squitter);
		setType(subtype.BDS4);
		
		if (!this.isB1() || !this.isB14() || !this.isB27() || this.getB40_47() != 0 || this.getB52_53() != 0) {
			throw new BadFormatException("BDS4 messages must have bits 1,14,27 enabled, 40-47 == 0 and 52-53 == 0");
		}
		
		byte[] msg = this.getMessage();
		
		selaltMCP = (short) (((msg[0]&0x7F)<<5) | ((msg[1]>>>3)&0x1F));
		selaltFMS = (short) (((msg[1]&0x3)<<10) | (msg[2]<<2) | ((msg[3]>>>6)&0x3));
		koll = (short) (((msg[3]&0x1F)<<7) | (msg[4]>>>1)&0x7F);
	}

	
	
	/**
	 * @return the selaltMCP in ft
	 */
	public int getSelaltMCP() {
		return selaltMCP * 16;
	}

	/**
	 * @return the selaltFMS in ft
	 */
	public int getSelaltFMS() {
		return selaltFMS * 16;
	}

	/**
	 * @return the koll in millibars
	 */
	public float getKoll() {
		return (float) (koll * 0.1 + 800.0);
	}

	public String toString() {
		return super.toString()+"\n"+
				"BDS4:\n"+
				"\tselaltMCP:\t\t"+getSelaltMCP()+"\n"+
				"\tselaltFMS:\t\t"+getSelaltFMS()+"\n"+
				"\tCallsign:\t\t"+getKoll();
	}
}
