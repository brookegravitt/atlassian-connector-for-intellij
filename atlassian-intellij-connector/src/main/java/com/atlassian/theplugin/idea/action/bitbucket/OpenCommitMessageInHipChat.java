package com.atlassian.theplugin.idea.action.bitbucket;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.annotate.LineNumberListener;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import git4idea.GitFileRevision;

import java.awt.datatransfer.StringSelection;
import java.net.URLEncoder;

public class OpenCommitMessageInHipChat extends AnAction implements LineNumberListener
{

    public static final String HIPCHAT_URL = "hipchat://www.hipchat.com/user/%s?message=%s";
    public static final String MESSAGE = "Hi - I'd like to talk to you about this commit\r\n%s\r\nCommit message - %s";
    //public static final String MESSAGE = "%s%%0AX";

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
        GitFileRevision revision = getCurrentCommit();
        event.getPresentation().setText(String.format("Ask %s about this in HipChat", revision.getAuthor()));
    }

    @Override
    public void actionPerformed(final AnActionEvent event)
    {
        VcsActionDetails details = IntelliJGitUtils.extractVcsActionDetails(event);
        GitFileRevision revision = getCurrentCommit();
        System.out.println("Revision:" + revision + " at line:"+lineNumber);

        int commitBlobLineNumber = IntelliJGitUtils.adjustSrcLineNumberToCommitBlobLineNumber(event, lineNumber);
        details.setLineNumber(commitBlobLineNumber);

        String bitbucketUrl = IntelliJGitUtils.createBitbucketCommitUrl(details, revision.getHash());
        //bitbucketUrl = IntelliJGitUtils.escapeFragment(bitbucketUrl);

        String message = String.format(MESSAGE, bitbucketUrl, revision.getCommitMessage());
        message = URLEncoder.encode(message);
        String hipChatUrl = String.format(HIPCHAT_URL, revision.getAuthorEmail(), message);

        BrowserUtil.browse(hipChatUrl);
    }

    public void consume(final Integer lineNumber)
    {
        System.out.println("line number event:" + lineNumber);
        this.lineNumber = lineNumber;
    }

    private GitFileRevision getCurrentCommit()
    {
        VcsRevisionNumber revisionNumber = fileAnnotation.getLineRevisionNumber(lineNumber);
        for(VcsFileRevision revision : fileAnnotation.getRevisions())
        {
            if(revision.getRevisionNumber().equals(revisionNumber))
            {
                return (GitFileRevision)revision;
            }
        }

        return null;
    }

}
