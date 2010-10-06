/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.commons.util.StringUtil;
import com.atlassian.theplugin.idea.bamboo.BambooToolWindowPanel;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.WindowManager;
import org.veryquick.embweb.HttpRequestHandler;
import org.veryquick.embweb.Response;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jacek Jaroczynski
 */
class IdeHttpServerHandler implements HttpRequestHandler {

	private final Map<String, AbstractDirectClickThroughHandler> registeredHandlers
			= new HashMap<String, AbstractDirectClickThroughHandler>() { {
		put("issue", new OpenIssueHandler());
		put("build", new OpenBuildHandler());
		put("stacktraceEntry", new OpenStackTraceEntryHandler());
		put("stacktrace", new OpenStackTraceHandler());
		put("icon", new AbstractDirectClickThroughHandler() {
			@Override
			public void handle(Map<String, String> parameters) {
			}
		});
	}};

	private final byte[] icon;

	public IdeHttpServerHandler(final byte[] iconArray) {
		this.icon = iconArray;
	}

	public Response handleRequest(final Type type, final String url, final Map<String, String> parameters) {

		final String method = StringUtil.removeLeadingAndTrailingSlashes(url);
		final Response response = new Response();

		if ("supportsCapabilities".equals(method)) {
			if (registeredHandlers.keySet().containsAll(parameters.keySet())) {
				writeIcon(response);
			} else {
				response.setNoContent();
			}
			return response;
		}

		final AbstractDirectClickThroughHandler clickThroughHandler = registeredHandlers.get(method);
		if (clickThroughHandler != null) {
			writeIcon(response);
			ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
				public void run() {
					clickThroughHandler.handle(parameters);
				}
			});
		} else {
			response.setNoContent();
			PluginUtil.getLogger().warn("Unknown command received: [" + method + "]");
		}
		return response;
	}


	private void writeIcon(final Response response) {
		response.setContentType("image/png");
		response.setBinaryContent(icon);
		response.setOk();
	}

	public static void bringIdeaToFront(final Project project) {
		WindowManager.getInstance().getFrame(project).setVisible(true);

		String osName = System.getProperty("os.name");
		osName = osName.toLowerCase();

		if (osName.contains("windows") || osName.contains("mac os x")) {
			WindowManager.getInstance().getFrame(project).setAlwaysOnTop(true);
			WindowManager.getInstance().getFrame(project).setAlwaysOnTop(false);

		} else { //for linux
			WindowManager.getInstance().getFrame(project).toFront();
		}

		// how to set focus???
		WindowManager.getInstance().getFrame(project).setFocusable(true);
		WindowManager.getInstance().getFrame(project).setFocusableWindowState(true);
		WindowManager.getInstance().getFrame(project).requestFocus();
		WindowManager.getInstance().getFrame(project).requestFocusInWindow();
	}

	private static final class OpenBuildHandler extends AbstractDirectClickThroughHandler {

		@Override
		public void handle(final Map<String, String> parameters) {
			final String buildKey = parameters.get("build_key");
			String buildNumber = parameters.get("build_number");
			final String serverUrl = parameters.get("server_url");

			final String testPackage = parameters.get("test_package");
			final String testClass = parameters.get("test_class");
			final String testMethod = parameters.get("test_method");

			int buildNumberInt = 0;

			try {
				buildNumberInt = Integer.valueOf(buildNumber);
			} catch (NumberFormatException e) {
				buildNumber = null;
			}

			final int buildNumberIntFinal = buildNumberInt;

			if (isDefined(buildKey) && isDefined(serverUrl) && isDefined(buildNumber)) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						// try to open received build in all open projects
						for (Project project : ProjectManager.getInstance().getOpenProjects()) {

							final BambooToolWindowPanel panel = IdeaHelper.getBambooToolWindowPanel(project);
							if (panel != null) {
								bringIdeaToFront(project);
								if (isDefined(testPackage) && isDefined(testClass) && isDefined(testMethod)) {
									panel.openBuildAndRunTest(buildKey, buildNumberIntFinal, serverUrl, testPackage, testClass,
											testMethod);
								} else if (!isDefined(testPackage) && !isDefined(testClass) && !isDefined(testMethod)) {
									panel.openBuild(buildKey, buildNumberIntFinal, serverUrl);
								} else {
									reportProblem("Cannot run test. At least one of the params is not provided: "
											+ "test_package, test_class, test_method");
								}
							} else {
								PluginUtil.getLogger().warn(
										"com.atlassian.theplugin.idea.bamboo.BambooToolWindowPanel is null");
							}
						}
					}
				});
			} else {
				reportProblem("Cannot open build. Incorrect call. "
						+ "At least one of the params is not provided: build_key, build_number, server_url");
			}
		}
	}

	private static class OpenIssueHandler extends AbstractDirectClickThroughHandler {

		@Override
		public void handle(final Map<String, String> parameters) {
			final String issueKey = parameters.get("issue_key");
			final String serverUrl = parameters.get("server_url");
			if (isDefined(issueKey) && isDefined(serverUrl)) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						// try to open received issueKey in all open projects
						for (Project project : ProjectManager.getInstance().getOpenProjects()) {

							final IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(project);
							if (panel != null) {
								bringIdeaToFront(project);
								panel.openIssue(issueKey, serverUrl);
							} else {
								reportProblem("com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel is null");
							}
						}
					}
				});
			} else {
				reportProblem("Cannot open issue: issue_key or server_url parameter is null");
			}
		}
	}

}
