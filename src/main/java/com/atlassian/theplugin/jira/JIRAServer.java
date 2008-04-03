package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.jira.api.*;

import java.util.*;

public class JIRAServer {
	private Server server;
	private boolean validServer = false;
	private String errorMessage = null;

	private List<JIRAProject> projects;
	private List statuses;
	private List issueTypes;
	private List savedFilters;
	private List<JIRAVersionBean> versions;
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
				List<JIRAProject> retrieved = jiraServerFacade.getProjects(server);
				projects = new ArrayList<JIRAProject>();
				projects.add(new JIRAProjectBean(-1, "Any"));
				projects.addAll(retrieved);
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
				List<JIRAConstant> retrieved = jiraServerFacade.getStatuses(server);
				statuses = new ArrayList<JIRAConstant>(retrieved.size() + 1);
				statuses.add(new JIRAStatusBean(-1, "Any", null));
				statuses.addAll(retrieved);
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
				List<JIRAConstant> retrieved = Collections.EMPTY_LIST;
				if (currentProject == null) {
					retrieved = jiraServerFacade.getIssueTypes(server);
				} else {
					retrieved = jiraServerFacade.getIssueTypesForProject(server, Long.toString(currentProject.getId()));
				}
				issueTypes = new ArrayList<JIRAConstant>(retrieved.size() + 1);
				issueTypes.add(new JIRAIssueTypeBean(-1, "Any", null));
				issueTypes.addAll(retrieved);

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
				List<JIRAConstant> retrieved = jiraServerFacade.getPriorieties(server);
				priorieties = new ArrayList(retrieved.size() + 1);
				priorieties.add(new JIRAPriorityBean(-1, "Any", null));
				priorieties.addAll(retrieved);
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
				List<JIRAQueryFragment> retrieved = jiraServerFacade.getResolutions(server);
				resolutions = new ArrayList<JIRAResolutionBean>(retrieved.size() + 1);
				resolutions.add(new JIRAResolutionBean(-1, "Any"));
				resolutions.addAll(retrieved);
				validServer = true;
			} catch (JIRAException e) {
				errorMessage = e.getCause().getMessage();
			}
		} else {
			validServer = true;
		}

		return resolutions;
	}

	public List<JIRAVersionBean> getVersions() {
		validServer = false;
		if (versions == null) {
			errorMessage = null;
			try {
				if (currentProject != null) {
					List<JIRAQueryFragment> retrieved = jiraServerFacade.getVersions(server, currentProject.getKey());
					versions = new ArrayList<JIRAVersionBean>(retrieved.size() + 4);
					versions.add(new JIRAVersionBean(-1, "Any"));
					versions.add(new JIRAVersionBean(-2, "No version"));
					versions.add(new JIRAVersionBean(-3, "Released versions"));
					for (JIRAQueryFragment jiraQueryFragment : retrieved) {
						if (((JIRAVersionBean) jiraQueryFragment).isReleased()) {
							versions.add((JIRAVersionBean) jiraQueryFragment);
						}
					}
					versions.add(new JIRAVersionBean(-4, "Unreleased versions"));
					for (JIRAQueryFragment jiraQueryFragment : retrieved) {
						if (!((JIRAVersionBean) jiraQueryFragment).isReleased()) {
							versions.add((JIRAVersionBean) jiraQueryFragment);
						}
					}
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

	public List<JIRAQueryFragment> getComponents() {
		validServer = false;
		if (components == null || (currentProject != null && !currentProject.getKey().equals(lastProject.getKey()))) {
			errorMessage = null;
			try {
				if (currentProject != null) {
					List<JIRAQueryFragment> retrieved = jiraServerFacade.getComponents(server, currentProject.getKey());
					components = new ArrayList<JIRAVersionBean>(retrieved.size() + 1);
					components.add(new JIRAComponentBean(-1, "Any"));
					components.addAll(retrieved);
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
