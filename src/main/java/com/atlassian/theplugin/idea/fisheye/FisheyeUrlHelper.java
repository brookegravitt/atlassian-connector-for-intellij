package com.atlassian.theplugin.idea.fisheye;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.FishEyeServer;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ProjectId;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: mwent
 * Date: Mar 10, 2009
 * Time: 12:00:06 PM
 */
public final class FisheyeUrlHelper {

	private FisheyeUrlHelper() {
	}

	@Nullable
	public static String getFisheyeUrl(final VirtualFile virtualFile, final Editor editor,
			final Project project) {
		final ProjectId projectId = CfgUtil.getProjectId(project);
		final ProjectConfiguration projectCfg = IdeaHelper.getCfgManager().getProjectConfiguration(projectId);
		if (projectCfg == null) {
			return null;
		}

		final FishEyeServer fishEyeServer = projectCfg.getDefaultFishEyeServer();
		if (fishEyeServer == null) {
			Messages.showInfoMessage(project,
					"Cannot determine enabled default FishEye server. Make sure you have configured it correctly.",
					"Configuration problem");
			return null;
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
			return null;
		}
		final VcsRevisionNumber rev = VcsIdeaHelper.getVcsRevisionNumber(project, virtualFile);
		if (rev == null) {
			Messages.showErrorDialog(project, "File " + virtualFile.getName() + " is not under version control!",
					"Error");
			return null;
		}
		return buildRemoteUrl(rev, fishEyeServer, projectCfg.getDefaultFishEyeRepo(), fisheyeProjPath,
				relativePath, lineNumber);
	}

	@Nullable
	public static String getFisheyeUrl(final Project project, final VirtualFile virtualFile, VcsRevisionNumber revision) {
		final ProjectId projectId = CfgUtil.getProjectId(project);
		final ProjectConfiguration projectCfg = IdeaHelper.getCfgManager().getProjectConfiguration(projectId);
		if (projectCfg == null) {
			return null;
		}

		final FishEyeServer fishEyeServer = projectCfg.getDefaultFishEyeServer();
		if (fishEyeServer == null) {
			Messages.showInfoMessage(project,
					"Cannot determine enabled default FishEye server. Make sure you have configured it correctly.",
					"Configuration problem");
			return null;
		}


		String fisheyeProjPath = projectCfg.getFishEyeProjectPath();
		if (fisheyeProjPath == null) {
			fisheyeProjPath = "";
		}

		final String relativePath = VfsUtil.getPath(project.getBaseDir(), virtualFile, '/');
		if (relativePath == null) {
			Messages.showErrorDialog(project, "Cannot determine relative path to file " + virtualFile.getName(),
					"Error");
			return null;
		}
		if (revision == null) {
			Messages.showErrorDialog(project, "File " + virtualFile.getName() + " is not under version control!",
					"Error");
			return null;
		}
		return buildRemoteUrl(revision, fishEyeServer, projectCfg.getDefaultFishEyeRepo(), fisheyeProjPath,
				relativePath, 1);
	}

	@Nullable
	public static String getFisheyeUrl(final PsiElement psiElement,
			final Project project) {

		final ProjectId projectId = CfgUtil.getProjectId(project);
		final ProjectConfiguration projectCfg = IdeaHelper.getCfgManager().getProjectConfiguration(projectId);
		if (projectCfg == null) {
			return null;
		}

		final FishEyeServer fishEyeServer = projectCfg.getDefaultFishEyeServer();
		if (fishEyeServer == null) {
			return null;
		}

		String fisheyeProjPath = projectCfg.getFishEyeProjectPath();
		if (fisheyeProjPath == null) {
			fisheyeProjPath = "";
		}

		int offset = psiElement.getTextRange().getStartOffset();
		VirtualFile virtualFile = psiElement.getContainingFile().getVirtualFile();

		if (virtualFile == null) {
			return null;
		}
		FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
		Document document = fileDocumentManager.getDocument(virtualFile);

		final int lineNumber = document.getLineNumber(offset) + 1;
		final String relativePath = VfsUtil.getPath(project.getBaseDir(), virtualFile, '/');
		if (relativePath == null) {
			return null;
		}
		final VcsRevisionNumber rev = VcsIdeaHelper.getVcsRevisionNumber(project, virtualFile);
		if (rev == null) {
			return null;
		}
		return buildRemoteUrl(rev, fishEyeServer, projectCfg.getDefaultFishEyeRepo(), fisheyeProjPath,
				relativePath, lineNumber);
	}

	private static String buildRemoteUrl(final VcsRevisionNumber rev, @NotNull final FishEyeServer fishEyeServer,
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
}
