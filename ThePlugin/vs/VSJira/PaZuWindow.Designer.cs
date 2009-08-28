namespace PaZu
{
    partial class PaZuWindow
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
            this.mainContainer = new System.Windows.Forms.ToolStripContainer();
            this.productTabs = new System.Windows.Forms.TabControl();
            this.tabJira = new System.Windows.Forms.TabPage();
            this.jiraContainer = new System.Windows.Forms.ToolStripContainer();
            this.statusStrip1 = new System.Windows.Forms.StatusStrip();
            this.jiraStatus = new System.Windows.Forms.ToolStripStatusLabel();
            this.getMoreIssues = new System.Windows.Forms.ToolStripStatusLabel();
            this.jiraSplitter = new System.Windows.Forms.SplitContainer();
            this.filterTreeContainer = new System.Windows.Forms.ToolStripContainer();
            this.filtersTree = new System.Windows.Forms.TreeView();
            this.toolStrip1 = new System.Windows.Forms.ToolStrip();
            this.buttonRefreshAll = new System.Windows.Forms.ToolStripButton();
            this.issueTreeContainer = new System.Windows.Forms.ToolStripContainer();
            this.toolStrip2 = new System.Windows.Forms.ToolStrip();
            this.globalToolBar = new System.Windows.Forms.ToolStrip();
            this.buttonProjectProperties = new System.Windows.Forms.ToolStripButton();
            this.buttonAbout = new System.Windows.Forms.ToolStripButton();
            this.toolStripContainer1 = new System.Windows.Forms.ToolStripContainer();
            this.mainContainer.ContentPanel.SuspendLayout();
            this.mainContainer.LeftToolStripPanel.SuspendLayout();
            this.mainContainer.SuspendLayout();
            this.productTabs.SuspendLayout();
            this.tabJira.SuspendLayout();
            this.jiraContainer.BottomToolStripPanel.SuspendLayout();
            this.jiraContainer.ContentPanel.SuspendLayout();
            this.jiraContainer.SuspendLayout();
            this.statusStrip1.SuspendLayout();
            this.jiraSplitter.Panel1.SuspendLayout();
            this.jiraSplitter.Panel2.SuspendLayout();
            this.jiraSplitter.SuspendLayout();
            this.filterTreeContainer.ContentPanel.SuspendLayout();
            this.filterTreeContainer.TopToolStripPanel.SuspendLayout();
            this.filterTreeContainer.SuspendLayout();
            this.toolStrip1.SuspendLayout();
            this.issueTreeContainer.TopToolStripPanel.SuspendLayout();
            this.issueTreeContainer.SuspendLayout();
            this.globalToolBar.SuspendLayout();
            this.toolStripContainer1.SuspendLayout();
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
            // statusStrip1
            // 
            this.statusStrip1.Dock = System.Windows.Forms.DockStyle.None;
            this.statusStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.jiraStatus,
            this.getMoreIssues});
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
            // getMoreIssues
            // 
            this.getMoreIssues.IsLink = true;
            this.getMoreIssues.Name = "getMoreIssues";
            this.getMoreIssues.Size = new System.Drawing.Size(97, 17);
            this.getMoreIssues.Text = "Get More Issues...";
            this.getMoreIssues.Visible = false;
            this.getMoreIssues.Click += new System.EventHandler(this.getMoreIssues_Click);
            // 
            // jiraSplitter
            // 
            this.jiraSplitter.Dock = System.Windows.Forms.DockStyle.Fill;
            this.jiraSplitter.Location = new System.Drawing.Point(0, 0);
            this.jiraSplitter.Name = "jiraSplitter";
            // 
            // jiraSplitter.Panel1
            // 
            this.jiraSplitter.Panel1.Controls.Add(this.filterTreeContainer);
            // 
            // jiraSplitter.Panel2
            // 
            this.jiraSplitter.Panel2.Controls.Add(this.issueTreeContainer);
            this.jiraSplitter.Size = new System.Drawing.Size(671, 215);
            this.jiraSplitter.SplitterDistance = 180;
            this.jiraSplitter.TabIndex = 0;
            // 
            // filterTreeContainer
            // 
            // 
            // filterTreeContainer.ContentPanel
            // 
            this.filterTreeContainer.ContentPanel.Controls.Add(this.filtersTree);
            this.filterTreeContainer.ContentPanel.Size = new System.Drawing.Size(180, 190);
            this.filterTreeContainer.Dock = System.Windows.Forms.DockStyle.Fill;
            this.filterTreeContainer.Location = new System.Drawing.Point(0, 0);
            this.filterTreeContainer.Name = "filterTreeContainer";
            this.filterTreeContainer.Size = new System.Drawing.Size(180, 215);
            this.filterTreeContainer.TabIndex = 0;
            this.filterTreeContainer.Text = "toolStripContainer2";
            // 
            // filterTreeContainer.TopToolStripPanel
            // 
            this.filterTreeContainer.TopToolStripPanel.Controls.Add(this.toolStrip1);
            // 
            // filtersTree
            // 
            this.filtersTree.Dock = System.Windows.Forms.DockStyle.Fill;
            this.filtersTree.HideSelection = false;
            this.filtersTree.Location = new System.Drawing.Point(0, 0);
            this.filtersTree.Name = "filtersTree";
            this.filtersTree.Size = new System.Drawing.Size(180, 190);
            this.filtersTree.TabIndex = 0;
            this.filtersTree.AfterSelect += new System.Windows.Forms.TreeViewEventHandler(this.filtersTree_AfterSelect);
            // 
            // toolStrip1
            // 
            this.toolStrip1.Dock = System.Windows.Forms.DockStyle.None;
            this.toolStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.buttonRefreshAll});
            this.toolStrip1.Location = new System.Drawing.Point(3, 0);
            this.toolStrip1.Name = "toolStrip1";
            this.toolStrip1.Size = new System.Drawing.Size(33, 25);
            this.toolStrip1.TabIndex = 0;
            // 
            // buttonRefreshAll
            // 
            this.buttonRefreshAll.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            this.buttonRefreshAll.Image = global::PaZu.Properties.Resources.refresh;
            this.buttonRefreshAll.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.buttonRefreshAll.Name = "buttonRefreshAll";
            this.buttonRefreshAll.Size = new System.Drawing.Size(23, 22);
            this.buttonRefreshAll.Text = "toolStripButton1";
            this.buttonRefreshAll.Click += new System.EventHandler(this.buttonRefreshAll_Click);
            // 
            // issueTreeContainer
            // 
            // 
            // issueTreeContainer.ContentPanel
            // 
            this.issueTreeContainer.ContentPanel.Size = new System.Drawing.Size(487, 190);
            this.issueTreeContainer.Dock = System.Windows.Forms.DockStyle.Fill;
            this.issueTreeContainer.Location = new System.Drawing.Point(0, 0);
            this.issueTreeContainer.Name = "issueTreeContainer";
            this.issueTreeContainer.Size = new System.Drawing.Size(487, 215);
            this.issueTreeContainer.TabIndex = 0;
            this.issueTreeContainer.Text = "toolStripContainer2";
            // 
            // issueTreeContainer.TopToolStripPanel
            // 
            this.issueTreeContainer.TopToolStripPanel.Controls.Add(this.toolStrip2);
            // 
            // toolStrip2
            // 
            this.toolStrip2.Dock = System.Windows.Forms.DockStyle.None;
            this.toolStrip2.Location = new System.Drawing.Point(3, 0);
            this.toolStrip2.Name = "toolStrip2";
            this.toolStrip2.Size = new System.Drawing.Size(109, 25);
            this.toolStrip2.TabIndex = 0;
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
            this.buttonProjectProperties.Image = global::PaZu.Properties.Resources.projectsettings;
            this.buttonProjectProperties.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.buttonProjectProperties.Name = "buttonProjectProperties";
            this.buttonProjectProperties.Size = new System.Drawing.Size(22, 20);
            this.buttonProjectProperties.Text = "Project Configuration";
            this.buttonProjectProperties.Click += new System.EventHandler(this.buttonProjectProperties_Click);
            // 
            // buttonAbout
            // 
            this.buttonAbout.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            this.buttonAbout.Image = global::PaZu.Properties.Resources.about;
            this.buttonAbout.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.buttonAbout.Name = "buttonAbout";
            this.buttonAbout.Size = new System.Drawing.Size(22, 20);
            this.buttonAbout.Text = "About";
            this.buttonAbout.Click += new System.EventHandler(this.buttonAbout_Click);
            // 
            // toolStripContainer1
            // 
            // 
            // toolStripContainer1.ContentPanel
            // 
            this.toolStripContainer1.ContentPanel.Size = new System.Drawing.Size(709, 294);
            this.toolStripContainer1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.toolStripContainer1.Location = new System.Drawing.Point(0, 0);
            this.toolStripContainer1.Name = "toolStripContainer1";
            this.toolStripContainer1.Size = new System.Drawing.Size(709, 319);
            this.toolStripContainer1.TabIndex = 0;
            this.toolStripContainer1.Text = "toolStripContainer1";
            // 
            // PaZuWindow
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.BackColor = System.Drawing.SystemColors.Control;
            this.Controls.Add(this.mainContainer);
            this.Controls.Add(this.toolStripContainer1);
            this.Name = "PaZuWindow";
            this.Size = new System.Drawing.Size(709, 319);
            this.Load += new System.EventHandler(this.PaZuWindow_Load);
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
            this.jiraContainer.ResumeLayout(false);
            this.jiraContainer.PerformLayout();
            this.statusStrip1.ResumeLayout(false);
            this.statusStrip1.PerformLayout();
            this.jiraSplitter.Panel1.ResumeLayout(false);
            this.jiraSplitter.Panel2.ResumeLayout(false);
            this.jiraSplitter.ResumeLayout(false);
            this.filterTreeContainer.ContentPanel.ResumeLayout(false);
            this.filterTreeContainer.TopToolStripPanel.ResumeLayout(false);
            this.filterTreeContainer.TopToolStripPanel.PerformLayout();
            this.filterTreeContainer.ResumeLayout(false);
            this.filterTreeContainer.PerformLayout();
            this.toolStrip1.ResumeLayout(false);
            this.toolStrip1.PerformLayout();
            this.issueTreeContainer.TopToolStripPanel.ResumeLayout(false);
            this.issueTreeContainer.TopToolStripPanel.PerformLayout();
            this.issueTreeContainer.ResumeLayout(false);
            this.issueTreeContainer.PerformLayout();
            this.globalToolBar.ResumeLayout(false);
            this.globalToolBar.PerformLayout();
            this.toolStripContainer1.ResumeLayout(false);
            this.toolStripContainer1.PerformLayout();
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
        private System.Windows.Forms.StatusStrip statusStrip1;
        private System.Windows.Forms.ToolStripStatusLabel jiraStatus;
        private System.Windows.Forms.ToolStripButton buttonAbout;
        private System.Windows.Forms.ToolStripContainer filterTreeContainer;
        private System.Windows.Forms.ToolStripContainer toolStripContainer1;
        private System.Windows.Forms.ToolStripContainer issueTreeContainer;
        private System.Windows.Forms.ToolStrip toolStrip1;
        private System.Windows.Forms.ToolStripButton buttonRefreshAll;
        private System.Windows.Forms.ToolStrip toolStrip2;
        private System.Windows.Forms.TreeView filtersTree;
        private System.Windows.Forms.ToolStripStatusLabel getMoreIssues;
    }
}
