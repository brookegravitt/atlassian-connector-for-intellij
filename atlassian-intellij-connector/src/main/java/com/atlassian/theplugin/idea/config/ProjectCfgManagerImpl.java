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
package com.atlassian.theplugin.idea.config;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.intellij.configuration.UserCfgBean;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.bamboo.BambooServerData;
import com.atlassian.theplugin.commons.cfg.*;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.StringUtil;
import com.atlassian.theplugin.configuration.WorkspaceConfigurationBean;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;

public class ProjectCfgManagerImpl implements ProjectCfgManager {

    private final WorkspaceConfigurationBean workspaceConfiguration;
    private final Project project;
    private ProjectConfiguration projectConfiguration = new ProjectConfiguration();
    private Collection<ConfigurationListener> listeners = new ArrayList<ConfigurationListener>(100);

    /**
     * Do NOT use the constructor. It is called by PICO.
     *
     * @param workspaceConfiguration used for default credentials purposes only (can be null for tests)
     */
    public ProjectCfgManagerImpl(WorkspaceConfigurationBean workspaceConfiguration, Project project) {
        this.workspaceConfiguration = workspaceConfiguration;
        this.project = project;
    }

    /**
     * This method has a package scope and should be used only for 'saving and modifying' purposes.
     * This method can also be used in JUnit tests.
     *
     * @return project configuration inner object
     */
    ProjectConfiguration getProjectConfiguration() {
        return projectConfiguration;
    }

    /**
     * This method has a package scope and should be used only for 'saving and modifying' purposes.
     * This method can also be used in JUnit tests.
     *
     * @param configuration new configuration
     */
    void updateProjectConfiguration(final ProjectConfiguration configuration) {

        if (configuration == null) {
            throw new NullPointerException("Project configuration cannot be null");
        }

        ProjectConfiguration oldConfiguration = null;
        if (getProjectConfiguration() != null) {
            oldConfiguration = new ProjectConfiguration(getProjectConfiguration());
        }

        projectConfiguration = configuration;

        notifyListeners(projectConfiguration, oldConfiguration);
    }

    ///////////////////////////////////////////////////////////////////
    ///////////////////// DEFAULT CREDENTIALS /////////////////////////
    ///////////////////////////////////////////////////////////////////
    /////////////////////// PACKAGE SCOPE /////////////////////////////
    ///////////////////////////////////////////////////////////////////

    boolean isDefaultCredentialsAsked() {
        return workspaceConfiguration.isDefaultCredentialsAsked();
    }

    void setDefaultCredentialsAsked(final boolean defaultCredentialsAsked) {
        workspaceConfiguration.setDefaultCredentialsAsked(defaultCredentialsAsked);
    }

    @NotNull
    public UserCfg getDefaultCredentials() {

        if (!isGoodProject(project)) {
            return new UserCfg(workspaceConfiguration.getDefaultCredentials().getUsername(),
                    StringUtil.decode(workspaceConfiguration.getDefaultCredentials().getEncodedPassword()));
        } else {
            String password = PasswordStorage.getPassword(project);
            if (password == null) {
                password = StringUtil.decode(workspaceConfiguration.getDefaultCredentials().getEncodedPassword());
                PasswordStorage.setPassword(project, password);
            }
                        
            return new UserCfg(workspaceConfiguration.getDefaultCredentials().getUsername(), password);
        }
    }


    void setDefaultCredentials(@NotNull final UserCfg defaultCredentials) {
        if (isGoodProject(project) && PasswordStorage.setPassword(project, defaultCredentials.getPassword())) {
            workspaceConfiguration.setDefaultCredentials(new UserCfgBean(defaultCredentials.getUsername(), ""));
        } else {
            workspaceConfiguration.setDefaultCredentials(
                    new UserCfgBean(defaultCredentials.getUsername(),
                            StringUtil.encode(defaultCredentials.getPassword())));
        }
    }

