package com.atlassian.theplugin.idea.ui;

import com.atlassian.theplugin.idea.jira.IssueDetailsToolWindow;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * User: pstefaniak
 * Date: Mar 22, 2010
 */
public class EditableIssueField extends JPanel {
    private final static Icon editIcon = IconLoader.getIcon("/icons/edit.png");
    private final static Icon emptyIcon = IconLoader.getIcon("/icons/empty.png");
    private EditIssueFieldButton button;
    private JComponent component; //ususally label displaying value of that field
    private final EditIssueFieldHandler handler;

    public EditableIssueField(JComponent component, EditIssueFieldHandler handler) {
        this.handler = handler;
        button = new EditIssueFieldButton();
        this.component = component;
        setBackground(component.getBackground());
        button.setBackground(component.getBackground());
        setBorder(BorderFactory.createEmptyBorder());
        rebuild();
        button.hideMe();
    }

    private void rebuild() {
        JPanel groupingPanel = new JPanel(new GridBagLayout());
        groupingPanel.setBorder(BorderFactory.createEmptyBorder());

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;


        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.weightx = 0.0;
        gbc1.weighty = 0.0;
        gbc1.fill = GridBagConstraints.NONE;

        removeAll();
        if (component != null) {
            groupingPanel.setBackground(component.getBackground());
            groupingPanel.add(component, gbc1);
        }

        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.weightx = 0.0;
        gbc1.weighty = 0.0;
        gbc1.gridx++;
        gbc1.anchor = GridBagConstraints.PAGE_START;
        gbc1.fill = GridBagConstraints.NONE;
        gbc1.insets = new Insets(0, 4, 4, 0);

        groupingPanel.add(button, gbc1);
        add(groupingPanel, gbc);

        IssueDetailsToolWindow.addFillerPanel(this, gbc, true);
        final MouseAdapter adapter = new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                button.showMe();
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                button.hideMe();
            }

        };
        groupingPanel.addMouseListener(adapter);

        component.addMouseListener(adapter);
        for (Component c : component.getComponents()) {
            c.addMouseListener(adapter);
        }
    }


    public void setButtonVisible(boolean isVisible) {
        button.setVisible(isVisible);
    }


    private class EditIssueFieldButton extends JRadioButton {
        public EditIssueFieldButton() {
            super();
            setIcon(editIcon);
            this.setBackground(com.intellij.util.ui.UIUtil.getLabelBackground());
            this.setBorder(BorderFactory.createEmptyBorder());
            this.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    if (handler != null) {
                        handler.handleClickedEditButton();
                    }
                }
            });

            this.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    showMe();
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }

                public void mouseExited(MouseEvent e) {
                    hideMe();
                    setCursor(Cursor.getDefaultCursor());
                }
            });
        }

        public void showMe() {
            setIcon(editIcon);
        }

        public void hideMe() {
            setIcon(emptyIcon);
        }
    }

    public interface EditIssueFieldHandler {
        void handleClickedEditButton();
    }
}

