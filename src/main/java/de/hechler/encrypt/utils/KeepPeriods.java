package de.hechler.encrypt.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KeepPeriods {

	private int days;
	private int weeks;
	private int months;
	private int years;

	private static final SimpleDateFormat SDF_YMD = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat SDF_YW = new SimpleDateFormat("yyyy-ww");
	
	public KeepPeriods(String periods) {
		days = parseUnit(periods, 'd');
		weeks = parseUnit(periods, 'w');
		months = parseUnit(periods, 'm');
		years = parseUnit(periods, 'y');
	}

	private int parseUnit(String periods, char unit) {
		int endPos = periods.indexOf(unit);
		if (endPos == -1) {
			return 0;
		}
		int startPos = endPos;
		if ((startPos > 0) && periods.charAt(startPos - 1) == '*') {
			return -1;
		}
		while ((startPos > 0) && isDigit(periods.charAt(startPos - 1))) {
			startPos--;
		}
		return Integer.parseInt(periods.substring(startPos, endPos));
	}

	private static boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	@Override
	public String toString() {
		return p2s(days, "d") + p2s(weeks, "w") + p2s(months, "m") + p2s(years, "y");
	}

	private String p2s(int n, String unit) {
		if (n == 0) {
			return "";
		}
		return i2s(n) + unit;
	}

	private static String i2s(int n) {
		if (n == -1) {
			return "*";
		}
		return Integer.toString(n);
	}
	
	public List<String> filterRemove(Collection<String> dates) {
		Set<String> result = new HashSet<>(dates);
		result.removeAll(filterKeep(dates));
		return sort(result);
	}

	public List<String> filterKeep(Collection<String> dates) {
		List<String> dayDates = new ArrayList<>();
		List<String> weekDates = new ArrayList<>();
		List<String> monthDates = new ArrayList<>();
		List<String> yearDates = new ArrayList<>();
		
		List<String> sortedDates = sort(dates);
		for (String dateStr:sortedDates) {
			if (!dateStr.matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]")) {
				throw new RuntimeException("invalid date format YYYY-MM-DD expected: '"+ dateStr+"'");
			}
			if (differentDay(dateStr, last(dayDates))) {
				addDate(dayDates, dateStr, days);
			}
			if (differentWeek(dateStr, last(weekDates))) {
				addDate(weekDates, dateStr, weeks);
			}
			if (differentMonth(dateStr, last(monthDates))) {
				addDate(monthDates, dateStr, months);
			}
			if (differentYear(dateStr, last(yearDates))) {
				addDate(yearDates, dateStr, years);
			}
		}
		
		Set<String> result = new HashSet<>();
		result.addAll(dayDates);
		result.addAll(weekDates);
		result.addAll(monthDates);
		result.addAll(yearDates);
		return sort(result);
	}

	private String last(List<String> list) {
		if (list.size() == 0) {
			return null;
		}
		return list.get(list.size()-1);
	}

	private List<String> sort(Collection<String> unorted) {
		List<String> sort = new ArrayList<>(unorted);
		Collections.sort(sort);
		return sort;
	}

	private void addDate(List<String> dates, String dateStr, int maxCount) {
		if (maxCount == 0) {
			return;
		}
		dates.add(dateStr);
		if (maxCount == -1) {
			return;
		}
		if (dates.size() > maxCount) {
			dates.remove(0);
		}
	}

	private boolean differentDay(String dateStr1, String dateStr2) {
		if (dateStr2 == null) {
			return true;
		}
		return !dateStr1.equals(dateStr2);
	}
	private boolean differentWeek(String dateStr1, String dateStr2) {
		if (dateStr2 == null) {
			return true;
		}
		String weekStr1 = formatWeek(parseDate(dateStr1));
		String weekStr2 = formatWeek(parseDate(dateStr2));
		return !weekStr1.equals(weekStr2);
	}
	private boolean differentMonth(String dateStr1, String dateStr2) {
		if (dateStr2 == null) {
			return true;
		}
		String monthStr1 = dateStr1.substring(0, 7);
		String monthStr2 = dateStr2.substring(0, 7);
		return !monthStr1.equals(monthStr2);
	}
	private boolean differentYear(String dateStr1, String dateStr2) {
		if (dateStr2 == null) {
			return true;
		}
		String yearStr1 = dateStr1.substring(0, 4);
		String yearStr2 = dateStr2.substring(0, 4);
		return !yearStr1.equals(yearStr2);
	}

	public static synchronized Date parseDate(String dateStr) {
		try {
			return SDF_YMD.parse(dateStr);
		} catch (ParseException e) {
			throw new RuntimeException(e.toString(), e);
		}
	}
	public static synchronized String formatDate(Date date) {
		return SDF_YMD.format(date);
	}
	public static synchronized String formatWeek(Date date) {
		return SDF_YW.format(date);
	}
	

}
