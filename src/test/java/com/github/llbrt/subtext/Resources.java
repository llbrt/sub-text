package com.github.llbrt.subtext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

class Resources {

	private static final String TEST_RESOURCES = "src/test/resources";

	static Path srtFilePath(String fileName) {
		return Path.of(TEST_RESOURCES + "/srtfiles/" + fileName + ".srt");
	}

	static Path copySrtFile(Path destDir, String fileName) throws IOException {
		var file = srtFilePath(fileName);
		Path copy = destDir.resolve(fileName + "-copy.srt");
		Files.copy(file, copy, StandardCopyOption.REPLACE_EXISTING);
		return copy;
	}

	static SrtFile createTestSrtFile(String fileName) {
		var file = srtFilePath(fileName);
		return new SrtFile(file);
	}

	static Path segmentsPath(String fileName) {
		return Path.of(TEST_RESOURCES + "/segments/" + fileName + ".segments");
	}

	static TimeSegments createTestTimeSegments(String fileName) {
		var file = segmentsPath(fileName);
		return new TimeSegments(file);
	}
}
