using System;
using System.Collections.Generic;
using System.Diagnostics;
using EnvDTE;
using PaZu.api;
using PaZu.util;

namespace PaZu.models
{
    class RecentlyViewedIssuesModel
    {
        private readonly List<RecentlyViewedIssue> issues = new List<RecentlyViewedIssue>();

        private static readonly RecentlyViewedIssuesModel INSTANCE = new RecentlyViewedIssuesModel();
        private const string RECENTLY_VIEWED_COUNT = "recentlyViewedIssuesCount_";
        private const string RECENTLY_VIEWED_ISSUE_KEY = "recentlyViewedIssueKey_";
        private const string RECENTLY_VIEWED_ISSUE_SERVER_GUID = "recentlyViewedIssueServerGuid_";

        private const int MAX_ITEMS = 10;

        public static RecentlyViewedIssuesModel Instance { get { return INSTANCE; } }

        private bool changedSinceLoading;

        public void add(JiraIssue issue)
        {
            lock (this)
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

        public void load(Globals globals, string solutionName)
        {
            lock (this)
            {
                issues.Clear();

                solutionName = ParameterSerializer.getKeyFromSolutionName(solutionName);

                int count = ParameterSerializer.loadParameter(globals, RECENTLY_VIEWED_COUNT + solutionName, -1);
                if (count != -1)
                {
                    try
                    {
                        if (count > MAX_ITEMS)
                            count = MAX_ITEMS;

                        for (int i = 1; i <= count; ++i)
                        {
                            string guidStr = ParameterSerializer.loadParameter(globals, RECENTLY_VIEWED_ISSUE_SERVER_GUID + solutionName + "_" + i, null);
                            Guid guid = new Guid(guidStr);
                            string key = ParameterSerializer.loadParameter(globals, RECENTLY_VIEWED_ISSUE_KEY + solutionName + "_" + i, null);
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

        public void save(Globals globals, string solutionName)
        {
            lock (this)
            {
                if (!changedSinceLoading)
                    return;

                solutionName = ParameterSerializer.getKeyFromSolutionName(solutionName);

                try
                {
                    ParameterSerializer.storeParameter(globals, RECENTLY_VIEWED_COUNT + solutionName, issues.Count);

                    int i = 1;
                    foreach (RecentlyViewedIssue issue in issues)
                    {
                        string var = RECENTLY_VIEWED_ISSUE_SERVER_GUID + solutionName + "_" + i;
                        ParameterSerializer.storeParameter(globals, var, issue.ServerGuid.ToString());
                        var = RECENTLY_VIEWED_ISSUE_KEY + solutionName + "_" + i;
                        ParameterSerializer.storeParameter(globals, var, issue.IssueKey);
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