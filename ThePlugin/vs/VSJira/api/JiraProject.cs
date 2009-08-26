namespace PaZu.api
{
    public class JiraProject : JiraNamedEntity
    {
        public JiraProject(int id, string key, string name) :             
            base(id, name, null)
        {
            Key = key;
        }

        public string Key { get; private set; }

        public override string ToString()
        {
            return Name;
        }
    }
}
