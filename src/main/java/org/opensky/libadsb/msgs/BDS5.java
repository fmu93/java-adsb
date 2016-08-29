package org.opensky.libadsb.msgs;

import java.io.Serializable;

import org.opensky.libadsb.exceptions.BadFormatException;
import org.opensky.libadsb.exceptions.MissingInformationException;

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
public class BDS5 extends EnhancedSurveillance implements Serializable {
	
	private static final long serialVersionUID = -7397309420290359454L;
	private boolean bankangle_available;
	private boolean sig_roll;
	private short roll_angle;
	private boolean ttrack_available;
	private boolean sig_tr;
	private short track_angle;
	private boolean gs_available;
	private short groundspeed;
	private boolean turnrate_available;
	private boolean sig_ang_var;
	private short ang_var;
	private boolean tas_available;
	private short trueairspeed;
	
	
	/** protected no-arg constructor e.g. for serialization with Kryo **/
	protected BDS5() { }

	/**
	 * @param raw_message raw ADS-B velocity-over-ground message as hex string
	 * @throws BadFormatException if message has wrong format
	 */
	public BDS5(String raw_message) throws BadFormatException {
		this(new EnhancedSurveillance(raw_message));
	}
	
	/**
	 * @param squitter extended squitter which contains this velocity over ground msg
	 * @throws BadFormatException if message has wrong format
	 */
	public BDS5(EnhancedSurveillance squitter) throws BadFormatException {
		super(squitter);
		setType(subtype.BDS5);
		
		if (!this.isB1() || !this.isB12() || !this.isB24() || !this.isB35() || !this.isB46()) {
			throw new BadFormatException("BDS5 messages must have bits 1,12,24,35 and 46 enabled");
		}
		
		byte[] msg = this.getMessage();
		
		bankangle_available = ((msg[0]&0x80)>>>7) == 1;
		sig_roll = ((msg[0]&0x40)>>>6) == 1;
		roll_angle = (short) (((msg[0]&0x3F)<<3) | ((msg[1]>>5)&0x7));
		ttrack_available = ((msg[1]&0x10)>>>4) == 1;
		sig_tr = ((msg[1]&0x8)>>>3) == 1;
		track_angle = (short) (((msg[1]&0x7)<<7) | ((msg[2]>>>1)&0x7F));
		gs_available = (msg[2]&0x1) == 1;
		groundspeed = (short) ((msg[3]<<2) | ((msg[4]&0xC0)>>>6));
		turnrate_available = ((msg[4]&0x20)>>>5) == 1;
		sig_ang_var = ((msg[4]&0x10)>>>4) == 1;
		ang_var = (short) (((msg[4]&0xF)<<5) | ((msg[5]>>>3)&0x1F));
		tas_available = ((msg[5]&0x4)>>>2) == 1;
		trueairspeed = (short) (((msg[5]&0x3)<<8) | (msg[6]&0xFF));
	}
	
	/**
	 * Aircraft roll angle
	 * 
	 * @return double: angle in degrees
	 * @throws MissingInformationException
	 */
	public double getBankangle() throws MissingInformationException {
		final double UNITCONV = 45.0 / 256.0;
		if (!bankangle_available) {
			throw new MissingInformationException("No bankangle information available!");
		}
		if (sig_roll) {
			return (roll_angle * UNITCONV);
		}
		return roll_angle * UNITCONV;
	}
	
	/**
	 * True track angle
	 * 
	 * @return angle in degrees to true north
	 * @throws MissingInformationException
	 */
	public double getTtrack() throws MissingInformationException {
		final double UNITCONV = 90.0 / 512.0;
		if (!ttrack_available) {
			throw new MissingInformationException("No ttrack information available!");
		}
		if (sig_tr) {
			return ((track_angle - Math.pow(2, 10)) * UNITCONV) + 360.0;
		}
		return track_angle * UNITCONV;
	}
	
	/**
	 * Aircraft ground speed
	 * 
	 * @return ground speed in knots
	 * @throws MissingInformationException
	 */
	public int getGs() throws MissingInformationException {
		if (!gs_available) {
			throw new MissingInformationException("No ground speed information available!");
		}
		return groundspeed * 2;
	}
	
	/**
	 * Track angle rate
	 * 
	 * @return angle rate in degrees/second
	 * @throws MissingInformationException
	 */
	public double getTurnrate() throws MissingInformationException {
		final double UNITCONV = 8.0 / 256.0;
		if (!turnrate_available) {
			throw new MissingInformationException("No turnrate information available!");
		}
		if (sig_ang_var) {
			return (ang_var - Math.pow(2,9)) * UNITCONV;
		}
		return ang_var * UNITCONV;
	}
	
	/**
	 * Aircraft true airspeed
	 * 
	 * @return true airspeed in knots
	 * @throws MissingInformationException
	 */
	public int getTAS() throws MissingInformationException {
		if (!tas_available) {
			throw new MissingInformationException("No true airspeed information available!");
		}
		return trueairspeed * 2;
	}

	/**
	 * @return the bankangle_available
	 */
	public boolean hasBankangle() {
		return bankangle_available;
	}

	/**
	 * @return the roll_angle
	 */
	public short getRoll_angle() {
		return roll_angle;
	}

	/**
	 * @return the ttrack_available
	 */
	public boolean hasTtrack() {
		return ttrack_available;
	}

	/**
	 * @return the track_angle
	 */
	public short getTrack_angle() {
		return track_angle;
	}

	/**
	 * @return the gs_available
	 */
	public boolean hasGs() {
		return gs_available;
	}

	/**
	 * @return the groundspeed
	 */
	public short getGroundspeed() {
		return groundspeed;
	}

	/**
	 * @return the turnrate_available
	 */
	public boolean hasTurnrate() {
		return turnrate_available;
	}

	/**
	 * @return the ang_var
	 */
	public short getAng_var() {
		return ang_var;
	}

	/**
	 * @return the tas_available
	 */
	public boolean hasTas() {
		return tas_available;
	}

	/**
	 * @return the trueairspeed
	 */
	public short getTrueairspeed() {
		return trueairspeed;
	}
	
	public String toString() {
		String ret = super.toString()+"\n" + "BDS5:\n";
		try { ret += "\tbankangle:\t\t"+getBankangle()+"\n"; }
		catch (Exception e) { ret += "\tbankangle:\t\tnot available\n"; }
		try { ret += "\tttrack:\t\t"+getTtrack()+"\n"; }
		catch (Exception e) { ret += "\tttrack:\t\tnot available\n"; }
		try { ret += "\tgs:\t\t"+getGs()+"\n"; }
		catch (Exception e) { ret += "\tgs:\t\tnot available\n"; }
		try { ret += "\tturnrate:\t\t"+getTurnrate()+"\n"; }
		catch (Exception e) { ret += "\tturnrate:\t\tnot available\n"; }
		try { ret += "\ttas:\t\t"+getTAS()+"\n"; }
		catch (Exception e) { ret += "\ttas:\t\tnot available\n"; }
		
		return ret;
	}
}
