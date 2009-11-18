package com.atlassian.theplugin.jira.model;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.api.HttpConnectionCfg;
import com.atlassian.connector.commons.jira.JIRAAction;
import com.atlassian.connector.commons.jira.JIRAActionField;
import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.connector.commons.jira.beans.JIRAAttachment;
import com.atlassian.connector.commons.jira.beans.JIRAComment;
import com.atlassian.connector.commons.jira.beans.JIRAComponentBean;
import com.atlassian.connector.commons.jira.beans.JIRAConstant;
import com.atlassian.connector.commons.jira.beans.JIRAPriorityBean;
import com.atlassian.connector.commons.jira.beans.JIRAProject;
import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;
import com.atlassian.connector.commons.jira.beans.JIRAResolutionBean;
import com.atlassian.connector.commons.jira.beans.JIRAUserBean;
import com.atlassian.connector.commons.jira.beans.JIRAVersionBean;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.JiraServerFacade;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModelImpl;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.configuration.JiraViewConfigurationBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: pmaruszak
 */
public class JIRAFilterListModelBuilderTest extends TestCase {
	private JIRAFilterListBuilder builder;
	private JIRAFilterListModel listModel;
	private Map<JiraServerCfg, List<JIRAQueryFragment>> savedFilters;
	private JIRAServerModelImpl serverModel;
    private JiraServerData jiraServerData;

	@Override
	public void setUp() throws Exception {
		super.setUp();
        jiraServerData = new JiraServerData(new Server(){
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
                return ServerType.JIRA_SERVER;
            }
        }, new UserCfg(), true);
		savedFilters = new HashMap<JiraServerCfg, List<JIRAQueryFragment>>();

		final JIRATestServerFacade2 facade = new JIRATestServerFacade2();


		serverModel = new JIRAServerModelIdea();
		serverModel.setFacade(facade);
		fillServersAndFilters(savedFilters);

		JiraWorkspaceConfiguration jiraCfg = new JiraWorkspaceConfiguration();
		fillJiraCfg(jiraCfg);

