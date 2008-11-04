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

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.api.*;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.ServerType;

import java.util.List;
import java.util.Calendar;
import java.util.ArrayList;

public class JIRAIssueListModelBuilderImplTest extends TestCase {

	private JIRATestServerFacade facade;

    public void setUp() throws Exception {
	    facade = new JIRATestServerFacade();
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAddIssuesOnUninitialized() {
	    JIRAIssueListModel model = JIRAIssueListModelImpl.createInstance();
	    JIRAIssueListModelBuilder builder = JIRAIssueListModelBuilderImpl.createInstance();
	    ((JIRAIssueListModelBuilderImpl) builder).setFacade(facade);
	    try {
		    builder.addIssuesToModel(model, 2);
		    assertEquals(0, model.getIssues().size());
		    builder.setServer(new JiraServerCfg("test", new ServerId()));
		    assertEquals(0, model.getIssues().size());
	    } catch (JIRAException e) {
		    fail("JIRA exception? How come?");
	    }
    }

	public void testAddSavedFilterIssues() {
		JIRAIssueListModel model = JIRAIssueListModelImpl.createInstance();
		JIRAIssueListModelBuilder builder = JIRAIssueListModelBuilderImpl.createInstance();
		((JIRAIssueListModelBuilderImpl) builder).setFacade(facade);
		builder.setServer(new JiraServerCfg("test", new ServerId()));
		builder.setSavedFilter(new JIRASavedFilterBean("test", 0));
		try {
			builder.addIssuesToModel(model, 25);
			builder.addIssuesToModel(model, 25);
			builder.addIssuesToModel(model, 25);
		} catch (JIRAException e) {
			fail("JIRA exception? How come?");
		}
		assertEquals(75, model.getIssues().size());
	}

	public void testAddCustomFilterIssues() {
		JIRAIssueListModel model = JIRAIssueListModelImpl.createInstance();
		JIRAIssueListModelBuilder builder = JIRAIssueListModelBuilderImpl.createInstance();
		((JIRAIssueListModelBuilderImpl) builder).setFacade(facade);
		builder.setServer(new JiraServerCfg("test", new ServerId()));
		List<JIRAQueryFragment> query = new ArrayList<JIRAQueryFragment>();
		query.add(new JIRAProjectBean());
		builder.setCustomFilter(query);
		try {
			builder.addIssuesToModel(model, 25);
			builder.addIssuesToModel(model, 25);
		} catch (JIRAException e) {
			fail("JIRA exception? How come?");
		}
		assertEquals(50, model.getIssues().size());
	}

	private	boolean listenerCalled = false;

	public void testListeners() {
		final JIRAIssueListModel model = JIRAIssueListModelImpl.createInstance();
		JIRAIssueListModelBuilder builder = JIRAIssueListModelBuilderImpl.createInstance();
		((JIRAIssueListModelBuilderImpl) builder).setFacade(facade);
		builder.setServer(new JiraServerCfg("test", new ServerId()));
		List<JIRAQueryFragment> query = new ArrayList<JIRAQueryFragment>();
		query.add(new JIRAProjectBean());
		builder.setCustomFilter(query);
		model.addModelListener(new JIRAIssueListModelListener() {
			public void modelChanged(JIRAIssueListModel m) {
				if (model == m) {
					listenerCalled = true;
				}
			}
		});
		try {
			builder.addIssuesToModel(model, 25);
		} catch (JIRAException e) {
			fail("JIRA exception? How come?");
		}
		assertTrue(listenerCalled);
		listenerCalled = false;
	}

	public void testReset() {
		JIRAIssueListModel model = JIRAIssueListModelImpl.createInstance();
		JIRAIssueListModelBuilder builder = JIRAIssueListModelBuilderImpl.createInstance();
		((JIRAIssueListModelBuilderImpl) builder).setFacade(facade);
		builder.setServer(new JiraServerCfg("test", new ServerId()));
		builder.setSavedFilter(new JIRASavedFilterBean("test", 0));
		try {
			builder.addIssuesToModel(model, 25);
			builder.addIssuesToModel(model, 25);
			builder.addIssuesToModel(model, 25);
		} catch (JIRAException e) {
			fail("JIRA exception? How come?");
		}
		assertEquals(75, model.getIssues().size());
		builder.reset(model);
		assertEquals(0, model.getIssues().size());
	}

	private class JIRATestServerFacade implements JIRAServerFacade {

		private List<JIRAIssue> createIssueList(int size) {
			List<JIRAIssue> list = new ArrayList<JIRAIssue>();
			for (int i = 0; i < size; ++i) {
				JIRAIssueBean issue = new JIRAIssueBean();
				list.add(issue);
			}
			return list;
		}

		public void testServerConnection(String url, String userName, String password) throws RemoteApiException {
		}

		public ServerType getServerType() {
			return null;
		}

		public List<JIRAIssue> getIssues(JiraServerCfg server, List<JIRAQueryFragment> query,
		                                 String sort, String sortOrder, int start, int size)
				throws JIRAException {
			return createIssueList(size);
		}

		public List<JIRAIssue> getSavedFilterIssues(JiraServerCfg server, List<JIRAQueryFragment> query,
		                                            String sort, String sortOrder, int start, int size)
				throws JIRAException {
			return createIssueList(size);
		}

		public List<JIRAProject> getProjects(JiraServerCfg server) throws JIRAException {
			return null;
		}

		public List<JIRAConstant> getIssueTypes(JiraServerCfg server) throws JIRAException {
			return null;
		}

		public List<JIRAConstant> getStatuses(JiraServerCfg server) throws JIRAException {
			return null;
		}

		public List<JIRAConstant> getIssueTypesForProject(JiraServerCfg server, String project) throws JIRAException {
			return null;
		}

		public List<JIRAQueryFragment> getSavedFilters(JiraServerCfg server) throws JIRAException {
			return null;
		}

		public List<JIRAComponentBean> getComponents(JiraServerCfg server, String projectKey) throws JIRAException {
			return null;
		}

		public List<JIRAVersionBean> getVersions(JiraServerCfg server, String projectKey) throws JIRAException {
			return null;
		}

		public List<JIRAConstant> getPriorities(JiraServerCfg server) throws JIRAException {
			return null;
		}

		public List<JIRAResolutionBean> getResolutions(JiraServerCfg server) throws JIRAException {
			return null;
		}

		public List<JIRAAction> getAvailableActions(JiraServerCfg server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public List<JIRAActionField> getFieldsForAction(JiraServerCfg server, JIRAIssue issue, JIRAAction action)
				throws JIRAException {
			return null;
		}

		public void progressWorkflowAction(JiraServerCfg server, JIRAIssue issue, JIRAAction action)
				throws JIRAException {
		}

		public void addComment(JiraServerCfg server, JIRAIssue issue, String comment) throws JIRAException {
		}

		public JIRAIssue createIssue(JiraServerCfg server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public JIRAIssue getIssueDetails(JiraServerCfg server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public void logWork(JiraServerCfg server, JIRAIssue issue, String timeSpent, Calendar startDate, String comment,
		                    boolean updateEstimate, String newEstimate) throws JIRAException {
		}

		public void setAssignee(JiraServerCfg server, JIRAIssue issue, String assignee) throws JIRAException {
		}

		public JIRAUserBean getUser(JiraServerCfg server, String loginName) throws JIRAException {
			return null;
		}

		public List<JIRAComment> getComments(JiraServerCfg server, JIRAIssue issue) throws JIRAException {
			return null;
		}

	}
}
