package com.atlassian.theplugin.idea;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

import java.util.Map;

/**
 * @author Wojciech Seliga
 */
public class OpenStackTraceHandler extends AbstractDirectClickThroughHandler {

	public void handle(final Map<String, String> parameters) {
		final String stacktrace = parameters.get("stacktrace");
		final String issueKey = parameters.get("issueKey");
		if (stacktrace == null) {
			reportProblem("Cannot open stacktrace. Incorrect call with params [" + parameters + "]");
			return;
		}

		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				if (ProjectManager.getInstance().getOpenProjects().length > 0) {
					Project project = ProjectManager.getInstance().getOpenProjects()[0];
                    IdeHttpWebServer.bringIdeaToFront(project);
					final String title = issueKey != null ? ("Stacktrace from " + issueKey) : "Stacktrace";
					IdeaVersionFacade.getInstance().openStackTrace(project, stacktrace, title);
				} else {
					reportProblem("Cannot find any open project while opening stacktrace");
				}
			}

		});

	}



}


