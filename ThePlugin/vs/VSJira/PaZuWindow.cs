using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Data;
using System.Text;
using System.Windows.Forms;
using System.Threading;

using PaZu.api;
using PaZu.dialogs;
using PaZu.models;
using PaZu.ui;

using Aga.Controls.Tree;
using Aga.Controls.Tree.NodeControls;

namespace PaZu
{
    public partial class PaZuWindow : UserControl, JiraIssueListModelListener
    {
        private JiraServerFacade facade = new JiraServerFacade();

    //    private BindingSource issueSource = new BindingSource(); 

        TreeViewAdv issuesTree;

        JiraIssueListModelBuilder builder;

        private JiraIssueListModel model = JiraIssueListModel.Instance;

        public PaZuWindow()
        {
            InitializeComponent();
            model.addListener(this);
            builder = new JiraIssueListModelBuilder(facade);
    //        setupIssueTable();
        }

        private TreeColumn colKeyAndSummary = new TreeColumn();
        private TreeColumn colPriority = new TreeColumn();
        private NodeIcon controlIcon = new NodeIcon();
        private NodeTextBox controlKeyAndSummary = new NodeTextBox();
        private NodeTextBox controlPriority = new NodeTextBox();

        private class IssueNode : Node
        {
            private JiraIssue issue;

            public IssueNode(JiraIssue issue)
            {
                this.issue = issue;
            }

            Image Icon 
            { 
                get { return Properties.Resources.ide_plugin_161; }
                set { }
            }
            string KeyAndSummary 
            { 
                get { return issue.Key + " - " + issue.Summary; }
                set { }
            }
            string Priority 
            { 
                get { return "Not implemented"; }
                set { }
            }
        }

        private void initIssuesTree()
        {
            issuesTree = new TreeViewAdv();
            issueTreeContainer.ContentPanel.Controls.Add(issuesTree);
            issuesTree.Dock = System.Windows.Forms.DockStyle.Fill;
            issuesTree.SelectionMode = TreeSelectionMode.Single;
            issuesTree.FullRowSelect = true;
            issuesTree.GridLineStyle = GridLineStyle.None;
            issuesTree.UseColumns = true;
            colKeyAndSummary.Header = "Summary";
            colPriority.Header = "Priority";
            colPriority.TextAlign = HorizontalAlignment.Right;
            controlIcon.ParentColumn = colKeyAndSummary;
            controlIcon.DataPropertyName = "Icon";
            controlIcon.LeftMargin = 0;
            controlKeyAndSummary.ParentColumn = colKeyAndSummary;
            controlKeyAndSummary.DataPropertyName = "KeyAndSummary";
            controlKeyAndSummary.Trimming = StringTrimming.EllipsisCharacter;
            controlKeyAndSummary.UseCompatibleTextRendering = true;
            controlKeyAndSummary.LeftMargin = 1;
            controlPriority.ParentColumn = colPriority;
            controlPriority.DataPropertyName = "Priority";
            controlPriority.Trimming = StringTrimming.EllipsisCharacter;
            controlPriority.UseCompatibleTextRendering = true;
            controlPriority.LeftMargin = 2;

            issuesTree.Columns.Add(colKeyAndSummary);
            issuesTree.Columns.Add(colPriority);
            issuesTree.NodeControls.Add(controlIcon);
            issuesTree.NodeControls.Add(controlKeyAndSummary);
            issuesTree.NodeControls.Add(controlPriority);

            colKeyAndSummary.Width = issuesTree.ClientRectangle.Width - 100 - System.Windows.Forms.SystemInformation.VerticalScrollBarWidth;
            //colKeyAndSummary.MaxColumnWidth = issuesTree.ClientRectangle.Width - 100;
            //colKeyAndSummary.MinColumnWidth = issuesTree.ClientRectangle.Width - 100;
            colPriority.Width = 100;
            //colPriority.MaxColumnWidth = 100;
            //colPriority.MinColumnWidth = 100;
            issuesTree.ClientSizeChanged += new EventHandler(issuesTree_ClientSizeChanged);
        }

        void issuesTree_ClientSizeChanged(object sender, EventArgs e)
        {
            colKeyAndSummary.Width = issuesTree.ClientSize.Width - 100 - System.Windows.Forms.SystemInformation.VerticalScrollBarWidth;
            //colKeyAndSummary.MaxColumnWidth = issuesTree.ClientRectangle.Width - 100;
            //colKeyAndSummary.MinColumnWidth = issuesTree.ClientRectangle.Width - 100;
        }

