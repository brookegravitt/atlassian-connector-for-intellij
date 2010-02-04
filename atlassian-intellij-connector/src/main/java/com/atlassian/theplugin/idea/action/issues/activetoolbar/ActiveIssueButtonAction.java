package com.atlassian.theplugin.idea.action.issues.activetoolbar;

import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.cache.CachedIconLoader;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.atlassian.theplugin.jira.cache.RecentlyOpenIssuesCache;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText;
import com.intellij.openapi.project.Project;
import com.intellij.util.ImageLoader;
import com.intellij.util.ui.EmptyIcon;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * User: kalamon
 * Date: Jul 16, 2009
 * Time: 12:10:54 PM
 */
public class ActiveIssueButtonAction extends AnAction implements CustomComponentAction {

    private static final int SUMMARY_LENGHT = 50;
    private static final Dimension MIN_SIZE = new Dimension(1, 21);
    private static final Insets INSETS = new Insets(2, 2, 2, 2);
    private static final Icon EMPTY_ICON = new EmptyIcon(1, 18);

    private PropertyChangeListener synchronizer;

    public void actionPerformed(final AnActionEvent event) {
        openIssue(event);
    }

    private void openIssue(final AnActionEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final Project currentProject = IdeaHelper.getCurrentProject(event);
                final JiraIssueAdapter issue;
                if (currentProject != null) {
                    final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(currentProject);
                    try {
                        issue = ActiveIssueUtils.getJIRAIssue(currentProject);
                        if (issue != null) {
                            if (panel != null) {
                                panel.openIssue(issue, true);
                            }
                        }
                    } catch (JIRAException e) {
                        if (panel != null) {
                            panel.setStatusErrorMessage("Error opening issue: " + e.getMessage(), e);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void update(final AnActionEvent event) {
        ActiveJiraIssue activeIssue = ActiveIssueUtils.getActiveJiraIssue(event);
        String text = "No active issue";
        String tooltip = "";
        JiraIssueAdapter issue = null;
        RecentlyOpenIssuesCache cache = IdeaHelper.getProjectComponent(event, RecentlyOpenIssuesCache.class);

        if (activeIssue != null) {
            text = activeIssue.getIssueKey();
            if (activeIssue.isPaused()) {
                text = text + " (paused)";
            }
            if (cache != null) {
                issue = cache.getLoadedRecenltyOpenIssue(activeIssue.getIssueKey(),
                        activeIssue.getServerId(),
                        activeIssue.getIssueUrl());

                if (issue != null) {
                    tooltip = issue.getSummary();
                    if (tooltip.length() > SUMMARY_LENGHT) {
                        tooltip = tooltip.substring(0, SUMMARY_LENGHT) + "...";
                    }
                    tooltip = text + ": " + tooltip;
                }
            }
        }

        event.getPresentation().setText(text);
        event.getPresentation().setDescription(tooltip);

        if (issue != null) {
            Icon icon = CachedIconLoader.getIcon(issue.getTypeIconUrl());
            // PL-1862 - a Throwable (!) is thrown if icon has bad size (how's that for meaningful error handling :))
            if (ImageLoader.isGoodSize(icon)) {
                event.getPresentation().setIcon(icon);
            }
        } else {
            event.getPresentation().setIcon(null);
        }

        event.getPresentation().setEnabled(activeIssue != null);
    }

    public JComponent createCustomComponent(Presentation presentation) {
        return new MyButtonWithText(this, presentation);
    }

    private final class MyButtonWithText extends ActionButtonWithText {
        private Presentation presentation;

        private class MySynchronizer implements PropertyChangeListener {
            public void propertyChange(PropertyChangeEvent event) {
                String propertyName = event.getPropertyName();
                if ("text".equals(propertyName)) {
                    updateButtonSize();
                } else if ("description".equals(propertyName)) {
                    updateTooltipText((String) event.getNewValue());
                } else if ("icon".equals(propertyName)) {
                    updateButtonSize();
                }
            }
        }

        private MyButtonWithText(AnAction anAction, Presentation presentation) {
            super(anAction, presentation, "", MIN_SIZE);
            this.presentation = presentation;
        }

        private void updateTooltipText(String description) {
            String tooltip = AnAction.createTooltipText(description, ActiveIssueButtonAction.this);
            setToolTipText(tooltip.length() <= 0 ? null : tooltip);
        }

        private void updateButtonSize() {
            Dimension dimension = new Dimension(getBasePreferredSize());
            String s = presentation.getText();
            FontMetrics fontmetrics = getFontMetrics(getFont());
            dimension.width += INSETS.left;
            dimension.width += fontmetrics.stringWidth(s);
            setPreferredSize(dimension);
            setSize(dimension);
            Component parent = getParent();
            if (parent != null) {
                parent.validate();
            }
        }

        public Dimension getBasePreferredSize() {
            if (presentation.getIcon() == null) {
                return MIN_SIZE;
            } else {
                return new Dimension(
                        presentation.getIcon().getIconWidth() + INSETS.left + INSETS.right,
                        presentation.getIcon().getIconHeight() + INSETS.top + INSETS.bottom);
            }
        }

        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected Icon getIcon() {
            if (presentation.getIcon() != null) {
                return presentation.getIcon();
            }
            return EMPTY_ICON;
        }

        @Override
        public void addNotify() {
            super.addNotify();
            if (synchronizer == null) {
                synchronizer = new MySynchronizer();
                presentation.addPropertyChangeListener(synchronizer);
            }
            updateButtonSize();
        }

        @Override
        public void removeNotify() {
            if (synchronizer != null) {
                presentation.removePropertyChangeListener(synchronizer);
                synchronizer = null;
            }
            super.removeNotify();
        }
    }
}
