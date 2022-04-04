package fr.moneyManaging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDate;

import me.dbManaging.DbConnection;

/**
 * Class used to create a current account. It inherits every methods used in
 * Account class and Profile Class
 * 
 * @author taiga
 *
 */
public class CurrentAccount extends Account {
	protected LocalDate m_saveDate = new LocalDate();
	protected int m_amountToSave;
	protected boolean m_manualSave = false;
	protected int m_confSave = 20;
	protected int m_totalAdvancedSaving;
	protected int m_idEcoAccount;
	protected double m_losses;

	public CurrentAccount(String profileName, String accountName) {
		super(profileName, accountName);
		this.m_saveDate = this.m_pointerDate.dayOfMonth().withMaximumValue();
		if (!findMonth(true)) {
			setNewMonth();
		}
	}

	/**
	 * Change all attributes to make them correspond to the parameters linked to
	 * m_pointerDate
	 */
	public void changeMonth() {
		this.m_saveDate = this.m_pointerDate.dayOfMonth().withMaximumValue();
		if (!findMonth(true)) {
			setNewMonth();
		}
		updateMonthTotalAdvancedSaving();
	}

	/**
	 * Modify the m_confSave parameter
	 * 
	 * @param percentage new percentage (between 0 and 100%)
	 */
	public void modifyConfSave(int percentage) {
		if (percentage >= 0 && percentage <= 100) {
			Connection connection = null;
			String file = "mainDb.db";
			String accountCx = "accountC_" + String.valueOf(m_accountId);
			try {
				// create a database connection
				connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
				Statement statement = connection.createStatement();
				statement.setQueryTimeout(30); // set timeout to 30 sec.

				m_confSave = percentage;
				calculateAmountToSave(false);

				PreparedStatement ps = connection.prepareStatement(
						String.format("update %s set conf_save = ?, amount_to_save = ? where date = ?", accountCx));
				ps.setInt(1, percentage);
				ps.setInt(2, m_amountToSave);
				ps.setString(3, m_saveDate.toString());
				ps.executeUpdate();

			} catch (SQLException e) {
				// if the error message is "out of memory",
				// it probably means no database file is found
				System.err.println(e.getMessage());
			} finally {
				try {
					if (connection != null)
						connection.close();
				} catch (SQLException e) {
					// connection close failed.
					System.err.println(e.getMessage());
				}
			}
		}
	}

	/**
	 * Calculate the amount of money to be saved for the month
	 * 
	 * @param overwrite true if the value stored in the database needs to be
	 *                  overwritten
	 */
	public void calculateAmountToSave(boolean overwrite) {
		if (!m_manualSave) {
			int amountToSave = (int) Math.ceil(m_totalProfit * m_confSave / 100);
			if (amountToSave >= 0) {
				m_amountToSave = amountToSave;
			} else {
				m_amountToSave = 0;
			}

			if (overwrite) {
				Connection connection = null;
				String file = "mainDb.db";
				String accountCx = "accountC_" + String.valueOf(m_accountId);
				try {
					// create a database connection
					connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
					Statement statement = connection.createStatement();
					statement.setQueryTimeout(30); // set timeout to 30 sec.

					PreparedStatement ps = connection.prepareStatement(
							String.format("update %s set amount_to_save = ? where date = ?", accountCx));
					ps.setInt(1, m_amountToSave);
					ps.setString(2, m_saveDate.toString());
					ps.executeUpdate();

				} catch (SQLException e) {
					// if the error message is "out of memory",
					// it probably means no database file is found
					System.err.println(e.getMessage());
				} finally {
					try {
						if (connection != null)
							connection.close();
					} catch (SQLException e) {
						// connection close failed.
						System.err.println(e.getMessage());
					}
				}
			}
		}
	}

