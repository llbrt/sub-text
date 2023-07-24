package com.github.llbrt.subtext;

import static java.util.Collections.unmodifiableList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TimeSegments {
	private static final Logger logger = LoggerFactory.getLogger(TimeSegments.class);

	private final @Nullable Path file;
	private final List<SrtTime.Value> segments = new ArrayList<>();

	TimeSegments(@Nullable Path file) {
		this.file = file;
	}

	List<SrtTime.Value> segments() {
		return unmodifiableList(segments);
	}

	void load() throws IOException {
		segments.clear();
		if (file == null) {
			// Nothing to do
			return;
		}

		var fileLines = Files.readAllLines(file);
		if (fileLines.isEmpty()) {
			// Nothing to do
			return;
		}

		fileLines = Utils.sanitize(fileLines);

		for (int i = 0; i < fileLines.size(); i++) {
			var timeSegment = fileLines.get(i);
			try {
				segments.add(SrtTime.readSrtTimeValue(i, timeSegment));
			} catch (DateTimeParseException e) {
				throw new IllegalArgumentException("Line #" + i + ": unexpected time interval: " + timeSegment, e);
			}
		}
		checkLoadedSegments();

		logger.info("Found {} time segment(s)", segments.size());
	}

	private void checkLoadedSegments() {

		// Make sure segments are sorted and don't overlap
		segments.sort(SrtTime.COMPARATOR);
		var current = segments.get(0);
		for (int i = 1; i < segments.size(); i++) {
			final var next = segments.get(i);
			if (current.overlap(next)) {
				throw new IllegalArgumentException("'" + current + "' overlap with '" + next + "'");
			}
			current = next;
		}
	}
}
