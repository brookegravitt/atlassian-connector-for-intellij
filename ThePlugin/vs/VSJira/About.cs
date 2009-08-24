using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;

namespace PaZu
{
    public partial class About : Form
    {
        private static string ABOUT_TXT = 
            "<div style=\"text-align:center;font-size:10px;font-family:Arial;\">\n"
            + "The Atlassian Connector for Microsot Visual Studio is an Vsual Studio Add-in that lets you work<br>\n"
            + "with the Atlassian products within your IDE. Now you don't have to switch between websites,<br>\n" 
            + "email messages and news feeds to see what's happening to your project and your code.<br>\n"
            + "Instead, you can see the relevant <a target=\"_blank\" href=\"http://www.atlassian.com/software/jira/\">JIRA</a> issues,\n"
            + "<a target=\"_blank\" href=\"http://www.atlassian.com/software/crucible/\">Crucible</a> reviews\n"
            + "and <a target=\"_blank\" href=\"http://www.atlassian.com/software/bamboo/\">Bamboo</a><br>\n" 
            + "build information right there in your development environment. Viewing your code in\n"
            + "<a target=\"_blank\" href=\"http://www.atlassian.com/software/fisheye/\">FishEye</a><br>\n" 
            + "is just a click away.<br><br>\n" 
            + "Developed by Atlassian for you to lust after<br>\n"
            + "<a target=\"_blank\" href=\"http://www.atlassian.com/\">http://www.atlassian.com/</a><br><br>\n" 
            + "Licensed under the Apache License, Version 0.1 - Copyright (c) Atlassian 2009\n"
            + "</div>";

        public About()
        {
            InitializeComponent();

            picture.Image = Properties.Resources.atlassian_538x235;
            browser.DocumentText = ABOUT_TXT;
            browser.ScrollBarsEnabled = false;
        }

        private void buttonClose_Click(object sender, EventArgs e)
        {
            Close();
        }
    }
}
