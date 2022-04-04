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

public class MultipleSpendController implements Initializable {
	@FXML private Button validate;
	@FXML private Button cancel;
	@FXML private TextField amountText;
	@FXML private TextField reasonText;
	@FXML private AnchorPane mainPane;
	@FXML private ChoiceBox<String> accountChoice;
	private MultipleAccounts m_accounts;
	private String m_profileName = "";
	private LocalDate date;
	
	public void initialize(URL location, ResourceBundle resources) {		
		mainPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
	}
	
	public void initData(String profileName, LocalDate date, MultipleAccounts accounts) {
		this.m_profileName = profileName;
		this.date = date;
		this.m_accounts = accounts;
		accountChoice.getItems().addAll(accounts.getAccountsName());
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
		if (m_profileName.length() != 0 && amount>0 && !accountChoice.getSelectionModel().isEmpty()) {
			int id = DbConnection.getAccountId(m_profileName, accountChoice.getSelectionModel().getSelectedItem());
			m_accounts.spend(date, id, amount, reason);
			((Stage)(((Button)event.getSource()).getScene().getWindow())).close();  
		}

	}

}