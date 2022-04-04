package fr.moneyManaging;

import org.joda.time.LocalDate;

public class DateIntStringList {
	LocalDate m_date = new LocalDate();
	int m_int = 0;
	String m_string = "";

	public DateIntStringList(LocalDate m_date, int m_int, String m_string) {
		super();
		this.m_date = m_date;
		this.m_int = m_int;
		this.m_string = m_string;
	}

	public LocalDate getM_date() {
		return m_date;
	}

	public void setM_date(LocalDate m_date) {
		this.m_date = m_date;
	}

	public int getM_int() {
		return m_int;
	}

	public void setM_int(int m_int) {
		this.m_int = m_int;
	}

	public String getM_string() {
		return m_string;
	}

	public void setM_string(String m_string) {
		this.m_string = m_string;
	}

}
