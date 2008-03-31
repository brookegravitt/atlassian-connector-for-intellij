package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.configuration.ProductServerConfiguration;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ThePluginApplicationComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SelectJIRAAction extends ComboBoxAction {

	@NotNull
	protected DefaultActionGroup createPopupActionGroup(JComponent jComponent) {
		ThePluginApplicationComponent appComponent = IdeaHelper.getAppComponent();

		final DefaultActionGroup g = new DefaultActionGroup();

		ProductServerConfiguration jConfig = appComponent.getState().getProductServers(ServerType.JIRA_SERVER);

		final ComboBoxButton button = (ComboBoxButton) jComponent;
		for (final Server server : jConfig.getEnabledServers()) {
			g.add(new AnAction(server.getName()) {
				public void actionPerformed(AnActionEvent event) {
					button.setText(event.getPresentation().getText());
					IdeaHelper.getJIRAToolWindowPanel(event).selectServer(server);
				}
			});
		}
		return g;
	}

	public void update(AnActionEvent event) {
		super.update(event);
		if (IdeaHelper.getCurrentJIRAServer() != null) {
			event.getPresentation().setText(IdeaHelper.getCurrentJIRAServer().getServer().getName());
		}
	}
}
