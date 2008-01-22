package com.atlassian.theplugin.idea;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.atlassian.theplugin.bamboo.BambooStatusListenerImpl;
import com.atlassian.theplugin.bamboo.BambooStatusChecker;
import com.atlassian.theplugin.bamboo.BuildStatus;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 14, 2008
 * Time: 3:41:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ThePluginProjectComponent implements ProjectComponent {
    private final Project project;
    private StatusBar statusBar;
    private JComponent statusBarComponent;
    private BambooStatusIcon statusBarIcon;
    private Timer timer = null;
    private BambooStatusChecker bambooStatusChecker = new BambooStatusChecker();
    private BambooStatusListenerImpl bambooStatusListener;

    public ThePluginProjectComponent(Project project) {
        this.project = project;
    }

    public void initComponent() {
        System.out.println("Init ThePlugin status component.");

        int timeout = 1000;
        if (timeout > 0) {
            timer = new Timer(timeout, new ActionListener()
            {

                public void actionPerformed(ActionEvent e) {
                    bambooStatusChecker.run();
                }
            });

        }
        statusBarIcon = new BambooStatusIcon();
        statusBarIcon.updateBambooStatus(BuildStatus.ERROR, "Waiting for Bamboo build statuses.");

        statusBarComponent = statusBarIcon;
        if (timer != null) {
            timer.start();
        }

        bambooStatusListener = new BambooStatusListenerImpl(statusBarIcon);
        bambooStatusChecker.registerListener(bambooStatusListener);
    }

    public void disposeComponent() {
        bambooStatusChecker.unregisterListener(bambooStatusListener);

        System.out.println("Dispose ThePlugin status component.");
        if (timer != null) {
            timer.stop();
        }
        statusBarComponent = null;
        statusBarIcon = null;


    }

    @NotNull
    public String getComponentName() {
        return "ThePluginProjectComponent";
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
        statusBarIcon.setText(status);
        statusBarIcon.setToolTipText(statusDescription);
    }
}
