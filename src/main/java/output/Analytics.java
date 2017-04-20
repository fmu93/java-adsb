package output;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.opensky.example.Core;

public class Analytics {

	private static List<List<String>> histoDFicao = new ArrayList<List<String>>();
	private static List<Integer> histoDFcount = new ArrayList<Integer>(Collections.nCopies(25,0));
	private static List<String> icaoList = new ArrayList<String>();
	private static List<String> icaoAirborneCount = new ArrayList<String>();
	private static List<String> icaoSurfCount = new ArrayList<String>();
	private static List<String> icaoAnyCount = new ArrayList<String>();
	private static List<String> icaoBothCount = new ArrayList<String>();
	private static List<Integer> bdsCount = new ArrayList<Integer>(Collections.nCopies(3, 0));
	private static List<Double> kollsmanList = new ArrayList<Double>();
	private static HashMap<String, Integer> last_alt = new HashMap<String, Integer>();
	private static HashMap<String, List<String>> last_pos = new HashMap<String, List<String>>();
	private static List<Object[]> kollsAlt = new ArrayList<Object[]>();


	public Analytics(){
		histoDFicao = new ArrayList<List<String>>();
		for (int i = 0; i<25 ; i++){
			histoDFicao.add(new ArrayList<String>());
		}
	}

	public void newAlt(String icao, Double alt){
		last_alt.put(icao, (int) Math.round(alt));
	}
	
	/**
	 * @param icao
	 * @param Long
	 * @param Lat
	 */
	public void newPos(String icao, String Long, String Lat){
		last_pos.put(icao, Arrays.asList(Long, Lat));
	}

	public void newDF(String icao, int df){
		if (df < 24){
			if (!histoDFicao.get(df).contains(icao)){
				histoDFicao.get(df).add(icao);
				histoDFcount.set(df, histoDFcount.get(df)+1);
			}
			if (!icaoList.contains(icao)){
				icaoList.add(icao);
			}
		}
	}

	public void newAirbone(String icao){
		if (!icaoAirborneCount.contains(icao)){
			icaoAirborneCount.add(icao);
		}
		if (!icaoAnyCount.contains(icao)){
			icaoAnyCount.add(icao);
		}
	}

	public void newSurf(String icao){
		if (!icaoSurfCount.contains(icao)){
			icaoSurfCount.add(icao);
		}
		if (!icaoAnyCount.contains(icao)){
			icaoAnyCount.add(icao);
		}
	}

	public static void newBDS(Byte bds){
		newBDS(Arrays.asList(bds));
	}

	public static void newBDS(List<Byte> bds){
		for (byte i : bds){
			if (i == 0x40)
				bdsCount.set(0, bdsCount.get(0) + 1);
			else if (i == 0x50)
				bdsCount.set(1, bdsCount.get(1) + 1);
			else if (i == 0x60)
				bdsCount.set(2, bdsCount.get(2) + 1);
		}
	}

	public void newKollsman(String icao, double kollsman){
		kollsmanList.add(kollsman);
		if (last_alt.containsKey(icao) && last_pos.containsKey(icao)){
			Object [] object = {icao, last_pos.get(icao).get(0), last_pos.get(icao).get(1), kollsman, last_alt.get(icao)};
			kollsAlt.add(object);
		}
	}

	public void printAnalytics(){
		
		Core.printConsole("\nSingle occurrence DF icao count:");
		for (int i = 0; i<histoDFicao.size(); i++){
			if (histoDFicao.get(i).size() > 0){
				Core.printConsole("DF" + i + "\t" + histoDFicao.get(i).size());
			}
		}
		Core.printConsole("\nTotal icao count:\t" + icaoList.size());

		Core.printConsole("Count of icao with any position:\t" + icaoAnyCount.size());
		Core.printConsole("Count of icao with airborne position:\t" + icaoAirborneCount.size());
		Core.printConsole("Count of icao with surface position:\t" + icaoSurfCount.size());

		for (String icao0 : icaoSurfCount){
			if (icaoAirborneCount.contains(icao0))
				icaoBothCount.add(icao0);
		}
		Core.printConsole("Count of icao with both positions:\t" + icaoBothCount.size());

		Core.printConsole("\nBDS assumptions from status bits:");
		for (int i = 0; i<bdsCount.size(); i++){
			Core.printConsole("BDS" + (i+4) + "0\t" + bdsCount.get(i));
		}
		
//		Core.printConsole("\nAssumed decoded kollsman values:");
//		Core.printConsole("ICAO\tLon\tLat\tKolls\talt(m)");
//		for (int i = 0; i<kollsAlt.size(); i++){
//			String all = "";
//			for (Object object:kollsAlt.get(i)){
//				all += String.valueOf(object) + "\t";
//			}
//			Core.printConsole(all);
//		}

	}
















}
