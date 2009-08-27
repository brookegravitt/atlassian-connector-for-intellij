﻿using System.Windows.Forms;
using EnvDTE;
using PaZu.api;

namespace PaZu
{
    public partial class IssueDetailsWindow : UserControl
    {
        public static IssueDetailsWindow Instance { get; private set; }
        public Window WindowInstance { get; set; }

        public IssueDetailsWindow()
        {
            InitializeComponent();

            Instance = this;
        }

        public void clearAllIssues()
        {
            issueTabs.TabPages.Clear();
        }

        public void openIssue(JiraIssue issue)
        {
            WindowInstance.Visible = true;

            string key = getIssueTabKey(issue);
            if (!issueTabs.TabPages.ContainsKey(key))
            {
                TabPage issueTab = new TabPage {Name = key, Text = issue.Key};
                IssueDetailsPanel issuePanel = new IssueDetailsPanel(issue, issueTabs, issueTab);
                issueTab.Controls.Add(issuePanel);
                issuePanel.Dock = DockStyle.Fill;
                issueTabs.TabPages.Add(issueTab);
            }
            issueTabs.SelectTab(key);
        }

        private static string getIssueTabKey(JiraIssue issue)
        {
            return issue.Server.GUID + issue.Key;
        }
    }
}
