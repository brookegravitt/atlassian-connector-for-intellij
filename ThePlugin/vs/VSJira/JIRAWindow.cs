using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Data;
using System.Text;
using System.Windows.Forms;
using VSJira.JIRA;

namespace VSJira
{
    public partial class JiraWindow : UserControl
    {
        private JiraSoapServiceService service = new JiraSoapServiceService();
        private string token = "";
        private RemoteProject[] pTable;

        public JiraWindow()
        {
            InitializeComponent();
            url.Text = "https://studio.atlassian.com";
            projects.SelectedIndexChanged += new System.EventHandler(this.projects_SelectedIndexChanged);
        }

        private void JIRAWindow_Load(object sender, EventArgs e)
        {
            login.Enabled = false;
        }

        private void projects_SelectedIndexChanged(object sender, EventArgs e)
        {
            try
            {
                issueTypes.Items.Clear();
                RemoteIssueType[] types = service.getIssueTypesForProject(token, pTable[projects.SelectedIndex].id);
                foreach (RemoteIssueType t in types)
                {
                    issueTypes.Items.Add(t.name);
                }
                issueTypes.SelectedIndex = 0;
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
            }
        }

        private void url_TextChanged(object sender, EventArgs e)
        {
            updateLoginButton();
        }

        private void login_Click(object sender, EventArgs e)
        {
            service.Url = url.Text + "/rpc/soap/jirasoapservice-v2";
            try
            {
                token = service.login(userName.Text, password.Text);
                pTable = service.getProjectsNoSchemes(token);
                foreach (RemoteProject p in pTable)
                {
                    string txt = p.key + ": " + p.name;
                    projects.Items.Add(txt);
                }
                projects.SelectedIndex = 0;
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
            }
        }

        private void userName_TextChanged(object sender, EventArgs e)
        {
            updateLoginButton();
        }

        private void updateLoginButton()
        {
            login.Enabled = (url.Text.Length > 0) && (userName.Text.Length > 0);
        }
    }
}
