package org.opensky.example;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.opensky.libadsb.Decoder;
import org.opensky.libadsb.Position;
import org.opensky.libadsb.PositionDecoder;
import org.opensky.libadsb.tools;
import org.opensky.libadsb.exceptions.BadFormatException;
import org.opensky.libadsb.exceptions.UnspecifiedFormatError;
import org.opensky.libadsb.msgs.AirbornePositionMsg;
import org.opensky.libadsb.msgs.AirspeedHeadingMsg;
import org.opensky.libadsb.msgs.AllCallReply;
import org.opensky.libadsb.msgs.AltitudeReply;
import org.opensky.libadsb.msgs.CommBAltitudeReply;
import org.opensky.libadsb.msgs.CommBIdentifyReply;
import org.opensky.libadsb.msgs.CommDExtendedLengthMsg;
import org.opensky.libadsb.msgs.EmergencyOrPriorityStatusMsg;
import org.opensky.libadsb.msgs.ExtendedSquitter;
import org.opensky.libadsb.msgs.IdentificationMsg;
import org.opensky.libadsb.msgs.IdentifyReply;
import org.opensky.libadsb.msgs.LongACAS;
import org.opensky.libadsb.msgs.MilitaryExtendedSquitter;
import org.opensky.libadsb.msgs.ModeSReply;
import org.opensky.libadsb.msgs.OperationalStatusMsg;
import org.opensky.libadsb.msgs.ShortACAS;
import org.opensky.libadsb.msgs.SurfacePositionMsg;
import org.opensky.libadsb.msgs.TCASResolutionAdvisoryMsg;
import org.opensky.libadsb.msgs.VelocityOverGroundMsg;

import output.Analytics;

/**
 * ADS-B decoder example: It reads STDIN line-by-line. It should be fed with
 * comma-separated timestamp and message. Example input:
 * 
 * 1,8d4b19f39911088090641010b9b0
 * 2,8d4ca513587153a8184a2fb5adeb
 * 3,8d3413c399014e23c80f947ce87c
 * 4,5d4ca88c079afe
 * 5,a0001838ca3e51f0a8000047a36a
 * 6,8d47a36a58c38668ffb55f000000
 * 7,5d506c28000000
 * 8,a8000102fe81c1000000004401e3
 * 9,a0001839000000000000004401e3
 * 
 * @author Matthias Schäfer (schaefer@opensky-network.org)
 */
public class ExampleDecoder{
	// we store the position decoder for each aircraft
	HashMap<String, PositionDecoder> decs;
	private PositionDecoder dec = new PositionDecoder();
	public static Position receiverPos;
	private static Analytics analytics = new Analytics();
	private boolean interrupted = false;


	public ExampleDecoder() {
		decs = new HashMap<String, PositionDecoder>();
	}

