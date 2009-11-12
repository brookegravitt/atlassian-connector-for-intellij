using System;
using PaZu.api;
using PaZu.models;

namespace PaZu.ui
{
    class CustomFilterTreeNode : TreeNodeWithServer
    {
        private readonly JiraServer server;
        private readonly CustomFilter filter;

        public CustomFilterTreeNode(JiraServer server, CustomFilter filter) : base("Custom Filter")
        {
            this.server = server;
            this.filter = filter;

            Tag = filter;
        }

        public override JiraServer Server
        {
            get { return server; }
            set { throw new NotImplementedException(); }
        }
    }
}
