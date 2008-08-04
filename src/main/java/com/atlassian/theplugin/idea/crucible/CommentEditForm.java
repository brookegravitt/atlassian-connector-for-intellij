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
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldBean;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldValue;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;


public class CommentEditForm extends DialogWrapper {
	private JPanel rootComponent;
	private JTextArea commentText;
	private JScrollPane commentPane;
	private JCheckBox defectCheckBox;
	private JComboBox rankComboBox;
	private JComboBox classificationComboBox;
	private JButton postButton;
	private JButton saveAsDraftButton;
	private JButton cancelButton;

	private Review review;
	private CommentBean comment;

	private boolean saveAsDraft = false;

	public CommentEditForm(Project project, Review review, CommentBean comment, List<CustomFieldDef> metrics) {
		super(project, false);

		this.review = review;
		this.comment = comment;

		$$$setupUI$$$();
		init();

		rankComboBox.addItem("select rank");
		classificationComboBox.addItem("select classification");

		for (CustomFieldDef metric : metrics) {
			if ("rank".equals(metric.getName())) {
				for (CustomFieldValue value : metric.getValues()) {
					rankComboBox.addItem(value.getName());
				}
			}
			if ("classification".equals(metric.getName())) {
				for (CustomFieldValue value : metric.getValues()) {
					classificationComboBox.addItem(value.getName());
				}
			}
		}

		postButton.setAction(getOKAction());
		postButton.setMnemonic('P');
		saveAsDraftButton.setAction(getDraftAction());
		saveAsDraftButton.setMnemonic('D');
		cancelButton.setAction(getCancelAction());
		commentText.setText(comment.getMessage());

		defectCheckBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				showMetricCombo(defectCheckBox.isSelected());
				pack();
			}
		});

		if (comment.isDefectRaised()) {
			defectCheckBox.setSelected(true);
			showMetricCombo(true);
		} else {
			defectCheckBox.setSelected(false);
			showMetricCombo(false);
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

	private void showMetricCombo(boolean visible) {
		rankComboBox.setVisible(visible);
		classificationComboBox.setVisible(visible);
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
		if (comment.isDefectRaised()) {
			if (rankComboBox.getSelectedIndex() > 0) {
				CustomFieldBean cf = new CustomFieldBean();
				cf.setConfigVersion(review.getMetricsVersion());
				cf.setValue((String) rankComboBox.getSelectedItem());
				comment.getCustomFields().put("rank", cf);
			}
			if (classificationComboBox.getSelectedIndex() > 0) {
				CustomFieldBean cf = new CustomFieldBean();
				cf.setConfigVersion(review.getMetricsVersion());
				cf.setValue((String) classificationComboBox.getSelectedItem());
				comment.getCustomFields().put("classification", cf);
			}
		}
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

	public CommentBean getComment() {
		return comment;
	}

	public void setComment(CommentBean comment) {
		this.comment = comment;
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
		rootComponent.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
		rootComponent.setMinimumSize(new Dimension(650, 300));
		commentPane = new JScrollPane();
		rootComponent.add(commentPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		commentText = new JTextArea();
		commentText.setLineWrap(false);
		commentText.setText("");
		commentText.setWrapStyleWord(false);
		commentPane.setViewportView(commentText);
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(1, 7, new Insets(0, 0, 0, 0), -1, -1));
		rootComponent.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		defectCheckBox = new JCheckBox();
		defectCheckBox.setText("Defect");
		panel1.add(defectCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
				null, null, null, 0, false));
		rankComboBox = new JComboBox();
		panel1.add(rankComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		classificationComboBox = new JComboBox();
		panel1.add(classificationComboBox, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST,
				GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
				null, null, 0, false));
		postButton = new JButton();
		postButton.setText("Post");
		panel1.add(postButton, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
				null, null, null, 0, false));
		saveAsDraftButton = new JButton();
		saveAsDraftButton.setText("Save as draft");
		panel1.add(saveAsDraftButton, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER,
				GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		cancelButton = new JButton();
		cancelButton.setText("Cancel");
		panel1.add(cancelButton, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
				null, null, null, 0, false));
		final Spacer spacer1 = new Spacer();
		panel1.add(spacer1, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootComponent;
	}
}
