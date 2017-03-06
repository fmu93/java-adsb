package org.opensky.example;

import java.io.File;
import java.util.Arrays;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;



public class Controller {
	public File selectedFile;
	public String[] args = new String[0];

    @FXML    private Button btSelHexx;
    @FXML    private Button btDecode;
    @FXML    private TextField txtICAO;
    @FXML 	 public ProgressBar pb;
    @FXML	private Label lblProgress;
    
    @FXML    void runDecode(ActionEvent event) {
    	try {
    		if (txtICAO.getText().isEmpty()){
    			
    		}else{
    			args = (String[]) Arrays.asList(txtICAO.getText()).toArray();
    		}

    		ExampleDecoder.runDecoder(args);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    @FXML    void selHexx(ActionEvent event) {
    	try{
    		FileChooser fc = new FileChooser();
    		fc.setInitialDirectory(new File(System.getProperty("user.dir")));
    		selectedFile = fc.showOpenDialog(null);

    		if (selectedFile != null){
    			btSelHexx.setText(selectedFile.getName());
    			Core.inputHexx = selectedFile;
    		}else{
    			System.out.println("not valid file!");
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }

    public void updatepb(double progress){
    		pb.setProgress(progress);
    		lblProgress.setText(String.format("%.0f", progress*100) + "%");
    		
    }
    



}
