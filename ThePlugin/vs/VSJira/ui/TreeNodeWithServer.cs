using System.Windows.Forms;
using PaZu.api;

namespace PaZu.ui
{
    public abstract class TreeNodeWithServer : TreeNode
    {
        protected TreeNodeWithServer(string name, int imageIdx) : base(name, imageIdx, imageIdx) {}
        public abstract JiraServer Server { get; set; }
    }
}