/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea.crucible;

import static com.intellij.openapi.ui.Messages.showMessageDialog;
import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.util.PluginUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.ui.UIUtil;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;


public class CrucibleChangeReviewStateForm extends DialogWrapper {
    private static final Icon ICON_COMPLETED = IconLoader.getIcon("/icons/icn_complete.gif");

    private JPanel rootComponent;
    private JPanel detailsPanel;
    private JPanel summaryPanel;
    private JPanel commentsPanel;
    private JCheckBox publishDraftsCheckBox;
    private JPanel publishPanel;

    private final ReviewAdapter review;
    private final IntelliJCrucibleServerFacade crucibleServerFacade;
    private final CrucibleAction action;
    private DescriptionPanel descriptionPanel;
    private final Project project;

    protected CrucibleChangeReviewStateForm(Project project, ReviewAdapter review, CrucibleAction action) {
        super(false);
        this.review = review;
        this.action = action;
        this.crucibleServerFacade = IntelliJCrucibleServerFacade.getInstance();
        this.project = project;

        $$$setupUI$$$();
        init();

        publishPanel.setVisible(false);

		if (CrucibleAction.SUMMARIZE.equals(action)) {
			setTitle("Summarize and Close Review");
			getOKAction().putValue(Action.NAME, "Summarize and Close Review");
		} else {
			setTitle(action.getDisplayName());
			getOKAction().putValue(Action.NAME, action.getDisplayName());

		}

		if (CrucibleAction.COMPLETE.equals(action)) {
			publishPanel.setVisible(true);
		} else if (CrucibleAction.CLOSE.equals(action)) {
			summaryPanel.setBackground(UIUtil.getWindowColor());
        }
    }

