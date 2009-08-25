using System;
using System.Collections.Generic;
using System.Text;
using PaZu.api;

namespace PaZu.models
{
    public class JiraIssueListModel
    {
        private static JiraIssueListModel instance = new JiraIssueListModel();

        private List<JiraIssueListModelListener> listeners = new List<JiraIssueListModelListener>();
        private List<JiraIssue> issues = new List<JiraIssue>();

        private JiraIssueListModel()
        {
        }

        public static JiraIssueListModel Instance { get { return instance; } }

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
            issues.Clear();
            if (notify)
            {
                notifyListeners();
            }
        }

        public void addIssues(ICollection<JiraIssue> newIssues)
        {
            foreach (JiraIssue issue in newIssues)
            {
                issues.Add(issue);
            }
            notifyListeners();
        }

        public ICollection<JiraIssue> Issues { get { return issues; } }

        private void notifyListeners()
        {
            foreach (JiraIssueListModelListener l in listeners)
            {
                l.modelChanged();
            }
        }
    }
}
