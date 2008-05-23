using System;
using System.Collections.Generic;
using System.Text;

namespace VSJira
{
    class RssClient
    {
        private string username;
        private string password;

        public RssClient()
        {
        }

        public void login(string username, string password)
        {
            this.username = username;
            this.password = password;
        }
    }
}
