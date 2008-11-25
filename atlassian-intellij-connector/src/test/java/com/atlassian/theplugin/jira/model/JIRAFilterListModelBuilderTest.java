package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.ConfigurationCredentialsListener;
import com.atlassian.theplugin.commons.cfg.ConfigurationListener;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.GlobalConfiguration;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ProjectId;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.configuration.JiraProjectConfiguration;
import com.atlassian.theplugin.configuration.JiraViewConfigurationBean;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.api.JIRAAction;
import com.atlassian.theplugin.jira.api.JIRAActionField;
import com.atlassian.theplugin.jira.api.JIRAComment;
import com.atlassian.theplugin.jira.api.JIRAComponentBean;
import com.atlassian.theplugin.jira.api.JIRAConstant;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRAIssueBean;
import com.atlassian.theplugin.jira.api.JIRAProject;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRAResolutionBean;
import com.atlassian.theplugin.jira.api.JIRAUserBean;
import com.atlassian.theplugin.jira.api.JIRAVersionBean;
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
	private JIRATestServerFacade facade;
	private JIRAFilterListBuilder builder;
	private JIRAFilterListModel listModel;
	private CfgManagerTest cfgManager;
	private Map<JiraServerCfg, List<JIRAQueryFragment>> savedFilters;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		facade = new JIRATestServerFacade();

		JiraProjectConfiguration jiraConfig = new JiraProjectConfiguration();

		facade = new JIRATestServerFacade();
		savedFilters = new HashMap<JiraServerCfg, List<JIRAQueryFragment>>();
		fillServersAndFilters(savedFilters);
		
		cfgManager = new CfgManagerTest(savedFilters);

		JiraProjectConfiguration jiraCfg = new JiraProjectConfiguration();
		fillJiraCfg(jiraCfg);

		builder = new JIRAFilterListBuilder(facade, cfgManager);
		listModel = new JIRAFilterListModel();
		builder.setListModel(listModel);
		builder.setJiraProjectCfg(jiraCfg);
	}

	public void fillJiraCfg(JiraProjectConfiguration jiraCfg){
		JiraViewConfigurationBean viewBean = new JiraViewConfigurationBean();
		viewBean.setViewFilterId("none");
		viewBean.setViewServerId("none");
		jiraCfg.setView(viewBean);

	}
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	void fillServersAndFilters(Map<JiraServerCfg, List<JIRAQueryFragment>> aSavedFilters){
		for(int i = 0; i<3; i++){
			JiraServerCfg server = new JiraServerCfg("jiraserver"+1, new ServerId());
			aSavedFilters.put(server, new ArrayList<JIRAQueryFragment>());
		}
	}

	public void testRebuildModel() {
		try {
			builder.rebuildModel();
		} catch (JIRAFilterListBuilder.JIRAServerFiltersBuilderException e) {
			fail();//we do not expect exception
		}
		assertEquals(3, listModel.getJIRAServers().size());
		JiraServerCfg jiraServer = savedFilters.keySet().iterator().next();
		assertEquals(listModel.getSavedFilters(jiraServer).size(), savedFilters.get(jiraServer).size());		

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
			List<JIRAQueryFragment> list = new ArrayList<JIRAQueryFragment>();
			for (JIRAQueryFragment query : savedFilters.get(server)){
				list.add(query);
			}
			return list;
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

		public JIRAIssue getIssueUpdate(final JiraServerCfg server, final JIRAIssue issue) throws JIRAException {
			return null;
		}

		public JIRAIssue getIssue(JiraServerCfg server, String key) throws JIRAException {
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
		}}

}

class CfgManagerTest implements CfgManager {
	Map<JiraServerCfg, List<JIRAQueryFragment>> savedFilters;


	public CfgManagerTest(final Map<JiraServerCfg, List<JIRAQueryFragment>> savedFilters ){

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

	public boolean removeProjectConfigurationListener(final ProjectId projectId, final ConfigurationListener configurationListener) {
		return false;
	}

	public Collection<CrucibleServerCfg> getAllEnabledCrucibleServers(final ProjectId projectId) {
		return null;
	}

	public Collection<JiraServerCfg> getAllEnabledJiraServers(final ProjectId projectId) {
		List<JiraServerCfg> list = new ArrayList<JiraServerCfg>();

		for (JiraServerCfg server : savedFilters.keySet()){
			list.add(server);
		}

		return list;
	}

	public Collection<ServerCfg> getAllUniqueServers() {
		return null;
	}

	public void addConfigurationCredentialsListener(final ProjectId projectId, final ConfigurationCredentialsListener listener) {
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

	public Collection<BambooServerCfg> getAllEnabledBambooServers(final ProjectId projectId) {
		return null;
	}
}

