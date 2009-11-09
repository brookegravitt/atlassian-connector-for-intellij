using System;
using PaZu.api;

namespace PaZu.ui
{
    class JiraSavedFilterTreeNode : TreeNodeWithServer
    {
        private JiraServer server;
        private JiraSavedFilter filter;

        public JiraSavedFilterTreeNode(JiraServer server, JiraSavedFilter filter)
            : base(filter.Name)
        {
            this.server = server;
            this.filter = filter;
        }

        public override JiraServer Server
        {
            get { return server; }
            set { throw new NotImplementedException(); }
        }

        public JiraSavedFilter Filter
        {
            get { return filter; }
        }
    }
}