	/**
	 * Activate or deactivate manualSave (if deactivate, it will recalculate
	 * m_amountToSave and stock it in the database
	 * 
	 * @param manualSave boolean
	 */
	public void confManualSave(boolean manualSave) {
		m_manualSave = manualSave;
		Connection connection = null;
		String file = "mainDb.db";
		String accountCx = "accountC_" + String.valueOf(m_accountId);
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			if (manualSave) {
				PreparedStatement ps = connection
						.prepareStatement(String.format("update %s set manual_save = ? where date = ?", accountCx));
				ps.setBoolean(1, manualSave);
				ps.setString(2, m_saveDate.toString());
				ps.executeUpdate();
			} else {
				calculateAmountToSave(false);
				PreparedStatement ps = connection.prepareStatement(
						String.format("update %s set manual_save = ?, amount_to_save = ? where date = ?", accountCx));
				ps.setBoolean(1, manualSave);
				ps.setInt(2, m_amountToSave);
				ps.setString(3, m_saveDate.toString());
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e.getMessage());
			}
		}
	}

	/**
	 * Check if the informations about saving for the selected date already exist in
	 * the database. If the informations exist, the method can overwrite the
	 * attributes corresponding to these informations.
	 * 
	 * @updateAttributes true if we want to update the attributes' informations
	 * 
	 * @return true if the informations exist
	 */
	public boolean findMonth(boolean updateAttributes) {
		Connection connection = null;
		String file = "mainDb.db";
		String accountCx = "accountC_" + String.valueOf(m_accountId);
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery(
					String.format("select * from %s where date = '%s'", accountCx, m_saveDate.toString()));
			if (!rs.isBeforeFirst()) {
				return false;
			} else if (updateAttributes == true) {
				rs.next();
				m_amountToSave = rs.getInt("amount_to_save");
				m_manualSave = rs.getBoolean("manual_save");
				m_confSave = rs.getInt("conf_save");
				m_totalAdvancedSaving = rs.getInt("totalAdvancedSaving");
				m_idEcoAccount = rs.getInt("id_eco_account");
				m_losses = rs.getDouble("losses");
				return true;
			}

		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e.getMessage());
			}
		}
		return true;
	}

	public void updateTotalAdvancedSaving(LocalDate date, int amount) {
		Connection connection = null;
		String file = "mainDb.db";
		String accountCx = "accountC_" + String.valueOf(m_accountId);
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			date = date.dayOfMonth().withMaximumValue();
			int totalAdvancedSaving = 0;
			ResultSet rs = statement.executeQuery(
					String.format("select totalAdvancedSaving from %s where date = '%s'", accountCx, date.toString()));
			if (rs.isBeforeFirst()) {
				rs.next();
				totalAdvancedSaving = rs.getInt("totalAdvancedSaving") + amount;
			}
			PreparedStatement ps = connection.prepareStatement(String.format(
					"update %s set totalAdvancedSaving = ? where date = ?",
					accountCx));
			ps.setInt(1, totalAdvancedSaving);
			ps.setString(2, date.toString());
			ps.executeUpdate();

		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e.getMessage());
			}
		}
	}

	/**
	 * Creates a new month if this month is not listed in the database. The
	 * attributes and the database will be both updated by this method.
	 */
	private void setNewMonth() {
		m_manualSave = true;
		m_confSave = 20;
		m_amountToSave = 0;
		m_losses = 0;

		// Get m_totalAdvancedSaving and m_idEcoAccount from the previous month
		LocalDate previousMonth = m_saveDate.dayOfMonth().withMinimumValue().minusDays(1);
		Connection connection = null;
		String file = "mainDb.db";
		String accountCx = "accountC_" + String.valueOf(m_accountId);
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery(String.format(
					"select totalAdvancedSaving, amount_to_save,id_eco_account from %s where date == '%s'", accountCx,
					previousMonth.toString()));
			if (!rs.isBeforeFirst()) {
				m_totalAdvancedSaving = 0;
				m_idEcoAccount = -1;
			} else {
				rs.next();
				m_totalAdvancedSaving = rs.getInt("totalAdvancedSaving") - rs.getInt("amount_to_save");
				m_idEcoAccount = rs.getInt("id_eco_account");
			}
			Collection<DateIntStringList> transactList = new ArrayList<DateIntStringList>();
			transactList = listEcoTransact().values();
			for (DateIntStringList transact : transactList) {
				m_totalAdvancedSaving += transact.getM_int();				
			}

		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e.getMessage());
			}
		}

		if (m_idEcoAccount == -1) {
			HashMap<Integer, String> listEcoAccounts = DbConnection.listAccounts(m_profileName, m_moneyId, 2);
			if (listEcoAccounts.size() > 0) {
				Map.Entry<Integer, String> entry = listEcoAccounts.entrySet().iterator().next();
				m_idEcoAccount = entry.getKey();
			}
		}
		inputInfoIntoDb();
	}
	
	private void updateMonthTotalAdvancedSaving() {
		LocalDate previousMonth = m_saveDate.dayOfMonth().withMinimumValue().minusDays(1);
		Connection connection = null;
		String file = "mainDb.db";
		String accountCx = "accountC_" + String.valueOf(m_accountId);
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			m_totalAdvancedSaving = 0;
			ResultSet rs = statement.executeQuery(String.format(
					"select totalAdvancedSaving, amount_to_save,id_eco_account from %s where date == '%s'", accountCx,
					previousMonth.toString()));
			if (rs.isBeforeFirst()) {
				rs.next();
				m_totalAdvancedSaving = rs.getInt("totalAdvancedSaving") - rs.getInt("amount_to_save");
			}
			Collection<DateIntStringList> transactList = new ArrayList<DateIntStringList>();
			transactList = listEcoTransact().values();
			for (DateIntStringList transact : transactList) {
				m_totalAdvancedSaving += transact.getM_int();				
			}

		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e.getMessage());
			}
		}

		if (m_idEcoAccount == -1) {
			HashMap<Integer, String> listEcoAccounts = DbConnection.listAccounts(m_profileName, m_moneyId, 2);
			if (listEcoAccounts.size() > 0) {
				Map.Entry<Integer, String> entry = listEcoAccounts.entrySet().iterator().next();
				m_idEcoAccount = entry.getKey();
			}
		}
		inputInfoIntoDb();
	}

	/**
	 * Stores the information about m_saveDate's month saving in the database. The
	 * corresponding row must already exist !
	 */
	public void inputInfoIntoDb() {
		Connection connection = null;
		String file = "mainDb.db";
		String accountCx = "accountC_" + String.valueOf(m_accountId);
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			if (!findMonth(false)) {
				statement.executeUpdate(String.format("insert into %s values ('%s', %d, %b, %d, %d, %d, %s)", accountCx,
						m_saveDate.toString(), m_amountToSave, m_manualSave, m_confSave, m_totalAdvancedSaving,
						m_idEcoAccount, m_losses));
			} else {
				PreparedStatement ps = connection.prepareStatement(String.format(
						"update %s set amount_to_save = ?, manual_save = ?, conf_save = ?, totalAdvancedSaving = ?, id_eco_account = ?, losses = ? where date = ?",
						accountCx));
				ps.setInt(1, m_amountToSave);
				ps.setBoolean(2, m_manualSave);
				ps.setInt(3, m_confSave);
				ps.setInt(4, m_totalAdvancedSaving);
				ps.setInt(5, m_idEcoAccount);
				ps.setDouble(6, m_losses);
				ps.setString(7, m_saveDate.toString());
				ps.executeUpdate();
			}

		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e.getMessage());
			}
		}
	}

	/**
	 * add to m_totalAdvancedSaving a certain amount of money and does a transaction
	 * with m_idEcoAccount with the reason "Transaction to save money"
	 * 
	 * @param amount amount of money to add
	 */
	public void advanceEco(int amount) {
		m_totalAdvancedSaving += amount;
		inputInfoIntoDb();
		internalTransact(amount * -1, "Transaction to save money", m_idEcoAccount);
	}

	/**
	 * Gets the list of save transactions done during the month corresponding to
	 * m_saveDate
	 * 
	 * @return Multimap<Integer, Object> with id_transact as keys and [date,
	 *         transact_amount, link] as values
	 */
	public HashMap<Integer, DateIntStringList> listEcoTransact() {
		HashMap<Integer, DateIntStringList> transactList = new HashMap<Integer, DateIntStringList>();
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery(String.format(
					"select id_transact,date, transact_amount, link from transactions where id_account = %d and date >= '%s' and date <= '%s' and reason = 'Transaction to save money'",
					m_accountId, m_pointerDate.dayOfMonth().withMinimumValue(),
					m_pointerDate.dayOfMonth().withMaximumValue()));
			int transactId;
			DateIntStringList value;
			String linkName = "";
			while (rs.next()) {
				transactId = rs.getInt("id_transact");
				linkName = DbConnection.getAccountName(m_profileName, rs.getInt("link"));
				value = new DateIntStringList(new LocalDate(rs.getString("date")), rs.getInt("transact_amount") * -1,
						linkName);
				transactList.put(transactId, value);
			}
			return transactList;
		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e.getMessage());
			}
		}
		return transactList;
	}

	/**
	 * add an amount of loss into the database
	 * 
	 * @param amount amount of the loss (can be negative if it is to correct a wrong
	 *               entry. Anyway there will be no record nor reason about the
	 *               amount of losses)
	 */
	public void addLosses(double amount) {
		m_losses += amount;
		inputInfoIntoDb();
	}

	/**
	 * get m_saveDate parameter
	 * 
	 * @return LocalDate (last day of the m_pointerDate's month)
	 */
	public LocalDate getSaveDate() {
		return m_saveDate;
	}

	/**
	 * get m_amountToSave parameter
	 * 
	 * @return int value
	 */
	public int getAmountToSave() {
		return m_amountToSave;
	}

	public void setAmountToSave(int amount) {

		if (amount >= 0) {
			this.m_amountToSave = amount;
			Connection connection = null;
			String file = "mainDb.db";
			String accountCx = "accountC_" + String.valueOf(m_accountId);
			try {
				// create a database connection
				connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
				Statement statement = connection.createStatement();
				statement.setQueryTimeout(30); // set timeout to 30 sec.

				PreparedStatement ps = connection
						.prepareStatement(String.format("update %s set amount_to_save = ? where date = ?", accountCx));
				ps.setInt(1, m_amountToSave);
				ps.setString(2, m_saveDate.toString());
				ps.executeUpdate();

			} catch (SQLException e) {
				// if the error message is "out of memory",
				// it probably means no database file is found
				System.err.println(e.getMessage());
			} finally {
				try {
					if (connection != null)
						connection.close();
				} catch (SQLException e) {
					// connection close failed.
					System.err.println(e.getMessage());
				}
			}
		}
	}

	/**
	 * Get m_manualSave parameter
	 * 
	 * @return true if the save settings are manual (means that we choose the amount
	 *         of money to save)
	 */
	public boolean getManualSave() {
		return m_manualSave;
	}

	/**
	 * Get m_confSave parameter
	 * 
	 * @return int corresponding to the percentage of money to save from
	 *         m_totalProfit
	 */
	public int getConfSave() {
		return m_confSave;
	}

	/**
	 * get m_totalAdvancedSaving parameter
	 * 
	 * @return int value of the amount of money already saved before the due date
	 */
	public int getTotalAdvancedSaving() {
		return m_totalAdvancedSaving;
	}

	/**
	 * get m_idEcoAccount parameter
	 * 
	 * @return the id of the EconomyAccount which will get the money
	 */
	public int getIdEcoAccount() {
		return m_idEcoAccount;
	}

	/**
	 * get m_losses parameter
	 * 
	 * @return double corresponding to the amount of money which has been lost
	 *         without reason
	 */
	public double getLosses() {
		return m_losses;
	}

	/**
	 * Manually modify the totalAdvancedSaving in case of troubleshooting
	 * 
	 * @param totalAdvancedSaving
	 */
	public void setTotalAdvancedSaving(int totalAdvancedSaving) {
		m_totalAdvancedSaving = totalAdvancedSaving;
		inputInfoIntoDb();
	}

	/**
	 * Choose the account where the saving will transact. The id must be valid, else
	 * the method will do nothing.
	 * 
	 * @param idEcoAccount
	 */
	public void setIdEcoAccount(int idEcoAccount) {
		HashMap<Integer, String> listEcoAccounts = DbConnection.listAccounts(m_profileName, m_moneyId, 2);
		if (listEcoAccounts.containsKey(idEcoAccount)) {
			m_idEcoAccount = idEcoAccount;
			inputInfoIntoDb();
		}
	}
}
