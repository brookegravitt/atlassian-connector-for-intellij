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

import com.atlassian.theplugin.idea.util.IdeaUiMultiTaskExecutor;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.commons.lang.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class DialogWithDetails extends DialogWrapper {
    private final String description;
    private final String exceptionStr;
    private JLabel ctrlDescription;
    private JTextArea ctrlDetailsText;
    private JPanel rootPane;
    private JScrollPane ctrlDetailsPane;
    private static final int MAX_DESCRIPTION_LENGTH = 80;

    protected DialogWithDetails(Project project, String description, Throwable exception) {
        super(project, false);
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            this.description = description.substring(0, MAX_DESCRIPTION_LENGTH) + "...";
        } else {
            this.description = description;
        }
        this.exceptionStr = getExceptionString(exception);

        setTitle(PluginUtil.PRODUCT_NAME);
        init();


    }

    protected DialogWithDetails(Project project, String description, String exceptionString) {
        super(project, false);
        this.description = description;
        this.exceptionStr = exceptionString;

        setTitle(PluginUtil.PRODUCT_NAME);
        init();
    }

    protected DialogWithDetails(Component parent, String description, String exception) {
        super(parent, false);
        this.description = description;
        this.exceptionStr = exception;

        setTitle(PluginUtil.PRODUCT_NAME);
        init();
        pack();
    }

    protected DialogWithDetails(Component parent, String description, Throwable exception) {
        super(parent, false);
        this.description = description;
        this.exceptionStr = getExceptionString(exception);

        setTitle(PluginUtil.PRODUCT_NAME);
        init();
        pack();
    }

    public static int showExceptionDialog(Project project, String description, String details) {
        final DialogWithDetails dialog = new DialogWithDetails(project, description, details);
        dialog.show();
        return dialog.getExitCode();
    }


    public static int showExceptionDialog(Component component, String description, String details) {
        final DialogWithDetails dialog = new DialogWithDetails(component, description, details);
        dialog.show();
        return dialog.getExitCode();
    }


    public static int showExceptionDialog(Project project, String description, Throwable exception) {
        final DialogWithDetails dialog = new DialogWithDetails(project, description, exception);
        dialog.show();
        return dialog.getExitCode();
    }

    public static int showExceptionDialog(Component parent, String description, Throwable exception) {
        final DialogWithDetails dialog = new DialogWithDetails(parent, description, exception);
        dialog.show();
        return dialog.getExitCode();
    }

    public static int showExceptionDialog(final Project project, final String description, final Exception exception,
                                          final String helpUrl) {
        final DialogWithDetails dialog = new DialogWithDetails(project, description, exception) {
            @Override
            protected Action[] createActions() {
                return new Action[]{getOKAction(), getDetailsAction(), getWebHelpAction(helpUrl)};
            }
        };
        dialog.show();

        return dialog.getExitCode();
    }

    public static int showExceptionDialog(final Component component, final List<IdeaUiMultiTaskExecutor.ErrorObject> errors) {
        if (errors == null || errors.size() == 0) {
            return 0;
        }
        final DialogWithDetails dialog = new DialogWithDetails(component,
                "<html>(1/" + errors.size() + ") " + errors.get(0).getMessage(),
                errors.get(0).getException()) {

            @Override
            protected Action[] createActions() {
                MutableInt currentErrorIndex = new MutableInt(0);
                PrevErrorAction prevAction = (PrevErrorAction) getPrevErrorAction(currentErrorIndex, errors);
                NextErrorAction nextAction = (NextErrorAction) getNextErrorAction(currentErrorIndex, errors);
                prevAction.setNextAction(nextAction);
                nextAction.setPrevAction(prevAction);
                return new Action[]{getOKAction(), getDetailsAction(), prevAction, nextAction};
            }
        };
        dialog.show();

        return dialog.getExitCode();
    }

    @Override
    protected void init() {
        super.init();

        ctrlDescription.setText(description);
        ctrlDescription.setIcon(getIcon());

        ctrlDetailsText.setText(exceptionStr);
        ctrlDetailsText.setCaretPosition(0);
        ctrlDetailsText.setEditable(false);

        ctrlDetailsPane.setVisible(false);
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        return rootPane;
    }

    protected Icon getIcon() {
        return Messages.getErrorIcon();
    }

    @Override
    protected Action[] createActions() {
        return new Action[]{getOKAction(), getDetailsAction()};
    }

    public static String getExceptionString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.getBuffer().toString();
    }

    protected Action getDetailsAction() {
        return new DetailsAction();
    }

    public Action getWebHelpAction(final String helpUrl) {
        return new WebHelpAction(helpUrl);
    }

    public Action getNextErrorAction(final MutableInt currentErrorIndex,
                                     final List<IdeaUiMultiTaskExecutor.ErrorObject> errors) {
        return new NextErrorAction(currentErrorIndex, errors);
    }

    public Action getPrevErrorAction(final MutableInt currentErrorIndex,
                                     final List<IdeaUiMultiTaskExecutor.ErrorObject> errors) {
        return new PrevErrorAction(currentErrorIndex, errors);
    } // CHECKSTYLE:OFF

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPane = new JPanel();
        rootPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        ctrlDescription = new JLabel();
        ctrlDescription.setText("Label");
        rootPane.add(ctrlDescription, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ctrlDetailsPane = new JScrollPane();
        rootPane.add(ctrlDetailsPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(600, 250), null, 0, false));
        ctrlDetailsText = new JTextArea();
        ctrlDetailsText.setBackground(new Color(-1));
        ctrlDetailsText.setEditable(false);
        ctrlDetailsText.setEnabled(true);
        ctrlDetailsPane.setViewportView(ctrlDetailsText);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPane;
    }

    // CHECKSTYLE:ON

    private final class DetailsAction extends AbstractAction {
        private static final String SHOW_TXT = "Show Details";
        private static final String HIDE_TXT = "Hide Details";

        private DetailsAction() {
            putValue(Action.NAME, SHOW_TXT);
        }

        public void actionPerformed(ActionEvent e) {

            if (ctrlDetailsPane.isVisible()) {
                ctrlDetailsPane.setVisible(false);
                putValue(Action.NAME, SHOW_TXT);
            } else {
                ctrlDetailsPane.setVisible(true);
                ctrlDetailsText.requestFocusInWindow();
                putValue(Action.NAME, HIDE_TXT);
            }

            pack();
        }
    }

    private static final class WebHelpAction extends AbstractAction {
        private String helpUrl;

        private WebHelpAction(final String helpUrl) {
            this.helpUrl = helpUrl;
            putValue(Action.NAME, "Help");
        }

        public void actionPerformed(final ActionEvent e) {
            BrowserUtil.launchBrowser(helpUrl);
        }
    }

    private final class NextErrorAction extends AbstractAction implements PrevNextAction {
        private MutableInt currentErrorIndex;
        private List<IdeaUiMultiTaskExecutor.ErrorObject> errors;
        private PrevNextAction prevAction;

        public NextErrorAction(final MutableInt currentErrorIndex, final List<IdeaUiMultiTaskExecutor.ErrorObject> errors) {
            this.currentErrorIndex = currentErrorIndex;
            this.errors = errors;
            putValue(Action.NAME, "Next");
            enableDisable();
        }

        public void actionPerformed(final ActionEvent e) {
            currentErrorIndex.setValue((currentErrorIndex.intValue() + 1));

            int index = currentErrorIndex.intValue();
            ctrlDescription.setText("<html>(" + (index + 1) + "/" + errors.size() + ") " + errors.get(index).getMessage());
            ctrlDetailsText.setText(getExceptionString(errors.get(index).getException()));
            ctrlDetailsText.setCaretPosition(0);

            enableDisable();

            if (prevAction != null) {
                prevAction.enableDisable();
            }
        }

        public void enableDisable() {
            if (currentErrorIndex.intValue() == errors.size() - 1) {
                setEnabled(false);
            } else {
                setEnabled(true);
            }
        }

        public void setPrevAction(final PrevNextAction prevAction) {
            this.prevAction = prevAction;
        }
    }

    private class PrevErrorAction extends AbstractAction implements PrevNextAction {
        private MutableInt currentErrorIndex;
        private List<IdeaUiMultiTaskExecutor.ErrorObject> errors;
        private PrevNextAction nextAction;

        public PrevErrorAction(final MutableInt currentErrorIndex, final List<IdeaUiMultiTaskExecutor.ErrorObject> errors) {
            this.currentErrorIndex = currentErrorIndex;
            this.errors = errors;
            putValue(Action.NAME, "Prev");
            enableDisable();
        }

        public void actionPerformed(final ActionEvent e) {
            currentErrorIndex.setValue((currentErrorIndex.intValue() + 1) % errors.size());

            int index = currentErrorIndex.intValue();
            ctrlDescription.setText("<html>(" + (index + 1) + "/" + errors.size() + ") " + errors.get(index).getMessage());
            ctrlDetailsText.setText(getExceptionString(errors.get(index).getException()));
            ctrlDetailsText.setCaretPosition(0);

            enableDisable();
            if (nextAction != null) {
                nextAction.enableDisable();
            }
        }

        public void enableDisable() {
            if (currentErrorIndex.intValue() == 0) {
                setEnabled(false);
            } else {
                setEnabled(true);
            }
        }

        public void setNextAction(final PrevNextAction nextAction) {
            this.nextAction = nextAction;
        }
    }

    private interface PrevNextAction {
        void enableDisable();
    }
}