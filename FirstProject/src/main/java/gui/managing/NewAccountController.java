package gui.managing;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import fr.moneyManaging.MoneyTypes;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetroStyleClass;
import me.dbManaging.DbConnection;

public class NewAccountController implements Initializable {
	@FXML private Button newAccountValidate;
	@FXML private Button newAccountCancel;
	@FXML private TextField newAccountNameTextField;
	@FXML private ChoiceBox<String> newAccountTypeChoice;
	@FXML private ChoiceBox<String> newAccountMoneyChoice;
	@FXML private TextField newAccountSource;
	@FXML private AnchorPane mainPane;
	private String m_profileName = "";
	
	public void initialize(URL location, ResourceBundle resources) {		
		mainPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
		newAccountTypeChoice.getItems().addAll("Current account","Economy account");
	}
	
	public void initData(String profileName) {
		this.m_profileName = profileName;
		Collection<String> moneyTypes = new ArrayList<String>();
		moneyTypes = MoneyTypes.viewMoneyTypes(m_profileName).values();
		newAccountMoneyChoice.getItems().addAll(moneyTypes);
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
	private void NewAccountValidate(ActionEvent event) {
		String newAccountName = newAccountNameTextField.getText();
		int newAccountMoneyId = MoneyTypes.getMoneyId(m_profileName,newAccountMoneyChoice.getValue());
		if (m_profileName.length()!=0 && newAccountName.length()!=0 && newAccountMoneyId>=0) {
			int accountType = -1;
			if (newAccountTypeChoice.getValue().equals("Current account")) {
				accountType = 1;
			} else {
				accountType = 2;
			}
			if (accountType != -1) {
				DbConnection.createAccount(m_profileName, newAccountName, newAccountMoneyId, accountType, newAccountSource.getText());
				((Stage)(((Button)event.getSource()).getScene().getWindow())).close();  
			}

		}

	}

}
