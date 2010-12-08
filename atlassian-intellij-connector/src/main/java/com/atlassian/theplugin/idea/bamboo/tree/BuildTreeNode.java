/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.theplugin.idea.bamboo.tree;

import com.atlassian.connector.intellij.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.util.DateUtil;
import com.atlassian.theplugin.idea.bamboo.BuildListModel;
import com.atlassian.theplugin.idea.ui.tree.paneltree.SelectableHoverLabel;
import com.atlassian.theplugin.util.Util;
import com.intellij.util.ui.UIUtil;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.lang.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Jacek Jaroczynski, but totally messed up by jgorycki :)
 */
public class BuildTreeNode extends AbstractBuildTreeNode {

    private static final int MAX_TOOLTIP_WIDTH = 500;
    private static final String BODY_WITH_STYLE =
            "<body style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif\">";


    private BambooBuildAdapter build;
    public static final String CODE_HAS_CHANGED = "Code has changed";
    private double reasonWidth;
    private double serverWidth;
    private double dateWidth;
    private double planInProgressWidth;
    private static final int LABEL_PADDING = 5;
    private boolean hover = false;
    private RendererPanel renderer;


    public BuildTreeNode(final BuildListModel buildModel, final BambooBuildAdapter build) {
        super(build.getPlanKey(), null, null);

        this.build = build;
        renderer = new RendererPanel();

        recalculateColumnWidths(buildModel);
    }

    private final class RendererPanel extends JPanel {
        private JPanel detailsPanel;
        private SelectableHoverLabel descriptionLabel;
        private SelectableHoverLabel planIsBuilding;
        ///details
        private SelectableHoverLabel empty0;
        private SelectableHoverLabel empty1;
        private SelectableHoverLabel reason;
        private SelectableHoverLabel empty2;
        private SelectableHoverLabel server;
        private SelectableHoverLabel empty3;
        private SelectableHoverLabel date;
        private JPanel padding;
        private JLabel buildStatusIcon;

        RendererPanel() {
            super(new FormLayout("pref, 1dlu, fill:min(pref;150px):grow, right:pref", "pref"));
            CellConstraints cc = new CellConstraints();
            planIsBuilding =  new SelectableHoverLabel(false, hover, true, build.getPlanStateString(), null,
                    SwingConstants.TRAILING, ICON_HEIGHT);

            reason = new SelectableHoverLabel(false, hover, true, getBuildReasonString(build), null,
                    SwingConstants.LEADING, ICON_HEIGHT);
            empty2 = new SelectableHoverLabel(false, hover, true, "", null,
                    SwingConstants.LEADING, ICON_HEIGHT);
            empty1 = new SelectableHoverLabel(false, hover, true, "", null,
                    SwingConstants.LEADING, ICON_HEIGHT);
            empty0 = new SelectableHoverLabel(false, hover, true, "", null,
                    SwingConstants.LEADING, ICON_HEIGHT);
            
            setBackground(UIUtil.getTreeTextBackground());

            buildStatusIcon = new JLabel(build.getBuildIcon());
            add(buildStatusIcon, cc.xy(1, 1));


            String description = getDescriptionMessage();
            descriptionLabel = new SelectableHoverLabel(false, hover, false, description, null,
                    SwingConstants.TRAILING, ICON_HEIGHT);
            add(descriptionLabel, cc.xy(2 + 1, 1));

            detailsPanel = createPanelForOtherBuildDetails(false, hover, true);
            add(detailsPanel, cc.xy(2 + 2, 1));
            addTooltipToPanel(build, this);
        }

