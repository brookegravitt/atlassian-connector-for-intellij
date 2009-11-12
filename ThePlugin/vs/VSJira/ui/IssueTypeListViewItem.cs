using System.Windows.Forms;
using PaZu.api;

namespace PaZu.ui
{
    class IssueTypeListViewItem : ListViewItem
    {
        public JiraNamedEntity IssueType { get; private set; }

        public IssueTypeListViewItem(JiraNamedEntity issueType, int imageIdx) : base(issueType.Name, imageIdx)
        {
            IssueType = issueType;
        }
    }
}
