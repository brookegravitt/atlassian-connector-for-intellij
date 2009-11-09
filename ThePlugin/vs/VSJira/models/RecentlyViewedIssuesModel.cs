using System.Collections.Generic;
using PaZu.api;

namespace PaZu.models
{
    class RecentlyViewedIssuesModel
    {
        private readonly List<RecentlyViewedIssue> issues = new List<RecentlyViewedIssue>();

        private static readonly RecentlyViewedIssuesModel INSTANCE = new RecentlyViewedIssuesModel();
        private const int MAX_ITEMS = 10;

        public static RecentlyViewedIssuesModel Instance { get { return INSTANCE; } }

        public void clear()
        {
            issues.Clear();
        }

        public void add(JiraIssue issue)
        {
            if (moveToFrontIfContains(issue))
            {
                return;
            }
            while (issues.Count >= MAX_ITEMS)
            {
                issues.RemoveAt(issues.Count - 1);
            }
            issues.Insert(0, new RecentlyViewedIssue(issue));
        }

        private bool moveToFrontIfContains(JiraIssue issue)
        {
            foreach (RecentlyViewedIssue rvi in issues)
            {
                if (!rvi.ServerGuid.Equals(issue.Server.GUID) || !rvi.IssueKey.Equals(issue.Key)) continue;
                issues.Remove(rvi);
                issues.Insert(0, rvi);
                return true;
            }
            return false;
        }

        public ICollection<RecentlyViewedIssue> Issues { get { return issues; } }
    }
}
