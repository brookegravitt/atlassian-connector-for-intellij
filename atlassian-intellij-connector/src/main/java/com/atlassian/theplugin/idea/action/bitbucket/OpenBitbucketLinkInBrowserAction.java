package com.atlassian.theplugin.idea.action.bitbucket;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitUtil;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;

import java.util.Collection;

public class OpenBitbucketLinkInBrowserAction extends AnAction
{

    @Override
    public void update(final AnActionEvent event) {
        System.out.println("Update in bitbucket action");
    }

    @Override
    public void actionPerformed(final AnActionEvent e)
    {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        final VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (virtualFile == null || project == null || project.isDisposed()) {
            return;
        }

        // What I need:
        // https://bitbucket.org/marcosscriven/intellij-plugin-test/src/6c30aff3cc1fa9c2f1d31836707e2a2023b43d83/src/Main.java?at=master
        // https://bitbucket.org/marcosscriven/intellij-plugin-test/src/6c30aff3cc1fa9c2f1d31836707e2a2023b43d83/src/Main.java?at=master#cl-4

        String BITBUCKET_URL = "https://bitbucket.org/%s/%s/src/%s/%s?at=%s";

        GitRepository gitRepository = GitUtil.getRepositoryManager(project).getRepositoryForFile(virtualFile);

        String branchName = gitRepository.getCurrentBranchName();
        String localGitRoot = gitRepository.getRoot().getPath();
        String localFilePath = virtualFile.getPath();
        String sourcePath = localFilePath.substring(localGitRoot.length());

        Collection<GitRemote> remotes = gitRepository.getRemotes();

        String remoteString = null;
        for(GitRemote remote : remotes)
        {
            remoteString = remote.getFirstUrl();
        }

        String[] userAndRepo = remoteString.substring(remoteString.lastIndexOf(":")+1).split("/");
        String username = userAndRepo[0];
        String repo = userAndRepo[1];
        String currentRevision = gitRepository.getCurrentRevision();


        String remoteUrl = String.format(BITBUCKET_URL, username, repo, currentRevision, sourcePath, branchName);
        if (remoteUrl != null) {
            BrowserUtil.browse(remoteUrl);
        }

        System.out.println("Performed bitbucket action");
    }

}
