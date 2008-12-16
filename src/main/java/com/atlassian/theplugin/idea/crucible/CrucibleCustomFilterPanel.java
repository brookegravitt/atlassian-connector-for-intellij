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
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.project.Project;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//todo PL-947
public class CrucibleCustomFilterPanel extends JPanel {
	private JPanel rootPanel;
	private JTextField filterTitle;
	private JCheckBox draftCheckBox;
	private JCheckBox pendingApprovalCheckBox;
	private JCheckBox underReviewCheckBox;
	private JCheckBox summarizeCheckBox;
	private JCheckBox closedCheckBox;
	private JCheckBox abandonedCheckBox;
	private JCheckBox rejectedCheckBox;
	private JCheckBox reviewNeedsFixingCheckBox;
	private JComboBox projectComboBox;
	private JComboBox authorComboBox;
	private JComboBox moderatorComboBox;
	private JComboBox creatorComboBox;
	private JComboBox reviewerStatusComboBox;
	private JComboBox reviewerComboBox;
	private JComboBox serverComboBox;
	private JComboBox matchRoleComboBox;
	private CrucibleServerFacade crucibleServerFacade;
	private transient CustomFilterBean filter;

	private ProjectBean anyProject;

	private UserBean anyUser;
	private final Project project;
	private final CfgManager cfgManager;

