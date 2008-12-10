package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.configuration.CrucibleProjectConfiguration;
import com.atlassian.theplugin.crucible.model.CrucibleFilterListModel;
import com.atlassian.theplugin.crucible.model.CrucibleFilterListModelListener;
import com.atlassian.theplugin.idea.crucible.tree.FilterTree;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;

/**
 * User: pmaruszak
 */
public class CrucibleCustomFilterDetailsPanel extends JPanel {
	private final CrucibleFilterListModel filterModel;
	private CustomFilterBean filter;
	private JLabel label = new JLabel();
	private JButton editButton = new JButton("Edit");
	private CrucibleProjectConfiguration projectCrucibleCfg;
	private Project project;
	private CfgManager cfgManager;

	public CrucibleCustomFilterDetailsPanel(final Project project, final CfgManager cfgManager,
											final CrucibleProjectConfiguration crucibleCfg,
											final CrucibleFilterListModel filterModel,
											final FilterTree tree) {
		super(new BorderLayout());
		this.filterModel = filterModel;
		this.projectCrucibleCfg = crucibleCfg;
		this.project = project;
		this.cfgManager = cfgManager;

		setLabelText();
		filter = crucibleCfg.getCrucibleFilters().getManualFilter();


		CrucibleFilterListModelListener listener = new CrucibleFilterListModelListener() {
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

		filterModel.addListener(listener);
		tree.addListener(listener);

		editButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {

				CrucibleCustomFilterDialog dialog =
						new CrucibleCustomFilterDialog(project, cfgManager,
								projectCrucibleCfg.getCrucibleFilters().getManualFilter());
				dialog.show();
				if (dialog.getExitCode() == 0 && dialog.getFilter() != null) {
					filter = dialog.getFilter();
					projectCrucibleCfg.getCrucibleFilters().setManualFilter(filter);
					setLabelText();
				}
			}
		});

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
	}

	private void setLabelText() {
		if (filter != null) {
			String html = "<html><body><table>";
			HashMap<String, String> map = filter.getPropertiesMap();
			for (Object key : map.keySet()) {
				if (key.toString().equals("Server")) {
					ServerId serverId = new ServerId(map.get(key));
					ServerCfg server = cfgManager.getServer(CfgUtil.getProjectId(project), serverId);
					html += "<tr><td>" + key + ":</td><td>" + server.getName() + "</td></tr>";
				} else {
					html += "<tr><td>" + key + ":</td><td>" + map.get(key) + "</td></tr>";
				}
			}
			html += "</table></body></html>";
			label.setText(html);
		}
	}
}
