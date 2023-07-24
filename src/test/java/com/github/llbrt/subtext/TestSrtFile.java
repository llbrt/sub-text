package com.github.llbrt.subtext;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TestSrtFile {

	@ParameterizedTest
	@ValueSource(strings = {
			"empty",
			"with-bom",
	})
	void loadSuccess(String fileName) throws Exception {
		var srtFile = Resources.createTestSrtFile(fileName);
		srtFile.load();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"err-end",
			"err-count",
			"err-text",
			"err-time",
			"err-time-overlap"
	})
	void loadFailing(String fileName) throws Exception {
		var srtFile = Resources.createTestSrtFile(fileName);
		assertThrows(IllegalArgumentException.class, () -> srtFile.load());
	}
}
