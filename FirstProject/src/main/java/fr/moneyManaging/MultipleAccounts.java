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

public class MultipleAccounts extends Profile {
	private Collection<String> m_accountsIdList = new ArrayList<String>();
	private Collection<String> m_accountsName = new ArrayList<String>();
	private Boolean m_haveSavingsData = false;
	private LocalDate m_saveDate = new LocalDate();
	private HashMap<String, Integer> m_amountToSave = new HashMap<>();
	private HashMap<String, Boolean> m_manualSave = new HashMap<>();
	private HashMap<String, Integer> m_confSave = new HashMap<>();
	private HashMap<String, Integer> m_totalAdvancedSaving = new HashMap<>();
	private HashMap<String, Integer> m_idEcoAccount = new HashMap<>();
	private HashMap<String, Double> m_losses = new HashMap<>();
	private int m_totalEveryAdvancedSaving = 0;

	public MultipleAccounts(String profileName, Collection<String> accountsList, Boolean haveSavingsData,
			LocalDate date) {
		super(profileName);
		for (String accountName : accountsList) {
			this.m_accountsName.add(accountName);
			this.m_accountsIdList.add(String.valueOf(DbConnection.getAccountId(profileName, accountName)));
			this.m_amountToSave.put(accountName, 0);
			this.m_manualSave.put(accountName, true);
			this.m_confSave.put(accountName, 20);
			this.m_totalAdvancedSaving.put(accountName, 0);
			this.m_idEcoAccount.put(accountName, -1);
			this.m_losses.put(accountName, 0.);
		}
		this.m_haveSavingsData = haveSavingsData;
		m_saveDate = date.dayOfMonth().withMaximumValue();
		for (String accountId : m_accountsIdList) {
			if (!findMonth(accountId, true)) {
				setNewMonth(accountId);
			}
		}
	}

	public Boolean haveSavingsData() {
		return m_haveSavingsData;
	}

	public Collection<String> getAccountsName() {
		return m_accountsName;
	}

