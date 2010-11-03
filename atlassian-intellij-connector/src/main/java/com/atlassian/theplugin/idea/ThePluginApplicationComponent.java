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

import com.atlassian.connector.commons.jira.JIRAServerFacade2Impl;
import com.atlassian.theplugin.commons.SchedulableChecker;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.ssl.PluginSSLProtocolSocketFactory;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.configuration.IdeaPluginConfigurationBean;
import com.atlassian.theplugin.idea.autoupdate.NewVersionChecker;
import com.atlassian.theplugin.idea.config.ConfigPanel;
import com.atlassian.theplugin.idea.ui.CertMessageDialogImpl;
import com.atlassian.theplugin.util.HttpConfigurableIdeaImpl;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.veryquick.embweb.EmbeddedServer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class ThePluginApplicationComponent implements ApplicationComponent, Configurable {


    private static final Icon PLUGIN_SETTINGS_ICON = IconLoader.getIcon("/icons/ico_plugin.png");
    private static DisplayChangeHandler displayChangeHandler = new DisplayChangeHandler();
    private ConfigPanel configPanel;

    private final IdeaPluginConfigurationBean configuration;
    private final NewVersionChecker newVersionChecker;

    private final Timer timer = new Timer("atlassian-idea-plugin background status checkers");
    private static final int TIMER_START_DELAY = 20000;

    private final Collection<TimerTask> scheduledComponents = new HashSet<TimerTask>();

    public Collection<SchedulableChecker> getSchedulableCheckers() {
        return schedulableCheckers;
    }

    private final Collection<SchedulableChecker> schedulableCheckers = new HashSet<SchedulableChecker>();


    public ThePluginApplicationComponent(IdeaPluginConfigurationBean configuration, final NewVersionChecker newVersionChecker) {
        this.configuration = configuration;

        this.newVersionChecker = newVersionChecker;
        this.configuration.transientSetHttpConfigurable(HttpConfigurableIdeaImpl.getInstance());

        this.schedulableCheckers.add(newVersionChecker);

        LoggerImpl.setInstance(
                new IdeaLoggerImpl(com.intellij.openapi.diagnostic.Logger.getInstance(LoggerImpl.LOGGER_CATEGORY)));

        ConfigurationFactory.setConfiguration(configuration);
        PluginSSLProtocolSocketFactory.initializeSocketFactory(configuration.getGeneralConfigurationData(),
                new CertMessageDialogImpl());

        // start Direct Click Through http server
        if (configuration.getGeneralConfigurationData().isHttpServerEnabled()) {
            startHttpServer(configuration.getGeneralConfigurationData().getHttpServerPort());
        }

        addActionToDiffToolbar();
        //FileContentCache.setCacheSize(configuration.getCrucibleConfigurationData().getReviewFileCacheSize());
        //providers.setCacheSize(configuration.getCrucibleConfigurationData().getReviewFileCacheSize());
        printLocalVariables();
    }


    private void printLocalVariables() {
        final String classPathMsg = "java.class.path: " + System.getProperty("java.class.path");
        PluginUtil.getLogger().error(classPathMsg);
        System.out.println(classPathMsg);
        // extension directories whose jars are included on the classpath
        final String extDirsMsg = "java.ext.dirs: " + System.getProperty("java.ext.dirs");
        PluginUtil.getLogger().info(extDirsMsg);
         System.out.println(extDirsMsg);
        // low level classpath, includes system jars
        final String libraryPathMsg = "java.library.path: " + System.getProperty("java.library.path");
        PluginUtil.getLogger().info(libraryPathMsg);
        System.out.println(libraryPathMsg);
        // character to separate (not terminate!) entries on the classpath, ; for Windows : for unix.
        final String pathSeparatorMsg = "path.separator: " + System.getProperty("path.separator");
        PluginUtil.getLogger().info(pathSeparatorMsg);
        System.out.println(pathSeparatorMsg);
    }

    private void addActionToDiffToolbar() {
        AnAction addCommentAction = ActionManager.getInstance().getAction("ThePlugin.Crucible.Add.Comment.In.Editor");
        if (addCommentAction != null) {
            IdeaVersionFacade.getInstance().addActionToDiffGroup(addCommentAction);
        }
    }

    @Nls
    public String getDisplayName() {
        return "Atlassian\nConnector";
    }

    @Nullable
    public Icon getIcon() {
        return PLUGIN_SETTINGS_ICON;
    }

    @Nullable
    @NonNls
    public String getHelpTopic() {
        return null;
    }

    public IdeaPluginConfigurationBean getConfiguration() {
        return configuration;
    }

    @NotNull
    @NonNls
    public String getComponentName() {
        return "ThePluginApplicationComponent";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
        timer.cancel();
    }

    public JComponent createComponent() {
        if (configPanel == null) {
            configPanel = new ConfigPanel(configuration, newVersionChecker);
        }
        return configPanel;
    }

    public boolean isModified() {
        return configPanel != null && configPanel.isModified();
    }

    private void disableTimers() {
        Iterator<TimerTask> i = scheduledComponents.iterator();
        while (i.hasNext()) {
            TimerTask timerTask = i.next();
            i.remove();
            timerTask.cancel();
        }

        timer.purge();
    }

    /**
     * Reschedule the BambooStatusChecker with immediate execution trigger.
     *
     * @param rightNow set to false if the first execution should be delayed by {@link #TIMER_START_DELAY}.
     */
    public void rescheduleStatusCheckers(boolean rightNow) {

        disableTimers();
        long delay = rightNow ? 0 : TIMER_START_DELAY;

        for (SchedulableChecker checker : schedulableCheckers) {
            if (checker.canSchedule()) {
                final TimerTask newTask = checker.newTimerTask();
                scheduledComponents.add(newTask);
                timer.schedule(newTask, delay, checker.getInterval());
            } else {
                checker.resetListenersState();
            }
        }
    }


    public void apply() throws ConfigurationException {
        if (configPanel != null) {
            // Get data from configPanel to component
            configPanel.saveData();
            rescheduleStatusCheckers(true);
        }

    }

    public void reset() {
        if (configPanel != null) {
            // Reset configPanel data from component
            configPanel.setData();
        }
    }

    public void disposeUIResources() {
        configPanel = null;
    }

    private void startHttpServer(final int httpServerPort) {

        // load icon
        InputStream iconStream = ThePluginProjectComponent.class.getResourceAsStream("/icons/idea_small.png");

        if (iconStream == null) {
            PluginUtil.getLogger().error("Failed to load icon for http server");
            return;
        }

        // convert icon to byte array
        byte[] iconArray;
        try {
            int size = iconStream.available();
            iconArray = new byte[size];
            int readSize = iconStream.read(iconArray, 0, size);
            int loopCount = 0;
            while (readSize < size && loopCount++ < 5) {
                readSize += iconStream.read(iconArray, readSize, size - readSize);
            }

            if (readSize < size) {
                PluginUtil.getLogger().error("Failed to load icon for http server");
                return;
            }
        } catch (IOException e) {
            PluginUtil.getLogger().error("Failed to load icon for http server");            
            return;
        }

        // create and start server
        try {
            EmbeddedServer.createInstance(httpServerPort, new IdeHttpServerHandler(iconArray), true);
        } catch (Exception e) {
            PluginUtil.getLogger().error("Failed to start http server", e);
        }
    }

    /*
    * Source downloaded from : // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6209673
    * 
    * */
    private static class DisplayChangeHandler
            implements sun.awt.DisplayChangedListener, Runnable {
        // We must keep a strong reference to the DisplayChangedListener,
        //  since SunDisplayChanger keeps only a WeakReference to it.
        private static DisplayChangeHandler displayChangeHack;

        static {
            try {
                GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice[] devices = env.getScreenDevices();
                if (displayChangeHack == null) {
                    displayChangeHack = new DisplayChangeHandler();
                }
                //Add ourselves as a listener to the GraphicsEnvironment if possible.
                env.getClass()
                        .getMethod("addDisplayChangedListener", new Class[]{sun.awt.DisplayChangedListener.class})
                        .invoke(env, new Object[]{displayChangeHack});
            } catch (Throwable t) {
                System.out.println("Could not create DisplayChangeHandler to plug mem leaks due to display changes: " + t);
            }
        }

        public void displayChanged() {
            EventQueue.invokeLater(this);
        }

        public void paletteChanged() {
            EventQueue.invokeLater(this);
        }

        public void run() {
            //Force the RepaintManager to clear out all of the VolatileImage back-buffers that it has cached.
            //	See Sun bug 6209673.
            // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6209673
            RepaintManager rm = RepaintManager.currentManager(null);
            Dimension size = rm.getDoubleBufferMaximumSize();
            rm.setDoubleBufferMaximumSize(new Dimension(0, 0));
            rm.setDoubleBufferMaximumSize(size);
        }
    }


}
