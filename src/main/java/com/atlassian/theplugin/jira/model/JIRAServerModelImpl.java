package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.*;

import java.util.*;

/**
 * User: jgorycki
 * Date: Nov 18, 2008
 * Time: 9:01:03 PM
 */
public class JIRAServerModelImpl implements JIRAServerModel {
	private JIRAServerFacade facade;

	private final Map<JiraServerCfg, JIRAServerCache> serverInfoMap = new HashMap<JiraServerCfg, JIRAServerCache>();
	private boolean modelFrozen = false;
	private Collection<FrozenModelListener> frozenListeners = new ArrayList<FrozenModelListener>();

	public JIRAServerModelImpl() {
		facade = JIRAServerFacadeImpl.getInstance();
	}

	// for unit testing
	public void setFacade(JIRAServerFacade newFacade) {
		facade = newFacade;
	}

	private synchronized JIRAServerCache getServer(JiraServerCfg cfg) {
		if (serverInfoMap.containsKey(cfg)) {
			return serverInfoMap.get(cfg);
		}
		JIRAServerCache srv = new JIRAServerCache(cfg, facade);
		serverInfoMap.put(cfg, srv);
		return srv;
	}

	public synchronized void clear(JiraServerCfg cfg) {
		if (cfg == null) {
			return;
		}

		serverInfoMap.remove(cfg);
	}

	public synchronized void clear(ServerId serverId) {
		if (serverId == null) {
			return;
		}

		JiraServerCfg server = null;

		for (JiraServerCfg s : serverInfoMap.keySet()) {
			if (s.getServerId().equals(serverId)) {
				server = s;
				break;
			}
		}

		serverInfoMap.remove(server);
	}

	public synchronized void clearAll() {
		serverInfoMap.clear();
	}

	public boolean checkServer(JiraServerCfg cfg) throws RemoteApiException {
		if (cfg == null) {
			return false;
		}
		JIRAServerCache srv = getServer(cfg);
		return srv.checkServer();
	}

	public String getErrorMessage(JiraServerCfg cfg) {
		if (cfg == null) {
			return "No Server configuration";
		}
		JIRAServerCache srv = getServer(cfg);
		return srv.getErrorMessage();
	}


	public List<JIRAProject> getProjects(JiraServerCfg cfg) {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return srv.getProjects();
	}

	public List<JIRAConstant> getStatuses(JiraServerCfg cfg) {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return srv.getStatuses();
	}

	public List<JIRAConstant> getIssueTypes(JiraServerCfg cfg, JIRAProject project) {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return srv.getIssueTypes(project);
	}

	public List<JIRAQueryFragment> getSavedFilters(JiraServerCfg cfg) {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return srv.getSavedFilters();
	}

	public List<JIRAConstant> getPriorities(JiraServerCfg cfg) {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return srv.getPriorities();
	}

	public List<JIRAResolutionBean> getResolutions(JiraServerCfg cfg) {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return srv.getResolutions();
	}

	public List<JIRAVersionBean> getVersions(JiraServerCfg cfg, JIRAProject project) {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return srv.getVersions(project);
	}

	public List<JIRAFixForVersionBean> getFixForVersions(JiraServerCfg cfg, JIRAProject project) {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return srv.getFixForVersions(project);
	}

	public List<JIRAComponentBean> getComponents(JiraServerCfg cfg, JIRAProject project) {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return srv.getComponents(project);
	}

	public Collection<JiraServerCfg> getServers() {
		return serverInfoMap.keySet();
	}

	public boolean isModelFrozen() {
		return this.modelFrozen;
	}

	public void setModelFrozen(boolean frozen) {
		this.modelFrozen = frozen;
		fireModelFrozen();
	}

	public void addFrozenModelListener(FrozenModelListener listener) {
		frozenListeners.add(listener);
	}

	public void removeFrozenModelListener(FrozenModelListener listener) {
		frozenListeners.remove(listener);
	}

	private void fireModelFrozen() {
		for (FrozenModelListener listener : frozenListeners) {
			listener.modelFrozen(this, modelFrozen);
		}
	}
}

