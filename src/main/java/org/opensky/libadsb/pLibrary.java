package org.opensky.libadsb;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class pLibrary {
	

	public static String seconds2time(long seconds){
		try{
			int hAverage = (int) Math.floor(seconds / 3600);
			int mAverage = (int) Math.floor((seconds - hAverage * 3600) / 60);
			int sAverage = (int) Math.floor(seconds - hAverage * 3600 - mAverage * 60);
			String time =  String.format("%02d", hAverage) + ":" + String.format("%02d", mAverage) + ":" + String.format("%02d", sAverage);
			return time;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static String seconds2time(double seconds){
		try{
			int hAverage = (int) Math.floor(seconds / 3600);
			int mAverage = (int) Math.floor((seconds - hAverage * 3600) / 60);
			int sAverage = (int) Math.floor(seconds - hAverage * 3600 - mAverage * 60);
			String time =  String.format("%02d", hAverage) + ":" + String.format("%02d", mAverage) + ":" + String.format("%02d", sAverage);
			return time;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static Integer time2seconds(String time){
		return time2seconds(time, 0);
	}

	public static Integer time2seconds(String time, int rollOver){
		try{
			List<String> frag = Arrays.asList(time.split(":"));
			if (frag.size() == 3){
				int seconds = Integer.parseInt(frag.get(0))*3600 + Integer.parseInt(frag.get(1))*60 + Integer.parseInt(frag.get(2));
				return seconds + (rollOver * 3600 * 24);
			}else{
				return null;
			}
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static Integer getTimeDifference(String startTime, String endTime){
		return getTimeDifference(startTime, endTime, true);
	}

	public static Integer getTimeDifference(String startTime, String endTime, boolean chkRollOver){
		// input in HH:MM:SS and output in seconds
		try{
			Integer startTimeSec = time2seconds(startTime);
			Integer endTimeSec = time2seconds(endTime);
			if (chkRollOver && (startTimeSec - endTimeSec) > 0){
				endTimeSec += 3600 * 24;
			}
			return endTimeSec - startTimeSec;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static String epochInSecondsString(String newEpochString){
		if (newEpochString.length() > 10){
			newEpochString = newEpochString.substring(0, newEpochString.length() - 3);
		}
		return newEpochString;
	}
	
	public static Long epochInSecondsLong(String newEpochString){
		long epochDouble = 0;
		if (newEpochString.length() > 10){
			epochDouble = Long.parseLong(newEpochString.substring(0, newEpochString.length() - 3));
		}else{
			epochDouble = Long.parseLong(newEpochString);
		}
		return epochDouble;
	}

	public static String epoch2time(String epoch){
		try{
			return epoch2time(Long.parseLong(epoch));			
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static String epoch2time(Long epoch){
		try{
			LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneId.of("Z"));
			return String.format("%02d", date.getHour()) + ":" + String.format("%02d", date.getMinute()) + ":" + String.format("%02d", date.getSecond());
			
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static double getListMedian(List<Integer> list) {
		if (list.size() == 0)
			return 0.0;
		if( list.size() == 1 )
			return list.get(0);
		Collections.sort(list);
		double median;
		if (list.size() % 2 == 0)
		    median = ((double)list.get(list.size()/2) + (double)list.get(list.size()/2-1))/2d;
		else
		    median = (double) list.get(list.size()/2);
		return median;
	}
	
	public static String list2string(List<String> list){
		return list2string(list, false);
	}
	
	public static String list2string(List<String> list, boolean hasHeader){
		String string = null;
		try{
			if (hasHeader){
				list = list.subList(1, list.size());
			}
			string = list.toString().substring(1, list.toString().length()-1);
			return string;
			
		}catch(Exception e){
			e.printStackTrace();
			return string;
		}
	}
	
	public static List<String> comma2list(String string){
		return comma2list(string, null);
	}
	
	public static List<String> comma2list(String string, String header){
		List<String> list = null;
		if (header != null){
			string = header + ", " + string;
		}
		try{
			list = Arrays.asList((string).split(", "));
			return list;
		}catch(Exception e){
			e.printStackTrace();
			return list;
		}
	}
	
	
	
	
}
