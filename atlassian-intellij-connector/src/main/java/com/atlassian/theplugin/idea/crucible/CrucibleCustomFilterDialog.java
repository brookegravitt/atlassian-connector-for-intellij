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
package com.atlassian.theplugin.idea.crucible;

import com.atlassian.connector.intellij.crucible.CrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.theplugin.commons.UiTask;
import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.config.CrucibleProjectWrapper;
import com.atlassian.theplugin.idea.config.CrucibleServerCfgWrapper;
import com.atlassian.theplugin.idea.config.GenericComboBoxItemWrapper;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.panels.VerticalBox;
import com.intellij.util.ui.UIUtil;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author pmaruszak
 */
public class CrucibleCustomFilterDialog extends DialogWrapper {
	private JPanel rootPanel;
	private JComboBox projectComboBox;
	private JComboBox authorComboBox;
	private JComboBox moderatorComboBox;
	private JComboBox creatorComboBox;
	private JComboBox reviewerComboBox;
	private JComboBox reviewerStatusComboBox;
	private JComboBox matchRoleComboBox;
	private JCheckBox draftCheckBox;
	private JCheckBox pendingApprovalCheckBox;
	private JCheckBox underReviewCheckBox;
	private JCheckBox summarizeCheckBox;
	private JCheckBox closedCheckBox;
	private JCheckBox abandonedCheckBox;
	private JCheckBox rejectedCheckBox;
	private JCheckBox reviewNeedsFixingCheckBox;
	private JComboBox serverComboBox;

	private final UserComboBoxItem CRUC_USER_ANY = new UserComboBoxItem(null) {
		@Override
		public String toString() {
			return "Any";
		}
	};

	private final ProjectCfgManagerImpl projectCfgManager;
	private final CustomFilterBean filter;
	private final UiTaskExecutor uiTaskExecutor;
	private final CrucibleServerFacade crucibleServerFacade;
	private static final CrucibleProjectWrapper CRUC_PROJECT_NONE = new CrucibleProjectWrapper(null);
	private static final UserComboBoxItem CRUC_USER_NONE = new UserComboBoxItem(null);
	private static final CrucibleProjectWrapper CRUC_PROJECT_ANY = new CrucibleProjectWrapper(null) {
		@Override
		public String toString() {
			return "Any";
		}
	};
	private JComponent statesPanel;
	private JPanel comboPanel;
	private static final String MATCH_ROLE_ANY = "Any";
	private static final String MATCH_ROLE_ALL = "All";
	private static final String REVIEWER_STATUS_ANY = "Any";
	private static final String REVIEWER_STATUS_INCOMPLETE = "Incomplete";
	private static final String REVIEWER_STATUS_COMPLETE = "Complete";
    private final FilterActionClear clearFilterAction = new FilterActionClear();

