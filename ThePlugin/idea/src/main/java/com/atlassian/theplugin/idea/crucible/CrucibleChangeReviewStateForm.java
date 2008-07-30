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

import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Action;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import static com.intellij.openapi.ui.Messages.showMessageDialog;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;


public class CrucibleChangeReviewStateForm extends DialogWrapper {
    private static final Icon ICON_COMPLETED = IconLoader.getIcon("/icons/icn_complete.gif");

    private JPanel rootComponent;
    private JPanel detailsPanel;
    private JPanel summaryPanel;
    private JPanel commentsPanel;
    private JCheckBox publishDraftsCheckBox;
    private JPanel publishPanel;

    private ReviewData review;
    private CrucibleServerFacade crucibleServerFacade;
    private Action action;
    private DescriptionPanel descriptionPanel;

    protected CrucibleChangeReviewStateForm(ReviewData review, Action action) {
        super(false);
        this.review = review;
        this.action = action;
        this.crucibleServerFacade = CrucibleServerFacadeImpl.getInstance();

        $$$setupUI$$$();
        init();

        publishPanel.setVisible(false);

        switch (action) {
            case CLOSE:
                setTitle("Close review");
                getOKAction().putValue(javax.swing.Action.NAME, "Close review...");
                break;
            case APPROVE:
                setTitle("Approve review");
                getOKAction().putValue(javax.swing.Action.NAME, "Approve review...");
                break;
            case ABANDON:
                setTitle("Abandon review");
                getOKAction().putValue(javax.swing.Action.NAME, "Abandon review...");
                break;
            case SUMMARIZE:
                setTitle("Summarize review");
                getOKAction().putValue(javax.swing.Action.NAME, "Summarize review...");
                break;
            case REOPEN:
                setTitle("Reopen review");
                getOKAction().putValue(javax.swing.Action.NAME, "Reopen review...");
                break;
            case RECOVER:
                setTitle("Recover abandoned review");
                getOKAction().putValue(javax.swing.Action.NAME, "Recover abandoned review...");
                break;
            case COMPLETE:
                setTitle("Complete review");
                getOKAction().putValue(javax.swing.Action.NAME, "Complete review...");
                publishPanel.setVisible(true);
                break;
            case UNCOMPLETE:
                setTitle("Uncomplete review");
                getOKAction().putValue(javax.swing.Action.NAME, "Uncomplete review...");
                break;
        }

        fillReviewInfo(review);
    }

