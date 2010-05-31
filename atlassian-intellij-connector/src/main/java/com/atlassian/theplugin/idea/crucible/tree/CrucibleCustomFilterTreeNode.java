package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.api.model.BasicProject;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.idea.ui.Entry;
import com.atlassian.theplugin.idea.ui.tree.paneltree.AbstractTreeNode;
import com.atlassian.theplugin.idea.ui.tree.paneltree.SelectableLabel;

import javax.swing.*;
import java.util.Collection;

/**
 * User: pmaruszak
 */
public class CrucibleCustomFilterTreeNode extends AbstractTreeNode {
    private final ProjectCfgManager projectCfgManager;
    private final CustomFilter filter;
    private final CrucibleReviewListModel reviewListModel;
    private static final SelectableLabel SELECTABLE_LABEL = new SelectableLabel(false, true, null, "", ICON_HEIGHT);
    private static final String NAME = "Custom Filter";
    private static final String TOOLTIP_FOOTER_HTML = "<hr style=\"height: '1'; text-align: 'left'; "
            + "color: 'black'; width: '100%'\">"
            + "<p style=\"font-size:'90%'; color:'grey'\">right click on filter node to edit</p>";

    public CrucibleCustomFilterTreeNode(final ProjectCfgManager projectCfgManager, CustomFilter filter,
                                        CrucibleReviewListModel reviewListModel) {
        super(NAME, null, null);
        this.projectCfgManager = projectCfgManager;
        this.filter = filter;
        this.reviewListModel = reviewListModel;


    }

    @Override
	public String toString() {
        int cnt = reviewListModel.getReviewCount(filter);
        String txt = NAME;
        if (cnt > -1) {
            txt += " (" + cnt + ")";
        }

        if (filter.isEmpty()) {
            txt += " (not defined)";
        }
        return txt;
    }

    @Override
	public JComponent getRenderer(JComponent c, boolean selected, boolean expanded, boolean hasFocus) {
        String txt = selected ? toString() : NAME;
        SELECTABLE_LABEL.setToolTipText(buildToolTipText());
        SELECTABLE_LABEL.setSelected(selected);
        SELECTABLE_LABEL.setEnabled(c.isEnabled());
        SELECTABLE_LABEL.setFont(c.getFont());
        SELECTABLE_LABEL.setText(txt);
        
        if (UIManager.getDimension(SELECTABLE_LABEL) != null) {
            SELECTABLE_LABEL.setPreferredSize(UIManager.getDimension(SELECTABLE_LABEL));
        } else {
            SelectableLabel tmp = new SelectableLabel(selected, c.isEnabled(), c.getFont(), txt, ICON_HEIGHT);
            SELECTABLE_LABEL.setPreferredSize(tmp.getPreferredSize());
            tmp = null;
        }

        return SELECTABLE_LABEL;
    }

    public CustomFilter getFilter() {
        return filter;
    }

    private String buildToolTipText() {
        Collection<Entry> entries;
        StringBuffer sb = new StringBuffer();
        sb.append("<html>");

        entries = getEntries(true);

        if (filter.isEmpty()) {

            sb.append("No Custom Filter Defined");
        } else {

            sb.append("<html><table>");
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


    private Collection<Entry> getEntries(boolean fetchRemoteData) {
		final Collection<Entry> myEntries = MiscUtil.buildArrayList();

		final ServerId serverId = filter.getServerId();
		final ServerData server = projectCfgManager.getCrucibleServerr(serverId);

		myEntries.add(new Entry("Server",
				(server != null ? server.getName() : "Server Unknown or Removed"), server == null));
		if (filter.getProjectKey() != null && filter.getProjectKey().length() > 0) {
			String projectName = filter.getProjectKey() + " <i>(fetching full name...)</i>";
			if (fetchRemoteData) {
				try {
					BasicProject crucibleProject = server != null
							? IntelliJCrucibleServerFacade.getInstance().getProject(server, filter.getProjectKey())
							: null;
					if (crucibleProject != null) {
						projectName = crucibleProject.getName();
					}
				} catch (RemoteApiException e) {
					// nothing here
				} catch (ServerPasswordNotProvidedException e) {
					// nothing here
				}
			}
			myEntries.add(new Entry("Project", projectName));
		}

		final State[] selStates = filter.getState();
		if (selStates != null && selStates.length > 0) {
			final StringBuilder states = new StringBuilder();
			for (int i = 0; i < selStates.length; i++) {
				states.append(selStates[i].getDisplayName());
				if (i < selStates.length - 1) {
					states.append(", ");
				}
			}
			myEntries.add(new Entry("State", states.toString()));
		}
		addIfNotEmpty(filter.getAuthor(), "Author", myEntries, server, fetchRemoteData);
		addIfNotEmpty(filter.getModerator(), "Moderator", myEntries, server, fetchRemoteData);
		addIfNotEmpty(filter.getCreator(), "Creator", myEntries, server, fetchRemoteData);
		addIfNotEmpty(filter.getReviewer(), "Reviewer", myEntries, server, fetchRemoteData);

		final Boolean reviewerStatus = (filter.getReviewer() != null && filter.getReviewer().length() > 0)
				? filter.isComplete() : filter.isAllReviewersComplete();
		if (reviewerStatus != null) {
			myEntries.add(new Entry("Reviewer Status", reviewerStatus ? "Complete" : "Incomplete"));
		}

		final Boolean orRoles = filter.isOrRoles();
		myEntries.add(new Entry("Match Roles", (orRoles == null || orRoles) ? "Any" : "All"));

		return myEntries;
	}

	private void addIfNotEmpty(String username, String name, Collection<Entry> entriesToFill,
			ServerData serverCfg, boolean fetchRemoteData) {
		if (username.length() > 0) {

			final String displayName = fetchRemoteData
					? serverCfg != null ? IntelliJCrucibleServerFacade.getInstance().getDisplayName(serverCfg, username) : null
					: username;
			entriesToFill.add(new Entry(name, displayName != null ? displayName : username));
		}
	}
}