    public void showDialog() {
        getOKAction().setEnabled(false);

        Task.Backgroundable fillTask = new Task.Backgroundable(project, "Retrieving Review Data", false) {

            @Override
            public void run(@NotNull final ProgressIndicator indicator) {
                try {
                    review.fillReview(crucibleServerFacade.getReview(review.getServerData(), review.getPermId()));
                } catch (RemoteApiException e) {
                    PluginUtil.getLogger().warn(e);
                } catch (ServerPasswordNotProvidedException e) {
                    PluginUtil.getLogger().warn(e);
                }
            }

            @Override
            public void onSuccess() {
                updateReviewInfo(review);
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        show();
                    }
                });
            }
        };

        ProgressManager.getInstance().run(fillTask);
    }

    public void updateReviewInfo(final ReviewAdapter reviewInfo) {
        detailsPanel.add(new DetailsPanel(reviewInfo), BorderLayout.CENTER);
        if (CrucibleAction.CLOSE.equals(action) || CrucibleAction.SUMMARIZE.equals(action) ||
                !"".equals(reviewInfo.getSummary())) {
            boolean isEditable = CrucibleAction.CLOSE.equals(action) || CrucibleAction.SUMMARIZE.equals(action);
            descriptionPanel = new DescriptionPanel(review, isEditable);
            summaryPanel.add(descriptionPanel, BorderLayout.CENTER);
        } else {
            summaryPanel.setVisible(false);
        }
        commentsPanel.add(new CommentsPanel(review), BorderLayout.CENTER);
        getOKAction().setEnabled(true);
        pack();
    }

    public JComponent getRootComponent() {
        return rootComponent;
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        return getRootComponent();
    }

    @Override
    protected void doOKAction() {
        Task.Backgroundable task = new Task.Backgroundable(project, "Changing review state") {

            @Override
			public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    runTransition(descriptionPanel != null ? descriptionPanel.getText() : null);
                } catch (RemoteApiException e) {
                    showErrorMessage(e.getMessage());
                } catch (ServerPasswordNotProvidedException e) {
                    showErrorMessage(e.getMessage());
                }
            }
        };

        ProgressManager.getInstance().run(task);

        super.doOKAction();
    }

    private void runTransition(String description) throws ServerPasswordNotProvidedException, RemoteApiException {
        if (description == null) {
            description = "";
        }
		if (CrucibleAction.SUMMARIZE.equals(action)) {
			crucibleServerFacade.changeReviewState(review.getServerData(), review.getPermId(), CrucibleAction.SUMMARIZE);
			crucibleServerFacade.closeReview(review.getServerData(), review.getPermId(), description);
		} else if (CrucibleAction.CLOSE.equals(action)) {
			crucibleServerFacade.closeReview(review.getServerData(), review.getPermId(), description);
		} else if (CrucibleAction.COMPLETE.equals(action)) {
			if (this.publishDraftsCheckBox.isSelected()) {
				crucibleServerFacade.publishAllCommentsForReview(review.getServerData(),
						review.getPermId());
			}
			crucibleServerFacade.completeReview(review.getServerData(), review.getPermId(), true);
		} else if (CrucibleAction.UNCOMPLETE.equals(action)) {
			crucibleServerFacade.completeReview(review.getServerData(), review.getPermId(), false);
		} else {
			crucibleServerFacade.changeReviewState(review.getServerData(), review.getPermId(), action);
		}
			
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                IdeaHelper.getAppComponent().rescheduleStatusCheckers(true);
            }
        });
    }

    private void showErrorMessage(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showMessageDialog(message, "Error changing review state: " + review.getServerData().getUrl(),
                        Messages.getErrorIcon());
            }
        });

    }

    private void createUIComponents() {
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootComponent = new JPanel();
        rootComponent.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootComponent.setMinimumSize(new Dimension(-1, -1));
        detailsPanel = new JPanel();
        detailsPanel.setLayout(new BorderLayout(0, 0));
        rootComponent.add(detailsPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        commentsPanel = new JPanel();
        commentsPanel.setLayout(new BorderLayout(0, 0));
        rootComponent.add(commentsPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        publishPanel = new JPanel();
        publishPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootComponent.add(publishPanel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        publishDraftsCheckBox = new JCheckBox();
        publishDraftsCheckBox.setSelected(true);
        publishDraftsCheckBox.setText("Publish all my draft comments");
        publishPanel.add(publishDraftsCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        publishPanel.add(spacer1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        summaryPanel = new JPanel();
        summaryPanel.setLayout(new BorderLayout(0, 0));
        rootComponent.add(summaryPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootComponent;
    }

    private class BoldLabel extends JLabel {
        public BoldLabel(String text) {
            super(text);
            setFont(getFont().deriveFont(Font.BOLD));
        }

        public BoldLabel() {
            this("");
        }
    }

    private class ReviewLabel extends HyperlinkLabel {
        ReviewLabel(final ReviewAdapter review) {
            super(review.getPermId().getId());
            addListener(review.getReviewUrl());
        }

        private void addListener(final String reviewUrl) {
            addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    BrowserUtil.launchBrowser(reviewUrl);
                }
            });
        }
    }

    private class DetailsPanel extends JPanel {
        public DetailsPanel(final ReviewAdapter review) {
            JPanel body = new JPanel();

            setLayout(new GridBagLayout());
            body.setLayout(new GridBagLayout());

            GridBagConstraints gbc1 = new GridBagConstraints();
            GridBagConstraints gbc2 = new GridBagConstraints();
            gbc1.anchor = GridBagConstraints.FIRST_LINE_START;
            gbc1.insets = new Insets(0, Constants.DIALOG_MARGIN, 0, Constants.DIALOG_MARGIN);
            gbc2.anchor = GridBagConstraints.FIRST_LINE_START;
            gbc2.fill = GridBagConstraints.HORIZONTAL;
            gbc2.weightx = 1.0;
            gbc1.gridx = 0;
            gbc2.gridx = 1;
            gbc1.gridy = 0;
            gbc2.gridy = 0;

            body.add(new BoldLabel("Id"), gbc1);
            body.add(new ReviewLabel(review), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            body.add(new BoldLabel("Name"), gbc1);
            body.add(new JLabel(review.getName(), SwingConstants.LEFT), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            body.add(new BoldLabel("State"), gbc1);
            body.add(new JLabel(review.getState().value(), SwingConstants.LEFT), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            body.add(new BoldLabel("Author"), gbc1);
            body.add(new JLabel(review.getAuthor().getDisplayName(), SwingConstants.LEFT), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            if (review.getModerator() != null) {
                body.add(new BoldLabel("Moderator"), gbc1);
                body.add(new JLabel(review.getModerator().getDisplayName(), SwingConstants.LEFT), gbc2);
                gbc1.gridy++;
                gbc2.gridy++;
            }
            body.add(new BoldLabel("Project Key"), gbc1);
            body.add(new JLabel(review.getProjectKey(), SwingConstants.LEFT), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            body.add(new BoldLabel("Repository Name"), gbc1);
            body.add(new JLabel(review.getRepoName(), SwingConstants.LEFT), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            body.add(new BoldLabel("Created"), gbc1);
            if (review.getCreateDate() != null) {
                body.add(new JLabel(review.getCreateDate().toString()), gbc2);
            }
            gbc1.gridy++;
            gbc2.gridy++;
            body.add(new BoldLabel("Closed"), gbc1);
            if (review.getCloseDate() != null) {
                body.add(new JLabel(review.getCloseDate().toString()), gbc2);
            }
            gbc1.gridy++;
            gbc2.gridy++;

            body.add(new BoldLabel("Reviewers"), gbc1);

			for (Reviewer reviewer : review.getReviewers()) {
				if (reviewer.isCompleted()) {
					body.add(new JLabel(reviewer.getDisplayName(), ICON_COMPLETED, SwingConstants.LEFT), gbc2);
				} else {
					body.add(new JLabel(reviewer.getDisplayName(), SwingConstants.LEFT), gbc2);
				}
				gbc1.gridy++;
				gbc2.gridy++;
			}

            gbc1.gridy++;
            gbc1.weighty = 1.0;
            gbc1.fill = GridBagConstraints.VERTICAL;
            body.add(new JPanel(), gbc1);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            JScrollPane scroll = new JScrollPane(body);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            add(scroll, gbc);

            Border b = BorderFactory.createTitledBorder("Review");
            setBorder(b);
            Insets i = b.getBorderInsets(this);
            setMinimumSize(new Dimension(0, i.top + i.bottom));

        }
    }

    private class CommentsPanel extends JPanel {
        public CommentsPanel(final ReviewAdapter review) {
            JPanel body = new JPanel();

            setLayout(new GridBagLayout());
            body.setLayout(new GridBagLayout());

            GridBagConstraints gbc1 = new GridBagConstraints();
            GridBagConstraints gbc2 = new GridBagConstraints();
            gbc1.anchor = GridBagConstraints.FIRST_LINE_START;
            gbc1.insets = new Insets(0, Constants.DIALOG_MARGIN, 0, Constants.DIALOG_MARGIN);
            gbc2.anchor = GridBagConstraints.FIRST_LINE_START;
            gbc2.fill = GridBagConstraints.HORIZONTAL;
            gbc2.weightx = 1.0;
            gbc1.gridx = 0;
            gbc2.gridx = 1;
            gbc1.gridy = 0;
            gbc2.gridy = 0;

            String userName = review.getServerData().getUsername();

			String totalComments = review.getNumberOfGeneralComments() + review.getNumberOfVersionedComments() + "";


			String myAllComments = review.getNumberOfGeneralComments(userName)
					+ review.getNumberOfVersionedComments(userName) + "";


			String myDrafts = review.getNumberOfGeneralCommentsDrafts(userName)
					+ review.getNumberOfVersionedCommentsDrafts(userName) + "";

			String myDefects = review.getNumberOfGeneralCommentsDefects(userName)
					+ review.getNumberOfVersionedCommentsDefects(userName) + "";

			String allDefects = review.getNumberOfGeneralCommentsDefects() + review.getNumberOfVersionedCommentsDefects()
					+ "";

			String allDrafts = review.getNumberOfVersionedCommentsDrafts() + review.getNumberOfGeneralCommentsDrafts() + "";

            body.add(new BoldLabel("My Draft Comments"), gbc1);
            body.add(new JLabel(myDrafts, SwingConstants.LEFT), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;

            body.add(new BoldLabel("My Defects Comments"), gbc1);
            body.add(new JLabel(myDefects, SwingConstants.LEFT), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;

            body.add(new BoldLabel("My All Comments"), gbc1);
            body.add(new JLabel(myAllComments, SwingConstants.LEFT), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;

            body.add(new BoldLabel("Total Comments"), gbc1);
            body.add(new JLabel(totalComments, SwingConstants.LEFT), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;

            body.add(new BoldLabel("Total Draft Comments"), gbc1);
            body.add(new JLabel(allDrafts, SwingConstants.LEFT), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;

            body.add(new BoldLabel("Total Defects"), gbc1);
            body.add(new JLabel(allDefects, SwingConstants.LEFT), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;

            gbc1.gridy++;
            gbc1.weighty = 1.0;
            gbc1.fill = GridBagConstraints.VERTICAL;
            body.add(new JPanel(), gbc1);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            JScrollPane scroll = new JScrollPane(body);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            add(scroll, gbc);

            Border b = BorderFactory.createTitledBorder("Comments");
            setBorder(b);
            Insets i = b.getBorderInsets(this);
            setMinimumSize(new Dimension(0, i.top + i.bottom));

        }
    }

    private class DescriptionPanel extends JPanel {
        private final JEditorPane body = new JEditorPane();

        public DescriptionPanel(final ReviewAdapter review, boolean isEditable) {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = 0;
            gbc.gridy = 0;

            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;

            JScrollPane sp = new JScrollPane(body, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            body.addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        BrowserUtil.launchBrowser(e.getURL().toString());
                    }
                }
            });

            body.setEditable(isEditable);
            body.setText(review.getSummary());
            sp.getViewport().setOpaque(false);
            body.setCaretPosition(0);
            if (isEditable) {
                body.setBackground(UIUtil.getTextFieldBackground());
            } else {
                body.setBackground(getBackground());
                body.setBorder(BorderFactory.createEmptyBorder());
                sp.setBorder(BorderFactory.createEmptyBorder());
                body.setForeground(UIUtil.getTextFieldForeground());
            }
            add(sp, gbc);

            Border b = BorderFactory.createTitledBorder("Review Summary");
            setBorder(b);
            Insets i = b.getBorderInsets(this);
            int minHeight = i.top + i.bottom;
            setMinimumSize(new Dimension(0, minHeight + 100));
        }


        public String getText() {
            return body.getText();
        }
    }
}
