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

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.jira.JIRAAction;
import com.atlassian.connector.commons.jira.JIRAActionField;
import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.connector.commons.jira.beans.*;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.JiraServerFacade;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
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
		});
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
                return null;
            }

            public String getName() {
                return null;
            }

            public String getUrl() {
                return null;
            }

            public boolean isEnabled() {
                return false;
            }

            public boolean isUseDefaultCredentials() {
                return false;
            }

            public String getUsername() {
                return null;
            }

            public String getPassword() {
                return null;
            }

            public ServerType getServerType() {
                return null;
            }

            public boolean isDontUseBasicAuth() {
                return false;
            }

            public UserCfg getBasicHttpUser() {
                return null;
            }

            public boolean isShared() {
                return false;
            }

            public void setShared(boolean global) {
            }
        });

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

        @Override
        public void reset() {
        }

        public void testServerConnection(JiraServerData server) throws RemoteApiException {
		}

        public void testServerConnection(ConnectionCfg httpConnectionCfg) throws RemoteApiException {

        }

        public ServerType getServerType() {
			return null;
		}

        public List<JIRASecurityLevelBean> getSecurityLevels(JiraServerData jiraServerData, String projectKey) throws RemoteApiException, JIRAException {
            return null;
        }

        public void setReporter(JiraServerData jiraServerData, JIRAIssue issue, String reporter) throws JIRAException {            
        }

		public void setSummary(JiraServerData jiraServerData, JIRAIssue issue, String summary) throws JIRAException {
		}

		public void setDescription(JiraServerData jiraServerData, JIRAIssue issue, String description) throws JIRAException {
		}

		public void setType(JiraServerData jiraServerData, JIRAIssue issue, String type) throws JIRAException {			
		}

		public void setPriority(JiraServerData jiraServerData, JIRAIssue issue, String priority) throws JIRAException {
		}

		public void setAffectedVersions(JiraServerData jiraServerData, JIRAIssue issue, String[] versions) throws JIRAException {
		}

		public void setFixVersions(JiraServerData jiraServerData, JIRAIssue issue, String[] versions) throws JIRAException {
		}

		public void setFields(JiraServerData jiraServerData, JIRAIssue issue, List<JIRAActionField> fields) throws JIRAException {
		}

//		public List<JiraIssueAdapter> getIssues(JiraServerData server, List<JIRAQueryFragment> query,
//				String sort, String sortOrder, int start, int size)
//				throws JIRAException {
//			return createIssueList(size);
//		}

//        public List<JiraIssueAdapter> getIssues(JiraServerData jiraServerData, String queryString,
//                                                String sort, String sortOrder, int start, int size) throws JIRAException {
//            return createIssueList(size);
//        }


        @Override
        public List<JiraIssueAdapter> getIssues(
                JiraServerData jiraServerData, JiraFilter filter, String sort, String sortOrder, int start, int size)
                throws JIRAException {
            return createIssueList(size);
        }

//        public List<JiraIssueAdapter> getSavedFilterIssues(JiraServerData server, List<JIRAQueryFragment> query,
//				String sort, String sortOrder, int start, int size)
//				throws JIRAException {
//			return createIssueList(size);
//		}


        @Override
        public List<JiraIssueAdapter> getSavedFilterIssues(
                JiraServerData jiraServerData, JIRASavedFilter filter, String sort, String sortOrder, int start, int size)
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

        @Override
        public List<JIRAConstant> getIssueTypesForProject(JiraServerData jiraServerData, long projectId, String projectKey) throws JIRAException {
            return null;
        }

		public List<JIRAConstant> getSubtaskIssueTypes(JiraServerData server) throws JIRAException {
			return null;
		}

        @Override
        public List<JIRAConstant> getSubtaskIssueTypesForProject(JiraServerData jiraServerData, long projectId, String projectKey) throws JIRAException {
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

		public void addAttachment(JiraServerData jiraServerData, String issueKey, String name, byte[] contents)
				throws JIRAException {
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
