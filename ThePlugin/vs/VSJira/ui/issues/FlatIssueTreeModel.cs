using System;
using System.Collections;
using System.Collections.Generic;
using Aga.Controls.Tree;

using PaZu.api;
using PaZu.models;

namespace PaZu.ui.issues
{
    class FlatIssueTreeModel : ITreeModel, JiraIssueListModelListener
    {
        private readonly JiraIssueListModel model;
        private readonly List<IssueNode> nodes = new List<IssueNode>();
        public FlatIssueTreeModel(JiraIssueListModel model)
        {
            this.model = model;
            fillModel(model.Issues);
            model.addListener(this);
        }

        public void shutdown()
        {
            model.removeListener(this);
        }

        private void fillModel(IEnumerable<JiraIssue> issues)
        {
            nodes.Clear();

            foreach (var issue in issues)
            {
                nodes.Add(new IssueNode(issue));
            }

            if (StructureChanged != null)
            {
                StructureChanged(this, new TreePathEventArgs(TreePath.Empty));
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

        public void modelChanged()
        {
            fillModel(model.Issues);
        }

        public void issueChanged(JiraIssue issue)
        {
            foreach (var node in nodes)
            {
                if (node.Issue.Id != issue.Id) continue;

                node.Issue = issue;
                if (NodesChanged != null)
                {
                    NodesChanged(this, new TreeModelEventArgs(TreePath.Empty, new object[] { node }));
                }
                
                return;
            }
        }

        public void updateIssue(JiraIssue issue)
        {
        }

        public event EventHandler<TreeModelEventArgs> NodesChanged;

        public event EventHandler<TreeModelEventArgs> NodesInserted;

        public event EventHandler<TreeModelEventArgs> NodesRemoved;

        public event EventHandler<TreePathEventArgs> StructureChanged;

        #endregion
    }
}
