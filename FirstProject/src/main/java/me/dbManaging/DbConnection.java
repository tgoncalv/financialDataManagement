package me.dbManaging;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.joda.time.LocalDate;

/**
 * This class provides every methods used to handle a database in
 * fr.moneyManaging package
 */
public class DbConnection {

	/**
	 * Replace the last occurrence of a string by another string
	 * 
	 * @param string      String to modify
	 * @param toReplace   String which will be removed
	 * @param replacement String which will be added
	 * @return The modified string or the unmodified string if the string to replace
	 *         doesn't exists
	 */
	public static String replaceLast(String string, String toReplace, String replacement) {
		int pos = string.lastIndexOf(toReplace);
		if (pos > -1) {
			return string.substring(0, pos) + replacement + string.substring(pos + toReplace.length(), string.length());
		} else {
			return string;
		}
	}

	/**
	 * 
	 * @param folder Name of the folder which we want to get the path
	 * @return The path of the folder which contains every resources needed to
	 *         execute the current file (the folder will be created if it doesn't
	 *         exist yet)
	 */
	public static String getResourcesEquivalentPath(String folder) {
		//String currentFilePath = System.getProperty("user.dir");
		String equivalentFilePath = "C:\\dev\\moneyManager\\" + folder;
		File file = new File(equivalentFilePath);
		if (!file.exists()) {
			file.mkdirs();
		}
		return equivalentFilePath;
	}

