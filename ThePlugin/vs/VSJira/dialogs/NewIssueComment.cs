using System.Windows.Forms;

namespace PaZu.dialogs
{
    public partial class NewIssueComment : Form
    {
        public NewIssueComment()
        {
            InitializeComponent();
            buttonOk.Enabled = false;
        }

        public string CommentBody { get { return commentText.Text; } }

        private void commentText_TextChanged(object sender, System.EventArgs e)
        {
            buttonOk.Enabled = commentText.Text.Trim().Length > 0;
        }
    }
}
