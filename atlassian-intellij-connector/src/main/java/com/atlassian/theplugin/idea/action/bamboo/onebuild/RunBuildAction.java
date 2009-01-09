package com.atlassian.theplugin.idea.action.bamboo.onebuild;

import com.intellij.openapi.actionSystem.AnActionEvent;


public class RunBuildAction extends AbstractBuildDetailsAction {

	@Override
	public void actionPerformed(AnActionEvent event) {
		runBuild(event);
	}
}
