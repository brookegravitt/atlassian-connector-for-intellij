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
	private boolean changed = false;

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

		JIRAServerCache srv = null;

		if (!changed) {
			srv = new JIRAServerCache(cfg, facade);
			serverInfoMap.put(cfg, srv);
		}
		return srv;
	}

	public synchronized void clear(JiraServerCfg cfg) {
		if (cfg == null) {
			return;
		}

		serverInfoMap.remove(cfg);

		if (modelFrozen) {
			changed = true;
		}
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

		if (modelFrozen) {
			changed = true;
		}
	}

	public synchronized void replace(JiraServerCfg newServer) {
		if (newServer == null) {
			return;
		}

		JiraServerCfg oldServer = null;

		for (JiraServerCfg s : serverInfoMap.keySet()) {
			if (s.getServerId().equals(newServer.getServerId())) {
				oldServer = s;
				break;
			}
		}

		if (oldServer != null) {
			serverInfoMap.put(newServer, serverInfoMap.get(oldServer));
			serverInfoMap.remove(oldServer);
		}

		if (modelFrozen) {
			changed = true;
		}
	}


	public synchronized void clearAll() {
		serverInfoMap.clear();
	}

	public Boolean checkServer(JiraServerCfg cfg) throws RemoteApiException {
		if (cfg == null) {
			return null;
		}

		JIRAServerCache srv = getServer(cfg);
		if (srv != null) {
			return srv.checkServer();
		}

		return null;
	}

	public String getErrorMessage(JiraServerCfg cfg) {
		if (cfg == null) {
			return "No Server configuration";
		}
		JIRAServerCache srv = getServer(cfg);
		if (srv != null) {
			return srv.getErrorMessage();
		}

		return "";
	}


	public List<JIRAProject> getProjects(JiraServerCfg cfg) throws JIRAException {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return (srv == null) ? null : srv.getProjects();
	}

	public List<JIRAConstant> getStatuses(JiraServerCfg cfg) throws JIRAException {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return (srv == null) ? null : srv.getStatuses();
	}

	public List<JIRAConstant> getIssueTypes(JiraServerCfg cfg, JIRAProject project, boolean includeAny)
			throws JIRAException {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return (srv == null) ? null : srv.getIssueTypes(project, includeAny);
	}

	public List<JIRAConstant> getSubtaskIssueTypes(JiraServerCfg cfg, JIRAProject project) throws JIRAException {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return (srv == null) ? null : srv.getSubtaskIssueTypes(project);
	}

	public List<JIRAQueryFragment> getSavedFilters(JiraServerCfg cfg) throws JIRAException {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return (srv == null) ? null : srv.getSavedFilters();
	}

	public List<JIRAConstant> getPriorities(JiraServerCfg cfg, boolean includeAny) throws JIRAException {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return (srv == null) ? null : srv.getPriorities(includeAny);
	}

	public List<JIRAResolutionBean> getResolutions(JiraServerCfg cfg, boolean includeAnyAndUnknown) throws JIRAException {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return (srv == null) ? null : srv.getResolutions(includeAnyAndUnknown);
	}

	public List<JIRAVersionBean> getVersions(JiraServerCfg cfg, JIRAProject project, boolean includeSpecialValues)
			throws JIRAException {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return (srv == null) ? null : srv.getVersions(project, includeSpecialValues);
	}

	public List<JIRAFixForVersionBean> getFixForVersions(JiraServerCfg cfg, JIRAProject project,
			boolean includeSpecialValues) throws JIRAException {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return (srv == null) ? null : srv.getFixForVersions(project, includeSpecialValues);
	}

	public List<JIRAComponentBean> getComponents(JiraServerCfg cfg, JIRAProject project, final boolean includeSpecialValues)
			throws JIRAException {
		if (cfg == null) {
			return null;
		}
		JIRAServerCache srv = getServer(cfg);
		return (srv == null) ? null : srv.getComponents(project, includeSpecialValues);
	}

	public Collection<JiraServerCfg> getServers() {
		return serverInfoMap.keySet();
	}

	public boolean isModelFrozen() {
		return this.modelFrozen;
	}

	public synchronized void setModelFrozen(boolean frozen) {
		this.modelFrozen = frozen;
		changed = false;
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

