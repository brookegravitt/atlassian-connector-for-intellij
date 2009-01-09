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
package com.atlassian.theplugin.idea.action.bamboo;

import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;

import java.awt.*;

/**
 * @author Jacek Jaroczynski
 */
public abstract class AbstractBuildAction extends AnAction {

	@Override
	public void update(AnActionEvent event) {
		super.update(event);
		BambooBuildAdapterIdea build = getBuild(event);
		if (build != null && build.getEnabled() && build.getBuildKey() != null && build.getBuildNumber() != null) {
			event.getPresentation().setEnabled(true);
		} else {
			event.getPresentation().setEnabled(false);
		}
	}

	protected void setStatusMessageUIThread(final Project project, final String message) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				setStatusMessage(project, message);
			}
		});
	}

	protected abstract void setStatusMessage(final Project project, final String message);

	protected abstract BambooBuildAdapterIdea getBuild(final AnActionEvent event);


	protected void runBuild(AnActionEvent e) {
		final Project project = IdeaHelper.getCurrentProject(e);
		final BambooBuildAdapterIdea build = getBuild(e);

		if (project != null && build != null && build.getBuildKey() != null) {

			Task.Backgroundable executeTask = new Task.Backgroundable(project, "Starting Build", false) {
				@Override
				public void run(final ProgressIndicator indicator) {

					try {
						setStatusMessageUIThread(project, "Starting build on plan: " + build.getBuildKey());
						BambooServerFacadeImpl.getInstance(PluginUtil.getLogger()).
								executeBuild(build.getServer(), build.getBuildKey());
						setStatusMessageUIThread(project, "Build executed on plan: " + build.getBuildKey());
					} catch (ServerPasswordNotProvidedException e) {
						setStatusMessageUIThread(project, "Build not executed: Password not provided for server");
					} catch (RemoteApiException e) {
						setStatusMessageUIThread(project, "Build not executed: " + e.getMessage());
					}
				}
			};

			ProgressManager.getInstance().run(executeTask);
		}
	}

	protected void openBuildInBrowser(final AnActionEvent e) {
		final BambooBuildAdapterIdea build = getBuild(e);

		if (build != null) {
			BrowserUtil.launchBrowser(build.getBuildResultUrl());
		}
	}
}
