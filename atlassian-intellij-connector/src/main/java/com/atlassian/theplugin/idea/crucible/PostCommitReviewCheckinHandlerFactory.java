package com.atlassian.theplugin.idea.crucible;

import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.theplugin.commons.configuration.CrucibleConfigurationBean;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.configuration.CrucibleWorkspaceConfiguration;
import com.atlassian.theplugin.configuration.WorkspaceConfigurationBean;
import com.atlassian.theplugin.idea.GridBagLayoutConstraints;
import com.atlassian.theplugin.idea.NullCheckinHandler;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class PostCommitReviewCheckinHandlerFactory extends CheckinHandlerFactory {
	private final CrucibleWorkspaceConfiguration config;
	private final ProjectCfgManagerImpl projectCfgManager;
	private final CrucibleConfigurationBean cruciblePluginConfig;

	public PostCommitReviewCheckinHandlerFactory(@NotNull final WorkspaceConfigurationBean projectConfiguration,
			@NotNull ProjectCfgManagerImpl cfgManager,
			@NotNull PluginConfigurationBean pluginCfg) {
		this.projectCfgManager = cfgManager;
		config = projectConfiguration.getCrucibleConfiguration();
		cruciblePluginConfig = pluginCfg.getCrucibleConfigurationData();
	}

	@Override
	@NotNull
	public CheckinHandler createHandler(CheckinProjectPanel checkinProjectPanel) {
        // PL-1604 - the only way to detect that we are in the "Commit" dialog and not in the
        // "Create Patch" dialog seems to be the fact that the VCS list has non-zero length
        if (checkinProjectPanel.getAffectedVcses().size() > 0) {
    		return new Handler(checkinProjectPanel);
        }
        return new NullCheckinHandler();
	}

	private class Handler extends CheckinHandler {
		private final CheckinProjectPanel checkinProjectPanel;
		private final JCheckBox cbCreateReview = new JCheckBox("Create Crucible review");

		private final RefreshableOnComponent afterCheckinConfig = new AfterCheckinConfiguration();
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
                LoggerImpl.getInstance().info("PostCommitReviewCheckinHandlerFactory.beforeCheckin() - showing post-commit form");
				form =
						new CrucibleCreatePostCommitReviewDelayedForm(checkinProjectPanel.getProject(),
								IntelliJCrucibleServerFacade.getInstance(), projectCfgManager, cruciblePluginConfig,
								checkinProjectPanel.getCommitMessage(), checkinProjectPanel.getVirtualFiles());
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
                LoggerImpl.getInstance().info("PostCommitReviewCheckinHandlerFactory.checkinSuccessful() - starting review creation");
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
