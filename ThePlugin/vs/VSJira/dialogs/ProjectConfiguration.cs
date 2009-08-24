using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using PaZu.api;
using PaZu.models;

namespace PaZu.dialogs
{
    public partial class ProjectConfiguration : Form
    {
        private TreeNode jiraRoot = new TreeNode("JIRA Servers");
        private TreeNode bambooRoot = new TreeNode("Bamboo Servers");
        private TreeNode crucibleRoot = new TreeNode("Crucible Servers");
        private TreeNode fisheyeRoot = new TreeNode("Fisheye Servers");

        private JiraServerModel jiraServerModel;

        private class JiraServerTreeNode : TreeNode
        {
            private JiraServer server;

            public JiraServerTreeNode(JiraServer server)
                : base(server.Name)
            {
                this.server = server;
            }

            public JiraServer Server 
                { 
                    get { return server; } 
                    set { server = value; Text = server.Name; }
                }
        }

        public ProjectConfiguration(JiraServerModel jiraServerModel)
        {
            InitializeComponent();

            this.jiraServerModel = jiraServerModel;

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
            if (jiraServerSelected)
            {
                serverDetails.Text = createServerSummaryText(((JiraServerTreeNode)serverTree.SelectedNode).Server);
            }
            else
            {
                serverDetails.Text = "";
            }
        }

        private string createServerSummaryText(JiraServer server)
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
            if (result == DialogResult.OK)
            {
                jiraServerModel.addServer(dialog.Server);
                JiraServerTreeNode newNode = new JiraServerTreeNode(dialog.Server);
                jiraRoot.Nodes.Add(newNode);
                serverTree.ExpandAll();
                serverTree.SelectedNode = newNode;
            }
        }

        private void buttonEdit_Click(object sender, EventArgs e)
        {
            JiraServerTreeNode selectedNode = (JiraServerTreeNode) serverTree.SelectedNode;
            AddOrEditJiraServer dialog = new AddOrEditJiraServer(jiraServerModel, selectedNode.Server);
            DialogResult result = dialog.ShowDialog();
            if (result == DialogResult.OK)
            {
                jiraServerModel.removeServer(selectedNode.Server.GUID);
                jiraServerModel.addServer(dialog.Server);
                selectedNode.Server = dialog.Server;
                serverTree.ExpandAll();
                serverDetails.Text = createServerSummaryText(selectedNode.Server);
                serverTree.SelectedNode = selectedNode;
            }
        }

        private void buttonDelete_Click(object sender, EventArgs e)
        {
            JiraServerTreeNode selectedNode = (JiraServerTreeNode)serverTree.SelectedNode;
            jiraServerModel.removeServer(selectedNode.Server.GUID);
            selectedNode.Remove();
            serverTree.ExpandAll();
            serverDetails.Text = "";
        }
    }
}
