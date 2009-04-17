package com.atlassian.theplugin.idea;

import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.awt.*;

public class PaZuCheckinHandlerFactory extends CheckinHandlerFactory {
    @NotNull
    public CheckinHandler createHandler(CheckinProjectPanel checkinProjectPanel) {
        return new Handler(checkinProjectPanel);
    }

    private class Handler extends CheckinHandler {
        private CheckinProjectPanel checkinProjectPanel;

        private RefreshableOnComponent beforeCheckinConfig = new BeforeCheckinConfiguration();
        private RefreshableOnComponent afterCheckinConfig = new AfterCheckinConfiguration();

        public Handler(CheckinProjectPanel checkinProjectPanel) {
            this.checkinProjectPanel = checkinProjectPanel;
        }

        @Override
        public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
            return beforeCheckinConfig;
        }

        @Override
        public RefreshableOnComponent getAfterCheckinConfigurationPanel() {
            return afterCheckinConfig;
        }

        @Override
        public ReturnResult beforeCheckin(@Nullable CommitExecutor commitExecutor) {
            System.out.println("PaZu handler - before checkin: " + commitExecutor);
            return ReturnResult.COMMIT;
        }

        @Override
        public ReturnResult beforeCheckin() {
            System.out.println("PaZu handler - before checkin (no args)");
            return ReturnResult.COMMIT;
        }

        @Override
        public void checkinSuccessful() {
            System.out.println("PaZu handler - checkin successful");
        }

        @Override
        public void checkinFailed(List<VcsException> vcsExceptions) {
            System.out.println("PaZu handler - checkin failed: " + vcsExceptions);
        }
    }

    private class BeforeCheckinConfiguration implements RefreshableOnComponent {
        public JComponent getComponent() {
            JPanel p = new JPanel();
            p.add(new JLabel("PaZu bccp"));
            p.setPreferredSize(new Dimension(40, 40));
            p.setBackground(Color.BLUE);
            return p;
        }

        public void refresh() {
        }

        public void saveState() {
        }

        public void restoreState() {
        }
    }

    private class AfterCheckinConfiguration implements RefreshableOnComponent {
        public JComponent getComponent() {
            JPanel p = new JPanel();
            p.add(new JLabel("PaZu accp"));
            p.setPreferredSize(new Dimension(40, 40));
            p.setBackground(Color.RED);
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
