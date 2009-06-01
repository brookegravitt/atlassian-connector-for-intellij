package com.atlassian.theplugin.idea.autoupdate;

import com.atlassian.theplugin.commons.configuration.GeneralConfigurationBean;
import com.atlassian.theplugin.commons.util.Version;
import com.atlassian.theplugin.idea.GenericHyperlinkListener;
import com.atlassian.theplugin.util.InfoServer;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

public class NewVersionInfoForm extends DialogWrapper {
    private static final String DOWNLOAD_TITLE = "Downloading new version of " + PluginUtil.getInstance().getName() + " plugin";
    private JPanel rootPane;
    private JLabel versionInfoLabel;
    private JPanel footerPanel;
    private JEditorPane releaseNotesLabel;

    private final Project project;


    private final GeneralConfigurationBean updateConfiguration;
    private final InfoServer.VersionInfo versionInfo;
    private boolean showConfigPath;

    public NewVersionInfoForm(final Project project, final GeneralConfigurationBean updateConfiguration,
                              final InfoServer.VersionInfo versionInfo, boolean showConfigPath) {
        super(project, false);
        this.updateConfiguration = updateConfiguration;
        this.versionInfo = versionInfo;
        this.project = project;
        this.showConfigPath = showConfigPath;
        initialize();
    }

    public NewVersionInfoForm(final Component parent, final GeneralConfigurationBean updateConfiguration,
                              final InfoServer.VersionInfo versionInfo, boolean showConfigPath) {
        super(parent, false);
        this.updateConfiguration = updateConfiguration;
        this.versionInfo = versionInfo;
        this.project = null;
        this.showConfigPath = showConfigPath;
        initialize();
    }

    private void initialize() {
        $$$setupUI$$$();
        createUIComponents();
        Version aVersion = versionInfo.getVersion();
        String versionInfoUpgrade = "<html>Do you want to upgrade from <b>" + PluginUtil.getInstance().getVersion() + " to <i>"
                + aVersion + "</i></b>?<br></html>";

        setTitle("New Atlassian Connector version " + aVersion + " is available.");
        setOKButtonText("Install");

        versionInfoLabel.setText(versionInfoUpgrade);
        StringBuilder sb = new StringBuilder();
        //releaseNotesUrl.setText("<html><a href=\"" + versionInfo.getReleaseNotesUrl() + "\">Release Notes</a><br></html>");


        sb.append(versionInfo.getReleaseNotes());

        releaseNotesLabel.setEditable(false);
        releaseNotesLabel.setContentType("text/html");
        releaseNotesLabel.addHyperlinkListener(new GenericHyperlinkListener());

        releaseNotesLabel.setText(sb.toString());

        init();

    }

    @Override
    protected void doOKAction() {
        Task.Backgroundable downloader = new Task.Backgroundable(project, DOWNLOAD_TITLE, false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                new PluginDownloader(versionInfo, updateConfiguration).run();
            }
        };
        ProgressManager.getInstance().run(downloader);
        dispose();
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        if (showConfigPath) {
            Messages.showMessageDialog("You can always install " + versionInfo.getVersion() + " version through " + PluginUtil.getInstance().getName()
                    + " configuration panel (Preferences | IDE Settings | " + PluginUtil.getInstance().getName() + " | General | Auto update | Check now)", "Information",
                    Messages.getInformationIcon());
        }

        dispose();
        super.doCancelAction();
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        return $$$getRootComponent$$$();
    }

    @Override
    public void dispose() {
        // so or so we mark this version so no more popups will appear
        updateConfiguration.setRejectedUpgrade(versionInfo.getVersion());
        super.dispose();
    }

    private void createUIComponents() {


        HyperlinkLabel label = new HyperlinkLabel("Release Notes");
        label.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                BrowserUtil.launchBrowser(versionInfo.getReleaseNotesUrl());
            }
        });


        footerPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        footerPanel.add(label, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        footerPanel.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
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
        rootPane.setMaximumSize(new Dimension(580, 466));
        rootPane.setMinimumSize(new Dimension(580, 466));
        rootPane.setPreferredSize(new Dimension(580, 466));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FormLayout("fill:d:grow", "center:max(d;4px):noGrow,top:3dlu:noGrow,center:d:grow"));
        rootPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, true));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setBackground(new Color(-1118482));
        scrollPane1.setMinimumSize(new Dimension(-1, -1));
        CellConstraints cc = new CellConstraints();
        panel1.add(scrollPane1, new CellConstraints(1, 3, 1, 1, CellConstraints.FILL, CellConstraints.FILL, new Insets(5, 5, 5, 5)));
        scrollPane1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null));
        releaseNotesLabel = new JEditorPane();
        releaseNotesLabel.setText("");
        scrollPane1.setViewportView(releaseNotesLabel);
        versionInfoLabel = new JLabel();
        versionInfoLabel.setText("Do you want to upgrade to the newest version?");
        panel1.add(versionInfoLabel, cc.xy(1, 1));
        footerPanel = new JPanel();
        footerPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPane.add(footerPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPane;
    }
}
