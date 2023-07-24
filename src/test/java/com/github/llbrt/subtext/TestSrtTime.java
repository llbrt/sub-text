package com.github.llbrt.subtext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class TestSrtTime {

	@ParameterizedTest
	@ValueSource(strings = {
			// Invalid separator
			"00:04:03,775--> 00:04:06,090",
			"00:04:03,775 - -> 00:04:06,090",
			"00:04:03,775 -> 00:04:06,090",
			"00:04:03,775 -- 00:04:06,090",
			"00:04:03,775 -->00:04:06,090",
			// Start after end
			"00:14:03,775 --> 00:04:06,090",
	})
	void invalidSrtTime(String srtTime) {
		assertThrows(IllegalArgumentException.class, () -> SrtTime.readSrtTimeValue(1, srtTime));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			// Invalid start
			"00:4:03,775 --> 00:04:06,090",
			// Invalid end
			"00:04:03,775 --> 00:4:06,090",
			"00:04:03,775 --> 00:4:06,09",
	})
	void invalidTimeFormat(String srtTime) {
		assertThrows(DateTimeParseException.class, () -> SrtTime.readSrtTimeValue(1, srtTime));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"00:04:03,775 --> 00:04:06,090",
			"00:04:00,775 --> 00:04:06,000",
			"00:00:00,000 --> 01:00:00,010",
	})
	void testToString(String srtTime) {
		assertEquals(srtTime, SrtTime.readSrtTimeValue(1, srtTime).toString());
	}

	@Test
	void overlapSelf() {
		var value = SrtTime.readSrtTimeValue(1, "00:04:03,775 --> 00:04:06,090");
		assertTrue(value.overlap(value));
	}

	@ParameterizedTest
	@CsvSource(delimiter = ';', value = {
			// Same end
			"00:04:03,774 --> 00:04:06,090; 00:04:03,775 --> 00:04:06,090",
			// Same start
			"00:04:03,775 --> 00:04:06,091; 00:04:03,775 --> 00:04:06,090",
			// v1 includes v2
			"00:04:03,775 --> 00:14:06,091; 00:05:03,775 --> 00:06:06,090",
			// Other
			"00:00:03,775 --> 00:00:06,091; 00:00:04,775 --> 00:01:00,090",
	})
	void doOverlap(String srtTime1, String srtTime2) {
		var value1 = SrtTime.readSrtTimeValue(1, srtTime1);
		var value2 = SrtTime.readSrtTimeValue(1, srtTime2);

		assertTrue(value1.overlap(value2));
		assertTrue(value2.overlap(value1));
	}

	@ParameterizedTest
	@CsvSource(delimiter = ';', value = {
			"00:04:03,774 --> 00:04:06,090; 00:14:03,775 --> 00:14:06,090",
	})
	void dontOverlap(String srtTime1, String srtTime2) {
		var value1 = SrtTime.readSrtTimeValue(1, srtTime1);
		var value2 = SrtTime.readSrtTimeValue(1, srtTime2);

		assertFalse(value1.overlap(value2));
		assertFalse(value2.overlap(value1));
	}

	@Test
	void compare() {
		var before = SrtTime.readSrtTimeValue(1, "00:04:03,774 --> 00:04:06,090");
		var after = SrtTime.readSrtTimeValue(1, "00:14:03,775 --> 00:14:06,090");
		var sameStart = SrtTime.readSrtTimeValue(1, "00:14:03,775 --> 00:14:07,090");

		assertTrue(before.compareTo(after) < 0);
		assertTrue(after.compareTo(before) > 0);

		assertTrue(after.compareTo(sameStart) < 0);
		assertTrue(sameStart.compareTo(after) > 0);

		assertEquals(0, after.compareTo(after));
	}

	@Test
	void sort() {
		var before = SrtTime.readSrtTimeValue(1, "00:04:03,774 --> 00:04:06,090");
		var after = SrtTime.readSrtTimeValue(1, "00:14:03,775 --> 00:14:06,090");
		var sameStart = SrtTime.readSrtTimeValue(1, "00:14:03,775 --> 00:14:07,090");

		var list = new ArrayList<SrtTime.Value>();
		list.add(sameStart);
		list.add(before);
		list.add(after);
		list.add(after);
		list.sort(SrtTime.COMPARATOR);

		assertEquals(before, list.get(0));
		assertEquals(after, list.get(1));
		assertEquals(after, list.get(2));
		assertEquals(sameStart, list.get(3));
	}

	@Test
	void extendEnd() {
		var time = SrtTime.readSrtTimeValue(1, "00:04:03,774 --> 00:04:06,090");
		var extended = time.extend(123_456);
		assertEquals("00:04:03,774 --> 00:06:09,546", extended.toString());
	}

	@Test
	void shift() {
		var time = SrtTime.readSrtTimeValue(1, "00:04:03,774 --> 00:04:06,090");
		var extended = time.shift(123_456);
		assertEquals("00:06:07,230 --> 00:06:09,546", extended.toString());
	}

	@Test
	void shiftBackward() {
		var time = SrtTime.readSrtTimeValue(1, "00:04:03,774 --> 00:04:06,090");
		var extended = time.shift(-123_456);
		assertEquals("00:02:00,318 --> 00:02:02,634", extended.toString());
	}
}