        void reformatPanel(boolean selected, boolean enabled) {
            buildStatusIcon.setIcon(build.getBuildIcon());
            descriptionLabel.setBackground(SelectableHoverLabel.getBgColor(selected, hover));
            descriptionLabel.setEnabled(enabled);
            planIsBuilding.setText(build.getPlanStateString());

            planIsBuilding.setSelected(selected, hover, enabled);
            setFixedComponentSize(planIsBuilding,
                    Double.valueOf(planInProgressWidth).intValue() + LABEL_PADDING, ICON_HEIGHT);
            empty0.setSelected(selected, hover, enabled);
            empty1.setSelected(selected, hover, enabled);
            reason.setSelected(selected, hover, enabled);
            empty2.setSelected(selected, hover, enabled);

            setFixedComponentSize(reason, Double.valueOf(reasonWidth).intValue() + LABEL_PADDING, ICON_HEIGHT);
            server.setSelected(selected, hover, enabled);
            setFixedComponentSize(server, Double.valueOf(serverWidth).intValue() + LABEL_PADDING, ICON_HEIGHT);
            empty3.setSelected(selected, hover, enabled);
            date.setSelected(selected, hover, enabled);
            setFixedComponentSize(date, Double.valueOf(dateWidth).intValue() + LABEL_PADDING, ICON_HEIGHT);

            padding.setBackground(SelectableHoverLabel.getBgColor(selected, hover));
            padding.setForeground(SelectableHoverLabel.getFgColor(selected, enabled));
            this.revalidate();
            this.repaint();
        }

        private String getDescriptionMessage() {
            StringBuilder sb = new StringBuilder();
            sb.append(build.getPlanKey());
            if (build.isValid()) {
                sb.append("-").append(build.getBuildNumberAsString());
            }
            //+- failed tests
            if (build.getStatus() == BuildStatus.FAILURE) {
                sb.append("    ").append(build.getTestsFailed()).append(" Tests Failed");
            }
            return sb.toString();
        }


        private JPanel createPanelForOtherBuildDetails(boolean selected, boolean aHover, boolean enabled) {



            detailsPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx++;
            gbc.gridy = 0;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.LINE_END;

            // gap
//             empty1 = new SelectableHoverLabel(selected, aHover, enabled, "", null,
//                    SwingConstants.LEADING, ICON_HEIGHT);
            empty1.setSelected(selected, aHover, enabled);
            setFixedComponentSize(empty1, 2 * GAP, ICON_HEIGHT);
            detailsPanel.add(empty1, gbc);
            // is plan buiding
            gbc.gridx++;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            planIsBuilding.setSelected(selected, aHover, enabled);
            detailsPanel.add(planIsBuilding, gbc);
            setFixedComponentSize(planIsBuilding, Double.valueOf(planInProgressWidth).intValue() + LABEL_PADDING, ICON_HEIGHT);
            // gap
            gbc.gridx++;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            empty0.setSelected(selected, aHover, enabled);

            detailsPanel.add(empty0, gbc);
            setFixedComponentSize(empty0, 2 * GAP, ICON_HEIGHT);
            // reason
            gbc.gridx++;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
//            reason = new SelectableHoverLabel(selected, aHover, enabled, getBuildReasonString(build), null,
//                    SwingConstants.LEADING, ICON_HEIGHT);
            reason.setSelected(selected, aHover, enabled);
            setFixedComponentSize(reason, Double.valueOf(reasonWidth).intValue() + LABEL_PADDING, ICON_HEIGHT);
            reason.setHorizontalAlignment(SwingConstants.RIGHT);
            detailsPanel.add(reason, gbc);

            // gap
            gbc.gridx++;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            empty2.setSelected(selected, hover, enabled);

//            empty2 = new SelectableHoverLabel(selected, aHover, enabled, "", null,
//                    SwingConstants.LEADING, ICON_HEIGHT);
            setFixedComponentSize(empty2, 2 * GAP, ICON_HEIGHT);
            detailsPanel.add(empty2, gbc);

            // server
            gbc.gridx++;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.LINE_END;
            server = new SelectableHoverLabel(selected, aHover, enabled, getBuildServerString(build), null,
                    SwingConstants.LEADING, ICON_HEIGHT);
            setFixedComponentSize(server, Double.valueOf(serverWidth).intValue() + LABEL_PADDING, ICON_HEIGHT);
            server.setHorizontalAlignment(SwingConstants.RIGHT);
            detailsPanel.add(server, gbc);

            // gap
            gbc.gridx++;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            empty3 = new SelectableHoverLabel(selected, aHover, enabled, "", null,
                    SwingConstants.LEADING, ICON_HEIGHT);
            setFixedComponentSize(empty3, 2 * GAP, ICON_HEIGHT);
            detailsPanel.add(empty3, gbc);

            // date
            gbc.gridx++;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.LINE_END;
            String relativeBuildDate = getRelativeBuildTimeString(build);
            date = new SelectableHoverLabel(selected, aHover, enabled, relativeBuildDate, null,
                    SwingConstants.LEADING, ICON_HEIGHT);
            setFixedComponentSize(date, Double.valueOf(dateWidth).intValue() + LABEL_PADDING, ICON_HEIGHT);
            date.setHorizontalAlignment(SwingConstants.RIGHT);
            detailsPanel.add(date, gbc);

            // padding
            padding = new JPanel();
            gbc.gridx++;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            padding.setPreferredSize(new Dimension(RIGHT_PADDING, ICON_HEIGHT));
            padding.setMinimumSize(new Dimension(RIGHT_PADDING, ICON_HEIGHT));
            padding.setMaximumSize(new Dimension(RIGHT_PADDING, ICON_HEIGHT));

            padding.setBackground(SelectableHoverLabel.getBgColor(selected, aHover));
            padding.setForeground(SelectableHoverLabel.getFgColor(selected, enabled));

            padding.setOpaque(true);
            detailsPanel.add(padding, gbc);

            return detailsPanel;
        }

    }

