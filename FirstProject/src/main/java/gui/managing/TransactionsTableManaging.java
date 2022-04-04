package gui.managing;

public class TransactionsTableManaging {
	private double amount;
	private String reason;
	private int id_transact;

	public TransactionsTableManaging(int id_transact, double amount, String reason) {
		super();
		this.amount = amount;
		this.reason = reason;
		this.id_transact = id_transact;
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