    public CrucibleCustomFilterDialog(@NotNull final Project project, @NotNull final ProjectCfgManagerImpl cfgManager,
			@NotNull CustomFilterBean filter, @NotNull final UiTaskExecutor uiTaskExecutor) {
		super(project, false);
		this.projectCfgManager = cfgManager;
		this.filter = filter;
		this.uiTaskExecutor = uiTaskExecutor;
		setupUi();
		setModal(true);

		final ServerData serverCfg = projectCfgManager.getCrucibleServerr(filter.getServerId());

		reviewerStatusComboBox.addItem(REVIEWER_STATUS_ANY);
		reviewerStatusComboBox.addItem(REVIEWER_STATUS_INCOMPLETE);
		reviewerStatusComboBox.addItem(REVIEWER_STATUS_COMPLETE);

		Boolean isComplete = (filter.getReviewer() != null && filter.getReviewer().length() > 0) ? filter.isComplete() : filter
				.isAllReviewersComplete();
		if (isComplete == null) {
			reviewerStatusComboBox.setSelectedIndex(0);
		} else if (!isComplete) {
			reviewerStatusComboBox.setSelectedIndex(1);
		} else {
			reviewerStatusComboBox.setSelectedIndex(2);
		}

		matchRoleComboBox.addItem(MATCH_ROLE_ANY);
		matchRoleComboBox.addItem(MATCH_ROLE_ALL);
		final Boolean orRoles = filter.isOrRoles();
		matchRoleComboBox.setSelectedIndex((orRoles == null || orRoles) ? 0 : 1);

		crucibleServerFacade = IntelliJCrucibleServerFacade.getInstance();
		fillInCrucibleServers();

        if (filter.isEmpty()) {
            unSelectAllItems();            
        } else {
            ServerData selectedServer = setSelectedServer(serverCfg);
            fillServerRelatedCombos(selectedServer);
        }

		setTitle("Configure Custom Filter");
		getOKAction().putValue(Action.NAME, "Apply");

		serverComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fillServerRelatedCombos(getSelectedServer());
			}
		});


		init();
		pack();
	}

    @Override
    protected Action[] createActions() {
return new Action[] {
                getOKAction(),
                getClearFilterAction(),
                getCancelAction()
        };


    }

    public CustomFilterBean getFilter() {

		CrucibleServerCfgWrapper s = ((CrucibleServerCfgWrapper) this.serverComboBox.getSelectedItem());
		filter.setServerId(s != null ? (ServerIdImpl)(s.getWrapped()).getServerId() : null);
        if (s != null) {
            filter.setEmpty(false);
        }

		filter.setTitle("Custom Filter");
		final CrucibleProjectWrapper o = (CrucibleProjectWrapper) projectComboBox.getSelectedItem();
		if (o != null && o != CRUC_PROJECT_ANY && o.getWrapped() != null) {
			filter.setProjectKey(o.getWrapped().getKey());
            filter.setEmpty(false);
		} else {
			filter.setProjectKey("");
		}

        User author = getUser(authorComboBox);
		if (author != null) {
			filter.setAuthor(author.getUserName());
            filter.setEmpty(false);
		} else {
			filter.setAuthor("");
		}

        User creator = getUser(creatorComboBox);
		if (creator !=null) {
			filter.setCreator(creator.getUserName());
            filter.setEmpty(false);
		} else {
			filter.setCreator("");
		}
        User moderator = getUser(moderatorComboBox);
		if (moderator != null) {
			filter.setModerator(moderator.getUserName());
            filter.setEmpty(false);
		} else {
			filter.setModerator("");
		}

        User reviewer = getUser(reviewerComboBox);
		if ( reviewer != null) {
			filter.setReviewer(reviewer.getUserName());
            filter.setEmpty(false);
		} else {
			filter.setReviewer("");
		}

		List<State> states = new ArrayList<State>();
		if (draftCheckBox.isSelected()) {
            filter.setEmpty(false);
			states.add(State.DRAFT);
		}
		if (pendingApprovalCheckBox.isSelected()) {
            filter.setEmpty(false);
			states.add(State.APPROVAL);
		}
		if (summarizeCheckBox.isSelected()) {
            filter.setEmpty(false);
			states.add(State.SUMMARIZE);
		}
		if (closedCheckBox.isSelected()) {
            filter.setEmpty(false);
			states.add(State.CLOSED);
		}
		if (rejectedCheckBox.isSelected()) {
            filter.setEmpty(false);
			states.add(State.REJECTED);
		}
		if (underReviewCheckBox.isSelected()) {
            filter.setEmpty(false);
			states.add(State.REVIEW);
		}
		if (abandonedCheckBox.isSelected()) {
            filter.setEmpty(false);
			states.add(State.ABANDONED);
		}

		if (reviewNeedsFixingCheckBox.isSelected()) {
            filter.setEmpty(false);
			states.add(State.UNKNOWN);
		}


		filter.setState(states.toArray(new State[states.size()]));
		// depending on whether reviewer is selected or not two flags below have will be set in a different manner
		String completeSel = (String) reviewerStatusComboBox.getSelectedItem();
                
		final Boolean complete = REVIEWER_STATUS_ANY.equals(completeSel)  ? null : REVIEWER_STATUS_COMPLETE.equals(completeSel);
		if (reviewer != null) {
			filter.setComplete(complete);
			filter.setAllReviewersComplete(null);
             filter.setEmpty(false);
		} else {
			filter.setComplete(null);
			filter.setAllReviewersComplete(complete);
		}

		String role = (String) matchRoleComboBox.getSelectedItem();
		filter.setOrRoles(MATCH_ROLE_ANY.equals(role));
        if (filter.isOrRoles()) {
             filter.setEmpty(false);
        }


		return filter;
	}

    private User getUser(JComboBox userCbx) {
        if (userCbx.getSelectedItem() != null && ((UserComboBoxItem)userCbx.getSelectedItem()).getWrapped() != null) {
            return ((UserComboBoxItem) userCbx.getSelectedItem()).getWrapped();
        }

        return null;
    }
	private ServerData setSelectedServer(ServerData serverCfg) {

		for (int i = 0; i < serverComboBox.getItemCount(); i++) {
            final Object wrapper = serverComboBox.getItemAt(i);
            if (wrapper instanceof CrucibleServerCfgWrapper &&
					((CrucibleServerCfgWrapper) wrapper).getWrapped().equals(serverCfg)) {
				serverComboBox.setSelectedItem(wrapper);
				return serverCfg;
			}
		}

		if (serverComboBox.getItemCount() > 0) {
            serverComboBox.setSelectedIndex(-1);
//			if (serverComboBox.getItemAt(0) instanceof CrucibleServerCfgWrapper) {
//				serverComboBox.setSelectedItem(serverComboBox.getItemAt(0));
//				return ((CrucibleServerCfgWrapper) serverComboBox.getItemAt(0)).getWrapped();
//			}
		}

		return null;
	}

	private void fillInCrucibleServers() {
		final Collection<ServerData> enabledServers = projectCfgManager.getAllEnabledCrucibleServerss();

		serverComboBox.removeAllItems();
		if (enabledServers.isEmpty()) {
			serverComboBox.setEnabled(false);
			serverComboBox.addItem(new CrucibleServerCfgWrapper(null));
			//@todo disable apply filter button in toolbar
		} else {
			for (ServerData server : enabledServers) {
				serverComboBox.addItem(new CrucibleServerCfgWrapper(server));
			}
		}


	}

	private void fillServerRelatedCombos(final ServerData server) {
		final ServerData serverData = (server != null) ? server : getSelectedServer();

         if (serverData != null) {
			projectComboBox.setEnabled(false);
			setStateForAllControls(false);

			uiTaskExecutor.execute(new UiTask() {
				private List<CrucibleProject> projects = Collections.emptyList();
				private List<User> users = Collections.emptyList();
				private String currentAction;

				public void run() throws Exception {
					currentAction = "fetching crucible projects";
					projects = crucibleServerFacade.getProjects(serverData);
					currentAction = "fetching crucible users";
					users = crucibleServerFacade.getUsers(serverData);
				}

				public void onSuccess() {
					updateServerRelatedCombos(projects, users);
					setStateForAllControls(true);
				}

				public void onError() {
					updateServerRelatedCombos(projects, users);
					setStateForAllControls(true);
				}

				public String getLastAction() {
					return currentAction;
				}

				public Component getComponent() {
					return CrucibleCustomFilterDialog.this.getRootPane();
				}
			});
		}
	}

	@Nullable
	private ServerData getSelectedServer() {
		final Object selectedItem = serverComboBox.getSelectedItem();
		if (serverComboBox.getItemCount() > 0 && selectedItem != null
				&& selectedItem instanceof CrucibleServerCfgWrapper) {
			return ((CrucibleServerCfgWrapper) selectedItem).getWrapped();
		}
		return null;
	}

    private void unSelectAllItems() {
        draftCheckBox.setSelected(false);
        pendingApprovalCheckBox.setSelected(false);
        underReviewCheckBox.setSelected(false);
        summarizeCheckBox.setSelected(false);
        closedCheckBox.setSelected(false);
        abandonedCheckBox.setSelected(false);
        rejectedCheckBox.setSelected(false);
        reviewNeedsFixingCheckBox.setSelected(false);

        setProject(null, projectComboBox);
        setActiveUser(null, authorComboBox);
        setActiveUser(null, moderatorComboBox);
        setActiveUser(null, creatorComboBox);
        setActiveUser(null, reviewerComboBox);
        setSelectedServer(null);
        reviewerStatusComboBox.setSelectedIndex(-1);
        matchRoleComboBox.setSelectedIndex(-1);
        filter.setEmpty(true);
    }
	private void updateServerRelatedCombos(List<CrucibleProject> projects, List<User> users) {

		draftCheckBox.setSelected(false);
		pendingApprovalCheckBox.setSelected(false);
		underReviewCheckBox.setSelected(false);
		summarizeCheckBox.setSelected(false);
		closedCheckBox.setSelected(false);
		abandonedCheckBox.setSelected(false);
		rejectedCheckBox.setSelected(false);
		reviewNeedsFixingCheckBox.setSelected(false);

		if (filter.getState() != null) {
			for (State state : filter.getState()) {
				switch (state) {
					case APPROVAL:
						pendingApprovalCheckBox.setSelected(true);
						break;
					case DRAFT:
						draftCheckBox.setSelected(true);
						break;
					case REVIEW:
						underReviewCheckBox.setSelected(true);
						break;
					case SUMMARIZE:
						summarizeCheckBox.setSelected(true);
						break;
					case CLOSED:
						closedCheckBox.setSelected(true);
						break;
					case REJECTED:
						rejectedCheckBox.setSelected(true);
						break;
					case ABANDONED:
						abandonedCheckBox.setSelected(true);
						break;
					case UNKNOWN:
						reviewNeedsFixingCheckBox.setSelected(true);
						break;
					default:
						break;
				}
			}
		}

		projectComboBox.removeAllItems();
		authorComboBox.removeAllItems();
		moderatorComboBox.removeAllItems();
		creatorComboBox.removeAllItems();
		reviewerComboBox.removeAllItems();

		if (projects.isEmpty()) {
			projectComboBox.setEnabled(false);
			projectComboBox.addItem(CRUC_PROJECT_NONE);
			setOKActionEnabled(false);
		} else {
			projectComboBox.addItem(CRUC_PROJECT_ANY);
			for (CrucibleProject crucibleProject : projects) {
				projectComboBox.addItem(new CrucibleProjectWrapper(crucibleProject));
			}
			projectComboBox.setEnabled(true);
			setOKActionEnabled(true);
		}

		if (!users.isEmpty()) {
			authorComboBox.addItem(CRUC_USER_ANY);
			moderatorComboBox.addItem(CRUC_USER_ANY);
			creatorComboBox.addItem(CRUC_USER_ANY);
			reviewerComboBox.addItem(CRUC_USER_ANY);
			for (User user : users) {
				authorComboBox.addItem(new UserComboBoxItem(user));
				moderatorComboBox.addItem(new UserComboBoxItem(user));
				creatorComboBox.addItem(new UserComboBoxItem(user));
				reviewerComboBox.addItem(new UserComboBoxItem(user));
			}
		} else {
			authorComboBox.addItem(CRUC_USER_NONE);
			moderatorComboBox.addItem(CRUC_USER_NONE);
			creatorComboBox.addItem(CRUC_USER_NONE);
			reviewerComboBox.addItem(CRUC_USER_NONE);
		}

		setProject(filter.getProjectKey(), projectComboBox);
		setActiveUser(filter.getAuthor(), authorComboBox);
		setActiveUser(filter.getCreator(), creatorComboBox);
		setActiveUser(filter.getModerator(), moderatorComboBox);
		setActiveUser(filter.getReviewer(), reviewerComboBox);
		if (isShowing() && rootPanel.getPreferredSize().getWidth() > rootPanel.getWidth()) {
			pack();
		}
	}

	private void setStateForAllControls(boolean isEnabled) {
		ApplicationManager.getApplication().assertIsDispatchThread();

		setOKActionEnabled(isEnabled);

		for (Component component : statesPanel.getComponents()) {
			component.setEnabled(isEnabled);
		}
		for (Component component : comboPanel.getComponents()) {
			component.setEnabled(isEnabled);
		}
		final Border border = statesPanel.getBorder();
		if (border instanceof TitledBorder) {
			TitledBorder titledBorder = (TitledBorder) border;
			titledBorder.setTitleColor(isEnabled ? UIUtil.getActiveTextColor() : UIUtil.getTextInactiveTextColor());
			// above call sucks!!! does not cause panel to be repainted, so do it manually
			statesPanel.repaint();
		}

	}

	private void setProject(String projectName, JComboBox combo) {
		if (projectName == null) {
			combo.setSelectedIndex(-1);
            return;
		}
		for (int i = 0; i < combo.getModel().getSize(); ++i) {
			CrucibleProjectWrapper item = (CrucibleProjectWrapper) combo.getModel().getElementAt(i);
			final CrucibleProject crucibleProject = item.getWrapped();
			if ((crucibleProject != null && projectName.equals(crucibleProject.getKey())) || (projectName.length() == 0
					&& crucibleProject == null)) {
				combo.setSelectedItem(item);
				break;
			}
		}
	}

	private void setActiveUser(String userName, JComboBox combo) {
		if (userName == null) {
            combo.setSelectedIndex(-1);
			return;
		}
		for (int i = 0; i < combo.getModel().getSize(); ++i) {
			final UserComboBoxItem item = (UserComboBoxItem) combo.getModel().getElementAt(i);
			final User user = item.getWrapped();
			if (user != null && userName.equals(user.getUserName()) || userName.length() == 0 && user == null) {
				combo.setSelectedItem(item);
				break;
			}
		}
	}

	@Override
	@Nullable
	protected JComponent createCenterPanel() {
		return rootPanel;
	}

	private void setupUi() {
		rootPanel = new JPanel();
		rootPanel.setLayout(new FormLayout(
				"fill:10dlu:noGrow,fill:pref:grow,left:14dlu:noGrow,fill:pref:nogrow,left:10dlu:noGrow",
				"10dlu:noGrow,pref:noGrow,3dlu:noGrow,fill:pref,10dlu:grow"));
		comboPanel = new JPanel();
		comboPanel.setLayout(new FormLayout("fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:min(d;200dlu):grow",
				"center:max(d;4px):noGrow,top:3dlu:nogrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,"
						+ "top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,"
						+ "center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):"
						+ "noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow"));
		comboPanel.setEnabled(true);
		CellConstraints cc = new CellConstraints();
		rootPanel.add(comboPanel, cc.xy(2, 4));
		projectComboBox = new JComboBox();
		comboPanel.add(projectComboBox, cc.xy(3, 3));
		authorComboBox = new JComboBox();
		comboPanel.add(authorComboBox, cc.xy(3, 5));
		moderatorComboBox = new JComboBox();
		comboPanel.add(moderatorComboBox, cc.xy(3, 7));
		creatorComboBox = new JComboBox();
		comboPanel.add(creatorComboBox, cc.xy(3, 9));
		reviewerComboBox = new JComboBox();
		comboPanel.add(reviewerComboBox, cc.xy(3, 11));
		reviewerStatusComboBox = new JComboBox();
		comboPanel.add(reviewerStatusComboBox, cc.xy(3, 13));
		matchRoleComboBox = new JComboBox();
		comboPanel.add(matchRoleComboBox, cc.xy(3, 15));
		final JLabel label1 = new JLabel();
		label1.setText("Project:");
		comboPanel.add(label1, cc.xy(1, 3));
		final JLabel label2 = new JLabel();
		label2.setInheritsPopupMenu(true);
		label2.setText("Author:");
		comboPanel.add(label2, cc.xy(1, 5));
		final JLabel label3 = new JLabel();
		label3.setText("Moderator:");
		comboPanel.add(label3, cc.xy(1, 7));
		final JLabel label4 = new JLabel();
		label4.setText("Creator:");
		comboPanel.add(label4, cc.xy(1, 9));
		final JLabel label5 = new JLabel();
		label5.setText("Reviewer:");
		comboPanel.add(label5, cc.xy(1, 11));
		final JLabel label6 = new JLabel();
		label6.setMinimumSize(new Dimension(10, 16));
		label6.setText("Reviewer Status:");
		comboPanel.add(label6, cc.xy(1, 13));
		final JLabel label7 = new JLabel();
		label7.setText("Match role");
		comboPanel.add(label7, cc.xy(1, 15));
		final JLabel label8 = new JLabel();
		label8.setText("Crucible server:");
		comboPanel.add(label8, cc.xy(1, 1));
		serverComboBox = new JComboBox();
		comboPanel.add(serverComboBox, cc.xy(3, 1));

		statesPanel = new VerticalBox();
		rootPanel.add(statesPanel, cc.xy(4, 4));
		statesPanel.setBorder(BorderFactory.createTitledBorder("Review State"));
		draftCheckBox = new JCheckBox();
		draftCheckBox.setText("Draft");
		statesPanel.add(draftCheckBox);
		pendingApprovalCheckBox = new JCheckBox();
		pendingApprovalCheckBox.setText("Pending Approval");
		statesPanel.add(pendingApprovalCheckBox);
		underReviewCheckBox = new JCheckBox();
		underReviewCheckBox.setText("Under Review");
		statesPanel.add(underReviewCheckBox);
		summarizeCheckBox = new JCheckBox();
		summarizeCheckBox.setText("Summarize");
		statesPanel.add(summarizeCheckBox);
		closedCheckBox = new JCheckBox();
		closedCheckBox.setText("Closed");
		statesPanel.add(closedCheckBox);
		abandonedCheckBox = new JCheckBox();
		abandonedCheckBox.setText("Abandoned");
		statesPanel.add(abandonedCheckBox);
		rejectedCheckBox = new JCheckBox();
		rejectedCheckBox.setText("Rejected");
		statesPanel.add(rejectedCheckBox);
		reviewNeedsFixingCheckBox = new JCheckBox();
		reviewNeedsFixingCheckBox.setText("Review needs fixing");
		statesPanel.add(reviewNeedsFixingCheckBox);
	}

    public Action getClearFilterAction() {
        return this.clearFilterAction;
    }


    private static class UserComboBoxItem extends GenericComboBoxItemWrapper<User> {

		public UserComboBoxItem(User user) {
			super(user);
		}

		@Override
		public String toString() {
			if (wrapped != null) {
				return wrapped.getDisplayName();
			}
			return "None";
		}
	}

       private final class FilterActionClear extends AbstractAction {
        private static final String CLEAR_FILTER = "Clear filter";

        private FilterActionClear() {
            putValue(Action.NAME, CLEAR_FILTER);
        }

        public void actionPerformed(ActionEvent event) {
            unSelectAllItems();
//            if (filterListModel != null) {
//                initialFilter.clear();
//                ApplicationManager.getApplication().executeOnPooledThread(new SyncViewWithModelRunnable());
//            }
        }
    }

}
