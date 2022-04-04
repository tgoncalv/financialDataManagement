package gui.managing;

import java.net.URL;
import java.util.ResourceBundle;

import fr.moneyManaging.Account;
import fr.moneyManaging.MoneyTypes;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetroStyleClass;

public class ChangeMoneyTypeController implements Initializable {
	@FXML
	private Button validate;
	@FXML
	private Button cancel;
	private String m_profileName = "";
	private Account m_account;
	@FXML private AnchorPane mainPane;
	
	@FXML
	private ListView<String> list = new ListView<>();

	public void initialize(URL location, ResourceBundle resources) {	
		mainPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
		list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
	}
	
	public void initData(String profileName, Account account) {
		this.m_profileName = profileName;
		this.m_account = account;
		list.getItems().addAll(MoneyTypes.viewMoneyTypes(m_profileName).values());
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
	private void validate(ActionEvent event) {
		if (m_profileName.length()!=0) {
			m_account.changeMoneyRef(list.getSelectionModel().getSelectedItem());
			((Stage) (((Button) event.getSource()).getScene().getWindow())).close();
		}
	}

}
