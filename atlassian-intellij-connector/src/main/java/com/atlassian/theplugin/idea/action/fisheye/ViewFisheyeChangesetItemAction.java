package com.atlassian.theplugin.idea.action.fisheye;

import com.atlassian.theplugin.commons.cfg.FishEyeServer;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.fisheye.FisheyeUrlHelper;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * User: mwent
 * Date: Mar 20, 2009
 * Time: 7:06:58 PM
 */
public class ViewFisheyeChangesetItemAction extends AbstractFisheyeAction {
	@Override
	public void actionPerformed(AnActionEvent event) {
		FishEyeServer cfg = getFishEyeServerCfg(event);
		String repository = getFishEyeRepository(event);
		if (cfg != null && repository != null) {
			Project project = IdeaHelper.getCurrentProject(event);
			Change change = ChangeListUtil.getChangeItem(event);
			VirtualFile virtualFile;
			String url = null;
			if (change.getAfterRevision() != null) {
				virtualFile = change.getAfterRevision().getFile().getVirtualFile();
				url = FisheyeUrlHelper.getFisheyeUrl(project, virtualFile, change.getAfterRevision().getRevisionNumber());
			} else {
				if (change.getBeforeRevision() != null) {
					virtualFile = change.getBeforeRevision().getFile().getVirtualFile();
					url = FisheyeUrlHelper.getFisheyeUrl(project, virtualFile, change.getBeforeRevision().getRevisionNumber());
				}
			}
			if (url != null) {
				BrowserUtil.launchBrowser(url);
			}
		}
	}

	@Override
	public void update(final AnActionEvent event) {
		super.update(event);
		final Change[] changes = DataKeys.CHANGES.getData(event.getDataContext());
		if (changes != null && changes.length == 1) {
			if (event.getPresentation().isVisible()) {
				event.getPresentation().setEnabled(ChangeListUtil.getRevision(event) != null);
			}
		} else {
			event.getPresentation().setEnabled(false);
		}
	}
}
