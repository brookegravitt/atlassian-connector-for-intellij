using System;
using System.Collections.Generic;
using System.Diagnostics;
using EnvDTE;
using PaZu.api;

namespace PaZu.models
{
    class RecentlyViewedIssuesModel
    {
        private readonly List<RecentlyViewedIssue> issues = new List<RecentlyViewedIssue>();

        private static readonly RecentlyViewedIssuesModel INSTANCE = new RecentlyViewedIssuesModel();
        private const string RECENTLY_VIEWED_COUNT = "recentlyViewedIssuesCount";
        private const string RECENTLY_VIEWED_ISSUE_KEY = "recentlyViewedIssueKey_";
        private const string RECENTLY_VIEWED_ISSUE_SERVER_GUID = "recentlyViewedIssueServerGuid_";

        private const int MAX_ITEMS = 10;

        public static RecentlyViewedIssuesModel Instance { get { return INSTANCE; } }

        private bool changedSinceLoading;

        public void add(JiraIssue issue)
        {
            lock(this)
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
                changedSinceLoading = true;
            }
        }

        private bool moveToFrontIfContains(JiraIssue issue)
        {
            foreach (RecentlyViewedIssue rvi in issues)
            {
                if (!rvi.ServerGuid.Equals(issue.Server.GUID) || !rvi.IssueKey.Equals(issue.Key)) continue;
                issues.Remove(rvi);
                issues.Insert(0, rvi);
                changedSinceLoading = true;
                return true;
            }
            return false;
        }

        public ICollection<RecentlyViewedIssue> Issues { get { return issues; } }

        public void load(Globals globals)
        {
            lock (this)
            {
                issues.Clear();

                if (globals.get_VariableExists(RECENTLY_VIEWED_COUNT))
                {
                    try
                    {
                        int count = int.Parse(globals[RECENTLY_VIEWED_COUNT].ToString());
                        if (count > MAX_ITEMS)
                        {
                            count = MAX_ITEMS;
                        }

                        for (int i = 1; i <= count; ++i)
                        {
                            string guidStr = globals[RECENTLY_VIEWED_ISSUE_SERVER_GUID + i].ToString();
                            Guid guid = new Guid(guidStr);
                            string key = globals[RECENTLY_VIEWED_ISSUE_KEY + i].ToString();

                            RecentlyViewedIssue issue = new RecentlyViewedIssue(guid, key);
                            issues.Add(issue);
                        }
                    }
                    catch (Exception e)
                    {
                        Debug.WriteLine(e);
                    }
                }
                changedSinceLoading = false;
            }
        }

        public void save(Globals globals)
        {
            lock (this)
            {
                if (!changedSinceLoading)
                    return;

                try
                {
                    globals[RECENTLY_VIEWED_COUNT] = issues.Count.ToString();
                    globals.set_VariablePersists(RECENTLY_VIEWED_COUNT, true);

                    int i = 1;
                    foreach (RecentlyViewedIssue issue in issues)
                    {
                        string var = RECENTLY_VIEWED_ISSUE_SERVER_GUID + i;
                        globals[var] = issue.ServerGuid.ToString();
                        globals.set_VariablePersists(var, true);
                        var = RECENTLY_VIEWED_ISSUE_KEY + i;
                        globals[var] = issue.IssueKey;
                        globals.set_VariablePersists(var, true);
                        ++i;
                    }
                }
                catch (Exception e)
                {
                    Debug.WriteLine(e);
                }
            }
        }
    }
}
