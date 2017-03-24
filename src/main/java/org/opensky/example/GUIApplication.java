package org.opensky.example;

import java.io.IOException;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GUIApplication extends Application{
	
	private Stage primaryStage;	
	public static Controller controller = new Controller();
	
	@Override
	public void start(Stage primaryStage) throws IOException{
		this.primaryStage = primaryStage;
		mainWindow();
	}
	
	public void mainWindow() throws IOException{
		try{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("GUI.fxml"));
			loader.setController(controller);
			BorderPane pane = loader.load();
			Scene scene = new Scene(pane, 500, 250);
			primaryStage.setMinHeight(500);
			primaryStage.setMinHeight(250);
			primaryStage.setScene(scene);
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		          public void handle(WindowEvent we) {
		              beforeClose();
		          }
		      });
			primaryStage.show();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		launch(args);
		
	}
	

	public void beforeClose(){
		System.out.println("bye!!");
		try{
			if (Core.decThread.isAlive()){
				Core.endDecoder(true);
			}
		}catch(Exception e){
			e.printStackTrace();
			
		}
		
	}
	
}
