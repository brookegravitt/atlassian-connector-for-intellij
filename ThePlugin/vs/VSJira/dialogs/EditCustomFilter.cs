using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using PaZu.api;

namespace PaZu.dialogs
{
    public partial class EditCustomFilter : Form
    {
        private readonly JiraServer server;

        public EditCustomFilter(JiraServer server)
        {
            this.server = server;
            InitializeComponent();
            StartPosition = FormStartPosition.CenterParent;
        }

        private void buttonClose_Click(object sender, EventArgs e)
        {
            Close();
        }
    }
}
