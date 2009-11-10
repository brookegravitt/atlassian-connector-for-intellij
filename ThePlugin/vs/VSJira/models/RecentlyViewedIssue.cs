using System;
using PaZu.api;

namespace PaZu.models
{
    class RecentlyViewedIssue
    {
        public Guid ServerGuid { get; private set; }
        public string IssueKey { get; private set; }

        public RecentlyViewedIssue(JiraIssue issue)
        {
            ServerGuid = issue.Server.GUID;
            IssueKey = issue.Key;
        }

        public RecentlyViewedIssue(Guid serverGuid, string issueKey)
        {
            ServerGuid = serverGuid;
            IssueKey = issueKey;
        }
    }
}
