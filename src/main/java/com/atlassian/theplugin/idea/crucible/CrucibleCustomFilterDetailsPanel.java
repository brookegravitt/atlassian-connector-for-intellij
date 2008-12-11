package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.configuration.CrucibleProjectConfiguration;
import com.atlassian.theplugin.crucible.model.CrucibleFilterSelectionListener;
import com.atlassian.theplugin.idea.crucible.filters.CustomFilterChangeListener;
import com.atlassian.theplugin.idea.crucible.tree.FilterTree;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * User: pmaruszak
 */
public class CrucibleCustomFilterDetailsPanel extends JPanel {
	private CustomFilterBean filter;
	private JLabel label = new JLabel();
	private JButton editButton = new JButton("Edit");
	private CrucibleProjectConfiguration projectCrucibleCfg;
	private Project project;
	private CfgManager cfgManager;
	private FilterTree tree;
	private Collection<CustomFilterChangeListener> listeners = new ArrayList<CustomFilterChangeListener>();

	public CrucibleCustomFilterDetailsPanel(final Project project, final CfgManager cfgManager,
											final CrucibleProjectConfiguration crucibleCfg,
											final FilterTree tree) {
		super(new BorderLayout());
		this.projectCrucibleCfg = crucibleCfg;
		this.project = project;
		this.cfgManager = cfgManager;
		this.tree = tree;

		filter = crucibleCfg.getCrucibleFilters().getManualFilter();
		init();
	}

	private void init() {
		setLabelText();

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		buttonPanel.add(editButton);
		JScrollPane scrollPane = new JScrollPane(label, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		scrollPane.getViewport().setBackground(label.getBackground());

		scrollPane.setWheelScrollingEnabled(true);
		add(scrollPane, BorderLayout.CENTER);
		TitledBorder border = BorderFactory.createTitledBorder("Custom Filter");

		this.setBorder(border);

		this.add(buttonPanel, BorderLayout.SOUTH);

		initListeners();
	}

	private void initListeners() {
		CrucibleFilterSelectionListener listener = new CrucibleFilterSelectionListener() {
			public void filterChanged() {
			}

			public void selectedCustomFilter(CustomFilter customFilter) {
				CrucibleCustomFilterDetailsPanel.this.filter = (CustomFilterBean) customFilter;
				setLabelText();
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
					setLabelText();
					// refresh reviews panel
					for (CustomFilterChangeListener listener : listeners) {
						listener.customFilterChanged(filter);
					}

				}
			}
		});
	}

	private void setLabelText() {
		if (filter != null) {
			String html = "<html><body><table>";
			HashMap<String, String> map = filter.getPropertiesMap();
			for (Object key : map.keySet()) {
				if (key.toString().equals("Server")) {
					ServerId serverId = new ServerId(map.get(key));
					ServerCfg server = cfgManager.getServer(CfgUtil.getProjectId(project), serverId);
						html += "<tr><td>" + key + ":</td><td>"
								// server is null when component is initialized by the PICO for the first time
								// todo force cfgManager to read configuration earlier
								+ (server != null ? server.getName() : "")
								+ "</td></tr>";
				} else {
					html += "<tr><td>" + key + ":</td><td>" + map.get(key) + "</td></tr>";
				}
			}
			html += "</table></body></html>";
			label.setText(html);
		}
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
