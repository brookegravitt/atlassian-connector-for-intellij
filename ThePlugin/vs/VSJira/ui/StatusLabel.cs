using System;
using System.Drawing;
using System.Windows.Forms;

namespace PaZu.ui
{
    public class StatusLabel
    {
        private readonly StatusStrip statusBar;
        private readonly ToolStripStatusLabel targetLabel;

        public StatusLabel(StatusStrip statusBar, ToolStripStatusLabel targetLabel)
        {
            this.statusBar = statusBar;
            this.targetLabel = targetLabel;
        }

        public void setError(string txt, Exception e)
        {
            statusBar.Invoke(new MethodInvoker(delegate
            {
                targetLabel.BackColor = Color.LightPink;
                Exception inner = e.InnerException;
                targetLabel.Text = txt + ": " + (inner != null ? inner.Message : e.Message);
            }));
        }

        public void setInfo(string txt)
        {
            statusBar.Invoke(new MethodInvoker(delegate
            {
                targetLabel.BackColor = Color.Transparent;
                targetLabel.Text = txt;
            }));
        }
    }
}
