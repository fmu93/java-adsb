package org.opensky.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import output.Analytics;

public class CommBMessage {

	private byte[] message;
	private double epochNow;
	private List<Byte> bds = new ArrayList<Byte>();
	private List <Integer> bds40StatusBits = Arrays.asList(1, 14, 27);
	private List <Integer> bds50StatusBits = Arrays.asList(1, 12, 24, 35, 46);
	private List <Integer> bds60StatusBits = Arrays.asList(1, 13, 24, 35, 46);
	private String icao;

	boolean hasBDS40 = false;
	boolean hasBDS50 = false;
	boolean hasBDS60 = false;

	double kollsman;
	int select_alt_FMS;
	int select_alt_MCP;
	double rollAngle;
	double trueTrackAngle;
	int gorundSpeed;
	double trackAngleRate;
	int trueAirspeed;
	double magneticHeading;
	int indicatedAirspeed;
	double mach;
	int barometricAltRate;
	int inertialVertVel;

	public CommBMessage(byte[] message, String icao) {
		this.message = message;
		this.icao = icao;
		this.epochNow = Core.decThread.decoder.getTimestamp();
		checkStatusBits();
		if (bds.size() > 0){
			validateBDS();
			if (hasBDS40)
				Analytics.newBDS((byte) 0x40);
			if (hasBDS50)
				Analytics.newBDS((byte) 0x50);
			if (hasBDS60)
				Analytics.newBDS((byte) 0x60);

		}
	}

	private void checkStatusBits(){

		if (hasStatusBits(bds40StatusBits) && hasReservedBits(40, 47) && hasReservedBits(52, 53)) // Selected vertical intention
			bds.add((byte) 0x40);
		if (hasStatusBits(bds50StatusBits)) // Track and turn report
			bds.add((byte) 0x50);
		if (hasStatusBits(bds60StatusBits)) // Heading and speed report
			bds.add((byte) 0x60);
	}

	private void validateBDS(){
		for (byte i : bds){
			if (i == 0x40)
				decodeBDS40();
			if (i == 0x50)
				decodeBDS50();
			if (i == 0x60)
				decodeBDS60();
		}
	}

	private void decodeBDS40(){
		select_alt_MCP = ((short) ((message[0]<<8&0xFF00 | message[1]&0xFF)>>>3)&0xFFF)*16;
		select_alt_FMS = ((short) (((message[1]<<8&0xFF00 | message[2]&0xFF)<<8&0xFF00 | message[3]&0xFF)>>>6)&0xFFF)*16;
		kollsman =  ((short) ((message[3]<<8&0xFF00 | message[4]&0xFF)>>>1)&0xFFF)*0.1 + 800;		
		hasBDS40 = true;
	}

	private void decodeBDS50(){
		boolean valid = true;
		boolean rollAngleSign = ((message[0]>>>6)&0x1) == 0x1;
		rollAngle = (short) (((message[0]<<8&0xFF00 | message[1]&0xFF)>>5)&0x1FF);
		rollAngle = (rollAngleSign ? rollAngle - 512 : rollAngle) * 0.17578125; // deg, neg for left wing down [-90, 90]
		boolean trueTrackAngleSign = ((message[1]>>>3)&0x1) == 0x1;
		trueTrackAngle = (short) (((message[1]<<8&0xFF00 | message[2]&0xFF)>>1)&0x3FF);
		trueTrackAngle = (trueTrackAngleSign ? trueTrackAngle + 1024 : trueTrackAngle) * 0.17578125; // deg, neg for west [-180, 180]
		gorundSpeed = ((short) (((message[3]<<8&0xFF00 | message[4]&0xFF)>>6)&0x3FF)) * 2; // knots
		boolean trackAngleRateSign = ((message[4]>>>4)&0x1) == 0x1;
		trackAngleRate = (short) (((message[4]<<8&0xFF00 | message[5]&0xFF)>>3)&0x1FF);
		trackAngleRate = (trackAngleRateSign ? trackAngleRate - 512  : trackAngleRate) * 0.03125; // deg, [-16, 16]
		trueAirspeed = ((short) ((message[5]<<8&0xFF00 | message[6]&0xFF)&0x3FF)) * 2; // knots

		// here reasonableness test 
		double compRollAngle = Math.toDegrees(Math.atan(trueAirspeed*1.68781*trackAngleRate*Math.PI/180/32)); // g = 32 ft/s2
		if (Math.abs(compRollAngle - rollAngle) > 5 ||  trueAirspeed > 1000 || gorundSpeed > 1000)
			valid = false;
		if (valid){
			hasBDS50 = true;
		}
	}