    private boolean isGoodProject(Project project) {
        return project != null  && project.isInitialized() && !project.isDisposed();
    }

    //////////////////////////////////////////////////////////////////
    ///////////////////// GET SERVER METHODS /////////////////////////
    //////////////////////////////////////////////////////////////////

    // SINGLE SERVER

    @Deprecated
    public ServerCfg getServer(final ServerId serverId) {
        for (ServerCfg server : getAllServers()) {
            if (serverId != null && server.getServerId().equals(serverId)) {
                return server;
            }
        }
        return null;
    }

    /**
     * Returns ServerData for server with serverId specified by parameter
     *
     * @param serverId
     * @return ServerData for enabled server with serverId specified by parameter
     */
    @Nullable
    public ServerData getServerr(final ServerId serverId) {
        for (ServerCfg server : getAllServers()) {
            if (serverId != null && server.getServerId().equals(serverId)) {
                return getServerData(server);
            }
        }
        return null;
    }

    /**
     * Returns ServerData for enabled server with serverId specified by parameter
     *
     * @param serverId
     * @return ServerData for enabled server with serverId specified by parameter
     */
    @Nullable
    public ServerData getEnabledServerr(final ServerId serverId) {
        for (ServerCfg server : getAllEnabledServers()) {
            if (serverId != null && server.getServerId().equals(serverId)) {
                return getServerData(server);
            }
        }
        return null;
    }


    public JiraServerData getJiraServerr(final ServerId serverId) {
        for (JiraServerCfg server : getAllJiraServers()) {
            if (serverId != null && server.getServerId().equals(serverId)) {
                return getServerData(server);
            }
        }
        return null;
    }

    public JiraServerData getEnabledJiraServerr(final ServerId serverId) {
        for (JiraServerCfg server : getAllEnabledJiraServers()) {
            if (serverId != null && server.getServerId().equals(serverId)) {
                return getServerData(server);
            }
        }
        return null;
    }


    public ServerData getCrucibleServerr(final ServerId serverId) {
        for (CrucibleServerCfg server : getAllCrucibleServers()) {
            if (serverId != null && server.getServerId().equals(serverId)) {
                return getServerData(server);
            }
        }
        return null;
    }


    public ServerData getEnabledCrucibleServerr(final ServerId serverId) {
        for (CrucibleServerCfg server : getAllEnabledCrucibleServers()) {
            if (serverId != null && server.getServerId().equals(serverId)) {
                return getServerData(server);
            }
        }
        return null;
    }

    // MULTIPLE SERVERS

    @Deprecated
        // that method should be private (not used by Unit Tests)
    Collection<ServerCfg> getAllServers() {
        return new ArrayList<ServerCfg>(projectConfiguration.getServers());
    }

    @Deprecated
        // that method should be private (not used by Unit Tests)
    Collection<ServerCfg> getAllServers(ServerType serverType) {

        Collection<ServerCfg> tmp = getAllServers();

        Collection<ServerCfg> ret = new ArrayList<ServerCfg>();

        for (ServerCfg serverCfg : tmp) {
            if (serverCfg.getServerType() == serverType) {
                ret.add(serverCfg);
            }
        }
        return ret;
    }

    public Collection<ServerData> getAllServerss() {
        Collection<ServerCfg> tmp = getAllServers();
        Collection<ServerData> ret = new ArrayList<ServerData>();

        for (ServerCfg serverCfg : tmp) {
            ret.add(getServerData(serverCfg));
        }
        return ret;
    }

    public Collection<ServerData> getAllServerss(final ServerType serverType) {
        Collection<ServerCfg> tmp = getAllServers();
        Collection<ServerData> ret = new ArrayList<ServerData>();

        for (ServerCfg serverCfg : tmp) {
            if (serverCfg.getServerType() == serverType) {
                ret.add(getServerData(serverCfg));
            }
        }
        return ret;
    }

