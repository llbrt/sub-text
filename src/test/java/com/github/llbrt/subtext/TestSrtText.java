package com.github.llbrt.subtext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.Collections;

import org.junit.jupiter.api.Test;

class TestSrtText {

	@Test
	void extendEnd() {
		var time = SrtTime.readSrtTimeValue(1, "00:04:03,774 --> 00:04:06,090");
		var srtText = new SrtText(1, time, Collections.singletonList("text"));
		var extended = srtText.extend(123_456);
		assertNotSame(srtText, extended);
		assertEquals("00:04:03,774 --> 00:06:09,546", extended.time().toString());
	}

	@Test
	void shift() {
		var time = SrtTime.readSrtTimeValue(1, "00:04:03,774 --> 00:04:06,090");
		var srtText = new SrtText(1, time, Collections.singletonList("text"));
		var extended = srtText.shift(123_456);
		assertNotSame(srtText, extended);
		assertEquals("00:06:07,230 --> 00:06:09,546", extended.time().toString());
	}
}
