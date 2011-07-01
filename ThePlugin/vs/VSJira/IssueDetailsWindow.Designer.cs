namespace PaZu
{
    partial class IssueDetailsWindow
    {
        /// <summary> 
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary> 
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Component Designer generated code

        /// <summary> 
        /// Required method for Designer support - do not modify 
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.issueTabs = new System.Windows.Forms.TabControl();
            this.SuspendLayout();
            // 
            // issueTabs
            // 
            this.issueTabs.Dock = System.Windows.Forms.DockStyle.Fill;
            this.issueTabs.Location = new System.Drawing.Point(0, 0);
            this.issueTabs.Name = "issueTabs";
            this.issueTabs.SelectedIndex = 0;
            this.issueTabs.Size = new System.Drawing.Size(866, 320);
            this.issueTabs.TabIndex = 0;
            // 
            // IssueDetailsWindow
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Controls.Add(this.issueTabs);
            this.Name = "IssueDetailsWindow";
            this.Size = new System.Drawing.Size(866, 320);
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.TabControl issueTabs;
    }
}
