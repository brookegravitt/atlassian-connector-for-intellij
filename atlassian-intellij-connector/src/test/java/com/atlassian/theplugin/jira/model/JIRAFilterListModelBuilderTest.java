package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.configuration.JiraViewConfigurationBean;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.api.*;
import junit.framework.TestCase;

import java.util.*;

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


		serverModel = new JIRAServerModelImpl();
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
			JiraServerCfg server = new JiraServerCfg("jiraserver" + 1, new ServerIdImpl());
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

		public void testServerConnection(String url, String userName, String password) throws RemoteApiException {
		}

		public void testServerConnection(final ServerData serverCfg) throws RemoteApiException {
		}

		public ServerType getServerType() {
			return null;
		}

		public List<JIRAIssue> getIssues(ServerData server, List<JIRAQueryFragment> query,
				String sort, String sortOrder, int start, int size)
				throws JIRAException {
			return createIssueList(size);
		}

		public List<JIRAIssue> getSavedFilterIssues(ServerData server, List<JIRAQueryFragment> query,
				String sort, String sortOrder, int start, int size)
				throws JIRAException {
			return createIssueList(size);
		}

		public List<JIRAProject> getProjects(ServerData server) throws JIRAException {
			return null;
		}

		public List<JIRAConstant> getIssueTypes(ServerData server) throws JIRAException {
			return null;
		}

		public List<JIRAConstant> getStatuses(ServerData server) throws JIRAException {
			return null;
		}

		public List<JIRAConstant> getIssueTypesForProject(ServerData server, String project) throws JIRAException {
			return null;
		}

		public List<JIRAConstant> getSubtaskIssueTypes(ServerData server) throws JIRAException {
			return null;
		}

		public List<JIRAConstant> getSubtaskIssueTypesForProject(ServerData server, String project) throws JIRAException {
			return null;
		}

		public List<JIRAQueryFragment> getSavedFilters(ServerData server) throws JIRAException {
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

		public List<JIRAComponentBean> getComponents(ServerData server, String projectKey) throws JIRAException {
			return null;
		}

		public List<JIRAVersionBean> getVersions(ServerData server, String projectKey) throws JIRAException {
			return null;
		}

		public List<JIRAPriorityBean> getPriorities(ServerData server) throws JIRAException {
			return null;
		}

		public List<JIRAResolutionBean> getResolutions(ServerData server) throws JIRAException {
			return new ArrayList<JIRAResolutionBean>();
		}

		public List<JIRAAction> getAvailableActions(ServerData server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public List<JIRAActionField> getFieldsForAction(ServerData server, JIRAIssue issue, JIRAAction action)
				throws JIRAException {
			return null;
		}

		public void progressWorkflowAction(ServerData server, JIRAIssue issue, JIRAAction action)
				throws JIRAException {
		}

		public void progressWorkflowAction(final ServerData server, final JIRAIssue issue, final JIRAAction action,
				final List<JIRAActionField> fields)
				throws JIRAException {
		}

		public void addComment(ServerData server, String issueKey, String comment) throws JIRAException {
		}

		public JIRAIssue createIssue(ServerData server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public JIRAIssue getIssueUpdate(final ServerData server, final JIRAIssue issue) throws JIRAException {
			return null;
		}

		public JIRAIssue getIssue(ServerData server, String key) throws JIRAException {
			return null;
		}

		public JIRAIssue getIssueDetails(ServerData server, JIRAIssue issue) throws JIRAException {
			return null;
		}

		public void logWork(ServerData server, JIRAIssue issue, String timeSpent, Calendar startDate, String comment,
				boolean updateEstimate, String newEstimate) throws JIRAException {
		}

		public void setAssignee(ServerData server, JIRAIssue issue, String assignee) throws JIRAException {
		}

		public JIRAUserBean getUser(ServerData server, String loginName) throws JIRAException {
			return null;
		}

		public List<JIRAComment> getComments(ServerData server, JIRAIssue issue) throws JIRAException {
			return null;
		}
	}

}

final class ServerDataProvider {
	private ServerDataProvider() {
	}

	public static ServerData getServerData(final Server serverCfg) {
		return new ServerData(serverCfg, serverCfg.getUserName(), serverCfg.getPassword());
	}
}

