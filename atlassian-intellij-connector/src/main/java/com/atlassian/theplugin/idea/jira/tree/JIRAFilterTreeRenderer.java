package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.idea.ui.Entry;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;
import com.atlassian.theplugin.jira.model.JiraCustomFilter;
import com.intellij.openapi.util.IconLoader;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * User: pmaruszak
 */
public class JIRAFilterTreeRenderer extends DefaultTreeCellRenderer {
    private static final Icon JIRA_MANUAL_FILTER_ICON = IconLoader.getIcon("/actions/showViewer.png");
    private static final Icon JIRA_SAVED_FILTER_ICON = IconLoader.getIcon("/actions/showSource.png");
    private static final String TOOLTIP_FOOTER_HTML = "<hr style=\"height: '1'; text-align: 'left'; "
            + "color: 'black'; width: '100%'\">"
            + "<p style=\"font-size:'90%'; color:'grey'\">right click on filter node to edit</p>";

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                                                  boolean expanded, boolean leaf, int row, boolean hasFocus) {

        JComponent c = (JComponent) super.getTreeCellRendererComponent(
                tree, value, selected, expanded, leaf, row, hasFocus);
        setToolTipText(null);

        if (value instanceof JIRAManualFilterTreeNode && c instanceof JLabel) {
            final JIRAManualFilterTreeNode filterTreeNode = (JIRAManualFilterTreeNode) value;
            setToolTipText(createManualFilterToolTipText(filterTreeNode.getManualFilter(),
                    filterTreeNode.getJiraServerCfg()));
            ((JLabel) c).setIcon(JIRA_MANUAL_FILTER_ICON);

            JiraCustomFilter filter = ((JIRAManualFilterTreeNode) value).getManualFilter();
            if (filter != null && filter.isEmpty()) {
                JLabel label = ((JLabel) c);
                label.setText(label.getText() + " (not defined)");               
            }
            return c;
            //return ((JIRAManualFilterTreeNode) value).getRenderer(c, selected, expanded, hasFocus);
        }

        if (value instanceof JIRASavedFilterTreeNode && c instanceof JLabel) {

            ((JLabel) c).setIcon(JIRA_SAVED_FILTER_ICON);
            return c;
        }

        if (value instanceof JiraRecentlyOpenTreeNode && c instanceof JLabel) {
            ((JLabel) c).setIcon(JIRA_SAVED_FILTER_ICON);
            return c;
        }

        if (value instanceof AbstractTreeNode) {
            return ((AbstractTreeNode) value).getRenderer(c, selected, expanded, hasFocus);
        }
        return c;
    }


    private String createManualFilterToolTipText(JiraCustomFilter manualFilter, ServerData server) {

        Collection<Entry> entries = MiscUtil.buildArrayList();
        Map<JiraCustomFilter.QueryElement, ArrayList<String>> map = manualFilter.groupBy(true);
        for (JiraCustomFilter.QueryElement element : map.keySet()) {
            entries.add(new Entry(element.getName(), StringUtils.join(map.get(element), ", ")));
        }

        if (entries.size() == 0) {
            // get also 'any' values
            map = manualFilter.groupBy(false);
            for (JiraCustomFilter.QueryElement element : map.keySet()) {
                entries.add(new Entry(element.getName(), StringUtils.join(map.get(element), ", ")));
            }
        }
        StringBuffer sb = new StringBuffer();
        sb.append("<html>");

        if (entries.size() == 0) {

            sb.append("No Custom Filter Defined");
        } else {

            sb.append("<html><table>");
            sb.append("<tr><td><font color=blue><b>").append(server.getName()).append("</b></td><td></td>");
            for (Entry entry : entries) {
                sb.append("<tr><td><b>").append(entry.getLabel()).append(":")
                        .append("</b></td><td>");
                if (entry.isError()) {
                    sb.append("<font color=red>");
                }
                sb.append(entry.getValue()).append("</td></tr>");
            }
            sb.append("</table>");
        }
        sb.append(TOOLTIP_FOOTER_HTML);
        sb.append("</html>");
        return sb.toString();
    }

}
