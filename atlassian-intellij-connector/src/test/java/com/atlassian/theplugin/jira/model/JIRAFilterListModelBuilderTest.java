package com.atlassian.theplugin.jira.model;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.jira.JIRAServerFacade;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.JIRAAction;
import com.atlassian.theplugin.commons.jira.api.JIRAActionField;
import com.atlassian.theplugin.commons.jira.api.JIRAComment;
import com.atlassian.theplugin.commons.jira.api.JIRAComponentBean;
import com.atlassian.theplugin.commons.jira.api.JIRAConstant;
import com.atlassian.theplugin.commons.jira.api.JIRAIssue;
import com.atlassian.theplugin.commons.jira.api.JIRAIssueBean;
import com.atlassian.theplugin.commons.jira.api.JIRAPriorityBean;
import com.atlassian.theplugin.commons.jira.api.JIRAProject;
import com.atlassian.theplugin.commons.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.commons.jira.api.JIRAResolutionBean;
import com.atlassian.theplugin.commons.jira.api.JIRAUserBean;
import com.atlassian.theplugin.commons.jira.api.JIRAVersionBean;
import com.atlassian.theplugin.commons.jira.api.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModelImpl;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.configuration.JiraViewConfigurationBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

/**
 * User: pmaruszak
 */
public class JIRAFilterListModelBuilderTest extends TestCase {
	private JIRAFilterListBuilder builder;
	private JIRAFilterListModel listModel;
	private Map<JiraServerCfg, List<JIRAQueryFragment>> savedFilters;
	private JIRAServerModelImpl serverModel;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		savedFilters = new HashMap<JiraServerCfg, List<JIRAQueryFragment>>();

		final JIRATestServerFacade facade = new JIRATestServerFacade();


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

	class JIRATestServerFacade implements JIRAServerFacade {

		private List<JIRAIssue> createIssueList(int size) {
			List<JIRAIssue> list = new ArrayList<JIRAIssue>();
			for (int i = 0; i < size; ++i) {
				JIRAIssueBean issue = new JIRAIssueBean();
				list.add(issue);
			}
			return list;
		}

		public void testServerConnection(final ConnectionCfg serverCfg) throws RemoteApiException {
		}

		public ServerType getServerType() {
			return null;
		}

		public List<JIRAIssue> getIssues(JiraServerData server, List<JIRAQueryFragment> query,
				String sort, String sortOrder, int start, int size)
				throws JIRAException {
			return createIssueList(size);
		}

		public List<JIRAIssue> getSavedFilterIssues(JiraServerData server, List<JIRAQueryFragment> query,
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

		public List<JIRAConstant> getSubtaskIssueTypesForProject(JiraServerData server, String project)
                throws JIRAException {
			return null;
		}

		public List<JIRAQueryFragment> getSavedFilters(JiraServerData server) throws JIRAException {
			List<JIRAQueryFragment> list = new ArrayList<JIRAQueryFragment>();
			for (JiraServerCfg serverCfg : savedFilters.keySet()) {
				if (server.getServerId().equals(serverCfg.getServerId())) {
					for (JIRAQueryFragment query : savedFilters.get(serverCfg)) {
						list.add(query);
					}
				}
			}
			return list;
		}

		public List<JIRAComponentBean> getComponents(JiraServerData server, String projectKey) throws JIRAException {
			return null;
		}

		public List<JIRAVersionBean> getVersions(JiraServerData server, String projectKey) throws JIRAException {
			return null;
		}

		public List<JIRAPriorityBean> getPriorities(JiraServerData server) throws JIRAException {
			return null;
		}

		public List<JIRAResolutionBean> getResolutions(JiraServerData server) throws JIRAException {
			return new ArrayList<JIRAResolutionBean>();
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

		public JIRAIssue createIssue(JiraServerData server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public JIRAIssue getIssueUpdate(final JiraServerData server, final JIRAIssue issue) throws JIRAException {
			return null;
		}

		public JIRAIssue getIssue(JiraServerData server, String key) throws JIRAException {
			return null;
		}

		public JIRAIssue getIssueDetails(JiraServerData server, JIRAIssue issue) throws JIRAException {
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

