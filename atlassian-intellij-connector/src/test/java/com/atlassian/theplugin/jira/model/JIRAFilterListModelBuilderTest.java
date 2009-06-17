package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.*;
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
	private JIRATestServerFacade facade;
	private JIRAFilterListBuilder builder;
	private JIRAFilterListModel listModel;
	private CfgManagerTest cfgManager;
	private Map<JiraServerCfg, List<JIRAQueryFragment>> savedFilters;
	private JIRAServerModelImpl serverModel;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		savedFilters = new HashMap<JiraServerCfg, List<JIRAQueryFragment>>();

		facade = new JIRATestServerFacade();


		serverModel = new JIRAServerModelImpl();
		serverModel.setFacade(facade);
		cfgManager = new CfgManagerTest(savedFilters);
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
		viewBean.setViewServerId("none");
		jiraCfg.setView(viewBean);

	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	void fillServersAndFilters(Map<JiraServerCfg, List<JIRAQueryFragment>> aSavedFilters)
			throws RemoteApiException {
		for (int i = 0; i < 3; i++) {
			JiraServerCfg server = new JiraServerCfg("jiraserver" + 1, new ServerId());
			aSavedFilters.put(server, new ArrayList<JIRAQueryFragment>());
			try {
				serverModel.getResolutions(cfgManager.getServerData(server), true);
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
		assertEquals(listModel.getSavedFilters(cfgManager.getServerData(jiraServer)).size(),
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
				if (server.getServerId().equals(serverCfg.getServerId().toString())) {
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

class CfgManagerTest implements CfgManager {
	Map<JiraServerCfg, List<JIRAQueryFragment>> savedFilters;

	CfgManagerTest(final Map<JiraServerCfg, List<JIRAQueryFragment>> savedFilters) {
		this.savedFilters = savedFilters;
	}

	public ProjectConfiguration getProjectConfiguration(final ProjectId projectId) {
		return null;
	}

	public Collection<ServerCfg> getAllServers(final ProjectId projectId) {
		return null;
	}

	public Collection<ServerCfg> getProjectSpecificServers(final ProjectId projectId) {
		return null;
	}

	public Collection<ServerCfg> getGlobalServers() {
		return null;
	}

	public Collection<ServerCfg> getAllEnabledServers(final ProjectId projectId) {
		return null;
	}

	public Collection<ServerCfg> getAllEnabledServers(final ProjectId projectId, final ServerType serverType) {
		return null;
	}

	public Collection<ServerCfg> getAllServersWithDefaultCredentials(final ProjectId projectId, final ServerType serverType) {
		return null;
	}

	public Collection<ServerCfg> getAllServersWithDefaultCredentials(final ProjectId projectId) {
		return null;
	}

	public void updateProjectConfiguration(final ProjectId projectId, final ProjectConfiguration projectConfiguration) {
	}

	public void updateGlobalConfiguration(final GlobalConfiguration globalConfiguration) {
	}

	public void addProjectSpecificServer(final ProjectId projectId, final ServerCfg serverCfg) {
	}

	public void addGlobalServer(final ServerCfg serverCfg) {
	}

	public ProjectConfiguration removeProject(final ProjectId projectId) {
		return null;
	}

	public ServerCfg removeGlobalServer(final ServerId serverId) {
		return null;
	}

	public ServerCfg removeProjectSpecificServer(final ProjectId projectId, final ServerId serverId) {
		return null;
	}

	public ServerCfg getServer(final ProjectId projectId, final ServerId serverId) {
		return null;
	}

	public void addProjectConfigurationListener(final ProjectId projectId, final ConfigurationListener configurationListener) {

	}

	public boolean removeProjectConfigurationListener(final ProjectId projectId,
			final ConfigurationListener configurationListener) {
		return false;
	}

	public Collection<CrucibleServerCfg> getAllEnabledCrucibleServers(final ProjectId projectId) {
		return null;
	}

	public Collection<JiraServerCfg> getAllEnabledJiraServers(final ProjectId projectId) {
		List<JiraServerCfg> list = new ArrayList<JiraServerCfg>();

		for (JiraServerCfg server : savedFilters.keySet()) {
			list.add(server);
		}

		return list;
	}

	public Collection<ServerCfg> getAllUniqueServers() {
		return null;
	}

	public void addConfigurationCredentialsListener(final ProjectId projectId,
			final ConfigurationCredentialsListener listener) {
	}

	public void removeAllConfigurationCredentialListeners(final ProjectId projectId) {
	}

	public boolean removeConfigurationCredentialsListener(final ProjectId projectId,
			final ConfigurationCredentialsListener listener) {
		return false;
	}

	public boolean hasProject(final ProjectId projectId) {
		return false;
	}

	public Collection<CrucibleServerCfg> getAllCrucibleServers(ProjectId projectId) {
		return null;
	}

	public ServerCfg getServer(final ProjectId projectId, final ServerData serverData) {
		return null;
	}

	public Collection<JiraServerCfg> getAllJiraServers(final ProjectId projectId) {
		return null;
	}

	public Collection<ServerCfg> getAllServers(final ProjectId projectId, final ServerType serverType) {
		return null;
	}

	public ServerCfg getEnabledServer(final ProjectId projectId, final ServerId serverId) {
		return null;
	}

	public ServerData getServerData(final Server serverCfg) {
		return new ServerData(serverCfg.getName(), serverCfg.getServerId().toString(), serverCfg.getUserName(),
				serverCfg.getPassword(), serverCfg.getUrl());
	}

	public ServerData getServerData(final ProjectId projectId, final ServerId serverId) {
		return null;
	}

	public ServerData getServerData(final ProjectId projectId, final Server server) {
		return new ServerData(server.getName(), server.getServerId().toString(), server.getUserName(),
				server.getPassword(), server.getUrl());
	}

	public Collection<BambooServerCfg> getAllEnabledBambooServers(final ProjectId projectId) {
		return null;
	}

	public void setSavedFilters(final Map<JiraServerCfg, List<JIRAQueryFragment>> savedFilters) {
		this.savedFilters = savedFilters;
	}
}

