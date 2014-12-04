package com.atlassian.theplugin.idea.action.bitbucket;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;

import java.awt.datatransfer.StringSelection;

public class CopyBitbucketLinkToClipboard extends AnAction
{

    @Override
    public void actionPerformed(final AnActionEvent event)
    {
        VcsActionDetails details = IntelliJGitUtils.extractVcsActionDetails(event);
        String bitbucketUrl = IntelliJGitUtils.createBitbucketUrl(details);
        CopyPasteManager.getInstance().setContents(new StringSelection(bitbucketUrl));
    }

}
