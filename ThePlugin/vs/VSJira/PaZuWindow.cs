using System;
using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;
using System.Threading;

using PaZu.api;
using PaZu.dialogs;
using PaZu.models;
using PaZu.ui;
using PaZu.ui.issues;

using Aga.Controls.Tree;
using Aga.Controls.Tree.NodeControls;

namespace PaZu
{
    public partial class PaZuWindow : UserControl, JiraIssueListModelListener
    {
        private readonly JiraServerFacade facade = new JiraServerFacade();

        TreeViewAdv issuesTree;

        private readonly JiraIssueListModelBuilder builder;

        private readonly JiraIssueListModel model = JiraIssueListModel.Instance;

        public PaZuWindow()
        {
            InitializeComponent();
            model.addListener(this);
            builder = new JiraIssueListModelBuilder(facade);
        }

        private readonly TreeColumn colKeyAndSummary = new TreeColumn();
        private readonly TreeColumn colStatus = new TreeColumn();
        private readonly TreeColumn colPriority = new TreeColumn();
        private readonly TreeColumn colUpdated = new TreeColumn();
        private readonly NodeIcon controlIssueTypeIcon = new NodeIcon();
        private readonly NodeTextBox controlKeyAndSummary = new NodeTextBox();
        private readonly NodeTextBox controlStatusText = new NodeTextBox();
        private readonly NodeIcon controlStatusIcon = new NodeIcon();
        private readonly NodeIcon controlPriorityIcon = new NodeIcon();
        private readonly NodeTextBox controlUpdated = new NodeTextBox();
        private const int MARGIN = 16;
        private const int STATUS_WIDTH = 100;
        private const int UPDATED_WIDTH = 300;
        private const int PRIORITY_WIDTH = 24;

        private void initIssuesTree()
        {
            issuesTree = new TreeViewAdv();
            issueTreeContainer.ContentPanel.Controls.Add(issuesTree);
            issuesTree.Dock = DockStyle.Fill;
            issuesTree.SelectionMode = TreeSelectionMode.Single;
            issuesTree.FullRowSelect = true;
            issuesTree.GridLineStyle = GridLineStyle.None;
            issuesTree.UseColumns = true;
            issuesTree.NodeMouseDoubleClick += issuesTree_NodeMouseDoubleClick;
            issuesTree.KeyPress += issuesTree_KeyPress;
            colKeyAndSummary.Header = "Summary";
            colStatus.Header = "Status";
            colPriority.Header = "P";
            colUpdated.Header = "Updated";

            int i = 0;
            controlIssueTypeIcon.ParentColumn = colKeyAndSummary;
            controlIssueTypeIcon.DataPropertyName = "IssueTypeIcon";
            controlIssueTypeIcon.LeftMargin = i++;
            
            controlKeyAndSummary.ParentColumn = colKeyAndSummary;
            controlKeyAndSummary.DataPropertyName = "KeyAndSummary";
            controlKeyAndSummary.Trimming = StringTrimming.EllipsisCharacter;
            controlKeyAndSummary.UseCompatibleTextRendering = true;
            controlKeyAndSummary.LeftMargin = i++;
            
            controlPriorityIcon.ParentColumn = colPriority;
            controlPriorityIcon.DataPropertyName = "PriorityIcon";
            controlPriorityIcon.LeftMargin = i++;
            
            controlStatusIcon.ParentColumn = colStatus;
            controlStatusIcon.DataPropertyName = "StatusIcon";
            controlStatusIcon.LeftMargin = i++;

            controlStatusText.ParentColumn = colStatus;
            controlStatusText.DataPropertyName = "StatusText";
            controlStatusText.Trimming = StringTrimming.EllipsisCharacter;
            controlStatusText.UseCompatibleTextRendering = true;
            controlStatusText.LeftMargin = i++;
            
            controlUpdated.ParentColumn = colUpdated;
            controlUpdated.DataPropertyName = "Updated";
            controlUpdated.Trimming = StringTrimming.EllipsisCharacter;
            controlUpdated.UseCompatibleTextRendering = true;
            controlUpdated.TextAlign = HorizontalAlignment.Right;
            controlUpdated.LeftMargin = i;

            issuesTree.Columns.Add(colKeyAndSummary);
            issuesTree.Columns.Add(colPriority);
            issuesTree.Columns.Add(colStatus);
            issuesTree.Columns.Add(colUpdated);

            issuesTree.NodeControls.Add(controlIssueTypeIcon);
            issuesTree.NodeControls.Add(controlKeyAndSummary);
            issuesTree.NodeControls.Add(controlPriorityIcon);
            issuesTree.NodeControls.Add(controlStatusIcon);
            issuesTree.NodeControls.Add(controlStatusText);
            issuesTree.NodeControls.Add(controlUpdated);

            setSummaryColumnWidth();

            colPriority.TextAlign = HorizontalAlignment.Left;
            colPriority.Width = PRIORITY_WIDTH;
            colPriority.MinColumnWidth = PRIORITY_WIDTH;
            colPriority.MaxColumnWidth = PRIORITY_WIDTH;
            colUpdated.Width = UPDATED_WIDTH;
            colUpdated.MinColumnWidth = UPDATED_WIDTH;
            colUpdated.MaxColumnWidth = UPDATED_WIDTH;
            colStatus.Width = STATUS_WIDTH;
            colStatus.MinColumnWidth = STATUS_WIDTH;
            colStatus.MaxColumnWidth = STATUS_WIDTH;
            colKeyAndSummary.TextAlign = HorizontalAlignment.Left;
            colPriority.TooltipText = "Priority";
            colStatus.TextAlign = HorizontalAlignment.Left;
            colPriority.TextAlign = HorizontalAlignment.Left;
            colUpdated.TextAlign = HorizontalAlignment.Right;

            jiraSplitter.Panel2.SizeChanged += issuesTree_SizeChanged;
        }

