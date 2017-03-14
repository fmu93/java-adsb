package org.opensky.example;

import java.io.File;

import output.SaveToDatabase;



public class Core{
	public static File inputHexx;
	public static GUIApplication guiApp;	
	public static SaveToDatabase saver;
	public static ExampleDecoder decoder;
	
	
	public static void main(String[] args){
		guiApp = new GUIApplication();
		saver = new SaveToDatabase();
		decoder = new ExampleDecoder();
		GUIApplication.main(null);
	}
	
	public static void runDecoder(String icaoFilter) throws Exception{
		
		String outFix = "digest_";
		String outFileName = outFix + inputHexx.getName();
		Core.saver.setOutPath(inputHexx.toPath().getParent(), outFileName);
		Core.decoder.runDecoder(icaoFilter);
	}
}
