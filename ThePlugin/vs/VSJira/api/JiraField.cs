namespace PaZu.api
{
    public class JiraField
    {
        public JiraField(string id, string name)
        {
            Id = id;
            Name = name;
        }

        public string Id { get; private set; }
        public string Name { get; private set; }
    }
}
