using System;
using PaZu.api;
using PaZu.models;

namespace PaZu.ui
{
    class CustomFilterTreeNode : TreeNodeWithServer
    {
        private readonly JiraServer server;

        public CustomFilterTreeNode(JiraServer server, JiraCustomFilter filter) : base("Custom Filter")
        {
            this.server = server;
            Filter = filter;

            Tag = filter;
        }

        public JiraCustomFilter Filter { get; private set; }

        public override JiraServer Server
        {
            get { return server; }
            set { throw new NotImplementedException(); }
        }
    }
}