	private void decodeBDS60(){
		boolean valid = true;
		boolean magneticHeadingSign = ((message[0]>>>6)&0x1) == 0x1;
		magneticHeading = (short) (((message[0]<<8&0xFF00 | message[1]&0xFF)>>4)&0x3FF);
		magneticHeading = (magneticHeadingSign ? magneticHeading + 1024 : magneticHeading) * 0.17578125;
		indicatedAirspeed = (short) (((message[1]<<8&0xFF00 | message[2]&0xFF)>>1)&0x3FF);
		mach = ((short) (((message[3]<<8&0xFF00 | message[4]&0xFF)>>6)&0x3FF))*0.004;
		boolean barometricAltRateSign = ((message[4]>>>4)&0x1) == 0x1;
		barometricAltRate = (short) (((message[4]<<8&0xFF00 | message[5]&0xFF)>>3)&0x1FF);
		barometricAltRate = (barometricAltRateSign ? barometricAltRate - 512 : barometricAltRate) * 32;
		boolean inertialVertVelSign = ((message[5]>>>1)&0x1) == 0x1;
		inertialVertVel = (short) ((message[5]<<8&0xFF00 | message[6]&0xFF)&0x1FF);
		inertialVertVel = (inertialVertVelSign ? inertialVertVel - 512 : inertialVertVel) * 32;

		// here reasonableness tests
		
		// check decoded data itself
		if (0>magneticHeading && magneticHeading>360 || Math.abs(barometricAltRate)>10000 || Math.abs(inertialVertVel)>10000
				|| Math.abs(barometricAltRate - inertialVertVel) > 500){
			valid = false;
		}
		// check change on IAS from previous data. Threshold on 40 knots/s or 20 m/s^2 TODO
		if (LastDatas.hasLastData(icao) && LastDatas.getAircraftData(icao).getHasIas() && 
				(indicatedAirspeed-LastDatas.getAircraftData(icao).getIas())/(epochNow-LastDatas.getAircraftData(icao).getTimeIas())>40){
			valid = false;
		}
		// flight mechanics
		boolean hasLastAlt = false;
		double lastAlt = 0;
		if (LastDatas.hasLastData(icao) && LastDatas.getAircraftData(icao).hasAlt() && Math.abs(epochNow - LastDatas.getAircraftData(icao).getTimeAlt()) < 20){
			lastAlt = LastDatas.getAircraftData(icao).getAlt(); // meters 
			hasLastAlt = true;
		}else if (LastDatas.hasLastData(icao) && LastDatas.getAircraftData(icao).isHasnoADSBAlt() && Math.abs(epochNow - LastDatas.getAircraftData(icao).getTimenoADSBAlt()) < 20){
			lastAlt = LastDatas.getAircraftData(icao).getAlt(); // meters 
			hasLastAlt = true;
		}
		if (hasLastAlt){
			double p0 = 101325; // Pa
			double rho0 = 1.225; // kg/m3
			double T0 = 228.16; // K
			double theta;
			double sigma;
			double delta;
			double T;
			double p;
			double rho;
			if (lastAlt <= 11000){
				theta = 1 - 0.0000225569*lastAlt;	            
				sigma = Math.pow(theta, 5.2561);
				delta = Math.pow(theta, 4.2561);
			}else{
				theta = 0.75187;
				sigma = 0.22336 * Math.exp(-0.000157688*(lastAlt-1100));
				delta = 1.33 * sigma;
			}
			T = T0*theta;
			p = p0*sigma;
			rho = rho0*delta;
			double delta_p = p0*(Math.pow(1+Math.pow(indicatedAirspeed*0.5144, 2)* 0.142857 *(rho0/p0), 3.5)-1);
			double tas = Math.sqrt(1.5*p/rho*(Math.pow(delta_p/p+1, 0.28571)-1));
			double m = tas / Math.sqrt(401.8*T);
			if (Math.abs(m - mach) > 0.15)
				valid = false;
		}
		
		if (valid)
			hasBDS60 = true;
	}


	/**
	 * @param statusPosition
	 * @return
	 */
	private boolean hasStatusBits(List<Integer> statusPosition){
		return hasStatusBits(statusPosition, true);
	}

	/**
	 * @param statusPositions ranging from 1 to 56 for the 7 byte commB Message
	 * @param sign true for 1, false for 0
	 * @return if this bits are all 1
	 */
	private boolean hasStatusBits(List<Integer> statusPositions, boolean sign){
		if (Collections.min(statusPositions)<1 || Collections.min(statusPositions)>56){
			return false;
		}
		boolean status = true;
		byte match = 0x01;
		if (!sign)
			match = 0x00;

		for (int pos : statusPositions){
			int byteIndex = (pos-1)/8; // rounded
			int shift = (byteIndex+1)*8 - pos;
			status = ((message[byteIndex]>>>shift) & 0x01) == match;
			if (!status) break;
		}		
		return status;
	}

	/**
	 * if reserved bits are expected to be zeroed
	 * @param startBit 
	 * @param endBit. if only one bit length, endBit = startBit
	 * @return
	 */
	private boolean hasReservedBits(int startBit, int endBit){
		if (startBit<1 || endBit>56){
			return false;
		}
		List<Integer> reservedBits = new ArrayList<Integer>();

		for (int i = startBit; i < endBit; i++){
			reservedBits.add(i);
		}
		return hasStatusBits(reservedBits, false);


	}



















}
