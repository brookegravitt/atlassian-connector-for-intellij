package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.idea.ui.KeyPressGobbler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.IconLoader;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

/**
 * User: jgorycki
 * Date: May 27, 2009
 * Time: 4:18:38 PM
 */
public class SearchIssueDialog extends DialogWrapper {

	private JTextField queryField;
	private static final String PROMPT_TEXT =
			"Quick Search (entering just issue key will open this issue directly in IDE):";

	public SearchIssueDialog(Project project) {
		super(project, true);
		setTitle("Search JIRA Issue");
		init();
	}

	@Nullable
	protected JComponent createCenterPanel() {
		JPanel p = new JPanel(new FormLayout("pref, 10dlu, fill:pref:grow", "d, 3dlu, d"));
		CellConstraints cc = new CellConstraints();

		p.add(new JLabel(IconLoader.getIcon("/actions/find.png")), cc.xy(1, 1));
		p.add(new JLabel(PROMPT_TEXT), cc.xy(2 + 1, 1));
		queryField = new JTextField();
		p.add(queryField, cc.xy(2 + 1, 2 + 1));
		queryField.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				setOKActionEnabled(queryField.getText().length() > 0);
			}

			public void removeUpdate(DocumentEvent e) {
				setOKActionEnabled(queryField.getText().length() > 0);
			}

			public void changedUpdate(DocumentEvent e) {
				setOKActionEnabled(queryField.getText().length() > 0);
			}
		});
		KeyPressGobbler.gobbleKeyPress(queryField);

		setOKActionEnabled(false);
		setButtonsAlignment(SwingConstants.CENTER);
		return p;
	}

	public JComponent getPreferredFocusedComponent() {
		return queryField;
	}

	public String getSearchQueryString() {
		return queryField.getText().trim();
	}
}
