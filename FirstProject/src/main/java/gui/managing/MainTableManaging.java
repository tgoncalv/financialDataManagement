package gui.managing;

import org.joda.time.LocalDate;

public class MainTableManaging {
	private LocalDate rowDate;
	private Double rowExpense, rowGain, rowProfit;
	
	public MainTableManaging(LocalDate rowDate, Double rowExpense, Double rowGain, Double rowProfit) {
		super();
		this.rowDate = rowDate;
		this.rowExpense = rowExpense;
		this.rowGain = rowGain;
		this.rowProfit = rowProfit;
	}

	public LocalDate getRowDate() {
		return rowDate;
	}

	public void setRowDate(LocalDate rowDate) {
		this.rowDate = rowDate;
	}

	public Double getRowExpense() {
		return rowExpense;
	}

	public void setRowExpense(Double rowExpense) {
		this.rowExpense = rowExpense;
	}

	public Double getRowGain() {
		return rowGain;
	}

	public void setRowGain(Double rowGain) {
		this.rowGain = rowGain;
	}

	public Double getRowProfit() {
		return rowProfit;
	}

	public void setRowProfit(Double rowProfit) {
		this.rowProfit = rowProfit;
	}
	
	
}
