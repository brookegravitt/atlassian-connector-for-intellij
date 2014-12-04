package com.atlassian.theplugin.idea.action.bitbucket;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;

import java.awt.datatransfer.StringSelection;

public class OpenCommitMessageInHipChat extends AnAction
{

    public OpenCommitMessageInHipChat(final String text)
    {
        super(text);
    }

    @Override
    public void actionPerformed(final AnActionEvent event)
    {
        VcsActionDetails details = IntelliJGitUtils.extractVcsActionDetails(event);
        String bitbucketUrl = IntelliJGitUtils.createBitbucketUrl(details);

        // VcsFileRevisionEx revision = (VcsFileRevisionEx)GitHistoryUtils.history(project, VcsUtil.getFilePath(virtualFile.getPath())).get(0);
        String hipChatUrl = "hipchat://www.hipchat.com/user/mscriven@atlassian.com?message=" + bitbucketUrl;
        BrowserUtil.browse(hipChatUrl);
    }

}
