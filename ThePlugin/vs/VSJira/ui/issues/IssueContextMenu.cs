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
        private readonly JiraIssueListModel model;
        private readonly StatusLabel status;
        private readonly TreeViewAdv tree;
        private readonly ToolStripMenuItem[] items;
        private JiraIssue issue;

        public IssueContextMenu(JiraIssueListModel model, StatusLabel status, TreeViewAdv tree, ToolStripMenuItem[] items)
        {
            this.model = model;
            this.status = status;
            this.tree = tree;
            this.items = items;

            Items.Add("dummy");

            Opened += issueContextMenuOpened;
            Opening += issueContextMenuOpening;
        }

        void issueContextMenuOpening(object sender, CancelEventArgs e)
        {
            var selected = tree.SelectedNode;
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
            catch (Exception e)
            {
                status.setError("Failed to retrieve issue actions", e);
            }
            if (actions == null || actions.Count == 0) return;

            Invoke(new MethodInvoker(delegate
                     {
                         Items.Add(new ToolStripSeparator());
                         foreach (var action in actions)
                         {
                             var actionCopy = action;
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
                       status.setInfo("Retrieveing fields for action \"" + action.Name + "\"...");
                       var fields = JiraServerFacade.Instance.getFieldsForAction(issue, action.Id);
                       if (fields == null || fields.Count == 0)
                       {
                           runActionLocally(action);
                       }
                       else
                       {
                           status.setInfo("Action \"" + action.Name 
                               + "\" requires input fields, opening action screen in the browser...");
                           Process.Start(issue.Server.Url
                               + "/secure/WorkflowUIDispatcher.jspa?id=" + issue.Id
                               + "&action=" + action.Id);
                       }
                   }
                   catch (Exception e)
                   {
                       status.setError("Failed to run action " + action.Name + " on issue " + issue.Key, e);
                   }
               }));
            runner.Start();
        }

        private void runActionLocally(JiraNamedEntity action)
        {
            status.setInfo("Running action \"" + action.Name + "\" on issue " + issue.Key + "...");
            JiraServerFacade.Instance.runIssueActionWithoutParams(issue, action);
            status.setInfo("Action \"" + action.Name + "\" successfully run on issue " + issue.Key);
            var newIssue = JiraServerFacade.Instance.getIssue(issue.Server, issue.Key);
            Invoke(new MethodInvoker(() => model.updateIssue(newIssue)));
        }
    }
}