	public void decodeMsg(double timestamp, String raw, String icao) throws Exception {
		ModeSReply msg;
		try {
			msg = Decoder.genericDecoder(raw);
		} catch (BadFormatException e) {
			System.out.println("Malformed message! Skipping it. Message: "+e.getMessage());
			return;
		} catch (UnspecifiedFormatError e) {
			System.out.println("Unspecified message! Skipping it...");
			return;
		}

		String icao24 = tools.toHexString(msg.getIcao24());
		
		if (icao != null && !icao.toLowerCase().equals(icao24)) return;

		// check for erroneous messages; some receivers set
		// parity field to the result of the CRC polynomial division
		if (tools.isZero(msg.getParity()) || msg.checkParity()) { // CRC is ok

			// cleanup decoders every 100.000 messages to avoid excessive memory usage
			// therefore, remove decoders which have not been used for more than one hour.
			List<String> to_remove = new ArrayList<String>();
			for (String key : decs.keySet())
				if (decs.get(key).getLastUsedTime()<timestamp-3600)
					to_remove.add(key);

			for (String key : to_remove)
				decs.remove(key);

			// now check the message type

			switch (msg.getType()) {
			case ADSB_AIRBORN_POSITION:
				AirbornePositionMsg airpos = (AirbornePositionMsg) msg;
				System.out.print("["+icao24+"]: ");

				// decode the position if possible
				if (decs.containsKey(icao24)) {
					dec = decs.get(icao24);
					airpos.setNICSupplementA(dec.getNICSupplementA());
					Position current = dec.decodePosition(timestamp, receiverPos, airpos);
					if (current == null  || !current.isReasonable()){
						System.out.println("Cannot decode position yet.");
					}else{
						System.out.println("Now at position ("+current.getLatitude()+","+current.getLongitude()+")");

						DecThread.saver.newDataEntry(timestamp, icao24, String.format("%.6f", current.getLatitude()), "LAT");
						DecThread.saver.newDataEntry(timestamp, icao24, String.format("%.6f", current.getLongitude()), "LON");

						analytics.newAirbone(icao24);
						analytics.newPos(icao24, String.format("%.6f", current.getLongitude()), String.format("%.6f", current.getLatitude()));
					}
				}
				else {
					dec = new PositionDecoder();
					dec.decodePosition(timestamp, receiverPos, airpos);
					decs.put(icao24, dec);
					System.out.println("First position.");
				}
				System.out.println("          Horizontal containment radius is "+airpos.getHorizontalContainmentRadiusLimit()+" m");
				System.out.println("          Altitude is "+ (airpos.hasAltitude() ? airpos.getAltitude() : "unknown") +" m");

				if (airpos.hasAltitude()){
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%.3f", airpos.getAltitude()/30.48), "FL");
					analytics.newAlt(icao24, airpos.getAltitude());
				}
				break;
			case ADSB_SURFACE_POSITION:
				SurfacePositionMsg surfpos = (SurfacePositionMsg) msg;

				System.out.print("["+icao24+"]: ");

				// decode the position if possible
				if (decs.containsKey(icao24)) {
					dec = decs.get(icao24);
					Position current = dec.decodePosition(timestamp, receiverPos, surfpos); 
					if (current == null || !current.isReasonable()) 
						System.out.println("Cannot decode position yet.");
					else{
						System.out.println("Now at position ("+current.getLatitude()+","+current.getLongitude()+")");
						DecThread.saver.newDataEntry(timestamp, icao24, String.format("%.6f", current.getLatitude()), "LAT");
						DecThread.saver.newDataEntry(timestamp, icao24, String.format("%.6f", current.getLongitude()), "LON");
						DecThread.saver.newDataEntry(timestamp, icao24, "gr", "FL");

						analytics.newSurf(icao24);
						analytics.newAlt(icao24, 600.0);
					}
				}
				else {
					dec = new PositionDecoder();
					dec.decodePosition(timestamp, receiverPos, surfpos);
					decs.put(icao24, dec);
					System.out.println("First position.");
				}
				System.out.println("          Horizontal containment radius is "+surfpos.getHorizontalContainmentRadiusLimit()+" m");
				if (surfpos.hasValidHeading()){
					System.out.println("          Heading is "+surfpos.getHeading()+" m");
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%.1f", surfpos.getHeading()), "MHEAD");
				}
				System.out.println("          Airplane is on the ground.");
				break;
			case ADSB_EMERGENCY:
				EmergencyOrPriorityStatusMsg status = (EmergencyOrPriorityStatusMsg) msg;
				System.out.println("["+icao24+"]: "+status.getEmergencyStateText());
				System.out.println("          Mode A code is "+status.getModeACode()[0]+
						status.getModeACode()[1]+status.getModeACode()[2]+status.getModeACode()[3]);
				break;
			case ADSB_AIRSPEED:
				AirspeedHeadingMsg airspeed = (AirspeedHeadingMsg) msg;
				System.out.println("["+icao24+"]: Airspeed is "+
						(airspeed.hasAirspeedInfo() ? airspeed.getAirspeed()+" m/s" : "unkown"));
				if (airspeed.hasAirspeedInfo()){
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%.1f", airspeed.getAirspeed()), "TAS");
				}
				if (airspeed.hasHeadingInfo()){
					System.out.println("          Heading is "+
							(airspeed.hasHeadingInfo() ? airspeed.getHeading()+"°" : "unkown"));
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%.1f", airspeed.getHeading()), "MHEAD");
				}

				if (airspeed.hasVerticalRateInfo()){
					System.out.println("          Vertical rate is "+
							(airspeed.hasVerticalRateInfo() ? airspeed.getVerticalRate()+" m/s" : "unkown"));
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%.1f", airspeed.getVerticalRate()), "VRATE");
				}
				break;
			case ADSB_IDENTIFICATION:
				IdentificationMsg ident = (IdentificationMsg) msg;
				System.out.println("["+icao24+"]: Callsign is "+new String(ident.getIdentity()));
				System.out.println("          Category: "+ident.getCategoryDescription());
				DecThread.saver.newDataEntry(timestamp, icao24, new String(ident.getIdentity()), "CALL");
				break;
			case ADSB_STATUS:
				OperationalStatusMsg opstat = (OperationalStatusMsg) msg;
				PositionDecoder dec;
				if (decs.containsKey(icao24))
					dec = decs.get(icao24);
				else {
					dec = new PositionDecoder();
					decs.put(icao24, dec);
				}
				dec.setNICSupplementA(opstat.getNICSupplementA());
				if (opstat.getSubtypeCode() == 1)
					dec.setNICSupplementC(opstat.getNICSupplementC());
				System.out.println("["+icao24+"]: Using ADS-B version "+opstat.getVersion());
				System.out.println("          Has ADS-B IN function: "+opstat.has1090ESIn());
				break;
			case ADSB_TCAS:
				TCASResolutionAdvisoryMsg tcas = (TCASResolutionAdvisoryMsg) msg;
				System.out.println("["+icao24+"]: TCAS Resolution Advisory completed: "+tcas.hasRATerminated());
				System.out.println("          Threat type is "+tcas.getThreatType());
				if (tcas.getThreatType() == 1) // it's a icao24 address
					System.out.println("          Threat identity is 0x"+String.format("%06x", tcas.getThreatIdentity()));
				break;
			case ADSB_VELOCITY:
				VelocityOverGroundMsg veloc = (VelocityOverGroundMsg) msg;
				System.out.println("["+icao24+"]: Velocity is "+(veloc.hasVelocityInfo() ? veloc.getVelocity() : "unknown")+" m/s");
				System.out.println("          Heading is "+(veloc.hasVelocityInfo() ? veloc.getHeading() : "unknown")+" degrees");
				System.out.println("          Vertical rate is "+(veloc.hasVerticalRateInfo() ? veloc.getVerticalRate() : "unknown")+" m/s");

				if (veloc.hasVelocityInfo()){
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%.1f", veloc.getVelocity()), "GS");
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%.1f", veloc.getHeading()), "TTRACK");
				}
				if (veloc.hasVerticalRateInfo()){
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%.1f", veloc.getVerticalRate()), "VRATE");
				}

				break;
			case EXTENDED_SQUITTER:
				System.out.println("["+icao24+"]: Unknown extended squitter with type code "+((ExtendedSquitter)msg).getFormatTypeCode()+"!");
				break;
			default:

			}
		}
		else if (msg.getDownlinkFormat() != 17) { // CRC failed
			switch (msg.getType()) {
			case MODES_REPLY:
				System.out.println("["+icao24+"]: Unknown message with DF "+msg.getDownlinkFormat());
				break;
			case SHORT_ACAS:
				ShortACAS acas = (ShortACAS)msg;
				System.out.println("["+icao24+"]: Altitude is "+acas.getAltitude()+" and ACAS is "+
						(acas.hasOperatingACAS() ? "operating." : "not operating."));
				System.out.println("          A/C is "+(acas.isAirborne() ? "airborne" : "on the ground")+
						" and sensitivity level is "+acas.getSensitivityLevel());
				break;
			case ALTITUDE_REPLY:
				AltitudeReply alti = (AltitudeReply)msg;
				System.out.println("["+icao24+"]: Short altitude reply: "+alti.getAltitude()+"m");
				break;
			case IDENTIFY_REPLY:
				IdentifyReply identify = (IdentifyReply)msg;
				System.out.println("["+icao24+"]: Short identify reply: "+identify.getIdentity());
				DecThread.saver.newDataEntry(timestamp, icao24, identify.getIdentity(), "SQUAWK");
				break;
			case ALL_CALL_REPLY:
				AllCallReply allcall = (AllCallReply)msg;
				System.out.println("["+icao24+"]: All-call reply for "+tools.toHexString(allcall.getInterrogatorID())+
						" ("+(allcall.hasValidInterrogatorID()?"valid":"invalid")+")");
				break;
			case LONG_ACAS:
				LongACAS long_acas = (LongACAS)msg;
				System.out.println("["+icao24+"]: Altitude is "+long_acas.getAltitude()+" and ACAS is "+
						(long_acas.hasOperatingACAS() ? "operating." : "not operating."));
				System.out.println("          A/C is "+(long_acas.isAirborne() ? "airborne" : "on the ground")+
						" and sensitivity level is "+long_acas.getSensitivityLevel());
				System.out.println("          RAC is "+(long_acas.hasValidRAC() ? "valid" : "not valid")+
						" and is "+long_acas.getResolutionAdvisoryComplement()+" (MTE="+long_acas.hasMultipleThreats()+")");
				System.out.println("          Maximum airspeed is "+long_acas.getMaximumAirspeed()+"m/s.");
				break;
			case MILITARY_EXTENDED_SQUITTER:
				MilitaryExtendedSquitter mil = (MilitaryExtendedSquitter)msg;
				System.out.println("["+icao24+"]: Military ES of application "+mil.getApplicationCode());
				System.out.println("          Message is 0x"+tools.toHexString(mil.getMessage()));
				break;
			case COMM_B_ALTITUDE_REPLY:
				CommBAltitudeReply commBaltitude = (CommBAltitudeReply)msg;
				System.out.println("["+icao24+"]: Long altitude reply: "+commBaltitude.getAltitude()+"m");
				System.out.println("          raw hexx: "+raw);
				System.out.println("          Comm-B message:"+commBaltitude.getMessage());
				
				if (commBaltitude.commBMessage.hasBDS40){
					analytics.newKollsman(icao24, commBaltitude.commBMessage.kollsman);
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%.1f", commBaltitude.commBMessage.kollsman), "KOLLS4");
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%d", commBaltitude.commBMessage.select_alt_MCP), "SELALT4");
				}
				
				if (commBaltitude.commBMessage.hasBDS50){
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%.2f", commBaltitude.commBMessage.rollAngle), "ROLL5");
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%.1f", commBaltitude.commBMessage.trueTrackAngle), "TTRACK5");
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%d", commBaltitude.commBMessage.gorundSpeed), "GS5");
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%.2f", commBaltitude.commBMessage.trackAngleRate), "TURNR5");
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%d", commBaltitude.commBMessage.trueAirspeed), "TAS5");
				}
				
				break;
			case COMM_B_IDENTIFY_REPLY:
				CommBIdentifyReply commBidentify = (CommBIdentifyReply)msg;
				System.out.println("["+icao24+"]: Long identify reply: "+commBidentify.getIdentity());
				System.out.println("          raw hexx: "+raw);
				System.out.println("          Comm-B message:"+commBidentify.getMessage());
				
				if (commBidentify.commBMessage.hasBDS40){
					analytics.newKollsman(icao24, commBidentify.commBMessage.kollsman);
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%.1f", commBidentify.commBMessage.kollsman), "KOLLS4");
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%d", commBidentify.commBMessage.select_alt_MCP), "SELALT4");
				}
				
				if (commBidentify.commBMessage.hasBDS50){
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%.2f", commBidentify.commBMessage.rollAngle), "ROLL5");
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%.1f", commBidentify.commBMessage.trueTrackAngle), "TTRACK5");
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%d", commBidentify.commBMessage.gorundSpeed), "GS5");
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%.2f", commBidentify.commBMessage.trackAngleRate), "TURNR5");
					DecThread.saver.newDataEntry(timestamp, icao24, String.format("%d", commBidentify.commBMessage.trueAirspeed), "TAS5");
				}
				
				break;
			case COMM_D_ELM:
				CommDExtendedLengthMsg commDELM = (CommDExtendedLengthMsg)msg;
				System.out.println("["+icao24+"]: ELM message w/ sequence no "+commDELM.getSequenceNumber()+
						" (ACK: "+commDELM.isAck()+")");
				System.out.println("          Message is 0x"+tools.toHexString(commDELM.getMessage()));
				break;
			default:
			}
		}
		else {
			System.out.println("Message contains biterrors.");
		}
		analytics.newDF(icao24, (int) msg.getDownlinkFormat());
	}

