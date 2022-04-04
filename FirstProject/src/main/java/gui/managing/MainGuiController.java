package gui.managing;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import org.controlsfx.control.ToggleSwitch;
import org.joda.time.LocalDate;

import fr.moneyManaging.Account;
import fr.moneyManaging.CurrentAccount;
import fr.moneyManaging.DateIntStringList;
import fr.moneyManaging.DetailedTransaction;
import fr.moneyManaging.DoubleDoubleDoubleList;
import fr.moneyManaging.DoubleStringIntList;
import fr.moneyManaging.EconomyAccount;
import fr.moneyManaging.MoneyTypes;
import fr.moneyManaging.MultipleAccounts;
import fr.moneyManaging.Profile;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.JMetroStyleClass;
import jfxtras.styles.jmetro.Style;
import me.dbManaging.DbConnection;

public class MainGuiController implements Initializable {
	@FXML
	BorderPane borderPane = new BorderPane();
	private String m_profileName = "";
	private String m_accountName = "";
	private Account m_actualAccount;
	private int m_actualAccountType = -1;
	private String m_actualAccountMoney = "none";
	private String m_actualAccountSource = "none";
	private LocalDate m_mainTablePointingDate = new LocalDate();
	private Collection<String> m_selectedAccounts = new ArrayList<String>();
	private MultipleAccounts m_multipleAccounts;
	private int multipleAccountAmountToSave = 0;

	// Attributes for the menu bar
	@FXML
	private MenuBar menuBar = new MenuBar();
	@FXML
	private Menu mainMenuProfile = new Menu("Profile");
	@FXML
	private Menu mainMenuAccounts = new Menu("Accounts");
	@FXML
	private Menu mainMenuMoneyTypes = new Menu("MoneyTypes");
	@FXML
	private Menu mainMenuShow = new Menu("Show");
	@FXML
	private Menu mainMenuSave = new Menu("Save");
	@FXML
	private Menu subMenuProfileSelect = new Menu("Select");
	@FXML
	private MenuItem subMenuProfileNew = new MenuItem("New");
	@FXML
	private MenuItem subMenuProfileDelete = new MenuItem("Delete");
	@FXML
	private Menu subMenuAccountsCurrent = new Menu("Current Accounts");
	@FXML
	private Menu subMenuAccountsEconomy = new Menu("Economy Accounts");
	@FXML
	private MenuItem subMenuAccountsNew = new MenuItem("New Account");
	@FXML
	private MenuItem subMenuAccountsDelete = new MenuItem("Delete an Account");
	@FXML
	private MenuItem subMenuMoneyTypes = new MenuItem("Money Types");
	@FXML
	private Menu subMenuShowDataBy = new Menu("Show Data by");
	@FXML
	private RadioMenuItem showByYear = new RadioMenuItem("year");
	@FXML
	private RadioMenuItem showByMonth = new RadioMenuItem("month");
	@FXML
	private RadioMenuItem showByWeek = new RadioMenuItem("week");
	private ToggleGroup showByGroup = new ToggleGroup();
	@FXML
	private MenuItem subMenuSaveLoad = new MenuItem("Load...");
	@FXML
	private MenuItem subMenuSaveSave = new MenuItem("Save");
	@FXML
	private MenuItem mergedView = new MenuItem("Merged view");

	// Attributes for the global informations
	@FXML
	private Text actualProfile = new Text();
	@FXML
	private Text actualAccountField = new Text();
	@FXML
	private Text actualAccountMoneyType = new Text();
	@FXML
	private Hyperlink changeMoneyTypeButton = new Hyperlink();
	@FXML
	private Text actualAccountSource = new Text();
	@FXML
	private Hyperlink changeAccountSource = new Hyperlink();

	// Attributes for the main table date selector
	@FXML
	private Button previousDate = new Button();
	@FXML
	private Button nextDate = new Button();
	@FXML
	private Text mainTableGlobalDate = new Text();

	// Attributes for the main table
	@FXML
	private TableView<MainTableManaging> mainTable = new TableView<MainTableManaging>();
	@FXML
	private TableColumn<MainTableManaging, LocalDate> dateColumn = new TableColumn<>();
	@FXML
	private TableColumn<MainTableManaging, Double> expensesColumn = new TableColumn<>();
	@FXML
	private TableColumn<MainTableManaging, Double> gainsColumn = new TableColumn<>();
	@FXML
	private TableColumn<MainTableManaging, Double> profitsColumn = new TableColumn<>();
	private ObservableList<MainTableManaging> mainTableRows = FXCollections.observableArrayList();

	// Attributes for the transaction table
	@FXML
	private Text selectedDateDay = new Text();
	@FXML
	private TableView<TransactionsTableManaging> transactionsTable = new TableView<TransactionsTableManaging>();
	@FXML
	private TableColumn<TransactionsTableManaging, Double> amountColumn = new TableColumn<>();
	@FXML
	private TableColumn<TransactionsTableManaging, String> reasonColumn = new TableColumn<>();
	private ObservableList<TransactionsTableManaging> transactionsTableRows = FXCollections.observableArrayList();
	@FXML
	private Button spendMoney = new Button();
	@FXML
	private Button obtainMoney = new Button();
	@FXML
	private Button doATransaction = new Button();
	@FXML
	private TableView<MultipleTransactionsTableManaging> multipleTransactionsTable = new TableView<MultipleTransactionsTableManaging>();
	@FXML
	private TableColumn<MultipleTransactionsTableManaging, String> multipleIdColumn = new TableColumn<>();
	@FXML
	private TableColumn<MultipleTransactionsTableManaging, Double> multipleAmountColumn = new TableColumn<>();
	@FXML
	private TableColumn<MultipleTransactionsTableManaging, String> multipleReasonColumn = new TableColumn<>();
	private ObservableList<MultipleTransactionsTableManaging> multipleTransactionsTableRows = FXCollections
			.observableArrayList();

	// Attributes for the month's results box
	@FXML
	private Text monthGainText = new Text();
	@FXML
	private Text cumulatedGainText = new Text();
	@FXML
	private Text cumulatedGainDateText = new Text();
	@FXML
	private Text TotalMoneyText = new Text();
	@FXML
	private DatePicker cumulatedGainDatePicker = new DatePicker();

	// Attributes for saving data
	@FXML
	private Text SavingMonthText = new Text();
	@FXML
	private ChoiceBox<String> debitAccount = new ChoiceBox<>();
	@FXML
	private ChoiceBox<String> associatedEcoAccount = new ChoiceBox<>();
	@FXML
	private ToggleSwitch manualSaving = new ToggleSwitch();
	@FXML
	private TextField economyRateText = new TextField();
	@FXML
	private TextField amountToSaveText = new TextField();
	@FXML
	private Text amountMoneyTypeText = new Text();
	@FXML
	private Text monthProfitText = new Text();
	@FXML
	private Text profitAfterSavingText = new Text();
	@FXML
	private Text advancedMoneyText = new Text();
	@FXML
	private Button advanceMoneyButton = new Button();
	@FXML
	private Text lossesText = new Text();
	@FXML
	private Button lossesButton = new Button();
	@FXML
	private TableView<SavingsTableManaging> savingsTable = new TableView<SavingsTableManaging>();
	@FXML
	private TableColumn<SavingsTableManaging, LocalDate> savedDateColumn = new TableColumn<>();
	@FXML
	private TableColumn<SavingsTableManaging, Double> savedAmountColumn = new TableColumn<>();
	@FXML
	private TableColumn<SavingsTableManaging, String> savedAccountLinkColumn = new TableColumn<>();
	private ObservableList<SavingsTableManaging> savingsTableRows = FXCollections.observableArrayList();
	@FXML
	private VBox boxForCurrentAccounts = new VBox();

