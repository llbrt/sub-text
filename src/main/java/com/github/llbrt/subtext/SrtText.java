package com.github.llbrt.subtext;

import java.util.Comparator;
import java.util.List;

record SrtText(int count, SrtTime.Value time, List<String> texts) {

	SrtText extend(int milliseconds) {
		return new SrtText(count, time.extend(milliseconds), texts);
	}

	SrtText shift(int milliseconds) {
		return new SrtText(count, time.shift(milliseconds), texts);
	}

	static final Comparator<SrtText> COMPARATOR = new Comparator<SrtText>() {

		@Override
		public int compare(SrtText t1, SrtText t2) {
			return t1.time.compareTo(t2.time);
		}
	};
}
