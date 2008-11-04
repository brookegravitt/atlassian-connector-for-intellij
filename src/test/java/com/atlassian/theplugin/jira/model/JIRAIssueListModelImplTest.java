/*
  Copyright (C) 2008 Atlassian
 
  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.theplugin.jira.model;

import junit.framework.TestCase;
import com.atlassian.theplugin.jira.api.JIRAIssueBean;
import com.atlassian.theplugin.jira.api.JIRAIssue;

import java.util.ArrayList;
import java.util.List;

public class JIRAIssueListModelImplTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAddOne() {
	    JIRAIssueListModel model = JIRAIssueListModelImpl.createInstance();
	    model.addIssue(new JIRAIssueBean());
	    assertEquals(1, model.getIssues().size());
    }

	public void testAddMany() {
		JIRAIssueListModel model = JIRAIssueListModelImpl.createInstance();
		List<JIRAIssue> list = new ArrayList<JIRAIssue>();
		list.add(new JIRAIssueBean());
		list.add(new JIRAIssueBean());
		list.add(new JIRAIssueBean());
		list.add(new JIRAIssueBean());
		model.addIssues(list);
		assertEquals(list.size(), model.getIssues().size());
	}

	public void testClear() {
		JIRAIssueListModel model = JIRAIssueListModelImpl.createInstance();
		model.addIssue(new JIRAIssueBean());
		assertEquals(1, model.getIssues().size());
		model.clear();
		assertEquals(0, model.getIssues().size());
	}

	private	boolean listenerCalled = false;

	public void testListeners() {
		final JIRAIssueListModel model = JIRAIssueListModelImpl.createInstance();
		JIRAIssueListModelListener l = new JIRAIssueListModelListener() {
			public void modelChanged(JIRAIssueListModel m) {
				if (model == m) {
					listenerCalled = true;
				}
			}
		};

		model.addModelListener(l);
		model.notifyListeners();
		assertTrue(listenerCalled);
		listenerCalled = false;
		model.removeModelListener(l);
		assertFalse(listenerCalled);
	}
}
