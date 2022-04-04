package gui.managing;

import java.net.URL;
import java.util.ResourceBundle;

import fr.moneyManaging.Account;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetroStyleClass;

public class ObtainController implements Initializable {
	@FXML private Button validate;
	@FXML private Button cancel;
	@FXML private TextField amountText;
	@FXML private TextField reasonText;
	@FXML private AnchorPane mainPane;
	private Account m_account;
	
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
		Double amount = Double.valueOf(amountText.getText());
		String reason = reasonText.getText();
		if (amount>0) {
			m_account.obtain(amount, reason);
			((Stage)(((Button)event.getSource()).getScene().getWindow())).close();  
		}

	}

}