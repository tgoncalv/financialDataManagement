package gui.managing;

import me.dbManaging.DbConnection;

public class MultipleTransactionsTableManaging {
	private double amount;
	private String reason, name_account;
	private int id_transact, id_account;
	

	public MultipleTransactionsTableManaging(int id_transact, double amount, String reason, int id_account, String profileName) {
		super();
		this.amount = amount;
		this.reason = reason;
		this.id_transact = id_transact;
		this.id_account = id_account;
		this.name_account = DbConnection.getAccountName(profileName, id_account);
	}

	public String getName_account() {
		return name_account;
	}

	public void setName_account(String name_account) {
		this.name_account = name_account;
	}

	public int getId_account() {
		return id_account;
	}

	public void setId_account(int id_account) {
		this.id_account = id_account;
	}

	public int getId_transact() {
		return id_transact;
	}

	public void setId_transact(int id_transact) {
		this.id_transact = id_transact;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}
