package com.atlassian.theplugin.idea;

import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.idea.crucible.CrucibleCreatePostCommitReviewDelayedForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.awt.*;

public class PostCommitReviewCheckinHandlerFactory extends CheckinHandlerFactory {
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
                        IdeaHelper.getCfgManager(), checkinProjectPanel.getCommitMessage(),
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
            }

            public void restoreState() {
            }
        }
    }
}
