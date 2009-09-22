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
package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.idea.util.IdeaUiMultiTaskExecutor;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.labels.LinkLabel;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Jacek Jaroczynski
 */
public class StatusBarPane extends JPanel implements StatusBar {
    private static final Dimension ED_PANE_MINE_SIZE = new Dimension(200, 200);
    protected final Color defaultColor = this.getBackground();

    protected JEditorPane textPanel = new JEditorPane();
    protected JPanel additionalPanel;
    private List<Throwable> errors = new ArrayList<Throwable>();
    private LinkLabel dismissLabel = null;

    public StatusBarPane(String initialText) {

        additionalPanel = new JPanel();
        additionalPanel.setLayout(new FlowLayout());

        textPanel.setMinimumSize(ED_PANE_MINE_SIZE);
        textPanel.setOpaque(false);
        textPanel.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        textPanel.setContentType("text/html");
        textPanel.setEditable(false);
        textPanel.setMargin(new Insets(5, 5, 5, 5));
        textPanel.setFont(new JLabel().getFont());
        textPanel.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(final HyperlinkEvent e) {
                if (e.getEventType().equals(javax.swing.event.HyperlinkEvent.EventType.ACTIVATED)) {
                    if (errors.size() == 1) {
                        DialogWithDetails.showExceptionDialog(StatusBarPane.this, errors.get(0).getMessage(), errors.get(0));
                    } else if (errors != null && errors.size() > 0) {
                        List<IdeaUiMultiTaskExecutor.ErrorObject> errorObjects =
                                new ArrayList<IdeaUiMultiTaskExecutor.ErrorObject>();
                        for (Throwable err : errors) {
                            errorObjects.add(new IdeaUiMultiTaskExecutor.ErrorObject(err.getMessage(), err));
                        }
                        DialogWithDetails.showExceptionDialog(StatusBarPane.this, errorObjects);
                    } else {
                        BrowserUtil.launchBrowser(e.getURL().toString());
                    }
                }
            }
        });

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();


        dismissLabel = new LinkLabel("", IconLoader.getIcon("/actions/cleanLight.png"));
        dismissLabel.setHorizontalTextPosition(SwingUtilities.LEFT);
        dismissLabel.setHorizontalAlignment(SwingUtilities.LEFT);
        dismissLabel.setToolTipText("Dismiss message");
        dismissLabel.addMouseListener(new MouseAdapter() {


            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                clear();
            }
        });

        gbc.gridx = 0;
        gbc.weightx = 0.0;
//		gbc.ipady = PAD_Y;
        add(dismissLabel, gbc);


        gbc.gridx++;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
//		gbc.ipady = PAD_Y;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(textPanel, gbc);


        gbc.gridx++;
        gbc.weightx = 0.0;
        add(additionalPanel, gbc);

        setInfoMessage(initialText, false);
    }


    public void addComponent(JComponent component) {
        additionalPanel.add(component);
    }


    public void setInfoMessage(String message, boolean rightAlign) {
//		textPanel.set setHorizontalAlignment(rightAlign ? SwingConstants.RIGHT : SwingConstants.LEFT);
        textPanel.setText(" " + message);
        textPanel.setBackground(defaultColor);
        setBackground(defaultColor);
        dismissLabel.setVisible(false);
        dismissLabel.setBackground(defaultColor);
        errors.clear();
    }

    public void setErrorMessage(String msg) {
        if (msg == null || msg.length() == 0) {
            msg = "Unknown error";
        }
        additionalPanel.setBackground(Constants.FAIL_COLOR);
        dismissLabel.setVisible(true);
        dismissLabel.setBackground(Constants.FAIL_COLOR);
//		textPanel.setHorizontalAlignment(SwingConstants.LEFT);
        textPanel.setBackground(Constants.FAIL_COLOR);
        setBackground(Constants.FAIL_COLOR);
        textPanel.setText(" " + msg);
        errors.clear();
    }

    public void setErrorMessage(String msg, Throwable e) {
        if (msg == null || msg.length() == 0) {
            msg = "Unknown error";
        }
        additionalPanel.setBackground(Constants.FAIL_COLOR);
        dismissLabel.setVisible(true);
        dismissLabel.setBackground(Constants.FAIL_COLOR);
//		textPanel.setHorizontalAlignment(SwingConstants.LEFT);
        textPanel.setBackground(Constants.FAIL_COLOR);
        setBackground(Constants.FAIL_COLOR);
        textPanel.setText("<html> " + msg + "&nbsp;&nbsp;<a href=\"http://www.nothing.com\">Error Details</a></html>");
        errors.clear();
        errors.add(e);
    }


    public void setErrorMessages(String msg, final Collection<Throwable> exceptions) {
        if (msg == null || msg.length() == 0) {
            msg = "Unknown error";
        }
        additionalPanel.setBackground(Constants.FAIL_COLOR);
        dismissLabel.setVisible(true);
        dismissLabel.setBackground(Constants.FAIL_COLOR);
//		textPanel.setHorizontalAlignment(SwingConstants.LEFT);
        textPanel.setBackground(Constants.FAIL_COLOR);
        setBackground(Constants.FAIL_COLOR);
        textPanel.setText("<html> " + msg + "&nbsp;&nbsp;<a href=\"http://www.nothing.com\">Error Details</a></html>");
        errors.clear();
        errors.addAll(exceptions);
    }

    private void clear() {
        dismissLabel.setVisible(false);
        textPanel.setText("");
        textPanel.setBackground(defaultColor);
        setBackground(defaultColor);
        additionalPanel.setBackground(defaultColor);
        dismissLabel.setBackground(defaultColor);
        errors.clear();
    }
}