        private void reloadKnownJiraServers()
        {
            filtersTree.Nodes.Clear();
            getMoreIssues.Visible = false;

            // copy to local list so that we can reuse in our threads
            List<JiraServer> servers = new List<JiraServer>(JiraServerModel.Instance.getAllServers());
            if (servers.Count == 0)
            {
                jiraStatus.Text = "No JIRA servers defined";
            }

            foreach (JiraServer server in servers)
            {
                filtersTree.Nodes.Add(new JiraServerTreeNode(server));
            }

            Thread savedFiltersThread = new Thread(new ThreadStart(delegate
                {
                    foreach (JiraServer server in servers)
                    {
                       setInfoStatus("Loading Saved Filters for server " + server.Name + "...");
                       try 
                        {
                            List<JiraSavedFilter> filters = facade.getSavedFilters(server);
                            Invoke(new MethodInvoker(delegate
                                {
                                    fillSavedFiltersForServer(server, filters);
                                    setInfoStatus("Loaded saved filters for server " + server.Name);
                                }));
                        }
                        catch (Exception e)
                        {
                            setErrorStatus("Failed to load saved filters", e);
                        }
                    }
                    Invoke(new MethodInvoker(delegate
                        {
                            filtersTree.ExpandAll();
                        }));
                }));
            savedFiltersThread.Start();
        }

        public void modelChanged()
        {
            Invoke(new MethodInvoker(delegate
                {
                    ICollection<JiraIssue> issues = model.Issues;

                    setInfoStatus("Loaded " + issues.Count + " issues");

                    TreeModel treeModel = new TreeModel();
                    foreach (JiraIssue i in issues)
                    {
                        treeModel.Nodes.Add(new IssueNode(i));
                    }
                    issuesTree.Model = treeModel;

                    getMoreIssues.Visible = true;
                }));
        }

        private void setErrorStatus(string txt, Exception e)
        {
            Invoke(new MethodInvoker(delegate
            {
                jiraStatus.BackColor = Color.LightPink;
                Exception inner = e.InnerException;
                jiraStatus.Text = txt + ": " + (inner != null ? inner.Message : e.Message);
            }));
        }

        private void setInfoStatus(string txt)
        {
            Invoke(new MethodInvoker(delegate
            {
                jiraStatus.BackColor = Color.Transparent;
                jiraStatus.Text = txt;
            }));
        }

        private void fillSavedFiltersForServer(JiraServer server, List<JiraSavedFilter> filters)
        {
            JiraServerTreeNode node = findNode(server);
            if (node == null)
            {
                return;
            }
            foreach (JiraSavedFilter filter in filters)
            {
                node.Nodes.Add(new JiraSavedFilterTreeNode(server, filter));
            }
            node.ExpandAll();
        }

        private JiraServerTreeNode findNode(JiraServer server)
        {
            foreach (TreeNode node in filtersTree.Nodes)
            {
                JiraServerTreeNode tn = (JiraServerTreeNode)node;
                if (tn.Server.GUID.Equals(server.GUID))
                {
                    return tn;
                }
            }
            return null;
        }

        private void buttonProjectProperties_Click(object sender, EventArgs e)
        {
            new ProjectConfiguration(JiraServerModel.Instance, facade).ShowDialog();

            // todo: only do this on model change - add server model listeners
            reloadKnownJiraServers();
        }

        private void buttonAbout_Click(object sender, EventArgs e)
        {
            new About().ShowDialog();
        }

        private void buttonRefreshAll_Click(object sender, EventArgs e)
        {
            reloadKnownJiraServers();
        }

        private void PaZuWindow_Load(object sender, EventArgs e)
        {
            initIssuesTree();
            reloadKnownJiraServers();
        }

        private void filtersTree_AfterSelect(object sender, TreeViewEventArgs e)
        {
            if (filtersTree.SelectedNode is JiraSavedFilterTreeNode)
            {
                JiraSavedFilterTreeNode node = (JiraSavedFilterTreeNode)filtersTree.SelectedNode;
                setInfoStatus("Loading issues...");
                getMoreIssues.Visible = false;
                Thread issueLoadThread = new Thread(new ThreadStart(delegate
                    {
                        builder.rebuildModelWithSavedFilter(model, node.Server, node.Filter);
                    }));
                issueLoadThread.Start();
            }
        }

        private void getMoreIssues_Click(object sender, EventArgs e)
        {
            JiraSavedFilterTreeNode node = (JiraSavedFilterTreeNode)filtersTree.SelectedNode;
            setInfoStatus("Loading issues...");
            getMoreIssues.Visible = false;
            Thread issueLoadThread = new Thread(new ThreadStart(delegate
            {
                builder.updateModelWithSavedFilter(model, node.Server, node.Filter);
            }));
            issueLoadThread.Start();

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
