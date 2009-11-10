using System;
using System.ComponentModel;
using System.Diagnostics;
using System.Windows.Forms;
using PaZu.api;
using PaZu.dialogs;
using PaZu.models;

namespace PaZu.ui.issues
{
    public sealed class FilterContextMenu : ContextMenuStrip
    {
        private readonly JiraServer server;
        private readonly CustomFilter filter;

        private readonly ToolStripMenuItem[] items;

        public FilterContextMenu(JiraServer server, CustomFilter filter)
        {
            this.server = server;
            this.filter = filter;

            items = new[]
                {
                    new ToolStripMenuItem("Edit Filter", Properties.Resources.edit_in_browser, new EventHandler(editFilter)), 
                    new ToolStripMenuItem("View Filter in Browser", Properties.Resources.view_in_browser, new EventHandler(browseFilter)), 
                };

            Items.Add("dummy");

            Opened += filterContextMenuOpened;
        }

        void filterContextMenuOpened(object sender, EventArgs e)
        {
            Items.Clear();
            Items.AddRange(items);
        }

        private void browseFilter(object sender, EventArgs e)
        {
            string url = server.Url;
            Process.Start(url);
        }

        private void editFilter(object sender, EventArgs e)
        {
            EditCustomFilter ecf = new EditCustomFilter(server, filter);
            ecf.ShowDialog();
        }
    }
}
