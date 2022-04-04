package gui.managing;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import fr.moneyManaging.MoneyTypes;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.JMetroStyleClass;
import jfxtras.styles.jmetro.Style;
import me.dbManaging.DbConnection;

public class MoneyTypesController implements Initializable {
	private String m_profileName = "";
	@FXML
	private ListView<String> moneyTypesList = new ListView<>();
	@FXML
	private Button exitButton;
	@FXML
	private ContextMenu contextMenu;
	@FXML
	private MenuItem createNewMoney;
	@FXML private AnchorPane mainPane;
	@FXML
	private MenuItem deleteMoney;

	public void initialize(URL location, ResourceBundle resources) {	
		mainPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
		moneyTypesList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		confListView();
	}

	public void initData(String profileName) {
		this.m_profileName = profileName;
		Collection<String> moneyTypes = new ArrayList<String>();
		moneyTypes = MoneyTypes.viewMoneyTypes(m_profileName).values();
		moneyTypesList.getItems().addAll(moneyTypes);
	}
	
	private void confListView() {
        moneyTypesList.setCellFactory(lv -> {

            ListCell<String> cell = new ListCell<>();

            ContextMenu contextMenu = new ContextMenu();

            MenuItem editItem = new MenuItem();
            editItem.textProperty().bind(Bindings.format("Create a new money", cell.itemProperty()));
            editItem.setOnAction(event -> {
                //String item = cell.getItem();
                // code to edit item...
                createMoney();
            });
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete \"%s\"", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
            	deleteMoney();
            });
            contextMenu.getItems().addAll(editItem, deleteItem);

            cell.textProperty().bind(cell.itemProperty());

            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell ;
        });
	}

	@FXML
	private void deleteMoney() {
		String moneyName = moneyTypesList.getSelectionModel().getSelectedItem();
		if (DbConnection.listAccounts(m_profileName, MoneyTypes.getMoneyId(m_profileName, moneyName), -1).size() > 0) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/DeleteMoneyError.fxml"));
				Parent root = loader.load();

				Stage stage = new Stage();
				stage.setTitle("Error");
				Scene scene = new Scene(root);
	            JMetro jMetro = new JMetro(Style.DARK);
	            jMetro.setScene(scene);
				stage.setScene(scene);
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setResizable(false);
				stage.showAndWait();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			MoneyTypes.deleteMoney(m_profileName, moneyName);
			moneyTypesList.getItems().clear();
			moneyTypesList.getItems().addAll(MoneyTypes.viewMoneyTypes(m_profileName).values());

		}
	}

	/*
	@FXML
	private void contextMenuSettings() {
		if (moneyTypesList.getSelectionModel().isEmpty()) {
			deleteMoney.disableProperty();
		} else {
			deleteMoney.setDisable(false);
		}

	}


	@FXML
	private void unselectItems() {

		
		System.out.println(moneyTypesList.cellFactoryProperty().getValue());
		moneyTypesList.isHover();
		moneyTypesList.cellFactoryProperty();
		System.out.println(moneyTypesList.getCellFactory());
		moneyTypesList.getFocusModel().focus(0);
		System.out.println(moneyTypesList.getSelectionModel().getSelectedIndex());
		if (moneyTypesList.getSelectionModel().getSelectedIndex() == -1) {
			moneyTypesList.getSelectionModel().clearSelection();
		}
	}
	*/
	
	

	@FXML
	private void createMoney() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/NewMoney.fxml"));
			Parent root = loader.load();

			NewMoneyController newMoneyController = loader.getController();
			newMoneyController.initData(m_profileName);

			Stage stage = new Stage();
			stage.setTitle("Create a new money");
			Scene scene = new Scene(root);
            JMetro jMetro = new JMetro(Style.DARK);
            jMetro.setScene(scene);
			stage.setScene(scene);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setResizable(false);
			stage.showAndWait();
			moneyTypesList.getItems().clear();
			moneyTypesList.getItems().addAll(MoneyTypes.viewMoneyTypes(m_profileName).values());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void exitButtonOnAction(ActionEvent event) {
		((Stage) (((Button) event.getSource()).getScene().getWindow())).close();
	}
}
