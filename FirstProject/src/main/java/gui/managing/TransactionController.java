package gui.managing;

import java.net.URL;
import java.util.ResourceBundle;

import fr.moneyManaging.Account;
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

public class TransactionController implements Initializable {
	@FXML private TextField amountText = new TextField();
	@FXML private ChoiceBox<String> linkChoice = new ChoiceBox<>();
	@FXML private TextField arrivalAmountText = new TextField();
	@FXML private TextField reasonText = new TextField();
	@FXML private Button validate = new Button();
	@FXML private AnchorPane mainPane;
	@FXML private Button cancel = new Button();
	
	private String m_profileName = "";
	private String m_accountName = "";
	private Account m_account;
	
	public void initialize(URL location, ResourceBundle resources) {		
		mainPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
	}
	
	public void initData(String profileName, String accountName, Account account) {
		this.m_profileName = profileName;
		this.m_accountName = accountName;
		this.m_account = account;
		linkChoice.getItems().addAll(DbConnection.listAccounts(m_profileName, -1, -1).values());
		linkChoice.getItems().remove(m_accountName);
	}
	
	@FXML
	private void exitButtonOnAction(ActionEvent event){
        ((Stage)(((Button)event.getSource()).getScene().getWindow())).close();      
}
	
	@FXML
	private void validate(ActionEvent event) {
		Boolean canValidate = true;
		int linked_account_id = DbConnection.getAccountId(m_profileName, linkChoice.getSelectionModel().getSelectedItem());
		Double amountDepart = Double.valueOf(amountText.getText());
		Double amountArrival = Double.valueOf(arrivalAmountText.getText());
		String reason = reasonText.getText();
		if (amountDepart == 0 || m_profileName.length()==0 || m_accountName.length()==0 || Math.signum(amountDepart) == Math.signum(amountArrival)) {
			canValidate = false;
		}
		if (canValidate) {
			m_account.internalInternationalTransact(amountDepart, amountArrival, reason, linked_account_id);
	        ((Stage)(((Button)event.getSource()).getScene().getWindow())).close();  
		}
	}
	
	
}