	public void initialize(URL location, ResourceBundle resources) {
		actualProfile.setText("Profile : not selected");

		showByYear.setToggleGroup(showByGroup);
		showByMonth.setToggleGroup(showByGroup);
		showByMonth.setSelected(true);
		showByWeek.setToggleGroup(showByGroup);

		updateMenuProfileSelect();
		updateMenuAccountSelect();

		savedDateColumn.setCellValueFactory(new PropertyValueFactory<SavingsTableManaging, LocalDate>("rowDate"));
		savedAmountColumn.setCellValueFactory(new PropertyValueFactory<SavingsTableManaging, Double>("rowAmount"));
		savedAccountLinkColumn.setCellValueFactory(new PropertyValueFactory<SavingsTableManaging, String>("rowLink"));
		savedDateColumn.setStyle("-fx-alignment: CENTER;");
		savedAmountColumn.setStyle("-fx-alignment: CENTER;");
		savedAccountLinkColumn.setStyle("-fx-alignment: CENTER;");
		savingsTable.setItems(savingsTableRows);

		amountColumn.setCellValueFactory(new PropertyValueFactory<TransactionsTableManaging, Double>("amount"));
		reasonColumn.setCellValueFactory(new PropertyValueFactory<TransactionsTableManaging, String>("reason"));
		amountColumn.setStyle("-fx-alignment: CENTER;");
		reasonColumn.setStyle("-fx-alignment: CENTER;");
		transactionsTable.setItems(transactionsTableRows);

		multipleIdColumn.setCellValueFactory(
				new PropertyValueFactory<MultipleTransactionsTableManaging, String>("name_account"));
		multipleAmountColumn
				.setCellValueFactory(new PropertyValueFactory<MultipleTransactionsTableManaging, Double>("amount"));
		multipleReasonColumn
				.setCellValueFactory(new PropertyValueFactory<MultipleTransactionsTableManaging, String>("reason"));
		multipleIdColumn.setStyle("-fx-alignment: CENTER;");
		multipleAmountColumn.setStyle("-fx-alignment: CENTER;");
		multipleReasonColumn.setStyle("-fx-alignment: CENTER;");
		multipleTransactionsTable.setItems(multipleTransactionsTableRows);

		dateColumn.setCellValueFactory(new PropertyValueFactory<MainTableManaging, LocalDate>("rowDate"));
		expensesColumn.setCellValueFactory(new PropertyValueFactory<MainTableManaging, Double>("rowExpense"));
		gainsColumn.setCellValueFactory(new PropertyValueFactory<MainTableManaging, Double>("rowGain"));
		profitsColumn.setCellValueFactory(new PropertyValueFactory<MainTableManaging, Double>("rowProfit"));
		dateColumn.setStyle("-fx-alignment: CENTER;");
		expensesColumn.setStyle("-fx-alignment: CENTER;");
		gainsColumn.setStyle("-fx-alignment: CENTER;");
		profitsColumn.setStyle("-fx-alignment: CENTER;");
		mainTable.setItems(mainTableRows);
		updateMainTable();

		// force the fields to be numeric only
		economyRateText.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*") || Integer.valueOf(newValue) < 0 || Integer.valueOf(newValue) > 100) {
					economyRateText.setText(newValue.replaceAll("[^\\d]", ""));
				}
				calculateAmountToSave();
			}
		});
		amountToSaveText.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*") || Integer.valueOf(newValue) < 0) {
					amountToSaveText.setText(newValue.replaceAll("[^\\d]", ""));
				}
				if (m_actualAccount != null && m_actualAccountType == 1) {
					((CurrentAccount) m_actualAccount).setAmountToSave(Integer.valueOf(amountToSaveText.getText()));
				} else if (m_multipleAccounts != null && m_multipleAccounts.haveSavingsData()) {
					String debitAccountName = debitAccount.getSelectionModel().getSelectedItem();
					m_multipleAccounts.setAmountToSave(debitAccountName, multipleAccountAmountToSave);
				}
				updateAmountToSave();
			}
		});

		associatedEcoAccount.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				changeAssociatedEcoAccount();
			}
		});
		
		debitAccount.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				updateSavingsData();
			}
		});

		// configure the context menu in the saving transactions' table
		savingsTable.setRowFactory(lv -> {

			TableRow<SavingsTableManaging> cell = new TableRow<>();

			ContextMenu contextMenu = new ContextMenu();

			MenuItem editItem = new MenuItem();
			editItem.textProperty().bind(Bindings.format("Edit", cell.itemProperty()));
			editItem.setOnAction(event -> {
				// String item = cell.getItem();
				// code to edit item...
				editSaveTransaction(cell.getItem().getTransactId());
			});
			MenuItem deleteItem = new MenuItem();
			deleteItem.textProperty().bind(Bindings.format("Delete", cell.itemProperty()));
			deleteItem.setOnAction(event -> {
				deleteSaveTransaction(cell.getItem().getTransactId());
			});
			contextMenu.getItems().addAll(editItem, deleteItem);

			// cell.textProperty().bind(cell.itemProperty());

			cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
				if (isNowEmpty) {
					cell.setContextMenu(null);
				} else {
					cell.setContextMenu(contextMenu);
				}
			});
			return cell;
		});

		transactionsTable.setRowFactory(lv -> {

			TableRow<TransactionsTableManaging> cell = new TableRow<>();

			ContextMenu contextMenu = new ContextMenu();

			MenuItem editItem = new MenuItem();
			editItem.textProperty().bind(Bindings.format("Edit", cell.itemProperty()));
			editItem.setOnAction(event -> {
				// String item = cell.getItem();
				// code to edit item...
				editTransaction(cell.getItem().getId_transact());
			});
			MenuItem deleteItem = new MenuItem();
			deleteItem.textProperty().bind(Bindings.format("Delete", cell.itemProperty()));
			deleteItem.setOnAction(event -> {
				deleteSaveTransaction(cell.getItem().getId_transact());
			});
			contextMenu.getItems().addAll(editItem, deleteItem);

			// cell.textProperty().bind(cell.itemProperty());

			cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
				if (isNowEmpty) {
					cell.setContextMenu(null);
				} else {
					cell.setContextMenu(contextMenu);
				}
			});
			return cell;
		});

		multipleTransactionsTable.setRowFactory(lv -> {

			TableRow<MultipleTransactionsTableManaging> cell = new TableRow<>();

			ContextMenu contextMenu = new ContextMenu();

			MenuItem editItem = new MenuItem();
			editItem.textProperty().bind(Bindings.format("Edit", cell.itemProperty()));
			editItem.setOnAction(event -> {
				// String item = cell.getItem();
				// code to edit item...
				editTransaction(cell.getItem().getId_transact());
			});
			MenuItem deleteItem = new MenuItem();
			deleteItem.textProperty().bind(Bindings.format("Delete", cell.itemProperty()));
			deleteItem.setOnAction(event -> {
				deleteSaveTransaction(cell.getItem().getId_transact());
			});
			contextMenu.getItems().addAll(editItem, deleteItem);

			// cell.textProperty().bind(cell.itemProperty());

			cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
				if (isNowEmpty) {
					cell.setContextMenu(null);
				} else {
					cell.setContextMenu(contextMenu);
				}
			});
			return cell;
		});

		// CSS
		borderPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
		mainTable.getStyleClass().addAll(JMetroStyleClass.TABLE_GRID_LINES, JMetroStyleClass.ALTERNATING_ROW_COLORS,
				JMetroStyleClass.BACKGROUND);
		transactionsTable.getStyleClass().addAll(JMetroStyleClass.TABLE_GRID_LINES,
				JMetroStyleClass.ALTERNATING_ROW_COLORS, JMetroStyleClass.BACKGROUND);
		savingsTable.getStyleClass().addAll(JMetroStyleClass.TABLE_GRID_LINES, JMetroStyleClass.ALTERNATING_ROW_COLORS,
				JMetroStyleClass.BACKGROUND);
		multipleTransactionsTable.getStyleClass().addAll(JMetroStyleClass.TABLE_GRID_LINES,
				JMetroStyleClass.ALTERNATING_ROW_COLORS, JMetroStyleClass.BACKGROUND);

	}

	/**
	 * Update the main table every time a modification is required.
	 */
	public void updateMainTable() {
		mainTableRows.clear();
		if (m_actualAccount != null || m_multipleAccounts != null) {
			int rowToSelect = 0;
			Toggle showBy = showByGroup.getSelectedToggle();
			Collection<LocalDate> datesToDisplay = new ArrayList<LocalDate>();
			if (showBy == showByYear) {
				rowToSelect = m_mainTablePointingDate.getDayOfYear() - 1;
				mainTableGlobalDate.setText("Année " + m_mainTablePointingDate.getYear());
				if (m_actualAccount != null) {
					m_actualAccount.confPeriodProfit("y");
				}
				LocalDate dateToAdd = m_mainTablePointingDate.dayOfYear().withMinimumValue();
				LocalDate maxDate = m_mainTablePointingDate.dayOfYear().withMaximumValue();
				do {
					datesToDisplay.add(dateToAdd);
					dateToAdd = dateToAdd.plusDays(1);
				} while (dateToAdd.isBefore(maxDate.plusDays(1)));
			} else if (showBy == showByWeek) {
				rowToSelect = m_mainTablePointingDate.getDayOfWeek() - 1;
				String month = m_mainTablePointingDate.monthOfYear().getAsText();
				String monthUpper = month.substring(0, 1).toUpperCase() + month.substring(1);
				mainTableGlobalDate.setText("Semaine " + m_mainTablePointingDate.getWeekOfWeekyear() + " (" + monthUpper
						+ " " + m_mainTablePointingDate.getYear() + ")");
				if (m_actualAccount != null) {
					m_actualAccount.confPeriodProfit("w");
				}
				LocalDate dateToAdd = m_mainTablePointingDate.dayOfWeek().withMinimumValue();
				LocalDate maxDate = m_mainTablePointingDate.dayOfWeek().withMaximumValue();
				do {
					datesToDisplay.add(dateToAdd);
					dateToAdd = dateToAdd.plusDays(1);
				} while (dateToAdd.isBefore(maxDate.plusDays(1)));
			} else {
				// month is the default display method
				rowToSelect = m_mainTablePointingDate.getDayOfMonth() - 1;
				String month = m_mainTablePointingDate.monthOfYear().getAsText();
				mainTableGlobalDate.setText(month.substring(0, 1).toUpperCase() + month.substring(1) + " "
						+ m_mainTablePointingDate.getYear());
				if (m_actualAccount != null) {
					m_actualAccount.confPeriodProfit("m");
				}
				LocalDate dateToAdd = m_mainTablePointingDate.dayOfMonth().withMinimumValue();
				LocalDate maxDate = m_mainTablePointingDate.dayOfMonth().withMaximumValue();
				do {
					datesToDisplay.add(dateToAdd);
					dateToAdd = dateToAdd.plusDays(1);
				} while (dateToAdd.isBefore(maxDate.plusDays(1)));
			}

			if (m_actualAccount != null) {
				m_actualAccount.select(m_mainTablePointingDate, 0);
				HashMap<LocalDate, Double> Expenses = new HashMap<>();
				HashMap<LocalDate, Double> Gains = new HashMap<>();
				HashMap<LocalDate, Double> Profits = new HashMap<>();

				if (showBy == showByYear) {
					Expenses = m_actualAccount.obtainYearExpenses();
					Gains = m_actualAccount.obtainYearGains();
					Profits = m_actualAccount.obtainYearProfits();
				} else {
					m_actualAccount.updateMonth();
					Expenses = m_actualAccount.getMonthExepenses();
					Gains = m_actualAccount.getMonthGains();
					Profits = m_actualAccount.getMonthProfits();
				}
				for (LocalDate eachDate : Expenses.keySet()) {
					Double roundedValue = MoneyTypes.round(Expenses.get(eachDate), 2);
					Expenses.put(eachDate, roundedValue);
				}
				for (LocalDate eachDate : Gains.keySet()) {
					Double roundedValue = MoneyTypes.round(Gains.get(eachDate), 2);
					Gains.put(eachDate, roundedValue);
				}
				for (LocalDate eachDate : Profits.keySet()) {
					Double roundedValue = MoneyTypes.round(Profits.get(eachDate), 2);
					Profits.put(eachDate, roundedValue);
				}

				for (LocalDate date : datesToDisplay) {
					mainTableRows.add(new MainTableManaging(date, Expenses.getOrDefault(date, null),
							Gains.getOrDefault(date, null), Profits.getOrDefault(date, null)));
				}
			} else if (m_multipleAccounts != null) {
				HashMap<LocalDate, DoubleDoubleDoubleList> transactionsList = new HashMap<>();
				LocalDate begDate = m_mainTablePointingDate.dayOfMonth().withMinimumValue();
				LocalDate endDate = m_mainTablePointingDate.dayOfMonth().withMaximumValue();
				if (showBy == showByYear) {
					begDate = m_mainTablePointingDate.dayOfYear().withMinimumValue();
					endDate = m_mainTablePointingDate.dayOfYear().withMaximumValue();
				}
				transactionsList = m_multipleAccounts.transactionsList(begDate, endDate, 1);

				DoubleDoubleDoubleList defaultList = new DoubleDoubleDoubleList(0., 0., 0.);
				DoubleDoubleDoubleList dateTransaction;
				for (LocalDate date : datesToDisplay) {
					dateTransaction = transactionsList.getOrDefault(date, defaultList);
					mainTableRows.add(new MainTableManaging(date, dateTransaction.getFirst(),
							dateTransaction.getSecond(), dateTransaction.getThird()));
				}

			}
			mainTable.getSelectionModel().select(rowToSelect);
		}
		updateTransactionsTable();
		updateMonthResults();
		updateSavingsData();
	}

	public void updateTransactionsTable() {
		transactionsTableRows.clear();
		multipleTransactionsTableRows.clear();
		if (m_profileName.length() != 0 && m_actualAccount != null) {
			transactionsTable.setVisible(true);
			multipleTransactionsTable.setVisible(false);
			transactionsTable.toFront();
			m_actualAccount.select(m_mainTablePointingDate, 0);
			selectedDateDay.setText("Date : " + m_mainTablePointingDate.dayOfWeek().getAsText() + " "
					+ m_mainTablePointingDate.getDayOfMonth() + " " + m_mainTablePointingDate.monthOfYear().getAsText()
					+ " " + m_mainTablePointingDate.getYear());
			HashMap<Integer, DoubleStringIntList> transactList = new HashMap<>();
			transactList = m_actualAccount.listTransact(m_mainTablePointingDate);
			if (m_actualAccountType == 2) {
				transactList.putAll(m_actualAccount.listEcoTransactSingleDay(m_mainTablePointingDate));
			}
			DoubleStringIntList value;
			for (int id_transact : transactList.keySet()) {
				value = transactList.get(id_transact);
				transactionsTableRows.add(
						new TransactionsTableManaging(id_transact, value.getFirstElement(), value.getSecondElement()));
			}
		} else if (m_profileName.length() != 0 && m_multipleAccounts != null) {
			transactionsTable.setVisible(false);
			multipleTransactionsTable.setVisible(true);
			multipleTransactionsTable.toFront();
			selectedDateDay.setText("Date : " + m_mainTablePointingDate.dayOfWeek().getAsText() + " "
					+ m_mainTablePointingDate.getDayOfMonth() + " " + m_mainTablePointingDate.monthOfYear().getAsText()
					+ " " + m_mainTablePointingDate.getYear());
			HashMap<Integer, DoubleStringIntList> transactList = new HashMap<>();
			if (m_multipleAccounts.haveSavingsData()) {
				transactList = m_multipleAccounts.detailedTransactions(m_mainTablePointingDate, 1);
			} else {
				transactList = m_multipleAccounts.detailedTransactions(m_mainTablePointingDate, -1);
			}
			DoubleStringIntList value;
			for (int id_transact : transactList.keySet()) {
				value = transactList.get(id_transact);
				multipleTransactionsTableRows.add(new MultipleTransactionsTableManaging(id_transact,
						value.getFirstElement(), value.getSecondElement(), value.getThirdElement(), m_profileName));
			}
		} else {
			selectedDateDay.setText("Date : none");
			TotalMoneyText.setText("Actual amount of money in the account : none");
			monthGainText.setText("Profit of (month) : none");
			cumulatedGainText.setText("Cumulative profit : none");
			cumulatedGainDateText.setText("Starting date for the cumulative profit :");
			cumulatedGainDatePicker.getEditor().clear();
		}
	}

	public void updateSavingsTable() {
		savingsTableRows.clear();
		HashMap<Integer, DateIntStringList> transactList = new HashMap<Integer, DateIntStringList>();
		if (m_actualAccount != null) {
			transactList = ((CurrentAccount) m_actualAccount).listEcoTransact();
		} else {
			transactList = m_multipleAccounts.listEcoTransact(m_mainTablePointingDate);
		}
		DateIntStringList value;
		for (int id_transact : transactList.keySet()) {
			value = transactList.get(id_transact);
			savingsTableRows.add(
					new SavingsTableManaging(value.getM_date(), value.getM_string(), value.getM_int(), id_transact));
		}
	}

	public void updateSavingsData() {
		Toggle showBy = showByGroup.getSelectedToggle();
		if (m_profileName.length() != 0 && showBy == showByMonth && m_actualAccount != null && m_actualAccount.getAccountType() == 1) {
			((CurrentAccount) m_actualAccount).changeMonth();
			boxForCurrentAccounts.setVisible(true);
			boxForCurrentAccounts.setDisable(false);
			SavingMonthText.setText("Savings data of " + m_mainTablePointingDate.monthOfYear().getAsText() + " :");
			manualSaving.setSelected(((CurrentAccount) m_actualAccount).getManualSave());
			amountMoneyTypeText.setText(m_actualAccount.getMoneyName());
			economyRateText.setText(String.valueOf(((CurrentAccount) m_actualAccount).getConfSave()));

			if (manualSaving.isSelected()) {
				economyRateText.setDisable(true);
				amountToSaveText.setEditable(true);
				amountToSaveText.setText(String.valueOf(((CurrentAccount) m_actualAccount).getAmountToSave()));
			} else {
				economyRateText.setDisable(false);
				amountToSaveText.setEditable(false);
				calculateAmountToSave();
			}

			monthProfitText.setText("Profit of " + m_mainTablePointingDate.monthOfYear().getAsText() + " : "
					+ MoneyTypes.round(m_actualAccount.obtainThisMonthRawProfit(m_mainTablePointingDate), 2) + " "
					+ m_actualAccount.getMoneyName());
			updateAmountToSave();

			advancedMoneyText
					.setText("Total advanced money : " + ((CurrentAccount) m_actualAccount).getTotalAdvancedSaving()
							+ " " + m_actualAccount.getMoneyName());
			updateSavingsTable();
			updateAssociatedEcoAccountsList();

			lossesText.setText("Losses : " + ((CurrentAccount) m_actualAccount).getLosses() + " "
					+ m_actualAccount.getMoneyName());

		} else if  (m_profileName.length() != 0 && showBy == showByMonth && m_multipleAccounts != null && m_multipleAccounts.haveSavingsData()) {
			String debitAccountName = debitAccount.getSelectionModel().getSelectedItem();
			m_multipleAccounts.changeMonth(debitAccountName, m_mainTablePointingDate);
			boxForCurrentAccounts.setVisible(true);
			boxForCurrentAccounts.setDisable(false);
			SavingMonthText.setText("Savings data of " + m_mainTablePointingDate.monthOfYear().getAsText() + " :");
			manualSaving.setSelected(m_multipleAccounts.getManualSave(debitAccountName));
			amountMoneyTypeText.setText(m_multipleAccounts.getMoneyName(debitAccountName));
			economyRateText.setText(String.valueOf(m_multipleAccounts.getConfSave(debitAccountName)));

			if (manualSaving.isSelected()) {
				economyRateText.setDisable(true);
				amountToSaveText.setEditable(true);
				int amountToSave = 0;
				for (int amount : m_multipleAccounts.getAmountToSave(debitAccountName).values()) {
					amountToSave += amount;
				}
				multipleAccountAmountToSave = m_multipleAccounts.getAmountToSave(debitAccountName).get(debitAccountName);
				amountToSaveText.setText(String.valueOf(amountToSave));
			} else {
				economyRateText.setDisable(false);
				amountToSaveText.setEditable(false);
				calculateAmountToSave();
			}

			monthProfitText.setText("Profit of " + m_mainTablePointingDate.monthOfYear().getAsText() + " : "
					+ MoneyTypes.round(m_multipleAccounts.obtainThisMonthRawProfit(m_mainTablePointingDate), 2) + " "
					+ m_multipleAccounts.getMoneyName(debitAccountName));
			updateAmountToSave();

			advancedMoneyText
					.setText("Total advanced money : " + m_multipleAccounts.getTotalAdvancedSaving(debitAccountName)
							+ " " + m_multipleAccounts.getMoneyName(debitAccountName));
			updateSavingsTable();
			updateAssociatedEcoAccountsList();

			lossesText.setText("Losses : " + (m_multipleAccounts).getLosses(debitAccountName) + " "
					+ m_multipleAccounts.getMoneyName(debitAccountName));
		} else {
			boxForCurrentAccounts.setVisible(false);
			boxForCurrentAccounts.setDisable(true);
		}
	}

	public void advanceEco() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/AdvanceEco.fxml"));
			Parent root = loader.load();

			AdvanceEcoController advanceEcoController = loader.getController();
			if (m_actualAccount != null) {
				advanceEcoController.initData(m_actualAccount);
			} else {
				String debitAccountName = debitAccount.getSelectionModel().getSelectedItem();
				advanceEcoController.initData(new CurrentAccount(m_profileName, debitAccountName));
			}

			Stage stage = new Stage();
			stage.setTitle("Advance the money to save");
			Scene scene = new Scene(root);
			JMetro jMetro = new JMetro(Style.DARK);
			jMetro.setScene(scene);

			scene.getStylesheets().add("gui/managing/application.css");
			stage.setScene(scene);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setResizable(false);
			stage.showAndWait();
			if (m_actualAccount != null) {
				advancedMoneyText
						.setText("Total advanced money : " + ((CurrentAccount) m_actualAccount).getTotalAdvancedSaving()
								+ " " + m_actualAccount.getMoneyName());
			} else {
				String debitAccountName = debitAccount.getSelectionModel().getSelectedItem();
				advancedMoneyText
						.setText("Total advanced money : " + m_multipleAccounts.getTotalAdvancedSaving(debitAccountName)
								+ " " + m_multipleAccounts.getMoneyName(debitAccountName));
			}
			updateSavingsTable();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addLosses() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/AddLosses.fxml"));
			Parent root = loader.load();

			AddLossesController addLossesController = loader.getController();
			if (m_actualAccount != null) {
				addLossesController.initData(m_actualAccount);
			} else {
				String debitAccountName = debitAccount.getSelectionModel().getSelectedItem();
				addLossesController.initData(new CurrentAccount(m_profileName, debitAccountName));
			}

			Stage stage = new Stage();
			stage.setTitle("Add a lost amount of money");
			Scene scene = new Scene(root);
			JMetro jMetro = new JMetro(Style.DARK);
			jMetro.setScene(scene);

			scene.getStylesheets().add("gui/managing/application.css");
			stage.setScene(scene);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setResizable(false);
			stage.showAndWait();
			if (m_actualAccount != null) {
				lossesText.setText("Losses : " + ((CurrentAccount) m_actualAccount).getLosses() + " "
						+ m_actualAccount.getMoneyName());
			} else {
				String debitAccountName = debitAccount.getSelectionModel().getSelectedItem();
				lossesText.setText("Losses : " + (m_multipleAccounts.getLosses(debitAccountName) + " "
						+ m_multipleAccounts.getMoneyName(debitAccountName)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateAmountToSave() {
		if (m_actualAccount != null) {
			Double profitAfterSaving = m_actualAccount.obtainThisMonthRawProfit(m_mainTablePointingDate)
					- Integer.valueOf(amountToSaveText.getText());
			profitAfterSavingText.setText("Profit after saving : " + MoneyTypes.round(profitAfterSaving, 2) + " "
					+ m_actualAccount.getMoneyName());
		} else {
			String debitAccountName = debitAccount.getSelectionModel().getSelectedItem();
			Double profitAfterSaving = m_multipleAccounts.obtainThisMonthRawProfit(m_mainTablePointingDate)
					- Integer.valueOf(amountToSaveText.getText());
			profitAfterSavingText.setText("Profit after saving : " + MoneyTypes.round(profitAfterSaving, 2) + " "
					+ m_multipleAccounts.getMoneyName(debitAccountName));
		}
	}

	public void updateAssociatedEcoAccountsList() {
		associatedEcoAccount.getItems().clear();
		if (m_accountName.length() > 0 && m_profileName.length() > 0 && m_actualAccountType == 1) {
			associatedEcoAccount.getItems().addAll(MoneyTypes.getAccountsListFromMoneyId(m_profileName,
					MoneyTypes.getMoneyIdFromAccount(m_profileName, m_accountName), 2).values());
			associatedEcoAccount.getSelectionModel().select(
					DbConnection.getAccountName(m_profileName, ((CurrentAccount) m_actualAccount).getIdEcoAccount()));
		} else if (m_multipleAccounts != null && m_multipleAccounts.haveSavingsData()) {
			String debitAccountName = debitAccount.getSelectionModel().getSelectedItem();
			associatedEcoAccount.getItems().addAll(MoneyTypes.getAccountsListFromMoneyId(m_profileName,
					MoneyTypes.getMoneyIdFromAccount(m_profileName, debitAccountName), 2).values());
			associatedEcoAccount.getSelectionModel().select(
					DbConnection.getAccountName(m_profileName, m_multipleAccounts.getIdEcoAccount(debitAccountName)));
		}
	}

	public void changeAssociatedEcoAccount() {
		if (m_accountName.length() > 0 && m_profileName.length() > 0 && m_actualAccountType == 1) {
			int ecoAccountId = DbConnection.getAccountId(m_profileName,
					associatedEcoAccount.getSelectionModel().getSelectedItem());
			((CurrentAccount) m_actualAccount).setIdEcoAccount(ecoAccountId);
		} else if (m_multipleAccounts != null && m_multipleAccounts.haveSavingsData()) {
			String debitAccountName = debitAccount.getSelectionModel().getSelectedItem();
			int ecoAccountId = DbConnection.getAccountId(m_profileName,
					associatedEcoAccount.getSelectionModel().getSelectedItem());
			m_multipleAccounts.setIdEcoAccount(debitAccountName, ecoAccountId);
		}
	}

	@FXML
	public void setManualSaving() {
		if (m_accountName.length() > 0 && m_profileName.length() > 0 && m_actualAccount != null && m_actualAccountType == 1) {
			if (manualSaving.isSelected()) {
				economyRateText.setDisable(true);
				amountToSaveText.setEditable(true);
				((CurrentAccount) m_actualAccount).confManualSave(true);
			} else {
				economyRateText.setDisable(false);
				amountToSaveText.setEditable(false);
				((CurrentAccount) m_actualAccount).confManualSave(false);
				calculateAmountToSave();
			}
		} else if (m_multipleAccounts != null && m_multipleAccounts.haveSavingsData()) {
			String debitAccountName = debitAccount.getSelectionModel().getSelectedItem();
			if (manualSaving.isSelected()) {
				economyRateText.setDisable(true);
				amountToSaveText.setEditable(true);
				m_multipleAccounts.confManualSave(debitAccountName, true);
			} else {
				economyRateText.setDisable(false);
				amountToSaveText.setEditable(false);
				m_multipleAccounts.confManualSave(debitAccountName, false);
				calculateAmountToSave();
			}
		}
	}

	public void calculateAmountToSave() {
		if (m_actualAccount != null) {
			((CurrentAccount) m_actualAccount).modifyConfSave(Integer.valueOf(economyRateText.getText()));
			((CurrentAccount) m_actualAccount).calculateAmountToSave(true);
			amountToSaveText.setText(String.valueOf(((CurrentAccount) m_actualAccount).getAmountToSave()));
		} else {
			String debitAccountName = debitAccount.getSelectionModel().getSelectedItem();
			m_multipleAccounts.modifyConfSave(debitAccountName, Integer.valueOf(economyRateText.getText()));
			m_multipleAccounts.calculateAmountToSave(debitAccountName, true);int amountToSave = 0;
			for (int amount : m_multipleAccounts.getAmountToSave(debitAccountName).values()) {
				amountToSave += amount;
			}
			multipleAccountAmountToSave = m_multipleAccounts.getAmountToSave(debitAccountName).get(debitAccountName);
			amountToSaveText.setText(String.valueOf(amountToSave));
		}
	}

	/**
	 * Creates menuItems corresponding to the list of the existing profile. This
	 * method need to be called every time the list of existing profiles in the
	 * database is updated.
	 */
	public void updateMenuProfileSelect() {
		subMenuProfileSelect.getItems().clear();
		List<String> profileList = new ArrayList<String>();
		profileList = Profile.showProfileList();
		for (String profileName : profileList) {
			if (profileName.length() != 0) {
				subMenuProfileSelect.getItems().add(new MenuItem(profileName));
				subMenuProfileSelect.getItems().get(subMenuProfileSelect.getItems().size() - 1)
						.setOnAction(new EventHandler<ActionEvent>() {
							String profileName = subMenuProfileSelect.getItems()
									.get(subMenuProfileSelect.getItems().size() - 1).getText();

							public void handle(ActionEvent e) {
								actualProfile.setText("Profile : " + profileName);
								actualAccountField.setText("Account : not selected");
								m_profileName = profileName;
								m_actualAccountType = -1;
								m_actualAccount = null;
								m_multipleAccounts = null;
								m_accountName = "";
								updateMenuAccountSelect();
								updateMainTable();
								updateAccountInfos();
							}
						});
			}
		}

	}

	public void updateMenuAccountSelect() {
		subMenuAccountsCurrent.getItems().clear();
		subMenuAccountsEconomy.getItems().clear();
		Collection<String> currentAccounts = new ArrayList<String>();
		Collection<String> economyAccounts = new ArrayList<String>();
		if (m_profileName.length() != 0) {
			currentAccounts = DbConnection.listAccounts(m_profileName, -1, 1).values();
			economyAccounts = DbConnection.listAccounts(m_profileName, -1, 2).values();

			String previousMoneyName = "";
			String nextMoneyName = "";
			Text moneyNameText = new Text();
			SeparatorMenuItem separWithText = new SeparatorMenuItem();

			for (String currentAccount : currentAccounts) {
				nextMoneyName = MoneyTypes.getMoneyNameFromAccount(m_profileName, currentAccount);
				if (!nextMoneyName.equals(previousMoneyName)) {
					if (previousMoneyName.length() > 0) {
						subMenuAccountsCurrent.getItems().add(new SeparatorMenuItem());
					}
					separWithText = new SeparatorMenuItem();
					moneyNameText = new Text("      " + nextMoneyName);
					separWithText.setContent(moneyNameText);
					subMenuAccountsCurrent.getItems().add(separWithText);
				}
				previousMoneyName = nextMoneyName;

				subMenuAccountsCurrent.getItems().add(new MenuItem(currentAccount));
				subMenuAccountsCurrent.getItems().get(subMenuAccountsCurrent.getItems().size() - 1)
						.setOnAction(new EventHandler<ActionEvent>() {
							String currentAccount = subMenuAccountsCurrent.getItems()
									.get(subMenuAccountsCurrent.getItems().size() - 1).getText();

							public void handle(ActionEvent e) {
								actualAccountField.setText("Account : " + currentAccount + " (Current account)");
								m_accountName = currentAccount;
								m_actualAccountType = 1;
								m_actualAccount = null;
								m_multipleAccounts = null;
								m_actualAccount = new CurrentAccount(m_profileName, m_accountName);

								LocalDate jodaDate = m_actualAccount.getPeriodProfitStart();
								int year = jodaDate.getYear();
								int month = jodaDate.getMonthOfYear();
								int day = jodaDate.getDayOfMonth();
								cumulatedGainDatePicker.setValue(java.time.LocalDate.of(year, month, day));

								debitAccount.getItems().clear();
								debitAccount.getItems().add(m_accountName);
								debitAccount.getSelectionModel().selectFirst();

								updateMainTable();
								updateAccountInfos();
								updateAssociatedEcoAccountsList();
							}
						});
			}

			previousMoneyName = "";
			nextMoneyName = "";

			for (String economyAccount : economyAccounts) {
				nextMoneyName = MoneyTypes.getMoneyNameFromAccount(m_profileName, economyAccount);
				if (!nextMoneyName.equals(previousMoneyName)) {
					if (previousMoneyName.length() > 0) {
						subMenuAccountsEconomy.getItems().add(new SeparatorMenuItem());
					}
					separWithText = new SeparatorMenuItem();
					moneyNameText = new Text("      " + nextMoneyName);
					separWithText.setContent(moneyNameText);
					subMenuAccountsEconomy.getItems().add(separWithText);
				}
				previousMoneyName = nextMoneyName;

				subMenuAccountsEconomy.getItems().add(new MenuItem(economyAccount));
				subMenuAccountsEconomy.getItems().get(subMenuAccountsEconomy.getItems().size() - 1)
						.setOnAction(new EventHandler<ActionEvent>() {
							String economyAccount = subMenuAccountsEconomy.getItems()
									.get(subMenuAccountsEconomy.getItems().size() - 1).getText();

							public void handle(ActionEvent e) {
								actualAccountField.setText("Account : " + economyAccount + " (Economy account)");
								m_accountName = economyAccount;
								m_actualAccountType = 2;
								m_actualAccount = null;
								m_multipleAccounts = null;
								m_actualAccount = new EconomyAccount(m_profileName, m_accountName);

								LocalDate jodaDate = m_actualAccount.getPeriodProfitStart();
								int year = jodaDate.getYear();
								int month = jodaDate.getMonthOfYear();
								int day = jodaDate.getDayOfMonth();
								cumulatedGainDatePicker.setValue(java.time.LocalDate.of(year, month, day));

								updateMainTable();
								updateAccountInfos();
							}
						});
			}

		}
	}

	/**
	 * Open a pop up window where we can create a new profile
	 * 
	 * @param event Click on the "New" menuItem in the menu bar
	 */
	public void goToNewProfile(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/NewProfile.fxml"));
			Parent root = loader.load();

			/*
			 * NewProfileController newProfileController = loader.getController();
			 */

			Stage stage = new Stage();
			stage.setTitle("Create a new profile");
			Scene scene = new Scene(root);
			JMetro jMetro = new JMetro(Style.DARK);
			jMetro.setScene(scene);

			scene.getStylesheets().add("gui/managing/application.css");
			stage.setScene(scene);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setResizable(false);
			stage.showAndWait();
			updateMenuProfileSelect();

			/*
			 * //Collect data from the pop up window and use them to create a new profile
			 * String newProfileName = newProfileController.getNewProfileName(); if
			 * (newProfileName.siz) Profile.createProfile(newProfileName);
			 */

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open a pop up window where we can delete a profile
	 * 
	 * @param event Click on the "Delete" menuItem in the menu bar
	 */
	public void goToDeleteProfile(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/DeleteProfile.fxml"));
			Parent root = loader.load();

			Stage stage = new Stage();
			stage.setTitle("Delete a profile");
			Scene scene = new Scene(root);
			JMetro jMetro = new JMetro(Style.DARK);
			jMetro.setScene(scene);

			scene.getStylesheets().add("gui/managing/application.css");
			stage.setScene(scene);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setResizable(false);
			stage.showAndWait();
			updateMenuProfileSelect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void goToDeleteAccount(ActionEvent event) {
		if (m_profileName.length() != 0) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/DeleteAccount.fxml"));
				Parent root = loader.load();

				DeleteAccountController deleteAccountController = loader.getController();
				deleteAccountController.initData(m_profileName);

				Stage stage = new Stage();
				stage.setTitle("Delete an account");
				Scene scene = new Scene(root);
				JMetro jMetro = new JMetro(Style.DARK);
				jMetro.setScene(scene);

				scene.getStylesheets().add("gui/managing/application.css");
				stage.setScene(scene);
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setResizable(false);
				stage.showAndWait();
				updateMenuAccountSelect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void goToNewAccount(ActionEvent event) {
		if (m_profileName.length() != 0) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/NewAccount.fxml"));
				Parent root = loader.load();

				NewAccountController newAccountController = loader.getController();
				newAccountController.initData(m_profileName);

				Stage stage = new Stage();
				stage.setTitle("Create a new account");
				Scene scene = new Scene(root);
				JMetro jMetro = new JMetro(Style.DARK);
				jMetro.setScene(scene);

				scene.getStylesheets().add("gui/managing/application.css");
				stage.setScene(scene);
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setResizable(false);
				stage.showAndWait();
				updateMenuAccountSelect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void goToMoneyTypes(ActionEvent event) {
		if (m_profileName.length() != 0) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/MoneyTypes.fxml"));
				Parent root = loader.load();

				MoneyTypesController moneyTypesController = loader.getController();
				moneyTypesController.initData(m_profileName);

				Stage stage = new Stage();
				stage.setTitle("List of the money types");
				Scene scene = new Scene(root);
				JMetro jMetro = new JMetro(Style.DARK);
				jMetro.setScene(scene);

				scene.getStylesheets().add("gui/managing/application.css");
				stage.setScene(scene);
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setResizable(false);
				stage.showAndWait();
				updateMenuAccountSelect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void changeMoneyType(ActionEvent event) {
		if (m_profileName.length() != 0 && m_actualAccount != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/ChangeMoneyType.fxml"));
				Parent root = loader.load();

				ChangeMoneyTypeController controller = loader.getController();
				controller.initData(m_profileName, m_actualAccount);

				Stage stage = new Stage();
				stage.setTitle("Change money type");
				Scene scene = new Scene(root);
				JMetro jMetro = new JMetro(Style.DARK);
				jMetro.setScene(scene);

				scene.getStylesheets().add("gui/managing/application.css");
				stage.setScene(scene);
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setResizable(false);
				stage.showAndWait();
				updateMenuAccountSelect();
				updateAccountInfos();
				updateSavingsData();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void changeAccountSource(ActionEvent event) {
		if (m_profileName.length() != 0 && m_actualAccount != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/ChangeAccountSource.fxml"));
				Parent root = loader.load();

				ChangeAccountSourceController controller = loader.getController();
				controller.initData(m_actualAccount, m_actualAccountSource);

				Stage stage = new Stage();
				stage.setTitle("Change account source");
				Scene scene = new Scene(root);
				JMetro jMetro = new JMetro(Style.DARK);
				jMetro.setScene(scene);

				scene.getStylesheets().add("gui/managing/application.css");
				stage.setScene(scene);
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setResizable(false);
				stage.showAndWait();
				updateMenuAccountSelect();
				updateAccountInfos();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void updateAccountInfos() {
		if (m_accountName.length() != 0) {
			m_actualAccountMoney = m_actualAccount.getMoneyName();
			m_actualAccountSource = m_actualAccount.getAccountSource();
			actualAccountMoneyType.setText("Money type : " + m_actualAccountMoney);
			actualAccountSource.setText("Source : " + m_actualAccountSource);

		} else {
			m_actualAccountMoney = "none";
			m_actualAccountSource = "none";
			actualAccountMoneyType.setText("Money type : " + m_actualAccountMoney);
			actualAccountSource.setText("Source : " + m_actualAccountSource);
			mainTableGlobalDate.setText("Select an account");
		}
	}

	public void nextDate() {
		Toggle showBy = showByGroup.getSelectedToggle();
		if (showBy == showByYear) {
			m_mainTablePointingDate = m_mainTablePointingDate.plusYears(1);
		}
		if (showBy == showByMonth) {
			m_mainTablePointingDate = m_mainTablePointingDate.plusMonths(1);
		}
		if (showBy == showByWeek) {
			m_mainTablePointingDate = m_mainTablePointingDate.plusWeeks(1);
		}
		updateMainTable();
	}

	public void previousDate() {
		Toggle showBy = showByGroup.getSelectedToggle();
		if (showBy == showByYear) {
			m_mainTablePointingDate = m_mainTablePointingDate.minusYears(1);
		}
		if (showBy == showByMonth) {
			m_mainTablePointingDate = m_mainTablePointingDate.minusMonths(1);
		}
		if (showBy == showByWeek) {
			m_mainTablePointingDate = m_mainTablePointingDate.minusWeeks(1);
		}
		updateMainTable();
	}

	public void updateSelectedDate() {
		m_mainTablePointingDate = mainTable.getSelectionModel().getSelectedItem().getRowDate();
		updateTransactionsTable();
		updateMonthResults();

	}

	public void editTransaction(int idTransaction) {
		if (m_profileName.length() != 0 && (!transactionsTable.getSelectionModel().isEmpty()
				|| !multipleTransactionsTable.getSelectionModel().isEmpty())) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/EditTransaction.fxml"));
				Parent root = loader.load();

				EditTransactionController controller = loader.getController();
				controller.initData(m_profileName, idTransaction);

				Stage stage = new Stage();
				stage.setTitle("Edit the transaction");
				Scene scene = new Scene(root);
				JMetro jMetro = new JMetro(Style.DARK);
				jMetro.setScene(scene);

				scene.getStylesheets().add("gui/managing/application.css");
				stage.setScene(scene);
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setResizable(false);
				stage.showAndWait();
				updateMainTable();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void doATransaction() {
		if (m_profileName.length() != 0 && m_actualAccount != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/Transaction.fxml"));
				Parent root = loader.load();

				TransactionController controller = loader.getController();
				controller.initData(m_profileName, m_accountName, m_actualAccount);

				Stage stage = new Stage();
				stage.setTitle("Do a transaction");
				Scene scene = new Scene(root);
				JMetro jMetro = new JMetro(Style.DARK);
				jMetro.setScene(scene);

				scene.getStylesheets().add("gui/managing/application.css");
				stage.setScene(scene);
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setResizable(false);
				stage.showAndWait();
				updateMainTable();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (m_profileName.length() != 0 && m_multipleAccounts != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/MultipleTransaction.fxml"));
				Parent root = loader.load();

				MultipleTransactionController controller = loader.getController();
				controller.initData(m_profileName, m_mainTablePointingDate, m_multipleAccounts);

				Stage stage = new Stage();
				stage.setTitle("Do a transaction");
				Scene scene = new Scene(root);
				JMetro jMetro = new JMetro(Style.DARK);
				jMetro.setScene(scene);

				scene.getStylesheets().add("gui/managing/application.css");
				stage.setScene(scene);
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setResizable(false);
				stage.showAndWait();
				updateMainTable();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void spend() {
		if (m_profileName.length() != 0 && m_actualAccount != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/Spend.fxml"));
				Parent root = loader.load();

				SpendController controller = loader.getController();
				controller.initData(m_actualAccount);

				Stage stage = new Stage();
				stage.setTitle("Spend money");
				Scene scene = new Scene(root);
				JMetro jMetro = new JMetro(Style.DARK);
				jMetro.setScene(scene);

				scene.getStylesheets().add("gui/managing/application.css");
				stage.setScene(scene);
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setResizable(false);
				stage.showAndWait();
				updateMainTable();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (m_profileName.length() != 0 && m_multipleAccounts != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/MultipleSpend.fxml"));
				Parent root = loader.load();

				MultipleSpendController controller = loader.getController();
				controller.initData(m_profileName, m_mainTablePointingDate, m_multipleAccounts);

				Stage stage = new Stage();
				stage.setTitle("Spend money");
				Scene scene = new Scene(root);
				JMetro jMetro = new JMetro(Style.DARK);
				jMetro.setScene(scene);

				scene.getStylesheets().add("gui/managing/application.css");
				stage.setScene(scene);
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setResizable(false);
				stage.showAndWait();
				updateMainTable();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void obtain() {
		if (m_profileName.length() != 0 && m_actualAccount != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/Obtain.fxml"));
				Parent root = loader.load();

				ObtainController controller = loader.getController();
				controller.initData(m_actualAccount);

				Stage stage = new Stage();
				stage.setTitle("Obtain money");
				Scene scene = new Scene(root);
				JMetro jMetro = new JMetro(Style.DARK);
				jMetro.setScene(scene);

				scene.getStylesheets().add("gui/managing/application.css");
				stage.setScene(scene);
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setResizable(false);
				stage.showAndWait();
				updateMainTable();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (m_profileName.length() != 0 && m_multipleAccounts != null) {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/MultipleObtain.fxml"));
				Parent root = loader.load();

				MultipleObtainController controller = loader.getController();
				controller.initData(m_profileName, m_mainTablePointingDate, m_multipleAccounts);

				Stage stage = new Stage();
				stage.setTitle("Obtain money");
				Scene scene = new Scene(root);
				JMetro jMetro = new JMetro(Style.DARK);
				jMetro.setScene(scene);

				scene.getStylesheets().add("gui/managing/application.css");
				stage.setScene(scene);
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setResizable(false);
				stage.showAndWait();
				updateMainTable();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Update the Box with every profits' informations about the selected month
	 */
	public void updateMonthResults() {
		if (m_actualAccount != null) {
			TotalMoneyText.setText("Actual amount of money in the account : "
					+ MoneyTypes.round(m_actualAccount.obtainActualTotalAmountOfMoney(m_mainTablePointingDate), 2));
			monthGainText.setText("Profit of " + m_mainTablePointingDate.monthOfYear().getAsText() + " : "
					+ MoneyTypes.round(m_actualAccount.obtainThisMonthRawProfit(m_mainTablePointingDate), 2));
			cumulatedGainText.setText("Cumulative profit : "
					+ MoneyTypes.round(m_actualAccount.obtainCumulatedProfit(m_mainTablePointingDate), 2));
		} else if (m_multipleAccounts != null) {
			TotalMoneyText.setText("Actual amount of money in these accounts : "
					+ MoneyTypes.round(m_multipleAccounts.obtainActualTotalAmountOfMoney(m_mainTablePointingDate), 2));
			monthGainText.setText("Profit of " + m_mainTablePointingDate.monthOfYear().getAsText() + " : "
					+ MoneyTypes.round(m_multipleAccounts.obtainThisMonthRawProfit(m_mainTablePointingDate), 2));
			if (cumulatedGainDatePicker.getValue() == null) {
				int year = m_mainTablePointingDate.getYear();
				int month = m_mainTablePointingDate.getMonthOfYear();
				int day = m_mainTablePointingDate.getDayOfMonth();
				cumulatedGainDatePicker.setValue(java.time.LocalDate.of(year, month, day));
			}
			cumulatedGainText
					.setText("Cumulative profit : " + MoneyTypes.round(m_multipleAccounts.obtainCumulatedProfit(
							new LocalDate(cumulatedGainDatePicker.getValue().toString()), m_mainTablePointingDate), 2));
		}
	}

	@FXML
	public void changeCumulatedGainDate() {
		if (m_actualAccount != null && m_actualAccountType == 1) {
			m_actualAccount.confCumulProfit(new LocalDate(cumulatedGainDatePicker.getValue().toString()));
		}
		updateMonthResults();
	}

	public void editSaveTransaction(int id_transaction) {
		try {
			if (m_actualAccount != null) {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/EditSaveTransaction.fxml"));
				Parent root = loader.load();

				EditSaveTransactionController controller = loader.getController();
				controller.initData(id_transaction, m_profileName, m_actualAccount);

				Stage stage = new Stage();
				stage.setTitle("Edit the transaction");
				Scene scene = new Scene(root);
				JMetro jMetro = new JMetro(Style.DARK);
				jMetro.setScene(scene);

				scene.getStylesheets().add("gui/managing/application.css");
				stage.setScene(scene);
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setResizable(false);
				stage.showAndWait();
				updateSavingsData();
			} else {
				FXMLLoader loader = new FXMLLoader(
						getClass().getResource("/gui/managing/EditMultipleSaveTransaction.fxml"));
				Parent root = loader.load();

				EditMultipleSaveTransactionController controller = loader.getController();
				controller.initData(id_transaction, m_profileName, m_selectedAccounts);

				Stage stage = new Stage();
				stage.setTitle("Edit the transaction");
				Scene scene = new Scene(root);
				JMetro jMetro = new JMetro(Style.DARK);
				jMetro.setScene(scene);

				scene.getStylesheets().add("gui/managing/application.css");
				stage.setScene(scene);
				stage.initModality(Modality.APPLICATION_MODAL);
				stage.setResizable(false);
				stage.showAndWait();
				updateSavingsData();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void deleteSaveTransaction(int id_transaction) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/DeleteSaveTransaction.fxml"));
			Parent root = loader.load();

			DeleteSaveTransactionController controller = loader.getController();
			if (m_actualAccount != null) {
				controller.initData(id_transaction, m_actualAccount);
			} else if (m_multipleAccounts != null) {
				DetailedTransaction detailedTransaction = new DetailedTransaction(id_transaction, m_profileName);
				String accountName = DbConnection.getAccountName(m_profileName, detailedTransaction.getId_account());
				Account account = new Account(m_profileName, accountName);
				controller.initData(id_transaction, account);
			}

			Stage stage = new Stage();
			stage.setTitle("Delete the transaction");
			Scene scene = new Scene(root);
			JMetro jMetro = new JMetro(Style.DARK);
			jMetro.setScene(scene);

			scene.getStylesheets().add("gui/managing/application.css");
			stage.setScene(scene);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setResizable(false);
			stage.showAndWait();
			updateMainTable();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void goToMergedView() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/managing/MergedView.fxml"));
			Parent root = loader.load();

			MergedViewController controller = loader.getController();
			controller.initData(m_profileName);

			Stage stage = new Stage();
			stage.setTitle("View merged accounts data");
			Scene scene = new Scene(root);
			JMetro jMetro = new JMetro(Style.DARK);
			jMetro.setScene(scene);
			scene.getStylesheets().add("gui/managing/application.css");
			stage.setScene(scene);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setResizable(false);
			stage.showAndWait();
			m_selectedAccounts = controller.getSelectedAccounts();
			if (m_selectedAccounts.size() > 1) {
				m_actualAccount = null;
				m_multipleAccounts = new MultipleAccounts(m_profileName, m_selectedAccounts,
						controller.haveSavingsData(), m_mainTablePointingDate);
				String accountFieldText = "Accounts :";
				Boolean first = true;
				for (String selectedAccount : m_selectedAccounts) {
					if (first) {
						accountFieldText += " " + selectedAccount;
						first = false;
					} else {
						accountFieldText += ", " + selectedAccount;
					}
				}
				actualAccountField.setText(accountFieldText);
				if (controller.haveSavingsData()) {
					debitAccount.getItems().clear();
					debitAccount.getItems().addAll(m_selectedAccounts);
					debitAccount.getSelectionModel().selectFirst();
				}
				updateMainTable();
			} else {
				m_multipleAccounts = null;
				m_selectedAccounts.clear();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}