    @Deprecated
        // that method should be private (not used by Unit Tests)
    Collection<ServerCfg> getAllEnabledServers() {

        Collection<ServerCfg> ret = new ArrayList<ServerCfg>();
        for (ServerCfg serverCfg : getAllServers()) {
            if (serverCfg.isEnabled()) {
                ret.add(serverCfg);
            }
        }
        return ret;
    }

    public Collection<ServerData> getAllEnabledServerss() {
        Collection<ServerCfg> tmp = getAllEnabledServers();
        Collection<ServerData> ret = new ArrayList<ServerData>();

        for (ServerCfg serverCfg : tmp) {
            ret.add(getServerData(serverCfg));
        }
        return ret;
    }


    @Deprecated
    // that method should be private (not used by Unit Tests)
    public Collection<ServerCfg> getAllEnabledServers(ServerType serverType) {

        Collection<ServerCfg> tmp = getAllEnabledServers();
        Collection<ServerCfg> ret = new ArrayList<ServerCfg>();

        for (ServerCfg serverCfg : tmp) {
            if (serverCfg.getServerType() == serverType) {
                ret.add(serverCfg);
            }
        }
        return ret;
    }

    public Collection<ServerData> getAllEnabledCrucibleServersContainingFisheye() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection<ServerData> getAllEnabledServerss(final ServerType serverType) {
        Collection<ServerCfg> tmp = getAllEnabledServers();
        Collection<ServerData> ret = new ArrayList<ServerData>();

        for (ServerCfg serverCfg : tmp) {
            if (serverCfg.getServerType() == serverType) {
                ret.add(getServerData(serverCfg));
            }
        }
        return ret;
    }

    @Deprecated
    Collection<BambooServerCfg> getAllBambooServers() {
        Collection<ServerCfg> tmp = getAllServers();

        ArrayList<BambooServerCfg> ret = new ArrayList<BambooServerCfg>();

        for (ServerCfg serverCfg : tmp) {
            if (serverCfg.getServerType() == ServerType.BAMBOO_SERVER && serverCfg instanceof BambooServerCfg) {
                ret.add((BambooServerCfg) serverCfg);
            }
        }
        return ret;
    }

    @Deprecated
    Collection<JiraServerCfg> getAllJiraServers() {
        Collection<ServerCfg> tmp = getAllServers();
        ArrayList<JiraServerCfg> ret = new ArrayList<JiraServerCfg>();

        for (ServerCfg serverCfg : tmp) {
            if (serverCfg.getServerType() == ServerType.JIRA_SERVER && serverCfg instanceof JiraServerCfg) {
                ret.add((JiraServerCfg) serverCfg);
            }
        }
        return ret;
    }


    public Collection<ServerData> getAllJiraServerss() {
        Collection<ServerCfg> tmp = getAllServers();
        ArrayList<ServerData> ret = new ArrayList<ServerData>();

        for (ServerCfg serverCfg : tmp) {
            if (serverCfg.getServerType() == ServerType.JIRA_SERVER && serverCfg instanceof JiraServerCfg) {
                JiraServerCfg jiraServerCfg = (JiraServerCfg) serverCfg;
                ret.add(getServerData(jiraServerCfg));
            }
        }
        return ret;
    }


    @Deprecated
    Collection<CrucibleServerCfg> getAllCrucibleServers() {

        Collection<ServerCfg> tmp = getAllServers();

        ArrayList<CrucibleServerCfg> ret = new ArrayList<CrucibleServerCfg>();

        for (ServerCfg serverCfg : tmp) {
            if (serverCfg.getServerType() == ServerType.CRUCIBLE_SERVER && serverCfg instanceof CrucibleServerCfg) {
                ret.add((CrucibleServerCfg) serverCfg);
            }
        }
        return ret;
    }

