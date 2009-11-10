using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Threading;
using System.Windows.Forms;
using PaZu.api;
using PaZu.models;
using PaZu.ui;

namespace PaZu.dialogs
{
    public partial class EditCustomFilter : Form
    {
        private readonly JiraServer server;
        private readonly CustomFilter filter;

        public EditCustomFilter(JiraServer server, CustomFilter filter)
        {
            this.server = server;
            this.filter = filter;

            InitializeComponent();

            listViewIssueTypes.Columns.Add("Name", listViewIssueTypes.Width - 10, HorizontalAlignment.Left);

            StartPosition = FormStartPosition.CenterParent;

            foreach (JiraProject project in JiraServerCache.Instance.getProjects(server).Values)
            {
                listBoxProjects.Items.Add(project);
            }
            refillIssueTypes(null);
        }

        private void refillIssueTypes(ICollection<JiraNamedEntity> issueTypes)
        {
            listViewIssueTypes.Items.Clear();

            ImageList imageList = new ImageList();
            
            int i = 0;

            if (issueTypes == null)
            {
                issueTypes = JiraServerCache.Instance.getIssueTypes(server).Values;
            }
            foreach (JiraNamedEntity issueType in issueTypes)
            {
                imageList.Images.Add(ImageCache.Instance.getImage(issueType.IconUrl));
                ListViewItem lvi = new IssueTypeListViewItem(issueType, i);
                listViewIssueTypes.Items.Add(lvi);
                ++i;
            }
            listViewIssueTypes.SmallImageList = imageList;
        }

        private void buttonClose_Click(object sender, EventArgs e)
        {
            Close();
        }

        private void listBoxProjects_SelectedValueChanged(object sender, EventArgs e)
        {
            if (listBoxProjects.SelectedItems.Count == 1)
            {
                setAllEnabled(false);
                JiraProject project = listBoxProjects.SelectedItems[0] as JiraProject;

                Thread runner = new Thread(new ThreadStart(delegate
                   {
                       try
                       {
                           List<JiraNamedEntity> issueTypes = JiraServerFacade.Instance.getIssueTypes(server, project);
                           Invoke(new MethodInvoker(() => refillIssueTypes(issueTypes)));
                       }
                       catch (Exception ex)
                       {
                           Debug.WriteLine(ex.Message);
                       }
                       Invoke(new MethodInvoker(() => setAllEnabled(true)));
                   }));
                runner.Start();
            }
            else
            {
                refillIssueTypes(null);
            }
        }

        private void setAllEnabled(bool enabled)
        {
            listBoxProjects.Enabled = enabled;
            listViewIssueTypes.Enabled = enabled;
            buttonOk.Enabled = enabled;
            buttonClear.Enabled = enabled;
        }
    }
}

