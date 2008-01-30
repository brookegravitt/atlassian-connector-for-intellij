package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.PluginConfigurationBean;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Timer;

//import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 2:57:21 PM
 * To change this template use File | Settings | File Templates.
 */

@State(name = "ThePluginSettings", storages = { @Storage(id = "thePlugin", file = "$APP_CONFIG$/thePlugin.xml") })
public class ThePluginApplicationComponent
		implements ApplicationComponent, Configurable, PersistentStateComponent<PluginConfigurationBean> {

	private PluginConfigurationForm form;
	private PluginConfigurationBean configuration = new PluginConfigurationBean();

	private final Timer timer = new Timer();
	private static final int TIMER_TICK = 20000;
	private BambooStatusChecker bambooStatusChecker;

	public BambooStatusChecker getBambooStatusChecker() {
		return bambooStatusChecker;
	}

	public Timer getTimer() {
		return timer;
	}

	@Nls
	public String getDisplayName() {
		return PluginInfo.getName();
	}

	@Nullable
	public Icon getIcon() {
		return null;
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

	public void initComponent() {
		ConfigurationFactory.setConfiguration(configuration);

		bambooStatusChecker = new BambooStatusChecker();
		timer.schedule(bambooStatusChecker, 0, TIMER_TICK);
	}

	public void disposeComponent() {
		timer.cancel();
	}

	public JComponent createComponent() {
		if (form == null) {
			form = new PluginConfigurationForm();
		}
		return form.getRootComponent();

	}

	public boolean isModified() {
		return form != null && form.isModified(configuration);
	}

	public void apply() throws ConfigurationException {
		if (form != null) {
			// Get data from form to component
			form.getData(configuration);
			bambooStatusChecker.cancel();
			timer.purge();
			try {
				bambooStatusChecker = (BambooStatusChecker) bambooStatusChecker.clone();
			} catch (CloneNotSupportedException e) {
				throw new ConfigurationException(e.getMessage(), "Error while restarting timer.");
			}
			timer.schedule(bambooStatusChecker, 0, TIMER_TICK);
		}

	}

	public void reset() {
		if (form != null) {
			// Reset form data from component
			form.setData(configuration);
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
}
