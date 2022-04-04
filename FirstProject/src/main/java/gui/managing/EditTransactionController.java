package gui.managing;

import java.net.URL;
import java.util.ResourceBundle;

import org.controlsfx.control.ToggleSwitch;

import fr.moneyManaging.Account;
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

public class EditTransactionController implements Initializable {
	@FXML private TextField amountText = new TextField();
	@FXML private ToggleSwitch linkedAccount = new ToggleSwitch();
	@FXML private ChoiceBox<String> linkChoice = new ChoiceBox<>();
	@FXML private TextField arrivalAmountText = new TextField();
	@FXML private TextField reasonText = new TextField();
	@FXML private Button validate = new Button();
	@FXML private AnchorPane mainPane;
	@FXML private Button cancel = new Button();
	private DetailedTransaction m_transaction;
	
	private String m_profileName = "";
	private int id_transaction = -1;
	
	public void initialize(URL location, ResourceBundle resources) {		
		mainPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
		
	}
	
	public void initData(String profileName, int id_transaction) {
		this.m_profileName = profileName;
		this.id_transaction = id_transaction;
		m_transaction = new DetailedTransaction(this.id_transaction, this.m_profileName);
		linkChoice.getItems().addAll(DbConnection.listAccounts(m_profileName, -1, -1).values());
		linkChoice.getItems().remove(DbConnection.getAccountName(m_profileName, m_transaction.getId_account()));
		amountText.setText(String.valueOf(m_transaction.getTransact_amount()));
		int linked_account = m_transaction.getLink();
		linkedAccount.setSelected(linked_account>=0);
		linkAnAccount();
		if (linked_account>=0) {
			DetailedTransaction linked_transaction = new DetailedTransaction(m_transaction.getLin_id_transact(), this.m_profileName);
			arrivalAmountText.setText(String.valueOf(linked_transaction.getTransact_amount()));
			linkChoice.getSelectionModel().select(DbConnection.getAccountName(m_profileName, linked_account));
		}
		reasonText.setText(m_transaction.getReason());
	}
	
	public void linkAnAccount() {
		if (linkedAccount.isSelected()) {
			linkChoice.setDisable(false);
			arrivalAmountText.setDisable(false);
		} else {
			linkChoice.setDisable(true);
			arrivalAmountText.setDisable(true);
		}
	}
	
	@FXML
	private void exitButtonOnAction(ActionEvent event){
        ((Stage)(((Button)event.getSource()).getScene().getWindow())).close();      
}
	
	@FXML
	private void validate(ActionEvent event) {
		Boolean canValidate = true;
		Boolean willLinkAccount = linkedAccount.isSelected();
		int linked_account_id = -1;
		Double amountDepart = Double.valueOf(amountText.getText());
		Double amountArrival = amountDepart * -1;
		String reason = reasonText.getText();
		if (willLinkAccount) {
				amountArrival = Double.valueOf(arrivalAmountText.getText());
				String accountToLink = linkChoice.getSelectionModel().getSelectedItem();
			if (Math.signum(amountDepart) == Math.signum(amountArrival) || accountToLink==null) {
				canValidate = false;
			} else {
				linked_account_id = DbConnection.getAccountId(m_profileName, accountToLink);
			}
		}
		if (amountDepart == 0 || m_profileName.length()==0 || id_transaction==-1) {
			canValidate = false;
		}
		if (canValidate) {
			Account.editTransact(m_profileName, id_transaction, amountDepart, amountArrival, reason, linked_account_id,-1);
	        ((Stage)(((Button)event.getSource()).getScene().getWindow())).close();  
		}
	}
	
	
}