    private void fillReviewInfo(final ReviewData review) {
        getOKAction().setEnabled(false);

        new Thread(new Runnable() {
            public void run() {
                Review reviewInfo = null;
                try {
                    reviewInfo = crucibleServerFacade.getReview(review.getServer(), review.getPermId());
                } catch (RemoteApiException e) {
                    // nothing can be done here
                } catch (ServerPasswordNotProvidedException e) {
                    // nothing can be done here
                }
                final ReviewData finalReviewInfo = new ReviewDataImpl(reviewInfo, review.getServer());
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        updateReviewInfo(finalReviewInfo);
                    }
                });
            }
        }, "atlassian-idea-plugin crucible patch upload combos refresh").start();
    }

    private void updateReviewInfo(ReviewData reviewInfo) {
        detailsPanel.add(new DetailsPanel(reviewInfo), BorderLayout.CENTER);
        if (Action.CLOSE.equals(action) || !"".equals(reviewInfo.getSummary())) {
            descriptionPanel = new DescriptionPanel(review);
            summaryPanel.add(descriptionPanel, BorderLayout.CENTER);
        }
        commentsPanel.add(new CommentsPanel(review), BorderLayout.CENTER);
        if (Action.CLOSE.equals(action)) {
            descriptionPanel.setEnabled(true);
            descriptionPanel.setEditable(true);
        } else {
            if (!"".equals(reviewInfo.getSummary())) {
                descriptionPanel.setEnabled(false);
                descriptionPanel.setEditable(false);
            }
        }
        pack();
        getOKAction().setEnabled(true);
    }

    public JComponent getRootComponent() {
        return rootComponent;
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return getRootComponent();
    }

    protected void doOKAction() {
        try {
            switch (action) {
                case APPROVE:
                    crucibleServerFacade.approveReview(review.getServer(), review.getPermId());
                    break;
                case ABANDON:
                    crucibleServerFacade.abandonReview(review.getServer(), review.getPermId());
                    break;
                case SUMMARIZE:
                    crucibleServerFacade.summarizeReview(review.getServer(), review.getPermId());
                    break;
                case CLOSE:
                    crucibleServerFacade.closeReview(review.getServer(), review.getPermId(), descriptionPanel.getText());
                    break;
                case REOPEN:
                    crucibleServerFacade.reopenReview(review.getServer(), review.getPermId());
                    break;
                case RECOVER:
                    crucibleServerFacade.recoverReview(review.getServer(), review.getPermId());
                    break;
                case COMPLETE:
                    if (this.publishDraftsCheckBox.isSelected()) {
                        crucibleServerFacade.publishAllCommentsForReview(review.getServer(), review.getPermId());
                    }
                    crucibleServerFacade.completeReview(review.getServer(), review.getPermId(), true);
                    break;
                case UNCOMPLETE:
                    crucibleServerFacade.completeReview(review.getServer(), review.getPermId(), false);
                    break;
            }
            IdeaHelper.getAppComponent().rescheduleStatusCheckers(true);
        } catch (RemoteApiException e) {
            showMessageDialog(e.getMessage(),
                    "Error changing review state: " + review.getServer().getUrlString(), Messages.getErrorIcon());
        } catch (ServerPasswordNotProvidedException e) {
            showMessageDialog(e.getMessage(), "Error changing review state: " + review.getServer().getUrlString(),
                    Messages.getErrorIcon());
        }

        super.doOKAction();
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
        rootComponent.setMinimumSize(new Dimension(450, 300));
        detailsPanel = new JPanel();
        detailsPanel.setLayout(new BorderLayout(0, 0));
        rootComponent.add(detailsPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        commentsPanel = new JPanel();
        commentsPanel.setLayout(new BorderLayout(0, 0));
        rootComponent.add(commentsPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        publishPanel = new JPanel();
        publishPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootComponent.add(publishPanel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        publishDraftsCheckBox = new JCheckBox();
        publishDraftsCheckBox.setSelected(true);
        publishDraftsCheckBox.setText("Publish all my draft comments");
        publishPanel.add(publishDraftsCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        publishPanel.add(spacer1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL,
                1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        summaryPanel = new JPanel();
        summaryPanel.setLayout(new BorderLayout(0, 0));
        rootComponent.add(summaryPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
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
        ReviewLabel(final ReviewData review) {
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
        public DetailsPanel(final ReviewData review) {
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
            body.add(new BoldLabel("Moderator"), gbc1);
            body.add(new JLabel(review.getModerator().getDisplayName(), SwingConstants.LEFT), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            body.add(new BoldLabel("Project key"), gbc1);
            body.add(new JLabel(review.getProjectKey(), SwingConstants.LEFT), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;
            body.add(new BoldLabel("Repository name"), gbc1);
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

            try {
                for (Reviewer reviewer : review.getReviewers()) {
                    if (reviewer.isCompleted()) {
                        body.add(new JLabel(reviewer.getDisplayName(), ICON_COMPLETED, SwingConstants.LEFT), gbc2);
                    } else {
                        body.add(new JLabel(reviewer.getDisplayName(), SwingConstants.LEFT), gbc2);
                    }
                    gbc1.gridy++;
                    gbc2.gridy++;
                }
            } catch (ValueNotYetInitialized valueNotYetInitialized) {
                valueNotYetInitialized
                        .printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
        public CommentsPanel(final ReviewData review) {
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

            String userName = review.getServer().getUserName();
            int generalComments = 0;
            int versionedComments = 0;
            int myGeneralComments = 0;
            int myVersionedComments = 0;
            int myDefects = 0;
            int myDrafts = 0;
            int allDefects = 0;
            int allDrafts = 0;
            try {
                generalComments = review.getGeneralComments().size();
                versionedComments = review.getVersionedComments().size();
                for (GeneralComment generalComment : review.getGeneralComments()) {
                    if (generalComment.getAuthor().equals(userName)) {
                        myGeneralComments++;
                        if (generalComment.isDraft()) {
                            myDrafts++;
                            allDrafts++;
                        }
                        if (generalComment.isDefectRaised()) {
                            myDefects++;
                            allDefects++;
                        }
                    } else {
                        if (generalComment.isDraft()) {
                            allDrafts++;
                        }
                        if (generalComment.isDefectRaised()) {
                            allDefects++;
                        }
                    }

                }
                for (VersionedComment comment : review.getVersionedComments()) {
                    if (comment.getAuthor().equals(userName)) {
                        myVersionedComments++;
                        if (comment.isDraft()) {
                            myDrafts++;
                            allDrafts++;
                        }
                        if (comment.isDefectRaised()) {
                            myDefects++;
                            allDefects++;
                        }
                    } else {
                        if (comment.isDraft()) {
                            allDrafts++;
                        }
                        if (comment.isDefectRaised()) {
                            allDefects++;
                        }
                    }

                }
            } catch (ValueNotYetInitialized valueNotYetInitialized) {
                valueNotYetInitialized
                        .printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }


            body.add(new BoldLabel("My draft comments"), gbc1);
            body.add(new JLabel(Integer.toString(myDrafts), SwingConstants.LEFT), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;

            body.add(new BoldLabel("All my comments"), gbc1);
            body.add(new JLabel(Integer.toString(myGeneralComments + myVersionedComments), SwingConstants.LEFT), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;

            body.add(new BoldLabel("Total comments"), gbc1);
            body.add(new JLabel(Integer.toString(generalComments + versionedComments), SwingConstants.LEFT), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;

            body.add(new BoldLabel("Total defects"), gbc1);
            body.add(new JLabel(Integer.toString(allDefects), SwingConstants.LEFT), gbc2);
            gbc1.gridy++;
            gbc2.gridy++;

            body.add(new BoldLabel("My defects comments"), gbc1);
            body.add(new JLabel(Integer.toString(myDefects), SwingConstants.LEFT), gbc2);
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
        JEditorPane body = new JEditorPane();

        public DescriptionPanel(final ReviewData review) {
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = 0;
            gbc.gridy = 0;

            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;

            JScrollPane sp = new JScrollPane(body,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            sp.setBorder(BorderFactory.createEmptyBorder());
            sp.setOpaque(false);
            body.setEditable(false);
            body.addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        BrowserUtil.launchBrowser(e.getURL().toString());
                    }
                }
            });

            body.setOpaque(false);
            body.setBorder(BorderFactory.createEmptyBorder());
            //body.setContentType("text/html");
            //body.setText("<html><head></head><body>" + review.getSummary() + "</body></html>");
            body.setText(review.getSummary());
            sp.getViewport().setOpaque(false);
            body.setCaretPosition(0);
            add(sp, gbc);

            Border b = BorderFactory.createTitledBorder("Review summary");
            setBorder(b);
            Insets i = b.getBorderInsets(this);
            int minHeight = i.top + i.bottom;
            setMinimumSize(new Dimension(0, minHeight));
        }

        public void setEditable(boolean editable) {
            body.setEditable(editable);
        }

        public void setEnabled(boolean enabled) {
            body.setEnabled(enabled);
        }

        public String getText() {
            return body.getText();
        }
    }
}
