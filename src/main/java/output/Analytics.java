package output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Analytics {

	private static List<List<String>> histoDFicao = new ArrayList<List<String>>();
	private static List<Integer> histoDFcount = new ArrayList<Integer>(Collections.nCopies(25,0));
	private static List<String> icaoList = new ArrayList<String>();
	private static List<String> icaoAirborneCount = new ArrayList<String>();
	private static List<String> icaoSurfCount = new ArrayList<String>();
	private static List<String> icaoAnyCount = new ArrayList<String>();
	private static List<String> icaoBothCount = new ArrayList<String>();


	public Analytics(){
		histoDFicao = new ArrayList<List<String>>();
		for (int i = 0; i<25 ; i++){
			histoDFicao.add(new ArrayList<String>());
		}
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

	public void printAnalytics(){
		System.out.println("Single occurrence DF icao count:");
		//		for (int i = 0; i<histoDFcount.size(); i++){
		//			if (histoDFcount.get(i) > 0){
		//				System.out.print("DF" + i + "\t");
		//				System.out.println(histoDFcount.get(i));
		//			}
		//		}
		for (int i = 0; i<histoDFicao.size(); i++){
			if (histoDFicao.get(i).size() > 0){
				System.out.print("DF" + i + "\t");
				System.out.println(histoDFicao.get(i).size());
			}
		}
		System.out.println("Total icao count:\t" + icaoList.size());

		System.out.println("Count of icao with any position:\t" + icaoAnyCount.size());
		System.out.println("Count of icao with airborne position:\t" + icaoAirborneCount.size());
		System.out.println("Count of icao with surface position:\t" + icaoSurfCount.size());

		for (String icao0 : icaoSurfCount){
			if (icaoAirborneCount.contains(icao0))
				icaoBothCount.add(icao0);
		}
		System.out.println("Count of icao with both positions:\t" + icaoBothCount.size());

	}





















}
