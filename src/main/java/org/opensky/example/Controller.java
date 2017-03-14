package org.opensky.example;

import java.io.File;
import java.util.Arrays;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;


public class Controller {
	
    @FXML    private Button btSelHexx;
    @FXML    private Button btDecode;
    @FXML    private TextField txtICAO;
    @FXML    private TextField txtPrecis;
    
 
    @FXML    void runDecode(ActionEvent event) throws Exception{
    	String icaoFilter = "";

		setPrecis();
    	if (txtICAO.getText().isEmpty()){
    		
    	}else{
    		icaoFilter = txtICAO.getText();
    	}
    	Core.runDecoder(icaoFilter);
    }

    @FXML    void selHexx(ActionEvent event) {
    	try{
    		FileChooser fc = new FileChooser();
    		fc.setInitialDirectory(new File(System.getProperty("user.dir")));
    		Core.inputHexx = fc.showOpenDialog(null);

    		if (Core.inputHexx != null){
    			btSelHexx.setText(Core.inputHexx.getName());
    		}else{
    			System.out.println("not valid file!");
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }

    @FXML    void setPrecis() {
    	if (txtPrecis.getText().matches("[0-9]+")){
    		Core.saver.setEpochPrecision(Integer.valueOf(txtPrecis.getText()));
    	}else{
    		Core.saver.setEpochPrecision(0);
    	}
    		
    	
    }



}