        private void issuesTree_KeyPress(object sender, KeyPressEventArgs e)
        {
            if (e.KeyChar != (char) Keys.Enter) return;
            TreeNodeAdv selectedNode = issuesTree.SelectedNode;
            if (selectedNode != null) openSelectedIssue(selectedNode.Tag as IssueNode);
        }

        private static void issuesTree_NodeMouseDoubleClick(object sender, TreeNodeAdvMouseEventArgs e)
        {
            IssueNode node = e.Node.Tag as IssueNode;
            openSelectedIssue(node);
        }

        private static void openSelectedIssue(IssueNode node)
        {
            if (node != null)
            {
                IssueDetailsWindow.Instance.openIssue(node.Issue);
            }
        }

        private void setSummaryColumnWidth()
        {
            // todo: well, this is lame. figure out how to handle filling first column to occupy all space in a propper manner
            int summaryWidth = jiraSplitter.Panel2.Width  
                               - PRIORITY_WIDTH - UPDATED_WIDTH - STATUS_WIDTH 
                               - SystemInformation.VerticalScrollBarWidth - MARGIN;
            if (summaryWidth < 0)
            {
                summaryWidth = 4 * PRIORITY_WIDTH;
            }
            colKeyAndSummary.Width = summaryWidth;
//            colKeyAndSummary.MinColumnWidth = summaryWidth;
//            colKeyAndSummary.MaxColumnWidth = summaryWidth;
        }

        void issuesTree_SizeChanged(object sender, EventArgs e)
        {
            setSummaryColumnWidth();
        }

