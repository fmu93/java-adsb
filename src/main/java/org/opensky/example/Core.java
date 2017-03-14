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
}