	public void runDecoder(String icaoFilter) throws Exception{
		String icao = null; // 44a826
		Core.printConsole(Core.inputHexx.getAbsolutePath().toString());
		if (icaoFilter.length() > 0) {
			icao = icaoFilter;
			System.err.println("Set filter to ICAO 24-bit ID '"+icao+"'.");
		}
		if (Core.receiverReason != null){
			receiverPos = new Position(Core.receiverReason.get(1), Core.receiverReason.get(0), 0.0);
			dec.setMaxRange((int) Math.abs(Core.receiverReason.get(2)*1000));
		}
		boolean firstEpoch = true;

		// iterate over STDIN
		Scanner sc = new Scanner(Core.inputHexx , "UTF-8");
		ExampleDecoder dec = new ExampleDecoder();
		while(sc.hasNext() && !interrupted) {

			String[] values = sc.nextLine().split("  *");
			if (values.length == 2){
				double timeStamp = Double.parseDouble(values[0]);
				if (firstEpoch){
					DecThread.saver.setWriter(timeStamp);
					firstEpoch = false;
				}
				dec.decodeMsg(timeStamp, values[1], icao);
			}
		}
		sc.close();

		// some printing for debugging an analysis
		Core.printConsole("\nAnalytics--> " + Core.inputHexx.getName());

		Core.printConsole("dataType\tcount");
		for (int i = 0 ; i < DecThread.saver.dataTypes.size(); i++){
			Core.printConsole(DecThread.saver.dataTypes.get(i) + "\t" + DecThread.saver.typeCount.get(i));
		}
		analytics.printAnalytics();
		Core.endDecoder(false);
	}
	
	public void setInterrupted(boolean interrupted){
		this.interrupted = interrupted;
	}




}
