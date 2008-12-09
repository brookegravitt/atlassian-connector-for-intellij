package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.configuration.CrucibleProjectConfiguration;
import com.atlassian.theplugin.crucible.model.CrucibleFilterListModel;
import com.atlassian.theplugin.crucible.model.CrucibleFilterListModelListener;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: pmaruszak
 */
public class CrucibleCustomFilterDetailsPanel extends JPanel {
	final private CrucibleFilterListModel filterModel;
	private CustomFilter filter;
	private JLabel label = new JLabel();
	private JButton editButton = new JButton("Edit");
	private CrucibleProjectConfiguration projectCrucibleCfg;

	public CrucibleCustomFilterDetailsPanel(final Project project, final CfgManager cfgManager,
											final CrucibleProjectConfiguration crucibleCfg,
											final CrucibleFilterListModel filterModel){
		super(new BorderLayout());
		this.filterModel = filterModel;
		this.projectCrucibleCfg = crucibleCfg;		

		setLabelText();
		filter = filterModel.getCustomFilter();

		filterModel.addListener(new CrucibleFilterListModelListener(){
			public void filterChanged() {
			}

			public void selectedCustomFilter(CustomFilter customFilter) {
				CrucibleCustomFilterDetailsPanel.this.filter = customFilter;
				setLabelText();
			}

			public void selectedCustomFilter() {

			}

			public void selectedPredefinedFilter(PredefinedFilter selectedPredefinedFilter) {

			}

		});

		editButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {

				CrucibleCustomFilterDialog dialog =
						new CrucibleCustomFilterDialog(project, cfgManager, filterModel, 
								projectCrucibleCfg.getCrucibleFilters().getManualFilter()) ;
				dialog.show();
				if (dialog.getExitCode() == 0 && dialog.getFilter() != null) {
					final CustomFilterBean customFilterBean = dialog.getFilter();
					projectCrucibleCfg.getCrucibleFilters().setManualFilter(customFilterBean);
					filterModel.setCustomFilter(customFilterBean);
				}

			}
		});

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		buttonPanel.add(editButton);

		this.add(buttonPanel, BorderLayout.SOUTH);
	}

	private void setLabelText() {
		if (filter != null) {
			label.setText(filter.toHtml());
		}
	}
}
