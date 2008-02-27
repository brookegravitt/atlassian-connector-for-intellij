package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.BambooStatusChecker;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.idea.config.ConfigPanel;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusChecker;
import com.atlassian.theplugin.jira.JIRAServer;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.Timer;

@State(name = "ThePluginSettings", storages = { @Storage(id = "thePlugin", file = "$APP_CONFIG$/thePlugin.xml") })
public class ThePluginApplicationComponent
		implements ApplicationComponent, Configurable, PersistentStateComponent<PluginConfigurationBean> {

	private static Icon pluginSettingsIcon;

	static {
		pluginSettingsIcon = IconLoader.getIcon("/icons/atlassian_icon-32.png");
	}

	private ConfigPanel form;
	private PluginConfigurationBean configuration = new PluginConfigurationBean();

	private final Timer timer = new Timer();
	private static final int TIMER_START_DELAY = 20000;

	private final Collection<TimerTask> scheduledComponents = new HashSet<TimerTask>();

	private UserDataContext userDataContext;
    private JIRAServer currentJIRAServer;

	public ThePluginProjectComponent getProjectComponent() {
		return projectComponent;
	}

	public void setProjectComponent(ThePluginProjectComponent projectComponent) {
		this.projectComponent = projectComponent;
	}

	private ThePluginProjectComponent projectComponent = null;

	BambooStatusChecker getBambooStatusChecker() {
		return bambooStatusChecker;
	}

	private BambooStatusChecker bambooStatusChecker = new BambooStatusChecker(IdeaActionScheduler.getInstance());


	@Nls
	public String getDisplayName() {
		return PluginInfoUtil.getName();
	}

	@Nullable
	public Icon getIcon() {
		return pluginSettingsIcon;
	}

	@Nullable
	@NonNls
	public String getHelpTopic() {
		return null;
	}

	@NonNls
	@NotNull
	public String getComponentName() {
		return "ThePluginApplicationComponent";
	}

	public UserDataContext getUserDataContext() {
		return userDataContext;
	}

	public void initComponent() {
		userDataContext = new UserDataContext();

		ConfigurationFactory.setConfiguration(configuration);

		rescheduleStatusCheckers(false);
	}

	public void disposeComponent() {
		timer.cancel();
	}

	public JComponent createComponent() {
		form = ConfigPanel.getInstance();
		return form;
	}

	public ConfigPanel getConfigDialog() {
		return form;
	}

	public boolean isModified() {
		return form != null && form.isModified();
	}

	private void disableTimers() {
		Iterator<TimerTask> i = scheduledComponents.iterator();
		while (i.hasNext()) {
			TimerTask timerTask = i.next();
			i.remove();
			timerTask.cancel();
		}

		timer.purge();
	}

	private Collection<SchedulableComponent> schedulableComponents = Arrays.asList(
			bambooStatusChecker,
			CrucibleStatusChecker.getIntance(),
			NewVersionChecker.getInstance()
	);

	/**
	 * Reschedule the BambooStatusChecker with immediate execution trigger.
	 *
	 * @param rightNow set to false if the first execution should be delayed by {@link #TIMER_START_DELAY}.
	 */
	public void rescheduleStatusCheckers(boolean rightNow) {
		disableTimers();

		long delay = rightNow ? 0 : TIMER_START_DELAY;

		for (SchedulableComponent component : schedulableComponents) {
			if (component.canSchedule()) {
				final TimerTask newTask = component.newTimerTask();
				scheduledComponents.add(newTask);
				timer.schedule(newTask, delay, component.getInterval());
			}
		}
	}



	public void apply() throws ConfigurationException {
		if (form != null) {
			// Get data from form to component
			form.getData();

			// show icons if necessary
			projectComponent.getStatusBarBambooIcon().showOrHideIcon();
			projectComponent.getStatusBarCrucibleIcon().showOrHideIcon();

			for (Project pr : ProjectManager.getInstance().getOpenProjects()) {
				ThePluginProjectComponent pc = pr.getComponent(ThePluginProjectComponent.class);
				pc.enablePlugin();
			}
			rescheduleStatusCheckers(true);
		}

	}

	public void reset() {
		if (form != null) {
			// Reset form data from component
			form.setData();
		}
	}

	public void disposeUIResources() {
		form = null;
	}

	public PluginConfigurationBean getState() {
		return configuration;
	}

	public void loadState(PluginConfigurationBean state) {
		configuration = state;
	}

	public JIRAServer getCurrentJIRAServer() {
        return currentJIRAServer;
    }

    public void setCurrentJIRAServer(JIRAServer currentJIRAServer) {
        this.currentJIRAServer = currentJIRAServer;
    }
}
