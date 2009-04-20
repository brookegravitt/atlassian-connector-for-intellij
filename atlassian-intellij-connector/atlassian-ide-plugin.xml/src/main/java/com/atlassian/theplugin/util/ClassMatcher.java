/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.theplugin.util;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Code partially borrowed from http://forums.sun.com/thread.jspa?messageID=2462435
 */
public final class ClassMatcher {
	private static final String ID_START = "[A-Za-z_$]";
	private static final String ID_PART = "[A-Za-z0-9_$]";

	// Note the aggressive closures (*+, ++) on the next two: we never
	// want the matcher to back off and try to match a shorter string
	// once it's identified a class name.
	private static final String ID = ID_START + ID_PART + "*+";
	private static final String FQCN = ID + "(?:\\." + ID + ")++";

	private static final String FQCN_PART = "[A-Za-z0-9_$.]";

	private ClassMatcher() {
	}

	static String makeRegex() {
		StringBuffer sb = new StringBuffer();

		// This prevents any match attempt from starting within a class name,
		// to eliminate erroneous matches on partial class names.
		sb.append("(?<!" + FQCN_PART + ")");

		// Positive lookahead for a class name ensures that we're not wasting
		// our time, while leaving the matcher positioned for the negative
		// lookahead that follows.  The class name is captured to \1.
		sb.append("(?=(" + FQCN + "))");

		// Now we "consume" the class name to move the matcher to the end.
		sb.append("\\1");

		return sb.toString();
	}

	private static final Pattern PATTERN = Pattern.compile(makeRegex());

	public static class MatchInfo {
		private final String match;
		private final int index;

		public MatchInfo(final String match, final int index) {
			this.match = match;
			this.index = index;
		}

		public int getIndex() {
			return index;
		}

		public String getMatch() {
			return match;
		}
	}

	public static Iterable<MatchInfo> find(final String input) {
		final Matcher m = PATTERN.matcher(input);
		return new Iterable<MatchInfo>() {
			public Iterator<MatchInfo> iterator() {
				return new Iterator<MatchInfo>() {
					private boolean isNext = m.find();
					public boolean hasNext() {
						return isNext;
					}

					public MatchInfo next() {
						final String res = m.group();
						final int index = m.start();
						isNext = m.find();
						return new MatchInfo(res, index);
					}

					public void remove() {
						throw new UnsupportedOperationException("Operation not supported");
					}
				};
			}


		};
	}


	public static void main(String[] args) {
		String test = "blah blah blah "
				+ "com.mycompany.projectname.thislayer.MyTestClass "
				+ "com.mycompany.projectname.test.MyClass "
				+ "com.mycompany.projectname.thislayer.MyClass "
				+ "arrayVar.length "
				+ "method.call() "
				+ "that will be $37.50, please.";

		for (MatchInfo s : find(test)) {
			System.out.println(s.getIndex() + ": " + s.getMatch());
		}
	}
}
