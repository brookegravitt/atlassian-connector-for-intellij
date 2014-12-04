package com.atlassian.theplugin.idea.action.bitbucket;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class OpenBitbucketLinkInBrowserAction extends AnAction
{

    @Override
    public void update(AnActionEvent event) {
        VcsActionDetails details = IntelliJGitUtils.extractVcsActionDetails(event);
        String text = "Open in Bitbucket";
        if(details.getProduct().equals("stash"))
        {
            text = "Open in Stash";
        }
        event.getPresentation().setText(text);
    }

    @Override
    public void actionPerformed(final AnActionEvent event)
    {
        VcsActionDetails details = IntelliJGitUtils.extractVcsActionDetails(event);
        String remoteUrl = null;
        if(details.getProduct().equals("bitbucket"))
        {
            remoteUrl = IntelliJGitUtils.createBitbucketUrl(details);
        }
        else
        {
            remoteUrl = IntelliJGitUtils.createStashUrl(details);
        }

        if (remoteUrl != null) {
            BrowserUtil.browse(remoteUrl);
        }

    }

}
