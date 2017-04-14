package org.opensky.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import output.Analytics;

public class CommBMessage {

	private byte[] message;
	private List<Byte> bds = new ArrayList<Byte>();
	private List <Integer> bds40StatusBits = Arrays.asList(1, 14, 27);
	private List <Integer> bds50StatusBits = Arrays.asList(1, 12, 24, 35, 46);
	private List <Integer> bds60StatusBits = Arrays.asList(1, 13, 24, 35, 46);

	double kollsman;
	boolean hasBDS40 = false;
	boolean hasBDS50 = false;
	boolean hasBDS60 = false;

	public CommBMessage(byte[] message) {
		this.message = message;
		checkStatusBits();
		if (bds.size() > 0){
			validateBDS();
			Analytics.newBDS(bds);
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
		int select_alt_MCP = (int) ((((message[0]&0xFF)<<8 | message[1]&0xFF)>>>3)&0xFFF)*16;
		int select_alt_FMS= (int) (((((message[1]&0xFF)<<8 | message[2]&0xFF)<<8 | message[3]&0xFF)>>>6)&0xFFF)*16;
		kollsman =  ((int) (((message[3]&0xFF)<<8 | message[4]&0xFF)>>>1)&0xFFF)*0.1 + 800;		
		hasBDS40 = true;
	}

	private void decodeBDS50(){
		boolean rollAngleSign = ((message[0]&0xFF>>>6)&0x1) == 0x1;
		double rollAngle = (double) (((message[0]&0xFF<<8 | message[1]&0xFF)>>5)&0x1FF) * 0.17578125;
		rollAngle = rollAngleSign ? rollAngle : rollAngle*-1;
		boolean trueTrackAngleSign = ((message[1]&0xFF>>>3)&0x1) == 0x1;
		double trueTrackAngle = (double) (((message[1]&0xFF<<8 | message[2]&0xFF)>>1)&0x3FF) * 0.17578125;
		trueTrackAngle = trueTrackAngleSign ? trueTrackAngle : trueTrackAngle*-1;
		int gorundSpeed = (int) (((message[3]&0xFF<<8 | message[4]&0xFF)>>6)&0x3FF) * 2;
		boolean trackAngleRateSign = ((message[4]&0xFF>>>4)&0x1) == 0x1;
		double trackAngleRate = (double) (((message[4]&0xFF<<8 | message[5]&0xFF)>>3)&0x1FF) * 0.03125;
		trackAngleRate = trackAngleRateSign ? trackAngleRate : trackAngleRate*-1;
		int trueAirspeed = (int) ((message[5]&0xFF<<8 | message[6]&0xFF)&0x3FF) * 2;
		
		// here reasonableness tests
		hasBDS50 = true;
	}

	private void decodeBDS60(){
		boolean magneticHeadingSign = ((message[0]&0xFF>>>6)&0x1) == 0x1;
		double magneticHeading = (double) (((message[0]&0xFF<<8 | message[1]&0xFF)>>4)&0x3FF) * 0.17578125;
		magneticHeading = magneticHeadingSign ? magneticHeading : magneticHeading*-1;
		int indicatedAirspeed = (int) (((message[1]&0xFF<<8 | message[2]&0xFF)>>1)&0x3FF);
		int mach = (int) (((message[3]&0xFF<<8 | message[4]&0xFF)>>6)&0x3FF)*4;
		boolean barometricAltRateSign = ((message[4]&0xFF>>>5)&0x1) == 0x1;
		int barometricAltRate = (int) (((message[4]&0xFF<<8 | message[5]&0xFF)>>3)&0x1FF) * 32;
		barometricAltRate = barometricAltRateSign ? barometricAltRate : barometricAltRate*-1;
		boolean inertialVertVelSign = ((message[5]&0xFF>>>1)&0x1) == 0x1;
		int inertialVertVel = (int) ((message[5]&0xFF<<8 | message[6]&0xFF)&0x1FF) * 32;
		inertialVertVel = inertialVertVelSign ? inertialVertVel : inertialVertVel*-1;
		
		// here reasonableness tests
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
			int byteIndex= pos/8; // rounded
			int shift = (byteIndex+1)*8 - pos;
			status = ((message[byteIndex]>>>shift) & 0x01) == match;
			if (!status) break;
		}		
		return status;
	}

	/**
	 * if reserved bits are expected to be zeroed
	 * @param startBit 
	 * @param endBit if only one bit length, endBit = startBit
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
