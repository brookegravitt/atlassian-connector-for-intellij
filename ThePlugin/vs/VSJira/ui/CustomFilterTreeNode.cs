using System;
using PaZu.api;

namespace PaZu.ui
{
    class CustomFilterTreeNode : TreeNodeWithServer
    {
        private readonly JiraServer server;

        public CustomFilterTreeNode(JiraServer server) : base("Custom Filter")
        {
            this.server = server;

            // todo: this will be the tooltip
            Tag = server.Name;
        }

        public override JiraServer Server
        {
            get { return server; }
            set { throw new NotImplementedException(); }
        }
    }
}
