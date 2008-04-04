package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.jira.api.*;

import java.util.*;

public class JIRAServer {
	public final static int LIST_SPECIAL_VALUES_COUNT = 1;
	public final static int VERSION_SPECIAL_VALUES_COUNT = LIST_SPECIAL_VALUES_COUNT + 3;	
	private static final int ANY_ID = -1;
	private static final int NO_VERSION_ID = -2;
	private static final int RELEASED_VERSION_ID = -3;
	private static final int UNRELEASED_VERSION_ID = -4;

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
				projects.add(new JIRAProjectBean(ANY_ID, "Any"));
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
				statuses.add(new JIRAStatusBean(ANY_ID, "Any", null));
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
				issueTypes.add(new JIRAIssueTypeBean(ANY_ID, "Any", null));
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
				priorieties.add(new JIRAPriorityBean(ANY_ID, "Any", null));
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
				resolutions.add(new JIRAResolutionBean(ANY_ID, "Any"));
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
					versions = new ArrayList<JIRAVersionBean>(retrieved.size() + VERSION_SPECIAL_VALUES_COUNT);
					versions.add(new JIRAVersionBean(ANY_ID, "Any"));
					versions.add(new JIRAVersionBean(NO_VERSION_ID, "No version"));
					versions.add(new JIRAVersionBean(RELEASED_VERSION_ID, "Released versions"));
					for (JIRAQueryFragment jiraQueryFragment : retrieved) {
						if (((JIRAVersionBean) jiraQueryFragment).isReleased()) {
							versions.add((JIRAVersionBean) jiraQueryFragment);
						}
					}
					versions.add(new JIRAVersionBean(UNRELEASED_VERSION_ID, "Unreleased versions"));
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
					components.add(new JIRAComponentBean(ANY_ID, "Any"));
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
