package fr.moneyManaging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.joda.time.LocalDate;

import me.dbManaging.DbConnection;

public class DetailedTransaction {
	private int id_transact, id_account, link, lin_id_transact;
	private double transact_amount;
	private LocalDate date;
	private String reason, profileName;

	public DetailedTransaction(int id_transact, String profileName) {
		super();
		this.id_transact = id_transact;
		this.profileName = profileName;
		Connection connection = null;
		String file = "mainDb.db";
		try {
			// create a database connection
			String profileResourcesPath = DbConnection.getResourcesEquivalentPath(this.profileName);
			connection = DriverManager.getConnection("jdbc:sqlite:" + profileResourcesPath + "\\" + file);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			ResultSet rs = statement.executeQuery(
					String.format("select * from transactions where id_transact = %d", this.id_transact));
			rs.next();
			this.id_account = rs.getInt("id_account");
			this.link = rs.getInt("link");
			this.lin_id_transact = rs.getInt("link_id_transact");
			this.transact_amount = rs.getDouble("transact_amount");
			this.date = new LocalDate(rs.getString("date"));
			this.reason = rs.getString("reason");
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

	public int getId_account() {
		return id_account;
	}

	public void setId_account(int id_account) {
		this.id_account = id_account;
	}

	public int getLink() {
		return link;
	}

	public void setLink(int link) {
		this.link = link;
	}

	public int getLin_id_transact() {
		return lin_id_transact;
	}

	public void setLin_id_transact(int lin_id_transact) {
		this.lin_id_transact = lin_id_transact;
	}

	public double getTransact_amount() {
		return transact_amount;
	}

	public void setTransact_amount(double transact_amount) {
		this.transact_amount = transact_amount;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public int getId_transact() {
		return id_transact;
	}

	public String getProfileName() {
		return profileName;
	}

	
	
}
