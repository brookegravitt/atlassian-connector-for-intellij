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

import com.atlassian.theplugin.commons.crucible.api.model.CommentBean;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.HyperlinkLabel;
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

}
