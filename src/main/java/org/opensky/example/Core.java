package org.opensky.example;

import java.io.File;
import java.util.List;

import output.SaveToDatabase;



public class Core extends Thread{
	
	public static GUIThread guiThread;
	public static DecThread decThread;
	
	public static File inputHexx;
	public static List<Double> receiverReason; // lat, lon, range (km)
	public static String icaoFilter = "";
	public static int epochPrecision = 0;
	
	
	public static void main(String[] args){
		
		guiThread = new GUIThread();
		guiThread.setName("gui");
		guiThread.start();
		
		decThread = new DecThread();
		decThread.setName("dec");
	}
	
	
	public static void runDecoder() throws Exception{
		if (inputHexx != null){
			GUIApplication.controller.setPb(-1);
			SaveToDatabase.setEpochPrecision(epochPrecision);
			decThread.start();
		}
	}


	public static void endDecoder(boolean forced) throws InterruptedException{
		decThread.terminateDec();
		setPb(1);
	}

	public static void setPb(double progress){
		GUIApplication.controller.setPb(progress);
	}
}

class DecThread extends Thread{
	public static SaveToDatabase saver;
	public static ExampleDecoder decoder;

	public static boolean isInterrupted = false;

	public void run(){
		decoder = new ExampleDecoder();
		saver = new SaveToDatabase();
		while(!isInterrupted){
			try{					
				String outFix = "digest_";
				String outFileName = outFix + Core.inputHexx.getName();
				saver.setOutPath(Core.inputHexx.toPath().getParent(), outFileName);
				decoder.runDecoder(Core.icaoFilter);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public void terminateDec(){
		isInterrupted = true;
		saver.flushMemory();
		saver.closeWriter();
	}
}

class GUIThread extends Thread{
	public static GUIApplication guiApp;	// 44a826

	public void run(){
		guiApp = new GUIApplication();
		GUIApplication.main(null);
	}

}



	
