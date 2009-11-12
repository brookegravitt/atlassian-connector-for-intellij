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
        private readonly JiraCustomFilter filter;

        public bool Changed { get; private set; }

        public EditCustomFilter(JiraServer server, JiraCustomFilter filter)
        {
            this.server = server;
            this.filter = filter;

            InitializeComponent();

            listViewIssueTypes.Columns.Add("Name", listViewIssueTypes.Width - 10, HorizontalAlignment.Left);

            StartPosition = FormStartPosition.CenterParent;

            SortedDictionary<string, JiraProject> projects = JiraServerCache.Instance.getProjects(server);

            foreach (string projectKey in projects.Keys)
            {
                listBoxProjects.Items.Add(projects[projectKey]);
            }
            refillIssueTypes(null);
            refillFixFor(null);
            refillComponents(null);
            refillAffectsVersions(null);

            manageSelections();

            listBoxProjects.SelectedValueChanged += listBoxProjects_SelectedValueChanged;
        }

        private void manageSelections()
        {
            foreach (JiraProject project in filter.Projects)
            {
                foreach (var item in listBoxProjects.Items)
                {
                    if (!project.Key.Equals(((JiraProject) item).Key)) continue;
                    listBoxProjects.SelectedItems.Add(item);
                    break;
                }
            }
            setProjectRelatedValues(true);
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

        private void refillFixFor(IEnumerable<JiraNamedEntity> fixFors)
        {
            listBoxFixForVersions.Items.Clear();

            if (fixFors == null)
                return;
            foreach (JiraNamedEntity fixFor in fixFors)
                listBoxFixForVersions.Items.Add(fixFor);
        }

        private void refillComponents(IEnumerable<JiraNamedEntity> comps)
        {
            listBoxComponents.Items.Clear();

            if (comps == null)
                return;
            foreach (JiraNamedEntity component in comps)
                listBoxComponents.Items.Add(component);
        }

        private void refillAffectsVersions(IEnumerable<JiraNamedEntity> versions)
        {
            listBoxAffectsVersions.Items.Clear();

            if (versions == null)
                return;
            foreach (JiraNamedEntity version in versions)
                listBoxAffectsVersions.Items.Add(version);
        }

        private void buttonClose_Click(object sender, EventArgs e)
        {
            Close();
        }

        private void listBoxProjects_SelectedValueChanged(object sender, EventArgs e)
        {
            setProjectRelatedValues(false);
        }

        private void setProjectRelatedValues(bool initial)
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
                           List<JiraNamedEntity> comps = JiraServerFacade.Instance.getComponents(server, project);
                           List<JiraNamedEntity> versions = JiraServerFacade.Instance.getVersions(server, project);
                           versions.Reverse();
                           Invoke(new MethodInvoker(delegate
                                                        {
                                                            refillIssueTypes(issueTypes);
                                                            refillComponents(comps);
                                                            refillFixFor(versions);
                                                            refillAffectsVersions(versions);

                                                            if (initial)
                                                                setProjectRelatedSelections();

                                                            setAllEnabled(true);
                                                        }));
                       }
                       catch (Exception ex)
                       {
                           if (ex is InvalidOperationException)
                           {
                               // probably the window got closed while we were fetching data
                               Debug.WriteLine(ex.Message);
                           }
                           else
                               MessageBox.Show("Unable to retrieve project-related data: " + ex.Message, "Error");
                       }
                   }));
                runner.Start();
            }
            else
            {
                refillIssueTypes(null);
                refillComponents(null);
                refillFixFor(null);
                refillAffectsVersions(null);

                if (initial)
                    setProjectRelatedSelections();
            }
        }

        private void setProjectRelatedSelections()
        {
            foreach (JiraNamedEntity issueType in filter.IssueTypes)
            {
                foreach (ListViewItem item in listViewIssueTypes.Items)
                {
                    if (issueType.Id != (((IssueTypeListViewItem) item).IssueType.Id)) continue;
                    item.Selected = true;
                    break;
                }
            }

            foreach (JiraNamedEntity fixFor in filter.FixForVersions)
            {
                foreach (var item in listBoxFixForVersions.Items)
                {
                    if (fixFor.Id != (((JiraNamedEntity) item).Id)) continue;
                    listBoxFixForVersions.SelectedItems.Add(item);
                    break;
                }
            }

            foreach (JiraNamedEntity comp in filter.Components)
            {
                foreach (var item in listBoxComponents.Items)
                {
                    if (comp.Id != (((JiraNamedEntity) item).Id)) continue;
                    listBoxComponents.SelectedItems.Add(item);
                    break;
                }
            }

            foreach (JiraNamedEntity affectVersion in filter.AffectsVersions)
            {
                foreach (var item in listBoxAffectsVersions.Items)
                {
                    if (affectVersion.Id != (((JiraNamedEntity) item).Id)) continue;
                    listBoxAffectsVersions.SelectedItems.Add(item);
                    break;
                }
            }
        }

        private void setAllEnabled(bool enabled)
        {
            listBoxProjects.Enabled = enabled;
            listViewIssueTypes.Enabled = enabled;
            listBoxFixForVersions.Enabled = enabled;
            listBoxComponents.Enabled = enabled;
            listBoxAffectsVersions.Enabled = enabled;
            buttonOk.Enabled = enabled;
            buttonClear.Enabled = enabled;
        }

        private void buttonClear_Click(object sender, EventArgs e)
        {
//            clearFilterValues();
            clearSelections();
//            Changed = true;
        }

        private void buttonOk_Click(object sender, EventArgs e)
        {
            clearFilterValues();
            foreach (var item in listBoxProjects.SelectedItems)
            {
                JiraProject proj = item as JiraProject;
                if (proj != null)
                    filter.Projects.Add(proj);
            }
            foreach (var item in listViewIssueTypes.SelectedItems)
            {
                IssueTypeListViewItem itlvi = item as IssueTypeListViewItem;
                if (itlvi != null)
                    filter.IssueTypes.Add(itlvi.IssueType);
            }
            foreach (var item in listBoxFixForVersions.SelectedItems)
            {
                JiraNamedEntity version = item as JiraNamedEntity;
                if (version != null)
                    filter.FixForVersions.Add(version);
            }
            foreach (var item in listBoxAffectsVersions.SelectedItems)
            {
                JiraNamedEntity version = item as JiraNamedEntity;
                if (version != null)
                    filter.AffectsVersions.Add(version);
            }
            foreach (var item in listBoxComponents.SelectedItems)
            {
                JiraNamedEntity comp = item as JiraNamedEntity;
                if (comp != null)
                    filter.Components.Add(comp);
            }
            Changed = true;
            Close();
        }

        private void clearSelections()
        {
            listViewIssueTypes.SelectedItems.Clear();
            listBoxFixForVersions.SelectedItems.Clear();
            listBoxComponents.SelectedItems.Clear();
            listBoxAffectsVersions.SelectedItems.Clear();
            // make it last, so that project-related updates are not triggered too early
            listBoxProjects.SelectedItems.Clear();
        }

        private void clearFilterValues()
        {
            filter.Projects.Clear();
            filter.IssueTypes.Clear();
            filter.AffectsVersions.Clear();
            filter.FixForVersions.Clear();
            filter.Components.Clear();
        }
    }
}

