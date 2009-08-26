using System;
using System.Collections.Generic;
using System.Text;
using System.Windows.Forms;
using PaZu.api;
using PaZu.models;
using PaZu.ui;

namespace PaZu.dialogs
{
    public partial class ProjectConfiguration : Form
    {
        private readonly TreeNode jiraRoot = new TreeNode("JIRA Servers");
        private readonly TreeNode bambooRoot = new TreeNode("Bamboo Servers");
        private readonly TreeNode crucibleRoot = new TreeNode("Crucible Servers");
        private readonly TreeNode fisheyeRoot = new TreeNode("Fisheye Servers");

        private readonly JiraServerModel jiraServerModel;
        private readonly JiraServerFacade facade;

        public bool SomethingChanged { get; private set; }

        public ProjectConfiguration(JiraServerModel jiraServerModel, JiraServerFacade facade)
        {
            InitializeComponent();

            this.jiraServerModel = jiraServerModel;
            this.facade = facade;

            ICollection<JiraServer> jiraServers = jiraServerModel.getAllServers();

            serverTree.Nodes.Add(jiraRoot);
            serverTree.Nodes.Add(bambooRoot);
            serverTree.Nodes.Add(crucibleRoot);
            serverTree.Nodes.Add(fisheyeRoot);

            foreach (JiraServer server in jiraServers)
            {
                jiraRoot.Nodes.Add(new JiraServerTreeNode(server));
            }

            serverTree.ExpandAll();
        }

        private void buttonClose_Click(object sender, EventArgs e)
        {
            Close();
        }

        private void serverTree_AfterSelect(object sender, TreeViewEventArgs e)
        {
            bool jiraRootSelected = serverTree.SelectedNode.Equals(jiraRoot);
            bool jiraServerSelected = serverTree.SelectedNode is JiraServerTreeNode;
            buttonAdd.Enabled = jiraRootSelected || jiraServerSelected;
            buttonEdit.Enabled = jiraServerSelected;
            buttonDelete.Enabled = jiraServerSelected;
            buttonTest.Enabled = jiraServerSelected;

            serverDetails.Text = jiraServerSelected 
                ? createServerSummaryText(((JiraServerTreeNode)serverTree.SelectedNode).Server) 
                : "";
        }

        private static string createServerSummaryText(JiraServer server)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("Name: ").Append(server.Name).Append("\r\n");
            sb.Append("URL: ").Append(server.Url).Append("\r\n");
            sb.Append("User Name: ").Append(server.UserName);

            return sb.ToString();
        }

        private void buttonAdd_Click(object sender, EventArgs e)
        {
            AddOrEditJiraServer dialog = new AddOrEditJiraServer(jiraServerModel, null);
            DialogResult result = dialog.ShowDialog();
            if (result != DialogResult.OK) return;
            jiraServerModel.addServer(dialog.Server);
            JiraServerTreeNode newNode = new JiraServerTreeNode(dialog.Server);
            jiraRoot.Nodes.Add(newNode);
            serverTree.ExpandAll();
            serverTree.SelectedNode = newNode;
            SomethingChanged = true;
        }

        private void buttonEdit_Click(object sender, EventArgs e)
        {
            JiraServerTreeNode selectedNode = (JiraServerTreeNode) serverTree.SelectedNode;
            AddOrEditJiraServer dialog = new AddOrEditJiraServer(jiraServerModel, selectedNode.Server);
            DialogResult result = dialog.ShowDialog();
            if (result != DialogResult.OK) return;
            jiraServerModel.removeServer(selectedNode.Server.GUID);
            jiraServerModel.addServer(dialog.Server);
            selectedNode.Server = dialog.Server;
            serverTree.ExpandAll();
            serverDetails.Text = createServerSummaryText(selectedNode.Server);
            serverTree.SelectedNode = selectedNode;
            SomethingChanged = true;
        }

        private void buttonDelete_Click(object sender, EventArgs e)
        {
            JiraServerTreeNode selectedNode = (JiraServerTreeNode)serverTree.SelectedNode;
            jiraServerModel.removeServer(selectedNode.Server.GUID);
            selectedNode.Remove();
            serverTree.ExpandAll();
            serverDetails.Text = "";
            SomethingChanged = true;
        }

        private void buttonTest_Click(object sender, EventArgs e)
        {
            JiraServerTreeNode selectedNode = (JiraServerTreeNode)serverTree.SelectedNode;
            new TestJiraConnection(facade, selectedNode.Server).ShowDialog();
        }
    }
}
