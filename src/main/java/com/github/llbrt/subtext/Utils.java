package com.github.llbrt.subtext;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

final class Utils {

	private static final String UTF8_BOM = String.valueOf(new char[] { 0xFEFF });

	static List<String> sanitize(List<String> fileLines) {
		// Remove UTF-8 BOM if any
		var result = fileLines;
		var first = fileLines.get(0);
		if (first.startsWith(UTF8_BOM)) {
			// Clone to mutable list
			result = new ArrayList<>(fileLines);
			result.set(0, first.substring(UTF8_BOM.length()));
		}
		return result;
	}

	static Duration duration(SrtTime.Value value) {
		return duration(value.start(), value.end());
	}

	static Duration duration(LocalTime before, LocalTime after) {
		assert before.isBefore(after);
		return Duration.ofNanos(after.getLong(ChronoField.NANO_OF_DAY) - before.getLong(ChronoField.NANO_OF_DAY));
	}
}
