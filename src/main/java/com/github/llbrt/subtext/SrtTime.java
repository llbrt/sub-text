package com.github.llbrt.subtext;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

final class SrtTime {

	private static final String TIME_SERARATOR = " --> ";

	static final Comparator<SrtTime.Value> COMPARATOR = new Comparator<SrtTime.Value>() {

		@Override
		public int compare(Value v1, Value v2) {
			return v1.compareTo(v2);
		}
	};

	static record Value(LocalTime start, LocalTime end) implements Comparable<SrtTime.Value> {

		Value extend(int milliseconds) {
			return new Value(start, end.plus(milliseconds, ChronoUnit.MILLIS));
		}

		Value shift(int milliseconds) {
			return new Value(start.plus(milliseconds, ChronoUnit.MILLIS), end.plus(milliseconds, ChronoUnit.MILLIS));
		}

		boolean overlap(Value other) {
			return !(end.isBefore(other.start) || other.end.isBefore(start));
		}

		@Override
		public int compareTo(SrtTime.Value o) {
			int startCompare = start.compareTo(o.start);
			if (startCompare == 0) {
				return end.compareTo(o.end);
			}
			return startCompare;
		}

		@Override
		public String toString() {
			return FORMATTER.format(start) + TIME_SERARATOR + FORMATTER.format(end);
		}
	};

	// Expected format: hours:minutes:seconds,milliseconds
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss,SSS");

	static SrtTime.Value readSrtTimeValue(int line, String timeText) {
		var times = timeText.split(TIME_SERARATOR);
		if (times.length != 2) {
			throw new IllegalArgumentException("Line #" + line + ": unexpected timestamp: " + timeText);
		}
		var start = LocalTime.parse(times[0], FORMATTER);
		var end = LocalTime.parse(times[1], FORMATTER);
		if (end.isBefore(start)) {
			throw new IllegalArgumentException("Line #" + line + ": invalid timestamp: " + timeText + " (end before start)");
		}
		return new Value(start, end);
	}
}
