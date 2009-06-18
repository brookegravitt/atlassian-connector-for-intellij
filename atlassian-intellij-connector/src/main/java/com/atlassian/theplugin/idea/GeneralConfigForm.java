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

package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.commons.configuration.CheckNowButtonOption;
import com.atlassian.theplugin.commons.configuration.GeneralConfigurationBean;
import com.atlassian.theplugin.idea.autoupdate.NewVersionButtonListener;
import com.atlassian.theplugin.idea.autoupdate.NewVersionChecker;
import com.atlassian.theplugin.util.UsageStatisticsGenerator;
import com.intellij.ide.BrowserUtil;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.net.HTTPProxySettingsDialog;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

public class GeneralConfigForm {
    private JCheckBox chkAutoUpdateEnabled;
    private JPanel mainPanel;
    private JButton checkNowButton;
    private JPanel autoUpdateConfigPanel;
    private JCheckBox chkUnstableVersionsCheckBox;
    private JCheckBox reportAnonymousUsageStatisticsCheckBox;
    private JRadioButton checkNewVersionStable;
    private JRadioButton checkNewVersionAll;
    private JPanel httpProxyPanel;
    private JButton httpProxyButton;
    private JRadioButton chkNoProxy;
    private JRadioButton chkUseIdeaProxy;
    private JEditorPane usageStatsHelp;
    private JCheckBox ctrlHttpServer;
    private JSpinner ctrlHttpServerPort;
    private Boolean isAnonymousFeedbackEnabled;
    private final NewVersionChecker newVersionChecker;

    public JRadioButton getCheckNewVersionStable() {
        return checkNewVersionStable;
    }

    public JRadioButton getCheckNewVersionAll() {
        return checkNewVersionAll;
    }

