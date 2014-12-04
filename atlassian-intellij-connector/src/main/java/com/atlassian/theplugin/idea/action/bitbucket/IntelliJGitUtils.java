package com.atlassian.theplugin.idea.action.bitbucket;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.localVcs.UpToDateLineNumberProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevisionEx;
import com.intellij.openapi.vcs.impl.UpToDateLineNumberProviderImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import git4idea.history.GitHistoryUtils;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;

import java.util.Collection;

public class IntelliJGitUtils
{

    // Example https://bitbucket.org/marcosscriven/intellij-plugin-test/src/6c30aff3cc1fa9c2f1d31836707e2a2023b43d83/src/Main.java?at=master#cl-4
    public static final String BITBUCKET_URL = "https://bitbucket.org/%s/%s/src/%s/%s?at=%s#cl-%d";

    // Example https://stash.atlassian.com/projects/JIRA/repos/jira/browse/pom.xml?at=refs%2Fheads%2Fissue%2FJDEV-30979-background-upgrades#12
    public static final String STASH_URL = "https://stash.atlassian.com/projects/%s/repos/%s/browse/%s?at=refs/heads/%s#%d";

    // Example https://bitbucket.org/marcosscriven/intellij-plugin-test.git/commits/6c30aff3cc1fa9c2f1d31836707e2a2023b43d83#chg-src/Main.java
    public static final String BITBUCKET_COMMIT_URL = "https://bitbucket.org/%s/%s/commits/%s#chg-%s";

    // Example https://stash.atlassian.com/projects/JIRA/repos/jira/commits/63d84f14f821ad397ab8e1336dd59f34d130248e#jira-ondemand-project/jira-ondemand-plugins/jira-ondemand-bundled-plugins/pom.xml
    public static final String STASH_COMMIT_URL = "https://stash.atlassian.com/projects/%s/repos/%s/commits/%s#%s";

    public static VcsActionDetails extractVcsActionDetails(AnActionEvent event)
    {
        final Project project = event.getData(CommonDataKeys.PROJECT);
        final VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        final Editor editor = event.getData(CommonDataKeys.EDITOR);

        if (virtualFile == null || project == null || project.isDisposed()) {
            return null;
        }

        GitRepository gitRepository = git4idea.GitUtil.getRepositoryManager(project).getRepositoryForFile(virtualFile);

        String branchName = gitRepository.getCurrentBranchName();
        String localGitRoot = gitRepository.getRoot().getPath();
        String localFilePath = virtualFile.getPath();
        String sourcePath = localFilePath.substring(localGitRoot.length()+1);

        Collection<GitRemote> remotes = gitRepository.getRemotes();

        String remoteString = null;
        String product = "bitbucket";

        for(GitRemote remote : remotes)
        {
            remoteString = remote.getFirstUrl();
        }

        if(remoteString.contains("stash"))
        {
            product = "stash";
            remoteString = remoteString.substring(remoteString.lastIndexOf(":"));
            remoteString = remoteString.substring(remoteString.indexOf("/") + 1);
        }
        else
        {
            remoteString = remoteString.substring(remoteString.lastIndexOf(":") + 1);
        }

        String[] userAndRepo = remoteString.substring(remoteString.lastIndexOf(":")+1).split("/");
        String repoOwner = userAndRepo[0];
        String repoName = userAndRepo[1];
        String currentRevision = gitRepository.getCurrentRevision();
        int currentLine = editor.getCaretModel().getVisualPosition().getLine()+1;

        try
        {
            VcsFileRevisionEx fileRevisionEx = (VcsFileRevisionEx) GitHistoryUtils.history(project, VcsUtil.getFilePath(virtualFile.getPath())).get(0);
        }
        catch (VcsException e)
        {
            throw new RuntimeException(e);
        }

        VcsActionDetails details = new VcsActionDetails(repoOwner, repoName, currentRevision, sourcePath, branchName, product, currentLine);
        return details;
    }

    public static int adjustSrcLineNumberToCommitBlobLineNumber(AnActionEvent event, int srcLineNumber)
    {
        Project project = event.getData(CommonDataKeys.PROJECT);
        VirtualFile virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE);
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);

        final UpToDateLineNumberProvider upToDateLineNumberProvider = new UpToDateLineNumberProviderImpl(document, project);
        int commitBlobLineNumber = upToDateLineNumberProvider.getLineNumber(srcLineNumber);
        return commitBlobLineNumber;
    }

    public static String createBitbucketUrl(VcsActionDetails details)
    {
        String bitbucketUrl = String.format(BITBUCKET_URL, details.getRepoOwner(), details.getRepoName(),
                details.getRevision(), details.getSourcePath(), details.getBranchName(), details.getLineNumber());
        return bitbucketUrl;
    }

    public static String createStashUrl(VcsActionDetails details)
    {
        String repoName = details.getRepoName().replace(".git", "");
        String stashUrl = String.format(STASH_URL, details.getRepoOwner(), repoName, details.getSourcePath(),
                details.getBranchName(), details.getLineNumber());
        return stashUrl;
    }

    public static String createBitbucketCommitUrl(VcsActionDetails details, String commitSha)
    {
        String bitbucketCommitUrl = String.format(BITBUCKET_COMMIT_URL, details.getRepoOwner(), details.getRepoName(),
                commitSha, details.getSourcePath());
        return bitbucketCommitUrl;
    }

    public static String createStashCommitUrl(VcsActionDetails details, String commitSha)
    {
        String repoName = details.getRepoName().replace(".git", "");
        String stashCommitUrl = String.format(STASH_COMMIT_URL, details.getRepoOwner(), repoName,
                commitSha, details.getSourcePath());
        return stashCommitUrl;
    }

    public static String escapeFragment(String urlString)
    {
        return urlString.replace("#", "%23");
    }

}
