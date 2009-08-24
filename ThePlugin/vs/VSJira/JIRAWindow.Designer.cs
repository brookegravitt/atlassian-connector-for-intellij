namespace PaZu
{
    partial class JiraWindow
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
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(JiraWindow));
            this.mainContainer = new System.Windows.Forms.ToolStripContainer();
            this.productTabs = new System.Windows.Forms.TabControl();
            this.tabJira = new System.Windows.Forms.TabPage();
            this.jiraContainer = new System.Windows.Forms.ToolStripContainer();
            this.statusStrip1 = new System.Windows.Forms.StatusStrip();
            this.jiraStatus = new System.Windows.Forms.ToolStripStatusLabel();
            this.jiraSplitter = new System.Windows.Forms.SplitContainer();
            this.filterTree = new System.Windows.Forms.TreeView();
            this.toolStrip1 = new System.Windows.Forms.ToolStrip();
            this.buttonJiraFilterRefresh = new System.Windows.Forms.ToolStripButton();
            this.globalToolBar = new System.Windows.Forms.ToolStrip();
            this.buttonProjectProperties = new System.Windows.Forms.ToolStripButton();
            this.buttonAbout = new System.Windows.Forms.ToolStripButton();
            this.mainContainer.ContentPanel.SuspendLayout();
            this.mainContainer.LeftToolStripPanel.SuspendLayout();
            this.mainContainer.SuspendLayout();
            this.productTabs.SuspendLayout();
            this.tabJira.SuspendLayout();
            this.jiraContainer.BottomToolStripPanel.SuspendLayout();
            this.jiraContainer.ContentPanel.SuspendLayout();
            this.jiraContainer.TopToolStripPanel.SuspendLayout();
            this.jiraContainer.SuspendLayout();
            this.statusStrip1.SuspendLayout();
            this.jiraSplitter.Panel1.SuspendLayout();
            this.jiraSplitter.SuspendLayout();
            this.toolStrip1.SuspendLayout();
            this.globalToolBar.SuspendLayout();
            this.SuspendLayout();
            // 
            // mainContainer
            // 
            // 
            // mainContainer.ContentPanel
            // 
            this.mainContainer.ContentPanel.Controls.Add(this.productTabs);
            this.mainContainer.ContentPanel.Size = new System.Drawing.Size(685, 294);
            this.mainContainer.Dock = System.Windows.Forms.DockStyle.Fill;
            // 
            // mainContainer.LeftToolStripPanel
            // 
            this.mainContainer.LeftToolStripPanel.Controls.Add(this.globalToolBar);
            this.mainContainer.Location = new System.Drawing.Point(0, 0);
            this.mainContainer.Name = "mainContainer";
            this.mainContainer.Size = new System.Drawing.Size(709, 319);
            this.mainContainer.TabIndex = 2;
            this.mainContainer.Text = "toolStripContainer1";
            // 
            // productTabs
            // 
            this.productTabs.Controls.Add(this.tabJira);
            this.productTabs.Dock = System.Windows.Forms.DockStyle.Fill;
            this.productTabs.Location = new System.Drawing.Point(0, 0);
            this.productTabs.Name = "productTabs";
            this.productTabs.SelectedIndex = 0;
            this.productTabs.Size = new System.Drawing.Size(685, 294);
            this.productTabs.TabIndex = 0;
            // 
            // tabJira
            // 
            this.tabJira.Controls.Add(this.jiraContainer);
            this.tabJira.Location = new System.Drawing.Point(4, 22);
            this.tabJira.Name = "tabJira";
            this.tabJira.Padding = new System.Windows.Forms.Padding(3);
            this.tabJira.Size = new System.Drawing.Size(677, 268);
            this.tabJira.TabIndex = 0;
            this.tabJira.Text = "Issues - JIRA";
            this.tabJira.UseVisualStyleBackColor = true;
            // 
            // jiraContainer
            // 
            // 
            // jiraContainer.BottomToolStripPanel
            // 
            this.jiraContainer.BottomToolStripPanel.Controls.Add(this.statusStrip1);
            // 
            // jiraContainer.ContentPanel
            // 
            this.jiraContainer.ContentPanel.Controls.Add(this.jiraSplitter);
            this.jiraContainer.ContentPanel.Size = new System.Drawing.Size(671, 215);
            this.jiraContainer.Dock = System.Windows.Forms.DockStyle.Fill;
            this.jiraContainer.Location = new System.Drawing.Point(3, 3);
            this.jiraContainer.Name = "jiraContainer";
            this.jiraContainer.Size = new System.Drawing.Size(671, 262);
            this.jiraContainer.TabIndex = 0;
            this.jiraContainer.Text = "toolStripContainer1";
            // 
            // jiraContainer.TopToolStripPanel
            // 
            this.jiraContainer.TopToolStripPanel.Controls.Add(this.toolStrip1);
            // 
            // statusStrip1
            // 
            this.statusStrip1.Dock = System.Windows.Forms.DockStyle.None;
            this.statusStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.jiraStatus});
            this.statusStrip1.Location = new System.Drawing.Point(0, 0);
            this.statusStrip1.Name = "statusStrip1";
            this.statusStrip1.Size = new System.Drawing.Size(671, 22);
            this.statusStrip1.TabIndex = 0;
            // 
            // jiraStatus
            // 
            this.jiraStatus.Name = "jiraStatus";
            this.jiraStatus.Size = new System.Drawing.Size(38, 17);
            this.jiraStatus.Text = "Ready";
            // 
            // jiraSplitter
            // 
            this.jiraSplitter.Dock = System.Windows.Forms.DockStyle.Fill;
            this.jiraSplitter.Location = new System.Drawing.Point(0, 0);
            this.jiraSplitter.Name = "jiraSplitter";
            // 
            // jiraSplitter.Panel1
            // 
            this.jiraSplitter.Panel1.Controls.Add(this.filterTree);
            this.jiraSplitter.Size = new System.Drawing.Size(671, 215);
            this.jiraSplitter.SplitterDistance = 182;
            this.jiraSplitter.TabIndex = 0;
            // 
            // filterTree
            // 
            this.filterTree.Dock = System.Windows.Forms.DockStyle.Fill;
            this.filterTree.Location = new System.Drawing.Point(0, 0);
            this.filterTree.Name = "filterTree";
            this.filterTree.Size = new System.Drawing.Size(182, 215);
            this.filterTree.TabIndex = 0;
            // 
            // toolStrip1
            // 
            this.toolStrip1.Dock = System.Windows.Forms.DockStyle.None;
            this.toolStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.buttonJiraFilterRefresh});
            this.toolStrip1.Location = new System.Drawing.Point(3, 0);
            this.toolStrip1.Name = "toolStrip1";
            this.toolStrip1.Size = new System.Drawing.Size(33, 25);
            this.toolStrip1.TabIndex = 0;
            // 
            // buttonJiraFilterRefresh
            // 
            this.buttonJiraFilterRefresh.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            this.buttonJiraFilterRefresh.Image = ((System.Drawing.Image)(resources.GetObject("buttonJiraFilterRefresh.Image")));
            this.buttonJiraFilterRefresh.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.buttonJiraFilterRefresh.Name = "buttonJiraFilterRefresh";
            this.buttonJiraFilterRefresh.Size = new System.Drawing.Size(23, 22);
            this.buttonJiraFilterRefresh.Text = "Refresh All";
            // 
            // globalToolBar
            // 
            this.globalToolBar.Dock = System.Windows.Forms.DockStyle.None;
            this.globalToolBar.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.buttonProjectProperties,
            this.buttonAbout});
            this.globalToolBar.Location = new System.Drawing.Point(0, 3);
            this.globalToolBar.Name = "globalToolBar";
            this.globalToolBar.Size = new System.Drawing.Size(24, 55);
            this.globalToolBar.TabIndex = 0;
            // 
            // buttonProjectProperties
            // 
            this.buttonProjectProperties.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            this.buttonProjectProperties.Image = ((System.Drawing.Image)(resources.GetObject("buttonProjectProperties.Image")));
            this.buttonProjectProperties.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.buttonProjectProperties.Name = "buttonProjectProperties";
            this.buttonProjectProperties.Size = new System.Drawing.Size(22, 20);
            this.buttonProjectProperties.Text = "Project Configuration";
            this.buttonProjectProperties.Click += new System.EventHandler(this.buttonProjectProperties_Click);
            // 
            // buttonAbout
            // 
            this.buttonAbout.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            this.buttonAbout.Image = ((System.Drawing.Image)(resources.GetObject("buttonAbout.Image")));
            this.buttonAbout.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.buttonAbout.Name = "buttonAbout";
            this.buttonAbout.Size = new System.Drawing.Size(22, 20);
            this.buttonAbout.Text = "About";
            this.buttonAbout.Click += new System.EventHandler(this.buttonAbout_Click);
            // 
            // JiraWindow
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.BackColor = System.Drawing.SystemColors.Control;
            this.Controls.Add(this.mainContainer);
            this.Name = "JiraWindow";
            this.Size = new System.Drawing.Size(709, 319);
            this.mainContainer.ContentPanel.ResumeLayout(false);
            this.mainContainer.LeftToolStripPanel.ResumeLayout(false);
            this.mainContainer.LeftToolStripPanel.PerformLayout();
            this.mainContainer.ResumeLayout(false);
            this.mainContainer.PerformLayout();
            this.productTabs.ResumeLayout(false);
            this.tabJira.ResumeLayout(false);
            this.jiraContainer.BottomToolStripPanel.ResumeLayout(false);
            this.jiraContainer.BottomToolStripPanel.PerformLayout();
            this.jiraContainer.ContentPanel.ResumeLayout(false);
            this.jiraContainer.TopToolStripPanel.ResumeLayout(false);
            this.jiraContainer.TopToolStripPanel.PerformLayout();
            this.jiraContainer.ResumeLayout(false);
            this.jiraContainer.PerformLayout();
            this.statusStrip1.ResumeLayout(false);
            this.statusStrip1.PerformLayout();
            this.jiraSplitter.Panel1.ResumeLayout(false);
            this.jiraSplitter.ResumeLayout(false);
            this.toolStrip1.ResumeLayout(false);
            this.toolStrip1.PerformLayout();
            this.globalToolBar.ResumeLayout(false);
            this.globalToolBar.PerformLayout();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.ToolStripContainer mainContainer;
        private System.Windows.Forms.ToolStripContainer jiraContainer;
        private System.Windows.Forms.TabControl productTabs;
        private System.Windows.Forms.TabPage tabJira;
        private System.Windows.Forms.ToolStrip globalToolBar;
        private System.Windows.Forms.ToolStripButton buttonProjectProperties;
        private System.Windows.Forms.SplitContainer jiraSplitter;
        private System.Windows.Forms.TreeView filterTree;
        private System.Windows.Forms.StatusStrip statusStrip1;
        private System.Windows.Forms.ToolStripStatusLabel jiraStatus;
        private System.Windows.Forms.ToolStrip toolStrip1;
        private System.Windows.Forms.ToolStripButton buttonJiraFilterRefresh;
        private System.Windows.Forms.ToolStripButton buttonAbout;
    }
}
