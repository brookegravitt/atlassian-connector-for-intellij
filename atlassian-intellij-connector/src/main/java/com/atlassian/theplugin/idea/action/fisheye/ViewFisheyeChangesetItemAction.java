package com.atlassian.theplugin.idea.action.fisheye;

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.fisheye.FisheyeUrlHelper;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;

/**
 * User: mwent
 * Date: Mar 20, 2009
 * Time: 7:06:58 PM
 */
public class ViewFisheyeChangesetItemAction extends AbstractFisheyeAction {
	private final Icon icon = IconLoader.getIcon("/icons/fisheye-16.png");

	@Override
	public void actionPerformed(AnActionEvent event) {
		ServerData cfg = getFishEyeServerCfg(event);
		String repository = getFishEyeRepository(event);
		if (cfg != null && repository != null) {
			Project project = IdeaHelper.getCurrentProject(event);
			Change change = ChangeListUtil.getChangeItem(event);
			VirtualFile virtualFile;
			String url = null;
			if (change.getAfterRevision() != null) {
				virtualFile = change.getAfterRevision().getFile().getVirtualFile();
				url = FisheyeUrlHelper.getFisheyeUrl(project, virtualFile, change.getAfterRevision().getRevisionNumber());
			}
			if (url != null) {
				BrowserUtil.launchBrowser(url);
			}
		}
	}

	@Override
	public void update(final AnActionEvent event) {
		super.update(event);
		if (event.getPresentation().getIcon() == null) {
			event.getPresentation().setIcon(icon);
		}
		boolean enabled = false;
		final Change[] changes = DataKeys.CHANGES.getData(event.getDataContext());
        Project project = IdeaHelper.getCurrentProject(event);
		if (changes != null && changes.length == 1 && project != null && project.getBaseDir() != null)  {
			if (changes[0].getAfterRevision() != null) {
				if (event.getPresentation().isVisible()) {
					enabled = true;
				}
			}
		}
		event.getPresentation().setEnabled(enabled);
	}
}
