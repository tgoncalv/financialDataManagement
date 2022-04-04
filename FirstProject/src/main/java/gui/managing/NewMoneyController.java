package gui.managing;

import java.net.URL;
import java.util.ResourceBundle;

import fr.moneyManaging.MoneyTypes;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetroStyleClass;

public class NewMoneyController implements Initializable {
	@FXML private Button newMoneyValidate;
	@FXML private Button newMoneyCancel;
	@FXML private TextField newMoneyNameTextField;
	private String m_profileName = "";
	@FXML private AnchorPane mainPane;
	
	public void initialize(URL location, ResourceBundle resources) {		
		mainPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
	}
	
	public void initData(String profileName) {
		this.m_profileName = profileName;
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
	private void NewMoneyValidate(ActionEvent event) {
		String newMoneyName = newMoneyNameTextField.getText();
		if (newMoneyName.length()!=0) {
			MoneyTypes.createMoney(m_profileName,newMoneyName);
			((Stage)(((Button)event.getSource()).getScene().getWindow())).close();  
		}

	}

}