package com.atlassian.theplugin.idea.action.fisheye;

import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.model.SvnRepository;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;

public abstract class FisheyeLinkAction extends AbstractFisheyeAction {
	protected abstract void performUrlAction(final String url);

	public void actionPerformed(final AnActionEvent event) {
		final Project project = IdeaHelper.getCurrentProject(event);
		final VirtualFile virtualFile = event.getData(DataKeys.VIRTUAL_FILE);
		final Editor editor = event.getData(DataKeys.EDITOR);
		final int lineNumber = editor.getDocument().getLineNumber(editor.getSelectionModel().getSelectionStart()) + 1;
		final CrucibleServerCfg crucibleServerCfg = getCrucibleServerCfg(event);

		new BuildFisheyeLinkTask(project, virtualFile, lineNumber, crucibleServerCfg,
				new FisheyeAction() {
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
		private final CrucibleServerCfg crucibleServerCfg;
		private final FisheyeAction action;
		private String url = null;
		private Project project;

		public BuildFisheyeLinkTask(final Project project, final VirtualFile virtualFile, int lineNumber,
				CrucibleServerCfg crucibleServerCfg, FisheyeAction action) {
			super(project, "Building Fisheye source pointer", false);
			this.project = project;
			this.virtualFile = virtualFile;
			this.lineNumber = lineNumber;
			this.crucibleServerCfg = crucibleServerCfg;
			this.action = action;
		}

		@Override
		public boolean shouldStartInBackground() {
			return true;
		}

		@Override
		public void run(ProgressIndicator indicator) {
			if (crucibleServerCfg != null) {
				if (virtualFile != null) {
					String link = VcsIdeaHelper.getRepositoryUrlForFile(project, virtualFile);
					String repoName = crucibleServerCfg.getRepositoryName();
					try {
						SvnRepository repo = CrucibleServerFacadeImpl.getInstance().getRepository(crucibleServerCfg, repoName);
						if (repo != null) {
							String repoUrl = repo.getUrl() + "/" + repo.getPath();

							AbstractVcs vcs = VcsUtil.getVcsFor(project, virtualFile);
							VcsRevisionNumber rev = vcs.getDiffProvider().getCurrentRevision(virtualFile);

							StringBuffer sb = new StringBuffer();
							sb.append(crucibleServerCfg.getUrl());
							sb.append("/browse/");
							sb.append(repoName);
							sb.append(link.replace(repoUrl, ""));
							sb.append("?r=");
							sb.append(rev.asString());
							sb.append("#l");
							sb.append(lineNumber);

							url = sb.toString();
						}
					} catch (Exception e) {
						// well, can't do anything
					}
				}
			}
		}

		@Override
		public void onSuccess() {
			if (url != null) {
				action.run(url);
			}
		}
	}

}
