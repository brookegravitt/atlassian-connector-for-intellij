using System;
using System.Collections.Generic;
using System.Text;

namespace PaZu
{
    public class JiraProject : JiraNamedEntity
    {
        private string key;

        public JiraProject(string id, string key, string name) :             
            base(id, name)
        {
            this.key = key;
        }

        public string Key
        {
            get
            {
                return key;
            }
        }

        public override string ToString()
        {
            return Key + ": " + Name;
        }
    }
}
