package com.github.llbrt.subtext;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TestTimeSegments {

	@ParameterizedTest
	@ValueSource(strings = {
			"empty",
	})
	void loadSuccess(String fileName) throws Exception {
		var segments = Resources.createTestTimeSegments(fileName);
		segments.load();
	}

	@Test
	void loadNullSuccess() throws Exception {
		var segments = new TimeSegments(null);
		segments.load();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"err-separator",
			"err-text",
			"err-time",
			"err-time-overlap"
	})
	void loadFailing(String fileName) throws Exception {
		var segments = Resources.createTestTimeSegments(fileName);
		assertThrows(IllegalArgumentException.class, () -> segments.load());
	}
}