    public GeneralConfigForm(NewVersionChecker checker) {
        newVersionChecker = checker;

        $$$setupUI$$$();
        checkNowButton.addActionListener(new NewVersionButtonListener(this));
        chkAutoUpdateEnabled.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                chkUnstableVersionsCheckBox.setEnabled(chkAutoUpdateEnabled.isSelected());
            }
        });
        reportAnonymousUsageStatisticsCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                isAnonymousFeedbackEnabled = reportAnonymousUsageStatisticsCheckBox.isSelected();
            }
        });
        httpProxyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                HTTPProxySettingsDialog proxyDialog = new HTTPProxySettingsDialog();
                proxyDialog.show();
            }
        });

        chkNoProxy.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                httpProxyButton.setEnabled(chkUseIdeaProxy.isSelected());
            }
        });

        chkUseIdeaProxy.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                httpProxyButton.setEnabled(chkUseIdeaProxy.isSelected());
            }
        });

        final SpinnerNumberModel numberModel =
                new SpinnerNumberModel(1, 1, 65535, 1);

        ctrlHttpServerPort.setModel(numberModel);
        
        JFormattedTextField spinnerTextField = ((JSpinner.DefaultEditor) ctrlHttpServerPort.getEditor()).getTextField();
        DefaultFormatterFactory spinnerFromatterFactory =
                (DefaultFormatterFactory) spinnerTextField.getFormatterFactory();
        ((NumberFormatter) spinnerFromatterFactory.getDefaultFormatter()).setFormat(new DecimalFormat("#####"));
        numberModel.setValue(GeneralConfigurationBean.HTTP_SERVER_PORT);



    }

    public Component getRootPane() {
        return mainPanel;
    }

    public boolean getIsAutoUpdateEnabled() {
        return chkAutoUpdateEnabled.isSelected();
    }

    public void setAutoUpdateEnabled(boolean autoUpdateEnabled) {
        chkAutoUpdateEnabled.setSelected(autoUpdateEnabled);
        chkUnstableVersionsCheckBox.setEnabled(autoUpdateEnabled);
    }

    public boolean isHttpServerEnabled() {
        return ctrlHttpServer.isSelected();
    }

    public void setHttpServerEnabled(boolean startServer) {
        this.ctrlHttpServer.setSelected(startServer);
    }

    public int getHttpServerPort() {
        return IdeaHelper.getSpinnerIntValue(ctrlHttpServerPort);
    }

    public void setHttpServerPort(int port) {
        ctrlHttpServerPort.getModel().setValue(port);
    }

    public boolean getIsCheckUnstableVersionsEnabled() {
        return chkUnstableVersionsCheckBox.isSelected();
    }

    public void setIsCheckUnstableVersionsEnabled(boolean isCheckUnstableVersionsEnabled) {
        chkUnstableVersionsCheckBox.setSelected(isCheckUnstableVersionsEnabled);
    }

    @Nullable
    public Boolean getIsAnonymousFeedbackEnabled() {
        return this.isAnonymousFeedbackEnabled;
    }

    public boolean getUseIdeaProxySettings() {
        return chkUseIdeaProxy.isSelected();
    }

    public void setUseIdeaProxySettings(boolean use) {
        chkUseIdeaProxy.setSelected(use);
        chkNoProxy.setSelected(!use);
        httpProxyButton.setEnabled(use);
    }

    public void setIsAnonymousFeedbackEnabled(Boolean isAnonymousFeedbackEnabled) {
        this.isAnonymousFeedbackEnabled = isAnonymousFeedbackEnabled;
        if (isAnonymousFeedbackEnabled == null || !isAnonymousFeedbackEnabled) {
            reportAnonymousUsageStatisticsCheckBox.setSelected(false);
        } else {
            reportAnonymousUsageStatisticsCheckBox.setSelected(true);
        }
    }

    public void setCheckNowButtonOption(CheckNowButtonOption option) {
        switch (option) {
            case STABLE_AND_SNAPSHOT:
                checkNewVersionAll.setSelected(true);
                break;
            case ONLY_STABLE:
            default:
                checkNewVersionStable.setSelected(true);
        }
    }

    public CheckNowButtonOption getCheckNotButtonOption() {
        if (checkNewVersionStable.isSelected()) {
            return CheckNowButtonOption.ONLY_STABLE;
        } else if (checkNewVersionAll.isSelected()) {
            return CheckNowButtonOption.STABLE_AND_SNAPSHOT;
        } else {
            return CheckNowButtonOption.ONLY_STABLE;
        }
    }


    public JComponent getRootComponent() {
        return $$$getRootComponent$$$();
    }

    public NewVersionChecker getNewVersionChecker() {
        return newVersionChecker;
    }

    private void createUIComponents() {
        usageStatsHelp = new JEditorPane();
        usageStatsHelp.setContentType("text/html");
        usageStatsHelp.setEditable(false);
        usageStatsHelp.setOpaque(false);
        usageStatsHelp.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        usageStatsHelp.setText("<html>(&nbsp;<a href=\""
                + UsageStatisticsGenerator.USAGE_STATS_HREF
                + "\">Details</a>&nbsp;)");
        usageStatsHelp.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    BrowserUtil.launchBrowser(e.getURL().toString());
                }
            }
        });
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new JPanel();
        mainPanel.setLayout(new FormLayout("fill:d:grow", "center:max(d;4px):noGrow,top:3dlu:noGrow,center:d:noGrow,top:4dlu:noGrow,center:max(d;4px):grow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12), null));
        autoUpdateConfigPanel = new JPanel();
        autoUpdateConfigPanel.setLayout(new GridLayoutManager(3, 3, new Insets(0, 12, 12, 12), -1, -1));
        CellConstraints cc = new CellConstraints();
        mainPanel.add(autoUpdateConfigPanel, cc.xy(1, 1));
        autoUpdateConfigPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Auto Upgrade"));
        checkNowButton = new JButton();
        checkNowButton.setText("Check Now");
        checkNowButton.setMnemonic('C');
        checkNowButton.setDisplayedMnemonicIndex(0);
        autoUpdateConfigPanel.add(checkNowButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        autoUpdateConfigPanel.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        chkUnstableVersionsCheckBox = new JCheckBox();
        chkUnstableVersionsCheckBox.setEnabled(false);
        chkUnstableVersionsCheckBox.setText("Check Snapshot Versions");
        panel1.add(chkUnstableVersionsCheckBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, new Dimension(12, -1), null, null, 0, false));
        chkAutoUpdateEnabled = new JCheckBox();
        chkAutoUpdateEnabled.setText("Enabled (Stable Version)");
        chkAutoUpdateEnabled.setMnemonic('E');
        chkAutoUpdateEnabled.setDisplayedMnemonicIndex(0);
        autoUpdateConfigPanel.add(chkAutoUpdateEnabled, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        autoUpdateConfigPanel.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        checkNewVersionStable = new JRadioButton();
        checkNewVersionStable.setSelected(true);
        checkNewVersionStable.setText("Stable Only");
        autoUpdateConfigPanel.add(checkNewVersionStable, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkNewVersionAll = new JRadioButton();
        checkNewVersionAll.setText("Stable + Snapshot");
        autoUpdateConfigPanel.add(checkNewVersionAll, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        httpProxyPanel = new JPanel();
        httpProxyPanel.setLayout(new GridLayoutManager(4, 2, new Insets(0, 12, 12, 0), -1, -1));
        mainPanel.add(httpProxyPanel, cc.xy(1, 3, CellConstraints.DEFAULT, CellConstraints.TOP));
        httpProxyPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "HTTP Proxy"));
        chkUseIdeaProxy = new JRadioButton();
        chkUseIdeaProxy.setText("Use IDEA Proxy Settings");
        httpProxyPanel.add(chkUseIdeaProxy, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        httpProxyButton = new JButton();
        httpProxyButton.setText("Edit IDEA Proxy Settings");
        httpProxyPanel.add(httpProxyButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        chkNoProxy = new JRadioButton();
        chkNoProxy.setText("Do Not Use Proxy");
        httpProxyPanel.add(chkNoProxy, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        httpProxyPanel.add(spacer3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setFont(new Font(label1.getFont().getName(), label1.getFont().getStyle(), 10));
        label1.setText("Information: You have to restart IDEA to apply changes in the proxy configuration");
        httpProxyPanel.add(label1, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FormLayout("left:p:noGrow,left:p:noGrow", "center:d:grow"));
        mainPanel.add(panel2, cc.xy(1, 9));
        reportAnonymousUsageStatisticsCheckBox = new JCheckBox();
        reportAnonymousUsageStatisticsCheckBox.setEnabled(true);
        reportAnonymousUsageStatisticsCheckBox.setSelected(false);
        reportAnonymousUsageStatisticsCheckBox.setText("Provide anonymous usage statistics to help us develop a better plugin");
        reportAnonymousUsageStatisticsCheckBox.setMnemonic('P');
        reportAnonymousUsageStatisticsCheckBox.setDisplayedMnemonicIndex(0);
        panel2.add(reportAnonymousUsageStatisticsCheckBox, cc.xy(1, 1));
        panel2.add(usageStatsHelp, cc.xy(2, 1, CellConstraints.LEFT, CellConstraints.CENTER));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(5, 3, new Insets(0, 12, 12, 12), -1, -1));
        mainPanel.add(panel3, cc.xy(1, 5, CellConstraints.DEFAULT, CellConstraints.TOP));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Direct Click Through"));
        ctrlHttpServer = new JCheckBox();
        ctrlHttpServer.setSelected(false);
        ctrlHttpServer.setText("Enable Direct Click Through");
        panel3.add(ctrlHttpServer, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel3.add(spacer4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setFont(new Font(label2.getFont().getName(), label2.getFont().getStyle(), 10));
        label2.setText("Information: You have to restart IDEA to apply changes (start/stop http server)");
        panel3.add(label2, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("  Direct Click Through TCP/IP Port: ");
        panel3.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        panel3.add(spacer5, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        panel3.add(spacer6, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        ctrlHttpServerPort = new JSpinner();
        panel3.add(ctrlHttpServerPort, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(70, -1), new Dimension(70, -1), null, 0, false));
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(checkNewVersionStable);
        buttonGroup.add(checkNewVersionAll);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(chkNoProxy);
        buttonGroup.add(chkUseIdeaProxy);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}

