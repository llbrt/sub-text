package com.github.llbrt.subtext;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class TestUtils {

	@Test
	void localTimeDuration() {
		var t1 = LocalTime.of(1, 2, 33);
		var t2 = LocalTime.of(2, 3, 3);
		var d = Utils.duration(t1, t2);
		assertEquals(3630, d.getSeconds());
		assertEquals(0, d.getNano());
	}

	@Test
	void srtTimeValueDuration() {
		var value = SrtTime.readSrtTimeValue(1, "00:04:02,175 --> 00:04:13,130");
		var d = Utils.duration(value);
		assertEquals(10, d.getSeconds());
		assertEquals(955_000_000, d.getNano());
	}
}