        private void reloadKnownJiraServers()
        {
            filtersTree.Nodes.Clear();
            model.clear(true);

            getMoreIssues.Visible = false;

            // copy to local list so that we can reuse in our threads
            List<JiraServer> servers = new List<JiraServer>(JiraServerModel.Instance.getAllServers());
            if (servers.Count == 0)
            {
                setInfoStatus("No JIRA servers defined");
                return;
            }

            foreach (JiraServer server in servers)
            {
                filtersTree.Nodes.Add(new JiraServerTreeNode(server));
            }

            Thread metadataThread = new Thread(new ThreadStart(delegate
                {
                    try
                    {
                        foreach (JiraServer server in servers)
                        {
                            setInfoStatus("[" + server.Name + "] Loading project definitions...");
                            List<JiraProject> projects = facade.getProjects(server);
                            JiraServerCache.Instance.clearProjects();
                            foreach (JiraProject proj in projects)
                            {
                                JiraServerCache.Instance.addProject(server, proj);
                            }
                            setInfoStatus("[" + server.Name + "] Loading issue types...");
                            List<JiraNamedEntity> issueTypes = facade.getIssueTypes(server);
                            JiraServerCache.Instance.clearIssueTypes();
                            foreach (JiraNamedEntity type in issueTypes)
                            {
                                JiraServerCache.Instance.addIssueType(server, type);
                                ImageCache.Instance.getImage(type.IconUrl);
                            }
                            setInfoStatus("[" + server.Name + "] Loading issue priorities...");
                            List<JiraNamedEntity> priorities = facade.getPriorities(server);
                            JiraServerCache.Instance.clearPriorities();
                            foreach (JiraNamedEntity prio in priorities)
                            {
                                JiraServerCache.Instance.addPriority(server, prio);
                                ImageCache.Instance.getImage(prio.IconUrl);
                            }
                            setInfoStatus("[" + server.Name + "] Loading issue statuses...");
                            List<JiraNamedEntity> statuses = facade.getStatuses(server);
                            JiraServerCache.Instance.clearStatuses();
                            foreach (JiraNamedEntity status in statuses)
                            {
                                JiraServerCache.Instance.addStatus(server, status);
                                ImageCache.Instance.getImage(status.IconUrl);
                            }

                            setInfoStatus("[" + server.Name + "] Loading saved filters...");
                            List<JiraSavedFilter> filters = facade.getSavedFilters(server);
                            JiraServer jiraServer = server;
                            Invoke(new MethodInvoker(delegate
                                                         {
                                                             fillSavedFiltersForServer(jiraServer, filters);
                                                             setInfoStatus("Loaded saved filters for server " +
                                                                           jiraServer.Name);
                                                         }));
                        }
                        Invoke(new MethodInvoker(() => filtersTree.ExpandAll()));
                    }
                    catch (Exception e)
                    {
                        setErrorStatus("Failed to load server metadata", e);
                    }
                }));
            metadataThread.Start();
        }

        public void modelChanged()
        {
            Invoke(new MethodInvoker(delegate
                {
                    ICollection<JiraIssue> issues = model.Issues;

                    setInfoStatus("Loaded " + issues.Count + " issues");

                    ITreeModel treeModel = new FlatIssueTreeModel(issues);
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

        private void fillSavedFiltersForServer(JiraServer server, IEnumerable<JiraSavedFilter> filters)
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
            ProjectConfiguration dialog = new ProjectConfiguration(JiraServerModel.Instance, facade);
            dialog.ShowDialog();
            if (dialog.SomethingChanged)
            {
                // todo: only do this for changed servers - add server model listeners
                reloadKnownJiraServers();
            }
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
            Invoke(new MethodInvoker(initIssuesTree));
            reloadKnownJiraServers();
        }

        private void filtersTree_AfterSelect(object sender, TreeViewEventArgs e)
        {
            if (filtersTree.SelectedNode is JiraSavedFilterTreeNode)
            {
                JiraSavedFilterTreeNode node = (JiraSavedFilterTreeNode)filtersTree.SelectedNode;
                setInfoStatus("Loading issues...");
                getMoreIssues.Visible = false;
                Thread issueLoadThread = 
                    new Thread(() => builder.rebuildModelWithSavedFilter(model, node.Server, node.Filter));
                issueLoadThread.Start();
            }
            else
            {
                model.clear(true);
            }
        }

        private void getMoreIssues_Click(object sender, EventArgs e)
        {
            JiraSavedFilterTreeNode node = (JiraSavedFilterTreeNode)filtersTree.SelectedNode;
            setInfoStatus("Loading issues...");
            getMoreIssues.Visible = false;
            Thread issueLoadThread = 
                new Thread(() => builder.updateModelWithSavedFilter(model, node.Server, node.Filter));
            issueLoadThread.Start();

        }

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
    }
}
