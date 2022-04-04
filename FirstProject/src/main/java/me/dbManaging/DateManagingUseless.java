package me.dbManaging;

import org.joda.time.DateTime;

/**
 * Useless class because I didn't we could use LocalDate instead of DateTime
 * @author taiga
 *
 */
public class DateManagingUseless {

	/**
	 * Transform for example 7 to 07 to get a better date displaying
	 * 
	 * @param string number to transform
	 * @return return the same number with a 0 at the beginning if the number's
	 *         length is equal to 1
	 */
	protected static String addAZero(String string) {
		if (string.length() == 1) {
			try {
				Integer.parseInt(string);
				return "0" + string;
			} catch (NumberFormatException nfe) {
				System.err.println(nfe.getMessage());
				return string;
			}
		}
		return string;
	}

	/**
	 * Transform for example 07 to 7 to use it to create a DateTime
	 * 
	 * @param string
	 * @return
	 */
	protected static String removeZero(String string) {
		try {
			Integer.parseInt(string);
			if (string.substring(0, 1) == "0" && string.length() == 2) {
				return string.substring(1);
			}
			return string;
		} catch (NumberFormatException nfe) {
			System.err.println(nfe.getMessage());
			return string;
		}
	}

	/**
	 * Transform a DateTime to a list of String to display
	 * 
	 * @param date
	 * @return
	 */
	public static String[] dateToString(DateTime date) {
		String[] stringDate = new String[2];
		String dayName = date.dayOfWeek().getAsText();
		stringDate[0] = dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
		String monthOfYear = addAZero(date.monthOfYear().getAsString());
		String dayOfMonth = addAZero(date.dayOfMonth().getAsString());
		stringDate[1] = date.year().getAsString() + "-" + monthOfYear + "-" + dayOfMonth;
		return stringDate;
	}
}
