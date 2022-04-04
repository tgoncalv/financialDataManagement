package gui.managing;

import java.net.URL;
import java.util.ResourceBundle;

import fr.moneyManaging.Account;
import fr.moneyManaging.CurrentAccount;
import fr.moneyManaging.DetailedTransaction;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetroStyleClass;

public class EditSaveTransactionController implements Initializable {
	@FXML private Button validate;
	@FXML private Button cancel;
	@FXML private TextField amountText;
	private int m_id_transaction;
	private String m_profileName;
	@FXML private AnchorPane mainPane;
	DetailedTransaction transaction;
	Account account;
	
	public void initialize(URL location, ResourceBundle resources) {		
		mainPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
	}
	
	public void initData(int id_transaction, String profileName, Account account) {
		this.m_id_transaction = id_transaction;
		this.m_profileName = profileName;
		transaction = new DetailedTransaction(m_id_transaction, m_profileName);
		amountText.setText(String.valueOf(-1*transaction.getTransact_amount()));
		this.account = account;
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
		int amount = Integer.valueOf(amountText.getText());
		if (amount>0) {
			Account.editTransact(m_profileName, m_id_transaction, amount*-1, amount, transaction.getReason(), transaction.getLink(),-1);
			((CurrentAccount) account).updateTotalAdvancedSaving(transaction.getDate(), amount);
			((Stage)(((Button)event.getSource()).getScene().getWindow())).close();  
		}

	}

}