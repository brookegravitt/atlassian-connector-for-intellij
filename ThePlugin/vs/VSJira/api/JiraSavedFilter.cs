using System;
using System.Collections.Generic;
using System.Text;

namespace PaZu.api
{
    public class JiraSavedFilter : JiraNamedEntity
    {
        public JiraSavedFilter(int id, string name)
            : base(id, name, null)
        {
        }
    }
}