    public Collection<ServerData> getAllCrucibleServerss() {
        Collection<ServerCfg> tmp = getAllServers();

        ArrayList<ServerData> ret = new ArrayList<ServerData>();

        for (ServerCfg serverCfg : tmp) {
            if (serverCfg.getServerType() == ServerType.CRUCIBLE_SERVER && serverCfg instanceof CrucibleServerCfg) {
                ret.add(getServerData(serverCfg));
            }
        }
        return ret;
    }




    public Collection<BambooServerData> getAllBambooServerss() {
        Collection<ServerCfg> tmp = getAllServers();
        Collection<BambooServerData> ret = new ArrayList<BambooServerData>();

        for (ServerCfg serverCfg : tmp) {
            if (serverCfg.getServerType() == ServerType.BAMBOO_SERVER && serverCfg instanceof BambooServerCfg) {
                ret.add(getServerData((BambooServerCfg) serverCfg));
            }
        }
        return ret;
    }

    @Deprecated
    Collection<BambooServerCfg> getAllEnabledBambooServers() {

        Collection<ServerCfg> tmp = getAllEnabledServers();
        Collection<BambooServerCfg> ret = new ArrayList<BambooServerCfg>();

        for (ServerCfg serverCfg : tmp) {
            if (serverCfg.getServerType() == ServerType.BAMBOO_SERVER && serverCfg instanceof BambooServerCfg) {
                BambooServerCfg bambooServerCfg = (BambooServerCfg) serverCfg;
                ret.add(bambooServerCfg);
            }
        }
        return ret;
    }


    public Collection<BambooServerData> getAllEnabledBambooServerss() {
        Collection<ServerCfg> tmp = getAllEnabledServers();
        Collection<BambooServerData> ret = new ArrayList<BambooServerData>();

        for (ServerCfg serverCfg : tmp) {
            if (serverCfg.getServerType() == ServerType.BAMBOO_SERVER && serverCfg instanceof BambooServerCfg) {
                ret.add(getServerData((BambooServerCfg) serverCfg));
            }
        }
        return ret;
    }

    @Deprecated
    Collection<JiraServerCfg> getAllEnabledJiraServers() {

        Collection<ServerCfg> tmp = getAllEnabledServers();
        Collection<JiraServerCfg> ret = new ArrayList<JiraServerCfg>();

        for (ServerCfg serverCfg : tmp) {
            if (serverCfg.getServerType() == ServerType.JIRA_SERVER && serverCfg instanceof JiraServerCfg) {
                JiraServerCfg jiraServerCfg = (JiraServerCfg) serverCfg;
                ret.add(jiraServerCfg);
            }
        }
        return ret;
    }


    public Collection<JiraServerData> getAllEnabledJiraServerss() {
        Collection<ServerCfg> tmp = getAllEnabledServers();
        Collection<JiraServerData> ret = new ArrayList<JiraServerData>();

        for (ServerCfg serverCfg : tmp) {
            if (serverCfg.getServerType() == ServerType.JIRA_SERVER && serverCfg instanceof JiraServerCfg) {
                JiraServerCfg jiraServerCfg = (JiraServerCfg) serverCfg;
                ret.add(getServerData(jiraServerCfg));
            }
        }
        return ret;
    }

    @Deprecated
    Collection<CrucibleServerCfg> getAllEnabledCrucibleServers() {

        Collection<ServerCfg> tmp = getAllEnabledServers();
        Collection<CrucibleServerCfg> ret = new ArrayList<CrucibleServerCfg>();

        for (ServerCfg serverCfg : tmp) {
            if (serverCfg.getServerType() == ServerType.CRUCIBLE_SERVER && serverCfg instanceof CrucibleServerCfg) {
                CrucibleServerCfg crucibleServerCfg = (CrucibleServerCfg) serverCfg;
                ret.add(crucibleServerCfg);
            }
        }
        return ret;

    }


