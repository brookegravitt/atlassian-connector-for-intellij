package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.UiTask;
import com.atlassian.theplugin.configuration.CrucibleProjectConfiguration;
import com.atlassian.theplugin.crucible.model.CrucibleFilterSelectionListener;
import com.atlassian.theplugin.idea.config.ProjectCfgManager;
import com.atlassian.theplugin.idea.crucible.filters.CustomFilterChangeListener;
import com.atlassian.theplugin.idea.crucible.tree.FilterTree;
import com.atlassian.theplugin.idea.ui.ScrollableTwoColumnPanel;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

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
	private final ScrollableTwoColumnPanel panel;
	private final ProjectCfgManager projectCfgManager;
	private final CrucibleProjectConfiguration projectCrucibleCfg;
	private final Project project;
	private final CrucibleServerFacade crucibleFacade;
	private final UiTaskExecutor uiTaskExecutor;
	private Collection<CustomFilterChangeListener> listeners = new ArrayList<CustomFilterChangeListener>();

	public CrucibleCustomFilterDetailsPanel(@NotNull final Project project, @NotNull final ProjectCfgManager projectCfgManager,
			final CrucibleProjectConfiguration crucibleCfg, final FilterTree tree,
			@NotNull final CrucibleServerFacade crucibleFacade, @NotNull final UiTaskExecutor uiTaskExecutor) {
		super(new BorderLayout());
		this.projectCfgManager = projectCfgManager;
		this.projectCrucibleCfg = crucibleCfg;
		this.project = project;
		this.crucibleFacade = crucibleFacade;
		this.uiTaskExecutor = uiTaskExecutor;
		panel = new ScrollableTwoColumnPanel();

		filter = crucibleCfg.getCrucibleFilters().getManualFilter();
		updateDetails();
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		final JButton editButton = new JButton("Edit");
		buttonPanel.add(editButton);
		add(panel, BorderLayout.CENTER);
		this.setBorder(BorderFactory.createTitledBorder("Custom Filter"));
		this.add(buttonPanel, BorderLayout.SOUTH);

		CrucibleFilterSelectionListener listener = new CrucibleFilterSelectionListener() {
			public void filterChanged() {
			}

			public void selectedCustomFilter(CustomFilter customFilter) {
				CrucibleCustomFilterDetailsPanel.this.filter = (CustomFilterBean) customFilter;
				updateDetails();
			}

			public void selectedPredefinedFilters(Collection<PredefinedFilter> selectedPredefinedFilter) {
			}

			public void unselectedCustomFilter() {
			}
		};

		tree.addSelectionListener(listener);

		editButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {

				final CrucibleCustomFilterDialog dialog = new CrucibleCustomFilterDialog(
						project, projectCfgManager.getCfgManager(), projectCrucibleCfg.getCrucibleFilters().getManualFilter(),
						uiTaskExecutor);

				dialog.show();

				if (dialog.getExitCode() == 0 && dialog.getFilter() != null) {
					filter = dialog.getFilter();
					projectCrucibleCfg.getCrucibleFilters().setManualFilter(filter);
					updateDetails();
					// refresh reviews panel
					for (CustomFilterChangeListener myListener : listeners) {
						myListener.customFilterChanged(filter);
					}

				}
			}
		});
	}

	private void updateDetails() {
		uiTaskExecutor.execute(new UiTask() {

			private Collection<ScrollableTwoColumnPanel.Entry> entries;
			public void run() throws Exception {
				entries = (filter != null) ? getEntries(filter) : MiscUtil.<ScrollableTwoColumnPanel.Entry>buildArrayList();
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
				return CrucibleCustomFilterDetailsPanel.this;
			}
		});
	}

	private void addIfNotEmpty(String username, String name, Collection<ScrollableTwoColumnPanel.Entry> entries,
			CrucibleServerCfg serverCfg) {
		if (username.length() > 0) {
			final String displayName = serverCfg != null ? crucibleFacade.getDisplayName(serverCfg, username) : null;
			entries.add(new ScrollableTwoColumnPanel.Entry(name, displayName != null ? displayName : username));
		}

	}

	public Collection<ScrollableTwoColumnPanel.Entry> getEntries(@NotNull final CustomFilter customFilter) {
		final Collection<ScrollableTwoColumnPanel.Entry> entries = MiscUtil.buildArrayList();
		final String states = StringUtils.join(customFilter.getState(), ", ");
		final String serverId = customFilter.getServerUid();
		final ServerCfg server = projectCfgManager.getCfgManager().getServer(
				CfgUtil.getProjectId(project), new ServerId(serverId));
		final CrucibleServerCfg crucibleServerCfg = (server instanceof CrucibleServerCfg) ? (CrucibleServerCfg) server : null;

		entries.add(new ScrollableTwoColumnPanel.Entry("Server", (server != null ? server.getName() : "???")));
		if (customFilter.getProjectKey() != null && customFilter.getProjectKey().length() > 0) {
			String projectName = customFilter.getProjectKey();
			try {
				CrucibleProject crucibleProject = crucibleServerCfg != null
						? crucibleFacade.getProject(crucibleServerCfg, customFilter.getProjectKey())
						: null;
				if (crucibleProject != null) {
					projectName = crucibleProject.getName();
				}
			} catch (RemoteApiException e) {
				// nothing here
			} catch (ServerPasswordNotProvidedException e) {
				// nothing here
			}
			entries.add(new ScrollableTwoColumnPanel.Entry("Project", projectName));

		}
		entries.add(new ScrollableTwoColumnPanel.Entry("State", states));
		addIfNotEmpty(customFilter.getAuthor(), "Author", entries, crucibleServerCfg);
		addIfNotEmpty(customFilter.getModerator(), "Moderator", entries, crucibleServerCfg);
		addIfNotEmpty(customFilter.getCreator(), "Creator", entries, crucibleServerCfg);
		addIfNotEmpty(customFilter.getReviewer(), "Reviewer", entries, crucibleServerCfg);

		final Boolean orRoles = customFilter.isOrRoles();
		entries.add(new ScrollableTwoColumnPanel.Entry("Match Roles", (orRoles == null || orRoles == false) ? "Any" : "All"));

		final Boolean reviewerStatus = customFilter.isComplete();
		if (reviewerStatus != null) {
			entries.add(new ScrollableTwoColumnPanel.Entry("Reviewer Status", reviewerStatus ? "Complete" : "Incomplete"));
		}

//		if (allReviewersComplete) {
//			map.put("All revievers completed", allReviewersComplete ? "true" : "false");
//		}
		return entries;
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
