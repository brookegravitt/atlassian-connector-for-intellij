using System.Windows.Forms;

namespace PaZu.ui
{
    class RecentlyOpenIssuesTreeNode : TreeNode
    {
        public RecentlyOpenIssuesTreeNode(int imageIdx) : base("Recently Open Issues", imageIdx, imageIdx)
        {
        }
    }
}
