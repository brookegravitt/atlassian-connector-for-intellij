package com.atlassian.theplugin.idea;

import com.intellij.execution.ExecutionManager;
import com.intellij.execution.ExecutionRegistry;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.runners.JavaProgramRunner;
import com.intellij.execution.ui.CloseAction;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.unscramble.UnscrambleSupport;

import javax.swing.*;
import java.awt.*;
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
					final ConsoleView consoleView = createConsoleView(project,
							issueKey != null ? ("Stacktrace from " + issueKey) : "Stacktrace");
//					String prefix = backTrackUrl != null ? (backTrackUrl + "\n") : "";
					printStacktrace(consoleView, stacktrace);
				} else {
					reportProblem("Cannot find any open project while opening stacktrace");
				}
			}

		});

	}

	public static void printStacktrace(final ConsoleView consoleView, final String unscrambledTrace) {
		consoleView.clear();
		consoleView.print(unscrambledTrace + "\n", ConsoleViewContentType.ERROR_OUTPUT);
		consoleView.performWhenNoDeferredOutput(
				new Runnable() {
					public void run() {
						consoleView.scrollTo(0);
					}
				}
		);
	}


	private static boolean showUnscrambledText(UnscrambleSupport unscramblesupport, String s, Project project, String s1) {
		String s2 = unscramblesupport != null ? unscramblesupport.unscramble(project, s1, s) : s1;
		if (s2 == null) {
			return false;
		} else {
			final ConsoleView consoleview = createConsoleView(project, "My stacktrace");
			consoleview.print((new StringBuilder()).append(s2).append("\n").toString(), ConsoleViewContentType.ERROR_OUTPUT);
			consoleview.performWhenNoDeferredOutput(new Runnable() {
				public void run() {
					consoleview.scrollTo(0);
				}
			});
			return true;
		}
	}

	private static ConsoleView createConsoleView(Project project, String tabTitle) {
		ConsoleView consoleview = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
		JavaProgramRunner runner = ExecutionRegistry.getInstance().getDefaultRunner();
		DefaultActionGroup toolbarActions = new DefaultActionGroup();
		MyConsolePanel consoleComponent = new MyConsolePanel(consoleview, toolbarActions);
		RunContentDescriptor descriptor = new RunContentDescriptor(consoleview, null, consoleComponent, tabTitle) {
			public boolean isContentReuseProhibited() {
				return true;
			}
		};
		toolbarActions.add(new CloseAction(runner, descriptor, project));
		for (AnAction action : consoleview.createUpDownStacktraceActions()) {
			toolbarActions.add(action);
		}
		ExecutionManager.getInstance(project).getContentManager().showRunContent(runner, descriptor);
		return consoleview;
	}

	private static final class MyConsolePanel extends JPanel {
		public MyConsolePanel(ExecutionConsole consoleView, ActionGroup toolbarActions) {
			super(new BorderLayout());
			JPanel toolbarPanel = new JPanel(new BorderLayout());
			toolbarPanel.add(ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, toolbarActions, false).getComponent());
			add(toolbarPanel, BorderLayout.WEST);
			add(consoleView.getComponent(), BorderLayout.CENTER);
		}
	}


}


