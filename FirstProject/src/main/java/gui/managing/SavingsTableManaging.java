package gui.managing;

import org.joda.time.LocalDate;

public class SavingsTableManaging {
	private LocalDate rowDate;
	private String rowLink;
	private int rowAmount;
	private int transactId;

	public SavingsTableManaging(LocalDate rowDate, String rowLink, int rowAmount, int transactId) {
		super();
		this.rowDate = rowDate;
		this.rowLink = rowLink;
		this.rowAmount = rowAmount;
		this.transactId = transactId;
	}

	public LocalDate getRowDate() {
		return rowDate;
	}

	public void setRowDate(LocalDate rowDate) {
		this.rowDate = rowDate;
	}

	public String getRowLink() {
		return rowLink;
	}

	public void setRowLink(String rowLink) {
		this.rowLink = rowLink;
	}

	public int getRowAmount() {
		return rowAmount;
	}

	public void setRowAmount(int rowAmount) {
		this.rowAmount = rowAmount;
	}

	public int getTransactId() {
		return transactId;
	}

	public void setTransactId(int transactId) {
		this.transactId = transactId;
	}

}
