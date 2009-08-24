using System;
using System.Collections.Generic;
using System.Text;

namespace PaZu
{
    public class JiraServer
    {
        private Guid guid;
        private string name;
        private string url;
        private string userName;
        private string password;

        public JiraServer(string name, string url, string userName, string password)
            : this(Guid.NewGuid(), name, url, userName, password)
        {
        }

        public JiraServer(Guid guid, string name, string url, string userName, string password)
        {
            this.guid = guid;
            this.name = name;
            this.url = url;
            this.userName = userName;
            this.password = password;
        }

        public JiraServer(JiraServer other)
        {
            if (other != null)
            {
                this.guid = other.guid;
                this.name = other.name;
                this.url = other.url;
                this.userName = other.userName;
                this.password = other.password;
            }
            else
            {
                guid = Guid.NewGuid();
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

        public Guid GUID
        {
            get { return guid; }
            set { guid = value; }
        }
    }
}
