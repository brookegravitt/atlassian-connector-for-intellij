using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Data;
using System.Text;
using System.Windows.Forms;
using PaZu.api;

namespace PaZu
{
    public partial class JiraWindow : UserControl
    {
    //    private JiraServerFacade facade = new JiraServerFacade();
    //    private JiraServer server;

    //    private BindingSource issueSource = new BindingSource(); 

        public JiraWindow()
        {
            InitializeComponent();
    //        setupIssueTable();
    //        url.Text = "https://studio.atlassian.com";
    //        statusMessage.Text = "Login to a JIRA server";
        }

        private void buttonProjectProperties_Click(object sender, EventArgs e)
        {
            new ProjectConfiguration(JiraServerModel.Instance).ShowDialog();
        }

        private void buttonAbout_Click(object sender, EventArgs e)
        {
            new About().ShowDialog();
        }

    //    private void JIRAWindow_Load(object sender, EventArgs e)
    //    {
    //        login.Enabled = false;
    //    }

    //    private class SavedFilterMenuEntry : ToolStripButton
    //    {
    //        public JiraSavedFilter filter;

    //        public SavedFilterMenuEntry(JiraSavedFilter f): base(f.Name)
    //        {
    //            DisplayStyle = ToolStripItemDisplayStyle.Text;
    //            filter = f;
    //        }
    //    }

    //    private void projects_SelectedIndexChanged(object sender, EventArgs e)
    //    {
    //        getSavedFilters();
    //    }

    //    private void savedFilters_SelectedIndexChanged(object sender, EventArgs e)
    //    {
    //    }

    //    private void url_TextChanged(object sender, EventArgs e)
    //    {
    //        updateLoginButton();
    //    }

    //    private void login_Click(object sender, EventArgs e)
    //    {
    //        server = new JiraServer(url.Text, userName.Text, password.Text);
    //        getSavedFilters();
    //        statusMessage.Text = "Select saved filter from the menu";
    //        //getProjects();
    //    }

    //    private void userName_TextChanged(object sender, EventArgs e)
    //    {
    //        updateLoginButton();
    //    }

    //    private void updateLoginButton()
    //    {
    //        login.Enabled = (url.Text.Length > 0) && (userName.Text.Length > 0);
    //    }

    //    private void savedFiltersMenu_DropDownItemClicked(object sender, ToolStripItemClickedEventArgs e)
    //    {
    //        string tooltip = savedFiltersMenu.ToolTipText;
    //        savedFiltersMenu.Text = e.ClickedItem.Text;
    //        savedFiltersMenu.ToolTipText = tooltip;

    //        getFiteredIssues(((SavedFilterMenuEntry)e.ClickedItem).filter);
    //    }

    //    private void setupIssueTable()
    //    {
    //        issueTable.AutoGenerateColumns = false;
    //        issueTable.AutoSize = true;

    //        issueTable.DataSource = issueSource;

    //        DataGridViewColumn column = new DataGridViewTextBoxColumn();
    //        column.DataPropertyName = "Key";
    //        column.Name = "key";
    //        column.Width = 50;
    //        issueTable.Columns.Add(column);
    //        column = new DataGridViewTextBoxColumn();
    //        column.DataPropertyName = "Summary";
    //        column.Name = "summary";
    //        column.AutoSizeMode = DataGridViewAutoSizeColumnMode.Fill;
    //        issueTable.Columns.Add(column);
    //    }

    //    private void getProjects()
    //    {
    //        try
    //        {
    //            List<JiraProject> list = facade.getProjects(server);
    //            foreach (JiraProject p in list)
    //            {
    //                projects.Items.Add(p);
    //            }
    //            projects.SelectedIndex = 0;
    //        }
    //        catch (Exception ex)
    //        {
    //            MessageBox.Show(ex.Message);
    //        }
    //    }

    //    private void getSavedFilters()
    //    {
    //        try
    //        {
    //            List<JiraSavedFilter> list = facade.getSavedFilters(server);
    //            foreach (JiraSavedFilter f in list)
    //            {
    //                savedFiltersMenu.DropDown.Items.Add(new SavedFilterMenuEntry(f));
    //            }
    //        }
    //        catch (Exception ex)
    //        {
    //            MessageBox.Show(ex.Message);
    //        }
    //    }

    //    private void getFiteredIssues(JiraSavedFilter f)
    //    {
    //        issueSource.Clear();
    //        List<JiraIssue> list = facade.getSavedFilterIssues(server, f);
    //        foreach (JiraIssue issue in list)
    //        {
    //            issueSource.Add(issue);
    //        }
    //        statusMessage.Text = "Loaded " + list.Count + " issues";
    //    }
    }
}
