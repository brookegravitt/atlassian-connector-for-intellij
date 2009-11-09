using System;
using System.Collections.Generic;
using System.Diagnostics;
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
        private readonly JiraServerFacade facade = JiraServerFacade.Instance;

        TreeViewAdv issuesTree;

        private readonly JiraIssueListModelBuilder builder;

        private readonly JiraIssueListModel model = JiraIssueListModel.Instance;

        private readonly StatusLabel status;

        public PaZuWindow()
        {
            InitializeComponent();

            status = new StatusLabel(statusStrip, jiraStatus);

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
        private const string RETRIEVING_ISSUES_FAILED = "Retrieving issues failed";
        private const int MARGIN = 16;
        private const int STATUS_WIDTH = 100;
        private const int UPDATED_WIDTH = 300;
        private const int PRIORITY_WIDTH = 24;

        private void initIssuesTree()
        {
            issuesTree = new TreeViewAdv();

            ITreeModel treeModel = new FlatIssueTreeModel(model);
            issuesTree.Model = treeModel;

            issueTreeContainer.ContentPanel.Controls.Add(issuesTree);
            issuesTree.Dock = DockStyle.Fill;
            issuesTree.SelectionMode = TreeSelectionMode.Single;
            issuesTree.FullRowSelect = true;
            issuesTree.GridLineStyle = GridLineStyle.None;
            issuesTree.UseColumns = true;
            issuesTree.NodeMouseDoubleClick += issuesTree_NodeMouseDoubleClick;
            issuesTree.KeyPress += issuesTree_KeyPress;
            issuesTree.SelectionChanged += issuesTree_SelectionChanged;

            ToolStripMenuItem[] items = new[]
                {
                    new ToolStripMenuItem("Open in IDE", Properties.Resources.open_in_ide, new EventHandler(openIssue)), 
                    new ToolStripMenuItem("View in Browser", Properties.Resources.view_in_browser, new EventHandler(browseIssue)), 
                    new ToolStripMenuItem("Edit in Browser", Properties.Resources.edit_in_browser, new EventHandler(browseEditIssue)), 
                };
            IssueContextMenu strip = new IssueContextMenu(model, status, issuesTree, items);
            issuesTree.ContextMenuStrip = strip;
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

            updateIssueListButtons();
        }

        void issuesTree_SelectionChanged(object sender, EventArgs e)
        {
            Invoke(new MethodInvoker(updateIssueListButtons));
        }

        private void updateIssueListButtons()
        {
            bool issueSelected = (issuesTree.SelectedNode != null && issuesTree.SelectedNode.Tag is IssueNode);
            buttonViewInBrowser.Enabled = issueSelected;
            buttonEditInBrowser.Enabled = issueSelected;
            buttonOpen.Enabled = issueSelected;
            buttonRefresh.Enabled = filtersTree.SelectedNode != null &&
                                    filtersTree.SelectedNode is JiraSavedFilterTreeNode;
            buttonSearch.Enabled = filtersTree.SelectedNode != null && filtersTree.SelectedNode is TreeNodeWithServer;
        }

        private delegate void IssueAction(JiraIssue issue);

        private void runSelectedIssueAction(IssueAction action)
        {
            TreeNodeAdv node = issuesTree.SelectedNode;
            if (node == null || !(node.Tag is IssueNode)) return;
            action((node.Tag as IssueNode).Issue);
        }

        private void browseIssue(object sender, EventArgs e)
        {
            runSelectedIssueAction(browseSelectedIssue);
        }

        private static void browseSelectedIssue(JiraIssue issue)
        {
            Process.Start(issue.Server.Url + "/browse/" + issue.Key);
        }

        private void browseEditIssue(object sender, EventArgs e)
        {
            runSelectedIssueAction(browseEditSelectedIssue);
        }

        private static void browseEditSelectedIssue(JiraIssue issue)
        {
            Process.Start(issue.Server.Url + "/secure/EditIssue!default.jspa?id=" + issue.Id);
        }

        private void openIssue(object sender, EventArgs e)
        {
            runSelectedIssueAction(openSelectedIssue);
        }

        private void issuesTree_KeyPress(object sender, KeyPressEventArgs e)
        {
            if (e.KeyChar != (char) Keys.Enter) return;
            runSelectedIssueAction(openSelectedIssue);
        }

        private void issuesTree_NodeMouseDoubleClick(object sender, TreeNodeAdvMouseEventArgs e)
        {
            runSelectedIssueAction(openSelectedIssue);
        }

        private static void openSelectedIssue(JiraIssue issue)
        {
            IssueDetailsWindow.Instance.openIssue(issue);
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
                status.setInfo("No JIRA servers defined");
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
                            status.setInfo("[" + server.Name + "] Loading project definitions...");
                            List<JiraProject> projects = facade.getProjects(server);
                            JiraServerCache.Instance.clearProjects();
                            foreach (JiraProject proj in projects)
                            {
                                JiraServerCache.Instance.addProject(server, proj);
                            }
                            status.setInfo("[" + server.Name + "] Loading issue types...");
                            List<JiraNamedEntity> issueTypes = facade.getIssueTypes(server);
                            JiraServerCache.Instance.clearIssueTypes();
                            foreach (JiraNamedEntity type in issueTypes)
                            {
                                JiraServerCache.Instance.addIssueType(server, type);
                                ImageCache.Instance.getImage(type.IconUrl);
                            }
                            status.setInfo("[" + server.Name + "] Loading issue priorities...");
                            List<JiraNamedEntity> priorities = facade.getPriorities(server);
                            JiraServerCache.Instance.clearPriorities();
                            foreach (JiraNamedEntity prio in priorities)
                            {
                                JiraServerCache.Instance.addPriority(server, prio);
                                ImageCache.Instance.getImage(prio.IconUrl);
                            }
                            status.setInfo("[" + server.Name + "] Loading issue statuses...");
                            List<JiraNamedEntity> statuses = facade.getStatuses(server);
                            JiraServerCache.Instance.clearStatuses();
                            foreach (JiraNamedEntity s in statuses)
                            {
                                JiraServerCache.Instance.addStatus(server, s);
                                ImageCache.Instance.getImage(s.IconUrl);
                            }

                            status.setInfo("[" + server.Name + "] Loading saved filters...");
                            List<JiraSavedFilter> filters = facade.getSavedFilters(server);
                            JiraServer jiraServer = server;
                            Invoke(new MethodInvoker(delegate
                                                         {
                                                             fillSavedFiltersForServer(jiraServer, filters);
                                                             status.setInfo("Loaded saved filters for server " +
                                                                           jiraServer.Name);
                                                         }));
                        }
                        Invoke(new MethodInvoker(delegate
                                                     {
                                                         filtersTree.Nodes.Add(new RecentlyOpenIssuesTreeNode());
                                                         filtersTree.ExpandAll();
                                                     }));
                    }
                    catch (Exception e)
                    {
                        status.setError("Failed to load server metadata", e);
                    }
                }));
            metadataThread.Start();
        }

        public void modelChanged()
        {
            Invoke(new MethodInvoker(delegate
                {
                    ICollection<JiraIssue> issues = model.Issues;

                    if (!(filtersTree.SelectedNode is JiraSavedFilterTreeNode)) return;

                    status.setInfo("Loaded " + issues.Count + " issues");

//                    FlatIssueTreeModel oldModel = issuesTree.Model as FlatIssueTreeModel;
//                    if (oldModel != null)
//                    {
//                        oldModel.shutdown();
//                    }
//                    ITreeModel treeModel = new FlatIssueTreeModel(model);
//                    issuesTree.Model = treeModel;

                    getMoreIssues.Visible = true;

                    updateIssueListButtons();
                }));
        }

        public void issueChanged(JiraIssue issue)
        {
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
            dialog.ShowDialog(this);
            if (dialog.SomethingChanged)
            {
                // todo: only do this for changed servers - add server model listeners
                reloadKnownJiraServers();
            }
        }

        private void buttonAbout_Click(object sender, EventArgs e)
        {
            new About().ShowDialog(this);
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
            updateIssueListButtons();
            if (filtersTree.SelectedNode is JiraSavedFilterTreeNode || filtersTree.SelectedNode is RecentlyOpenIssuesTreeNode)
            {
                reloadIssues();
            }
            else
            {
                model.clear(true);
            }
        }

        private void reloadIssues()
        {
            JiraSavedFilterTreeNode savedFilterNode = filtersTree.SelectedNode as JiraSavedFilterTreeNode;
            RecentlyOpenIssuesTreeNode recentIssuesNode = filtersTree.SelectedNode as RecentlyOpenIssuesTreeNode;
            status.setInfo("Loading issues...");
            getMoreIssues.Visible = false;

            Thread issueLoadThread = null;
            
            if (savedFilterNode != null)
                issueLoadThread = reloadIssuesWithSavedFilter(savedFilterNode);
            else if (recentIssuesNode != null)
                issueLoadThread = reloadIssuesWithRecentlyViewedIssues();

            if (issueLoadThread != null) 
                issueLoadThread.Start();
        }

        private Thread reloadIssuesWithRecentlyViewedIssues()
        {
            return new Thread(new ThreadStart(delegate
            {
                try
                {
                    builder.rebuildModelWithRecentlyViewedIssues(model);
                }
                catch (Exception ex)
                {
                    status.setError(RETRIEVING_ISSUES_FAILED, ex);
                }
            }));
        }

        private Thread reloadIssuesWithSavedFilter(JiraSavedFilterTreeNode savedFilterNode)
        {
            return new Thread(new ThreadStart(delegate
                                  {
                                      try
                                      {
                                          builder.rebuildModelWithSavedFilter(model, savedFilterNode.Server, savedFilterNode.Filter);
                                      }
                                      catch (Exception ex)
                                      {
                                          status.setError(RETRIEVING_ISSUES_FAILED, ex);
                                      }
                                  }));
        }

        private void getMoreIssues_Click(object sender, EventArgs e)
        {
            JiraSavedFilterTreeNode node = (JiraSavedFilterTreeNode)filtersTree.SelectedNode;
            status.setInfo("Loading issues...");
            getMoreIssues.Visible = false;
            Thread issueLoadThread =
                new Thread(new ThreadStart(delegate
                       {
                           try
                           {
                               builder.updateModelWithSavedFilter(model, node.Server, node.Filter);
                           }
                           catch (Exception ex)
                           {
                               status.setError(RETRIEVING_ISSUES_FAILED, ex);
                           }
                       }));
            issueLoadThread.Start();
        }

        private void buttonOpen_Click(object sender, EventArgs e)
        {
            runSelectedIssueAction(openSelectedIssue);
        }

        private void buttonViewInBrowser_Click(object sender, EventArgs e)
        {
            runSelectedIssueAction(browseSelectedIssue);
        }

        private void buttonEditInBrowser_Click(object sender, EventArgs e)
        {
            runSelectedIssueAction(browseEditSelectedIssue);
        }

        private void buttonRefresh_Click(object sender, EventArgs e)
        {
            reloadIssues();
        }

        private void buttonSearch_Click(object sender, EventArgs e)
        {
            TreeNodeWithServer node = filtersTree.SelectedNode as TreeNodeWithServer;
            if (node == null) return;
            SearchIssue dlg = new SearchIssue(node.Server, model, status);
            dlg.ShowDialog(this);
        }
    }
}