    public Collection<ServerData> getAllEnabledCrucibleServerss() {
        Collection<ServerCfg> tmp = getAllEnabledServers();
        Collection<ServerData> ret = new ArrayList<ServerData>();

        for (ServerCfg serverCfg : tmp) {
            if (serverCfg.getServerType() == ServerType.CRUCIBLE_SERVER && serverCfg instanceof CrucibleServerCfg) {
                CrucibleServerCfg crucibleServerCfg = (CrucibleServerCfg) serverCfg;
                ret.add(getServerData(crucibleServerCfg));
            }
        }
        return ret;

    }

    ///////////////////////////////////////////////////////////////
    ///////////////////// DEFAULT SERVERS /////////////////////////
    ///////////////////////////////////////////////////////////////

    @Nullable
    public JiraServerData getDefaultJiraServer() {
        ProjectConfiguration prjCfg = getProjectConfiguration();
        if (prjCfg != null) {
            JiraServerCfg jiraServer = prjCfg.getDefaultJiraServer();
            if (jiraServer != null) {
                return getServerData(jiraServer);
            }
        }

        // PL-1697
        Collection<JiraServerData> allServers = getAllEnabledJiraServerss();
        if (allServers != null && allServers.size() == 1) {
            return allServers.iterator().next();
        }

        return null;
    }

    @Nullable
    public ServerData getDefaultCrucibleServer() {
        ProjectConfiguration prjCfg = getProjectConfiguration();
        if (prjCfg != null) {
            CrucibleServerCfg crucibleServer = prjCfg.getDefaultCrucibleServer();
            if (crucibleServer != null) {
                return getServerData(crucibleServer);
            }
        }

        // PL-1697
        Collection<ServerData> allServers = getAllEnabledCrucibleServerss();
        if (allServers != null && allServers.size() == 1) {
            return allServers.iterator().next();
        }

        return null;
    }

    public ServerData getDefaultFishEyeServer() {
        return null;
    }


    public boolean isDefaultJiraServerValid() {
        boolean defaultValid = getProjectConfiguration().isDefaultJiraServerValid();
        if (defaultValid) {
            return true;
        }

        // PL-1697
        Collection<JiraServerData> allServers = getAllEnabledJiraServerss();
        return allServers != null && allServers.size() == 1;
    }

    public Collection<ServerData> getAllFishEyeServerss() {
        return null;
    }

    public String getDefaultCrucibleRepo() {
        return getProjectConfiguration().getDefaultCrucibleRepo();
    }

    public String getDefaultCrucibleProject() {
        return getProjectConfiguration().getDefaultCrucibleProject();
    }

    public String getDefaultFishEyeRepo() {
        return null;
    }

    public String getFishEyeProjectPath() {
        return null;
    }

    private static ServerData findServer(final URL serverUrl, final Collection<ServerData> servers) {

        ServerData enabledServer = null;
        ServerData disabledServer = null;

        // find matching server
        for (ServerData server : servers) {

            URL url;

            try {
                url = new URL(server.getUrl());
            } catch (MalformedURLException e) {
                // skip the server if url is broken
                continue;
            }

            // compare urls (skip protocols and query string)
            if (url.getHost().equalsIgnoreCase(serverUrl.getHost())
                    && url.getPort() == serverUrl.getPort()
                    && (((url.getPath() == null || url.getPath().equals("") || url.getPath().equals("/"))
                    && (serverUrl.getPath() == null || serverUrl.getPath().equals("") || serverUrl.getPath().equals("/")))
                    || (url.getPath() != null && serverUrl.getPath() != null && url.getPath().equals(serverUrl.getPath())))) {

                if (server.isEnabled()) {
                    enabledServer = server;
                    break;
                } else if (disabledServer == null) {
                    disabledServer = server;
                }
            }
        }

        if (enabledServer != null) {
            return enabledServer;
        }

        return disabledServer;
    }

