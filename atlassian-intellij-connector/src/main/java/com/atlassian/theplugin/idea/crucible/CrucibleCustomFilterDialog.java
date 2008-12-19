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

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.UiTask;
import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.idea.config.CrucibleProjectWrapper;
import com.atlassian.theplugin.idea.config.CrucibleServerCfgWrapper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.panels.VerticalBox;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;
import javax.swing.*;
import java.awt.*;
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

	private CrucibleProjectBean anyProject;

	private UserBean anyUser;

	private final CfgManager cfgManager;
	private final Project project;
	private final CustomFilterBean filter;
	private final UiTaskExecutor uiTaskExecutor;
	private final CrucibleServerFacade crucibleServerFacade;
	private CrucibleServerCfg serverCfg;

	CrucibleCustomFilterDialog(@NotNull final Project project, @NotNull final CfgManager cfgManager,
							   @NotNull CustomFilterBean filter, @NotNull final UiTaskExecutor uiTaskExecutor) {
		super(project, false);
		this.project = project;
		this.cfgManager = cfgManager;
		this.filter = filter;
		this.uiTaskExecutor = uiTaskExecutor;
		setupUi();

		this.serverCfg = (CrucibleServerCfg) cfgManager
				.getServer(CfgUtil.getProjectId(project), new ServerId(filter.getServerUid()));

		anyProject = new CrucibleProjectBean();
		anyProject.setName("Any");
		anyUser = new UserBean();
		anyUser.setDisplayName("Any");
		anyUser.setUserName("_any_");

		projectComboBox.addItem(new CrucibleProjectWrapper(anyProject));
		projectComboBox.setSelectedIndex(0);
		authorComboBox.addItem(new UserComboBoxItem(anyUser));
		authorComboBox.setSelectedIndex(0);
		moderatorComboBox.addItem(new UserComboBoxItem(anyUser));
		moderatorComboBox.setSelectedIndex(0);
		creatorComboBox.addItem(new UserComboBoxItem(anyUser));
		creatorComboBox.setSelectedIndex(0);
		reviewerComboBox.addItem(new UserComboBoxItem(anyUser));
		reviewerComboBox.setSelectedIndex(0);

		reviewerStatusComboBox.addItem("Any");
		reviewerStatusComboBox.addItem("Incomplete");
		reviewerStatusComboBox.addItem("Complete");
		reviewerStatusComboBox.setSelectedIndex(0);

		matchRoleComboBox.addItem("Any");
		matchRoleComboBox.addItem("All");
		matchRoleComboBox.setSelectedIndex(0);

		crucibleServerFacade = CrucibleServerFacadeImpl.getInstance();
		fillInCrucibleServers();
		fillServerRelatedCombos(serverCfg);
		setModal(true);
		setTitle("Configure Custom Filter");
		getOKAction().putValue(Action.NAME, "Apply");

//		setFilter(filter);
		fillInCrucibleServers();
		if (serverComboBox.getItemCount() > 0) {
			serverComboBox.setSelectedIndex(0);
		}

		fillServerRelatedCombos(getSelectedServer());


		serverComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fillServerRelatedCombos(getSelectedServer());

			}
		});

		init();
		pack();
	}

	public CustomFilterBean getFilter() {
		CrucibleServerCfg s = ((CrucibleServerCfgWrapper) this.serverComboBox.getSelectedItem()).getWrapped();
		filter.setServerUid(s.getServerId().getUuid().toString());

		filter.setTitle("Custom Filter");
		final CrucibleProjectWrapper o = (CrucibleProjectWrapper) projectComboBox.getSelectedItem();
		if (!o.getWrapped().equals(anyProject)) {
			filter.setProjectKey(o.getWrapped().getKey());
		} else {
			filter.setProjectKey("");
		}
		if (!((UserComboBoxItem) authorComboBox.getSelectedItem()).getUserData().getUserName().equals(anyUser.getUserName())) {
			filter.setAuthor(((UserComboBoxItem) authorComboBox.getSelectedItem()).getUserData().getUserName());
		} else {
			filter.setAuthor("");
		}
		if (!((UserComboBoxItem) creatorComboBox.getSelectedItem()).getUserData().getUserName().equals(anyUser.getUserName())) {
			filter.setCreator(((UserComboBoxItem) creatorComboBox.getSelectedItem()).getUserData().getUserName());
		} else {
			filter.setCreator("");
		}
		if (!((UserComboBoxItem) moderatorComboBox.getSelectedItem()).getUserData().getUserName()
				.equals(anyUser.getUserName())) {
			filter.setModerator(((UserComboBoxItem) moderatorComboBox.getSelectedItem()).getUserData().getUserName());
		} else {
			filter.setModerator("");
		}
		if (!((UserComboBoxItem) reviewerComboBox.getSelectedItem()).getUserData().getUserName()
				.equals(anyUser.getUserName())) {
			filter.setReviewer(((UserComboBoxItem) reviewerComboBox.getSelectedItem()).getUserData().getUserName());
		} else {
			filter.setReviewer("");
		}

		List<String> states = new ArrayList<String>();
		if (draftCheckBox.isSelected()) {
			states.add(State.DRAFT.value());
		}
		if (pendingApprovalCheckBox.isSelected()) {
			states.add(State.APPROVAL.value());
		}
		if (summarizeCheckBox.isSelected()) {
			states.add(State.SUMMARIZE.value());
		}
		if (closedCheckBox.isSelected()) {
			states.add(State.CLOSED.value());
		}
		if (rejectedCheckBox.isSelected()) {
			states.add(State.REJECTED.value());
		}
		if (underReviewCheckBox.isSelected()) {
			states.add(State.REVIEW.value());
		}
		if (abandonedCheckBox.isSelected()) {
			states.add(State.ABANDONED.value());
		}


		filter.setState(states.toArray(new String[states.size()]));

		String role = (String) matchRoleComboBox.getSelectedItem();
		filter.setOrRoles("Any".equals(role));

		String complete = (String) reviewerStatusComboBox.getSelectedItem();
		filter.setComplete("Any".equals(complete) ? null : "Complete".equals(complete));


		return filter;
	}

	public void setSelectedServer(CrucibleServerCfg serverCfg) {
		for (int i = 0; i < serverComboBox.getItemCount(); i++) {
			if (serverComboBox.getItemAt(i) instanceof CrucibleServerCfgWrapper &&
					((CrucibleServerCfgWrapper) serverComboBox.getItemAt(i)).getWrapped().equals(serverCfg)) {
				serverComboBox.setSelectedItem(serverComboBox.getItemAt(i));
			}
		}
	}

	private void fillInCrucibleServers() {
		final Collection<CrucibleServerCfg> enabledServers = cfgManager
				.getAllEnabledCrucibleServers(CfgUtil.getProjectId(project));

		serverComboBox.removeAllItems();
		if (enabledServers.isEmpty()) {
			serverComboBox.setEnabled(false);
			serverComboBox.addItem(new CrucibleServerCfgWrapper(null));
			//@todo disable apply filter button in toolbar
		} else {
			for (CrucibleServerCfg server : enabledServers) {
				serverComboBox.addItem(new CrucibleServerCfgWrapper(server));
			}
		}


	}

	private void fillServerRelatedCombos(final CrucibleServerCfg server) {
		final CrucibleServerCfg crucibleServerCfg = (server != null) ? server : getSelectedServer();

		if (crucibleServerCfg != null) {
			projectComboBox.setEnabled(false);

			uiTaskExecutor.execute(new UiTask() {
				private List<CrucibleProject> projects = Collections.emptyList();
				private List<User> users = Collections.emptyList();
				private String currentAction;

				public void run() throws Exception {
					currentAction = "fetching crucible projects";
					projects = crucibleServerFacade.getProjects(crucibleServerCfg);
					currentAction = "fetching crucible users";
					users = crucibleServerFacade.getUsers(crucibleServerCfg);
				}

				public void onSuccess() {
					updateServerRelatedCombos(projects, users);
				}

				public void onError() {
					updateServerRelatedCombos(projects, users);
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
	private CrucibleServerCfg getSelectedServer() {
		final Object selectedItem = serverComboBox.getSelectedItem();
		if (serverComboBox.getItemCount() > 0 && selectedItem != null
				&& selectedItem instanceof CrucibleServerCfgWrapper) {
			return ((CrucibleServerCfgWrapper) selectedItem).getWrapped();
		}
		return null;
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
			for (String state : filter.getState()) {
				State value = State.fromValue(state);
				switch (value) {
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
					default:
						break;
				}
			}
		}

		projectComboBox.removeAllItems();
		projectComboBox.addItem(new CrucibleProjectWrapper(anyProject));
		authorComboBox.removeAllItems();
		authorComboBox.addItem(new UserComboBoxItem(anyUser));
		moderatorComboBox.removeAllItems();
		moderatorComboBox.addItem(new UserComboBoxItem(anyUser));
		creatorComboBox.removeAllItems();
		creatorComboBox.addItem(new UserComboBoxItem(anyUser));
		reviewerComboBox.removeAllItems();
		reviewerComboBox.addItem(new UserComboBoxItem(anyUser));

		if (projects.isEmpty()) {
			projectComboBox.setEnabled(false);
			projectComboBox.addItem(new CrucibleProjectWrapper(null));
			setOKActionEnabled(false);
		} else {
			for (CrucibleProject crucibleProject : projects) {
				projectComboBox.addItem(new CrucibleProjectWrapper(crucibleProject));
			}
			projectComboBox.setEnabled(true);
			setOKActionEnabled(true);
		}

		if (!users.isEmpty()) {
			for (User user : users) {
				authorComboBox.addItem(new UserComboBoxItem(user));
				moderatorComboBox.addItem(new UserComboBoxItem(user));
				creatorComboBox.addItem(new UserComboBoxItem(user));
				reviewerComboBox.addItem(new UserComboBoxItem(user));
			}
		}
		setProject(filter.getProjectKey(), projectComboBox);
		setActiveUser(filter.getAuthor(), authorComboBox);
		setActiveUser(filter.getCreator(), creatorComboBox);
		setActiveUser(filter.getModerator(), moderatorComboBox);
		setActiveUser(filter.getReviewer(), reviewerComboBox);
		pack();

	}

	private void setProject(String projectName, JComboBox combo) {
		if (projectName != null) {
			for (int i = 0; i < combo.getModel().getSize(); ++i) {
				CrucibleProjectWrapper item = (CrucibleProjectWrapper) combo.getModel().getElementAt(i);
				if (projectName.equals(item.getWrapped().getKey())) {
					combo.setSelectedItem(item);
					break;
				}
			}
		}
	}

	private void setActiveUser(String userName, JComboBox combo) {
		if (userName != null) {
			for (int i = 0; i < combo.getModel().getSize(); ++i) {
				UserComboBoxItem item = (UserComboBoxItem) combo.getModel().getElementAt(i);
				if (userName.equals(item.getUserData().getUserName())) {
					combo.setSelectedItem(item);
					break;
				}
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
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new FormLayout("fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:min(d;200dlu):grow",
				"center:max(d;4px):noGrow,top:3dlu:nogrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,"
						+ "top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,"
						+ "center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):"
						+ "noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow"));
		panel1.setEnabled(true);
		CellConstraints cc = new CellConstraints();
		rootPanel.add(panel1, cc.xy(2, 4));
		projectComboBox = new JComboBox();
		panel1.add(projectComboBox, cc.xy(3, 3));
		authorComboBox = new JComboBox();
		panel1.add(authorComboBox, cc.xy(3, 5));
		moderatorComboBox = new JComboBox();
		panel1.add(moderatorComboBox, cc.xy(3, 7));
		creatorComboBox = new JComboBox();
		panel1.add(creatorComboBox, cc.xy(3, 9));
		reviewerComboBox = new JComboBox();
		panel1.add(reviewerComboBox, cc.xy(3, 11));
		reviewerStatusComboBox = new JComboBox();
		panel1.add(reviewerStatusComboBox, cc.xy(3, 13));
		matchRoleComboBox = new JComboBox();
		panel1.add(matchRoleComboBox, cc.xy(3, 15));
		final JLabel label1 = new JLabel();
		label1.setText("Project:");
		panel1.add(label1, cc.xy(1, 3));
		final JLabel label2 = new JLabel();
		label2.setInheritsPopupMenu(true);
		label2.setText("Author:");
		panel1.add(label2, cc.xy(1, 5));
		final JLabel label3 = new JLabel();
		label3.setText("Moderator:");
		panel1.add(label3, cc.xy(1, 7));
		final JLabel label4 = new JLabel();
		label4.setText("Creator:");
		panel1.add(label4, cc.xy(1, 9));
		final JLabel label5 = new JLabel();
		label5.setText("Reviewer:");
		panel1.add(label5, cc.xy(1, 11));
		final JLabel label6 = new JLabel();
		label6.setMinimumSize(new Dimension(10, 16));
		label6.setText("Reviewer Status:");
		panel1.add(label6, cc.xy(1, 13));
		final JLabel label7 = new JLabel();
		label7.setText("Match role");
		panel1.add(label7, cc.xy(1, 15));
		final JLabel label8 = new JLabel();
		label8.setText("Crucible server:");
		panel1.add(label8, cc.xy(1, 1));
		serverComboBox = new JComboBox();
		panel1.add(serverComboBox, cc.xy(3, 1));

		final JComponent panel2 = new VerticalBox();
		rootPanel.add(panel2, cc.xy(4, 4));
		panel2.setBorder(BorderFactory.createTitledBorder("Review State"));
		draftCheckBox = new JCheckBox();
		draftCheckBox.setText("Draft");
		panel2.add(draftCheckBox);
		pendingApprovalCheckBox = new JCheckBox();
		pendingApprovalCheckBox.setEnabled(false);
		pendingApprovalCheckBox.setText("Pending Approval");
		panel2.add(pendingApprovalCheckBox);
		underReviewCheckBox = new JCheckBox();
		underReviewCheckBox.setText("Under Review");
		panel2.add(underReviewCheckBox);
		summarizeCheckBox = new JCheckBox();
		summarizeCheckBox.setText("Summarize");
		panel2.add(summarizeCheckBox);
		closedCheckBox = new JCheckBox();
		closedCheckBox.setText("Closed");
		panel2.add(closedCheckBox);
		abandonedCheckBox = new JCheckBox();
		abandonedCheckBox.setText("Abandoned");
		panel2.add(abandonedCheckBox);
		rejectedCheckBox = new JCheckBox();
		rejectedCheckBox.setText("Rejected");
		panel2.add(rejectedCheckBox);
		reviewNeedsFixingCheckBox = new JCheckBox();
		reviewNeedsFixingCheckBox.setEnabled(false);
		reviewNeedsFixingCheckBox.setText("Review needs fixing");
		panel2.add(reviewNeedsFixingCheckBox);
	}


	private class UserComboBoxItem {
		private final User user;

		public UserComboBoxItem(User user) {
			this.user = user;
		}

		@Override
		public String toString() {
			return user.getDisplayName();
		}

		public User getUserData() {
			return user;
		}
	}

}
