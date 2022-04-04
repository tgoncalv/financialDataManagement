package fr.moneyManaging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 * Class used to create an economy account. It inherits every methods used in
 * Account class and Profile Class
 * 
 * @author taiga
 *
 */
public class EconomyAccount extends Account {

	public EconomyAccount(String profileName, String accountName) {
		super(profileName, accountName);
	}

	/**
	 * Gets the list of save transactions (reason = "Transaction to save money")
	 * done during the month corresponding to m_saveDate
	 * 
	 * @return Multimap<Integer, Object> with id_transact as keys and [date,
	 *         transact_amount, link] as values
	 */
	public HashMap<Integer, DoubleStringIntList> listEcoTransact() {
		HashMap<Integer, DoubleStringIntList> transactList = new HashMap<Integer, DoubleStringIntList>();
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:" + m_profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery(String.format(
					"select id_transact,date, transact_amount, link,reason from transactions where id_account = %d and date >= '%s' and date <= '%s' and reason = 'Transaction to save money'",
					m_accountId, m_pointerDate.dayOfMonth().withMinimumValue(),
					m_pointerDate.dayOfMonth().withMaximumValue()));
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
}
