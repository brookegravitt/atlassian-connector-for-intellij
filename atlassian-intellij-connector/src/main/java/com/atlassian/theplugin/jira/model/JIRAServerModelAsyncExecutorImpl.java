package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.jira.api.*;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: jgorycki
 * Date: Nov 19, 2008
 * Time: 3:58:05 PM
 */
public final class JIRAServerModelAsyncExecutorImpl implements JIRAServerModelAsyncExecutor {
	private final JIRAServerModel model;
	private final JiraServerCfg config;
	private final Project project;

	private JIRAServerModelAsyncExecutorImpl(Project project, JIRAServerModel model, JiraServerCfg config) {
		if (project == null || model == null || config == null) {
			throw new IllegalArgumentException();
		}
		this.project = project;
		this.model = model;
		this.config = config;
	}

	public static JIRAServerModelAsyncExecutor createInstance(Project p, JIRAServerModel m, JiraServerCfg cfg) {
		return new JIRAServerModelAsyncExecutorImpl(p, m, cfg);
	}

	public String getErrorMessage() {
		return model.getErrorMessage(config);
	}

	public void checkServer(final JIRAServerModelAsyncExecutorListener<Boolean> listener) {
		execute(new ModelExecutor<Boolean>() {
			public String getTitle() { return "Checking JIRA server"; }
			public List<Boolean> execute(JIRAProject p) {
				boolean result = model.checkServer(config);
				final List<Boolean> list = new ArrayList<Boolean>();
				list.add(result);
				return list;
			}
		}, null, listener);
	}

	public void getProjects(final JIRAServerModelAsyncExecutorListener<JIRAProject> listener) {
		execute(new ModelExecutor<JIRAProject>() {
			public String getTitle() { return "Getting projects"; }
			public List<JIRAProject> execute(JIRAProject p) { return model.getProjects(config);	}
		}, null, listener);
	}

	public void getStatuses(final JIRAServerModelAsyncExecutorListener<JIRAConstant> listener) {
		execute(new ModelExecutor<JIRAConstant>() {
			public String getTitle() { return "Getting statuses"; }
			public List<JIRAConstant> execute(JIRAProject p) { return model.getStatuses(config); }
		}, null, listener);
	}

	public void getIssueTypes(final JIRAProject p,
							  final JIRAServerModelAsyncExecutorListener<JIRAConstant> listener) {
		execute(new ModelExecutor<JIRAConstant>() {
			public String getTitle() { return "Getting issue types"; }
			public List<JIRAConstant> execute(JIRAProject p) { return model.getIssueTypes(config, p); }
		}, p, listener);
	}

	public void getSavedFilters(final JIRAServerModelAsyncExecutorListener<JIRAQueryFragment> listener) {
		execute(new ModelExecutor<JIRAQueryFragment>() {
			public String getTitle() { return "Getting saved filters"; }
			public List<JIRAQueryFragment> execute(JIRAProject p) { return model.getSavedFilters(config); }
		}, null, listener);
	}

	public void getPriorities(final JIRAServerModelAsyncExecutorListener<JIRAConstant> listener) {
		execute(new ModelExecutor<JIRAConstant>() {
			public String getTitle() { return "Getting priorities"; }
			public List<JIRAConstant> execute(JIRAProject p) { return model.getPriorities(config); }
		}, null, listener);
	}

	public void getResolutions(final JIRAServerModelAsyncExecutorListener<JIRAResolutionBean> listener) {
		execute(new ModelExecutor<JIRAResolutionBean>() {
			public String getTitle() { return "Getting resolutions"; }
			public List<JIRAResolutionBean> execute(JIRAProject p) { return model.getResolutions(config); }
		}, null, listener);
	}

	public void getVersions(final JIRAProject p,
							final JIRAServerModelAsyncExecutorListener<JIRAVersionBean> listener) {
		execute(new ModelExecutor<JIRAVersionBean>() {
			public String getTitle() { return "Getting versions"; }
			public List<JIRAVersionBean> execute(JIRAProject p) { return model.getVersions(config, p); }
		}, p, listener);
	}

	public void getFixForVersions(final JIRAProject p,
								  final JIRAServerModelAsyncExecutorListener<JIRAFixForVersionBean> listener) {
		execute(new ModelExecutor<JIRAFixForVersionBean>() {
			public String getTitle() { return "Getting \"Fix for\" versions"; }
			public List<JIRAFixForVersionBean> execute(JIRAProject p) { return model.getFixForVersions(config, p); }
		}, p, listener);
	}

	public void getComponents(final JIRAProject p,
							  final JIRAServerModelAsyncExecutorListener<JIRAComponentBean> listener) {
		execute(new ModelExecutor<JIRAComponentBean>() {
			public String getTitle() { return "Getting components"; }
			public List<JIRAComponentBean> execute(JIRAProject p) { return model.getComponents(config, p); }
		}, p, listener);
	}

	private interface ModelExecutor<T> {
		String getTitle();
		List<T> execute(JIRAProject p);
	}

	private <T> void notifyListener(final JIRAServerModelAsyncExecutorListener<T> listener, final List<T> result) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				listener.finished(result);
			}
		});
	}

	private <T> void execute(final ModelExecutor<T> modelExecutor, final JIRAProject p,
								final JIRAServerModelAsyncExecutorListener<T> listener) {
		StringBuilder title = new StringBuilder(modelExecutor.getTitle());
		if (p != null) {
			title.append(" for project ").append(p.getName());
		}
		Thread t = new Thread(new Runnable() {
			public void run() {
				notifyListener(listener, modelExecutor.execute(p));
			}
		});
		t.start();
	}
}
