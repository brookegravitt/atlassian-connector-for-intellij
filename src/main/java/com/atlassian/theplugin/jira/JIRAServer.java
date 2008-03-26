package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.jira.api.*;

import java.util.List;

public class JIRAServer {
	private Server server;
	private boolean validServer = false;
	private String errorMessage = null;

	private List<JIRAProject> projects;
    private List statuses;
    private List issueTypes;

	private final JIRAServerFacade jiraServerFacade;

	public JIRAServer(JIRAServerFacade jiraServerFacade) {
		this.jiraServerFacade = jiraServerFacade;
	}

    public JIRAServer(Server server, JIRAServerFacade jiraServerFacade) {
        this.server = server;
		this.jiraServerFacade = jiraServerFacade;
	}

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
 
    public List<JIRAProject> getProjects() {
		validServer = false;
		if (projects == null) {
			errorMessage = null;
			try {
                projects = jiraServerFacade.getProjects(server);
				validServer = true;
			} catch (JIRAException e) {
				errorMessage = e.getMessage();
				e.printStackTrace();
            }
        } else {
			validServer = true;
		}
		               
        return projects;
    }

    public List<JIRAConstant> getStatuses() {
		validServer = false;
		if (statuses == null) {
			errorMessage = null;
			try {
                statuses = jiraServerFacade.getStatuses(server);
				validServer = true;
			} catch (JIRAException e) {
				errorMessage = e.getCause().getMessage();
            }
        } else {
			validServer = true;
		}

        return statuses;
    }

    public List<JIRAConstant> getIssueTypes() {
		validServer = false;
		if (issueTypes == null) {
			errorMessage = null;			
			try {
                issueTypes = jiraServerFacade.getIssueTypes(server);
				validServer = true;
			} catch (JIRAException e) {
				errorMessage = e.getMessage();
            }
        } else {
			validServer = true;
		}
		
		return issueTypes;
	}

	public boolean isValidServer() {
		return validServer;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
}
