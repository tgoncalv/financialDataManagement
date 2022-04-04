package fr.moneyManaging;

public class DoubleDoubleDoubleList {
	private Double first,second,third;

	public DoubleDoubleDoubleList(Double first, Double second, Double third) {
		super();
		this.first = first;
		this.second = second;
		this.third = third;
	}
	public void addToFirst(double amount) {
		this.first += amount;
	}
	
	public void addToSecond(double amount) {
		this.second += amount;
	}
	
	public void addToThird(double amount) {
		this.third += amount;
	}

	public Double getFirst() {
		if (first == 0.) {
			return null;
		}
		return MoneyTypes.round(first,2);
	}

	public void setFirst(Double first) {
		this.first = first;
	}

	public Double getSecond() {
		if (second == 0.) {
			return null;
		}
		return MoneyTypes.round(second,2);
	}

	public void setSecond(Double second) {
		this.second = second;
	}

	public Double getThird() {
		if (third == 0.) {
			return null;
		}
		return MoneyTypes.round(third,2);
	}

	public void setThird(Double third) {
		this.third = third;
	}
	
	
}
