package gui.managing;

import java.net.URL;
import java.util.ResourceBundle;

import fr.moneyManaging.Account;
import fr.moneyManaging.CurrentAccount;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetroStyleClass;

public class AddLossesController implements Initializable {
	@FXML private Button validate;
	@FXML private Button cancel;
	@FXML private TextField amountText;
	private Account m_account;
	@FXML private AnchorPane mainPane;
	
	public void initialize(URL location, ResourceBundle resources) {	
		mainPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
	}
	
	public void initData(Account account) {
		this.m_account = account;
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
	private void Validate(ActionEvent event) {
		double amount = Double.valueOf(amountText.getText());
		if (amount!=0) {
			((CurrentAccount) m_account).addLosses(amount);
			((Stage)(((Button)event.getSource()).getScene().getWindow())).close();  
		}

	}

}