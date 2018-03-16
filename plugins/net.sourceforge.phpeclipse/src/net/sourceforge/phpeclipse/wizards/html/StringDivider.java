/*
 * $Id: StringDivider.java,v 1.2 2006-10-21 23:18:43 pombredanne Exp $
 * Copyright Narushima Hironori. All rights reserved.
 */
package net.sourceforge.phpeclipse.wizards.html;

import java.util.Arrays;
import java.util.regex.Pattern;

public class StringDivider {

	static Pattern tagNameChoosePattern = Pattern
			.compile("<[\\s/]*(\\w+)\\s*.*>");

	String[] splitRegexpCandidates = { "\t", ",", "\\s", "\\s+", };

	public StringDivider() {
	}

	public String[][] divide(String content) {
		return divide(content, getDivideSuitedRegexp(content));
	}

	public String[][] divide(String content, String regexp) {
		String[] lines = content.split("\n");
		int len = lines.length;
		String[][] dist = new String[len][];

		int max = Integer.MIN_VALUE;
		for (int i = 0; i < len; i++) {
			String line = lines[i];
			String[] cells = line.split(regexp);
			dist[i] = cells;
			if (max < cells.length) {
				max = cells.length;
			}
		}
		for (int i = 0; i < len; i++) {
			String[] newArray = new String[max];
			Arrays.fill(newArray, "");
			System.arraycopy(dist[i], 0, newArray, 0, dist[i].length);
			dist[i] = newArray;
		}
		return dist;
	}

	public String getDivideSuitedRegexp(String content) {
		String[] lines = content.split("\n");

		String resultRegexp = null;
		int score = Integer.MAX_VALUE, cellCount = Integer.MIN_VALUE;

		for (int i = 0; i < splitRegexpCandidates.length; i++) {
			String regexp = splitRegexpCandidates[i];
			int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;
			for (int j = 0; j < lines.length; j++) {
				String[] vals = lines[j].split(regexp);
				if (max < vals.length) {
					max = vals.length;
				}
				if (min > vals.length) {
					min = vals.length;
				}
			}
			int s = max - min;
			if (score > s || (score == s && max > cellCount)) {
				cellCount = max;
				score = s;
				resultRegexp = regexp;
			}
		}
		return resultRegexp;
	}

}
