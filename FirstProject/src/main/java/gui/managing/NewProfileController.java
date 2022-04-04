package gui.managing;

import java.net.URL;
import java.util.ResourceBundle;

import fr.moneyManaging.Profile;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetroStyleClass;

public class NewProfileController implements Initializable {
	@FXML private Button newProfileValidate;
	@FXML private AnchorPane mainPane;
	@FXML private Button newProfileCancel;
	@FXML private TextField newProfileNameTextField;
	
	public void initialize(URL location, ResourceBundle resources) {		
		mainPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
	}
	
	@FXML
	/**
	 * Close the window where the ActionEvent is performed.
	 * @param event	Click on a button
	 */
	private void exitButtonOnAction(ActionEvent event){
        ((Stage)(((Button)event.getSource()).getScene().getWindow())).close();      
}
	
	@FXML
	private void NewProfileValidate(ActionEvent event) {
		String newProfileName = newProfileNameTextField.getText();
		if (newProfileName.length()!=0) {
			Profile.createProfile(newProfileName);
			((Stage)(((Button)event.getSource()).getScene().getWindow())).close();  
		}

	}

}