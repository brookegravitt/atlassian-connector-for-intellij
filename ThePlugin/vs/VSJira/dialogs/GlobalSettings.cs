using System.Windows.Forms;

namespace PaZu.dialogs
{
    public partial class GlobalSettings : Form
    {
        static GlobalSettings()
        {
            JiraIssuesBatch = 25;
        }

        public GlobalSettings()
        {
            InitializeComponent();

            StartPosition = FormStartPosition.CenterParent;
        }

        public static int JiraIssuesBatch { get; private set; }
    }
}
