package com.atlassian.theplugin.commons.jira.cache;

import com.atlassian.connector.commons.jira.JIRAServerFacade2Impl;
import com.atlassian.connector.commons.jira.beans.JIRAComponentBean;
import com.atlassian.connector.commons.jira.beans.JIRAConstant;
import com.atlassian.connector.commons.jira.beans.JIRAFixForVersionBean;
import com.atlassian.connector.commons.jira.beans.JIRAPriorityBean;
import com.atlassian.connector.commons.jira.beans.JIRAProject;
import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;
import com.atlassian.connector.commons.jira.beans.JIRAResolutionBean;
import com.atlassian.connector.commons.jira.beans.JIRASecurityLevelBean;
import com.atlassian.connector.commons.jira.beans.JIRAVersionBean;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.jira.IntelliJJiraServerFacade;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.JiraServerFacade;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.Logger;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * User: jgorycki
 * Date: Nov 18, 2008
 * Time: 9:01:03 PM
 */
public abstract class JIRAServerModelImpl implements JIRAServerModel {
	private JiraServerFacade facade;

	private final Map<JiraServerData, JIRAServerCache> serverInfoMap = new HashMap<JiraServerData, JIRAServerCache>();
	private boolean changed = false;

    private Set<JiraServerData> nonResponsiveServers = new HashSet<JiraServerData>();

    public JIRAServerModelImpl(Logger logger) {
		facade = IntelliJJiraServerFacade.getInstance();
        JIRAServerFacade2Impl.setLogger(logger);
	}

	// for unit testing
	public void setFacade(JiraServerFacade newFacade) {
		facade = newFacade;
	}

	private synchronized JIRAServerCache getServer(JiraServerData jiraServerData) {
		if (serverInfoMap.containsKey(jiraServerData)) {
			return serverInfoMap.get(jiraServerData);
		}

		JIRAServerCache srv = null;

		if (!changed) {
			srv = new JIRAServerCache(jiraServerData, facade);
			serverInfoMap.put(jiraServerData, srv);
		}
		return srv;
	}

	public synchronized void clear(JiraServerData cfg) {
		if (cfg == null) {
			return;
		}

		serverInfoMap.remove(cfg);

		if (isFrozen()) {
			changed = true;
		}
	}

	public synchronized void clear(ServerId serverId) {
		if (serverId == null) {
			return;
		}

		JiraServerData server = null;

		for (JiraServerData s : serverInfoMap.keySet()) {
			if (s.getServerId().equals(serverId)) {
				server = s;
				break;
			}
		}

		serverInfoMap.remove(server);

        nonResponsiveServers.remove(server);

		if (isFrozen()) {
			changed = true;
		}
	}

    @Override
    public void resetFacade() {
        facade.reset();
    }

    public synchronized void replace(JiraServerData newJiraServerData) {
		if (newJiraServerData == null) {
			return;
		}

		JiraServerData oldServer = null;

		for (JiraServerData s : serverInfoMap.keySet()) {
			if (s.getServerId().equals(newJiraServerData.getServerId())) {
				oldServer = s;
				break;
			}
		}

		if (oldServer != null) {
			serverInfoMap.put(newJiraServerData, serverInfoMap.get(oldServer));
			serverInfoMap.remove(oldServer);
            nonResponsiveServers.remove(oldServer);
        }

		if (isFrozen()) {
			changed = true;
		}
	}

    public boolean isChanged() {
        return changed;
    }


    public synchronized void clearAll() {
		serverInfoMap.clear();
        nonResponsiveServers.clear();
	}

	public Boolean checkServer(JiraServerData jiraServerData) throws RemoteApiException {
		if (jiraServerData == null) {
			return null;
		}

		JIRAServerCache srv = getServer(jiraServerData);
		if (srv != null) {
            boolean works = false;
            nonResponsiveServers.remove(jiraServerData);
            try {
                works = srv.checkServer();
                if (!works) {
                    nonResponsiveServers.add(jiraServerData);
                }
            } catch (RemoteApiException e) {
                nonResponsiveServers.add(jiraServerData);
                throw e;
            }
            return works;
		}

		return null;
	}

    public boolean isServerResponding(JiraServerData jiraServerData) {
        return !nonResponsiveServers.contains(jiraServerData);
    }

    public String getErrorMessage(JiraServerData jiraServerData) {
		if (jiraServerData == null) {
			return "No Server configuration";
		}
		JIRAServerCache srv = getServer(jiraServerData);
		if (srv != null) {
			return srv.getErrorMessage();
		}

		return "";
	}


