using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using PaZu.api;
using PaZu.models;

namespace PaZu.dialogs
{
    public partial class AddOrEditJiraServer : Form
    {
        private static int invocations = 0;

        private bool editing;

        private JiraServer server;
        private JiraServerModel jiraServerModel;

        public AddOrEditJiraServer(JiraServerModel jiraServerModel, JiraServer server)
        {
            InitializeComponent();

            editing = server != null;

            this.server = new JiraServer(server);
            this.jiraServerModel = jiraServerModel;

            Text = editing ? "Edit JIRA Server" : "Add JIRA Server";
            buttonAddOrEdit.Text = editing ? "Apply Changes" : "Add Server";

            if (editing)
            {
                name.Text = server.Name;
                name.Enabled = false;

                url.Text = server.Url;
                user.Text = server.UserName;
                password.Text = server.Password;
            }
            else
            {
                ++invocations;
                name.Text = "JIRA Server #" + invocations;
                buttonAddOrEdit.Enabled = false;
            }
        }

        private void buttonAddOrEdit_Click(object sender, EventArgs e)
        {
            server.Name = name.Text;
            server.Url = url.Text;
            server.UserName = user.Text;
            server.Password = user.Text;

            DialogResult = DialogResult.OK;
            Close();
        }

        private void name_TextChanged(object sender, EventArgs e)
        {
            checkIfValid();
        }

        private void url_TextChanged(object sender, EventArgs e)
        {
            checkIfValid();
        }

        private void user_TextChanged(object sender, EventArgs e)
        {
            checkIfValid();
        }

        private void checkIfValid()
        {
            buttonAddOrEdit.Enabled = name.TextLength > 0 && url.TextLength > 0 && user.TextLength > 0;
        }

        public JiraServer Server { get { return server; } }
    }
}
