package gui.managing;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetroStyleClass;

public class DeleteMoneyErrorController implements Initializable {
	@FXML private Button okButton;
	@FXML private AnchorPane mainPane;
	
	public void initialize(URL location, ResourceBundle resources) {		
		mainPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
	}
	
	@FXML
	/**
	 * Close the window where the ActionEvent is performed.
	 * @param event	Click on a button
	 */
	private void newMoneyCancel(ActionEvent event){
        ((Stage)(((Button)event.getSource()).getScene().getWindow())).close();      
}

}