	private static Set<String> getTablesList(Connection connection) {
		Set<String> tablesList = new HashSet<String>();
		try {
			ResultSet resultSet = connection.getMetaData().getTables(null, null, "%", null);
			while (resultSet.next()) {
				tablesList.add(resultSet.getString(3));
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
		return tablesList;
	}

	/**
	 * Creates a database file for a new profile. The tables moneyTypes,
	 * transactions and accounts are created.
	 * 
	 * @param folder Folder which will contain every personal files of the new
	 *               profile
	 */
	public static void initializeDb(String folder) {
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			String equivalentFilePath = getResourcesEquivalentPath(folder);
			connection = DriverManager.getConnection("jdbc:sqlite:" + equivalentFilePath + "\\" + file);
			
			Set<String> tablesList = getTablesList(connection);
			connection = DriverManager.getConnection("jdbc:sqlite:" + equivalentFilePath + "\\" + file);

			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			for (String table : tablesList) {
				statement.executeUpdate(String.format("drop table if exists %s", table));
			}

			statement.executeUpdate("create table moneyTypes (id_money integer, name string)");
			statement.executeUpdate(
					"create table transactions (id_transact integer, id_account integer, date date, transact_amount double, reason string, link integer, link_id_transact integer)");
			statement.executeUpdate(
					"create table accounts (id_account integer, name string, id_money integer, type integer, source string, period_profit string, cumul_profit_start date)");

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
	 * Creates an account with an unique ID in the table accounts. One or two tables
	 * associated to this account will be created (the number of tables depends to
	 * the type of account)
	 * 
	 * @param profileName Name of the profile where the account will be created
	 * @param accountName Name of the new account (must be different to the other
	 *                    accounts' name)
	 * @param moneyId     id of the money which will be used
	 * @param type        type of the account (1 = CurrentAccount (use two tables),
	 *                    2 = EconomyAccount (use one table))
	 * @param source      source of the money used (Bank account, cash, virtual,...)
	 */
	public static void createAccount(String profileName, String accountName, int moneyId, int type, String source) {
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			String equivalentFilePath = DbConnection.getResourcesEquivalentPath(profileName);
			connection = DriverManager.getConnection("jdbc:sqlite:" + equivalentFilePath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery("Select name from accounts");
			Boolean validName = true;
			while (rs.next()) {
				if (rs.getString("name").equals(accountName)) {
					validName = false;
					break;
				}
			}
			rs = statement.executeQuery("Select id_money from moneyTypes");
			Boolean validMoneyId = false;
			while (rs.next()) {
				if (rs.getInt("id_money") == moneyId) {
					validMoneyId = true;
					break;
				}
			}
			if (validName && validMoneyId) {
				LocalDate today = new LocalDate();
				String cumul_profit_start = today.toString();
				rs = statement.executeQuery("select max(id_account) from accounts");
				rs.next();
				int accountId = rs.getInt(1) + 1;
				String period_profit = "m"; //w = week, m = month, y=year
				statement.executeUpdate(String.format("insert into accounts values (%d, '%s', %d, %d, '%s', '%s', '%s')",
						accountId, accountName, moneyId, type, source,period_profit, cumul_profit_start));

				// Creation of a table associated to this new account
				String table = "account_" + String.valueOf(accountId);
				statement.executeUpdate(String.format("drop table if exists %s", table));
				if (type == 1) {// type currentAccount
					statement.executeUpdate(String.format(
							"create table %s (date date, expense_tot double, income_tot double, profit double)",
							table));
					String table2 = "accountC_" + String.valueOf(accountId); // A current account needs a second table
					statement.executeUpdate(String.format(
							"create table %s (date date, amount_to_save int, manual_save boolean, conf_save int, totalAdvancedSaving int, id_eco_account int, losses double)",
							table2));
				} else if (type == 2) {// type economyAccount
					statement.executeUpdate(String.format(
							"create table %s (date date, expense_tot double, income_tot double, profit double)",
							table));
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
	 * Delete an existing account
	 * @param profileName	name of the profile where the account is stored
	 * @param accountId	id of the account
	 */
	public static void deleteAccount(String profileName, int accountId) {
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			String equivalentFilePath = DbConnection.getResourcesEquivalentPath(profileName);
			connection = DriverManager.getConnection("jdbc:sqlite:" + equivalentFilePath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery(String.format("select count(id_account) from accounts where id_account=%d",accountId));
			rs.next();
			if (rs.getInt(1)==1) {
				rs = statement.executeQuery(String.format("select type from accounts where id_account=%d",accountId));
				rs.next();
				int type = rs.getInt("type");
				statement.executeUpdate(String.format("delete from accounts where id_account=%d",accountId));
				
				// Delete tables associated to this account
				String table = "account_" + String.valueOf(accountId);
				if (type == 1) {// type currentAccount
					statement.executeUpdate(String.format("drop table if exists %s", table));
					String table2 = "accountC_" + String.valueOf(accountId); // A current account have two table
					statement.executeUpdate(String.format("drop table if exists %s", table2));
				} else if (type == 2) {// type economyAccount
					statement.executeUpdate(String.format("drop table if exists %s", table));
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
	 * get the id of the corresponding account's name
	 * @param profileName	name of the profile where the account is stored
	 * @param accountName	name of the account
	 * @return	id of the account
	 */
	public static int getAccountId(String profileName, String accountName) {
		int accountId = -1;
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			String equivalentFilePath = DbConnection.getResourcesEquivalentPath(profileName);
			connection = DriverManager.getConnection("jdbc:sqlite:" + equivalentFilePath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement
					.executeQuery(String.format("select id_account from accounts where name = '%s'", accountName));
			rs.next();
			accountId = rs.getInt(1);
			return accountId;

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
		return accountId;
	}
	
	/**
	 * get the name of the corresponding account's id
	 * @param profileName	name of the profile where the account is stored
	 * @param accountId	id of the account
	 * @return name of the account
	 */
	public static String getAccountName(String profileName, int accountId) {
		String accountName = "";
		if (accountId<0) {
			return accountName; //this method is used when transactions are done. When the transaction is external, the default linkId is equal to -1
		}
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			String equivalentFilePath = DbConnection.getResourcesEquivalentPath(profileName);
			connection = DriverManager.getConnection("jdbc:sqlite:" + equivalentFilePath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement
					.executeQuery(String.format("select name from accounts where id_account = %d", accountId));
			rs.next();
			accountName = rs.getString(1);
			return accountName;

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
		return accountName;
	}
	
	/**
	 * Get the periodProfit parameter (y/m/w)
	 * @param profileName	name of the profile
	 * @param accountId	id of the account concerned
	 * @return	returned y,m or w (= year,month or week)
	 */
	public static String getPeriodProfit(String profileName, int accountId) {
		String periodProfit = "m";
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			String equivalentFilePath = DbConnection.getResourcesEquivalentPath(profileName);
			connection = DriverManager.getConnection("jdbc:sqlite:" + equivalentFilePath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement
					.executeQuery(String.format("select period_profit from accounts where id_account = %d", accountId));
			rs.next();
			periodProfit = rs.getString(1);
			return periodProfit;

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
		return periodProfit;
	}
	
	/**
	 * Get the account's money source (wallet, bank card,...)
	 * @param profileName	name of the profile
	 * @param accountId	id of the concerned account
	 * @return	String
	 */
	public static String getAccountSource(String profileName, int accountId) {
		String accountSource = "";
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			String equivalentFilePath = DbConnection.getResourcesEquivalentPath(profileName);
			connection = DriverManager.getConnection("jdbc:sqlite:" + equivalentFilePath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement
					.executeQuery(String.format("select source from accounts where id_account = %d", accountId));
			rs.next();
			accountSource = rs.getString(1);
			return accountSource;

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
		return accountSource;	
	}
	
	/**
	 * Get the account's type
	 * @param profileName	name of the profile
	 * @param accountId	id of the concerned account
	 * @return	integer (1=Current account, 2=Economy account)
	 */
	public static int getAccountType(String profileName, int accountId) {
		int accountType = 0;
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			String equivalentFilePath = DbConnection.getResourcesEquivalentPath(profileName);
			connection = DriverManager.getConnection("jdbc:sqlite:" + equivalentFilePath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement
					.executeQuery(String.format("select type from accounts where id_account = %d", accountId));
			rs.next();
			accountType = rs.getInt(1);
			return accountType;

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
		return accountType;
	}
	
	/**
	 * Get the list of accounts possessing a particular money id and type
	 * @param profileName	name of the profile
	 * @param idMoney	id of the money (put -1 if every types of money are accepted)
	 * @param type	int corresponding to the type (put -1 if every types are accepted)
	 * @return	HashMap<Integer idAccount, String nameAccount>
	 */
	public static LinkedHashMap<Integer, String> listAccounts(String profileName, int idMoney, int type) {
		LinkedHashMap<Integer, String> listAccounts = new LinkedHashMap<Integer, String>();
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			String equivalentFilePath = DbConnection.getResourcesEquivalentPath(profileName);
			connection = DriverManager.getConnection("jdbc:sqlite:" + equivalentFilePath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs;
			if (idMoney >= 0) {
				if (type >= 0) {
					rs = statement
							.executeQuery(String.format("select id_account, name from accounts where id_money = %d and type = %d order by name", idMoney, type));	
				} else {
					rs = statement
							.executeQuery(String.format("select id_account, name from accounts where id_money = %d order by name", idMoney));
				}
				
			} else {
				if (type >= 0) {
					rs = statement
							.executeQuery(String.format("select id_account, name from accounts where type = %d order by id_money, name", type));
				} else {
					rs = statement
							.executeQuery("select id_account, name from accounts order by id_money,name");
				}
			}
			
			while (rs.next()) {
				listAccounts.put(rs.getInt("id_account"), rs.getString("name"));
			}
			return listAccounts;

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
		return listAccounts;
	}
}