	public List<JIRAProject> getProjects(JiraServerData jiraServerData) throws JIRAException {
		if (jiraServerData == null) {
			return null;
		}
		JIRAServerCache srv = getServer(jiraServerData);
		return (srv == null) ? null : srv.getProjects(nonResponsiveServers.contains(jiraServerData));
	}

	public List<JIRAConstant> getStatuses(JiraServerData jiraServerData) throws JIRAException {
		if (jiraServerData == null) {
			return null;
		}
		JIRAServerCache srv = getServer(jiraServerData);
		return (srv == null) ? null : srv.getStatuses();
	}

	public List<JIRAConstant> getIssueTypes(JiraServerData jiraServerData, JIRAProject project, boolean includeAny)
			throws JIRAException {
		if (jiraServerData == null) {
			return null;
		}
		JIRAServerCache srv = getServer(jiraServerData);
		return (srv == null) ? null : srv.getIssueTypes(project, includeAny);
	}

	public List<JIRAConstant> getSubtaskIssueTypes(JiraServerData jiraServerData, JIRAProject project) throws JIRAException {
		if (jiraServerData == null) {
			return null;
		}
		JIRAServerCache srv = getServer(jiraServerData);
		return (srv == null) ? null : srv.getSubtaskIssueTypes(project);
	}

	public List<JIRAQueryFragment> getSavedFilters(JiraServerData jiraServerData) throws JIRAException {
		if (jiraServerData == null) {
			return null;
		}
		JIRAServerCache srv = getServer(jiraServerData);
		return (srv == null) ? null : srv.getSavedFilters();
	}

	public List<JIRAPriorityBean> getPriorities(JiraServerData jiraServerData, boolean includeAny) throws JIRAException {
		if (jiraServerData == null) {
			return null;
		}
		JIRAServerCache srv = getServer(jiraServerData);
		return (srv == null) ? null : srv.getPriorities(includeAny);
	}

	public List<JIRAResolutionBean> getResolutions(JiraServerData jiraServerData, boolean includeAnyAndUnknown)
            throws JIRAException {
		if (jiraServerData == null) {
			return null;
		}
		JIRAServerCache srv = getServer(jiraServerData);
		return (srv == null) ? null : srv.getResolutions(includeAnyAndUnknown);
	}

	public List<JIRAVersionBean> getVersions(JiraServerData jiraServerData, JIRAProject project, boolean includeSpecialValues)
			throws JIRAException {
		if (jiraServerData == null) {
			return null;
		}
		JIRAServerCache srv = getServer(jiraServerData);
		return (srv == null) ? null : srv.getVersions(project, includeSpecialValues);
	}

	public List<JIRAFixForVersionBean> getFixForVersions(JiraServerData jiraServerData, JIRAProject project,
			boolean includeSpecialValues) throws JIRAException {
		if (jiraServerData == null) {
			return null;
		}
		JIRAServerCache srv = getServer(jiraServerData);
		return (srv == null) ? null : srv.getFixForVersions(project, includeSpecialValues);
	}

	public List<JIRAComponentBean> getComponents(JiraServerData jiraServerData, JIRAProject project,
                                                 final boolean includeSpecialValues)
			throws JIRAException {
		if (jiraServerData == null) {
			return null;
		}
		JIRAServerCache srv = getServer(jiraServerData);
		return (srv == null) ? null : srv.getComponents(project, includeSpecialValues);
	}

	public Collection<JiraServerData> getServers() {
		return serverInfoMap.keySet();
	}


    public List<Pair<String, String>> getUsers(JiraServerData jiraServerData) {
		if (jiraServerData == null) {
			return null;
		}
		JIRAServerCache srv = getServer(jiraServerData);
		return (srv == null) ? null : srv.getUsers();
    }

    public void addUser(JiraServerData jiraServerData, String userId, String userName) {
		if (jiraServerData == null) {
			return;
		}
		JIRAServerCache srv = getServer(jiraServerData);
		if (srv != null) {
            srv.addUser(userId, userName);
        }
    }

    @Nullable
    public List<JIRASecurityLevelBean> getSecurityLevels(JiraServerData jiraServerData, String projectKey) 
            throws RemoteApiException, JIRAException {
        if (jiraServerData == null) {
            return null;
        }
        JIRAServerCache srv = getServer(jiraServerData);
        if (srv != null) {
            return srv.getSecurityLevels(projectKey);
        }

        return null;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public abstract boolean isFrozen();
}

