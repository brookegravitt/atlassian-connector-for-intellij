package com.atlassian.theplugin.idea;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.unscramble.AnalyzeStacktraceUtil;

import java.util.Map;

/**
 * @author Wojciech Seliga
 */
public class OpenStackTraceHandler extends DirectClickThroughRequest {


	private final Map<String, String> parameters;

	public OpenStackTraceHandler(final Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public void run() {
		final String stacktrace = parameters.get("stacktrace");
		final String issueKey = parameters.get("issueKey");
//		final String backTrackUrl = parameters.get("backtrackUrl");
		if (stacktrace == null) {
			reportProblem("Cannot open stacktrace. Incorrect call with params [" + parameters + "]");
			return;
		}

		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				if (ProjectManager.getInstance().getOpenProjects().length > 0) {
					Project project = ProjectManager.getInstance().getOpenProjects()[0];
					IdeHttpServerHandler.bringIdeaToFront(project);
					final ConsoleView consoleView = AnalyzeStacktraceUtil.addConsole(project, null,
							issueKey != null ? ("Stacktrace from " + issueKey) : "Stacktrace");
//					String prefix = backTrackUrl != null ? (backTrackUrl + "\n") : "";
					AnalyzeStacktraceUtil.printStacktrace(consoleView, stacktrace);
				} else {
					reportProblem("Cannot find any open project while opening stacktrace");
				}
			}

		});

	}
}

