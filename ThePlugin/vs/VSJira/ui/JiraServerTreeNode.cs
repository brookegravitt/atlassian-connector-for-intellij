using System;
using System.Collections.Generic;
using System.Text;
using System.Windows.Forms;
using PaZu.api;

namespace PaZu.ui
{
    public class JiraServerTreeNode : TreeNode
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
}
