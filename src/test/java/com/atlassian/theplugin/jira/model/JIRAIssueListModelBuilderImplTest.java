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

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.api.*;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class JIRAIssueListModelBuilderImplTest extends TestCase {

	private JIRATestServerFacade facade;

	@Override
	public void setUp() throws Exception {
		facade = new JIRATestServerFacade();
		super.setUp();
	}

	public void testAddIssuesOnUninitialized() {
		JIRAIssueListModel model = new JIRAIssueListModelImpl();
		JIRAIssueListModelBuilder builder = new JIRAIssueListModelBuilderImpl(null);
		builder.setModel(model);
		((JIRAIssueListModelBuilderImpl) builder).setFacade(facade);
		JiraServerCfg server = new JiraServerCfg("test", new ServerId());
		List<JIRAQueryFragment> query = new ArrayList<JIRAQueryFragment>();
		try {
			builder.addIssuesToModel(new JIRAManualFilter("manual filter", query), server, 2, true);
			assertEquals(0, model.getIssues().size());
//			builder.setServer(server);
			assertEquals(0, model.getIssues().size());
		} catch (JIRAException e) {
			fail("JIRA exception? How come?");
		}
	}

	public void testAddSavedFilterIssues() {
		JIRAIssueListModel model = new JIRAIssueListModelImpl();
		JIRAIssueListModelBuilder builder = new JIRAIssueListModelBuilderImpl(null);
		builder.setModel(model);
		((JIRAIssueListModelBuilderImpl) builder).setFacade(facade);
		JiraServerCfg server = new JiraServerCfg("test", new ServerId());
		JIRASavedFilterBean savedFilter = new JIRASavedFilterBean("test", 0);
		try {
			builder.addIssuesToModel(savedFilter, server, 25, true);
			builder.addIssuesToModel(savedFilter, server, 25, false);
			builder.addIssuesToModel(savedFilter, server, 25, false);
		} catch (JIRAException e) {
			fail("JIRA exception? How come?");
		}
		assertEquals(75, model.getIssues().size());
	}

	public void testAddCustomFilterIssues() {
		JIRAIssueListModel model = new JIRAIssueListModelImpl();
		JIRAIssueListModelBuilder builder = new JIRAIssueListModelBuilderImpl(null);
		builder.setModel(model);

		((JIRAIssueListModelBuilderImpl) builder).setFacade(facade);
		JiraServerCfg server = new JiraServerCfg("test", new ServerId());
		List<JIRAQueryFragment> query = new ArrayList<JIRAQueryFragment>();
		query.add(new JIRAProjectBean());
		JIRAManualFilter manualFilter = new JIRAManualFilter("manual filter", query);
		try {
			builder.addIssuesToModel(manualFilter, server, 25, true);
			builder.addIssuesToModel(manualFilter, server, 25, false);
		} catch (JIRAException e) {
			fail("JIRA exception? How come?");
		}
		assertEquals(50, model.getIssues().size());
	}

	private boolean listenerCalled;

	public void testListeners() {
		final JIRAIssueListModel model = new JIRAIssueListModelImpl();
		JIRAIssueListModelBuilder builder = new JIRAIssueListModelBuilderImpl(null);
		builder.setModel(model);

		((JIRAIssueListModelBuilderImpl) builder).setFacade(facade);
		JiraServerCfg server = new JiraServerCfg("test", new ServerId());
		List<JIRAQueryFragment> query = new ArrayList<JIRAQueryFragment>();
		query.add(new JIRAProjectBean());
		model.addModelListener(new JIRAIssueListModelListener() {
			public void modelChanged(JIRAIssueListModel m) {
				if (model == m) {
					listenerCalled = true;
				}
			}

			public void issuesLoaded(JIRAIssueListModel model, int loadedIssues) {

			}
		});
		try {
			builder.addIssuesToModel(new JIRAManualFilter("manula filter", query), server, 25, true);
		} catch (JIRAException e) {
			fail("JIRA exception? How come?");
		}
		assertTrue(listenerCalled);
		listenerCalled = false;
	}

	public void testReset() {
		JIRAIssueListModel model = new JIRAIssueListModelImpl();
		JIRAIssueListModelBuilder builder = new JIRAIssueListModelBuilderImpl(null);
		builder.setModel(model);
		((JIRAIssueListModelBuilderImpl) builder).setFacade(facade);
		JiraServerCfg server = new JiraServerCfg("test", new ServerId());
		JIRASavedFilterBean savedFilter = new JIRASavedFilterBean("test", 0);
		try {
			builder.addIssuesToModel(savedFilter, server, 25, true);
			builder.addIssuesToModel(savedFilter, server, 25, false);
			builder.addIssuesToModel(savedFilter, server, 25, false);
		} catch (JIRAException e) {
			fail("JIRA exception? How come?");
		}
		assertEquals(75, model.getIssues().size());
		builder.reset();
		assertEquals(0, model.getIssues().size());
	}

	private class JIRATestServerFacade implements JIRAServerFacade {

		private int idx;
		private JIRAIssueBean proto = new JIRAIssueBean();

		private List<JIRAIssue> createIssueList(int size) {
			List<JIRAIssue> list = new ArrayList<JIRAIssue>();
			for (int i = 0; i < size; ++i) {
				proto.setKey("A-" + Long.valueOf(idx).toString());
				++idx;
				JIRAIssueBean issue = new JIRAIssueBean(proto);
				list.add(issue);
			}
			return list;
		}

		public void testServerConnection(String url, String userName, String password) throws RemoteApiException {
		}

		public void testServerConnection(final ServerCfg serverCfg) throws RemoteApiException {
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

		public List<JIRAConstant> getSubtaskIssueTypes(JiraServerCfg server) throws JIRAException {
			return null;
		}

		public List<JIRAConstant> getSubtaskIssueTypesForProject(JiraServerCfg server, String project) throws JIRAException {
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

		public void progressWorkflowAction(final JiraServerCfg server, final JIRAIssue issue, final JIRAAction action,
				final List<JIRAActionField> fields)
				throws JIRAException {

		}

		public void addComment(JiraServerCfg server, String issueKey, String comment) throws JIRAException {
		}

		public JIRAIssue createIssue(JiraServerCfg server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public JIRAIssue getIssueDetails(JiraServerCfg server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public JIRAIssue getIssueUpdate(JiraServerCfg server, JIRAIssue issue) throws JIRAException {
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

		public JIRAIssue getIssue(JiraServerCfg server, String key) throws JIRAException {
			return null;
		}
	}
}
