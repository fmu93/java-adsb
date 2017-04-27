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
	private double gs;
	private double ttrack;
	private double tas;
	private double mhead;

	public void newEntry(double epochTime, String data, int dataTypeIndex){

		if (dataTypeIndex == 0)
			call = data;
		else if (dataTypeIndex == 3 && data != "gr"){
			fl = Double.parseDouble(data);
			alt = Double.parseDouble(data)*30.48;
			timeAlt = epochTime;
			hasAlt = true;
		}
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





