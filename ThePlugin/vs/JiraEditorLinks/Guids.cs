// Guids.cs
// MUST match guids.h
using System;

namespace Atlassian.JiraEditorLinks
{
    static class GuidList
    {
        public const string GUID_JIRA_LINKS_PKG_STRING = "19298e21-4f08-404e-b87d-fc938afd3350";

        public const string GUID_JIRA_LINK_MARKER_SERVICE_STRING = "34D3D2C5-60CD-4d79-8BD8-7759EBB3C27A";

        public const string GUID_JIRA_LINKS_CMD_SET_STRING = "55884d4c-11b5-4d68-8946-8f4a9ea88741";
        
        public const string JIRA_LINK_BACKGROUND_MARKER_STRING = "658DDF58-FC14-4db9-8110-B52A6845B6CF";
        
        public const string JIRA_LINK_MARGIN_MARKER = "D7F03136-206D-4674-ADC7-DA0E9EE38869";

        public static readonly Guid GuidFontsAndColorsTextEditor = new Guid("a27b4e24-a735-4d1d-b8e7-9716e1e3d8e0");

        public static readonly Guid GuidJiraLinksCmdSet = new Guid(GUID_JIRA_LINKS_CMD_SET_STRING);

        public static readonly Guid JiraLinkBackgroundMarker = new Guid(JIRA_LINK_BACKGROUND_MARKER_STRING);

        public static readonly Guid JiraLinkMarginMarker = new Guid(JIRA_LINK_MARGIN_MARKER);
    };
}