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

public class ChangeAccountSourceController implements Initializable {
	@FXML private Button validate;
	@FXML private Button cancel;
	@FXML private TextField textField;
	private Account m_account;
	private String m_source = "";
	@FXML private AnchorPane mainPane;
	
	public void initialize(URL location, ResourceBundle resources) {		
		mainPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
	}
	
	public void initData(Account account, String source) {
		this.m_account = account;
		this.m_source = source;
		textField.setText(source);
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
	private void NewMoneyValidate(ActionEvent event) {
		String source = textField.getText();
		if (source.length()!=0 && !source.equals(m_source)) {
			m_account.changeAccountSource(source);
			((Stage)(((Button)event.getSource()).getScene().getWindow())).close();  
		}

	}

}