    private void recalculateColumnWidths(final BuildListModel buildModel) {
        JLabel l = new JLabel();

        reasonWidth = 0.0;
        serverWidth = 0.0;
        dateWidth = 0.0;
        planInProgressWidth = 0.0;

        for (BambooBuildAdapter b : buildModel.getBuilds()) {
            // PL-1202 - argument to TextLayout must be a non-empty string
            String reason = getBuildReasonString(b);
            TextLayout layoutStatus = new TextLayout(reason.length() > 0 ? reason : ".",
                    l.getFont(), new FontRenderContext(null, true, true));
            reasonWidth = Math.max(layoutStatus.getBounds().getWidth(), reasonWidth);
            String server = getBuildServerString(b);
            TextLayout layoutName = new TextLayout(server.length() > 0 ? server : ".",
                    l.getFont(), new FontRenderContext(null, true, true));
            serverWidth = Math.max(layoutName.getBounds().getWidth(), serverWidth);
            String date = getRelativeBuildTimeString(b);
            TextLayout layoutDate = new TextLayout(date.length() > 0 ? date : ".",
                    l.getFont(), new FontRenderContext(null, true, true));
            dateWidth = Math.max(layoutDate.getBounds().getWidth(), dateWidth);

            TextLayout planStatus = new TextLayout(
                    build.getPlanStateString().length() > 0 ? build.getPlanStateString() : " ",
                    l.getFont(), new FontRenderContext(null, true, true));
            planInProgressWidth =  Math.max(planStatus.getBounds().getWidth(), planInProgressWidth);
        }
    }

    @Override
    public BambooBuildAdapter getBuild() {
        return build;
    }

    @Override
    public String toString() {
        return build.getPlanKey();
    }

    @Override
    public JComponent getRenderer(final JComponent c, final boolean selected,
                                  final boolean expanded, final boolean hasFocus) {
        renderer.reformatPanel(selected, c.isEnabled());
        return renderer;
    }

