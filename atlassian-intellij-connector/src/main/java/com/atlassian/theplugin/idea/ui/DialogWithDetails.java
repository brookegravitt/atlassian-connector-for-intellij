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
package com.atlassian.theplugin.idea.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ui.UIUtil;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

public class DialogWithDetails extends DialogWrapper {
	private final String description;
	private final String question;
	private final boolean showExceptionTitle;
	private final String exceptionStr;
	private final String exceptionTitle;
	private JComponent exceptionPane;
	private final JPanel thePanel = new JPanel();
	private CellConstraints exceptionCC;

	protected DialogWithDetails(Project project, String description, String question, Throwable exception, String title,
			boolean showExceptionTitle) {
		super(project, false);
		this.description = description;
		this.question = question;
		this.showExceptionTitle = showExceptionTitle;
		this.exceptionStr = getExceptionString(exception);
		this.exceptionTitle = exception.getLocalizedMessage();

		setTitle(title);

		init();
	}

	public static int showYesNoDialog(Project project, String description,
			String question, Throwable exception, String title) {
		DialogWithDetails dialog = new DialogWithDetails(project, description, question, exception, title, true);
		dialog.show();
		return dialog.getExitCode();
	}

	public static int showExceptionDialog(Project project, String description,
			Throwable exception, String title) {
		final DialogWithDetails dialog = new DialogWithDetails(project, description, null, exception, title, false) {
			@Override
			protected Action[] createActions() {
				return new Action[]{getOKAction(), getDetailsAction()};
			}

			@Override
			protected Icon getIcon() {
				return Messages.getErrorIcon();
			}
		};
		dialog.show();
		return dialog.getExitCode();
	}




	@Override
	@Nullable
	protected JComponent createCenterPanel() {

		FormLayout formLayout = new FormLayout("3dlu, pref, 3dlu, 15dlu, 300dlu, 3dlu",
				"3dlu, pref, 3dlu, max(10dlu;pref), 3dlu, pref, 9dlu, pref, 9dlu");

		//CHECKSTYLE:MAGIC:OFF
		CellConstraints iconCC = new CellConstraints(2, 2, 1, 5);
		CellConstraints descrCC = new CellConstraints(4, 2, 2, 1);
		exceptionCC = new CellConstraints(5, 6, 1, 1);
		CellConstraints questionCC = new CellConstraints(4, 8, 2, 1);

		thePanel.setLayout(formLayout);

		JLabel iconLabel = new JLabel(getIcon());
		iconLabel.setVerticalAlignment(JLabel.TOP);
		thePanel.add(iconLabel, iconCC);

		JTextArea descrArea = new JTextArea(description);
		descrArea.setEditable(false);
		descrArea.setLineWrap(true);
		descrArea.setBackground(UIUtil.getOptionPaneBackground());
		thePanel.add(descrArea, descrCC);

		if (showExceptionTitle) {
			final JLabel exTitleLabel = new JLabel(exceptionTitle);
			exTitleLabel.setFont(exTitleLabel.getFont().deriveFont(Font.ITALIC));
			CellConstraints exceptionTitleCC = new CellConstraints(5, 4, 1, 1);
			thePanel.add(exTitleLabel, exceptionTitleCC);
		}

		JTextArea exceptionArea = new JTextArea(10, 60);
		exceptionArea.setText(exceptionStr);
		exceptionArea.setEditable(false);
		exceptionArea.setCaretPosition(0);
		exceptionArea.setTabSize(2);
		//CHECKSTYLE:MAGIC:ON
		exceptionPane = new JScrollPane(exceptionArea);

		JTextArea questionArea = new JTextArea(question);
		questionArea.setEditable(false);
		questionArea.setBackground(UIUtil.getOptionPaneBackground());
		thePanel.add(questionArea, questionCC);

		return thePanel;
	}

	protected Icon getIcon() {
		return Messages.getQuestionIcon();
	}

	@Override
	protected Action[] createActions() {
		return new Action[]{new DetailsAction(), getOKAction(), getCancelAction()};
	}

	private static String getExceptionString(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.getBuffer().toString();
	}

	protected Action getDetailsAction() {
		return new DetailsAction();
	}

	private class DetailsAction extends AbstractAction {
		private static final String SHOW_TXT = "Show Exception Details";
		private static final String HIDE_TXT = "Hide Exception Details";

		public DetailsAction() {
			putValue(Action.NAME, SHOW_TXT);
		}

		public void actionPerformed(ActionEvent e) {
			if (exceptionPane.getParent() != null) {
				thePanel.remove(exceptionPane);
				putValue(Action.NAME, SHOW_TXT);
			} else {
				thePanel.add(exceptionPane, exceptionCC);
				putValue(Action.NAME, HIDE_TXT);
			}
			pack();
		}
	}

}