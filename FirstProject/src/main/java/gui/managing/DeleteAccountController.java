package gui.managing;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetroStyleClass;
import me.dbManaging.DbConnection;

public class DeleteAccountController implements Initializable {
	@FXML
	private Button deleteAccountValidate;
	@FXML
	private Button deleteAccountCancel;
	private String m_profileName = "";
	@FXML private AnchorPane mainPane;

	@FXML
	private ListView<String> accountList = new ListView<>();

	public void initialize(URL location, ResourceBundle resources) {	
		mainPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
		accountList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
	}

	public void initData(String profileName) {
		this.m_profileName = profileName;
		Collection<String> arrayAccountList = new ArrayList<String>();
		arrayAccountList = DbConnection.listAccounts(m_profileName, -1, -1).values();
		accountList.getItems().addAll(arrayAccountList);
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
	private void deleteAccountValidate(ActionEvent event) {
		if (m_profileName.length() != 0) {
			for (String selectedAccountName : accountList.getSelectionModel().getSelectedItems()) {
				DbConnection.deleteAccount(m_profileName,
						DbConnection.getAccountId(m_profileName, selectedAccountName));
			}
			((Stage) (((Button) event.getSource()).getScene().getWindow())).close();
		}

	}

}
