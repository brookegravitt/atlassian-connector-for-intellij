using System;
using System.Diagnostics;
using System.Windows.Forms;
using Microsoft.VisualStudio;
using Microsoft.VisualStudio.OLE.Interop;
using Microsoft.VisualStudio.TextManager.Interop;

namespace Atlassian.JiraEditorLinks.EventSinks
{
    public sealed class TextMarkerClientEventSink : IVsTextMarkerClient, IVsTextMarkerClientAdvanced, IVsTextMarkerClientEx 
    {
//        private IVsTextLineMarker marginMarker;

        public IVsTextLineMarker MarginMarker { get; set; }
        public IVsTextLineMarker BackgroundMarker { get; set; }
        //        {
//            set { marginMarker = value; }
//        }

        #region IVsTextMarkerClient Members

        public void MarkerInvalidated()
        {
            JiraEditorLinkManager.OnMarkerInvalidated(MarginMarker);
            JiraEditorLinkManager.OnMarkerInvalidated(BackgroundMarker);
        }

        public int GetTipText(IVsTextMarker pMarker, string[] pbstrText)
        {
            pbstrText[0] = "Right-click to navigate to PL-1357";
            return VSConstants.S_OK;
        }

        public void OnBufferSave(string pszFileName)
        {
        }

        public void OnBeforeBufferClose()
        {
        }

        public int OnMarkerTextChanged(IVsTextMarker pMarker)
        {
            return VSConstants.S_OK;
        }

        public int MarkerInvalidated(IVsTextLines pBuffer, IVsTextMarker pMarker)
        {
            return VSConstants.S_OK;
        }

        public int OnHoverOverMarker(IVsTextView pView, IVsTextMarker pMarker, int fShowUI)
        {
            Debug.WriteLine("cursor hovers over marker");
            return VSConstants.S_OK;
        }

        public int GetMarkerCommandInfo(IVsTextMarker pMarker, int iItem, string[] pbstrText, uint[] pcmdf)
        {
            // For each command we add we have to specify that we support it.
            // Furthermore it should always be enabled.
            const uint menuItemFlags = (uint)(OLECMDF.OLECMDF_SUPPORTED | OLECMDF.OLECMDF_ENABLED);

            if (pcmdf != null)
                pcmdf[0] = menuItemFlags;

            if (pbstrText == null)
                return VSConstants.S_OK;

            switch (iItem)
            {
                case 0:
                    pbstrText[0] = "Open Issue in browser";
//                    pcmdf[0] = menuItemFlags;
                    return VSConstants.S_OK;

//                case 1:
//                    pbstrText[0] = Res.CommandShowCloneIntersections;
//                    pcmdf[0] = menuItemFlags;
//                    return VSConstants.S_OK;

                default:
                    return VSConstants.S_FALSE;
            }
        }

        public int ExecMarkerCommand(IVsTextMarker pMarker, int iItem)
        {
            switch (iItem)
            {
                case 0:
                    try
                    {
                        Process.Start("https://studio.atlassian.com/browse/PL-1357");
                    }
                    catch (Exception)
                    {
                    }
                    return VSConstants.S_OK;

//                case 1:
//                    CloneDetectiveManager.ShowCloneIntersections();
//                    return VSConstants.S_OK;

                default:
                    return VSConstants.S_FALSE;
            }
        }

        public void OnAfterSpanReload()
        {
        }

        public int OnAfterMarkerChange(IVsTextMarker pMarker)
        {
             return VSConstants.S_OK;
        }

        #endregion
    }
}