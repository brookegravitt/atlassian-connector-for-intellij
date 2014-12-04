package com.atlassian.theplugin.idea.action.bitbucket;

public class VcsActionDetails
{

    private String repoOwner;
    private String repoName;
    private String revision;
    private String sourcePath;
    private String branchName;
    private int lineNumber;

    public VcsActionDetails(final String repoOwner, final String repoName, final String revision, final String sourcePath, final String branchName, final int lineNumber)
    {
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.revision = revision;
        this.sourcePath = sourcePath;
        this.branchName = branchName;
        this.lineNumber = lineNumber;
    }

    public String getRepoOwner()
    {
        return repoOwner;
    }

    public String getRepoName()
    {
        return repoName;
    }

    public String getRevision()
    {
        return revision;
    }

    public String getSourcePath()
    {
        return sourcePath;
    }

    public String getBranchName()
    {
        return branchName;
    }

    public int getLineNumber()
    {
        return lineNumber;
    }
}
