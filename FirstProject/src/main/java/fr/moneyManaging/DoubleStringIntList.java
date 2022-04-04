package fr.moneyManaging;

public class DoubleStringIntList {
	private Double firstElement;
	private String secondElement;
	private int thirdElement;

	public DoubleStringIntList(Double firstElement, String secondElement, int thirdElement) {
		super();
		this.firstElement = firstElement;
		this.secondElement = secondElement;
		this.thirdElement = thirdElement;
	}

	public Double getFirstElement() {
		return firstElement;
	}

	public void setFirstElement(Double firstElement) {
		this.firstElement = firstElement;
	}

	public String getSecondElement() {
		return secondElement;
	}

	public void setSecondElement(String secondElement) {
		this.secondElement = secondElement;
	}

	public int getThirdElement() {
		return thirdElement;
	}

	public void setThirdElement(int thirdElement) {
		this.thirdElement = thirdElement;
	}

}
