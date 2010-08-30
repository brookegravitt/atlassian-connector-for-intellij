package com.atlassian.theplugin.idea.crucible;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.connector.intellij.fisheye.IntelliJFishEyeServerFacade;
import com.atlassian.theplugin.commons.configuration.CrucibleConfigurationBean;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.configuration.CrucibleWorkspaceConfiguration;
import com.atlassian.theplugin.configuration.WorkspaceConfigurationBean;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.NullCheckinHandler;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.changes.committed.CommittedChangesCache;
import com.intellij.openapi.vcs.changes.ui.CommitChangeListDialog;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.vcs.versionBrowser.ChangeBrowserSettings;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.util.Consumer;
import com.intellij.util.PairConsumer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

public class PostCommitReviewCheckinHandlerFactory extends CheckinHandlerFactory {
	private final CrucibleWorkspaceConfiguration config;
    private final WorkspaceConfigurationBean projectConfiguration;
    private final ProjectCfgManager projectCfgManager;
    private final Project project;
    private final CrucibleConfigurationBean cruciblePluginConfig;

    public PostCommitReviewCheckinHandlerFactory(@NotNull final WorkspaceConfigurationBean projectConfiguration,
			@NotNull ProjectCfgManager cfgManager,
			@NotNull PluginConfigurationBean pluginCfg, @NotNull Project project) {
        this.projectConfiguration = projectConfiguration;
        this.projectCfgManager = cfgManager;
        this.project = project;
        config = projectConfiguration.getCrucibleConfiguration();
		cruciblePluginConfig = pluginCfg.getCrucibleConfigurationData();
	}

	@Override
	@NotNull
	public CheckinHandler createHandler(CheckinProjectPanel checkinProjectPanel) {
        // PL-1604 - the only way to detect that we are in the "Commit" dialog and not in the
        // "Create Patch" dialog seems to be the fact that the VCS list has non-zero length
        if (((CommitChangeListDialog) checkinProjectPanel).getAffectedVcses().size() > 0) {
    		return new Handler(checkinProjectPanel);
        }
        return new NullCheckinHandler();
	}

	private class Handler extends CheckinHandler {
		private final CheckinProjectPanel checkinProjectPanel;
		private final JCheckBox cbCreateReview = new JCheckBox("Create Crucible review");
        private final JCheckBox cbAddChangesetToReview = new JCheckBox("Add Changeset to review");

		private final RefreshableOnComponent afterCheckinConfig = new AfterCheckinConfiguration();
		private CrucibleCreatePostCommitReviewDelayedForm commitForm;
        private static final String ERROR_REFRESHING_VCS_HISTORY = "Error refreshing VCS history";
        private static final int REVISIONS_COUNT = 10;

        public Handler(CheckinProjectPanel checkinProjectPanel) {
			this.checkinProjectPanel = checkinProjectPanel;
			cbCreateReview.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
                    boolean selected = cbCreateReview.isSelected();
                    config.setCreateReviewOnCommit(selected);
                    if (selected) {
                        cbAddChangesetToReview.setSelected(false);
                    }
				}
			});
            cbAddChangesetToReview.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    boolean selected = cbAddChangesetToReview.isSelected();
                    config.setAddChangesetToReviewOnCommit(selected);
                    if (selected) {
                        cbCreateReview.setSelected(false);
                    }
                }
            });
		}

		@Override
		public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
			return super.getBeforeCheckinConfigurationPanel();
		}


        public RefreshableOnComponent getAfterCheckinConfigurationPanel(Disposable parentDisposable) {
            return super.getAfterCheckinConfigurationPanel(parentDisposable);    //To change body of overridden methods use File | Settings | File Templates.
        }

        
		public RefreshableOnComponent getAfterCheckinConfigurationPanel() {
			return afterCheckinConfig;
		}

        //           @Override     -- different definition for ides X and 9 do not uncomment
        public ReturnResult beforeCheckin(@Nullable CommitExecutor executor, PairConsumer<Object, Object> additionalDataConsumer) {
            return beforeCheckin(executor);
        }
