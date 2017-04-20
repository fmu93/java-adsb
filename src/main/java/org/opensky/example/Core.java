package org.opensky.example;

import java.io.File;
import java.util.List;

import javafx.application.Platform;
import output.SaveToDatabase;



public class Core{
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
	}	

	public static void runDecoder() throws Exception{
		if (inputHexx != null){
			decThread = new DecThread();
			decThread.setName("dec");
			setIndicatorProgress(-1);
			decThread.start();
		}
	}

	public static void endDecoder(boolean forced) throws InterruptedException{
		decThread.terminateDec();
		if (!forced)
			setIndicatorProgress(1);
	}
	
	public static void setIndicatorProgress(final double progress){
		Platform.runLater(new Runnable(){
			public void run(){
				GUIApplication.controller.setIndicatorProgress(progress);
			}
		});
	}
	
	public static void printConsole(final String message){
		Platform.runLater(new Runnable(){
			public void run(){
				GUIApplication.controller.printConsole(message);
			}
		});
	}

}

class DecThread extends Thread{
	public static SaveToDatabase saver;
	public static ExampleDecoder decoder;

	public void run(){
		decoder = new ExampleDecoder();
		saver = new SaveToDatabase(Core.epochPrecision);
		try{					
			String outFix = "digest_";
			String outFileName = outFix + Core.inputHexx.getName();
			saver.setOutPath(Core.inputHexx.toPath().getParent(), outFileName);
			decoder.runDecoder(Core.icaoFilter);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void terminateDec(){
		decoder.setInterrupted(true);
		saver.flushMemory();
		saver.closeWriter();
		this.interrupt();
		
	}
}

class GUIThread extends Thread{
	public GUIApplication guiApp;	// 44a826

	public void run(){
		guiApp = new GUIApplication();
		GUIApplication.main(null);
	}
}




