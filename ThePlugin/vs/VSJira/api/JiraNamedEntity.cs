namespace PaZu.api
{
    public class JiraNamedEntity
    {
        public JiraNamedEntity(int id, string name, string iconUrl)
        {
            Id = id;
            Name = name;
            IconUrl = iconUrl;
        }

        public int Id { get; private set; }
        public string Name { get; private set; }
        public string IconUrl { get; private set; }
    }
}