//           @Override     -- different definition for ides X and 9 do not uncomment
        //@Override
		public ReturnResult beforeCheckin(@Nullable CommitExecutor commitExecutor) {
            Project project = checkinProjectPanel.getProject();
			if (cbCreateReview.isSelected()) {
                LoggerImpl.getInstance().info(
                        "PostCommitReviewCheckinHandlerFactory.beforeCheckin() - showing post-commit form");
				commitForm =
						new CrucibleCreatePostCommitReviewDelayedForm(project,
								IntelliJCrucibleServerFacade.getInstance(), projectCfgManager, cruciblePluginConfig,
								checkinProjectPanel.getCommitMessage(), checkinProjectPanel.getVirtualFiles());
				commitForm.show();
			} else {
                commitForm = null;
            }

			return ReturnResult.COMMIT;
		}

		@Override
		public ReturnResult beforeCheckin() {
			return beforeCheckin();
		}

		@Override
		public void checkinSuccessful() {
			if (commitForm != null) {
                LoggerImpl.getInstance().info(
                        "PostCommitReviewCheckinHandlerFactory.checkinSuccessful() - starting review creation");
				commitForm.startReviewCreation();
			}
            if (cbAddChangesetToReview.isSelected()) {
                addChangesetToReview();
            }
		}

        private void addChangesetToReview() {
            final Project project = checkinProjectPanel.getProject();

            final ChangeBrowserSettings changeBrowserSettings = new ChangeBrowserSettings();
            CommittedChangesCache.getInstance(project).getProjectChangesAsync(
                    changeBrowserSettings, REVISIONS_COUNT, false,
                    new Consumer<List<CommittedChangeList>>() {
                        public void consume(List<CommittedChangeList> list) {
                            if (list != null && list.size() > 0) {
                                Collections.reverse(list);
                                for (CommittedChangeList changeList : list) {
                                    if (VcsIdeaHelper.isMyCommittedChangeList(
                                            changeList, checkinProjectPanel.getVirtualFiles())) {

                                        ChangeList[] chlist = new ChangeList[1];
                                        chlist[0] = changeList;
                                        CrucibleHelperForm form = new CrucibleHelperForm(
                                                project, IntelliJFishEyeServerFacade.getInstance(),
                                                IntelliJCrucibleServerFacade.getInstance(),
                                                chlist, IdeaHelper.getProjectCfgManager(project));
                                        form.setChangesetAddTimeout(cruciblePluginConfig.getReviewCreationTimeout());
                                        form.show();
                                        break;
                                    }
                                }
                            } else {
                                AbstractVcsHelper.getInstance(project).showError(null, ERROR_REFRESHING_VCS_HISTORY);
                            }
                        }
                    }, new Consumer<List<VcsException>>() {
                        public void consume(List<VcsException> list) {
                            AbstractVcsHelper helper = AbstractVcsHelper.getInstance(project);
                            if (helper != null) {
                                helper.showErrors(list, ERROR_REFRESHING_VCS_HISTORY);
                            }
                        }
                    });
        }

		@Override
		public void checkinFailed(List<VcsException> vcsExceptions) {
		}

		private class AfterCheckinConfiguration implements RefreshableOnComponent {

			public JComponent getComponent() {
				JPanel p = new JPanel(new FormLayout("pref, fill:pref:grow", "pref, pref"));
                CellConstraints cc = new CellConstraints();
				p.add(cbCreateReview, cc.xy(1, 1));
				p.add(new JPanel(), cc.xy(2, 1));

                p.add(cbAddChangesetToReview, cc.xy(1, 2));
                p.add(new JPanel(), cc.xy(2, 2));

				return p;
			}

			public void refresh() {
			}

			public void saveState() {
				config.setCreateReviewOnCommit(cbCreateReview.isSelected());
                config.setAddChangesetToReviewOnCommit(cbAddChangesetToReview.isSelected());
			}

			public void restoreState() {
                boolean reviewOnCommit = config.isCreateReviewOnCommit();
                cbCreateReview.setSelected(reviewOnCommit);
                if (!reviewOnCommit) {
                    cbAddChangesetToReview.setSelected(config.isAddChangesetToReviewOnCommit());
                }
			}
		}
	}
}
