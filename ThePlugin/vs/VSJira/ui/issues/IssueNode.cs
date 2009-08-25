using System;
using System.Collections.Generic;
using System.Text;
using System.Drawing;
using PaZu.api;

namespace PaZu.ui.issues
{
    public class IssueNode
    {
        private JiraIssue issue;

        public IssueNode(JiraIssue issue)
        {
            this.issue = issue;
        }

        public Image Icon
        {
            get { return Properties.Resources.ide_plugin_161; }
        }
        public string KeyAndSummary
        {
            get { return issue.Key + " - " + issue.Summary; }
        }
        public string Priority
        {
            get { return "Not implemented"; }
        }
    }
}
