using System;
using System.Diagnostics;
using System.Text.RegularExpressions;
using System.Threading;
using System.Web;
using System.Windows.Forms;
using PaZu.api;
using PaZu.models;
using PaZu.ui;

namespace PaZu.dialogs
{
    public sealed partial class SearchIssue : Form
    {
        private static readonly Regex ISSUE_REGEX = new Regex(@"([a-zA-Z]+-\d+)");

        public JiraServer Server { get; set; }
        public JiraIssueListModel Model { get; set; }
        public StatusLabel Status { get; set; }

        public SearchIssue(JiraServer server, JiraIssueListModel model, StatusLabel status)
        {
            Server = server;
            Model = model;
            Status = status;
            InitializeComponent();

            Text = "Search issue on server \"" + server.Name + "\"";

            buttonOk.Enabled = false;
            StartPosition = FormStartPosition.CenterParent;
        }

        private void textQueryString_TextChanged(object sender, EventArgs e)
        {
            buttonOk.Enabled = textQueryString.Text.Length > 0;
        }

        private void buttonOk_Click(object sender, EventArgs e)
        {
            executeSearchAndClose();
        }

        private void textQueryString_KeyPress(object sender, KeyPressEventArgs e)
        {
            if (e.KeyChar != (char)Keys.Enter) return;
            executeSearchAndClose();
        }

        private void fetchAndOpenIssue(string key)
        {
            textQueryString.Enabled = false;
            buttonOk.Enabled = false;
            buttonCancel.Enabled = false;
            Thread runner = new Thread(new ThreadStart(delegate
               {
                   try
                   {
                       Status.setInfo("Fetching issue " + key + "...");
                       JiraIssue issue = JiraServerFacade.Instance.getIssue(Server, key);
                       if (issue != null)
                       {
                           Status.setInfo("Issue " + key + " found");
                           Invoke(new MethodInvoker(delegate
                                                        {
                                                            Close();
                                                            IssueDetailsWindow.Instance.openIssue(issue);
                                                        }));
                       }
                   }
                   catch (Exception ex)
                   {
                       Status.setError("Failed to find issue " + key, ex);
                       Invoke(new MethodInvoker(delegate
                                                    {
                                                        MessageBox.Show(
                                                            "Unable to find issue " + key + " on server \"" +
                                                            Server.Name + "\"" + "\n\n" +
                                                            ex.Message, "Error");
                                                        Close();
                                                    }));
                   }
               }));
            runner.Start();
        }

        private void executeSearchAndClose()
        {
            string query = textQueryString.Text.Trim();
            if (query.Length == 0) return;

            if (ISSUE_REGEX.IsMatch(query))
            {
                JiraIssue foundIssue = null;
                foreach (JiraIssue issue in Model.Issues)
                {
                    if (!issue.Key.Equals(query) || !issue.Server.Url.Equals(Server.Url)) continue;
                    foundIssue = issue;
                    break;
                }

                if (foundIssue == null)
                {
                    string key = query.ToUpper();
                    fetchAndOpenIssue(key);
                    return;
                }
                IssueDetailsWindow.Instance.openIssue(foundIssue);
            }
            else
            {
                string url = Server.Url + "/secure/QuickSearch.jspa?searchString=" + HttpUtility.UrlEncode(query);
                Process.Start(url);
            }
            Close();
        }
    }
}