    public ServerData findServer(final String serverUrl, final Collection<ServerData> servers) {

        ServerData enabledServer = null;
        ServerData disabledServer = null;

        String trimmedServerUrl;
        if (serverUrl.endsWith("/")) {
            trimmedServerUrl = serverUrl.substring(0, serverUrl.lastIndexOf("/"));
        } else {
            trimmedServerUrl = serverUrl;
        }
        // find matching server
        for (ServerData server : servers) {
            if (server.getUrl().trim().equals(trimmedServerUrl)) {
                if (server.isEnabled()) {
                    enabledServer = server;
                    break;
                } else if (disabledServer == null) {
                    disabledServer = server;
                }
            }
        }

        if (enabledServer != null) {
            return enabledServer;
        }

        if (disabledServer != null) {
            return disabledServer;
        }

        URL url;

        try {
            url = new URL(trimmedServerUrl);
        } catch (MalformedURLException e) {
            PluginUtil.getLogger().warn("Error opening issue. Invalid url [" + trimmedServerUrl + "]", e);
            return null;
        }

        return findServer(url, servers);
    }

    //////////////////////////////////////////////////////////////////
    ///////////////////// CONFIG LISTENERS ///////////////////////////
    //////////////////////////////////////////////////////////////////

    public synchronized void addProjectConfigurationListener(final ConfigurationListener configurationListener) {

        if (configurationListener == null) {
            return;
        }

        if (listeners == null) {
            listeners = new CopyOnWriteArraySet<ConfigurationListener>(); //MiscUtil.buildHashSet();
        }
        listeners.add(configurationListener);
    }

    public synchronized boolean removeProjectConfigurationListener(final ConfigurationListener configurationListener) {

        if (configurationListener == null) {
            return false;
        }

        return listeners.remove(configurationListener);
    }

    ///////////////////////////////////////////////////////////////////
    /////////////////// PRIVATE SERVER DATA STUFF /////////////////////
    ///////////////////////////////////////////////////////////////////

    @NotNull
    private ServerData getServerData(@NotNull Server serverCfg) {
        ServerData.Builder builder = new ServerData.Builder(serverCfg);

        builder.defaultUser(getDefaultCredentials());
        return builder.build();
    }

    private BambooServerData getServerData(@NotNull BambooServerCfg serverCfg) {
        BambooServerData.Builder builder = new BambooServerData.Builder(serverCfg);
        builder.defaultUser(getDefaultCredentials());
        return builder.build();

    }

    private JiraServerData getServerData(@NotNull JiraServerCfg serverCfg) {
        JiraServerData.Builder builder = new JiraServerData.Builder(serverCfg);
        builder.defaultUser(getDefaultCredentials());
        return builder.build();
    }

    //////////////////////////////////////////////////////////////////
    ///////////////////// ADD REMOVE SERVERS /////////////////////////
    //////////////////////////////////////////////////////////////////

    /**
     * todo Should be package scope (only for JUnit tests)
     *
     * @param serverCfg
     */
    public void addServer(final ServerCfg serverCfg) {

        if (serverCfg == null) {
            return;
        }

        if (projectConfiguration == null) {
            projectConfiguration = new ProjectConfiguration();
        }

        if (!projectConfiguration.getServers().contains(serverCfg)) {
            projectConfiguration.getServers().add(serverCfg);
        }
    }

    /**
     * Package scope (only for JUnit tests)
     *
     * @param serverId id of the server to remove
     * @return removed server or null if nothing removed
     */
    ServerCfg removeServer(final ServerId serverId) {

        if (serverId == null) {
            return null;
        }

        Iterator<ServerCfg> it = projectConfiguration.getServers().iterator();
        while (it.hasNext()) {
            ServerCfg serverCfg = it.next();
            if (serverCfg.getServerId().equals(serverId)) {
                it.remove();
                return serverCfg;
            }
        }
        return null;
    }

    //////////////////////////////////////////////////////////////////
    ///////////////////// LISTENERS ACTIONS //////////////////////////
    //////////////////////////////////////////////////////////////////

