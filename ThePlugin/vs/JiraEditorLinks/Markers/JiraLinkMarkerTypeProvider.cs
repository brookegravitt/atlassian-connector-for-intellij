using System;
using Microsoft.VisualStudio;
using Microsoft.VisualStudio.TextManager.Interop;
using System.Runtime.InteropServices;

namespace Atlassian.JiraEditorLinks.Markers
{
    [Guid(GuidList.GUID_JIRA_LINK_MARKER_SERVICE_STRING)]
    public sealed class JiraLinkMarkerTypeProvider : IVsTextMarkerTypeProvider
    {
        private readonly JiraLinkBackgroundMarkerType backgroundMarkerType = new JiraLinkBackgroundMarkerType();
        private readonly JiraLinkMarginMarkerType marginMarkerType = new JiraLinkMarginMarkerType();

        public int GetTextMarkerType(ref Guid pguidMarker, out IVsPackageDefinedTextMarkerType ppMarkerType)
        {
            // This method is called by Visual Studio when it needs the marker
            // type information in order to retrieve the implementing objects.
            if (pguidMarker == GuidList.JiraLinkBackgroundMarker)
            {
                ppMarkerType = backgroundMarkerType;
                return VSConstants.S_OK;
            }
            
            if (pguidMarker == GuidList.JiraLinkMarginMarker)
            {
                ppMarkerType = marginMarkerType;
                return VSConstants.S_OK;
            }

            ppMarkerType = null;
            return VSConstants.E_FAIL;
        }

        internal static void InitializeMarkerIds(JiraEditorLinksPackage package)
        {
            // Retrieve the Text Marker IDs. We need them to be able to create instances.
            IVsTextManager textManager = (IVsTextManager)package.GetService(typeof(SVsTextManager));

            int markerId;
            Guid markerGuid = GuidList.JiraLinkBackgroundMarker;
            ErrorHandler.ThrowOnFailure(textManager.GetRegisteredMarkerTypeID(ref markerGuid, out markerId));
            JiraLinkBackgroundMarkerType.Id = markerId;

            markerGuid = GuidList.JiraLinkMarginMarker;
            ErrorHandler.ThrowOnFailure(textManager.GetRegisteredMarkerTypeID(ref markerGuid, out markerId));
            JiraLinkMarginMarkerType.Id = markerId;
        }
    }
}