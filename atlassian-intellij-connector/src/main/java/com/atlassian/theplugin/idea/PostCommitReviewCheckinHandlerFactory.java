package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.configuration.CrucibleConfigurationBean;
import com.atlassian.theplugin.configuration.CrucibleWorkspaceConfiguration;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.config.IntelliJProjectCfgManager;
import com.atlassian.theplugin.idea.crucible.CrucibleCreatePostCommitReviewDelayedForm;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class PostCommitReviewCheckinHandlerFactory extends CheckinHandlerFactory {
	private CrucibleWorkspaceConfiguration config;
	private final IntelliJProjectCfgManager projectCfgManager;
    private CrucibleConfigurationBean cruciblePluginConfig;

    public PostCommitReviewCheckinHandlerFactory(@NotNull final ProjectConfigurationBean projectConfiguration,
			@NotNull IntelliJProjectCfgManager cfgManager,
            @NotNull PluginConfigurationBean pluginCfg) {
		this.projectCfgManager = cfgManager;
		config = projectConfiguration.getCrucibleConfiguration();
        cruciblePluginConfig = pluginCfg.getCrucibleConfigurationData();
	}

	@NotNull
	public CheckinHandler createHandler(CheckinProjectPanel checkinProjectPanel) {
		return new Handler(checkinProjectPanel);
	}

	private class Handler extends CheckinHandler {
		private CheckinProjectPanel checkinProjectPanel;
		private JCheckBox cbCreateReview = new JCheckBox("Create Crucible review");

		private RefreshableOnComponent afterCheckinConfig = new AfterCheckinConfiguration();
		private CrucibleCreatePostCommitReviewDelayedForm form;

		public Handler(CheckinProjectPanel checkinProjectPanel) {
			this.checkinProjectPanel = checkinProjectPanel;
			cbCreateReview.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					config.setCreateReviewOnCommit(cbCreateReview.isSelected());
				}
			});
		}

		@Override
		public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
			return super.getBeforeCheckinConfigurationPanel();
		}

		@Override
		public RefreshableOnComponent getAfterCheckinConfigurationPanel() {
			return afterCheckinConfig;
		}

		@Override
		public ReturnResult beforeCheckin(@Nullable CommitExecutor commitExecutor) {
			if (cbCreateReview.isSelected()) {
				form = new CrucibleCreatePostCommitReviewDelayedForm(
						checkinProjectPanel.getProject(), CrucibleServerFacadeImpl.getInstance(),
						projectCfgManager, cruciblePluginConfig, checkinProjectPanel.getCommitMessage(),
						checkinProjectPanel.getVirtualFiles());
				form.show();
			}
			return ReturnResult.COMMIT;
		}

		@Override
		public ReturnResult beforeCheckin() {
			return beforeCheckin(null);
		}

		@Override
		public void checkinSuccessful() {
			if (form != null) {
				form.startReviewCreation();
			}
		}

		@Override
		public void checkinFailed(List<VcsException> vcsExceptions) {
		}

		private class AfterCheckinConfiguration implements RefreshableOnComponent {

			public JComponent getComponent() {
				JPanel p = new JPanel(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.weightx = 0.0;
				gbc.weighty = 0.0;
				gbc.fill = GridBagConstraints.NONE;
				p.add(cbCreateReview, gbc);
				gbc.gridx++;
				gbc.weightx = 1.0;
				gbc.fill = GridBagLayoutConstraints.HORIZONTAL;
				p.add(new JPanel(), gbc);
				return p;
			}

			public void refresh() {
			}

			public void saveState() {
				config.setCreateReviewOnCommit(cbCreateReview.isSelected());
			}

			public void restoreState() {
				cbCreateReview.setSelected(config.isCreateReviewOnCommit());
			}
		}
	}
}
