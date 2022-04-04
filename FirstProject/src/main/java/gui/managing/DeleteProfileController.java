package gui.managing;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import fr.moneyManaging.Profile;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetroStyleClass;

public class DeleteProfileController implements Initializable {
	@FXML
	private Button deleteProfileValidate;
	@FXML
	private Button deleteProfileCancel;
	@FXML private AnchorPane mainPane;

	@FXML
	private ListView<String> profileList = new ListView<>();

	public void initialize(URL location, ResourceBundle resources) {	
		mainPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
		Collection<String> arrayProfileList = new ArrayList<String>();
		arrayProfileList = Profile.showProfileList();
		profileList.getItems().addAll(arrayProfileList);
		profileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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
	private void DeleteProfileValidate(ActionEvent event) {
		for (String selectedProfileName : profileList.getSelectionModel().getSelectedItems()) {
			if (!selectedProfileName.equals("Main profile")) {
				Profile.deleteProfile(selectedProfileName);
				
			}
		}
		((Stage) (((Button) event.getSource()).getScene().getWindow())).close();
	}

}
