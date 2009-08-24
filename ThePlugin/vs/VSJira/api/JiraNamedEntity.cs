using System;
using System.Collections.Generic;
using System.Text;

namespace PaZu.api
{
    public abstract class JiraNamedEntity
    {
        private string id;
        private string name;

        public JiraNamedEntity(string id, string name)
        {
            this.id = id;
            this.name = name;
        }

        public string ID
        {
            get
            {
                return id;
            }
        }

        public string Name
        {
            get
            {
                return name;
            }
        }
    }
}
