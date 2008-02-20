package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.idea.bamboo.BambooStatusChecker;
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
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

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
	private static final int TIMER_TICK = 20000;
	private static final int TIMER_START_DELAY = 0;
	private TimerTask bambooStatusCheckerTask;
	private TimerTask crucibleStatusCheckerTask;

	private UserDataContext userDataContext;
    private JIRAServer currentJIRAServer;
	private static final long PLUGIN_UPDATE_ATTEMPT_DELAY = 20000;

	public Timer getTimer() {
		return timer;
	}

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
	public String getComponentName() {
		return "ThePluginApplicationComponent";
	}

	public UserDataContext getUserDataContext() {
		return userDataContext;
	}

	public void initComponent() {
		userDataContext = new UserDataContext();

		ConfigurationFactory.setConfiguration(configuration);

		if (configuration.isPluginEnabled()) {
			triggerStatusCheckers(false);
		}
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
		if (bambooStatusCheckerTask != null) {
			bambooStatusCheckerTask.cancel();
		}
		if (crucibleStatusCheckerTask != null) {
			crucibleStatusCheckerTask.cancel();
		}
		timer.purge();
	}

	/**
	 * Reschedule the BambooStatusChecker with immediate execution trigger.
	 */
	public void triggerStatusCheckers(Boolean rightNow) {
		int delay = TIMER_START_DELAY;
		if (rightNow) {
			delay = 0;
		}
		bambooStatusCheckerTask = BambooStatusChecker.getInstance().newTimerTask();
		timer.schedule(bambooStatusCheckerTask, delay, TIMER_TICK);
		crucibleStatusCheckerTask = CrucibleStatusChecker.getIntance().newTimerTask();
		timer.schedule(crucibleStatusCheckerTask, delay, TIMER_TICK);

		TimerTask updatePluginTimerTask = NewVersionChecker.getInstance().newTimerTask();
		timer.schedule(updatePluginTimerTask, PLUGIN_UPDATE_ATTEMPT_DELAY);
	}



	public void apply() throws ConfigurationException {
		if (form != null) {
			// Get data from form to component
			form.getData();

			disableTimers();
			if (configuration.isPluginEnabled()) {
				for (Project pr : ProjectManager.getInstance().getOpenProjects()) {
					ThePluginProjectComponent pc = pr.getComponent(ThePluginProjectComponent.class);
					pc.enablePlugin();
				}
				triggerStatusCheckers(true);
			} else {
				for (Project pr : ProjectManager.getInstance().getOpenProjects()) {
					ThePluginProjectComponent pc = pr.getComponent(ThePluginProjectComponent.class);
					pc.disablePlugin();
				}
            }
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
