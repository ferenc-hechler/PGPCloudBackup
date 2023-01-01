package de.hechler.decryptdownloader;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.hechler.encrypt.utils.KeepPeriods;

class KeepPeriodsTest {

	
	@Test
	void testKeepPeriods() {
		checkKeepPeriods("1d1w1m1y");
		checkKeepPeriods("1d2w3m4y");
		checkKeepPeriods("4d3w2m1y");
		checkKeepPeriods("10d11w12m13y");
		checkKeepPeriods("*d*w*m*y");
		checkKeepPeriods("0d0w0m0y", "");
		checkKeepPeriods("01d02w03m04y", "1d2w3m4y");
		checkKeepPeriods("4y3m2w1d", "1d2w3m4y");
		checkKeepPeriods("12d", "12d");
		checkKeepPeriods("12w", "12w");
		checkKeepPeriods("12m", "12m");
		checkKeepPeriods("12y", "12y");
		checkKeepPeriods("7d1y", "7d1y");
	}
	
	private void checkKeepPeriods(String testPeriods) {
		checkKeepPeriods(testPeriods, testPeriods);
	}

	private void checkKeepPeriods(String testPeriods, String expectedResult) {
		KeepPeriods kp = new KeepPeriods(testPeriods);
		assertEquals(expectedResult, kp.toString());
	}

	@Test
	void testFilter() {
		checkKeepDates("*d", "2022-12-26", 3, "[2022-12-26, 2022-12-27, 2022-12-28]");
		checkKeepDates("2d", "2022-12-26", 10, "[2023-01-03, 2023-01-04]");
		checkRemoveDates("2d", "2022-12-26", 10, "[2022-12-26, 2022-12-27, 2022-12-28, 2022-12-29, 2022-12-30, 2022-12-31, 2023-01-01, 2023-01-02]");
		checkKeepDates("2w", "2022-12-26", 70, "[2023-02-20, 2023-02-27]");
		checkKeepDates("2m", "2022-12-26", 365, "[2023-11-01, 2023-12-01]");
		checkKeepDates("2y", "2022-12-26", 3650, "[2031-01-01, 2032-01-01]");
		checkKeepDates("2d2w", "2022-12-26", 70, "[2023-02-20, 2023-02-27, 2023-03-04, 2023-03-05]");
		checkKeepDates("2d2w", "2023-02-20", 14, "[2023-02-20, 2023-02-27, 2023-03-04, 2023-03-05]");
		checkRemoveDates("2d2w", "2023-02-20", 14, "[2023-02-21, 2023-02-22, 2023-02-23, 2023-02-24, 2023-02-25, 2023-02-26, 2023-02-28, 2023-03-01, 2023-03-02, 2023-03-03]");
		checkKeepDates("7d4w3m*y", "2022-12-26", 1500, "[2022-12-26, 2023-01-01, 2024-01-01, 2025-01-01, 2026-01-01, 2026-12-01, 2027-01-01, 2027-01-11, 2027-01-18, 2027-01-25, 2027-01-27, 2027-01-28, 2027-01-29, 2027-01-30, 2027-01-31, 2027-02-01, 2027-02-02]");

		Set<String> keepDates = new HashSet<>(checkKeepDates("7d4w3m*y", "2022-12-26", 1500, null));
		Set<String> removeDates = new HashSet<>(checkRemoveDates("7d4w3m*y", "2022-12-26", 1500, null));
		assertEquals(keepDates.size(), 17);
		assertEquals(removeDates.size(), 1500-17);
		keepDates.removeAll(removeDates);
		assertEquals(keepDates.size(), 17);
	}

	private List<String> checkKeepDates(String periodStr, String startDate, int count, String expectedResult) {
		KeepPeriods kp = new KeepPeriods(periodStr);
		Date date = KeepPeriods.parseDate(startDate);
		List<String> dates = new ArrayList<>();
		for (int d=0; d<count; d++) {
			dates.add(KeepPeriods.formatDate(date));
			date = new Date(date.getTime()+1000*60*60*24);
		}
		List<String> keepDates = kp.filterKeep(dates);
		if (expectedResult != null) {
			assertEquals(expectedResult, keepDates.toString());
		}
		return keepDates;
	}
	private List<String> checkRemoveDates(String periodStr, String startDate, int count, String expectedResult) {
		KeepPeriods kp = new KeepPeriods(periodStr);
		Date date = KeepPeriods.parseDate(startDate);
		List<String> dates = new ArrayList<>();
		for (int d=0; d<count; d++) {
			dates.add(KeepPeriods.formatDate(date));
			date = new Date(date.getTime()+1000*60*60*24);
		}
		List<String> removeDates = kp.filterRemove(dates);
		if (expectedResult != null) {
			assertEquals(expectedResult, removeDates.toString());
		}
		return removeDates;
	}

}
