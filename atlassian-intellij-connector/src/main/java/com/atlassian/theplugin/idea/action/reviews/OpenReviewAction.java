package com.atlassian.theplugin.idea.action.reviews;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.connector.intellij.crucible.content.ContentDownloader;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * @author jgorycki
 */
public class OpenReviewAction extends AbstractCrucibleToolbarAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = IdeaHelper.getCurrentProject(e);
        ReviewAdapter review = e.getData(Constants.REVIEW_KEY);
        if (review != null) {
            //IdeaHelper.getReviewDetailsToolWindow(project).showReview(review, false);
            IdeaHelper.getReviewListToolWindowPanel(project).openReview(review, false);
            ContentDownloader.getInstance().downloadFilesContent(project, review);
        }
    }

    @Override
    public boolean onUpdate(AnActionEvent e) {
        return (e.getData(Constants.REVIEW_KEY) != null);
    }
}