	/**
	 * Every transactions done between two dates
	 * 
	 * @param debDate
	 * @param endDate
	 * @param type    -1 if we want every transactions, 1 if we don't want
	 *                "Transaction to save money", 2 if we only want "Transaction to
	 *                save money"
	 * @return
	 */
	public HashMap<LocalDate, DoubleDoubleDoubleList> transactionsList(LocalDate debDate, LocalDate endDate, int type) {
		HashMap<LocalDate, DoubleDoubleDoubleList> transactList = new HashMap<LocalDate, DoubleDoubleDoubleList>();
		Connection connection = null;
		String file = "mainDb.db";
		LocalDate dateToAdd = debDate;
		do {
			transactList.put(dateToAdd, new DoubleDoubleDoubleList(0., 0., 0.));
			dateToAdd = dateToAdd.plusDays(1);
		} while (dateToAdd.isBefore(endDate.plusDays(1)));
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery(String.format(
					"select date, transact_amount from transactions where date>= '%s' and date <= '%s' and id_account in (%s) and reason != 'Transaction to save money'",
					debDate.toString(), endDate.toString(), String.join(", ", m_accountsIdList)));
			if (type == -1) {
				rs = statement.executeQuery(String.format(
						"select date, transact_amount from transactions where date>= '%s' and date <= '%s' and id_account in (%s)",
						debDate.toString(), endDate.toString(), String.join(", ", m_accountsIdList)));
			} else if (type == 2) {
				rs = statement.executeQuery(String.format(
						"select date, transact_amount from transactions where date>= '%s' and date <= '%s' and id_account in (%s) and reason == 'Transaction to save money'",
						debDate.toString(), endDate.toString(), String.join(", ", m_accountsIdList)));
			}
			LocalDate date = new LocalDate();
			double amount;
			while (rs.next()) {
				date = new LocalDate(rs.getString("date"));
				amount = rs.getDouble("transact_amount");
				transactList.get(date).addToThird(amount);
				if (amount >= 0) {
					transactList.get(date).addToSecond(amount);
				} else {
					transactList.get(date).addToFirst(amount);
				}
			}
			dateToAdd = debDate;
			Double previousAmount = 0.;
			do {
				Double todayAmount = transactList.get(dateToAdd).getThird();
				if (todayAmount == null) {
					todayAmount = 0.;
				}
				transactList.get(dateToAdd).addToThird(previousAmount);
				dateToAdd = dateToAdd.plusDays(1);
				previousAmount += todayAmount;
			} while (dateToAdd.isBefore(endDate.plusDays(1)));
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
	 * Gets the list of save transactions done during the month corresponding to
	 * m_saveDate
	 * 
	 * @return Multimap<Integer, Object> with id_transact as keys and [date,
	 *         transact_amount, link] as values
	 */
	public HashMap<Integer, DateIntStringList> listEcoTransact(LocalDate date) {
		HashMap<Integer, DateIntStringList> transactList = new HashMap<Integer, DateIntStringList>();
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery(String.format(
					"select id_transact,date, transact_amount, link from transactions where id_account in (%s) and date >= '%s' and date <= '%s' and reason = 'Transaction to save money'",
					String.join(", ", m_accountsIdList), date.dayOfMonth().withMinimumValue(),
					date.dayOfMonth().withMaximumValue()));
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
	 * Gets every transactions for a specific day
	 * 
	 * @param date
	 * @param type -1 if we want every transactions, 1 if we don't want "Transaction
	 *             to save money", 2 if we only want "Transaction to save money"
	 * @return
	 */
	public HashMap<Integer, DoubleStringIntList> detailedTransactions(LocalDate date, int type) {
		HashMap<Integer, DoubleStringIntList> transactList = new HashMap<Integer, DoubleStringIntList>();
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.
			ResultSet rs = statement.executeQuery(String.format(
					"select id_transact, transact_amount, reason, id_account from transactions where date = '%s' and id_account in (%s)",
					date.toString(), String.join(", ", m_accountsIdList)));
			if (type == 1) {
				rs = statement.executeQuery(String.format(
						"select id_transact, transact_amount, reason, id_account from transactions where date = '%s' and id_account in (%s) and reason != 'Transaction to save money'",
						date.toString(), String.join(", ", m_accountsIdList)));
			} else if (type == -2) {
				rs = statement.executeQuery(String.format(
						"select id_transact, transact_amount, reason, id_account from transactions where date = '%s' and id_account in (%s) and reason = 'Transaction to save money'",
						date.toString(), String.join(", ", m_accountsIdList)));
			}

			while (rs.next()) {
				transactList.put(rs.getInt("id_transact"), new DoubleStringIntList(rs.getDouble("transact_amount"),
						rs.getString("reason"), rs.getInt("id_account")));
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
		return transactList;
	}

	public void spend(LocalDate date, int spendingAccount, Double amount, String reason) {
		transaction(date, spendingAccount, Math.abs(amount) * -1, Math.abs(amount), reason, -1);
	}

	public void obtain(LocalDate date, int obtainingAccount, Double amount, String reason) {
		transaction(date, obtainingAccount, Math.abs(amount), Math.abs(amount) * -1, reason, -1);
	}

	public void transaction(LocalDate date, int idDepart, double amountDepart, double amountArrival, String reason,
			int linkId) {
		Connection connection = null;
		String file = "mainDb.db";
		try {
			if (Math.signum(amountDepart) != Math.signum(amountArrival)) {
				// create a database connection
				connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
				Statement statement = connection.createStatement();
				statement.setQueryTimeout(30); // set timeout to 30 sec.

				ResultSet rs = statement.executeQuery("select max(id_transact) from transactions");
				rs.next();
				int transactId = rs.getInt(1) + 1;
				int linkIdTransact = -1;
				if (linkId >= 0) {
					linkIdTransact = transactId + 1;
				}

				statement
						.executeUpdate(String.format("insert into transactions values (%d, %d, '%s', %s, '%s', %d, %d)",
								transactId, idDepart, date.toString(), amountDepart, reason, linkId, linkIdTransact));
				if (linkId >= 0) {
					statement.executeUpdate(String.format(
							"insert into transactions values (%d, %d, '%s', %s, '%s', %d, %d)", linkIdTransact, linkId,
							date.toString(), amountArrival, reason, idDepart, transactId));
				}
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

	public void deleteTransaction(int transactId) {
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery(
					String.format("select link_id_transact from transactions where id_transact = %d", transactId));
			rs.next();
			int linkId = rs.getInt(1);
			if (linkId >= 0) {
				statement.executeUpdate(String.format("delete from transactions where id_transact = %d", linkId));
			}
			statement.executeUpdate(String.format("delete from transactions where id_transact = %d", transactId));
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

	public double obtainActualTotalAmountOfMoney(LocalDate date) {
		Double total = 0.;
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.
			ResultSet rs = statement.executeQuery(String.format(
					"select sum(transact_amount) from transactions where date <= '%s' and id_account in (%s)",
					date.toString(), String.join(", ", m_accountsIdList)));
			rs.next();
			total = rs.getDouble(1);
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
		return total;
	}

	public double obtainThisMonthRawProfit(LocalDate date) {
		Double total = 0.;
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery(String.format(
					"select sum(transact_amount) from transactions where id_account in (%s) and date >= '%s' and date <= '%s' and reason != 'Transaction to save money'",
					String.join(", ", m_accountsIdList), date.dayOfMonth().withMinimumValue().toString(),
					date.dayOfMonth().withMaximumValue().toString()));
			rs.next();
			total = rs.getDouble(1);
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
		return total;
	}

	public double obtainCumulatedProfit(LocalDate startDate, LocalDate endDate) {
		Double total = 0.;
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery(String.format(
					"select sum(transact_amount) from transactions where id_account in (%s) and date >= '%s' and date <= '%s' and reason != 'Transaction to save money'",
					String.join(", ", m_accountsIdList), startDate.toString(), endDate.toString()));
			rs.next();
			total = rs.getDouble(1);
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
		return total;
	}

	/**
	 * Change all attributes to make them correspond to the parameters linked to
	 * m_pointerDate
	 */
	public void changeMonth(String debitAccountName, LocalDate pointerDate) {
		this.m_saveDate = pointerDate.dayOfMonth().withMaximumValue();
		for (String accountId : m_accountsIdList) {
			if (!findMonth(accountId, true)) {
				setNewMonth(accountId);
			}
		}
		updateMonthTotalAdvancedSaving(debitAccountName);
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
	public Boolean findMonth(String accountId, Boolean updateAttributes) {
		Connection connection = null;
		String file = "mainDb.db";
		String accountCx = "accountC_" + accountId;
		String accountName = DbConnection.getAccountName(m_profileName, Integer.valueOf(accountId));
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
				m_amountToSave.replace(accountName, rs.getInt("amount_to_save"));
				m_manualSave.replace(accountName, rs.getBoolean("manual_save"));
				m_confSave.replace(accountName, rs.getInt("conf_save"));
				m_totalAdvancedSaving.replace(accountName, rs.getInt("totalAdvancedSaving"));
				m_idEcoAccount.replace(accountName, rs.getInt("id_eco_account"));
				m_losses.replace(accountName, rs.getDouble("losses"));
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

	public void setNewMonth(String accountId) {
		Boolean manualSave = true;
		int confSave = 20;
		int amountToSave = 0;
		Double losses = 0.;
		int totalAdvancedSaving = 0;
		int idEcoAccount = -1;

		// Get m_totalAdvancedSaving and m_idEcoAccount from the previous month
		LocalDate previousMonth = m_saveDate.dayOfMonth().withMinimumValue().minusDays(1);
		Connection connection = null;
		String file = "mainDb.db";
		String accountCx = "accountC_" + accountId;
		String accountName = DbConnection.getAccountName(m_profileName, Integer.valueOf(accountId));
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery(String.format(
					"select totalAdvancedSaving, amount_to_save,id_eco_account from %s where date == '%s'", accountCx,
					previousMonth.toString()));
			if (rs.isBeforeFirst()) {
				rs.next();
				totalAdvancedSaving = rs.getInt("totalAdvancedSaving") - rs.getInt("amount_to_save");
				idEcoAccount = rs.getInt("id_eco_account");
			}
			Collection<DateIntStringList> transactList = new ArrayList<DateIntStringList>();
			transactList = listEcoTransact(m_saveDate).values();
			for (DateIntStringList transact : transactList) {
				totalAdvancedSaving += transact.getM_int();
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

		if (idEcoAccount == -1) {
			HashMap<Integer, String> listEcoAccounts = DbConnection.listAccounts(m_profileName,
					MoneyTypes.getMoneyIdFromAccount(m_profileName,
							DbConnection.getAccountName(m_profileName, Integer.valueOf(accountId))),
					2);
			if (listEcoAccounts.size() > 0) {
				Map.Entry<Integer, String> entry = listEcoAccounts.entrySet().iterator().next();
				idEcoAccount = entry.getKey();
			}
		}

		m_amountToSave.replace(accountName, amountToSave);
		m_manualSave.replace(accountName, manualSave);
		m_confSave.replace(accountName, confSave);
		m_totalAdvancedSaving.replace(accountName, totalAdvancedSaving);
		m_idEcoAccount.replace(accountName, idEcoAccount);
		m_losses.replace(accountName, losses);
		inputInfoIntoDb(accountId);
	}

	/**
	 * Stores the information about m_saveDate's month saving in the database. The
	 * corresponding row must already exist !
	 */
	public void inputInfoIntoDb(String accountId) {
		Connection connection = null;
		String file = "mainDb.db";
		String accountCx = "accountC_" + accountId;
		String accountName = DbConnection.getAccountName(m_profileName, Integer.valueOf(accountId));
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery(
					String.format("select * from %s where date = '%s'", accountCx, m_saveDate.toString()));
			if (!rs.isBeforeFirst()) {
				statement.executeUpdate(String.format("insert into %s values ('%s', %d, %b, %d, %d, %d, %s)", accountCx,
						m_saveDate.toString(), m_amountToSave.get(accountName), m_manualSave.get(accountName),
						m_confSave.get(accountName), m_totalAdvancedSaving.get(accountName),
						m_idEcoAccount.get(accountName), m_losses.get(accountName)));
			} else {
				PreparedStatement ps = connection.prepareStatement(String.format(
						"update %s set amount_to_save = ?, manual_save = ?, conf_save = ?, totalAdvancedSaving = ?, id_eco_account = ?, losses = ? where date = ?",
						accountCx));
				ps.setInt(1, m_amountToSave.get(accountName));
				ps.setBoolean(2, m_manualSave.get(accountName));
				ps.setInt(3, m_confSave.get(accountName));
				ps.setInt(4, m_totalAdvancedSaving.get(accountName));
				ps.setInt(5, m_idEcoAccount.get(accountName));
				ps.setDouble(6, m_losses.get(accountName));
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

	private void updateMonthTotalAdvancedSaving(String debitAccountName) {
		LocalDate previousMonth = m_saveDate.dayOfMonth().withMinimumValue().minusDays(1);
		Connection connection = null;
		String file = "mainDb.db";
		int accountId = DbConnection.getAccountId(m_profileName, debitAccountName);
		m_totalEveryAdvancedSaving = 0;
		m_totalAdvancedSaving.replace(debitAccountName, 0);
		String accountCx = "accountC_" + String.valueOf(accountId);
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery(String.format(
					"select totalAdvancedSaving, amount_to_save,id_eco_account from %s where date == '%s'", accountCx,
					previousMonth.toString()));
			if (rs.isBeforeFirst()) {
				rs.next();
				m_totalEveryAdvancedSaving = rs.getInt("totalAdvancedSaving") - rs.getInt("amount_to_save");
				m_totalAdvancedSaving.replace(debitAccountName, m_totalEveryAdvancedSaving);
			}

			HashMap<Integer, DateIntStringList> listEcoTransact = listEcoTransact(m_saveDate);
			for (DateIntStringList transact : listEcoTransact.values()) {
				m_totalEveryAdvancedSaving += transact.getM_int();
			}

			for (int transactId : listEcoTransact.keySet()) {
				DetailedTransaction transaction = new DetailedTransaction(transactId, m_profileName);
				if (transaction.getId_account() == accountId) {
					int oldValue = m_totalAdvancedSaving.get(debitAccountName);
					m_totalAdvancedSaving.replace(debitAccountName,
							oldValue + listEcoTransact.get(transactId).getM_int());
				}
			}

			PreparedStatement ps = connection
					.prepareStatement(String.format("update %s set totalAdvancedSaving = ? where date = ?", accountCx));
			ps.setInt(1, m_totalAdvancedSaving.get(debitAccountName));
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

	public Boolean getManualSave(String debitAccountName) {
		return m_manualSave.get(debitAccountName);
	}

	public String getMoneyName(String debitAccountName) {
		return MoneyTypes.getMoneyNameFromAccount(m_profileName, debitAccountName);
	}

	public Integer getConfSave(String debitAccountName) {
		return m_confSave.get(debitAccountName);
	}

	public HashMap<String, Integer> getAmountToSave(String debitAccountName) {
		return m_amountToSave;
	}

	public int getTotalAdvancedSaving(String debitAccountName) {
		Connection connection = null;
		String file = "mainDb.db";
		int totalAdvancedSaving = 0;
		for (String accountId : m_accountsIdList) {
			String accountCx = "accountC_" + accountId;
			try {
				// create a database connection
				connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
				Statement statement = connection.createStatement();
				statement.setQueryTimeout(30); // set timeout to 30 sec.

				ResultSet rs = statement.executeQuery(
						String.format("select * from %s where date = '%s'", accountCx, m_saveDate.toString()));
				totalAdvancedSaving += rs.getInt("totalAdvancedSaving");

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
		return totalAdvancedSaving;
	}

	public Double getLosses(String debitAccountName) {
		Connection connection = null;
		String file = "mainDb.db";
		Double losses = 0.;
		for (String accountId : m_accountsIdList) {
			String accountCx = "accountC_" + accountId;
			try {
				// create a database connection
				connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
				Statement statement = connection.createStatement();
				statement.setQueryTimeout(30); // set timeout to 30 sec.

				ResultSet rs = statement.executeQuery(
						String.format("select * from %s where date = '%s'", accountCx, m_saveDate.toString()));
				losses += rs.getDouble("losses");

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
		return losses;
	}

	public int getIdEcoAccount(String debitAccountName) {
		return m_idEcoAccount.get(debitAccountName);
	}

	public void setIdEcoAccount(String debitAccountName, int idEcoAccount) {
		m_idEcoAccount.replace(debitAccountName, idEcoAccount);
		Connection connection = null;
		String file = "mainDb.db";
		int accountId = DbConnection.getAccountId(m_profileName, debitAccountName);
		String accountCx = "accountC_" + String.valueOf(accountId);
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			PreparedStatement ps = connection
					.prepareStatement(String.format("update %s set id_eco_account = ? where date = ?", accountCx));
			ps.setInt(1, idEcoAccount);
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

	public void setAmountToSave(String debitAccountName, int amount) {
		m_amountToSave.replace(debitAccountName, amount);
		Connection connection = null;
		String file = "mainDb.db";
		int accountId = DbConnection.getAccountId(m_profileName, debitAccountName);
		String accountCx = "accountC_" + String.valueOf(accountId);
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			PreparedStatement ps = connection
					.prepareStatement(String.format("update %s set amount_to_save = ? where date = ?", accountCx));
			ps.setInt(1, amount);
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

	public void confManualSave(String debitAccountName, Boolean manualSave) {
		m_manualSave.replace(debitAccountName, manualSave);
		Connection connection = null;
		String file = "mainDb.db";
		int accountId = DbConnection.getAccountId(m_profileName, debitAccountName);
		String accountCx = "accountC_" + String.valueOf(accountId);
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			PreparedStatement ps = connection
					.prepareStatement(String.format("update %s set manual_save = ? where date = ?", accountCx));
			ps.setBoolean(1, manualSave);
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

	public void modifyConfSave(String debitAccountName, int confSave) {
		if (0 <= confSave && confSave <= 100) {
			m_confSave.replace(debitAccountName, confSave);
			Connection connection = null;
			String file = "mainDb.db";
			int accountId = DbConnection.getAccountId(m_profileName, debitAccountName);
			String accountCx = "accountC_" + String.valueOf(accountId);
			try {
				// create a database connection
				connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
				Statement statement = connection.createStatement();
				statement.setQueryTimeout(30); // set timeout to 30 sec.

				PreparedStatement ps = connection
						.prepareStatement(String.format("update %s set conf_save = ? where date = ?", accountCx));
				ps.setInt(1, confSave);
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
	 * Calculate the amount of money to be saved for the month
	 * 
	 * @param overwrite true if the value stored in the database needs to be
	 *                  overwritten
	 */
	public void calculateAmountToSave(String debitAccountName, boolean overwrite) {
		Boolean m_manualSave = getManualSave(debitAccountName);
		if (!m_manualSave) {
			int m_confSave = getConfSave(debitAccountName);
			double m_totalProfit = obtainThisMonthRawProfit(m_saveDate);
			int amountToSave = (int) Math.ceil(m_totalProfit * m_confSave / 100);
			if (amountToSave >= 0) {
				m_amountToSave.replace(debitAccountName, amountToSave);
			} else {
				m_amountToSave.replace(debitAccountName, 0);
			}

			if (overwrite) {
				Connection connection = null;
				String file = "mainDb.db";
				int accountId = DbConnection.getAccountId(m_profileName, debitAccountName);
				String accountCx = "accountC_" + String.valueOf(accountId);
				try {
					// create a database connection
					connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
					Statement statement = connection.createStatement();
					statement.setQueryTimeout(30); // set timeout to 30 sec.

					PreparedStatement ps = connection.prepareStatement(
							String.format("update %s set amount_to_save = ? where date = ?", accountCx));
					ps.setInt(1, m_amountToSave.get(debitAccountName));
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

}
