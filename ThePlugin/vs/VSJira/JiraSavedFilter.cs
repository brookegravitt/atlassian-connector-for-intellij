using System;
using System.Collections.Generic;
using System.Text;

namespace PaZu
{
    public class JiraSavedFilter : JiraNamedEntity
    {
        public JiraSavedFilter(string id, string name)
            : base(id, name)
        {
        }

        public override string ToString()
        {
            return ID + ": " + Name;
        }
    }
}
