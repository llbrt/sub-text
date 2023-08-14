package com.github.llbrt.subtext;

import static com.github.llbrt.subtext.Utils.duration;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Collections.unmodifiableList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.llbrt.subtext.SrtTime.Value;

final class SrtFile {
	private static final Logger logger = LoggerFactory.getLogger(SrtFile.class);

	private final Path file;
	private final List<SrtText> texts = new ArrayList<>();

	SrtFile(Path file) {
		this.file = file;
	}

	SrtFile(Path file, List<SrtText> texts) {
		this.file = file;
		this.texts.addAll(texts);
	}

	void load(boolean verifyCount) throws IOException {
		texts.clear();
		var fileLines = Files.readAllLines(file);
		if (fileLines.isEmpty()) {
			// Nothing to do
			logger.info("File empty");
			return;
		}

		fileLines = Utils.sanitize(fileLines);

		int currentId = 0;
		for (int i = 0; i < fileLines.size(); i++) {
			// Should remain at least 3 lines
			if ((fileLines.size() - i) < 3) {
				throw new IllegalArgumentException("Line #" + i + ": unexpected file line count");
			}
			int nextId = Integer.valueOf(fileLines.get(i));
			if (verifyCount && nextId != (currentId + 1)) {
				throw new IllegalArgumentException("Line #" + i + ": unexpected index=" + nextId);
			}
			currentId++;

			var timeSegment = fileLines.get(++i);

			var subtitle = new ArrayList<String>();
			do {
				if ((i + 1) == fileLines.size()) {
					throw new IllegalArgumentException("Line #" + i + ": unexpected end of file");
				}
				var line = fileLines.get(++i);
				if (line.isBlank()) {
					break;
				}
				subtitle.add(line);
			} while (true);

			texts.add(newSrtText(i, currentId, timeSegment, subtitle));
		}

		checkTexts();

		logger.info("Found {} elements", texts.size());
	}

	void save() throws IOException {
		checkTexts();

		// Write contents to the destination file
		try (var writer = Files.newBufferedWriter(file, CREATE, TRUNCATE_EXISTING)) {
			for (int i = 0; i < texts.size(); i++) {
				var text = texts.get(i);
				writer.write(Integer.toString(i + 1));
				writer.newLine();
				writer.write(text.time().toString());
				writer.newLine();
				for (var line : text.texts()) {
					writer.write(line);
					writer.newLine();
				}
				writer.newLine();
			}
		}

		logger.info("Wrote {} elements in '{}'", texts.size(), file);
	}

	/**
	 * Extends display time by <code>duration</code> milliseconds.
	 *
	 * @param duration extension in milliseconds.
	 */
	void extendTexts(int duration) {
		for (int i = 0; i < texts.size(); i++) {
			texts.set(i, texts.get(i).extend(duration));
		}
		// Verify text after update
		checkTexts();
		logger.info("Display time extended by {} milliseconds", duration);
	}

	/**
	 * Shift start time by <code>delta</code> milliseconds. May be positive or negative.
	 *
	 * @param delta shift in milliseconds.
	 */
	void shiftTexts(int delta) {
		for (int i = 0; i < texts.size(); i++) {
			texts.set(i, texts.get(i).shift(delta));
		}
		// Verify text after update
		logger.info("Display start time shifted by {} milliseconds", delta);
	}

	private void checkTexts() {
		// Make sure texts are sorted and don't overlap
		texts.sort(SrtText.COMPARATOR);
		var current = texts.get(0);
		for (int i = 1; i < texts.size(); i++) {
			final var next = texts.get(i);
			if (current.time().overlap(next.time())) {
				throw new IllegalArgumentException("Text #" + current.count() + " overlap with next one");
			}
			current = next;
		}
	}

	List<SrtText> extractTimeSegments(TimeSegments ts) {
		var segments = ts.segments();
		if (segments.isEmpty() || texts.isEmpty()) {
			return unmodifiableList(texts);
		}

		var result = new ArrayList<SrtText>();
		var delta = Duration.ofNanos(0);

		var iterator = texts.listIterator();
		for (Value value : segments) {

			var nextDelta = delta.plus(duration(value));

			// Go to the next text inside the interval
			while (true) {
				var currentText = nextSrtText(iterator, value);
				if (currentText == null || !currentText.time().overlap(value)) {
					break;
				}

				// Get new start/end times
				var start = LocalTime.of(0, 0).plus(delta);
				var duration = duration(currentText.time());
				if (value.start().isBefore(currentText.time().start())) {
					start = start.plus(duration(value.start(), currentText.time().start()));
				} else {
					logger.warn("Display time of text #{} reduced (start)", currentText.count());
					duration = duration.minus(duration(currentText.time().start(), value.start()));
				}

				LocalTime end;
				if (currentText.time().end().isBefore(value.end())) {
					end = start.plus(duration);
				} else {
					end = LocalTime.of(0, 0).plus(nextDelta);
					logger.warn("Display time of text #{} reduced (end)", currentText.count());
				}

				result.add(new SrtText(result.size() + 1, new SrtTime.Value(start, end), currentText.texts()));
			}

			// Update delta for the next segment
			delta = nextDelta;
		}

		logger.info("New text list ready, {} texts dropped", texts.size() - result.size());

		return result;
	}

	private SrtText nextSrtText(ListIterator<SrtText> iterator, SrtTime.Value value) {
		while (iterator.hasNext()) {
			var currentText = iterator.next();
			if (currentText.time().overlap(value)) {
				return currentText;
			}
			if (currentText.time().start().isAfter(value.end())) {
				// After the current time segment, maybe ok for the next segment
				iterator.previous();
				break;
			}
		}
		return null;
	}

	private SrtText newSrtText(int line, int count, String timeInterval, List<String> text) {
		if (text.isEmpty()) {
			throw new IllegalArgumentException("Line #" + line + ": no text found");
		}
		try {
			Value value = SrtTime.readSrtTimeValue(line, timeInterval);
			return new SrtText(count, value, text);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Line #" + line + ": unexpected time interval: " + timeInterval, e);
		}
	}
}
