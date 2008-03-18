package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.jira.api.*;

import java.util.List;

public class JIRAServer {
    private Server server;
    private List<JIRAProject> projects;
    private List statuses;
    private List issueTypes;

    public JIRAServer() {
    }

    public JIRAServer(Server server) {
        this.server = server;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
 
    public List<JIRAProject> getProjects() {
        if (projects == null) {
            try {
                projects = JIRAServerFactory.getJIRAServerFacade().getProjects(server);
            } catch (JIRAException e) {
                e.printStackTrace();
            }
        }
		               
        return projects;
    }

    public List<JIRAConstant> getStatuses() {
        if (statuses == null) {
            try {
                statuses = JIRAServerFactory.getJIRAServerFacade().getStatuses(server);
            } catch (JIRAException e) {
                e.printStackTrace();
            }
        }

        return statuses;
    }

    public List<JIRAConstant> getIssueTypes() {
        if (issueTypes == null) {
            try {
                issueTypes = JIRAServerFactory.getJIRAServerFacade().getIssueTypes(server);
            } catch (JIRAException e) {
                e.printStackTrace();
            }
        }

        return issueTypes;
    }
}
