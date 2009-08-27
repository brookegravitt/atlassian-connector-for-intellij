using System;
using System.Diagnostics;
using System.Windows.Forms;

namespace PaZu.dialogs
{
    public partial class About : Form
    {
        private bool pageLoaded;

        public About()
        {
            InitializeComponent();

            picture.Image = Properties.Resources.atlassian_538x235;
            browser.DocumentText = Properties.Resources.about_html;
            browser.ScrollBarsEnabled = false;
        }

        private void buttonClose_Click(object sender, EventArgs e)
        {
            Close();
        }

        private void browser_Navigating(object sender, WebBrowserNavigatingEventArgs e)
        {
            if (!pageLoaded) return;
            string url = e.Url.ToString();
            Process.Start(url);
            e.Cancel = true;
        }

        private void browser_DocumentCompleted(object sender, WebBrowserDocumentCompletedEventArgs e)
        {
            pageLoaded = true;
        }
    }
}
