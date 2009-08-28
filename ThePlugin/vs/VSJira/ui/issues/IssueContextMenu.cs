using System;
using System.Threading;
using System.Windows.Forms;
using Aga.Controls.Tree;
using PaZu.api;

namespace PaZu.ui.issues
{
    public sealed class IssueContextMenu : ContextMenuStrip
    {
//        private JiraIssue issue;

        public IssueContextMenu(TreeViewAdv node)
        {

            Opened += issueContextMenuOpened;
        }

        void issueContextMenuOpened(object sender, EventArgs e)
        {
//            issue = node.Issue;

            Items.Add("1");
            Items.Add("2");

            Thread loaderThread = new Thread(addIssueActionItems);
            loaderThread.Start();
        }

        private void addIssueActionItems()
        {
            Thread.Sleep(2000);
            Invoke(new MethodInvoker(delegate
                         {
                             Items.Add("dynamic 1");
                             Items.Add("dynamic 2");
                         }));
        }
    }
}
