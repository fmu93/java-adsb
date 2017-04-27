package output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opensky.example.LastDatas;
import org.opensky.libadsb.tools;


public class SaveToDatabase {
	private File outFile;
	private Map<Double, HashMap<String, List<String>>> epochMap = new LinkedHashMap<Double, HashMap<String, List<String>>>();
	int keyCount = 0;
	public List<String> dataTypes = Arrays.asList(
		"CALL", 
		"LAT",
		"LON",
		"FL",
		"VRATE",
		"GS",
		"TTRACK",
		"TAS",
		"MHEAD",
		"SQUAWK",
		"SELALT4",
		"KOLLS4",
		"ROLL5",
		"TTRACK5",
		"GS5",
		"TURNR5",
		"TAS5",
		"MHEAD6",
		"IAS6",
		"MACH6",
		"EVENT");
	private int linesToFlush = 100000;
	public List<Integer> typeCount = new ArrayList<Integer>(Collections.nCopies(dataTypes.size(), 0));
	private static int epochPrecision = 0;
	
	public SaveToDatabase(int epochPrecision){
		SaveToDatabase.epochPrecision = epochPrecision;
	}
	
	public int getEpochPrecision() {
		return epochPrecision;
	}

	public void setEpochPrecision(int epochPrecision1) {
		epochPrecision = epochPrecision1;
	}

	private PrintWriter writer;

	
	public void setOutPath(Path outPath, String outFileName){
		outFile = new File(outPath + System.getProperty("file.separator") + outFileName);
	}
	
	public void newDataEntry(Double epochTimeUnrounded, String icao, String data, String type){
		double epochTime = tools.round(epochTimeUnrounded, epochPrecision);
		int dataTypeIndex = dataTypes.indexOf(type);
		keyCount++;
		typeCount.set(dataTypeIndex, typeCount.get(dataTypeIndex)+1);
		// existing epochTime
		if (epochMap.containsKey(epochTime)){
			// existing icao at time
			if (epochMap.get(epochTime).containsKey(icao)){				
				epochMap.get(epochTime).get(icao).set(dataTypeIndex, data);
				
				// new icao at time
			}else{
				List<String> newList = new ArrayList<String>(Collections.nCopies(dataTypes.size(), ""));
				newList.set(dataTypeIndex, data);
				epochMap.get(epochTime).put(icao, newList);
			}
			
			// new epochTime with new icao
		}else{
			List<String> newList = new ArrayList<String>(Collections.nCopies(dataTypes.size(), ""));
			newList.set(dataTypeIndex, data);
			HashMap<String, List<String>> newIcaoMap= new HashMap<String, List<String>>();
			newIcaoMap.put(icao, newList);
			epochMap.put(epochTime, newIcaoMap);
		}
		
		if (keyCount > linesToFlush){
			flushMemory();
		}
		LastDatas.setAircraftData(epochTime, icao, data, dataTypeIndex);
	}
	
	
	public void flushMemory(){
		for (double timestamp : epochMap.keySet()){
			for (String icao : epochMap.get(timestamp).keySet()){
				String lineToWrite = String.format("%." + epochPrecision + "f", timestamp)+ "\t" + tools.epoch2time((long) timestamp) + "\t" + icao;
				for (String data : epochMap.get(timestamp).get(icao)){
					lineToWrite += "\t" + data;
				}
				writer.println(lineToWrite);
			}
		}
//		writer.flush();
		epochMap.clear();
		keyCount = 0;
	}
	

	public void setWriter(double firstEpoch){
		try {
			writer = new PrintWriter(outFile, "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String header = new String();
		
		header = "timestamp\t" + tools.epoch2date((long) firstEpoch) + "\ticao";
		for (String dataType : dataTypes){
			header += "\t" + dataType;
		}
		writer.println(header);
		writer.flush();
	}
	
	public void closeWriter(){
		writer.close();
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
