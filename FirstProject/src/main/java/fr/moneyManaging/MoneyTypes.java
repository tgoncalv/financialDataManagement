package fr.moneyManaging;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import me.dbManaging.DbConnection;

/**
 * This class provides every tools needed to handle the moneyTypes database for
 * a designated profile
 * 
 * @author Taiga
 *
 */
public class MoneyTypes extends Profile {
	MoneyTypes(String profileName) {
		super(profileName);
	}

	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = BigDecimal.valueOf(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	/**
	 * Get every types of money (€,£,...) saved in the databases
	 * 
	 * @param profileName Name of the profile which we want to access
	 * @return A dictionary with the money's id as the key and it's name as the
	 *         value
	 */
	public static HashMap<Integer, String> viewMoneyTypes(String profileName) {
		HashMap<Integer, String> moneyTypes = new HashMap<Integer, String>();
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			String equivalentFilePath = DbConnection.getResourcesEquivalentPath(profileName);
			connection = DriverManager.getConnection("jdbc:sqlite:" + equivalentFilePath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery("select * from moneyTypes");

			while (rs.next()) {
				// read the result set
				moneyTypes.put(rs.getInt("id_money"), rs.getString("name"));
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
		return moneyTypes;
	}

	/**
	 * get the id of the corresponding money's name
	 * @param profileName	name of the profile where the money is used
	 * @param moneyName	name of the money
	 * @return	the id of the money
	 */
	public static int getMoneyId(String profileName, String moneyName) {
		int moneyId = -1;
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			String equivalentFilePath = DbConnection.getResourcesEquivalentPath(profileName);
			connection = DriverManager.getConnection("jdbc:sqlite:" + equivalentFilePath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement
					.executeQuery(String.format("select id_money from moneyTypes where name = '%s'", moneyName));
			rs.next();
			moneyId = rs.getInt(1);
			return moneyId;

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
		return moneyId;
	}
	
	/**
	 * get the name of the correspondig money's id
	 * @param profileName	name of the profile where the money is used
	 * @param moneyId	the id of the money
	 * @return	the name of the money
	 */
	public static String getMoneyName(String profileName, int moneyId) {
		String moneyName = "";
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			String equivalentFilePath = DbConnection.getResourcesEquivalentPath(profileName);
			connection = DriverManager.getConnection("jdbc:sqlite:" + equivalentFilePath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement
					.executeQuery(String.format("select name from moneyTypes where id_money = %d", moneyId));
			rs.next();
			moneyName = rs.getString(1);
			return moneyName;

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
		return moneyName;
	}

	/**
	 * Creates a new money in the database
	 * 
	 * @param profileName profile of the concerned database
	 * @param moneyName   name of the new money (the method does nothing if the name
	 *                    is already used)
	 */
	public static void createMoney(String profileName, String moneyName) {
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			String equivalentFilePath = DbConnection.getResourcesEquivalentPath(profileName);
			connection = DriverManager.getConnection("jdbc:sqlite:" + equivalentFilePath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery("Select name from moneyTypes");
			Boolean validName = true;
			while (rs.next()) {
				if (rs.getString("name").equals(moneyName)) {
					validName = false;
					break;
				}
			}
			if (validName) {
				rs = statement.executeQuery("select max(id_money) from moneyTypes");
				rs.next();
				int moneyId = rs.getInt(1) + 1;
				statement.executeUpdate(String.format("insert into moneyTypes values (%d, '%s')", moneyId, moneyName));
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
	 * Deletes a money type in the database
	 * 
	 * @param profileName profile of the concerned database
	 * @param moneyName   name of the money to delete (does nothing if the name does
	 *                    not exist)
	 */
	public static void deleteMoney(String profileName, String moneyName) {
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			String equivalentFilePath = DbConnection.getResourcesEquivalentPath(profileName);
			connection = DriverManager.getConnection("jdbc:sqlite:" + equivalentFilePath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			statement.executeUpdate(String.format("delete from moneyTypes where name = '%s'", moneyName));

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
	
	public static int getMoneyIdFromAccount(String profileName, String accountName) {
		int moneyId = -1;
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			String equivalentFilePath = DbConnection.getResourcesEquivalentPath(profileName);
			connection = DriverManager.getConnection("jdbc:sqlite:" + equivalentFilePath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement
					.executeQuery(String.format("select id_money from accounts where name = '%s'", accountName));
			rs.next();
			moneyId = rs.getInt(1);
			return moneyId;

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
		return moneyId;
	}
	
	public static String getMoneyNameFromAccount(String profileName, String accountName) {
		String moneyName = "";
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			String equivalentFilePath = DbConnection.getResourcesEquivalentPath(profileName);
			connection = DriverManager.getConnection("jdbc:sqlite:" + equivalentFilePath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement
					.executeQuery(String.format("select moneyTypes.name from accounts join moneyTypes on accounts.id_money == moneyTypes.id_money where accounts.name = '%s'", accountName));
			rs.next();
			moneyName = rs.getString(1);

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
		return moneyName;
	}
	
	/**
	 * Get the list of every accounts with the same money Id
	 * @param porifleName	profile where the accounts are stored
	 * @param moneyId	the concerned money Id
	 * @param accountType	account type (1 = current, 2 = economy, -1 = every accounts) 
	 * @return	HashMap<AccountID, AccountNAME>
	 */
	public static HashMap<Integer, String> getAccountsListFromMoneyId(String profileName, int moneyId, int accountType) {
		HashMap<Integer, String> accountsList = new HashMap<>();
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			String equivalentFilePath = DbConnection.getResourcesEquivalentPath(profileName);
			connection = DriverManager.getConnection("jdbc:sqlite:" + equivalentFilePath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs;
			if (accountType == -1) {
				rs = statement
						.executeQuery(String.format("select name, id_account from accounts where id_money = %d", moneyId));
			} else {
				rs = statement
						.executeQuery(String.format("select name, id_account from accounts where id_money = %d and type = %d", moneyId, accountType));				
			}
			while (rs.next()) {
				accountsList.put(rs.getInt("id_account"), rs.getString("name"));
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
		return accountsList;
	}
	
}
