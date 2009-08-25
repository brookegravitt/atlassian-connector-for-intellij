using System;
using System.Collections.Generic;
using System.Text;
using System.Windows.Forms;
using PaZu.api;

namespace PaZu.ui
{
    class JiraSavedFilterTreeNode : TreeNode
    {
        private JiraServer server;
        private JiraSavedFilter filter;

        public JiraSavedFilterTreeNode(JiraServer server, JiraSavedFilter filter)
            : base(filter.Name)
        {
            this.server = server;
            this.filter = filter;
        }

        public JiraServer Server
        {
            get { return server; }
        }

        public JiraSavedFilter Filter
        {
            get { return filter; }
        }
    }
}