    private interface ProjectListenerAction {
        void run(final ConfigurationListener projectListener);
    }


    private void notifyListeners(ProjectConfiguration newConfiguration, ProjectConfiguration oldConfiguration) {

        ProjectListenerAction[] actions = {
                new UpdateConfigurationListenerAction(newConfiguration),
                new ConfigurationTypeChangedAction(newConfiguration, oldConfiguration),
                new ServerChangedAction(newConfiguration, oldConfiguration),
                new ServerAddedAction(newConfiguration, oldConfiguration),
                new ServerRemovedAction(newConfiguration, oldConfiguration),
                new ServerEnabledDisabledAction(newConfiguration, oldConfiguration)
        };

        for (ProjectListenerAction action : actions) {
            notifyListeners(action);
        }
    }

    private synchronized void notifyListeners(ProjectListenerAction listenerAction) {
        Collection<ConfigurationListener> localListeners = new ArrayList<ConfigurationListener>(listeners);
        if (localListeners != null) {
            for (ConfigurationListener projectListener : localListeners) {
                listenerAction.run(projectListener);
            }
        }
    }

    private static class UpdateConfigurationListenerAction implements ProjectListenerAction {

        private final ProjectConfiguration projectConfiguration;

        public UpdateConfigurationListenerAction(final ProjectConfiguration projectConfiguration) {
            this.projectConfiguration = projectConfiguration;
        }

        public void run(final ConfigurationListener projectListener
        ) {
            projectListener.configurationUpdated(projectConfiguration);
        }
    }

    private class ServerChangedAction implements ProjectListenerAction {
        protected final ProjectConfiguration newConfiguration;
        protected ProjectConfiguration oldConfiguration;

        public ServerChangedAction(ProjectConfiguration newConfiguration, ProjectConfiguration oldConfiguration) {
            this.newConfiguration = newConfiguration;
            this.oldConfiguration = oldConfiguration;
        }

        public void run(ConfigurationListener projectListener) {
            if (oldConfiguration == null || newConfiguration == null) {
                return;
            }                 

            for (ServerCfg oldServer : oldConfiguration.getServers()) {
                ServerCfg newServer = newConfiguration.getServerCfg(oldServer.getServerId());

                // server general update
                if (newServer != null && !oldServer.equals(newServer)) {
                    projectListener.serverDataChanged(getServerData(oldServer));

                    // server url or credentials updated
                    if (checkCredentialsChanged(oldServer, newServer)
                            || checkUrlChanged(oldServer, newConfiguration.getServerCfg(oldServer.getServerId()))) {
                        projectListener.serverConnectionDataChanged(oldServer.getServerId());
                    }

                    // server name updated
                    if (!oldServer.getName().equals(newServer.getName())) {
                        projectListener.serverNameChanged(oldServer.getServerId());
                    }

                }
            }
        }

        protected boolean checkCredentialsChanged(final ServerCfg oldServer, final ServerCfg newServer) {
            if (newServer == null) {
                return false;
            }

            return !oldServer.getUsername().equals(newServer.getUsername())
                    || !oldServer.getPassword().equals(newServer.getPassword())
                    || oldServer.isUseDefaultCredentials() != newServer.isUseDefaultCredentials();

        }

        private boolean checkUrlChanged(final ServerCfg oldServer, final ServerCfg newServer) {
            if (newServer == null) {
                return false;
            }
            return !oldServer.getUrl().equals(newServer.getUrl());
        }
    }

    private class ServerAddedAction implements ProjectListenerAction {
        private final ProjectConfiguration newConfiguration;
        private final ProjectConfiguration oldConfiguration;

        public ServerAddedAction(ProjectConfiguration newConfiguration, ProjectConfiguration oldConfiguration) {
            this.newConfiguration = newConfiguration;
            this.oldConfiguration = oldConfiguration;
        }

