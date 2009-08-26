using System.Windows.Forms;
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
                issueTabs.TabPages.Add(key, issue.Key);
            }
            issueTabs.SelectTab(key);
        }

        private static string getIssueTabKey(JiraIssue issue)
        {
            return issue.Server.GUID + issue.Key;
        }
    }
}
