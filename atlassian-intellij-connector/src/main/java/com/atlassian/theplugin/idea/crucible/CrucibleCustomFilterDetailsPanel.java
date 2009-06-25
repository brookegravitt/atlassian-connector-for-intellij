package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.UiTask;
import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.configuration.CrucibleWorkspaceConfiguration;
import com.atlassian.theplugin.crucible.model.CrucibleFilterSelectionListener;
import com.atlassian.theplugin.crucible.model.CrucibleFilterSelectionListenerAdapter;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.atlassian.theplugin.idea.crucible.filters.CustomFilterChangeListener;
import com.atlassian.theplugin.idea.crucible.tree.FilterTree;
import com.atlassian.theplugin.idea.ui.ScrollableTwoColumnPanel;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

/**
 * User: pmaruszak
 */
public class CrucibleCustomFilterDetailsPanel extends JPanel {
	private CustomFilterBean filter;
	private ScrollableTwoColumnPanel panel;
	private final ProjectCfgManagerImpl projectCfgManager;
	private final CrucibleWorkspaceConfiguration projectCrucibleCfg;
	private final Project project;
	private final CrucibleServerFacade crucibleFacade;
	private final UiTaskExecutor uiTaskExecutor;
	private Collection<CustomFilterChangeListener> listeners = new ArrayList<CustomFilterChangeListener>();

	public CrucibleCustomFilterDetailsPanel(@NotNull final Project project,
			@NotNull final ProjectCfgManagerImpl projectCfgManager,
			final CrucibleWorkspaceConfiguration crucibleCfg, final FilterTree tree,
			@NotNull final CrucibleServerFacade crucibleFacade, @NotNull final UiTaskExecutor uiTaskExecutor) {
		super(new BorderLayout());
		this.projectCfgManager = projectCfgManager;
		this.projectCrucibleCfg = crucibleCfg;
		this.project = project;
		this.crucibleFacade = crucibleFacade;
		this.uiTaskExecutor = uiTaskExecutor;

		updateDetails(crucibleCfg.getCrucibleFilters().getManualFilter());
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		final JButton editButton = new JButton("Edit");
		buttonPanel.add(editButton);
		this.setBorder(BorderFactory.createTitledBorder("Custom Filter"));
		this.add(buttonPanel, BorderLayout.SOUTH);

		CrucibleFilterSelectionListener listener = new CrucibleFilterSelectionListenerAdapter() {

			public void selectedCustomFilter(CustomFilter customFilter) {
				updateDetails((CustomFilterBean) customFilter);
			}
		};

		tree.addSelectionListener(listener);

		editButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {

				final CrucibleCustomFilterDialog dialog = new CrucibleCustomFilterDialog(
						project, projectCfgManager, projectCrucibleCfg.getCrucibleFilters().getManualFilter(),
						uiTaskExecutor);

				dialog.show();

				if (dialog.getExitCode() == 0 && dialog.getFilter() != null) {
					projectCrucibleCfg.getCrucibleFilters().setManualFilter(filter);
					updateDetails(dialog.getFilter());
					// refresh reviews panel
					for (CustomFilterChangeListener myListener : listeners) {
						myListener.customFilterChanged(filter);
					}

				}
			}
		});

		projectCfgManager.addProjectConfigurationListener(new ConfigurationListenerAdapter() {

			@Override
			public void serverRemoved(ServerCfg oldServer) {
				updateFilterServer(oldServer);
			}

			// we need to also handle adding server. Consider scenario:
			// 1. remove the last CRU server, that is also a filter's server
			// 2. custom filter details panel disappears
			// 3. add some other CRU server
			// 4. custom filter details panel reappears and it has to have correct data (info about invalid server)
			@Override
			public void serverAdded(ServerCfg newServer) {
				updateFilterServer(newServer);
			}

			private void updateFilterServer(ServerCfg oldServer) {
				if (oldServer.getServerType().equals(ServerType.CRUCIBLE_SERVER)) {
					updateDetails(filter);
				}
			}
		});
	}

	private synchronized void updateDetails(final CustomFilterBean manualFilter) {
		filter = manualFilter;
		if (panel != null) {
			remove(panel);
		}
		panel = new ScrollableTwoColumnPanel();
		add(panel, BorderLayout.CENTER);
		validate();

		uiTaskExecutor.execute(new MyUiTask(filter, panel, projectCfgManager, crucibleFacade, project));
	}

	public void addCustomFilterChangeListener(CustomFilterChangeListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeCustomFilterChangeListener(CustomFilterChangeListener listener) {
		listeners.remove(listener);
	}
}

class MyUiTask implements UiTask {

