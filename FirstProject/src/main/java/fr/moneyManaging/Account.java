package fr.moneyManaging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.joda.time.LocalDate;

import me.dbManaging.DbConnection;

/**
 * Class containing every methods used for both CurrentAccount and
 * EconomyAccount. Thud the account object must be created through
 * CurrentAccount or EconomyAccount class
 * 
 * @author taiga
 *
 */
public class Account extends Profile {
	protected int m_pointerColumn = -1;
	protected LocalDate m_pointerDate = new LocalDate();
	protected int m_moneyId = -1;
	protected String m_moneyName;
	protected String m_accountName;
	protected int m_accountId;
	protected String m_periodProfit;
	protected LocalDate m_periodProfitStart = new LocalDate();
	protected int m_accountType;
	protected String m_accountSource;

	protected HashMap<LocalDate, Double> m_monthExpenses = new HashMap<LocalDate, Double>(); // all the spending done
																								// during the month
																								// corresponding to
	// m_pointerDate
	protected HashMap<LocalDate, Double> m_monthGains = new HashMap<LocalDate, Double>(); // every amount of money
																							// obtained during the month
																							// corresponding
	// to m_pointerDate
	protected HashMap<LocalDate, Double> m_monthProfits = new HashMap<LocalDate, Double>(); // sum of all transactions
																							// done during the month
																							// distinct day
	// by day
	protected double m_totalProfit; // sum of all transactions done during the month corresponding to m_pointerDate

	public Account(String profileName, String accountName) {
		super(profileName);
		this.m_accountName = accountName;
		this.m_moneyId = MoneyTypes.getMoneyIdFromAccount(profileName, accountName);
		this.m_moneyName = MoneyTypes.getMoneyName(profileName, m_moneyId);
		this.m_accountId = DbConnection.getAccountId(profileName, accountName);
		this.m_periodProfit = DbConnection.getPeriodProfit(profileName, m_accountId);
		this.m_accountType = DbConnection.getAccountType(profileName, m_accountId);
		this.m_accountSource = DbConnection.getAccountSource(profileName, m_accountId);
		updateMonth();
		initializeCumulProfitStart();
	}

