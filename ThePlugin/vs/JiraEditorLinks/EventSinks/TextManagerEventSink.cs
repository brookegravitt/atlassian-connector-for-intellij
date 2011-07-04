using System;
using System.Collections.Generic;
using Microsoft.VisualStudio;
using Microsoft.VisualStudio.OLE.Interop;
using Microsoft.VisualStudio.TextManager.Interop;

namespace Atlassian.JiraEditorLinks.EventSinks
{
    internal sealed class TextManagerEventSink : IVsTextManagerEvents
    {
        private readonly Dictionary<IVsTextLines, int> documentViewCounts = new Dictionary<IVsTextLines, int>();

        #region IVsTextManagerEvents Members

        public void OnRegisterMarkerType(int iMarkerType)
        {
        }

        public void OnRegisterView(IVsTextView pView)
        {
            // We have to keep track of the number of views that are currently open per
            // document. That way we can discover when a document is opened and insert
            // our custom text markers.
            IVsTextLines textLines;
            ErrorHandler.ThrowOnFailure(pView.GetBuffer(out textLines));
            if (textLines == null)
                return;

            // Increment the stored view count.
            int documentViewCount;
            documentViewCounts.TryGetValue(textLines, out documentViewCount);
            documentViewCounts[textLines] = documentViewCount + 1;

            // If this view belongs to a document that had no views before the document
            // has been opened. It's time to notify the JiraEditorLinkManager about it.
            // However there is a problem: The text buffer represented by textLines has
            // not been initialized yet, i.e. the file name is not set and the file
            // content is not loaded yet. So we need to subscribe to the text buffer to
            // get notified when the file load procedure completed.
            if (documentViewCount != 0) return;

            TextBufferDataEventSink textBufferDataEventSink = new TextBufferDataEventSink();
            IConnectionPoint connectionPoint;
            uint cookie;

            IConnectionPointContainer textBufferData = (IConnectionPointContainer)textLines;
            Guid interfaceGuid = typeof(IVsTextBufferDataEvents).GUID;
            textBufferData.FindConnectionPoint(ref interfaceGuid, out connectionPoint);
            connectionPoint.Advise(textBufferDataEventSink, out cookie);

            textBufferDataEventSink.TextLines = textLines;
            textBufferDataEventSink.ConnectionPoint = connectionPoint;
            textBufferDataEventSink.Cookie = cookie;
        }

        public void OnUnregisterView(IVsTextView pView)
        {
            // It's interesting to us when a document is closed because we need this
            // information to keep track when a document is opened. Furthermore we
            // have to free all text markers.
            IVsTextLines textLines;
            ErrorHandler.ThrowOnFailure(pView.GetBuffer(out textLines));
            if (textLines == null)
                return;

            // Decrement the stored view count. This is a little bit special as we use
            // IVsTextLines instances as keys in our dictionary. That means that we
            // have to remove the whole entry from the dictionary when the counter drops
            // to zero to prevent memory leaks.
            int documentViewCount;
            
            if (!documentViewCounts.TryGetValue(textLines, out documentViewCount)) return;

            if (documentViewCount > 1)
            {
                // There are several open views for the same document. In this case
                // we only have to decrement the view count.
                documentViewCounts[textLines] = documentViewCount - 1;
            }
            else
            {
                // When we reach this branch the last view of a document has been
                // closed. That means we have to free the whole IVsTextLines reference
                // by removing it from the dictionary.
                documentViewCounts.Remove(textLines);

                // Notify the CloneDetectiveManager of this event.
                JiraEditorLinkManager.OnDocumentClosed(textLines);
            }
        }

        public void OnUserPreferencesChanged(VIEWPREFERENCES[] pViewPrefs, FRAMEPREFERENCES[] pFramePrefs, LANGPREFERENCES[] pLangPrefs, FONTCOLORPREFERENCES[] pColorPrefs)
        {
        }

        #endregion
    }
}