	private Collection<ScrollableTwoColumnPanel.Entry> entries;
	@Nullable
	private final CustomFilterBean filter;
	@NotNull
	private final ScrollableTwoColumnPanel panel;
	@NotNull
	private final ProjectCfgManagerImpl projectCfgManager;
	@NotNull
	private final CrucibleServerFacade crucibleFacade;
	private final Project project;

	public MyUiTask(@Nullable CustomFilterBean filter, @NotNull final ScrollableTwoColumnPanel panel,
			@NotNull ProjectCfgManagerImpl projectCfgManager, @NotNull final CrucibleServerFacade crucibleFacade,
			@NotNull final Project project) {
		this.filter = filter;
		this.panel = panel;
		this.projectCfgManager = projectCfgManager;
		this.crucibleFacade = crucibleFacade;
		this.project = project;
		if (filter != null) {
			panel.updateContent(getEntries(filter, false));
		}
	}

	public void run() throws Exception {
		entries = (filter != null) ? getEntries(filter, true) : MiscUtil.<ScrollableTwoColumnPanel.Entry>buildArrayList();
	}

	public void onSuccess() {
		panel.updateContent(entries);
	}

	public void onError() {
	}

	public String getLastAction() {
		return "resolving Crucible dictionary data";
	}

	public Component getComponent() {
		return panel;
	}

	private Collection<ScrollableTwoColumnPanel.Entry> getEntries(@NotNull final CustomFilter customFilter,
			boolean fetchRemoteData) {
		final Collection<ScrollableTwoColumnPanel.Entry> myEntries = MiscUtil.buildArrayList();

		final IServerId serverId = new ServerId(customFilter.getServerUid());
		final ServerCfg server = projectCfgManager.getServer(serverId);
		final CrucibleServerCfg crucibleServerCfg =
				(server instanceof CrucibleServerCfg) ? (CrucibleServerCfg) server : null;

		myEntries.add(new ScrollableTwoColumnPanel.Entry("Server",
				(server != null ? server.getName() : "Server Unknown or Removed"), server == null));
		if (customFilter.getProjectKey() != null && customFilter.getProjectKey().length() > 0) {
			String projectName = customFilter.getProjectKey() + " <i>(fetching full name...)</i>";
			if (fetchRemoteData) {
				try {
					CrucibleProject crucibleProject = crucibleServerCfg != null
							? crucibleFacade.getProject(projectCfgManager.getServerData(crucibleServerCfg),
							customFilter.getProjectKey())
							: null;
					if (crucibleProject != null) {
						projectName = crucibleProject.getName();
					}
				} catch (RemoteApiException e) {
					// nothing here
				} catch (ServerPasswordNotProvidedException e) {
					// nothing here
				}
			}
			myEntries.add(new ScrollableTwoColumnPanel.Entry("Project", projectName));
		}

		final State[] selStates = customFilter.getState();
		if (selStates != null && selStates.length > 0) {
			final StringBuilder states = new StringBuilder();
			for (int i = 0; i < selStates.length; i++) {
				states.append(selStates[i].getDisplayName());
				if (i < selStates.length - 1) {
					states.append(", ");
				}
			}
			myEntries.add(new ScrollableTwoColumnPanel.Entry("State", states.toString()));
		}
		addIfNotEmpty(customFilter.getAuthor(), "Author", myEntries, crucibleServerCfg, fetchRemoteData);
		addIfNotEmpty(customFilter.getModerator(), "Moderator", myEntries, crucibleServerCfg, fetchRemoteData);
		addIfNotEmpty(customFilter.getCreator(), "Creator", myEntries, crucibleServerCfg, fetchRemoteData);
		addIfNotEmpty(customFilter.getReviewer(), "Reviewer", myEntries, crucibleServerCfg, fetchRemoteData);

		final Boolean reviewerStatus = (customFilter.getReviewer() != null && customFilter.getReviewer().length() > 0)
				? customFilter.isComplete() : customFilter.isAllReviewersComplete();
		if (reviewerStatus != null) {
			myEntries.add(new ScrollableTwoColumnPanel.Entry("Reviewer Status", reviewerStatus ? "Complete" : "Incomplete"));
		}

		final Boolean orRoles = customFilter.isOrRoles();
		myEntries.add(new ScrollableTwoColumnPanel.Entry("Match Roles", (orRoles == null || orRoles) ? "Any" : "All"));

		return myEntries;
	}

	private void addIfNotEmpty(String username, String name, Collection<ScrollableTwoColumnPanel.Entry> entriesToFill,
			CrucibleServerCfg serverCfg, boolean fetchRemoteData) {
		if (username.length() > 0) {

			final String displayName = fetchRemoteData
					? serverCfg != null ? crucibleFacade.getDisplayName(projectCfgManager.getServerData(serverCfg),
					username) : null
					: username;
			entriesToFill.add(new ScrollableTwoColumnPanel.Entry(name, displayName != null ? displayName : username));
		}
	}
}

