package com.atlassian.theplugin.idea.action.fisheye;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.FishEyeServer;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ProjectId;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public abstract class FisheyeLinkAction extends AbstractFisheyeAction {

	protected abstract void performUrlAction(final String url);

	private String buildRemoteUrl(final VcsRevisionNumber rev, @NotNull final FishEyeServer fishEyeServer,
			@NotNull final String repo, @NotNull final String projectPath, @NotNull final String fileRelativePath,
			final int lineNumber) {

		StringBuffer sb = new StringBuffer();
		sb.append(fishEyeServer.getUrl());
		sb.append("/browse/");
		sb.append(repo);
		sb.append('/');
		sb.append(projectPath);
		if (sb.charAt(sb.length() - 1) != '/') {
			sb.append("/");
		}
		sb.append(fileRelativePath);
		if (rev != null) {
			sb.append("?r=");
			sb.append(rev.asString());
			sb.append("#l");
			sb.append(lineNumber);
		}

		return sb.toString();
	}


	@Override
	public void actionPerformed(final AnActionEvent event) {
		final VirtualFile virtualFile = event.getData(DataKeys.VIRTUAL_FILE);
		final Editor editor = event.getData(DataKeys.EDITOR);
		if (editor == null || virtualFile == null) {
			return;
		}

		final Project project = IdeaHelper.getCurrentProject(event);
		if (project == null) {
			return;
		}
		final ProjectId projectId = CfgUtil.getProjectId(project);
		final ProjectConfiguration projectCfg = IdeaHelper.getCfgManager().getProjectConfiguration(projectId);
		if (projectCfg == null) {
			return;
		}

		final FishEyeServer fishEyeServer = projectCfg.getDefaultFishEyeServer();
		if (fishEyeServer == null) {
			Messages.showInfoMessage(project,
					"Cannot determine enabled default FishEye server. Make sure you have configured it correctly.",
					"Configuration problem");
			return;
		}


		String fisheyeProjPath = projectCfg.getFishEyeProjectPath();
		if (fisheyeProjPath == null) {
			fisheyeProjPath = "";
		}

		final int lineNumber = editor.getDocument().getLineNumber(editor.getSelectionModel().getSelectionStart()) + 1;
		final String relativePath = VfsUtil.getPath(project.getBaseDir(), virtualFile, '/');
		if (relativePath == null) {
			Messages.showErrorDialog(project, "Cannot determine relative path to file " + virtualFile.getName(),
					"Error");
			return;
		}
		final VcsRevisionNumber rev = VcsIdeaHelper.getVcsRevisionNumber(project, virtualFile);
		if (rev == null) {
			Messages.showErrorDialog(project, "File " + virtualFile.getName() + " is not under version control!",
					"Error");
			return;
		}
		final String url = buildRemoteUrl(rev, fishEyeServer, projectCfg.getDefaultFishEyeRepo(), fisheyeProjPath,
				relativePath, lineNumber);
		performUrlAction(url);

	}

	public interface FisheyeAction {
		void run(String url);
	}
}