    @NotNull
    private static String getBuildReasonString(BambooBuildAdapter build) {
        StringBuilder sb = new StringBuilder();

        String commiters = getCommiters(build);

        if (!build.getReason().equals(CODE_HAS_CHANGED) || commiters.length() == 0) {
            sb.append(build.getReason());
        }

        // commiters
        if (commiters.length() > 0 && build.getReason().equals(CODE_HAS_CHANGED)) {
            sb.append("Changes by: ").append(commiters);
        }

        return sb.toString();
    }


    @NotNull
    private static String getRelativeBuildTimeString(BambooBuildAdapter build) {
        return DateUtil.getRelativePastDate(build.getCompletionDate());
    }

    @NotNull
    private static String getBuildServerString(BambooBuildAdapter build) {
        return "(" + build.getServer().getName() + ")";
    }

    private static String getCommiters(BambooBuildAdapter build) {
        StringBuilder commiters = new StringBuilder();

        Collection<String> c = build.getCommiters();
        for (Iterator<String> iterator = c.iterator(); iterator.hasNext();) {
            commiters.append(iterator.next());
            if (iterator.hasNext()) {
                commiters.append(", ");
            }
        }
        return commiters.toString();
    }

    public static void addTooltipToPanel(BambooBuildAdapter build, JPanel p) {
        final JToolTip jToolTip = p.createToolTip();
        jToolTip.setTipText(buildTolltip(build, 0));
        final int prefWidth = jToolTip.getPreferredSize().width;
        int width = prefWidth > MAX_TOOLTIP_WIDTH ? MAX_TOOLTIP_WIDTH : 0;
        p.setToolTipText(buildTolltip(build, width));
    }

    private static String buildTolltip(BambooBuildAdapter build, int width) {
        StringBuilder sb = new StringBuilder(
                "<html>"
                        + BODY_WITH_STYLE);
        final String widthString = width > 0 ? "width='" + width + "px'" : "";
        sb.append("<table ").append(widthString).append(" align='center' cols='2'>");
        sb.append("<tr><td colspan='2'><b><font color='blue'>");
        sb.append(build.getPlanKey());
        if (build.isValid()) {
            sb.append("-").append(build.getNumber());
        }
        sb.append("</font></b>");

        sb.append("<tr><td valign=\"top\"><b>Name:</b></td><td valign=\"top\">");

        sb.append(StringEscapeUtils.escapeHtml(build.getPlanName()));
        sb.append("</td></tr>");

        sb.append("<tr><td valign=\"top\"><b>Project:</b></td><td valign=\"top\">");

        String project = build.getProjectName();
        sb.append(StringEscapeUtils.escapeHtml(project));
        sb.append("</td></tr>");

        sb.append("<tr><td valign=\"top\"><b>Reason:</b></td><td valign=\"top\">");
        sb.append(build.getReason());
        sb.append("</td></tr>");

        if (getCommiters(build).length() > 0) {
            sb.append("<tr><td valign=\"top\"><b>Commiters:</b></td><td valign=\"top\">");
            sb.append(getCommiters(build));
            sb.append("</td></tr>");
        }

        sb.append("<tr><td valign=\"top\"><b>Server:</b></td><td valign=\"top\">");
        sb.append(build.getServerName());
        sb.append("</td></tr>");

        sb.append("<tr><td valign=\"top\"><b>Build Date:</b></td><td valign=\"top\">");
        String date = getRelativeBuildTimeString(build);
        sb.append(StringEscapeUtils.escapeHtml(date).replace("\n", Util.HTML_NEW_LINE).replace(" ", "&nbsp;")
                .replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;"));

        sb.append("</td></tr>");

        sb.append("</table>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private static void setFixedComponentSize(JComponent c, int width, int height) {
        c.setPreferredSize(new Dimension(width, height));
        c.setMinimumSize(new Dimension(width, height));
        c.setMaximumSize(new Dimension(width, height));
    }

    public void setHover(final boolean b) {
        this.hover = b;
    }

    public boolean isHover() {
        return hover;
    }

}
