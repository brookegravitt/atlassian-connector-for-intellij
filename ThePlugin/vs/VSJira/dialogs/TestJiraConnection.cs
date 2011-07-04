using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using System.Threading;

using PaZu.api;
using PaZu.api.soap;

namespace PaZu.dialogs
{
    public partial class TestJiraConnection : Form
    {
        private JiraServer server;
        private JiraServerFacade facade;

        private bool testInProgress = true;
        private Thread worker;

        public TestJiraConnection(JiraServerFacade facade, JiraServer server)
        {
            InitializeComponent();

            this.server = server;
            this.facade = facade;

            status.Text = "Testing connection to server " + server.Name + ", please wait...";
            buttonClose.Text = "Cancel";

            worker = new Thread(new ThreadStart(delegate
            {
                string result = "Success!!!";
                try
                {
                    facade.login(server);
                }
                catch (SoapSession.LoginException e)
                {
                    result = e.InnerException.Message;
                }
                Invoke(new MethodInvoker(delegate
                {
                    stopTest(result);
                }));
            }));

            worker.Start();
        }

        private void buttonClose_Click(object sender, EventArgs e)
        {
            if (!testInProgress)
            {
                Close();
            }
            else
            {
                // too brutal?
                worker.Abort();
                stopTest("Test aborted");
            }
        }

        private void stopTest(string text)
        {
            testInProgress = false;
            status.Text = text;
            progress.Visible = false;
            buttonClose.Text = "Close";
        }
    }
}
