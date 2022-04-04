package gui.managing;

import java.net.URL;
import java.util.ResourceBundle;

import fr.moneyManaging.Account;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetroStyleClass;

public class DeleteSaveTransactionController implements Initializable {
	@FXML
	private Button validate;
	@FXML
	private Button cancel;
	@FXML private AnchorPane mainPane;
	private int m_id_transaction;
	private Account account;

	public void initialize(URL location, ResourceBundle resources) {	
		mainPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
	}

	public void initData(int id_transaction, Account account) {
		this.m_id_transaction = id_transaction;
		this.account = account;
	}

	@FXML
	/**
	 * Close the window where the ActionEvent is performed.
	 * 
	 * @param event Click on a button
	 */
	private void exitButtonOnAction(ActionEvent event) {
		((Stage) (((Button) event.getSource()).getScene().getWindow())).close();
	}

	@FXML
	private void Validate(ActionEvent event) {
		account.deleteTransact(m_id_transaction);
		((Stage) (((Button) event.getSource()).getScene().getWindow())).close();

	}

}