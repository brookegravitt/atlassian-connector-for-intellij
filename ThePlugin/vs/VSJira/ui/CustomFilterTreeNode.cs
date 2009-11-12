using System;
using PaZu.api;
using PaZu.models;

namespace PaZu.ui
{
    class CustomFilterTreeNode : TreeNodeWithServer
    {
        private readonly JiraServer server;

        public CustomFilterTreeNode(JiraServer server, JiraCustomFilter filter, int imageIdx) : base("Custom Filter", imageIdx)
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
