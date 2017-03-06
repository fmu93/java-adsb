package org.opensky.example;

import java.io.File;

public class Core{
	public static File inputHexx;
	public static GUIApplication guiApp;
	
	
	public static void main(String[] args){
		guiApp = new GUIApplication();
		GUIApplication.main(null);
		GUIApplication.controller.updatepb(0.5);
		GUIApplication.controller.pb.setProgress(0.5);
	}
}
