using System;
using System.Collections.Generic;
using System.Text;

namespace PaZu
{
    public class JiraServer
    {
        private string name;
        private string url;
        private string userName;
        private string password;

        public JiraServer(string name, string url, string userName, string password)
        {
            this.name = name;
            this.url = url;
            this.userName = userName;
            this.password = password;
        }

        public JiraServer(JiraServer other)
        {
            if (other != null)
            {
                this.name = other.name;
                this.url = other.url;
                this.userName = other.userName;
                this.password = other.password;
            }
        }

        public string Name 
            { 
                get { return name; } 
                set { name = value; } 
            }

        public string Url 
            { 
                get { return url; } 
                set { url = value; } 
            }

        public string UserName 
            { 
                get { return userName; }
                set { userName = value; }
            }

        public string Password 
            { 
                get { return password; }
                set { password = value; }
            }
    }
}
