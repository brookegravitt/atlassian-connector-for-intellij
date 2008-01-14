package com.atlassian.theplugin.idea;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 14, 2008
 * Time: 3:41:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ThePluginModuleComponent implements ProjectComponent {
    private final Project project;
    private StatusBar statusBar;
    private JComponent statusBarComponent;
    private JLabel bambooLabel;

    public ThePluginModuleComponent(Project project) {
        this.project = project;
    }

    public void initComponent() {
        System.out.println("initComponent");
        bambooLabel = new JLabel();
        bambooLabel.setText("BMB");
        bambooLabel.setToolTipText("ToolTip");
        
        statusBarComponent = bambooLabel;

    }

    public void disposeComponent() {
        System.out.println("disposeComponent");
        statusBarComponent = null;
        bambooLabel = null;
    }

    @NotNull
    public String getComponentName() {
        return "ThePluginModuleComponent";
    }

    public void projectOpened() {
        statusBar = WindowManager.getInstance().getStatusBar(project);
        statusBar.addCustomIndicationComponent(statusBarComponent);
        System.out.println("projectOpened");
    }

    public void projectClosed() {
        statusBar.setInfo("disposeComponent");
        statusBar.removeCustomIndicationComponent(statusBarComponent);
        System.out.println("projectClosed");

    }

    public void setBambooStatus(String status, String statusDescription) {
        bambooLabel.setText(status);
        bambooLabel.setToolTipText(statusDescription);
    }
}
