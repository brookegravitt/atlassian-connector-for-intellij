using System;
using System.Collections.Generic;
using System.Text;

namespace VSJira
{
    public class JiraServer
    {
        private string url;
        private string userName;
        private string password;

        public JiraServer(string url, string userName, string password)
        {
            this.url = url;
            this.userName = userName;
            this.password = password;
        }

        public string Url
        {
            get
            {
                return url;
            }
        }

        public string UserName
        {
            get
            {
                return userName;
            }
        }

        public string Password
        {
            get
            {
                return password;
            }
        }
    }
}
