package com.github.llbrt.subtext;

import java.nio.file.Path;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExecutionException;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

@Command(name = "subfileprocessor")
public final class SrtFileProcessor implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(SrtFileProcessor.class);

	@Option(names = { "-e", "--extend" }, description = "Extend subtitle duration (in milliseconds)", defaultValue = "0")
	private int durationIncrement;

	@Option(names = { "-s", "--shift" }, description = "Shift subtitle start (in milliseconds)", defaultValue = "0")
	private int shiftStart;

	@Option(names = { "-o", "--output" }, description = "Output file")
	private Optional<Path> outputFile;

	@Parameters(index = "0", description = "Path to the subtitle file")
	private Path subtitles;

	@Parameters(index = "1", arity = "0..1", description = "File containing the list of segments to keep (one per line, same time format as srt file)")
	private Path segments;

	@Spec
	private CommandSpec spec;

	@Override
	public void run() {
		// Load file
		var inputFile = new SrtFile(subtitles);
		try {
			inputFile.load();
		} catch (Exception e) {
			logger.error("Failed to load '{}'", subtitles, e);
			throw new ExecutionException(spec.commandLine(), "Invalid input file");
		}

		// Load time segments
		var timeSegments = new TimeSegments(segments);
		try {
			timeSegments.load();
		} catch (Exception e) {
			logger.error("Failed to load '{}'", segments, e);
			throw new ExecutionException(spec.commandLine(), "Invalid input file");
		}

		if (durationIncrement > 0) {
			// Update subtitle display length
			try {
				inputFile.extendTexts(durationIncrement);
			} catch (Exception e) {
				logger.error("Failed to extend subtitles of '{}'", subtitles, e);
				throw new ExecutionException(spec.commandLine(), "Duration increment too large");
			}
		}
		if (shiftStart != 0) {
			// Update subtitle start/end times
			try {
				inputFile.shiftTexts(shiftStart);
			} catch (Exception e) {
				logger.error("Failed to shift subtitles of '{}'", subtitles, e);
				throw new ExecutionException(spec.commandLine(), "Shifting subtitles failed");
			}
		}

		var newTexts = inputFile.extractTimeSegments(timeSegments);
		var destination = outputFile.orElse(subtitles.resolveSibling(subtitles.getFileName().toString() + ".new.srt"));
		var newSrt = new SrtFile(destination, newTexts);
		try {
			newSrt.save();
		} catch (Exception e) {
			logger.error("Failed to save new texts to '{}'", destination, e);
			throw new ExecutionException(spec.commandLine(), "Invalid contents or output file");
		}
	}

	public static void main(String... args) {
		int exitCode = new CommandLine(new SrtFileProcessor())
				.setExecutionExceptionHandler(new IExecutionExceptionHandler() {
					@Override
					public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult) throws Exception {
						logger.error(ex.getMessage());
						return commandLine.getCommandSpec().exitCodeOnExecutionException();
					}

				})
				.execute(args);
		System.exit(exitCode);
	}
}
