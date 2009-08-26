using System.Drawing;
using PaZu.api;
using PaZu.models;

namespace PaZu.ui.issues
{
    public class IssueNode
    {
        private readonly JiraIssue issue;

        public IssueNode(JiraIssue issue)
        {
            this.issue = issue;
        }

        public Image IssueTypeIcon { get { return ImageCache.Instance.getImage(issue.IssueTypeIconUrl); } }
        public string KeyAndSummary { get { return issue.Key + " - " + issue.Summary; } }
        public Image PriorityIcon { get { return ImageCache.Instance.getImage(issue.PriorityIconUrl); } }
        public string StatusText { get { return issue.Status; } }
        public Image StatusIcon { get { return ImageCache.Instance.getImage(issue.StatusIconUrl); } }
        public string Updated { get { return issue.UpdateDate; } }
    }
}
