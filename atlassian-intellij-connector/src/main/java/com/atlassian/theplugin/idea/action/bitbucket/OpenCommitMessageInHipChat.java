package com.atlassian.theplugin.idea.action.bitbucket;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.annotate.LineNumberListener;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;

import java.awt.datatransfer.StringSelection;

public class OpenCommitMessageInHipChat extends AnAction implements LineNumberListener
{

    private final FileAnnotation fileAnnotation;
    private int lineNumber;

    public OpenCommitMessageInHipChat(final FileAnnotation fileAnnotation)
    {
        super("Talk about commit in HipChat");
        this.fileAnnotation = fileAnnotation;
    }

    @Override
    public void update(AnActionEvent event) {
        System.out.println("Event in HipChat action:" + event);
    }

    @Override
    public void actionPerformed(final AnActionEvent event)
    {
        VcsActionDetails details = IntelliJGitUtils.extractVcsActionDetails(event);
        VcsRevisionNumber commitRevision = fileAnnotation.getLineRevisionNumber(lineNumber);
        System.out.println("Revision:" + commitRevision + " at line:"+lineNumber);

        int commitBlobLineNumber = IntelliJGitUtils.adjustSrcLineNumberToCommitBlobLineNumber(event, lineNumber);
        details.setLineNumber(commitBlobLineNumber);

        String bitbucketUrl = IntelliJGitUtils.createBitbucketCommitUrl(details, commitRevision.asString());
        String hipChatUrl = "hipchat://www.hipchat.com/user/mscriven@atlassian.com?message=" + bitbucketUrl;
        hipChatUrl = IntelliJGitUtils.escapeFragment(hipChatUrl);

        BrowserUtil.browse(hipChatUrl);
    }

    public void consume(final Integer lineNumber)
    {
        System.out.println("line number event:" + lineNumber);
        this.lineNumber = lineNumber;
    }

}