        public void run(ConfigurationListener projectListener) {
            if (oldConfiguration == null || newConfiguration == null) {
                return;
            }

            for (ServerCfg newServer : newConfiguration.getServers()) {
                if (oldConfiguration.getServerCfg(newServer.getServerId()) == null) {
//					projectListener.serverAdded(newServer);
                    projectListener.serverAdded(getServerData(newServer));
                }
            }
        }
    }

    private class ServerRemovedAction implements ProjectListenerAction {
        private final ProjectConfiguration newConfiguration;
        private final ProjectConfiguration oldConfiguration;

        public ServerRemovedAction(ProjectConfiguration newConfiguration, ProjectConfiguration oldConfiguration) {
            this.newConfiguration = newConfiguration;
            this.oldConfiguration = oldConfiguration;
        }

        public void run(ConfigurationListener projectListener) {
            if (oldConfiguration == null || newConfiguration == null) {
                return;
            }

            for (ServerCfg oldServer : oldConfiguration.getServers()) {
                if (newConfiguration.getServerCfg(oldServer.getServerId()) == null) {
//					projectListener.serverRemoved(oldServer);
                    projectListener.serverRemoved(getServerData(oldServer));
                }
            }
        }
    }

    private class ServerEnabledDisabledAction implements ProjectListenerAction {

        private final ProjectConfiguration newConfiguration;
        private final ProjectConfiguration oldConfiguration;

        public ServerEnabledDisabledAction(ProjectConfiguration newConfiguration, ProjectConfiguration oldConfiguration) {
            this.newConfiguration = newConfiguration;
            this.oldConfiguration = oldConfiguration;
        }

        public void run(ConfigurationListener projectListener) {
            if (oldConfiguration == null || newConfiguration == null) {
                return;
            }

            for (ServerCfg oldServer : oldConfiguration.getServers()) {
                ServerCfg newServer = newConfiguration.getServerCfg(oldServer.getServerId());
                if (newServer != null) {
                    if (!oldServer.isEnabled() && newServer.isEnabled()) {
                        projectListener.serverEnabled(getServerData(oldServer));
                    } else if (oldServer.isEnabled() && !newServer.isEnabled()) {
                        projectListener.serverDisabled(oldServer.getServerId());
                    }
                }
            }
        }

    }

    private static class ConfigurationTypeChangedAction implements ProjectListenerAction {
        private final ProjectConfiguration newConfiguration;
        private final ProjectConfiguration oldConfiguration;

        public ConfigurationTypeChangedAction(
                ProjectConfiguration newConfiguration, ProjectConfiguration oldConfiguration) {
            this.newConfiguration = newConfiguration;
            this.oldConfiguration = oldConfiguration;
        }

        public void run(ConfigurationListener projectListener) {
            if (oldConfiguration == null || newConfiguration == null) {
                return;
            }

            // Collections.constainsAll is used in both directions below instead of Collection.equlas
            // as equals for Collection compares only references
            // and we cannot be sure if used implementation overrides equals correctly
            // and e.g. equals for List requires the same order of elements

            // JIRA servers changed
            Collection<JiraServerCfg> newJiraServers = newConfiguration.getAllJIRAServers();
            Collection<JiraServerCfg> oldJiraServers = oldConfiguration.getAllJIRAServers();
            if (!newJiraServers.containsAll(oldJiraServers) || !oldJiraServers.containsAll(newJiraServers)) {
                projectListener.jiraServersChanged(newConfiguration);
            }

            // Bamboo servers changed
            Collection<BambooServerCfg> newBambooServers = newConfiguration.getAllBambooServers();
            Collection<BambooServerCfg> oldBambooServers = oldConfiguration.getAllBambooServers();
            if (!newBambooServers.containsAll(oldBambooServers) || !oldBambooServers.containsAll(newBambooServers)) {
                projectListener.bambooServersChanged(newConfiguration);
            }
		}
	}

}
