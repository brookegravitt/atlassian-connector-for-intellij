package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.jira.api.JIRAConstant;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAProject;

import java.util.Collections;
import java.util.List;

public class JIRAServer {
	private Server server;
	private boolean validServer = false;
	private String errorMessage = null;

	private List<JIRAProject> projects;
	private List statuses;
	private List issueTypes;
	private List savedFilters;
	private List versions;
	private List components;
	private List priorieties;
	private List resolutions;

	private JIRAProject lastProject = null;
	private JIRAProject currentProject = null;

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
				if (currentProject == null) {
					issueTypes = jiraServerFacade.getIssueTypes(server);
				} else {
					issueTypes = jiraServerFacade.getIssueTypesForProject(server, Long.toString(currentProject.getId()));
				}
				lastProject = currentProject;
				validServer = true;
			} catch (JIRAException e) {
				System.out.println("e = " + e);
				errorMessage = e.getMessage();
			}
		} else {
			validServer = true;
		}

		return issueTypes;
	}

	public List<JIRAConstant> getSavedFilters() {
		validServer = false;
		if (savedFilters == null) {
			errorMessage = null;
			try {
				savedFilters = jiraServerFacade.getSavedFilters(server);
				validServer = true;
			} catch (JIRAException e) {
				errorMessage = e.getMessage();
			}
		} else {
			validServer = true;
		}

		return savedFilters;
	}

	public List<JIRAConstant> getPriorieties() {
		validServer = false;
		if (priorieties == null) {
			errorMessage = null;
			try {
				priorieties = jiraServerFacade.getPriorieties(server);
				validServer = true;
			} catch (JIRAException e) {
				errorMessage = e.getCause().getMessage();
			}
		} else {
			validServer = true;
		}

		return priorieties;
	}

	public List<JIRAConstant> getResolutions() {
		validServer = false;
		if (resolutions == null) {
			errorMessage = null;
			try {
				resolutions = jiraServerFacade.getResolutions(server);
				validServer = true;
			} catch (JIRAException e) {
				errorMessage = e.getCause().getMessage();
			}
		} else {
			validServer = true;
		}

		return resolutions;
	}

	public List getVersions() {
		validServer = false;
		if (versions == null) {
			errorMessage = null;
			try {
				if (currentProject != null) {
					versions = jiraServerFacade.getVersions(server, currentProject.getKey());
					lastProject = currentProject;
				} else {
					versions = Collections.EMPTY_LIST;
				}
				validServer = true;
			} catch (JIRAException e) {
				errorMessage = e.getMessage();
			}
		} else {
			validServer = true;
		}

		return versions;
	}

	public List getComponents() {
		validServer = false;
		if (components == null || (currentProject != null && !currentProject.getKey().equals(lastProject.getKey()))) {
			errorMessage = null;
			try {
				if (currentProject != null) {
					components = jiraServerFacade.getComponents(server, currentProject.getKey());
					lastProject = currentProject;
				} else {
					components = Collections.EMPTY_LIST;
				}
				validServer = true;
			} catch (JIRAException e) {
				errorMessage = e.getMessage();
			}
		} else {
			validServer = true;
		}

		return components;
	}

	public boolean isValidServer() {
		return validServer;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public JIRAProject getCurrentProject() {
		return currentProject;
	}

	public void setCurrentProject(JIRAProject currentProject) {
		this.currentProject = currentProject;
		versions = null;
		components = null;
		issueTypes = null;
	}
}
