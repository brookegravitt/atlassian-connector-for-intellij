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
package com.atlassian.theplugin.idea.bamboo.tree;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import java.util.Date;

/**
 * @author Jacek Jaroczynski
 */
public enum DatePeriod {
	TODAY("Today"),
	YESTERDAY("Yesterday"),
	LAST_WEEK("Last Week"),
	LAST_MONTH("Last Month"),
	OLDER("Older");

	private String name;
	private static final int DAYS_IN_WEEK = 7;

	DatePeriod(String name) {
		this.name = name;
	}

	public static DatePeriod getBuilDate(Date aDate) {

		DateTime date = new DateTime(aDate);

		DateMidnight midnight = new DateMidnight();

		if (date.isAfter(midnight)) {
			return TODAY;
		} else if (date.isAfter(midnight.minusDays(1))) {
			return YESTERDAY;
		} else if (date.isAfter(midnight.minusDays(DAYS_IN_WEEK))) {
			return LAST_WEEK;
		} else if (date.isAfter(midnight.minusMonths(1))) {
			return LAST_MONTH;
		} else {
			return OLDER;
		}
	}

	public String toString() {
		return name;
	}
}
