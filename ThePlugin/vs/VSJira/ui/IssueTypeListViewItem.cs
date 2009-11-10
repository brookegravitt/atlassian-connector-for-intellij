using System.Windows.Forms;
using PaZu.api;

namespace PaZu.ui
{
    class IssueTypeListViewItem : ListViewItem
    {
        public JiraNamedEntity issueType { get; private set; }

        public IssueTypeListViewItem(JiraNamedEntity issueType, int imageIdx) : base(issueType.Name, imageIdx)
        {
            this.issueType = issueType;
        }
    }
}
