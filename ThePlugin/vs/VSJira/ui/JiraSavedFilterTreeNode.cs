using System;
using PaZu.api;

namespace PaZu.ui
{
    class JiraSavedFilterTreeNode : TreeNodeWithServer
    {
        private readonly JiraServer server;

        public JiraSavedFilterTreeNode(JiraServer server, JiraSavedFilter filter)
            : base(filter.Name)
        {
            this.server = server;
            Filter = filter;
        }

        public override JiraServer Server
        {
            get { return server; }
            set { throw new NotImplementedException(); }
        }

        public JiraSavedFilter Filter { get; private set; }
    }
}
