package gui.managing;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

import fr.moneyManaging.Account;
import fr.moneyManaging.CurrentAccount;
import fr.moneyManaging.DetailedTransaction;
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

public class EditMultipleSaveTransactionController implements Initializable {
	@FXML private Button validate;
	@FXML private Button cancel;
	@FXML private TextField amountText;
	private int m_id_transaction;
	private String m_profileName;
	@FXML private AnchorPane mainPane;
	@FXML private ChoiceBox<String> accountChoice;
	DetailedTransaction transaction;
	Account account;
	
	public void initialize(URL location, ResourceBundle resources) {		
		mainPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
	}
	
	public void initData(int id_transaction, String profileName, Collection<String> accountsList) {
		this.m_id_transaction = id_transaction;
		this.m_profileName = profileName;
		transaction = new DetailedTransaction(m_id_transaction, m_profileName);
		amountText.setText(String.valueOf(-1*transaction.getTransact_amount()));
		accountChoice.getItems().addAll(accountsList);
		accountChoice.getSelectionModel().select(DbConnection.getAccountName(profileName, transaction.getId_account()));
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
			String selectedAccountName = accountChoice.getSelectionModel().getSelectedItem();
			int departAccountId = DbConnection.getAccountId(m_profileName, selectedAccountName);
			account = new CurrentAccount(m_profileName, selectedAccountName);
			Account.editTransact(m_profileName, m_id_transaction, amount*-1, amount, transaction.getReason(), transaction.getLink(), departAccountId);
			((CurrentAccount) account).updateTotalAdvancedSaving(transaction.getDate(), amount);
			((Stage)(((Button)event.getSource()).getScene().getWindow())).close();  
		}

	}

}