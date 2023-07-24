package com.github.llbrt.subtext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

class TestSrtFileProcessor {

	private static final IExecutionExceptionHandler HANDLER_NO_OUTPUT = new IExecutionExceptionHandler() {
		@Override
		public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult) throws Exception {
			return commandLine.getCommandSpec().exitCodeOnExecutionException();
		}
	};

	@TempDir
	public static Path tempDirRoot;
	public static Path srtFileSource;

	@BeforeAll
	static void copySrtFile() throws IOException {
		srtFileSource = Resources.copySrtFile(tempDirRoot, "file");
	}

	@Test
	// Command without any operation
	void noOperation() throws Exception {
		SrtFileProcessor app = new SrtFileProcessor();
		CommandLine cmd = new CommandLine(app);
		int exitCode = cmd.execute(srtFileSource.toString());
		assertEquals(0, exitCode);

		// Should have written into source + ".new.srt"
		Path destination = Path.of(srtFileSource.toString() + ".new.srt");
		assertTrue(Files.isRegularFile(destination));

		// Should be identical as source
		assertEquals(-1, Files.mismatch(srtFileSource, destination));
	}

	@Test
	// Extend display time
	void extendTime() throws Exception {
		SrtFileProcessor app = new SrtFileProcessor();
		CommandLine cmd = new CommandLine(app);
		Path destination = Files.createTempFile(tempDirRoot, "dest", ".srt");
		int exitCode = cmd.execute("-e", "1001", "-o", destination.toString(), srtFileSource.toString());
		assertEquals(0, exitCode);

		assertTrue(Files.isRegularFile(destination));
		var extended = Resources.srtFilePath("file-extended");
		assertEquals(-1, Files.mismatch(extended, destination));
	}

	@Test
	// Extend display time causes overlap
	void extendTimeWithOverlap_fails() throws Exception {
		SrtFileProcessor app = new SrtFileProcessor();
		CommandLine cmd = new CommandLine(app);
		Path destination = Files.createTempFile(tempDirRoot, "dest", ".srt");
		int exitCode = cmd
				.setExecutionExceptionHandler(HANDLER_NO_OUTPUT)
				.execute("-e", "10001", "-o", destination.toString(), srtFileSource.toString());
		assertEquals(1, exitCode);
	}

	@Test
	// Extend display time
	void shift() throws Exception {
		SrtFileProcessor app = new SrtFileProcessor();
		CommandLine cmd = new CommandLine(app);
		Path destination = Files.createTempFile(tempDirRoot, "dest", ".srt");
		int exitCode = cmd.execute("-s", "1001", "-o", destination.toString(), srtFileSource.toString());
		assertEquals(0, exitCode);

		assertTrue(Files.isRegularFile(destination));
		var shifted = Resources.srtFilePath("file-shifted");
		assertEquals(-1, Files.mismatch(shifted, destination));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			// 1 segment, 2 texts
			"segment-text2",
			// 2 segments, 2 texts
			"segment2-text2",
			// 3 segments, 3 texts
			"segment3-text3",
			// Segment truncate start
			"segment2-text2-trunc-start",
			// Segment truncate end
			"segment2-text2-trunc-end",
	})
	void withSegments(String keyword) throws Exception {
		SrtFileProcessor app = new SrtFileProcessor();
		CommandLine cmd = new CommandLine(app);
		Path destination = Files.createTempFile(tempDirRoot, "dest", ".srt");
		Path segments = Resources.segmentsPath(keyword);
		int exitCode = cmd.execute("-o", destination.toString(), srtFileSource.toString(), segments.toString());
		assertEquals(0, exitCode);

		assertTrue(Files.isRegularFile(destination));
		var referenceFile = Resources.srtFilePath("file-" + keyword);
		assertEquals(-1, Files.mismatch(referenceFile, destination));
	}
}
