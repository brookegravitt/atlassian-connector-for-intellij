using System;
using System.Collections;
using System.Collections.Generic;
using Aga.Controls.Tree;

using PaZu.api;

namespace PaZu.ui.issues
{
    class FlatIssueTreeModel : ITreeModel
    {
        private readonly List<IssueNode> nodes = new List<IssueNode>();
        public FlatIssueTreeModel(IEnumerable<JiraIssue> issues)
        {
            foreach (JiraIssue issue in issues)
            {
                nodes.Add(new IssueNode(issue));
            }
        }

        #region ITreeModel Members

        IEnumerable ITreeModel.GetChildren(TreePath treePath)
        {
            return treePath.IsEmpty() ? nodes : null;
        }

        bool ITreeModel.IsLeaf(TreePath treePath)
        {
            return true;
        }

        public void updateIssue(JiraIssue issue)
        {
            foreach (IssueNode node in nodes)
            {
                if (node.Issue.Id != issue.Id) continue;

                node.Issue = issue;
                NodesChanged(this, new TreeModelEventArgs(TreePath.Empty, new object[] { node }));
                return;
            }    
        }

        public event EventHandler<TreeModelEventArgs> NodesChanged;

        public event EventHandler<TreeModelEventArgs> NodesInserted;

        public event EventHandler<TreeModelEventArgs> NodesRemoved;

        public event EventHandler<TreePathEventArgs> StructureChanged;

        #endregion
    }
}
