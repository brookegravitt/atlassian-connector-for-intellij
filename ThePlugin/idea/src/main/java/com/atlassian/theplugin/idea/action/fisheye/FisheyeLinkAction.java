package com.atlassian.theplugin.idea.action.fisheye;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.model.SvnRepository;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;

public abstract class FisheyeLinkAction extends AbstractFisheyeAction {
	protected abstract void performUrlAction(final String url);

	@Override
	public void actionPerformed(final AnActionEvent event) {
		final Project project = IdeaHelper.getCurrentProject(event);
		final VirtualFile virtualFile = event.getData(DataKeys.VIRTUAL_FILE);
		final Editor editor = event.getData(DataKeys.EDITOR);
		if (editor == null || project == null || virtualFile == null) {
			return;
		}
		final int lineNumber = editor.getDocument().getLineNumber(editor.getSelectionModel().getSelectionStart()) + 1;
		new BuildFisheyeLinkTask(project, virtualFile, lineNumber, IdeaHelper.getCfgManager(), new FisheyeAction() {
					public void run(final String url) {
						performUrlAction(url);
					}
				}).queue();
	}

	public interface FisheyeAction {
		void run(String url);
	}

	private class BuildFisheyeLinkTask extends Task.Backgroundable {
		private final VirtualFile virtualFile;
		private final int lineNumber;
		private final CfgManager cfgManager;
		private final FisheyeAction action;
		private String url = null;
		private final Project project;
		private StringBuilder errorMsg = new StringBuilder();

		public BuildFisheyeLinkTask(@NotNull final Project project, @NotNull final VirtualFile virtualFile,
				int lineNumber, @NotNull final CfgManager cfgManager, @NotNull FisheyeAction action) {
			super(project, "Building Fisheye source pointer", false);
			this.project = project;
			this.virtualFile = virtualFile;
			this.lineNumber = lineNumber;
			this.cfgManager = cfgManager;
			this.action = action;

		}

		@Override
		public boolean shouldStartInBackground() {
			return true;
		}

		@Override
		public void run(ProgressIndicator indicator) {
			final String fileRepoUrl = VcsIdeaHelper.getRepositoryUrlForFile(project, virtualFile);
			final AbstractVcs vcs = VcsUtil.getVcsFor(project, virtualFile);
			if (vcs == null) {
				errorMsg.append("Cannot determing SCM support for the project");
				return;
			}

			final DiffProvider diffProvider = vcs.getDiffProvider();
			if (diffProvider == null) {
				errorMsg.append("Cannot obtain DiffProvider for the project");
				return;
			}
			final VcsRevisionNumber rev = diffProvider.getCurrentRevision(virtualFile);
			if (rev == null) {
				errorMsg.append("Cannot determine revision number of file ").append(virtualFile.getName());
				return;
			}

			for (CrucibleServerCfg crucCfg : cfgManager.getAllEnabledCrucibleServers(CfgUtil.getProjectId(project))) {
				if (crucCfg.isFisheyeInstance() == false) {
					continue;
				}

				url = getRemoteUrl(crucCfg, fileRepoUrl, rev);
				if (url != null) {
					return;
				}
			}
		}

		private String getRemoteUrl(final CrucibleServerCfg crucCfg, final String fileRepoUrl,
				final VcsRevisionNumber rev) {
			String repoName = crucCfg.getRepositoryName();
			if (repoName == null) {
				return null;
			}

			try {
				SvnRepository repo = CrucibleServerFacadeImpl.getInstance().getRepository(crucCfg, repoName);
				if (repo != null) {
					String repoUrl = repo.getUrl() + "/" + repo.getPath();
					if (fileRepoUrl.startsWith(repoUrl) == false) {
						return null;
					}
					StringBuffer sb = new StringBuffer();
					sb.append(crucCfg.getUrl());
					sb.append("/browse/");
					sb.append(repoName);

					sb.append(fileRepoUrl.substring(repoUrl.length()));
					sb.append("?r=");
					sb.append(rev.asString());
					sb.append("#l");
					sb.append(lineNumber);

					return sb.toString();
				}
			} catch (Exception e) {
				LoggerImpl.getInstance().warn(e);
				return null;
			}
			return null;
		}

		@Override
		public void onSuccess() {
			if (errorMsg.length() != 0) {
				Messages.showErrorDialog(project, errorMsg.toString(), "Error while opening file in FishEye");
				return;
			}
			if (url == null) {
				Messages.showErrorDialog(project, "Cannot map file " + virtualFile.getName() + " to any FishEye instance.\n"
						+ "Make sure you have configured your servers properly.", "Error");
			}
			if (url != null) {
				action.run(url);
			}
		}
	}

}
