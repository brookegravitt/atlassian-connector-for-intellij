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

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.CommentBean;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class CommentEditForm extends DialogWrapper {
    private JPanel rootComponent;
    private JTextArea commentText;
    private JScrollPane commentPane;
    private JCheckBox defectCheckBox;
    private JButton postButton;
    private JButton saveAsDraftButton;
    private JButton cancelButton;
    private JPanel comboPanel;
    private JPanel toolPanel;
    private JPanel errorPanel;
    private HyperlinkLabel errorLabel;


    private boolean saveAsDraft = false;
    private final CommentBean comment;
    private Throwable lastError;

    public CommentEditForm(final Project project, final ReviewAdapter review, final CommentBean comment) {

        super(project, false);
        this.comment = comment;


        $$$setupUI$$$();
        init();

        this.errorPanel.setVisible(false);
        errorPanel.add(new JLabel("Comment submission failed - "));
        errorLabel = new HyperlinkLabel("click here for details");
        errorLabel.setOpaque(false);
        errorLabel.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (lastError != null) {
                    DialogWithDetails.showExceptionDialog(project, lastError.getMessage(), lastError);
                }
            }
        });
        errorPanel.add(errorLabel);

        comboPanel.setLayout(new FlowLayout());

        final CrucibleReviewMetricsCombos combos = new CrucibleReviewMetricsCombos(comment.getCustomFields(),
                review.getMetricDefinitions(),
                comboPanel);

        postButton.setAction(getOKAction());
        postButton.setMnemonic('P');
        saveAsDraftButton.setAction(getDraftAction());
        saveAsDraftButton.setMnemonic('D');
        cancelButton.setAction(getCancelAction());


        commentText.setText(comment.getMessage());

        defectCheckBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                combos.showMetricCombos(defectCheckBox.isSelected());
                pack();
            }
        });

        if (comment.isDefectRaised()) {
            defectCheckBox.setSelected(true);
            combos.showMetricCombos(true);
        } else {
            defectCheckBox.setSelected(false);
            combos.showMetricCombos(false);
        }

        if (comment.isReply()) {
            defectCheckBox.setVisible(false);
            if (comment.getPermId() != null) {
                setTitle("Edit Reply");
            } else {
                setTitle("Add Reply");
            }
        } else {
            if (comment.getPermId() != null) {
                setTitle("Edit Comment");
            } else {
                setTitle("Add Comment");
            }
        }

        if (comment.getPermId() != null) {
            if (comment.isDraft()) {
                saveAsDraftButton.setVisible(true);
            } else {
                saveAsDraftButton.setVisible(false);
            }

        } else {
            saveAsDraftButton.setVisible(true);
        }

        getOKAction().putValue(Action.NAME, "Post");
    }

    public CommentEditForm(Project project, final ReviewAdapter review, final CommentBean data, final Throwable error) {
        this(project, review, data);
        if (error != null) {
            errorPanel.setBackground(Constants.FAIL_COLOR);
            errorPanel.setVisible(true);
            lastError = error;
        }
    }

    public JComponent getPreferredFocusedComponent() {
        return commentText;
    }

    public JComponent getRootComponent() {
        return rootComponent;
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return getRootComponent();
    }


    protected void doOKAction() {
        comment.setDraft(saveAsDraft);
        comment.setDefectRaised(defectCheckBox.isSelected());
        comment.setMessage(commentText.getText());
        super.doOKAction();
    }

    @Override
    protected Action[] createActions() {
        return new Action[0];
    }

    public Action getDraftAction() {
        return draftAction;
    }

    private Action draftAction = new AbstractAction() {
        {
            putValue(Action.NAME, "Save as Draft");
        }

        public void actionPerformed(ActionEvent e) {
            saveAsDraft = true;
            doOKAction();
        }
    };

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
        rootComponent.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootComponent.setMinimumSize(new Dimension(650, 300));
        commentPane = new JScrollPane();
        rootComponent.add(commentPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        commentText = new JTextArea();
        commentText.setLineWrap(true);
        commentText.setText("");
        commentText.setWrapStyleWord(true);
        commentPane.setViewportView(commentText);
        toolPanel = new JPanel();
        toolPanel.setLayout(new GridLayoutManager(1, 6, new Insets(0, 0, 0, 0), -1, -1));
        rootComponent.add(toolPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        defectCheckBox = new JCheckBox();
        defectCheckBox.setText("Defect");
        toolPanel.add(defectCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        postButton = new JButton();
        postButton.setText("Post");
        toolPanel.add(postButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveAsDraftButton = new JButton();
        saveAsDraftButton.setText("Save as draft");
        toolPanel.add(saveAsDraftButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        toolPanel.add(cancelButton, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        toolPanel.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        comboPanel = new JPanel();
        comboPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        toolPanel.add(comboPanel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        errorPanel = new JPanel();
        errorPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        rootComponent.add(errorPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootComponent;
    }
}
