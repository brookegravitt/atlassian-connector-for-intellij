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

import com.atlassian.connector.commons.api.HttpConnectionCfg;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.JiraServerFacade;
import com.atlassian.theplugin.commons.jira.api.JIRAAction;
import com.atlassian.theplugin.commons.jira.api.JIRAActionField;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.api.commons.JIRAIssue;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAAttachment;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAComment;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAComponentBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAConstant;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAPriorityBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAProject;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAProjectBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAQueryFragment;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAResolutionBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRASavedFilterBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAUserBean;
import com.atlassian.theplugin.commons.jira.api.commons.beans.JIRAVersionBean;
import com.atlassian.theplugin.commons.jira.api.commons.rss.JIRAException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class JIRAIssueListModelBuilderImplTest extends TestCase {

	private JIRATestServerFacade2 facade;

	@Override
	public void setUp() throws Exception {
		facade = new JIRATestServerFacade2();
		super.setUp();
	}

	public void testAddIssuesOnUninitialized() {
		JIRAIssueListModel model = new JIRAIssueListModelImpl();
		JIRAIssueListModelBuilder builder = new JIRAIssueListModelBuilderImpl(null);
		builder.setModel(model);
		((JIRAIssueListModelBuilderImpl) builder).setFacade(facade);
		JiraServerData server = createServerData();
		List<JIRAQueryFragment> query = new ArrayList<JIRAQueryFragment>();
		try {
			builder.addIssuesToModel(
                    new JiraCustomFilter(UUID.randomUUID().toString(), "manual filter", query), server, 2, true);
			assertEquals(0, model.getIssues().size());
//			builder.setHttpConnectionCfg(server);
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
		JiraServerData server = createServerData();
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

	private JiraServerData createServerData() {
		return new JiraServerData(new JiraServerCfg(true, "test", new ServerIdImpl(), true) {
			@Override
			public ServerType getServerType() {
				return null;
			}

			@Override
			public JiraServerCfg getClone() {
				return null;
			}
		}, "", "", true);
	}

	public void testAddCustomFilterIssues() {
		JIRAIssueListModel model = new JIRAIssueListModelImpl();
		JIRAIssueListModelBuilder builder = new JIRAIssueListModelBuilderImpl(null);
		builder.setModel(model);

		((JIRAIssueListModelBuilderImpl) builder).setFacade(facade);
		JiraServerData server = createServerData();
		List<JIRAQueryFragment> query = new ArrayList<JIRAQueryFragment>();
		query.add(new JIRAProjectBean());
		JiraCustomFilter manualFilter = new JiraCustomFilter(UUID.randomUUID().toString(), "manual filter", query);
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
		JiraServerData server = createServerData();
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

			public void issueUpdated(final JiraIssueAdapter issue) {
			}
		});
		try {
			builder.addIssuesToModel(
                    new JiraCustomFilter(UUID.randomUUID().toString(), "manula filter", query), server, 25, true);
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
		JiraServerData server = createServerData();
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

	private class JIRATestServerFacade2 implements JiraServerFacade {

		private int idx;
        private final JiraServerData jiraServerData = new JiraServerData(new Server() {
            public ServerIdImpl getServerId() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getName() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getUrl() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isEnabled() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isUseDefaultCredentials() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getUsername() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getPassword() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public ServerType getServerType() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        }, new UserCfg() {}, true);

        private final JiraIssueAdapter proto = new JiraIssueAdapter(jiraServerData
        );
		private List<JiraIssueAdapter> createIssueList(int size) {
			List<JiraIssueAdapter> list = new ArrayList<JiraIssueAdapter>();
			for (int i = 0; i < size; ++i) {
				proto.setKey("A-" + Long.valueOf(idx).toString());
				++idx;
				JiraIssueAdapter issue = new JiraIssueAdapter(proto);
				list.add(issue);
			}
			return list;
		}

		public void testServerConnection(JiraServerData server) throws RemoteApiException {
		}

        public void testServerConnection(HttpConnectionCfg httpConnectionCfg) throws RemoteApiException {
            
        }

        public ServerType getServerType() {
			return null;
		}

		public List<JiraIssueAdapter> getIssues(JiraServerData server, List<JIRAQueryFragment> query,
				String sort, String sortOrder, int start, int size)
				throws JIRAException {
			return createIssueList(size);
		}

		public List<JiraIssueAdapter> getSavedFilterIssues(JiraServerData server, List<JIRAQueryFragment> query,
				String sort, String sortOrder, int start, int size)
				throws JIRAException {
			return createIssueList(size);
		}

		public List<JIRAProject> getProjects(JiraServerData server) throws JIRAException {
			return null;
		}

		public List<JIRAConstant> getIssueTypes(JiraServerData server) throws JIRAException {
			return null;
		}

		public List<JIRAConstant> getStatuses(JiraServerData server) throws JIRAException {
			return null;
		}

		public List<JIRAConstant> getIssueTypesForProject(JiraServerData server, String project) throws JIRAException {
			return null;
		}

		public List<JIRAConstant> getSubtaskIssueTypes(JiraServerData server) throws JIRAException {
			return null;
		}

		public List<JIRAConstant> getSubtaskIssueTypesForProject(JiraServerData server, String project) throws JIRAException {
			return null;
		}

		public List<JIRAQueryFragment> getSavedFilters(JiraServerData server) throws JIRAException {
			return null;
		}

		public List<JIRAComponentBean> getComponents(JiraServerData server, String projectKey) throws JIRAException {
			return null;
		}

        public Collection<JIRAAttachment> getIssueAttachements(JiraServerData server, JIRAIssue issue)
                throws JIRAException {
            return null;
        }

        public List<JIRAVersionBean> getVersions(JiraServerData server, String projectKey) throws JIRAException {
			return null;
		}

		public List<JIRAPriorityBean> getPriorities(JiraServerData server) throws JIRAException {
			return null;
		}

		public List<JIRAResolutionBean> getResolutions(JiraServerData server) throws JIRAException {
			return null;
		}

		public List<JIRAAction> getAvailableActions(JiraServerData server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public List<JIRAActionField> getFieldsForAction(JiraServerData server, JIRAIssue issue, JIRAAction action)
				throws JIRAException {
			return null;
		}

		public void progressWorkflowAction(JiraServerData server, JIRAIssue issue, JIRAAction action)
				throws JIRAException {
		}

		public void progressWorkflowAction(final JiraServerData server, final JIRAIssue issue, final JIRAAction action,
				final List<JIRAActionField> fields)
				throws JIRAException {

		}

		public void addComment(JiraServerData server, String issueKey, String comment) throws JIRAException {
		}

		public JiraIssueAdapter createIssue(JiraServerData server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public JiraIssueAdapter getIssueDetails(JiraServerData server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public JiraIssueAdapter getIssueUpdate(JiraServerData server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public void logWork(JiraServerData server, JIRAIssue issue, String timeSpent, Calendar startDate, String comment,
				boolean updateEstimate, String newEstimate) throws JIRAException {
		}

		public void setAssignee(JiraServerData server, JIRAIssue issue, String assignee) throws JIRAException {
		}

		public JIRAUserBean getUser(JiraServerData server, String loginName) throws JIRAException {
			return null;
		}

		public List<JIRAComment> getComments(JiraServerData server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public JiraIssueAdapter getIssue(JiraServerData server, String key) throws JIRAException {
			return null;
		}
	}
}
