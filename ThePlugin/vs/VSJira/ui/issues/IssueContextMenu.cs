using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Diagnostics;
using System.Threading;
using System.Windows.Forms;
using Aga.Controls.Tree;
using PaZu.api;
using PaZu.models;

namespace PaZu.ui.issues
{
    public sealed class IssueContextMenu : ContextMenuStrip
    {
        private readonly TreeViewAdv tree;
        private readonly ToolStripMenuItem[] items;
        private JiraIssue issue;

        public IssueContextMenu(TreeViewAdv tree, ToolStripMenuItem[] items)
        {
            this.tree = tree;
            this.items = items;

            Items.Add("dummy");

            Opened += issueContextMenuOpened;
            Opening += issueContextMenuOpening;
        }

        void issueContextMenuOpening(object sender, CancelEventArgs e)
        {
            TreeNodeAdv selected = tree.SelectedNode;
            if (selected == null || !(selected.Tag is IssueNode))
            {
                e.Cancel = true;
                return;
            }
            issue = ((IssueNode) selected.Tag).Issue;
        }
        
        void issueContextMenuOpened(object sender, EventArgs e)
        {
            Items.Clear();

            Items.AddRange(items);

            Thread loaderThread = new Thread(addIssueActionItems);
            loaderThread.Start();
        }

        private void addIssueActionItems()
        {
            List<JiraNamedEntity> actions = null;
            try
            {
                actions = JiraServerFacade.Instance.getActionsForIssue(issue);
            }
            catch (Exception)
            {
                // todo - report in the status bar
            }
            if (actions == null || actions.Count == 0) return;

            Invoke(new MethodInvoker(delegate
                     {
                         Items.Add(new ToolStripSeparator());
                         foreach (JiraNamedEntity action in actions)
                         {
                             JiraNamedEntity actionCopy = action;
                             ToolStripMenuItem item = new ToolStripMenuItem(
                                 action.Name, null, new EventHandler(delegate { runAction(actionCopy); }));
                             Items.Add(item);
                         }
                     }));
        }

        private void runAction(JiraNamedEntity action)
        {
            Thread runner = new Thread(new ThreadStart(delegate
               {
                   try
                   {
                       List<JiraField> fields = JiraServerFacade.Instance.getFieldsForAction(issue, action.Id);
                       if (fields == null || fields.Count == 0)
                       {
                           runActionLocally(action);
                       }
                       else
                       {
                           Process.Start(issue.Server.Url
                               + "/secure/WorkflowUIDispatcher.jspa?id=" + issue.Id
                               + "&action=" + action.Id);
                       }
                   }
                   catch (Exception e)
                   {
                       // todo - report in the status bar 
                   }
               }));
            runner.Start();
        }

        private void runActionLocally(JiraNamedEntity action)
        {
            JiraServerFacade.Instance.runIssueActionWithoutParams(issue, action);
            JiraIssue newIssue = JiraServerFacade.Instance.getIssue(issue.Server, issue.Key);
            Invoke(new MethodInvoker(() => ((FlatIssueTreeModel) tree.Model).updateIssue(newIssue)));
        }
    }
}
