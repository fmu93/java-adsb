package org.opensky.example;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.opensky.libadsb.pLibrary;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;


public class Controller {

	@FXML    private Button btSelHexx;
	@FXML    private Button btDecode;
	@FXML    private TextField txtICAO;
	@FXML    private TextField txtPrecis;
	@FXML	 private TextField txtRange;
	@FXML	 private TextField txtLatLon;
	@FXML	 private ProgressIndicator progInd;


	@FXML    void runDecode(ActionEvent event) throws Exception{

		setPrecis();
		setReceiverReason();
		if (txtICAO.getText().isEmpty()){

		}else{
			Core.icaoFilter = txtICAO.getText();
		}
		Core.runDecoder();
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
			Core.epochPrecision = Integer.valueOf(txtPrecis.getText());
		}else{
			Core.epochPrecision = 0;
		}   	
	}

	public void setReceiverReason(){    	
		try{
			List<String> LatLon = pLibrary.comma2list(txtLatLon.getText());
			if (LatLon.size() == 2){
				double lat = Double.valueOf(LatLon.get(0));
				double lon = Double.valueOf(LatLon.get(1));
				double range = Double.valueOf(txtRange.getText());

				Core.receiverReason = Arrays.asList(lat, lon, range);
			}
		}catch(Exception e){
			Core.receiverReason = null;
		}
	}

	public void setPb(double progress){
		try{
			progInd.setProgress(progress);
		}catch(Exception e){

		}
	}

}
