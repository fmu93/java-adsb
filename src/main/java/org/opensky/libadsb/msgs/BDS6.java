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
public class BDS6 extends EnhancedSurveillance implements Serializable {
	
	private static final long serialVersionUID = -7397309420290359454L;
	private boolean sign;
	private short mag_head;
	private short ias;
	private short mach;
	private boolean sig_baro_vr;
	private short baro_vr;
	private boolean sig_ins_vr;
	private short ins_vr;
	
	
	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected BDS6() { }

	/**
	 * @param raw_message raw ADS-B velocity-over-ground message as hex string
	 * @throws BadFormatException if message has wrong format
	 */
	public BDS6(String raw_message) throws BadFormatException {
		this(new EnhancedSurveillance(raw_message));
	}
	
	/**
	 * @param squitter extended squitter which contains this velocity over ground msg
	 * @throws BadFormatException if message has wrong format
	 */
	public BDS6(EnhancedSurveillance squitter) throws BadFormatException {
		super(squitter);
		setType(subtype.BDS6);
		
		if (!this.isB1() || !this.isB13() || !this.isB24() || !this.isB35() || !this.isB46()) {
			throw new BadFormatException("BDS6 messages must have bits 1,13,24,35 and 46 enabled");
		}
		
		byte[] msg = this.getMessage();
		
		sign = ((msg[0]&0x40)>>>6) == 1;
		mag_head = (short) (((msg[0]&0x3F)<<4) | ((msg[1]>>4)&0xF));
		ias = (short) (((msg[1]&0x7)<<7) | ((msg[2]>>1)&0x7F));
		mach = (short) (((msg[3]&0xFF)<<2) | ((msg[4]>>>6)&0x3));
		sig_baro_vr = ((msg[4]&0x10)>>>4) == 1;
		baro_vr = (short) (((msg[4]&0xF)<<5) | ((msg[5]>>>3)&0x1F));
		sig_ins_vr = ((msg[5]&0x2)>>>1) == 1;
		ins_vr = (short) (((msg[5]&0x1)<<8) | (msg[6]&0xFF));
	}
	
	/**
	 * Magnetic heading of aircraft
	 * @return double: heading in degrees to magnetic north
	 */
	public double getMag_head() {
		final double UNITCONV = 90.0 / 512.0;
		if (sign) {
			return 360 - (mag_head * UNITCONV);
		}
		return mag_head * UNITCONV;
	}
	
	/**
	 * Indicated airspeed
	 * @return int airspeed in knots
	 */
	public int getIas() {
		return ias;
	}
	
	/**
	 * Aircraft MACH number
	 * @return double Mach number
	 */
	public double getMach() {
		return mach * 2.048 / 512.0;
	}
	
	/**
	 * Vertical rate from barometric measurement
	 * @return int rate in feet/min
	 */
	public int getBaro_vr() {
		final int UNITCONV = 32;
		if (sig_baro_vr) {
			return -1 * (baro_vr * UNITCONV);
		}
		return baro_vr * UNITCONV;
	}
	
	/**
	 * Vertical rate from on-board equipments
	 * @return int rate in feet/min
	 */
	public int getIns_vr() {
		final int UNITCONV = 32;
		if (sig_ins_vr) {
			return -1 * (ins_vr * UNITCONV);
		}
		return ins_vr * UNITCONV;
	}
	
	public String toString() {
		String ret = super.toString()+"\n" + "BDS6:\n";
		try { ret += "\tmhead:\t\t"+getMag_head()+"\n"; }
		catch (Exception e) { ret += "\tmhead:\t\tnot available\n"; }
		try { ret += "\tIAS:\t\t"+getIas()+"\n"; }
		catch (Exception e) { ret += "\tIAS:\t\tnot available\n"; }
		try { ret += "\tMACH:\t\t"+getMach()+"\n"; }
		catch (Exception e) { ret += "\tMACH:\t\tnot available\n"; }
		try { ret += "\tbaro_vr:\t\t"+getBaro_vr()+"\n"; }
		catch (Exception e) { ret += "\tbaro_vr:\t\tnot available\n"; }
		try { ret += "\tins_vr:\t\t"+getIns_vr()+"\n"; }
		catch (Exception e) { ret += "\tins_vr:\t\tnot available\n"; }
		
		return ret;
	}
}
