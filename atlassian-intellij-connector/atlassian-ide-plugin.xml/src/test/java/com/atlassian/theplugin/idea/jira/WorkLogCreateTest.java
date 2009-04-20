/*
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea.jira;

import junit.framework.TestCase;

import java.util.Date;

public class WorkLogCreateTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetFormatedDurationStringForNow() {
		String res = WorkLogCreateAndMaybeDeactivateDialog.getFormatedDurationString(new Date());
	    assertTrue(res.length() == 0);
    }

	public void testGetFormatedDurationStringForHourAgo() {
		Date d = new Date();
		d.setTime(d.getTime() - 1000 * 3600);
		String res = WorkLogCreateAndMaybeDeactivateDialog.getFormatedDurationString(d);
		assertEquals(res, "1h");
	}

	public void testGetFormatedDurationStringForHalfAnHourAgo() {
		Date d = new Date();
		d.setTime(d.getTime() - 1000 * 1800);
		String res = WorkLogCreateAndMaybeDeactivateDialog.getFormatedDurationString(d);
		assertEquals(res, "30m");
	}

	public void testGetFormatedDurationStringForTwoAndAHalfAnHourAgo() {
		Date d = new Date();
		d.setTime(d.getTime() - 1000 * (1800 + 2 * 3600));
		String res = WorkLogCreateAndMaybeDeactivateDialog.getFormatedDurationString(d);
		assertEquals(res, "2h 30m");
	}

	public void testGetFormatedDurationStringForTooLongTimeAgo() {
		Date d = new Date();
		d.setTime(d.getTime() - 1000 * 3600 * 24);
		String res = WorkLogCreateAndMaybeDeactivateDialog.getFormatedDurationString(d);
		assertTrue(res.length() == 0);
	}

}
