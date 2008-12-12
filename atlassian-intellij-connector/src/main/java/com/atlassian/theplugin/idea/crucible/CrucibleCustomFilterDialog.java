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
import com.intellij.openapi.ui.DialogWrapper;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: pmaruszak
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

	private ProjectBean anyProject;

	private UserBean anyUser;

	CrucibleCustomFilterPanel panel;
	private CfgManager cfgManager;
	private Project project;
	private CustomFilterBean filter;
	private CrucibleServerFacade crucibleServerFacade;
	private CrucibleServerCfg serverCfg;

	CrucibleCustomFilterDialog(final Project project, final CfgManager cfgManager, @NotNull CustomFilterBean filter) {
		super(project, false);
		this.project = project;
		this.cfgManager = cfgManager;
		this.filter = filter;
		$$$setupUI$$$();

		if (filter != null) {
			this.serverCfg = (CrucibleServerCfg) cfgManager
					.getServer(CfgUtil.getProjectId(project), new ServerId(filter.getServerUid()));
		}

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
		fillInCrucibleServers();
		fillServerRelatedCombos(serverCfg);
		setModal(true);
		setResizable(false);
		setTitle("Configure Custom Filter");
		getOKAction().putValue(Action.NAME, "Apply");

		setFilter(filter);




		serverComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fillServerRelatedCombos(getSelectedServer());

			}
		});

		init();
		pack();


	}

	public void setFilter(CustomFilterBean filter) {
		ServerCfg server = null;

		if (filter == null) {
			this.filter = new CustomFilterBean();
		} else {
			this.filter = filter;
			//final ServerId serverId = new ServerId(filter.getServerUid());
			//server = cfgManager.getServer(CfgUtil.getProjectId(project), serverId);
			//filterTitle.setText((filter.getTitle()));
		}

		fillInCrucibleServers();
		if (server == null && serverComboBox.getItemCount() > 0) {
			serverComboBox.setSelectedIndex(0);
		}

		fillServerRelatedCombos(getSelectedServer());
	}


	public CustomFilterBean getFilter() {
		CrucibleServerCfg s = ((ServerComboBoxItem) this.serverComboBox.getSelectedItem()).getServer();
		filter.setServerUid(s.getServerId().getUuid().toString());

		filter.setTitle("Custom Filter");
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
		if (abandonedCheckBox.isSelected()) {
			states.add(State.ABANDONED.value());
		}


		filter.setState(states.toArray(new String[states.size()]));

		String role = (String) matchRoleComboBox.getSelectedItem();
		filter.setOrRoles("Any".equals(role));

		String complete = (String) reviewerStatusComboBox.getSelectedItem();
		filter.setComplete("Complete".equals(complete));


		return filter;
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

	private void fillServerRelatedCombos(final CrucibleServerCfg server) {
		final CrucibleServerCfg crucibleServerCfg = (server != null) ? server : getSelectedServer();

		if (crucibleServerCfg != null) {
			projectComboBox.setEnabled(false);

			new Thread(new Runnable() {
				public void run() {
					List<com.atlassian.theplugin.commons.crucible.api.model.Project> projects = new ArrayList<com.atlassian.theplugin.commons.crucible.api.model.Project>();
					List<User> users = new ArrayList<User>();
					try {
						projects = crucibleServerFacade.getProjects(crucibleServerCfg);
						users = crucibleServerFacade.getUsers(crucibleServerCfg);
					} catch (RemoteApiException e) {
						// nothing can be done here
					} catch (ServerPasswordNotProvidedException e) {
						// nothing can be done here
					}
					final List<com.atlassian.theplugin.commons.crucible.api.model.Project> finalProjects = projects;
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

	private CrucibleServerCfg getSelectedServer() {
		CrucibleServerCfg server = null;

		if (serverComboBox.getItemCount() > 0 &&
				serverComboBox.getSelectedItem() != null &&
				serverComboBox.getSelectedItem() instanceof ServerComboBoxItem) {
			server = ((ServerComboBoxItem) serverComboBox.getSelectedItem()).getServer();
		}

		return server;
	}

	private void updateServerRelatedCombos(List<com.atlassian.theplugin.commons.crucible.api.model.Project> projects, List<User> users) {
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
			for (com.atlassian.theplugin.commons.crucible.api.model.Project project : projects) {
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
					case ABANDONED:
						abandonedCheckBox.setSelected(true);
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

	@Nullable
	protected JComponent createCenterPanel() {
		return $$$getRootComponent$$$();
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
		rootPanel.setLayout(new FormLayout("fill:16px:noGrow,fill:319px:noGrow,fill:max(d;4px):noGrow,left:10dlu:noGrow,fill:160px:noGrow,left:4dlu:noGrow,fill:max(d;16px):noGrow", "center:max(d;8px):noGrow,top:3dlu:noGrow,center:15px:noGrow,top:3dlu:noGrow,center:222px:noGrow,center:max(d;16px):noGrow"));
		rootPanel.setMaximumSize(new Dimension(543, 271));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new FormLayout("fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:d:grow", "center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow"));
		panel1.setEnabled(true);
		CellConstraints cc = new CellConstraints();
		rootPanel.add(panel1, cc.xy(2, 5, CellConstraints.DEFAULT, CellConstraints.TOP));
		projectComboBox = new JComboBox();
		projectComboBox.setMinimumSize(new Dimension(120, 27));
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
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new FormLayout("fill:p:noGrow", "center:max(p;4px):grow,top:p:grow,center:p:grow,top:p:grow,center:max(p;4px):grow,top:p:grow,center:p:grow,top:p:grow"));
		panel2.setMinimumSize(new Dimension(-1, -1));
		panel2.setPreferredSize(new Dimension(-1, -1));
		rootPanel.add(panel2, cc.xy(5, 5, CellConstraints.DEFAULT, CellConstraints.FILL));
		panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font(panel2.getFont().getName(), panel2.getFont().getStyle(), panel2.getFont().getSize())));
		draftCheckBox = new JCheckBox();
		draftCheckBox.setText("Draft");
		panel2.add(draftCheckBox, cc.xy(1, 1, CellConstraints.DEFAULT, CellConstraints.FILL));
		pendingApprovalCheckBox = new JCheckBox();
		pendingApprovalCheckBox.setEnabled(false);
		pendingApprovalCheckBox.setText("Pending Approval");
		panel2.add(pendingApprovalCheckBox, cc.xy(1, 2, CellConstraints.DEFAULT, CellConstraints.FILL));
		underReviewCheckBox = new JCheckBox();
		underReviewCheckBox.setText("Under Review");
		panel2.add(underReviewCheckBox, cc.xy(1, 3, CellConstraints.DEFAULT, CellConstraints.FILL));
		summarizeCheckBox = new JCheckBox();
		summarizeCheckBox.setText("Summarize");
		panel2.add(summarizeCheckBox, cc.xy(1, 4, CellConstraints.DEFAULT, CellConstraints.FILL));
		closedCheckBox = new JCheckBox();
		closedCheckBox.setText("Closed");
		panel2.add(closedCheckBox, cc.xy(1, 5, CellConstraints.DEFAULT, CellConstraints.FILL));
		abandonedCheckBox = new JCheckBox();
		abandonedCheckBox.setText("Abandoned");
		panel2.add(abandonedCheckBox, cc.xy(1, 6, CellConstraints.DEFAULT, CellConstraints.FILL));
		rejectedCheckBox = new JCheckBox();
		rejectedCheckBox.setText("Rejected");
		panel2.add(rejectedCheckBox, cc.xy(1, 7, CellConstraints.DEFAULT, CellConstraints.FILL));
		reviewNeedsFixingCheckBox = new JCheckBox();
		reviewNeedsFixingCheckBox.setEnabled(false);
		reviewNeedsFixingCheckBox.setText("Review needs fixing");
		panel2.add(reviewNeedsFixingCheckBox, cc.xy(1, 8, CellConstraints.DEFAULT, CellConstraints.FILL));
		final JLabel label9 = new JLabel();
		label9.setText("Review State");
		rootPanel.add(label9, cc.xy(5, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootPanel;
	}

	private static final class ProjectComboBoxItem {
		private final com.atlassian.theplugin.commons.crucible.api.model.Project project;

		private ProjectComboBoxItem(com.atlassian.theplugin.commons.crucible.api.model.Project project) {
			this.project = project;
		}

		public String toString() {
			return project.getName();
		}

		public com.atlassian.theplugin.commons.crucible.api.model.Project getProject() {
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
}
