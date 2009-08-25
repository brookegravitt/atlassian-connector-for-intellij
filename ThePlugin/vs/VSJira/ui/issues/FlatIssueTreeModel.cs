using System;
using System.Collections.Generic;
using System.Text;

using Aga.Controls.Tree;

using PaZu.api;

namespace PaZu.ui.issues
{
    class FlatIssueTreeModel : ITreeModel
    {
        private List<IssueNode> nodes = new List<IssueNode>();
        public FlatIssueTreeModel(ICollection<JiraIssue> issues)
        {
            foreach (JiraIssue issue in issues)
            {
                nodes.Add(new IssueNode(issue));
            }
        }

        #region ITreeModel Members

        System.Collections.IEnumerable ITreeModel.GetChildren(TreePath treePath)
        {
            if (treePath.IsEmpty())
            {
                return nodes;
            }
            return null;
        }

        bool ITreeModel.IsLeaf(TreePath treePath)
        {
            return true;
        }

        public event EventHandler<TreeModelEventArgs> NodesChanged;

        public event EventHandler<TreeModelEventArgs> NodesInserted;

        public event EventHandler<TreeModelEventArgs> NodesRemoved;

        public event EventHandler<TreePathEventArgs> StructureChanged;

        #endregion
    }
}
