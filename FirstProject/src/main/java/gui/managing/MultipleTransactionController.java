package gui.managing;

import java.net.URL;
import java.util.ResourceBundle;

import org.joda.time.LocalDate;

import fr.moneyManaging.MultipleAccounts;
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

public class MultipleTransactionController implements Initializable {
	@FXML private TextField amountText = new TextField();
	@FXML private ChoiceBox<String> linkChoice = new ChoiceBox<>();
	@FXML private TextField arrivalAmountText = new TextField();
	@FXML private TextField reasonText = new TextField();
	@FXML private Button validate = new Button();
	@FXML private AnchorPane mainPane;
	@FXML private Button cancel = new Button();
	@FXML private ChoiceBox<String> departId = new ChoiceBox<>();
	
	private String m_profileName = "";
	private MultipleAccounts accounts;
	private LocalDate date;
	
	public void initialize(URL location, ResourceBundle resources) {		
		mainPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
	}
	
	public void initData(String profileName, LocalDate date, MultipleAccounts accounts) {
		this.m_profileName = profileName;
		this.accounts = accounts;
		this.date = date;
		departId.getItems().addAll(accounts.getAccountsName());
		linkChoice.getItems().addAll(DbConnection.listAccounts(m_profileName, -1, -1).values());
	}
	
	@FXML
	private void exitButtonOnAction(ActionEvent event){
        ((Stage)(((Button)event.getSource()).getScene().getWindow())).close();      
}
	
	@FXML
	private void validate(ActionEvent event) {
		if (!departId.getSelectionModel().isEmpty()) {
		Boolean canValidate = true;
		int id = DbConnection.getAccountId(m_profileName, departId.getSelectionModel().getSelectedItem());
		int linked_account_id = DbConnection.getAccountId(m_profileName, linkChoice.getSelectionModel().getSelectedItem());
		Double amountDepart = Double.valueOf(amountText.getText());
		Double amountArrival = Double.valueOf(arrivalAmountText.getText());
		String reason = reasonText.getText();
		if (amountDepart == 0 || m_profileName.length()==0 || Math.signum(amountDepart) == Math.signum(amountArrival) || id==linked_account_id) {
			canValidate = false;
		}
		if (canValidate) {
			accounts.transaction(date, id, amountDepart, amountArrival, reason, linked_account_id);
	        ((Stage)(((Button)event.getSource()).getScene().getWindow())).close();  
		}
	}
	}	
	
}
