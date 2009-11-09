using System.Windows.Forms;
using PaZu.api;

namespace PaZu.ui
{
    public class JiraServerTreeNode : TreeNodeWithServer
    {
        private JiraServer server;

        public JiraServerTreeNode(JiraServer server)
            : base(server.Name)
        {
            this.server = server;
        }

        public override JiraServer Server
        {
            get { return server; }
            set { server = value; Text = server.Name; }
        }
    }
}
