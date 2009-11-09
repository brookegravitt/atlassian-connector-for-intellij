using System.Windows.Forms;
using PaZu.api;

namespace PaZu.ui
{
    public abstract class TreeNodeWithServer : TreeNode
    {
        protected TreeNodeWithServer(string name) : base(name) {}
        public abstract JiraServer Server { get; set; }
    }
}