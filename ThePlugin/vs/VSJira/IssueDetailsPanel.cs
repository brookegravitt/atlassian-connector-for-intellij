﻿using System;
using System.Drawing;
using System.Text;
using System.Threading;
using System.Windows.Forms;
using System.Diagnostics;

using PaZu.api;
using PaZu.dialogs;

namespace PaZu
{
    public partial class IssueDetailsPanel : UserControl
    {
        private readonly JiraServerFacade facade = JiraServerFacade.Instance;

        private JiraIssue issue;
        private readonly TabControl tabWindow;
        private readonly TabPage myTab;
        private bool issueCommentsLoaded;
        private bool issueDescriptionLoaded;
        private bool issueSummaryLoaded;
        private const int A_LOT = 100000;

        public IssueDetailsPanel(JiraIssue issue, TabControl tabWindow, TabPage myTab)
        {
            InitializeComponent();

            this.issue = issue;
            this.tabWindow = tabWindow;
            this.myTab = myTab;
        }

        private void IssueDetailsPanel_Load(object sender, EventArgs e)
        {
            rebuildAllPanels(false);
            setInfoStatus("No issue details yet");
            runRefreshThread();
        }

        private void rebuildAllPanels(bool enableRefresh)
        {
            Invoke(new MethodInvoker(delegate
                                         {
                                             rebuildSummaryPanel();
                                             rebuildDescriptionPanel();
                                             rebuildCommentsPanel(true);
                                             buttonRefresh.Enabled = enableRefresh;
                                         }));
        }

        private void runRefreshThread()
        {
            Thread worker = new Thread(new ThreadStart(delegate
                       {
                           try
                           {
                               setInfoStatus("Retrieving issue details...");
                               issue = facade.getIssue(issue.Server, issue.Key);
                               setInfoStatus("Issue details retrieved");
                           }
                           catch (Exception e)
                           {
                               setErrorStatus("Failed to retrieve issue details", e);
                           }
                           rebuildAllPanels(true);
                       }));
            worker.Start();
        }

        private void setErrorStatus(string txt, Exception e)
        {
            Invoke(new MethodInvoker(delegate
            {
                jiraStatus.BackColor = Color.LightPink;
                Exception inner = e.InnerException;
                jiraStatus.Text = "[" + issue.Key + "] " + txt + ": " + (inner != null ? inner.Message : e.Message);
            }));
        }

        private void setInfoStatus(string txt)
        {
            Invoke(new MethodInvoker(delegate
            {
                jiraStatus.BackColor = Color.Transparent;
                jiraStatus.Text = "[" + issue.Key + "] " + txt;
            }));
        }

        private string createCommentsHtml(bool expanded)
        {
            StringBuilder sb = new StringBuilder();

            sb.Append("<html>\n<head>\n")
                .Append(Properties.Resources.comments_css)
                .Append(Properties.Resources.toggler_javascript)
                .Append("</head>\n<body>\n");

            for (int i = 0; i < issue.Comments.Count; ++i)
            {
                sb.Append("<div class=\"comment_header\">")
                    .Append("<div class=\"author\">").Append(issue.Comments[i].Author)
                    .Append(" <span class=\"date\">").Append(issue.Comments[i].Created).Append("</span></div>")
                    .Append("<a href=\"javascript:toggle('")
                    .Append(i).Append("', '").Append(i).Append("control');\"><div class=\"toggler\" id=\"")
                    .Append(i).Append("control\">").Append(expanded ? "collapse" : "expand").Append("</div></a></div>\n");

                sb.Append("<div class=\"comment_body\" style=\"display:")
                    .Append(expanded ? "block" : "none").Append(";\" id=\"").Append(i).Append("\">")
                    .Append(issue.Comments[i].Body).Append("</div>\n");
            }

            sb.Append("</body></html>");

            return sb.ToString();
        }

        private string createDescriptionHtml()
        {
            StringBuilder sb = new StringBuilder();

            sb.Append("<html>\n<head>\n").Append(Properties.Resources.summary_and_description_css)
                .Append("\n</head>\n<body class=\"description\">\n")
                .Append(issue.Description)
                .Append("\n</body>\n</html>\n");

            return sb.ToString();            
        }

