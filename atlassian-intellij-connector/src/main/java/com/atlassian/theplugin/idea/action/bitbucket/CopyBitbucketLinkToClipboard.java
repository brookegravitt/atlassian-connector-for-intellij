package com.atlassian.theplugin.idea.action.bitbucket;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;

import java.awt.datatransfer.StringSelection;

public class CopyBitbucketLinkToClipboard extends AnAction
{

    @Override
    public void update(AnActionEvent event) {
        VcsActionDetails details = IntelliJGitUtils.extractVcsActionDetails(event);
        String text = "Copy Bitbucket link to clipboard";
        if(details.getProduct().equals("stash"))
        {
            text = "Copy Stash link to clipboard";
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
        CopyPasteManager.getInstance().setContents(new StringSelection(remoteUrl));
    }

}