		builder = new JIRAFilterListBuilder();
		listModel = new JIRAFilterListModel();
		builder.setListModel(listModel);
		builder.setJiraWorkspaceCfg(jiraCfg);
	}

	public void fillJiraCfg(JiraWorkspaceConfiguration jiraCfg) {
		JiraViewConfigurationBean viewBean = new JiraViewConfigurationBean();
		viewBean.setViewFilterId("none");
		viewBean.setViewServerIdd(new ServerIdImpl());
		jiraCfg.setView(viewBean);

	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	// todo change JiraServerCfg to ServerData if possible
	void fillServersAndFilters(Map<JiraServerCfg, List<JIRAQueryFragment>> aSavedFilters)
			throws RemoteApiException {
		for (int i = 0; i < 3; i++) {
			JiraServerCfg server = new JiraServerCfg("jiraserver" + 1, new ServerIdImpl(), true);
			aSavedFilters.put(server, new ArrayList<JIRAQueryFragment>());
			try {
				serverModel.getResolutions(ServerDataProvider.getServerData(server), true);
			} catch (JIRAException e) {
			}
		}
	}

	public void testRebuildModel() {
		try {
			builder.rebuildModel(serverModel);
		} catch (JIRAFilterListBuilder.JIRAServerFiltersBuilderException e) {
			fail(); //we do not expect exception
		}
		assertEquals(3, listModel.getJIRAServers().size());
		JiraServerCfg jiraServer = savedFilters.keySet().iterator().next();
		assertEquals(listModel.getSavedFilters(ServerDataProvider.getServerData(jiraServer)).size(),
				savedFilters.get(jiraServer).size());

	}

	class JIRATestServerFacade2 implements JiraServerFacade {

		private List<JiraIssueAdapter> createIssueList(int size) {
			List<JiraIssueAdapter> list = new ArrayList<JiraIssueAdapter>();
			for (int i = 0; i < size; ++i) {
				JiraIssueAdapter issue = new JiraIssueAdapter(jiraServerData);
				list.add(issue);
			}
			return list;
		}

		public void testServerConnection(final JiraServerData jiraServerData) throws RemoteApiException {
		}

        public void testServerConnection(HttpConnectionCfg httpConnectionCfg) throws RemoteApiException {
        }

        public void testServerConnection(ConnectionCfg httpConnectionCfg) throws RemoteApiException {
            
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

		public List<JIRAProject> getProjects(JiraServerData jiraServerData) throws JIRAException {
			return null;
		}

		public List<JIRAConstant> getIssueTypes(JiraServerData jiraServerData) throws JIRAException {
			return null;
		}

		public List<JIRAConstant> getStatuses(JiraServerData jiraServerData) throws JIRAException {
			return null;
		}

		public List<JIRAConstant> getIssueTypesForProject(JiraServerData jiraServerData, String project) throws JIRAException {
			return null;
		}

		public List<JIRAConstant> getSubtaskIssueTypes(JiraServerData jiraServerData) throws JIRAException {
			return null;
		}

		public List<JIRAConstant> getSubtaskIssueTypesForProject(JiraServerData jiraServerData, String project)
                throws JIRAException {
			return null;
		}

        public Collection<JIRAAttachment> getIssueAttachements(JiraServerData jiraServerData, JIRAIssue issue) throws JIRAException {
            return null;
        }

        public List<JIRAQueryFragment> getSavedFilters(JiraServerData jiraServerData) throws JIRAException {
			List<JIRAQueryFragment> list = new ArrayList<JIRAQueryFragment>();
			for (JiraServerCfg serverCfg : savedFilters.keySet()) {
				if (jiraServerData.getServerId().equals(serverCfg.getServerId())) {
					for (JIRAQueryFragment query : savedFilters.get(serverCfg)) {
						list.add(query);
					}
				}
			}
			return list;
		}

		public List<JIRAComponentBean> getComponents(JiraServerData jiraServerData, String projectKey) throws JIRAException {
			return null;
		}

		public List<JIRAVersionBean> getVersions(JiraServerData jiraServerData, String projectKey) throws JIRAException {
			return null;
		}

		public List<JIRAPriorityBean> getPriorities(JiraServerData jiraServerData) throws JIRAException {
			return null;
		}

		public List<JIRAResolutionBean> getResolutions(JiraServerData jiraServerData) throws JIRAException {
			return new ArrayList<JIRAResolutionBean>();
		}

		public List<JIRAAction> getAvailableActions(JiraServerData jiraServerData, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public List<JIRAActionField> getFieldsForAction(JiraServerData jiraServerData, JIRAIssue issue, JIRAAction action)
				throws JIRAException {
			return null;
		}

		public void progressWorkflowAction(JiraServerData jiraServerData, JIRAIssue issue, JIRAAction action)
				throws JIRAException {
		}

		public void progressWorkflowAction(final JiraServerData jiraServerData, final JIRAIssue issue, final JIRAAction action,
				final List<JIRAActionField> fields)
				throws JIRAException {
		}

		public void addComment(JiraServerData jiraServerData, String issueKey, String comment) throws JIRAException {
		}

		public JiraIssueAdapter createIssue(JiraServerData jiraServerData, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public JiraIssueAdapter getIssueUpdate(final JiraServerData server, final JiraIssueAdapter issue) throws JIRAException {
			return null;
		}

		public JiraIssueAdapter getIssue(JiraServerData jiraServerData, String key) throws JIRAException {
			return null;
		}

		public JiraIssueAdapter getIssueDetails(JiraServerData jiraServerData, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public void logWork(JiraServerData jiraServerData, JIRAIssue issue, String timeSpent, Calendar startDate, String comment,
				boolean updateEstimate, String newEstimate) throws JIRAException {
		}

		public void setAssignee(JiraServerData jiraServerData, JIRAIssue issue, String assignee) throws JIRAException {
		}

		public JIRAUserBean getUser(JiraServerData jiraServerData, String loginName) throws JIRAException {
			return null;
		}

		public List<JIRAComment> getComments(JiraServerData jiraServerData, JIRAIssue issue) throws JIRAException {
			return null;
		}
	}

}

final class ServerDataProvider {
	private ServerDataProvider() {
	}

	public static ServerData getServerData(final Server serverCfg) {
		return new ServerData(serverCfg, serverCfg.getUsername(), serverCfg.getPassword());
	}

    public static JiraServerData getServerData(final JiraServerCfg serverCfg) {
        return new JiraServerData(serverCfg, serverCfg.getUsername(), serverCfg.getPassword(), true);
    }

}

