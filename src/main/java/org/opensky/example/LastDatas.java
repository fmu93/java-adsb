package org.opensky.example;

import java.util.HashMap;


public class LastDatas {
	private static HashMap<String, LastData> aircraftDatas = new HashMap<String, LastData>();

	public static void setAircraftData(double epochTime, String icao, String data, int dataTypeIndex){
		if (aircraftDatas.containsKey(icao)){
			aircraftDatas.get(icao).newEntry(epochTime, data, dataTypeIndex);
		}else{
			LastData lastData = new LastData();
			lastData.newEntry(epochTime, data, dataTypeIndex);
			aircraftDatas.put(icao, lastData);
		}
	}

	public static LastData getAircraftData(String icao){
		if (aircraftDatas.containsKey(icao))
			return aircraftDatas.get(icao);
		else
			return null;
	}
	
	public static boolean hasLastData(String icao){
		if (aircraftDatas.containsKey(icao))
			return true;
		else
			return false;
	}
}


class LastData{
	private String call;
	private double lat;
	private double lon;
	private double fl;
	private double alt;
	private double timeAlt;
	private boolean hasAlt = false;
	private double vrate;
	private double timeVrate;
	private boolean hasVrate = false;
	private double noADSBAlt;
	private double timenoADSBAlt;
	private boolean hasnoADSBAlt = false;
	private double gs;
	private double timeGs;
	private boolean hasGs = false;
	private double ttrack;
	private double timeTtrack;
	private boolean hasTtrack = false;
	private double tas;
	private double timeTas;
	private boolean hasTas = false;
	private double ias;
	private double timeIas;
	private boolean hasIas = false;
	private double mhead;

	public void newEntry(double epochTime, String data, int dataTypeIndex){
		if (dataTypeIndex == 0)
			call = data;
		else if (dataTypeIndex == 3){
			fl = Double.parseDouble(data);
			alt = Double.parseDouble(data)*30.48;
			timeAlt = epochTime;
			hasAlt = true;
		}else if (dataTypeIndex == 5){
			vrate = Double.parseDouble(data);
			timeVrate = epochTime;
			hasVrate = true;
		}else if (dataTypeIndex == 7){
			ttrack = Double.parseDouble(data);
			timeTtrack = epochTime;
			hasTtrack = true;
		}else if (dataTypeIndex == 11){
			noADSBAlt = Double.parseDouble(data)*30.48;
			timenoADSBAlt = epochTime;
			hasnoADSBAlt = true;
		}else if (dataTypeIndex == 18){
			tas = Double.parseDouble(data);
			timeTas = epochTime;
			hasTas = true;
		}else if (dataTypeIndex == 16){
			gs = Double.parseDouble(data);
			timeGs = epochTime;
			hasGs = true;
		}else if (dataTypeIndex == 20){
			ias = Double.parseDouble(data);
			timeIas = epochTime;
			hasIas = true;
		}
	}
	
	public double getVrate(){
		return vrate;
	}
	
	public double getTimeVrate(){
		return timeVrate;
	}
	
	public boolean getHasVrate(){
		return hasVrate;
	}
	
	public double getTtrack(){
		return ttrack;
	}
	
	public double getTimeTtrack(){
		return timeTtrack;
	}
	
	public boolean getHasTtrack(){
		return hasTtrack;
	}
	
	public double getGs(){
		return gs;
	}
	
	public double getTimeGs(){
		return timeGs;
	}
	
	public boolean getHasGs(){
		return hasGs;
	}
	
	public double getTas(){
		return tas;
	}
	
	public double getTimeTas(){
		return timeTas;
	}
	
	public boolean getHasIas(){
		return hasIas;
	}
	
	public double getIas(){
		return ias;
	}
	
	public double getTimeIas(){
		return timeIas;
	}
	
	public boolean getHasTas(){
		return hasTas;
	}
	
	public double getNoADSBAlt() {
		return noADSBAlt;
	}

	public double getTimenoADSBAlt() {
		return timenoADSBAlt;
	}

	public boolean isHasnoADSBAlt() {
		return hasnoADSBAlt;
	}

	public double getAlt() {
		return alt;
	}
	
	public boolean hasAlt(){
		return hasAlt;
	}
	
	public double getTimeAlt(){
		return timeAlt;
	}
	

}





