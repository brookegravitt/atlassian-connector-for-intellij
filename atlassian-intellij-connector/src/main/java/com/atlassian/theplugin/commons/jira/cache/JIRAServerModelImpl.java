package com.atlassian.theplugin.commons.jira.cache;

import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.jira.JIRAServerFacade;
import com.atlassian.theplugin.commons.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.*;
import com.atlassian.theplugin.commons.jira.api.rss.JIRAException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: jgorycki
 * Date: Nov 18, 2008
 * Time: 9:01:03 PM
 */
public abstract class JIRAServerModelImpl implements JIRAServerModel {
	private JIRAServerFacade facade;

	private final Map<JiraServerData, JIRAServerCache> serverInfoMap = new HashMap<JiraServerData, JIRAServerCache>();
	private boolean changed = false;

	public JIRAServerModelImpl(Logger logger) {
		facade = JIRAServerFacadeImpl.getInstance();
        JIRAServerFacadeImpl.setLogger(logger);
	}

	// for unit testing
	public void setFacade(JIRAServerFacade newFacade) {
		facade = newFacade;
	}

	private synchronized JIRAServerCache getServer(JiraServerData cfg) {
		if (serverInfoMap.containsKey(cfg)) {
			return serverInfoMap.get(cfg);
		}

		JIRAServerCache srv = null;

		if (!changed) {
			srv = new JIRAServerCache(cfg, facade);
			serverInfoMap.put(cfg, srv);
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

		ServerData server = null;

		for (ServerData s : serverInfoMap.keySet()) {
			if (s.getServerId().equals(serverId)) {
				server = s;
				break;
			}
		}

		serverInfoMap.remove(server);

		if (isFrozen()) {
			changed = true;
		}
	}

	public synchronized void replace(JiraServerData newServer) {
		if (newServer == null) {
			return;
		}

		ServerData oldServer = null;

		for (ServerData s : serverInfoMap.keySet()) {
			if (s.getServerId().equals(newServer.getServerId())) {
				oldServer = s;
				break;
			}
		}

		if (oldServer != null) {
			serverInfoMap.put(newServer, serverInfoMap.get(oldServer));
			serverInfoMap.remove(oldServer);
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
	}

	public Boolean checkServer(JiraServerData cfg) throws RemoteApiException {
		if (cfg == null) {
			return null;
		}

		JIRAServerCache srv = getServer(cfg);
		if (srv != null) {
			return srv.checkServer();
		}

		return null;
	}

	public String getErrorMessage(JiraServerData cfg) {
		if (cfg == null) {
			return "No Server configuration";
		}
		JIRAServerCache srv = getServer(cfg);
		if (srv != null) {
			return srv.getErrorMessage();
		}

		return "";
	}


	public List<JIRAProject> getProjects(JiraServerData cfg) throws JIRAException {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return (srv == null) ? null : srv.getProjects();
	}

	public List<JIRAConstant> getStatuses(JiraServerData cfg) throws JIRAException {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return (srv == null) ? null : srv.getStatuses();
	}

	public List<JIRAConstant> getIssueTypes(JiraServerData cfg, JIRAProject project, boolean includeAny)
			throws JIRAException {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return (srv == null) ? null : srv.getIssueTypes(project, includeAny);
	}

	public List<JIRAConstant> getSubtaskIssueTypes(JiraServerData cfg, JIRAProject project) throws JIRAException {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return (srv == null) ? null : srv.getSubtaskIssueTypes(project);
	}

	public List<JIRAQueryFragment> getSavedFilters(JiraServerData cfg) throws JIRAException {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return (srv == null) ? null : srv.getSavedFilters();
	}

	public List<JIRAPriorityBean> getPriorities(JiraServerData cfg, boolean includeAny) throws JIRAException {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return (srv == null) ? null : srv.getPriorities(includeAny);
	}

	public List<JIRAResolutionBean> getResolutions(JiraServerData cfg, boolean includeAnyAndUnknown)
            throws JIRAException {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return (srv == null) ? null : srv.getResolutions(includeAnyAndUnknown);
	}

	public List<JIRAVersionBean> getVersions(JiraServerData cfg, JIRAProject project, boolean includeSpecialValues)
			throws JIRAException {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return (srv == null) ? null : srv.getVersions(project, includeSpecialValues);
	}

	public List<JIRAFixForVersionBean> getFixForVersions(JiraServerData cfg, JIRAProject project,
			boolean includeSpecialValues) throws JIRAException {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return (srv == null) ? null : srv.getFixForVersions(project, includeSpecialValues);
	}

	public List<JIRAComponentBean> getComponents(JiraServerData cfg, JIRAProject project,
                                                 final boolean includeSpecialValues)
			throws JIRAException {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return (srv == null) ? null : srv.getComponents(project, includeSpecialValues);
	}

	public Collection<JiraServerData> getServers() {
		return serverInfoMap.keySet();
	}

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public abstract boolean isFrozen();
}

