package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.configuration.JiraFilterEntryBean;
import com.atlassian.theplugin.configuration.JiraFiltersBean;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.model.*;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.Key;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: pmaruszak
 */
public final class IssuesToolWindowPanel extends JPanel implements ConfigurationListener, MessageStatusDisplay {
	private static final Key<IssuesToolWindowPanel> WINDOW_PROJECT_KEY = Key.create(IssuesToolWindowPanel.class.getName());
	private Project project;
	private PluginConfigurationBean pluginConfiguration;
	private ProjectConfigurationBean projectConfigurationBean;
	private CfgManager cfgManager;
	private JPanel serversPanel = new JPanel(new BorderLayout());
	private JPanel issuesPanel = new JPanel(new BorderLayout());
	private final Splitter splitPane = new Splitter(true);
	private static final String SERVERS_TOOL_BAR = "ThePlugin.JiraServers.ServersToolBar";
	private JIRAFilterListModel jiraFilterListModel;

	public MessageScrollPane getMessagePane() {
		return messagePane;
	}

	private MessageScrollPane messagePane;
	private JIRAIssueListModel jiraIssueListModel;

	private final Map<JiraServerCfg, JIRAServer> jiraServerCache = new HashMap<JiraServerCfg, JIRAServer>();

	private IssuesToolWindowPanel(
			final Project project, final PluginConfigurationBean pluginConfiguration,
			final ProjectConfigurationBean projectConfigurationBean, final CfgManager cfgManager) {
		this.project = project;
		this.pluginConfiguration = pluginConfiguration;
		this.projectConfigurationBean = projectConfigurationBean;
		this.cfgManager = cfgManager;
		setLayout(new BorderLayout());
		this.messagePane = new MessageScrollPane("Issues panel");
		add(messagePane, BorderLayout.SOUTH);


		splitPane.setShowDividerControls(true);
		splitPane.setFirstComponent(createServersContent());
        splitPane.setSecondComponent(createIssuesContent());
        splitPane.setHonorComponentsMinimumSize(true);

		addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                final Dimension dimension = e.getComponent().getSize();
                final boolean doVertical = dimension.getWidth() < dimension.getHeight();
                if (doVertical != splitPane.getOrientation()) {
                    splitPane.setOrientation(doVertical);
                }

            }
        });

		add(splitPane, BorderLayout.CENTER);


		this.jiraFilterListModel = new JIRAFilterListModel();
		this.jiraIssueListModel = JIRAIssueListModelImpl.createInstance();
		IdeaHelper.getProjectComponent(project, JIRAIssueListModelBuilderImpl.class).setModel(jiraIssueListModel);
		IdeaHelper.getProjectComponent(project, JIRAServerFiltersBuilder.class).setModel(jiraFilterListModel);
		IdeaHelper.getProjectComponent(project, JIRAServerFiltersBuilder.class).setProjectId(CfgUtil.getProjectId(project));

		IdeaHelper.getCfgManager().addProjectConfigurationListener(CfgUtil.getProjectId(project), this);

		refreshModels();
	}

	public static synchronized IssuesToolWindowPanel getInstance(final Project project,
			final ProjectConfigurationBean projectConfigurationBean,
			final CfgManager cfgManager) {
		IssuesToolWindowPanel window = project.getUserData(WINDOW_PROJECT_KEY);

		if (window == null) {
			window = new IssuesToolWindowPanel(project, IdeaHelper.getPluginConfiguration(),
					projectConfigurationBean, cfgManager);
			project.putUserData(WINDOW_PROJECT_KEY, window);
		}
		return window;
	}

	private void refreshFilterModel() {

		try {
			IdeaHelper.getProjectComponent(project, JIRAServerFiltersBuilder.class).refreshSavedFiltersAll();
		} catch (JIRAServerFiltersBuilder.JIRAServerFiltersBuilderException e) {
			//@todo show in message editPane
			setStatusMessage(e.getMessage(), true);
		}
	}

	private JComponent createIssuesContent() {
		issuesPanel = new JPanel(new BorderLayout());

		JScrollPane scrollPane = new JScrollPane(createIssuesTree(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setWheelScrollingEnabled(true);

		issuesPanel.add(scrollPane, BorderLayout.CENTER);
		issuesPanel.add(createIssuesToolbar(), BorderLayout.NORTH);
		return issuesPanel;
	}

	private JComponent createIssuesToolbar() {
		ActionManager actionManager = ActionManager.getInstance();
		ActionGroup toolbar = (ActionGroup) actionManager.getAction("ThePlugin.JiraIssues.IssuesToolBar");
		ActionToolbar actionToolbar = actionManager
				.createActionToolbar(" ThePlugin.JiraIssues.IssuesToolBar.Place", toolbar, true);


		CellConstraints cc = new CellConstraints();

		final JPanel toolBarPanel = new JPanel(
				new FormLayout("left:1dlu:grow, right:1dlu:grow, left:4dlu:grow, right:pref:grow", "pref:grow"));
		toolBarPanel.add(new JLabel("nothing"), cc.xy(1, 1));
		toolBarPanel.add(new JLabel("Group by"), cc.xy(2, 1));
		toolBarPanel.add(actionToolbar.getComponent(), cc.xy(2 + 1, 1));
		toolBarPanel.add(new JLabel("Search"), cc.xy(2 + 2, 1));

		return toolBarPanel;
	}

	private JComponent createServersContent() {
		serversPanel = new JPanel(new BorderLayout());

		JScrollPane scrollPane = new JScrollPane(createJiraServersTree(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		scrollPane.setWheelScrollingEnabled(true);
		serversPanel.add(scrollPane, BorderLayout.CENTER);
		serversPanel.add(createServersToolbar(), BorderLayout.NORTH);
		return serversPanel;
	}

	private JComponent createServersToolbar() {
		ActionManager actionManager = ActionManager.getInstance();
        ActionGroup toolbar = (ActionGroup) actionManager.getAction(SERVERS_TOOL_BAR);
        ActionToolbar actionToolbar = actionManager.createActionToolbar("ThePlugin.Issues.ServersToolBar.Place", toolbar, true);

		return actionToolbar.getComponent();
	}

	private JComponent createJiraServersTree() {
		return null;  //To change body of created methods use File | Settings | File Templates.
	}

	private JComponent createIssuesTree() {
		return null;
	}

	public void configurationUpdated(final ProjectConfiguration aProjectConfiguration) {
		refreshModels();
	}

	private void refreshModels() {
		Task.Backgroundable task = new Task.Backgroundable(project, "Retrieving JIRA information", false) {

			public void run(final ProgressIndicator indicator) {
				jiraServerCache.clear();

				for(JiraServerCfg server: IdeaHelper.getCfgManager().getAllEnabledJiraServers(CfgUtil.getProjectId(project))){
					final JIRAServerFacade jiraServerFacade = JIRAServerFacadeImpl.getInstance();
					JIRAServer jiraServer = new JIRAServer(server, jiraServerFacade);

					//@todo
//					if (!jiraServer.checkServer()) {
//						//setStatusMessage("Unable to connect to server. " + jiraServer.getErrorMessage(), true);
//
//						EventQueue.invokeLater(
//								new MissingPasswordHandlerJIRA(jiraServerFacade, jiraServer.getServer(), this));
//						return;
//					}
					String serverStr = "[" + server.getName() + "] ";
					setMessage(serverStr + "Retrieving saved filters...");
					jiraServer.getSavedFilters();

					setMessage(serverStr + "Retrieving projects...");
					jiraServer.getProjects();

					setMessage(serverStr + "Retrieving issue types...");
					jiraServer.getIssueTypes();

					setMessage(serverStr + "Retrieving statuses...");
					jiraServer.getStatuses();

					setMessage(serverStr + "Retrieving resolutions...");
					jiraServer.getResolutions();

					setMessage(serverStr + "Retrieving priorities...");
					jiraServer.getPriorieties();

					jiraServerCache.put(server, jiraServer);
				}

				refreshFilterModel();

				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
						refreshModelsFinished();
					}
				});
			}
		};

		ProgressManager.getInstance().run(task);


	}
	private static List<JIRAQueryFragment> getFragments(List<JiraFilterEntryBean> query){
		List<JIRAQueryFragment> fragments = new ArrayList<JIRAQueryFragment>();

		for (JiraFilterEntryBean filterMapBean : query) {
			Map<String, String> filter = filterMapBean.getFilterEntry();
			String className = filter.get("filterTypeClass");
			try {
				Class<?> c = Class.forName(className);
				fragments.add((JIRAQueryFragment) c.getConstructor(Map.class).newInstance(filter));
			} catch (Exception e) {
				LoggerImpl.getInstance().error(e);
			}
		}
		return fragments;
	}

	private void refreshModelsFinished() {
		final ProjectId projectId = CfgUtil.getProjectId(project);
		
		//get all stored manual filters for JIRA server
		for(JiraServerCfg server: IdeaHelper.getCfgManager().getAllEnabledJiraServers(projectId)){
			setMessage("[" + server.getName() +"] Getting saved filters...");
			JiraFiltersBean bean = projectConfigurationBean.getJiraConfiguration()
					.getJiraFilters(server.getServerId().toString());
			
			if (bean != null){
				jiraFilterListModel.setManualFilter(server, getFragments(bean.getManualFilter()));
			}
		}
		jiraFilterListModel.notifyListeners();
	}

	public void projectUnregistered() {
		//To change body of implemented methods use File | Settings | File Templates.
	}


	public void setMessage(final String message) {
		messagePane.setMessage(message);
	}

	public void setStatusMessage(final String msg, final boolean isError) {
		messagePane.setStatusMessage(msg, isError);
	}
}
