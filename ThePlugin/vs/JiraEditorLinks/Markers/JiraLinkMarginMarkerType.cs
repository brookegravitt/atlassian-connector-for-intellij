using System;
using Microsoft.VisualStudio;
using Microsoft.VisualStudio.OLE.Interop;
using Microsoft.VisualStudio.TextManager.Interop;

namespace Atlassian.JiraEditorLinks.Markers
{
    public sealed class JiraLinkMarginMarkerType : IVsPackageDefinedTextMarkerType, IVsMergeableUIItem//, IVsHiColorItem
    {
        private const string JIRA_LINK_MARGIN = "JIRA Link (Margin)";

        public static int Id { get; internal set; }

        #region IVsPackageDefinedTextMarkerType Members

        public int GetVisualStyle(out uint pdwVisualFlags)
        {
            //pdwVisualFlags = (uint)MARKERVISUAL.MV_COLOR_ALWAYS;

            pdwVisualFlags = (uint)MARKERVISUAL.MV_LINE | (uint)MARKERVISUAL.MV_TIP_FOR_BODY | (uint)MARKERVISUAL.MV_COLOR_ALWAYS ;
//            pdwVisualFlags = (uint)MARKERVISUAL.MV_TIP_FOR_BODY | (uint)MARKERVISUAL.MV_COLOR_ALWAYS;

            return VSConstants.S_OK;
        }

        public int GetDefaultColors(COLORINDEX[] piForeground, COLORINDEX[] piBackground)
        {
            piForeground[0] = COLORINDEX.CI_DARKBLUE;
            piBackground[0] = COLORINDEX.CI_WHITE;
            return VSConstants.S_OK;

            // This method won't be called as we implement the IVsHiColorItem interfacce.
//            return VSConstants.E_NOTIMPL;
        }

        public int GetDefaultLineStyle(COLORINDEX[] piLineColor, LINESTYLE[] piLineIndex)
        {
            piLineColor[0] = COLORINDEX.CI_DARKBLUE;
            piLineIndex[0] = LINESTYLE.LI_SOLID;

            return VSConstants.S_OK;
        }

        public int GetDefaultFontFlags(out uint pdwFontFlags)
        {
            pdwFontFlags = (uint) FONTFLAGS.FF_DEFAULT;

            return VSConstants.S_OK;
        }

        public int GetBehaviorFlags(out uint pdwFlags)
        {
//            pdwFlags = (uint)MARKERBEHAVIORFLAGS2.MB_INHERIT_BACKGROUND;

            pdwFlags = (uint) MARKERBEHAVIORFLAGS.MB_TRACK_EDIT_ON_RELOAD;

            return VSConstants.S_OK;
        }

        public int GetPriorityIndex(out int piPriorityIndex)
        {
            piPriorityIndex = (int)MARKERTYPE.MARKER_READONLY;
//            piPriorityIndex = (int) MARKERTYPE.MARKER_BOOKMARK;
//            piPriorityIndex = (int) MARKERTYPE.MARKER_SHORTCUT;
//            piPriorityIndex = (int) MARKERTYPE.MARKER_CODESENSE_ERROR;

            return VSConstants.S_OK;
        }

        public int DrawGlyphWithColors(IntPtr hdc, RECT[] pRect, int iMarkerType,
                                       IVsTextMarkerColorSet pMarkerColors, uint dwGlyphDrawFlags, int iLineHeight)
        {
            return VSConstants.S_OK;
        }

        #endregion

        #region IVsMergeableUIItem Members

        public int GetCanonicalName(out string pbstrNonLocalizeName)
        {
            pbstrNonLocalizeName = JIRA_LINK_MARGIN;

            return VSConstants.S_OK;
        }

        public int GetDisplayName(out string pbstrDisplayName)
        {
            // This string is displayed in the "Fonts and Colors" section
            // of the Visual Studio Options dialog.
            pbstrDisplayName = JIRA_LINK_MARGIN;

            return VSConstants.S_OK;
        }

        public int GetMergingPriority(out int piMergingPriority)
        {
            piMergingPriority = 0;

            return VSConstants.S_OK;
        }

        public int GetDescription(out string pbstrDesc)
        {
            pbstrDesc = JIRA_LINK_MARGIN;

            return VSConstants.S_OK;
        }

        #endregion

//        #region IVsHiColorItem Members

//        public int GetColorData(int cdElement, out uint pcrColor)
//        {
//            __tagVSCOLORDATA colorData = (__tagVSCOLORDATA)cdElement;

//            switch (colorData)
//            {
//                case __tagVSCOLORDATA.CD_FOREGROUND:
//                case __tagVSCOLORDATA.CD_LINECOLOR:
//                     Purple.
//                    pcrColor = 0x00800080;
//                    break;

//                case __tagVSCOLORDATA.CD_BACKGROUND:
                    // White.
//                    pcrColor = 0x00FFFFFF;
//                    pcrColor = 0x00FFFF00;
//                    break;

//                default:
//                    throw ExceptionBuilder.UnhandledCaseLabel(colorData);
//            }

//            return VSConstants.S_OK;
//        }

//        #endregion
    }
}