	public void initializeCumulProfitStart() {
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery(
					String.format("select cumul_profit_start from accounts where id_account = %d", m_accountId));
			rs.next();
			m_periodProfitStart = new LocalDate(rs.getString(1));

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
	 * Add a spending in the transaction's database for the date m_pointerDate and
	 * the account m_accountId. The transaction must be external (else use
	 * internalTransact method)
	 * 
	 * @param amount value of the money spent (will become automatically a negative
	 *               value)
	 * @param reason the reason that the money was spend
	 */
	public void spend(double amount, String reason) {
		internalTransact(Math.abs(amount) * -1, reason, -1);
	}

	/**
	 * Add a gain in the transaction's database fir the date m_pointerDate and the
	 * account m_accountId. The transaction must be external (else use
	 * internalTransact method)
	 * 
	 * @param amount value of the money obtained (will become automatically a
	 *               positive value)
	 * @param reason the reason that the money has been obtained
	 */
	public void obtain(double amount, String reason) {
		internalTransact(Math.abs(amount), reason, -1);
	}

	/**
	 * Does an internal transaction between two accounts which use the same money
	 * type
	 * 
	 * @param amount amount of the transaction (spend = negative, obtain = positive)
	 * @param reason reason of the transaction
	 * @param linkId corresponds to the id of another account's id when the
	 *               transaction is intern, if not, the default is -1
	 */
	public void internalTransact(double amount, String reason, int linkId) {
		String linkName = DbConnection.getAccountName(m_profileName, linkId);
		if (linkId == -1 || m_moneyId == MoneyTypes.getMoneyIdFromAccount(m_profileName, linkName)) {
			internalInternationalTransact(amount, amount * -1, reason, linkId);
		}
	}

	/**
	 * Does an internal transaction between two accounts which can possess different
	 * money type. Thus the transaction amount differs from the donor's account to
	 * the receiver's account
	 * 
	 * @param amountDepart  amount of the transaction done by m_accountId (spend =
	 *                      negative, obtain = positive)
	 * @param amountArrival amount of the transaction done by linkId (must have the
	 *                      opposite sign to amountDepart)
	 * @param reason        reason of the transaction
	 * @param linkId        corresponds to the id of another account's id when the
	 *                      transaction is intern, if not, the default is -1
	 */
	public void internalInternationalTransact(double amountDepart, double amountArrival, String reason, int linkId) {
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

				statement.executeUpdate(
						String.format("insert into transactions values (%d, %d, '%s', %s, '%s', %d, %d)", transactId,
								m_accountId, m_pointerDate.toString(), amountDepart, reason, linkId, linkIdTransact));
				if (linkId >= 0) {
					statement.executeUpdate(String.format(
							"insert into transactions values (%d, %d, '%s', %s, '%s', %d, %d)", linkIdTransact, linkId,
							m_pointerDate.toString(), amountArrival, reason, m_accountId, transactId));
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

	/**
	 * Get the actual amount of money in the account
	 * 
	 * @param date set the desired date
	 * @return
	 */
	public Double obtainActualTotalAmountOfMoney(LocalDate date) {
		Double total = 0.;
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery(String.format(
					"select sum(transact_amount) from transactions where id_account = %d and date <= '%s'",
					m_accountId, date.toString()));
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
	 * Get the raw profit (=profit without transactions to save money) of the
	 * LocalDate's month
	 * 
	 * @param date LocalDate
	 * @return double
	 */
	public Double obtainThisMonthRawProfit(LocalDate date) {
		Double total = 0.;
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery(String.format(
					"select sum(transact_amount) from transactions where id_account = %d and date >= '%s' and date <= '%s' and reason != 'Transaction to save money'",
					m_accountId, date.dayOfMonth().withMinimumValue().toString(),
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

	/**
	 * Get the cumulative profits before saving money
	 * 
	 * @param date end date of the calculation (the beginning date is
	 *             m_periodProfitStart)
	 * @return Double amount
	 */
	public Double obtainCumulatedProfit(LocalDate date) {
		Double total = 0.;
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery(String.format(
					"select sum(transact_amount) from transactions where id_account = %d and date >= '%s' and date <= '%s' and reason != 'Transaction to save money'",
					m_accountId, m_periodProfitStart.toString(), date.toString()));
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
	 * Edits an existing transaction
	 * 
	 * @param profileName   name of the profile where the transaction exists (the
	 *                      method is static)
	 * @param transactId    id of the transaction
	 * @param amountDepart  new amount of the depart transaction
	 * @param amountArrival new amount of the arrival transaction ( = anything if
	 *                      linkId == -1)
	 * @param reason        new reason of the transaction
	 * @param linkId        new linked account id if the transaction is intern
	 * @param departAccountId Changes the id of the account corresponding to "amountDepart". Enter -1 to conserve the initial account.
	 */
	public static void editTransact(String profileName, int transactId, double amountDepart, double amountArrival,
			String reason, int linkId, int departAccountId) {
		Connection connection = null;
		String file = "mainDb.db";
		try {
			if (Math.signum(amountDepart) != Math.signum(amountArrival)) {
				// create a database connection
				String profileResourcesPath = DbConnection.getResourcesEquivalentPath(profileName);
				connection = DriverManager.getConnection("jdbc:sqlite:" + profileResourcesPath + "\\" + file);
				Statement statement = connection.createStatement();
				statement.setQueryTimeout(30); // set timeout to 30 sec.

				ResultSet rs = statement.executeQuery(String.format(
						"select link_id_transact, link, date, id_account from transactions where id_transact = %d",
						transactId));
				rs.next();
				int link_id_transact = rs.getInt("link_id_transact");
				int ancientLinkId = rs.getInt("link");
				String date = rs.getString("date");
				int accountId = rs.getInt("id_account");					
				if (departAccountId != -1) {
					accountId = departAccountId;
				}
				if (ancientLinkId == -1 && linkId >= 0) {
					rs = statement.executeQuery("select max(id_transact) from transactions");
					rs.next();
					link_id_transact = rs.getInt(1) + 1;
					statement.executeUpdate(
							String.format("insert into transactions values (%d, %d, '%s', %s, '%s', %d, %d)",
									link_id_transact, linkId, date, amountArrival, reason, accountId, transactId));
				} else if (ancientLinkId >= 0 && linkId == -1) {
					statement.executeUpdate(
							String.format("delete from transactions where id_transact = %d", link_id_transact));
					link_id_transact = -1;
				} else if (ancientLinkId >= 0 && linkId >= 0) {
					PreparedStatement ps = connection.prepareStatement(
							"update transactions set id_account = ?, transact_amount = ?, reason = ?, link = ?, link_id_transact = ? where id_transact = ?");
					ps.setInt(1, linkId);
					ps.setDouble(2, amountArrival);
					ps.setString(3, reason);
					ps.setInt(4, accountId);
					ps.setInt(5, transactId);
					ps.setInt(6, link_id_transact);
					ps.executeUpdate();
				}

				PreparedStatement ps = connection.prepareStatement(
						"update transactions set transact_amount = ?, reason = ?, link = ?, link_id_transact = ? where id_transact = ?");
				ps.setDouble(1, amountDepart);
				ps.setString(2, reason);
				ps.setInt(3, linkId);
				ps.setInt(4, link_id_transact);
				ps.setInt(5, transactId);
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
	 * Delete a transaction
	 * 
	 * @param transactId id of the concerned transaction
	 */
	public void deleteTransact(int transactId) {
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

	/**
	 * Get every transactions details for a given date (except if the reason is
	 * "Transaction to save money")
	 * 
	 * @param date date of the transactions
	 * @return HashMap with transact_id as keys and [transact_amount, reason, link]
	 *         as values
	 */
	public HashMap<Integer, DoubleStringIntList> listTransact(LocalDate date) {
		HashMap<Integer, DoubleStringIntList> transactList = new HashMap<Integer, DoubleStringIntList>();
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery(String.format(
					"select id_transact, transact_amount, reason, link from transactions where id_account = %d and date='%s' and reason != 'Transaction to save money'",
					m_accountId, date.toString()));
			int transactId;
			DoubleStringIntList value;
			while (rs.next()) {
				transactId = rs.getInt("id_transact");
				value = new DoubleStringIntList(rs.getDouble("transact_amount"), rs.getString("reason"),
						rs.getInt("link"));
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
	
	public HashMap<Integer, DoubleStringIntList> listEcoTransactSingleDay(LocalDate date) {
		HashMap<Integer, DoubleStringIntList> transactList = new HashMap<Integer, DoubleStringIntList>();
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery(String.format(
					"select id_transact, transact_amount, reason, link from transactions where id_account = %d and date='%s' and reason == 'Transaction to save money'",
					m_accountId, date.toString()));
			int transactId;
			DoubleStringIntList value;
			while (rs.next()) {
				transactId = rs.getInt("id_transact");
				value = new DoubleStringIntList(rs.getDouble("transact_amount"), rs.getString("reason"),
						rs.getInt("link"));
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

	public void confPeriodProfit(String periodProfit) {
		Connection connection = null;
		String file = "mainDb.db";
		if (periodProfit == "y" || periodProfit == "m" || periodProfit == "m") {
			try {
				// create a database connection
				connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
				Statement statement = connection.createStatement();
				statement.setQueryTimeout(30); // set timeout to 30 sec.

				PreparedStatement ps = connection
						.prepareStatement("update accounts set period_profit = ? where id_account = ?");
				ps.setString(1, periodProfit);
				ps.setInt(2, m_accountId);
				ps.executeUpdate();
				m_periodProfit = periodProfit;

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
	 * Modify the m_periodProfitStart parameter for the object and in the database
	 * 
	 * @param date new LocalDate of the parameter to change
	 */
	public void confCumulProfit(LocalDate date) {
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			PreparedStatement ps = connection
					.prepareStatement("update accounts set cumul_profit_start = ? where id_account = ?");
			ps.setString(1, date.toString());
			ps.setInt(2, m_accountId);
			ps.executeUpdate();
			m_periodProfitStart = date;

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
	 * Change m_moneyId, m_moneyName parameters for the object and id_money in the
	 * accounts' table for the actual m_accountId
	 * 
	 * @param moneyName Name of the new money used (must exist because the method
	 *                  doesn't verify it's existence)
	 */
	public void changeMoneyRef(String moneyName) {
		Connection connection = null;
		String file = "mainDb.db";
		int moneyId = MoneyTypes.getMoneyId(m_profileName, moneyName);
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			PreparedStatement ps = connection.prepareStatement("update accounts set id_money = ? where id_account = ?");
			ps.setInt(1, moneyId);
			ps.setInt(2, m_accountId);
			ps.executeUpdate();
			m_moneyId = moneyId;
			m_moneyName = moneyName;

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
	 * Change the source parameter
	 * 
	 * @param source new source (wallet, bank card,...)
	 */
	public void changeAccountSource(String source) {
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			PreparedStatement ps = connection.prepareStatement("update accounts set source = ? where id_account = ?");
			ps.setString(1, source);
			ps.setInt(2, m_accountId);
			ps.executeUpdate();
			m_accountSource = source;

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
	 * Update m_totalProfit, m_monthExpenses, m_monthGains and m_monthProfits Does
	 * not take into account transactions with the reason : "Transaction to save
	 * money"
	 */
	public void updateMonth() {
		m_totalProfit = 0;
		if (!m_monthProfits.isEmpty()) {
			m_monthProfits.clear();

		}
		if (!m_monthExpenses.isEmpty()) {
			m_monthExpenses.clear();
		}
		if (!m_monthGains.isEmpty()) {
			m_monthGains.clear();
		}
		LocalDate dateToAdd = m_pointerDate.dayOfMonth().withMinimumValue();
		LocalDate maxDate = m_pointerDate.dayOfMonth().withMaximumValue();
		do {
			m_monthProfits.put(dateToAdd,0.);
			dateToAdd = dateToAdd.plusDays(1);
		} while (dateToAdd.isBefore(maxDate.plusDays(1)));
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs;
			if (m_accountType == 1) {
				rs = statement.executeQuery(String.format(
						"select date, transact_amount from transactions where id_account = %d and date >= '%s' and date <= '%s' and reason != 'Transaction to save money'",
						m_accountId, m_pointerDate.dayOfMonth().withMinimumValue(),
						m_pointerDate.dayOfMonth().withMaximumValue()));
			} else {
				rs = statement.executeQuery(String.format(
						"select date, transact_amount from transactions where id_account = %d and date >= '%s' and date <= '%s'",
						m_accountId, m_pointerDate.dayOfMonth().withMinimumValue(),
						m_pointerDate.dayOfMonth().withMaximumValue()));
			}

			LocalDate date;
			double amount;
			while (rs.next()) {
				date = new LocalDate(rs.getString("date"));
				amount = rs.getDouble("transact_amount");
				m_totalProfit += amount;
				m_monthProfits.put(date, m_monthProfits.getOrDefault(date, 0.) + amount);
				if (amount >= 0) {
					m_monthGains.put(date, m_monthGains.getOrDefault(date, 0.) + amount);
				} else {
					m_monthExpenses.put(date, m_monthExpenses.getOrDefault(date, 0.) - amount); // -amount because the
																								// result must be
																								// positive
				}
			}
			dateToAdd = m_pointerDate.dayOfMonth().withMinimumValue();
			maxDate = m_pointerDate.dayOfMonth().withMaximumValue();
			Double previousAmount = 0.;
			do {
				Double todayAmount = m_monthProfits.get(dateToAdd);
				m_monthProfits.put(dateToAdd, todayAmount + previousAmount);
				dateToAdd = dateToAdd.plusDays(1);
				previousAmount += todayAmount;
			} while (dateToAdd.isBefore(maxDate.plusDays(1)));
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

	public HashMap<LocalDate, Double> obtainYearExpenses() {
		HashMap<LocalDate, Double> yearExpenses = new HashMap<LocalDate, Double>();
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs;
			if (m_accountType == 1) {
				rs = statement.executeQuery(String.format(
						"select date, transact_amount from transactions where id_account = %d and date >= '%s' and date <= '%s' and reason != 'Transaction to save money'",
						m_accountId, m_pointerDate.dayOfYear().withMinimumValue(),
						m_pointerDate.dayOfYear().withMaximumValue()));
			} else {
				rs = statement.executeQuery(String.format(
						"select date, transact_amount from transactions where id_account = %d and date >= '%s' and date <= '%s'",
						m_accountId, m_pointerDate.dayOfYear().withMinimumValue(),
						m_pointerDate.dayOfYear().withMaximumValue()));
			}

			LocalDate date;
			double amount;
			while (rs.next()) {
				date = new LocalDate(rs.getString("date"));
				amount = rs.getDouble("transact_amount");
				m_totalProfit += amount;
				if (amount < 0) {
					yearExpenses.put(date, yearExpenses.getOrDefault(date, 0.) - amount); // -amount because the
																							// result must be
																							// positive
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
		return yearExpenses;
	}

	public HashMap<LocalDate, Double> obtainYearGains() {
		HashMap<LocalDate, Double> yearGains = new HashMap<LocalDate, Double>();
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs;
			if (m_accountType == 1) {
				rs = statement.executeQuery(String.format(
						"select date, transact_amount from transactions where id_account = %d and date >= '%s' and date <= '%s' and reason != 'Transaction to save money'",
						m_accountId, m_pointerDate.dayOfYear().withMinimumValue(),
						m_pointerDate.dayOfYear().withMaximumValue()));
			} else {
				rs = statement.executeQuery(String.format(
						"select date, transact_amount from transactions where id_account = %d and date >= '%s' and date <= '%s'",
						m_accountId, m_pointerDate.dayOfYear().withMinimumValue(),
						m_pointerDate.dayOfYear().withMaximumValue()));
			}

			LocalDate date;
			double amount;
			while (rs.next()) {
				date = new LocalDate(rs.getString("date"));
				amount = rs.getDouble("transact_amount");
				m_totalProfit += amount;
				if (amount >= 0) {
					yearGains.put(date, yearGains.getOrDefault(date, 0.) + amount);
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
		return yearGains;
	}

	public HashMap<LocalDate, Double> obtainYearProfits() {
		HashMap<LocalDate, Double> yearProfits = new HashMap<LocalDate, Double>();
		LocalDate dateToAdd = m_pointerDate.dayOfMonth().withMinimumValue();
		LocalDate maxDate = m_pointerDate.dayOfMonth().withMaximumValue();
		do {
			yearProfits.put(dateToAdd,0.);
			dateToAdd = dateToAdd.plusDays(1);
		} while (dateToAdd.isBefore(maxDate.plusDays(1)));
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs;
			if (m_accountType == 1) {
				rs = statement.executeQuery(String.format(
						"select date, transact_amount from transactions where id_account = %d and date >= '%s' and date <= '%s' and reason != 'Transaction to save money'",
						m_accountId, m_pointerDate.dayOfYear().withMinimumValue(),
						m_pointerDate.dayOfYear().withMaximumValue()));
			} else {
				rs = statement.executeQuery(String.format(
						"select date, transact_amount from transactions where id_account = %d and date >= '%s' and date <= '%s'",
						m_accountId, m_pointerDate.dayOfYear().withMinimumValue(),
						m_pointerDate.dayOfYear().withMaximumValue()));
			}

			LocalDate date;
			double amount;
			while (rs.next()) {
				date = new LocalDate(rs.getString("date"));
				amount = rs.getDouble("transact_amount");
				m_totalProfit += amount;
				if (amount >= 0) {
					yearProfits.put(date, yearProfits.getOrDefault(date, 0.) + amount);
				} else {
					yearProfits.put(date, yearProfits.getOrDefault(date, 0.) + amount); // -amount because the
					// result must be
					// positive
				}
			}
			dateToAdd = m_pointerDate.dayOfMonth().withMinimumValue();
			maxDate = m_pointerDate.dayOfMonth().withMaximumValue();
			Double previousAmount = 0.;
			do {
				Double todayAmount = yearProfits.get(dateToAdd);
				yearProfits.put(dateToAdd, todayAmount + previousAmount);
				dateToAdd = dateToAdd.plusDays(1);
				previousAmount += todayAmount;
			} while (dateToAdd.isBefore(maxDate.plusDays(1)));
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
		return yearProfits;
	}

	/**
	 * Modify m_pointerDate and m_pointerColumn
	 * 
	 * @param date
	 * @param column
	 */
	public void select(LocalDate date, int column) {
		m_pointerDate = date;
		if (column == 0 || column == 1 || column == 2) {
			m_pointerColumn = column;
		}
	}

	/**
	 * Get the value of m_pointerDate
	 * 
	 * @return LocalDate value (yyyy-mm-dd)
	 */
	public LocalDate getPointerDate() {
		return m_pointerDate;
	}

	/**
	 * Get the value of m_pointerColumn
	 * 
	 * @return int value (0,1 or 2)
	 */
	public int getPointerColumn() {
		return m_pointerColumn;
	}

	/**
	 * Get the name of the money used for this account
	 * 
	 * @return String
	 */
	public String getMoneyName() {
		return m_moneyName;
	}

	public int getMoneyId() {
		return m_moneyId;
	}

	public int getAccountId() {
		return m_accountId;
	}

	/**
	 * Get the name of the actual account
	 * 
	 * @return String
	 */
	public String getAccountName() {
		return m_accountName;
	}

	/**
	 * Get the m_periodProfit parameter
	 * 
	 * @return String (y for year, m for month, w for week)
	 */
	public String getPeriodProfit() {
		return m_periodProfit;
	}

	/**
	 * Get the m_periodProfitStart parameter
	 * 
	 * @return LocalDate
	 */
	public LocalDate getPeriodProfitStart() {
		return m_periodProfitStart;
	}

	/**
	 * Get the m_accountSource parameter
	 * 
	 * @return String
	 */
	public String getAccountSource() {
		return m_accountSource;
	}

	/**
	 * Get the m_accountType parameter
	 * 
	 * @return int (1=Current account, 2=Economy account)
	 */
	public int getAccountType() {
		return m_accountType;
	}

	/**
	 * Get the m_monthExpenses parameter
	 * 
	 * @return HashMap<LocalDate, Double>
	 */
	public HashMap<LocalDate, Double> getMonthExepenses() {
		return m_monthExpenses;
	}

	/**
	 * Get the m_monthGains parameter
	 * 
	 * @return HashMap<LocalDate, Double>
	 */
	public HashMap<LocalDate, Double> getMonthGains() {
		return m_monthGains;
	}

	/**
	 * Get the m_monthProfits parameter
	 * 
	 * @return HashMap<LocalDate, Double>
	 */
	public HashMap<LocalDate, Double> getMonthProfits() {
		return m_monthProfits;
	}

	/**
	 * Get the m_totalProfit (of the selected month) parameter
	 * 
	 * @return double
	 */
	public double getTotalProfit() {
		return m_totalProfit;
	}

}
