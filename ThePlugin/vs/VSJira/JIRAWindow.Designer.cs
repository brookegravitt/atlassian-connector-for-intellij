namespace VSJira
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
            this.projects = new System.Windows.Forms.ComboBox();
            this.login = new System.Windows.Forms.Button();
            this.url = new System.Windows.Forms.TextBox();
            this.userName = new System.Windows.Forms.TextBox();
            this.password = new System.Windows.Forms.TextBox();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.statusBar = new System.Windows.Forms.StatusStrip();
            this.statusMessage = new System.Windows.Forms.ToolStripStatusLabel();
            this.toolStrip1 = new System.Windows.Forms.ToolStrip();
            this.refreshButton = new System.Windows.Forms.ToolStripButton();
            this.savedFiltersMenu = new System.Windows.Forms.ToolStripDropDownButton();
            this.panel1 = new System.Windows.Forms.Panel();
            this.issueTable = new System.Windows.Forms.DataGridView();
            this.panel2 = new System.Windows.Forms.Panel();
            this.label4 = new System.Windows.Forms.Label();
            this.statusBar.SuspendLayout();
            this.toolStrip1.SuspendLayout();
            this.panel1.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.issueTable)).BeginInit();
            this.panel2.SuspendLayout();
            this.SuspendLayout();
            // 
            // projects
            // 
            this.projects.Enabled = false;
            this.projects.FormattingEnabled = true;
            this.projects.Location = new System.Drawing.Point(201, 108);
            this.projects.Name = "projects";
            this.projects.Size = new System.Drawing.Size(165, 21);
            this.projects.TabIndex = 6;
            this.projects.SelectedIndexChanged += new System.EventHandler(this.projects_SelectedIndexChanged);
            // 
            // login
            // 
            this.login.Location = new System.Drawing.Point(297, 66);
            this.login.Name = "login";
            this.login.Size = new System.Drawing.Size(69, 26);
            this.login.TabIndex = 4;
            this.login.Text = "Login";
            this.login.UseVisualStyleBackColor = true;
            this.login.Click += new System.EventHandler(this.login_Click);
            // 
            // url
            // 
            this.url.Location = new System.Drawing.Point(81, 9);
            this.url.Name = "url";
            this.url.Size = new System.Drawing.Size(285, 20);
            this.url.TabIndex = 1;
            this.url.TextChanged += new System.EventHandler(this.url_TextChanged);
            // 
            // userName
            // 
            this.userName.Location = new System.Drawing.Point(81, 41);
            this.userName.Name = "userName";
            this.userName.Size = new System.Drawing.Size(100, 20);
            this.userName.TabIndex = 2;
            this.userName.TextChanged += new System.EventHandler(this.userName_TextChanged);
            // 
            // password
            // 
            this.password.Location = new System.Drawing.Point(81, 72);
            this.password.Name = "password";
            this.password.PasswordChar = '*';
            this.password.Size = new System.Drawing.Size(100, 20);
            this.password.TabIndex = 3;
            this.password.UseSystemPasswordChar = true;
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(13, 44);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(60, 13);
            this.label1.TabIndex = 5;
            this.label1.Text = "User Name";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(13, 75);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(53, 13);
            this.label2.TabIndex = 6;
            this.label2.Text = "Password";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(13, 12);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(45, 13);
            this.label3.TabIndex = 7;
            this.label3.Text = "Address";
            // 
            // statusBar
            // 
            this.statusBar.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.statusMessage});
            this.statusBar.Location = new System.Drawing.Point(0, 662);
            this.statusBar.Name = "statusBar";
            this.statusBar.Size = new System.Drawing.Size(542, 22);
            this.statusBar.SizingGrip = false;
            this.statusBar.TabIndex = 9;
            this.statusBar.Text = "statusStrip1";
            // 
            // statusMessage
            // 
            this.statusMessage.Name = "statusMessage";
            this.statusMessage.Size = new System.Drawing.Size(61, 17);
            this.statusMessage.Text = "Select filter";
            // 
            // toolStrip1
            // 
            this.toolStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.refreshButton,
            this.savedFiltersMenu});
            this.toolStrip1.Location = new System.Drawing.Point(0, 0);
            this.toolStrip1.Name = "toolStrip1";
            this.toolStrip1.Size = new System.Drawing.Size(542, 25);
            this.toolStrip1.TabIndex = 10;
            this.toolStrip1.Text = "toolStrip1";
            // 
            // refreshButton
            // 
            this.refreshButton.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Image;
            this.refreshButton.Image = ((System.Drawing.Image)(resources.GetObject("refreshButton.Image")));
            this.refreshButton.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.refreshButton.Name = "refreshButton";
            this.refreshButton.Size = new System.Drawing.Size(23, 22);
            this.refreshButton.Text = "Refresh";
            // 
            // savedFiltersMenu
            // 
            this.savedFiltersMenu.DisplayStyle = System.Windows.Forms.ToolStripItemDisplayStyle.Text;
            this.savedFiltersMenu.Image = ((System.Drawing.Image)(resources.GetObject("savedFiltersMenu.Image")));
            this.savedFiltersMenu.ImageTransparentColor = System.Drawing.Color.Magenta;
            this.savedFiltersMenu.Name = "savedFiltersMenu";
            this.savedFiltersMenu.Size = new System.Drawing.Size(109, 22);
            this.savedFiltersMenu.Text = "Select Saved Filter";
            this.savedFiltersMenu.DropDownItemClicked += new System.Windows.Forms.ToolStripItemClickedEventHandler(this.savedFiltersMenu_DropDownItemClicked);
            // 
            // panel1
            // 
            this.panel1.Controls.Add(this.issueTable);
            this.panel1.Controls.Add(this.panel2);
            this.panel1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.panel1.Location = new System.Drawing.Point(0, 25);
            this.panel1.Name = "panel1";
            this.panel1.Size = new System.Drawing.Size(542, 637);
            this.panel1.TabIndex = 11;
            // 
            // issueTable
            // 
            this.issueTable.AllowUserToAddRows = false;
            this.issueTable.AllowUserToDeleteRows = false;
            this.issueTable.AllowUserToResizeRows = false;
            this.issueTable.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.issueTable.Dock = System.Windows.Forms.DockStyle.Fill;
            this.issueTable.Location = new System.Drawing.Point(0, 146);
            this.issueTable.Name = "issueTable";
            this.issueTable.ReadOnly = true;
            this.issueTable.RowHeadersVisible = false;
            this.issueTable.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
            this.issueTable.Size = new System.Drawing.Size(542, 491);
            this.issueTable.TabIndex = 10;
            // 
            // panel2
            // 
            this.panel2.BorderStyle = System.Windows.Forms.BorderStyle.Fixed3D;
            this.panel2.Controls.Add(this.label4);
            this.panel2.Controls.Add(this.userName);
            this.panel2.Controls.Add(this.label1);
            this.panel2.Controls.Add(this.projects);
            this.panel2.Controls.Add(this.password);
            this.panel2.Controls.Add(this.label3);
            this.panel2.Controls.Add(this.url);
            this.panel2.Controls.Add(this.label2);
            this.panel2.Controls.Add(this.login);
            this.panel2.Dock = System.Windows.Forms.DockStyle.Top;
            this.panel2.Location = new System.Drawing.Point(0, 0);
            this.panel2.Name = "panel2";
            this.panel2.Size = new System.Drawing.Size(542, 146);
            this.panel2.TabIndex = 9;
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(136, 111);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(45, 13);
            this.label4.TabIndex = 8;
            this.label4.Text = "Projects";
            // 
            // JiraWindow
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.BackColor = System.Drawing.SystemColors.Control;
            this.Controls.Add(this.panel1);
            this.Controls.Add(this.toolStrip1);
            this.Controls.Add(this.statusBar);
            this.Name = "JiraWindow";
            this.Size = new System.Drawing.Size(542, 684);
            this.Load += new System.EventHandler(this.JIRAWindow_Load);
            this.statusBar.ResumeLayout(false);
            this.statusBar.PerformLayout();
            this.toolStrip1.ResumeLayout(false);
            this.toolStrip1.PerformLayout();
            this.panel1.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.issueTable)).EndInit();
            this.panel2.ResumeLayout(false);
            this.panel2.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.ComboBox projects;
        private System.Windows.Forms.Button login;
        private System.Windows.Forms.TextBox url;
        private System.Windows.Forms.TextBox userName;
        private System.Windows.Forms.TextBox password;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.StatusStrip statusBar;
        private System.Windows.Forms.ToolStrip toolStrip1;
        private System.Windows.Forms.Panel panel1;
        private System.Windows.Forms.Panel panel2;
        private System.Windows.Forms.DataGridView issueTable;
        private System.Windows.Forms.ToolStripButton refreshButton;
        private System.Windows.Forms.ToolStripDropDownButton savedFiltersMenu;
        private System.Windows.Forms.ToolStripStatusLabel statusMessage;
        private System.Windows.Forms.Label label4;
    }
}
