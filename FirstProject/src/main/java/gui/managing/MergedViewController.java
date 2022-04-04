package gui.managing;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import fr.moneyManaging.MoneyTypes;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import jfxtras.styles.jmetro.JMetroStyleClass;
import me.dbManaging.DbConnection;

public class MergedViewController implements Initializable {
	private String m_profileName = "";
	@FXML
	private ListView<String> accountsList = new ListView<>();
	@FXML
	private Button exitButton;
	@FXML
	private Button validateButton;
	@FXML
	private AnchorPane mainPane;
	@FXML
	private Button selectAllButton;
	@FXML
	private Button unselectAllButton;
	@FXML
	private ChoiceBox<String> moneyTypeChoiceBox;
	@FXML
	private ChoiceBox<String> accountTypeChoiceBox;
	private Collection<String> m_selectedAccounts = new ArrayList<>();
	private Map<String, ObservableValue<Boolean>> map = new HashMap<>();
	private HashMap<Integer, String> moneyTypes = new HashMap<>();

	public void initialize(URL location, ResourceBundle resources) {
		mainPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
		accountsList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
	}

	public void initData(String profileName) {
		this.m_profileName = profileName;
		moneyTypes = MoneyTypes.viewMoneyTypes(profileName);
		moneyTypeChoiceBox.getItems().add("All");
		moneyTypeChoiceBox.getItems().addAll(moneyTypes.values());
		moneyTypeChoiceBox.getSelectionModel().selectFirst();
		moneyTypeChoiceBox.getSelectionModel().selectedItemProperty()
		.addListener((ObservableValue<? extends String> observable, String oldValue,
				String newValue) -> updateList());
		accountTypeChoiceBox.getItems().addAll("All", "Current Account", "Economy Account");
		accountTypeChoiceBox.getSelectionModel().selectedItemProperty()
				.addListener((ObservableValue<? extends String> observable, String oldValue,
						String newValue) -> updateList());
		accountTypeChoiceBox.getSelectionModel().selectFirst();

	}

	@FXML
	private void updateList() {
		String accountType = accountTypeChoiceBox.getSelectionModel().getSelectedItem();
		String moneyType = moneyTypeChoiceBox.getSelectionModel().getSelectedItem();
		int accountTypeInt = -1, moneyTypeInt = -1;
		if (!accountType.equals("All")) {
			if (accountType.equals("Current Account")) {
				accountTypeInt = 1;
			}
			else if (accountType.equals("Economy Account")) {
				accountTypeInt = 2;
			}
		}
		if (!moneyType.equals("All")) {
			for (Map.Entry<Integer,String> moneyTypeEntry : this.moneyTypes.entrySet()) {
				if (moneyTypeEntry.getValue() == moneyType) {
					moneyTypeInt = moneyTypeEntry.getKey();
					break;
				}
			}
		}
		accountsList.getItems().clear();
		m_selectedAccounts.clear();
		map.clear();
		Collection<String> accountsName = DbConnection.listAccounts(m_profileName,moneyTypeInt, accountTypeInt).values();
		for (String accountName : accountsName) {
			map.put(accountName, new SimpleBooleanProperty(false));
		}
		accountsList.getItems().addAll(map.keySet());

		Callback<String, ObservableValue<Boolean>> itemToBoolean = (String item) -> map.get(item);

		Callback<ListView<String>, ListCell<String>> defaultCellFactory = CheckBoxListCell.forListView(itemToBoolean);

		accountsList.setCellFactory(lv -> {
			ListCell<String> cell = defaultCellFactory.call(lv);
			return cell;
		});
	}

	@FXML
	private void selectAll(ActionEvent event) {
		for (String key : map.keySet()) {
			accountsList.getSelectionModel().select(key);
			map.replace(key, new SimpleBooleanProperty(true));
		}
		accountsList.getSelectionModel().clearSelection();
	}
	
	@FXML
	private void unselectAll(ActionEvent event) {
		for (String key : map.keySet()) {
			accountsList.getSelectionModel().select(key);
			map.replace(key, new SimpleBooleanProperty(false));
		}
		accountsList.getSelectionModel().clearSelection();
	}
	
	@FXML
	private void exitButtonOnAction(ActionEvent event) {
		((Stage) (((Button) event.getSource()).getScene().getWindow())).close();
	}

	@FXML
	private void validate(ActionEvent event) {
		Collection<String> selectedAccounts = new ArrayList<>();
		for (String key : map.keySet()) {
			if (map.get(key).getValue()) {
				selectedAccounts.add(key);
			}
		}
		if (m_profileName.length() > 0 && selectedAccounts.size() > 1) {
			m_selectedAccounts = selectedAccounts;
			((Stage) (((Button) event.getSource()).getScene().getWindow())).close();
		}
	}

	public Collection<String> getSelectedAccounts() {
		return m_selectedAccounts;
	}
	
	public Boolean haveSavingsData() {
		return (accountTypeChoiceBox.getSelectionModel().getSelectedIndex()!=0 && moneyTypeChoiceBox.getSelectionModel().getSelectedIndex()!=0);
	}

	@FXML
	private void unselect() {
		String checkBox = accountsList.getSelectionModel().getSelectedItem();
		if (map.get(checkBox).getValue()) {
			map.replace(checkBox, new SimpleBooleanProperty(false));
		} else {
			map.replace(checkBox, new SimpleBooleanProperty(true));
		}
		accountsList.getSelectionModel().clearSelection();
	}
}
