package com.atlassian.theplugin.idea.bamboo;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.atlassian.theplugin.idea.ui.KeyPressGobbler;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * User: kalamon
 * Date: Jul 3, 2009
 * Time: 12:24:24 PM
 */
public class SearchBuildDialog extends DialogWrapper {

    private JTextField queryField;
    private static final String PROMPT_TEXT =
            "Quick Search (enter build plan key and build number, separated by hyphen):";
    private static final char HYPHEN = '-';

    public SearchBuildDialog(Project project) {
        super(project, true);
        setTitle("Search Build");
        init();
    }

    @Nullable
    protected JComponent createCenterPanel() {
        JPanel p = new JPanel(new FormLayout("pref, 10dlu, fill:pref:grow", "d, 3dlu, d"));
        CellConstraints cc = new CellConstraints();

        p.add(new JLabel(IconLoader.getIcon("/icons/find-bamboo.png")), cc.xy(1, 1));
        p.add(new JLabel(PROMPT_TEXT), cc.xy(2 + 1, 1));
        queryField = new JTextField();
        p.add(queryField, cc.xy(2 + 1, 2 + 1));
        queryField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                handleQueryStringChange();
            }

            public void removeUpdate(DocumentEvent e) {
                handleQueryStringChange();
            }

            public void changedUpdate(DocumentEvent e) {
                handleQueryStringChange();
            }
        });
        KeyPressGobbler.gobbleKeyPress(queryField);

        setOKActionEnabled(false);
        setButtonsAlignment(SwingConstants.CENTER);
        return p;
    }

    private void handleQueryStringChange() {
        boolean valid = isSearchKeyValid();
        queryField.setForeground(valid ? Color.BLACK : Color.RED);
        setOKActionEnabled(valid);
    }

    private boolean isSearchKeyValid() {
        String key = queryField.getText().trim();
        int hyphenPos = key.lastIndexOf(HYPHEN);
        if (hyphenPos <= 0 || key.length() <= hyphenPos + 1) {
            return false;
        }
        try {
            return Integer.valueOf(key.substring(hyphenPos + 1)) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public JComponent getPreferredFocusedComponent() {
        return queryField;
    }

    public String getPlanKey() {
        if (!isSearchKeyValid()) {
            return null;
        }
        String txt = queryField.getText().trim().toUpperCase();
        return txt.substring(0, txt.lastIndexOf(HYPHEN));
    }

    public int getBuildNumber() {
        if (!isSearchKeyValid()) {
            return 0;
        }
        String txt = queryField.getText().trim();
        return Integer.valueOf(txt.substring(txt.lastIndexOf(HYPHEN) + 1));
    }
}