	CrucibleCustomFilterPanel(final Project project, final CfgManager cfgManager) {
		this.project = project;
		this.cfgManager = cfgManager;
		$$$setupUI$$$();

		anyProject = new ProjectBean();
		anyProject.setName("Any");
		anyUser = new UserBean();
		anyUser.setDisplayName("Any");
		anyUser.setUserName("_any_");

		reviewerStatusComboBox.addItem("Any");
		reviewerStatusComboBox.addItem("Incomplete");
		reviewerStatusComboBox.addItem("Complete");

		matchRoleComboBox.addItem("Any");
		matchRoleComboBox.addItem("All");

		crucibleServerFacade = CrucibleServerFacadeImpl.getInstance();
		serverComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fillServerRelatedCombos(getSelectedServer());

			}
		});
	}

	public void setFilter(CustomFilterBean filter) {
		ServerCfg server = null;

		if (filter == null) {
			this.filter = new CustomFilterBean();
		} else {
			this.filter = filter;
			final ServerId serverId = new ServerId(filter.getServerUid());
			server = cfgManager.getServer(CfgUtil.getProjectId(project), serverId);
			filterTitle.setText((filter.getTitle()));
		}

		fillInCrucibleServers();
		if (server == null && serverComboBox.getItemCount() > 0) {
			serverComboBox.setSelectedIndex(0);
		}

		fillServerRelatedCombos(getSelectedServer());
	}

	public void setSelectedServer(CrucibleServerCfg serverCfg) {
		ServerComboBoxItem item = new ServerComboBoxItem(serverCfg);
		for (int i = 0; i < serverComboBox.getItemCount(); i++) {
			if (serverComboBox.getItemAt(i) instanceof ServerComboBoxItem
					&& ((ServerComboBoxItem) serverComboBox.getItemAt(i)).getServer().equals(serverCfg)) {
				serverComboBox.setSelectedItem(serverComboBox.getItemAt(i));
			}

		}
	}

	private CrucibleServerCfg getSelectedServer() {
		CrucibleServerCfg server = null;

		if (serverComboBox.getItemCount() > 0 &&
				serverComboBox.getSelectedItem() != null &&
				serverComboBox.getSelectedItem() instanceof ServerComboBoxItem) {
			server = ((ServerComboBoxItem) serverComboBox.getSelectedItem()).getServer();
		}

		return server;
	}

	public CustomFilterBean getFilter() {
		CrucibleServerCfg s = ((ServerComboBoxItem) this.serverComboBox.getSelectedItem()).getServer();
		filter.setServerUid(s.getServerId().getUuid().toString());

		filter.setTitle(filterTitle.getText());
		if (!((ProjectComboBoxItem) projectComboBox.getSelectedItem()).getProject().getName().equals(anyProject.getName())) {
			filter.setProjectKey(((ProjectComboBoxItem) projectComboBox.getSelectedItem()).getProject().getKey());
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
		filter.setState(states.toArray(new String[states.size()]));

		String role = (String) matchRoleComboBox.getSelectedItem();
		filter.setOrRoles("Any".equals(role));

		String complete = (String) reviewerStatusComboBox.getSelectedItem();
		filter.setComplete("Complete".equals(complete));

		return filter;
	}

	private void fillServerRelatedCombos(final CrucibleServerCfg server) {
		final CrucibleServerCfg crucibleServerCfg = (server != null) ? server : getSelectedServer();

		if (crucibleServerCfg != null) {
			projectComboBox.setEnabled(false);

			new Thread(new Runnable() {
				public void run() {
					List<CrucibleProject> projects = new ArrayList<CrucibleProject>();
					List<User> users = new ArrayList<User>();
					try {
						projects = crucibleServerFacade.getProjects(crucibleServerCfg);
						users = crucibleServerFacade.getUsers(crucibleServerCfg);
					} catch (RemoteApiException e) {
						// nothing can be done here
					} catch (ServerPasswordNotProvidedException e) {
						// nothing can be done here
					}
					final List<CrucibleProject> finalProjects = projects;
					final List<User> finalUsers = users;

					EventQueue.invokeLater(new Runnable() {
						public void run() {
							updateServerRelatedCombos(finalProjects, finalUsers);
						}
					});
				}
			}, "atlassian-idea-plugin crucible custom filter upload combos refresh").start();
		}
	}

	private void updateServerRelatedCombos(List<CrucibleProject> projects, List<User> users) {
		projectComboBox.removeAllItems();
		projectComboBox.addItem(new ProjectComboBoxItem(anyProject));
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
			projectComboBox.addItem("No projects");
			setEnabledApplyButton(false);
		} else {
			for (CrucibleProject project : projects) {
				projectComboBox.addItem(new ProjectComboBoxItem(project));
			}
			projectComboBox.setEnabled(true);
			setEnabledApplyButton(true);
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
				}
			}
		}
	}

	private void setProject(String projectName, JComboBox combo) {
		if (projectName != null) {
			for (int i = 0; i < combo.getModel().getSize(); ++i) {
				ProjectComboBoxItem item = (ProjectComboBoxItem) combo.getModel().getElementAt(i);
				if (projectName.equals(item.getProject().getKey())) {
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

	private void setEnabledApplyButton(Boolean enabled) {
		ActionManager.getInstance().getAction("ThePlugin.Crucible.ShowFilter").getTemplatePresentation().setEnabled(enabled);
	}

	private void fillInCrucibleServers() {
		final Collection<CrucibleServerCfg> enabledServers = cfgManager.getAllEnabledCrucibleServers(
				CfgUtil.getProjectId(project));

		serverComboBox.removeAllItems();
		if (enabledServers.isEmpty()) {
			serverComboBox.setEnabled(false);
			serverComboBox.addItem("Enable a Crucible server first!");
			//@todo disable apply filter button in toolbar
		} else {
			for (CrucibleServerCfg server : enabledServers) {
				serverComboBox.addItem(new ServerComboBoxItem(server));
			}
		}


	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		rootPanel = new JPanel();
		rootPanel.setLayout(new FormLayout("fill:p:grow,fill:max(d;4px):noGrow", "top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:19px:noGrow,center:max(d;4px):noGrow,center:max(d;4px):noGrow,center:33px:noGrow,center:18px:noGrow,center:31px:noGrow,center:19px:noGrow,center:30px:noGrow,center:19px:noGrow,center:30px:noGrow,center:17px:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,top:4dlu:noGrow,center:24px:noGrow,center:23px:noGrow,center:25px:noGrow,center:max(d;4px):noGrow,center:25px:noGrow,center:24px:noGrow,center:24px:noGrow,center:max(d;4px):noGrow"));
		filterTitle = new JTextField();
		CellConstraints cc = new CellConstraints();
		rootPanel.add(filterTitle, cc.xyw(1, 7, 2, CellConstraints.FILL, CellConstraints.DEFAULT));
		final JLabel label1 = new JLabel();
		label1.setText("Title:");
		rootPanel.add(label1, cc.xy(1, 5));
		final JLabel label2 = new JLabel();
		label2.setText("Project:");
		rootPanel.add(label2, cc.xy(1, 9));
		projectComboBox = new JComboBox();
		projectComboBox.setMinimumSize(new Dimension(120, 27));
		rootPanel.add(projectComboBox, cc.xy(1, 10));
		authorComboBox = new JComboBox();
		rootPanel.add(authorComboBox, cc.xy(1, 12));
		final JLabel label3 = new JLabel();
		label3.setInheritsPopupMenu(true);
		label3.setText("Author:");
		rootPanel.add(label3, cc.xy(1, 11));
		moderatorComboBox = new JComboBox();
		rootPanel.add(moderatorComboBox, cc.xy(1, 14));
		final JLabel label4 = new JLabel();
		label4.setText("Moderator:");
		rootPanel.add(label4, cc.xy(1, 13));
		creatorComboBox = new JComboBox();
		rootPanel.add(creatorComboBox, cc.xy(1, 16));
		final JLabel label5 = new JLabel();
		label5.setText("Creator:");
		rootPanel.add(label5, cc.xy(1, 15));
		reviewerComboBox = new JComboBox();
		rootPanel.add(reviewerComboBox, cc.xy(1, 18));
		final JLabel label6 = new JLabel();
		label6.setText("Reviewer:");
		rootPanel.add(label6, cc.xy(1, 17));
		reviewerStatusComboBox = new JComboBox();
		rootPanel.add(reviewerStatusComboBox, cc.xy(1, 20));
		final JLabel label7 = new JLabel();
		label7.setMinimumSize(new Dimension(10, 16));
		label7.setText("Reviewer Status:");
		rootPanel.add(label7, cc.xy(1, 19));
		draftCheckBox = new JCheckBox();
		draftCheckBox.setText("Draft");
		rootPanel.add(draftCheckBox, cc.xy(1, 27));
		pendingApprovalCheckBox = new JCheckBox();
		pendingApprovalCheckBox.setEnabled(false);
		pendingApprovalCheckBox.setText("Pending Approval");
		rootPanel.add(pendingApprovalCheckBox, cc.xy(1, 28));
		underReviewCheckBox = new JCheckBox();
		underReviewCheckBox.setText("Under Review");
		rootPanel.add(underReviewCheckBox, cc.xy(1, 29));
		summarizeCheckBox = new JCheckBox();
		summarizeCheckBox.setText("Summarize");
		rootPanel.add(summarizeCheckBox, cc.xy(1, 30));
		closedCheckBox = new JCheckBox();
		closedCheckBox.setText("Closed");
		rootPanel.add(closedCheckBox, cc.xy(1, 31));
		abandonedCheckBox = new JCheckBox();
		abandonedCheckBox.setText("Abandoned");
		rootPanel.add(abandonedCheckBox, cc.xy(1, 32));
		rejectedCheckBox = new JCheckBox();
		rejectedCheckBox.setText("Rejected");
		rootPanel.add(rejectedCheckBox, cc.xy(1, 33));
		reviewNeedsFixingCheckBox = new JCheckBox();
		reviewNeedsFixingCheckBox.setEnabled(false);
		reviewNeedsFixingCheckBox.setText("Review needs fixing");
		rootPanel.add(reviewNeedsFixingCheckBox, cc.xy(1, 34));
		serverComboBox = new JComboBox();
		rootPanel.add(serverComboBox, cc.xy(1, 4));
		final JLabel label8 = new JLabel();
		label8.setText("Crucible server:");
		rootPanel.add(label8, cc.xy(1, 2));
		final JLabel label9 = new JLabel();
		label9.setText("Match role");
		rootPanel.add(label9, cc.xy(1, 22));
		matchRoleComboBox = new JComboBox();
		rootPanel.add(matchRoleComboBox, cc.xy(1, 24));
		label1.setLabelFor(filterTitle);
		label2.setLabelFor(projectComboBox);
		label3.setLabelFor(authorComboBox);
		label4.setLabelFor(moderatorComboBox);
		label5.setLabelFor(creatorComboBox);
		label6.setLabelFor(reviewerComboBox);
		label7.setLabelFor(reviewerStatusComboBox);
		label8.setLabelFor(serverComboBox);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootPanel;
	}


	private static final class ServerComboBoxItem {
		private final CrucibleServerCfg server;

		private ServerComboBoxItem(CrucibleServerCfg server) {
			this.server = server;
		}

		public String toString() {
			return server.getName();
		}

		public CrucibleServerCfg getServer() {
			return server;
		}
	}

	private static final class ProjectComboBoxItem {
		private final CrucibleProject project;

		private ProjectComboBoxItem(CrucibleProject project) {
			this.project = project;
		}

		public String toString() {
			return project.getName();
		}

		public CrucibleProject getProject() {
			return project;
		}
	}


	private class UserComboBoxItem {
		private final User user;

		public UserComboBoxItem(User user) {
			this.user = user;
		}

		public String toString() {
			return user.getDisplayName();
		}

		public User getUserData() {
			return user;
		}
	}
}

