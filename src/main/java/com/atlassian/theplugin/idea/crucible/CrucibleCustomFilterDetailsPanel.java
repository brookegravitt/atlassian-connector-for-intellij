package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.configuration.CrucibleProjectConfiguration;
import com.atlassian.theplugin.crucible.model.CrucibleFilterSelectionListener;
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
	private final CrucibleProjectConfiguration projectCrucibleCfg;
	private final Project project;
	private final CfgManager cfgManager;
	private Collection<CustomFilterChangeListener> listeners = new ArrayList<CustomFilterChangeListener>();

	public CrucibleCustomFilterDetailsPanel(@NotNull final Project project, @NotNull final CfgManager cfgManager,
			@NotNull final CrucibleServerFacade crucibleServerFacade, final CrucibleProjectConfiguration crucibleCfg,
			final FilterTree tree) {
		super(new BorderLayout());
		this.projectCrucibleCfg = crucibleCfg;
		this.project = project;
		this.cfgManager = cfgManager;
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

				CrucibleCustomFilterDialog dialog = new CrucibleCustomFilterDialog(
						project, cfgManager, projectCrucibleCfg.getCrucibleFilters().getManualFilter());

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
		final Collection<ScrollableTwoColumnPanel.Entry> entries = (filter != null)
				? getEntries(filter) : MiscUtil.<ScrollableTwoColumnPanel.Entry>buildArrayList();
		panel.updateContent(entries);
	}

	private static void addIfNotEmpty(String username, String name, Collection<ScrollableTwoColumnPanel.Entry> entries,
			CrucibleServerCfg serverCfg) {
		if (username.length() > 0) {
			CrucibleServerFacade facade = CrucibleServerFacadeImpl.getInstance();
			final String displayName = serverCfg != null ? facade.getDisplayName(serverCfg, username) : null;
			entries.add(new ScrollableTwoColumnPanel.Entry(name, displayName != null ? displayName : username));
		}

	}

	public Collection<ScrollableTwoColumnPanel.Entry> getEntries(@NotNull CustomFilter customFilter) {
		final Collection<ScrollableTwoColumnPanel.Entry> entries = MiscUtil.buildArrayList();
		final String states = StringUtils.join(customFilter.getState(), ", ");
		final String serverId = customFilter.getServerUid();
		// server is null when component is initialized by the PICO for the first time
		// todo force cfgManager to read configuration earlier
		final ServerCfg server = cfgManager.getServer(CfgUtil.getProjectId(project), new ServerId(serverId));
		final CrucibleServerCfg crucibleServerCfg = (server instanceof CrucibleServerCfg) ? (CrucibleServerCfg) server : null;

		entries.add(new ScrollableTwoColumnPanel.Entry("Server", (server != null ? server.getName() : "???")));
		entries.add(new ScrollableTwoColumnPanel.Entry("Project key", customFilter.getProjectKey()));
		entries.add(new ScrollableTwoColumnPanel.Entry("State", states));
		addIfNotEmpty(customFilter.getAuthor(), "Author", entries, crucibleServerCfg);
		addIfNotEmpty(customFilter.getModerator(), "Moderator", entries, crucibleServerCfg);
		addIfNotEmpty(customFilter.getCreator(), "Creator", entries, crucibleServerCfg);
		addIfNotEmpty(customFilter.getReviewer(), "Reviewer", entries, crucibleServerCfg);

//		if (orRoles) {
//			map.put("Role", orRoles ? "true" : "false");
//		}
//		if (complete) {
//			map.put("Complete", complete ? "true" : "false");
//		}
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
