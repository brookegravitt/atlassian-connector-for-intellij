package com.atlassian.theplugin.idea.action.bitbucket;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.localVcs.UpToDateLineNumberProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.impl.UpToDateLineNumberProviderImpl;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;

import java.util.Collection;

public class IntelliJGitUtils
{

    // Example https://bitbucket.org/marcosscriven/intellij-plugin-test/src/6c30aff3cc1fa9c2f1d31836707e2a2023b43d83/src/Main.java?at=master#cl-4
    public static final String BITBUCKET_URL = "https://bitbucket.org/%s/%s/src/%s/%s?at=%s#cl-%d";

    // Example https://bitbucket.org/marcosscriven/intellij-plugin-test.git/commits/6c30aff3cc1fa9c2f1d31836707e2a2023b43d83#chg-src/Main.java
    public static final String BITBUCKET_COMMIT_URL = "https://bitbucket.org/%s/%s/commits/%s#chg-%s";

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
        for(GitRemote remote : remotes)
        {
            remoteString = remote.getFirstUrl();
        }

        String[] userAndRepo = remoteString.substring(remoteString.lastIndexOf(":")+1).split("/");
        String repoOwner = userAndRepo[0];
        String repoName = userAndRepo[1];
        String currentRevision = gitRepository.getCurrentRevision();
        int currentLine = editor.getCaretModel().getVisualPosition().getLine()+1;

        VcsActionDetails details = new VcsActionDetails(repoOwner, repoName, currentRevision, sourcePath, branchName, currentLine);
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

    public static String createBitbucketCommitUrl(VcsActionDetails details, String commitSha)
    {
        String bitbucketCommitUrl = String.format(BITBUCKET_COMMIT_URL, details.getRepoOwner(), details.getRepoName(),
                commitSha, details.getSourcePath());
        return bitbucketCommitUrl;
    }

    public static String escapeFragment(String urlString)
    {
        return urlString.replace("#", "%23");
    }

}
