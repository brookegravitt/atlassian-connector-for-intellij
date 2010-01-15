package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;

import java.awt.*;

/**
* @author Wojciech Seliga
*/
abstract class DirectClickThroughRequest implements Runnable {

	protected static boolean isDefined(final String param) {
		return param != null && param.length() > 0;
	}

	protected void reportProblem(final String problem) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				PluginUtil.getLogger().warn(problem);

				// try to show message box in the first open IDEA window
				if (ProjectManager.getInstance().getOpenProjects().length > 0) {
					Project project = ProjectManager.getInstance().getOpenProjects()[0];
					IdeHttpServerHandler.bringIdeaToFront(project);
					Messages.showInfoMessage(project, problem, PluginUtil.PRODUCT_NAME);
				} else {
					Messages.showInfoMessage(problem, PluginUtil.PRODUCT_NAME);
				}
			}
		});
	}
}
