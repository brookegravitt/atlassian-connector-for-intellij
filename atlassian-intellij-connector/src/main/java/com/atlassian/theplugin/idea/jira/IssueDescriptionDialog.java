package com.atlassian.theplugin.idea.jira;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Dimension;
import java.awt.Insets;

public class IssueDescriptionDialog extends DialogWrapper {
    private JPanel mainPanel;
    private JTextArea textArea;

    public IssueDescriptionDialog(String issueKey, String description) {
        super(false);
		doLayout();
		init();
		setTitle("Change Description for " + issueKey);
        setOKButtonText("Change");
		textArea.setText(description);
    }

    public String getDescription() {
        return textArea.getText();
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return textArea;
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        return mainPanel;
    }

    private void doLayout() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 1, new Insets(5, 5, 5, 5), -1, -1));
        mainPanel.setMinimumSize(new Dimension(400, 100));
        mainPanel.setPreferredSize(new Dimension(500, 150));
        final JScrollPane scrollPane1 = new JScrollPane();
        mainPanel.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane1.setViewportView(textArea);
    }
}