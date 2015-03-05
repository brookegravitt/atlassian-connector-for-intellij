using System;
using System.Collections.Generic;
using PaZu.api;

namespace PaZu.models
{
    class JiraServerCache
    {
        private static readonly JiraServerCache INSTANCE = new JiraServerCache();

        public static JiraServerCache Instance { get { return INSTANCE; } }

        private readonly SortedDictionary<Guid, SortedDictionary<string, JiraProject>> projectCache = 
            new SortedDictionary<Guid, SortedDictionary<string, JiraProject>>();

        private readonly SortedDictionary<Guid, SortedDictionary<int, JiraNamedEntity>> issueTypeCache =
            new SortedDictionary<Guid, SortedDictionary<int, JiraNamedEntity>>();

        private readonly SortedDictionary<Guid, SortedDictionary<int, JiraNamedEntity>> priorityCache =
            new SortedDictionary<Guid, SortedDictionary<int, JiraNamedEntity>>();

        private readonly SortedDictionary<Guid, SortedDictionary<int, JiraNamedEntity>> statusCache =
            new SortedDictionary<Guid, SortedDictionary<int, JiraNamedEntity>>();

        public SortedDictionary<string, JiraProject> getProjects(JiraServer server)
        {
            lock(projectCache)
            {
                if (projectCache.ContainsKey(server.GUID))
                {
                    return projectCache[server.GUID];
                }
            }
            return null;
        }

        public void addProject(JiraServer server, JiraProject project)
        {
            lock(projectCache)
            {
                if (!projectCache.ContainsKey(server.GUID))
                {
                    projectCache[server.GUID] = new SortedDictionary<string, JiraProject>();
                }
                projectCache[server.GUID][project.Key] = project;
            }
        }

        public void clearProjects()
        {
            lock(projectCache)
            {
                projectCache.Clear();
            }
        }

        public SortedDictionary<int, JiraNamedEntity> getIssueTypes(JiraServer server)
        {
            lock(issueTypeCache)
            {
                if (issueTypeCache.ContainsKey(server.GUID))
                {
                    return issueTypeCache[server.GUID];
                }
            }
            return null;
        }

        public void addIssueType(JiraServer server, JiraNamedEntity issueType)
        {
            lock(issueTypeCache)
            {
                if (!issueTypeCache.ContainsKey(server.GUID))
                {
                    issueTypeCache[server.GUID] = new SortedDictionary<int, JiraNamedEntity>();
                }
                issueTypeCache[server.GUID][issueType.Id] = issueType;
            }
        }

        public void clearIssueTypes()
        {
            lock(issueTypeCache)
            {
                issueTypeCache.Clear();
            }
        }

        public SortedDictionary<int, JiraNamedEntity> getPriorities(JiraServer server)
        {
            lock (priorityCache)
            {
                if (priorityCache.ContainsKey(server.GUID))
                {
                    return priorityCache[server.GUID];
                }
            }
            return null;
        }

        public void addPriority(JiraServer server, JiraNamedEntity priority)
        {
            lock (priorityCache)
            {
                if (!priorityCache.ContainsKey(server.GUID))
                {
                    priorityCache[server.GUID] = new SortedDictionary<int, JiraNamedEntity>();
                }
                priorityCache[server.GUID][priority.Id] = priority;
            }
        }

        public void clearPriorities()
        {
            lock (priorityCache)
            {
                priorityCache.Clear();
            }
        }

        public SortedDictionary<int, JiraNamedEntity> getStatues(JiraServer server)
        {
            lock (statusCache)
            {
                if (statusCache.ContainsKey(server.GUID))
                {
                    return statusCache[server.GUID];
                }
            }
            return null;
        }

        public void addStatus(JiraServer server, JiraNamedEntity status)
        {
            lock (statusCache)
            {
                if (!statusCache.ContainsKey(server.GUID))
                {
                    statusCache[server.GUID] = new SortedDictionary<int, JiraNamedEntity>();
                }
                statusCache[server.GUID][status.Id] = status;
            }
        }

        public void clearStatuses()
        {
            lock(statusCache)
            {
                statusCache.Clear();
            }
        }
    }
}