        private string createSummaryHtml()
        {
            StringBuilder sb = new StringBuilder();

            sb.Append("<html>\n<head>\n").Append(Properties.Resources.summary_and_description_css)
                .Append("\n</head>\n<body>\n<table class=\"summary\">\n")
                .Append("<tr><td><b>Type</b></td><td>")
                .Append("<img alt=\"\" src=\"").Append(issue.IssueTypeIconUrl).Append("\"/>").Append(issue.IssueType).Append("</td></tr>\n")
                .Append("<tr><td><b>Status</b></td><td>")
                .Append("<img alt=\"\" src=\"").Append(issue.StatusIconUrl).Append("\"/>").Append(issue.Status).Append("</td></tr>\n")
                .Append("<tr><td><b>Priority</b></td><td>")
                .Append("<img alt=\"\" src=\"").Append(issue.PriorityIconUrl).Append("\"/>").Append(issue.Priority).Append("</td></tr>\n")
                .Append("<tr><td><b>Assignee</b></td><td>")
                .Append(issue.Assignee).Append("</td></tr>\n")
                .Append("<tr><td><b>Reporter</b></td><td>")
                .Append(issue.Reporter).Append("</td></tr>\n")
                .Append("<tr><td><b>Resolution</b></td><td>")
                .Append(issue.Resolution).Append("</td></tr>\n")
                .Append("<tr><td><b>Created</b></td><td>")
                .Append(issue.CreationDate).Append("</td></tr>\n")
                .Append("<tr><td><b>Updated</b></td><td>")
                .Append(issue.UpdateDate).Append("</td></tr>\n")
                .Append("<tr><td><b>Affects Version</b></td><td>")
                .Append("TODO").Append("</td></tr>\n")
                .Append("<tr><td><b>Fix Version</b></td><td>")
                .Append("TODO").Append("</td></tr>\n")
                .Append("<tr><td><b>Component</b></td><td>")
                .Append("TODO").Append("</td></tr>\n")
                .Append("<tr><td><b>Original Estimate</b></td><td>")
                .Append(issue.OriginalEstimate ?? "None").Append("</td></tr>\n")
                .Append("<tr><td><b>Remaining Estimate</b></td><td>")
                .Append(issue.RemainingEstimate ?? "None").Append("</td></tr>\n")
                .Append("<tr><td><b>Time Spent</b></td><td>")
                .Append(issue.TimeSpent ?? "None").Append("</td></tr>\n")
                .Append("\n</table>\n</body>\n</html>\n");

            return sb.ToString();
        }

        private void rebuildDescriptionPanel()
        {
            issueDescriptionLoaded = false;
            issueDescription.DocumentText = createDescriptionHtml();
        }

        private void rebuildSummaryPanel()
        {
            issueSummaryLoaded = false;
            issueSummary.DocumentText = createSummaryHtml();
        }

        private void rebuildCommentsPanel(bool expanded)
        {
            issueCommentsLoaded = false;
            issueComments.DocumentText = createCommentsHtml(expanded);
        }

        private void buttonAddComment_Click(object sender, EventArgs e)
        {
            NewIssueComment dlg = new NewIssueComment();
            dlg.ShowDialog();
            if (dlg.DialogResult != DialogResult.OK) return;

            Thread addCommentThread = new Thread(new ThreadStart(delegate
                         {
                             try
                             {
                                 setInfoStatus("Adding comment to issue...");
                                 facade.addComment(issue, dlg.CommentBody);
                                 setInfoStatus("Comment added, refreshing view...");
                                 runRefreshThread();
                             }
                             catch (Exception ex)
                             {
                                 setErrorStatus("Adding comment failed", ex);
                             }
                         }));
            addCommentThread.Start();
        }

        private void buttonExpandAll_Click(object sender, EventArgs e)
        {
            rebuildCommentsPanel(true);
        }

        private void buttonCollapseAll_Click(object sender, EventArgs e)
        {
            rebuildCommentsPanel(false);
        }

        private void buttonRefresh_Click(object sender, EventArgs e)
        {
            buttonRefresh.Enabled = false;
            runRefreshThread();
        }

        private void buttonClose_Click(object sender, EventArgs e)
        {
            tabWindow.TabPages.Remove(myTab);
            if (tabWindow.TabPages.Count == 0)
            {
                IssueDetailsWindow.Instance.WindowInstance.Visible = false;
            }
        }

        private void issueSummary_DocumentCompleted(object sender, WebBrowserDocumentCompletedEventArgs e)
        {
            issueSummaryLoaded = true;
        }

        private void issueDescription_DocumentCompleted(object sender, WebBrowserDocumentCompletedEventArgs e)
        {
            issueDescriptionLoaded = true;
        }

        private void issueComments_DocumentCompleted(object sender, WebBrowserDocumentCompletedEventArgs e)
        {
            issueCommentsLoaded = true;
// ReSharper disable PossibleNullReferenceException
            issueComments.Document.Body.ScrollTop = A_LOT;
// ReSharper restore PossibleNullReferenceException
        }

        private void issueComments_Navigating(object sender, WebBrowserNavigatingEventArgs e)
        {
            if (!issueCommentsLoaded) return;
            if (e.Url.ToString().StartsWith("javascript:toggle(")) return;
            navigate(e);
        }

        private void issueDescription_Navigating(object sender, WebBrowserNavigatingEventArgs e)
        {
            if (!issueDescriptionLoaded) return;
            navigate(e);
        }

        private void issueSummary_Navigating(object sender, WebBrowserNavigatingEventArgs e)
        {
            if (!issueSummaryLoaded) return;
            navigate(e);
        }

        private static void navigate(WebBrowserNavigatingEventArgs e)
        {
            string url = e.Url.ToString();
            Process.Start(url);
            e.Cancel = true;
        }

        private void buttonViewInBrowser_Click(object sender, EventArgs e)
        {
            Process.Start(issue.Server.Url + "/browse/" + issue.Key);
        }
    }
}
