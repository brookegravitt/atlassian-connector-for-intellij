using System;
using System.Collections.Generic;
using PaZu.api;

namespace PaZu.models
{
    public class JiraIssueListModel
    {
        private static readonly JiraIssueListModel INSTANCE = new JiraIssueListModel();

        private readonly List<JiraIssueListModelListener> listeners = new List<JiraIssueListModelListener>();
        private readonly List<JiraIssue> issues = new List<JiraIssue>();
        public ICollection<JiraIssue> Issues { get { return issues; } }

        private JiraIssueListModel()
        {
        }

        public static JiraIssueListModel Instance { get { return INSTANCE; } }

        public void addListener(JiraIssueListModelListener l)
        {
            listeners.Add(l);
        }

        public void removeListener(JiraIssueListModelListener l)
        {
            listeners.Remove(l);
        }

        public void removeAllListeners()
        {
            listeners.Clear();
        }

        public void clear(bool notify)
        {
            lock (issues)
            {
                issues.Clear();
                if (notify)
                {
                    notifyListenersOfModelChange();
                }
            }
        }

        public void addIssues(ICollection<JiraIssue> newIssues)
        {
            lock(issues)
            {
                foreach (var issue in newIssues)
                {
                    issues.Add(issue);
                }
                notifyListenersOfModelChange();
            }
        }

        public void updateIssue(JiraIssue issue)
        {
            lock (issues)
            {
                foreach (var i in Issues)
                {
                    if (!i.Id.Equals(issue.Id)) continue;
                    issues.Remove(i);
                    issues.Add(issue);
                    notifyListenersOfIssueChange(issue);
                    break;
                }
            }
        }

        private void notifyListenersOfIssueChange(JiraIssue issue)
        {
            foreach (var l in listeners)
            {
                l.issueChanged(issue);
            }
        }

        private void notifyListenersOfModelChange()
        {
            foreach (var l in listeners)
            {
                l.modelChanged();
            }
        }
    }
}
