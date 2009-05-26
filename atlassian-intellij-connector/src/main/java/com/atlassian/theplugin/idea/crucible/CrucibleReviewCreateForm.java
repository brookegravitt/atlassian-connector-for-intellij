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
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.crucible.model.UpdateReason;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.config.IntelliJProjectCfgManager;
import com.atlassian.theplugin.idea.crucible.comboitems.RepositoryComboBoxItem;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.ListSpeedSearch;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static javax.swing.Action.NAME;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;


public abstract class CrucibleReviewCreateForm extends DialogWrapper {
	private JPanel rootComponent;
	private JTextField titleText;
	private JComboBox crucibleServersComboBox;
	private JTextArea statementArea;
	private JComboBox repoComboBox;
	private JComboBox projectsComboBox;
	private JComboBox authorComboBox;
	private JComboBox moderatorComboBox;
	private JList reviewersList;
	private JCheckBox allowCheckBox;
	private JCheckBox leaveAsDraftCheckBox;
	private JPanel customComponentPanel;
	private JLabel repositoryLabel;
	private JLabel selectedReviewers;
	private DefaultListModel model;
	private UserListCellRenderer cellRenderer = new UserListCellRenderer();

	protected Project project;
	protected CrucibleServerFacade crucibleServerFacade;
	private final IntelliJProjectCfgManager projectCfgManager;
	private int reviewCreationTimeout = -1;
	private static final int MILLISECONDS_IN_MINUTE = 1000 * 60;

	protected void setCustomComponent(JComponent component) {
		customComponentPanel.removeAll();
		if (component != null) {
			customComponentPanel.add(component);
			customComponentPanel.validate();
		}
	}

	public CrucibleReviewCreateForm(Project project, CrucibleServerFacade crucibleServerFacade, String commitMessage,
			@NotNull final IntelliJProjectCfgManager projectCfgManager, @NotNull String dialogTitle) {
		super(false);
		this.project = project;
		this.crucibleServerFacade = crucibleServerFacade;
		this.projectCfgManager = projectCfgManager;
		setTitle(dialogTitle);

		$$$setupUI$$$();
		init();

		if (!shouldShowRepo()) {
			repositoryLabel.setVisible(false);
			repoComboBox.setVisible(false);
		}

		customComponentPanel.setLayout(new BorderLayout());
		titleText.setText(commitMessage);
		getOKAction().putValue(NAME, "Create review...");
		crucibleServersComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (crucibleServersComboBox.getItemCount() > 0 && crucibleServersComboBox.getSelectedItem() != null &&
						crucibleServersComboBox.getSelectedItem() instanceof ServerComboBoxItem) {
					final ServerComboBoxItem boxItem = (ServerComboBoxItem) crucibleServersComboBox.getSelectedItem();
					fillServerRelatedCombos(boxItem.getServer());
				}
			}
		});

		reviewersList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int index = reviewersList.locationToIndex(e.getPoint());
				setCheckboxState(index);
				refreshUserModel();
				reviewersList.setSelectedIndex(index);
			}
		});

		new ListSpeedSearch(reviewersList) {
			@Override
			protected boolean compare(final String s, final String s1) {
				return s != null && s1 != null ? s.toUpperCase().contains(s1.toUpperCase()) : super.compare(s, s1);
			}
		};

//		reviewersList.addKeyListener(new KeyAdapter() {
//			@Override
//			public void keyPressed(KeyEvent e) {
//				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
//					int index = reviewersList.getSelectedIndex();
//					setCheckboxState(index);
//					refreshUserModel();
//					reviewersList.setSelectedIndex(index);
//				}
//			}
//		});

		moderatorComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent event) {

				if (moderatorComboBox.getSelectedItem() instanceof UserComboBoxItem) {
					refreshUserModel();
				}
			}
		});

		ActionListener enableOkActionListener = new ActionListener() {

			public void actionPerformed(final ActionEvent event) {
				getOKAction().setEnabled(isValidForm());
			}
		};

		authorComboBox.addActionListener(enableOkActionListener);
		repoComboBox.addActionListener(enableOkActionListener);
		projectsComboBox.addActionListener(enableOkActionListener);
		crucibleServersComboBox.addActionListener(enableOkActionListener);
		titleText.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(final DocumentEvent e) {
				getOKAction().setEnabled(isValidForm());
			}

			public void removeUpdate(final DocumentEvent e) {
				getOKAction().setEnabled(isValidForm());
			}

			public void changedUpdate(final DocumentEvent e) {
				getOKAction().setEnabled(isValidForm());
			}
		});

		fillInCrucibleServers();
	}

	private void refreshUserModel() {
		final Object selectedItem = moderatorComboBox.getSelectedItem();
		if (selectedItem instanceof UserComboBoxItem) {
			final UserComboBoxItem userComboBoxItem = (UserComboBoxItem) selectedItem;
			User moderatorUser = userComboBoxItem.getUser();
			final ArrayList<User> disabledUsers = new ArrayList<User>();
			disabledUsers.add(moderatorUser);
			cellRenderer.setDisabledUsers(disabledUsers);
			for (int i = 0; i < model.size(); i++) {
				UserListItem reviewer = (UserListItem) model.get(i);
				if (reviewer.getUser().equals(moderatorUser)) {
					reviewer.setSelected(false);
				}
			}
			reviewersList.setModel(model);

			Collection<UserListItem> displayedSelectedUsers = new ArrayList<UserListItem>();
			Collection<UserListItem> allSelectedUsers = new ArrayList<UserListItem>();
			for (int i = 0; i < reviewersList.getModel().getSize(); ++i) {
				UserListItem user = (UserListItem) reviewersList.getModel().getElementAt(i);
				if (user.isSelected()) {
					allSelectedUsers.add(user);
					displayedSelectedUsers.add(user);

					String displayStr = prepareSelectedReviewersString(displayedSelectedUsers, allSelectedUsers);
					int displayStrWidth = selectedReviewers.getFontMetrics(selectedReviewers.getFont()).stringWidth(displayStr);

					if (displayStrWidth > reviewersList.getWidth()) {
						displayedSelectedUsers.remove(user);
					}
				}
			}

			if (displayedSelectedUsers.size() == 0) {
				this.selectedReviewers.setText("None");
				this.selectedReviewers.setToolTipText(null);
			} else {
				String labelText = prepareSelectedReviewersString(displayedSelectedUsers, allSelectedUsers);
				if (displayedSelectedUsers.size() < allSelectedUsers.size()) {
					labelText += " ...";
				}
				this.selectedReviewers.setText(labelText);
				this.selectedReviewers.setToolTipText(prepareSelectedReviewersTooltip(allSelectedUsers));
			}

			reviewersList.revalidate();
			reviewersList.repaint();
		}
		getOKAction().setEnabled(isValidForm());
	}

	private String prepareSelectedReviewersTooltip(final Collection<UserListItem> selectedUsersTooltip) {
		StringBuilder ret = new StringBuilder("<html>");

		for (UserListItem user : selectedUsersTooltip) {
			ret.append(user.getUser().getDisplayName());
			ret.append("<br>");
		}

		ret.append("</html>");

		return ret.toString();
	}

	private String prepareSelectedReviewersString(final Collection<UserListItem> selectedUsersLabel,
			final Collection<UserListItem> allSelectedUsers) {
		return "(" + allSelectedUsers.size() + " of " + reviewersList.getModel().getSize() + ") "
				+ StringUtils.join(selectedUsersLabel, ", ");
	}

	private void setCheckboxState(int index) {
		if (index != -1) {
			UserListItem pi = (UserListItem) reviewersList.getModel().getElementAt(index);
			pi.setSelected(!pi.isSelected());
//			setViewState(index, pi.isSelected());
			repaint();
		}
	}

	// CHECKSTYLE:ON

	@Override
	public JComponent getPreferredFocusedComponent() {
		return titleText;
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		createUIComponents();
		rootComponent = new JPanel();
		rootComponent.setLayout(new FormLayout("fill:d:grow",
				"center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,center:max(d;4px):noGrow,center:p:grow,top:3dlu:noGrow,fill:d:noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow"));
		rootComponent.setMinimumSize(new Dimension(800, 505));
		final JLabel label1 = new JLabel();
		label1.setText("Title:");
		CellConstraints cc = new CellConstraints();
		rootComponent.add(label1, cc.xy(1, 1));
		titleText = new JTextField();
		rootComponent.add(titleText, cc.xy(1, 3, CellConstraints.FILL, CellConstraints.DEFAULT));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new FormLayout(
				"fill:d:noGrow,left:4dlu:noGrow,fill:300px:grow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:max(p;4px):grow",
				"center:d:noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
		rootComponent.add(panel1, cc.xy(1, 5));
		final JLabel label2 = new JLabel();
		label2.setText("Server:");
		panel1.add(label2, cc.xy(1, 1, CellConstraints.DEFAULT, CellConstraints.CENTER));
		crucibleServersComboBox = new JComboBox();
		panel1.add(crucibleServersComboBox, cc.xy(3, 1));
		final JLabel label3 = new JLabel();
		label3.setInheritsPopupMenu(false);
		label3.setText("Project:");
		panel1.add(label3, cc.xy(1, 3, CellConstraints.DEFAULT, CellConstraints.CENTER));
		projectsComboBox = new JComboBox();
		panel1.add(projectsComboBox, cc.xy(3, 3));
		repositoryLabel = new JLabel();
		repositoryLabel.setText("Repository:");
		panel1.add(repositoryLabel, cc.xy(1, 5, CellConstraints.DEFAULT, CellConstraints.CENTER));
		final JLabel label4 = new JLabel();
		label4.setText("Moderator:");
		panel1.add(label4, cc.xy(1, 7, CellConstraints.DEFAULT, CellConstraints.CENTER));
		final JLabel label5 = new JLabel();
		label5.setText("Author:");
		panel1.add(label5, cc.xy(1, 9, CellConstraints.DEFAULT, CellConstraints.CENTER));
		repoComboBox = new JComboBox();
		panel1.add(repoComboBox, cc.xy(3, 5));
		moderatorComboBox = new JComboBox();
		panel1.add(moderatorComboBox, cc.xy(3, 7));
		authorComboBox = new JComboBox();
		panel1.add(authorComboBox, cc.xy(3, 9));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new BorderLayout(0, 0));
		panel1.add(panel2, cc.xywh(7, 1, 1, 7, CellConstraints.DEFAULT, CellConstraints.FILL));
		final JScrollPane scrollPane1 = new JScrollPane();
		panel2.add(scrollPane1, BorderLayout.CENTER);
		scrollPane1.setViewportView(reviewersList);
		final JLabel label6 = new JLabel();
		label6.setText("Reviewers: ");
		panel1.add(label6, cc.xy(5, 1, CellConstraints.RIGHT, CellConstraints.TOP));
		allowCheckBox = new JCheckBox();
		allowCheckBox.setEnabled(true);
		allowCheckBox.setText("Allow anyone to join");
		panel1.add(allowCheckBox, cc.xy(7, 11));
		final JLabel label7 = new JLabel();
		label7.setText("Selected: ");
		panel1.add(label7, cc.xy(5, 9, CellConstraints.RIGHT, CellConstraints.DEFAULT));
		selectedReviewers = new JLabel();
		selectedReviewers.setHorizontalTextPosition(2);
		selectedReviewers.setText("None");
		panel1.add(selectedReviewers, cc.xy(7, 9, CellConstraints.LEFT, CellConstraints.DEFAULT));
		final JLabel label8 = new JLabel();
		label8.setText("Statement of Objectives:");
		rootComponent.add(label8, cc.xy(1, 7));
		final JScrollPane scrollPane2 = new JScrollPane();
		rootComponent.add(scrollPane2, cc.xy(1, 9, CellConstraints.FILL, CellConstraints.FILL));
		statementArea = new JTextArea();
		statementArea.setLineWrap(true);
		statementArea.setRows(5);
		scrollPane2.setViewportView(statementArea);
		customComponentPanel = new JPanel();
		customComponentPanel.setLayout(new BorderLayout(0, 0));
		rootComponent.add(customComponentPanel, cc.xy(1, 11, CellConstraints.DEFAULT, CellConstraints.FILL));
		leaveAsDraftCheckBox = new JCheckBox();
		leaveAsDraftCheckBox.setText("Save review as Draft");
		rootComponent.add(leaveAsDraftCheckBox, cc.xy(1, 13));
		label1.setLabelFor(titleText);
		label2.setLabelFor(crucibleServersComboBox);
		label5.setLabelFor(scrollPane1);
		label8.setLabelFor(statementArea);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootComponent;
	}

	// CHECKSTYLE:OFF

	private static final class ServerComboBoxItem {
		private final ServerData server;

		private ServerComboBoxItem(ServerData server) {
			this.server = server;
		}

		@Override
		public String toString() {
			return server.getName();
		}

		public ServerData getServer() {
			return server;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof ServerComboBoxItem)) {
				return false;
			}

			final ServerComboBoxItem boxItem = (ServerComboBoxItem) o;

			//noinspection RedundantIfStatement
			if (!server.equals(boxItem.server)) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return server.hashCode();
		}
	}

	private static final class ProjectComboBoxItem {
		private final CrucibleProject wrappedProject;

		private ProjectComboBoxItem(@NotNull final CrucibleProject project) {
			this.wrappedProject = project;
		}

		@Override
		public String toString() {
			return wrappedProject.getName();
		}

		public CrucibleProject getWrappedProject() {
			return wrappedProject;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			final ProjectComboBoxItem that = (ProjectComboBoxItem) o;

			//noinspection RedundantIfStatement
			if (!wrappedProject.equals(that.wrappedProject)) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return wrappedProject.hashCode();
		}
	}

	private static final class UserComboBoxItem {
		private final User user;

		private UserComboBoxItem(User user) {
			this.user = user;
		}

		@Override
		public String toString() {
			return user.getDisplayName();
		}

		public User getUser() {
			return user;
		}
	}


	private void fillInCrucibleServers() {
		final Collection<CrucibleServerCfg> enabledServers = projectCfgManager.getCfgManager().
				getAllEnabledCrucibleServers(CfgUtil.getProjectId(project));
		if (enabledServers.isEmpty()) {
			crucibleServersComboBox.setEnabled(false);
			crucibleServersComboBox.addItem("Enable a Crucible server first!");
			getOKAction().setEnabled(false);
		} else {
			for (CrucibleServerCfg server : enabledServers) {
				crucibleServersComboBox.addItem(new ServerComboBoxItem(projectCfgManager.getServerData(server)));
			}
			final ServerData defCrucServer = projectCfgManager.getDefaultCrucibleServer();
			if (defCrucServer != null) {
				crucibleServersComboBox.setSelectedItem(new ServerComboBoxItem(defCrucServer));
			}
		}
	}

	private void fillServerRelatedCombos(final ServerData server) {
		projectsComboBox.removeAllItems();
		if (shouldShowRepo()) {
			repoComboBox.removeAllItems();
		}
		authorComboBox.removeAllItems();
		moderatorComboBox.removeAllItems();
		model.removeAllElements();
		getOKAction().setEnabled(false);

		final CrucibleServerData data = crucibleData.get(server.getServerId());

		if (data == null) {
			new Thread(new Runnable() {
				public void run() {
					List<CrucibleProject> projects = new ArrayList<CrucibleProject>();
					List<Repository> repositories = new ArrayList<Repository>();
					List<User> users = new ArrayList<User>();

					try {
						projects = crucibleServerFacade.getProjects(server);
						if (shouldShowRepo()) {
							repositories = crucibleServerFacade.getRepositories(server);
						}
						users = crucibleServerFacade.getUsers(server);
					} catch (final Exception e) {
						if (CrucibleReviewCreateForm.this.getRootComponent().isShowing()) {
							ApplicationManager.getApplication().invokeAndWait(new Runnable() {
								public void run() {
									DialogWithDetails.showExceptionDialog(project, "Cannot retrieve data from Crucible server",
											e);
								}
							}, ModalityState.stateForComponent(CrucibleReviewCreateForm.this.getRootComponent()));
						}
					}
					final CrucibleServerData crucibleServerData = new CrucibleServerData(repositories, projects, users);
					crucibleData.put(server.getServerId(), crucibleServerData);
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							updateServerRelatedCombos(server, crucibleServerData);
						}
					});
				}
			}, "atlassian-idea-plugin crucible patch upload combos refresh").start();
		} else {
			updateServerRelatedCombos(server, data);

		}
	}


	protected static class CrucibleServerData {
		private final List<CrucibleProject> projects;

		private final List<Repository> repositories;

		private final List<User> users;

		public CrucibleServerData(final List<Repository> repositories, final List<CrucibleProject> projects,
				final List<User> users) {
			this.repositories = repositories;
			this.projects = projects;
			this.users = users;
		}

		public List<CrucibleProject> getProjects() {
			return projects;
		}

		public List<Repository> getRepositories() {
			return repositories;
		}

		public List<User> getUsers() {
			return users;
		}
	}

	private Map<String, CrucibleServerData> crucibleData = MiscUtil.buildConcurrentHashMap(5);


	private void updateServerRelatedCombos(final ServerData server, final CrucibleServerData crucibleServerData) {

		final ServerComboBoxItem selectedItem = (ServerComboBoxItem) crucibleServersComboBox.getSelectedItem();
		if (selectedItem == null || !selectedItem.getServer().equals(server)) {
			return;
		}

		// we are doing here once more, as it's executed by a separate thread and meantime
		// the combos could have been populated by another thread
		projectsComboBox.removeAllItems();
		if (shouldShowRepo()) {
			repoComboBox.removeAllItems();
		}
		authorComboBox.removeAllItems();
		moderatorComboBox.removeAllItems();
		model.removeAllElements();

		ProjectConfiguration prjCfg = projectCfgManager.getCfgManager().getProjectConfiguration(CfgUtil.getProjectId(project));
		if (crucibleServerData.getProjects().isEmpty()) {
			projectsComboBox.setEnabled(false);
			projectsComboBox.addItem("No projects");
			getOKAction().setEnabled(false);
		} else {
			projectsComboBox.setEnabled(true);
			for (CrucibleProject myProject : crucibleServerData.getProjects()) {
				projectsComboBox.addItem(new ProjectComboBoxItem(myProject));
			}

			// setting default project if such is defined
			if (prjCfg != null) {
				final String defaultProjectKey = prjCfg.getDefaultCrucibleProject();
				if (defaultProjectKey != null) {
					for (int i = 0; i < projectsComboBox.getItemCount(); ++i) {
						if (projectsComboBox.getItemAt(i) instanceof ProjectComboBoxItem) {
							if (((ProjectComboBoxItem) projectsComboBox.getItemAt(i)).getWrappedProject().getKey()
									.equals(defaultProjectKey)) {
								projectsComboBox.setSelectedIndex(i);
								break;
							}
						}
					}
				}
			}
		}

		if (shouldShowRepo()) {
			repoComboBox.addItem(""); // repo is not required for instance for patch review
			if (!crucibleServerData.getRepositories().isEmpty()) {
				for (Repository repo : crucibleServerData.getRepositories()) {
					repoComboBox.addItem(new RepositoryComboBoxItem(repo));
				}

				// setting default repo if such is defined

				if (prjCfg != null) {
					final String defaultRepo = prjCfg.getDefaultCrucibleRepo();
					if (defaultRepo != null) {
						for (int i = 0; i < repoComboBox.getItemCount(); ++i) {
							if (repoComboBox.getItemAt(i) instanceof RepositoryComboBoxItem) {
								if (((RepositoryComboBoxItem) repoComboBox.getItemAt(i)).getRepository().getName()
										.equals(defaultRepo)) {
									repoComboBox.setSelectedIndex(i);
									break;
								}
							}
						}
					}
				}
				getOKAction().setEnabled(true);
			}
			// if only one repository
			if (shouldAutoSelectRepo(crucibleServerData)) {
				repoComboBox.setSelectedIndex(repoComboBox.getItemCount() - 1);
			}
		}
		authorComboBox.addItem("");
		moderatorComboBox.addItem("");
		if (!crucibleServerData.getUsers().isEmpty()) {
			int indexToSelect = -1;
			int index = 0;
			for (User user : crucibleServerData.getUsers()) {
				authorComboBox.addItem(new UserComboBoxItem(user));
				moderatorComboBox.addItem(new UserComboBoxItem(user));
				if (user.getUserName().equals(server.getUserName())) {
					indexToSelect = index + 1;
				}

				model.addElement(new UserListItem(user, false));
				index++;
			}
			if (indexToSelect != -1) {
				authorComboBox.setSelectedIndex(indexToSelect);
				moderatorComboBox.setSelectedIndex(indexToSelect);
			}
		}

		getOKAction().setEnabled(isValidForm());

	}

	protected boolean shouldShowRepo() {
		return true;
	}


	protected boolean shouldAutoSelectRepo(CrucibleServerData crucibleServerData) {
		return false;
	}

	public JComponent getRootComponent() {
		return rootComponent;
	}

	@Override
	@Nullable
	protected JComponent createCenterPanel() {
		return getRootComponent();
	}

	protected class ReviewProvider extends ReviewBean {
		private final ServerData server;

		public ReviewProvider(ServerData server) {
			super(server.getUrl());
			this.server = server;
		}

		@NotNull
		@Override
		public User getAuthor() {
			if (authorComboBox.getSelectedItem() instanceof UserComboBoxItem) {
				return ((UserComboBoxItem) authorComboBox.getSelectedItem()).getUser();
			} else {
				return null;
			}
		}

		@Override
		public User getCreator() {
			UserBean user = new UserBean();
			user.setUserName(server.getUserName());
			return user;
		}

		@Override
		public String getDescription() {
			return statementArea.getText();
		}

		@NotNull
		@Override
		public User getModerator() {
			if (moderatorComboBox.getSelectedItem() instanceof UserComboBoxItem) {
				return ((UserComboBoxItem) moderatorComboBox.getSelectedItem()).getUser();
			} else {
				return null;
			}
		}

		@Override
		public String getName() {
			return titleText.getText();
		}

		@Nullable
		@Override
		public PermId getParentReview() {
			return null;
		}

		@Nullable
		@Override
		public PermId getPermId() {
			return null;
		}

		@NotNull
		@Override
		public String getProjectKey() {
			return ((ProjectComboBoxItem) projectsComboBox.getSelectedItem()).getWrappedProject().getKey();
		}

		@Nullable
		@Override
		public String getRepoName() {
			if (repoComboBox.getSelectedItem() instanceof RepositoryComboBoxItem) {
				return ((RepositoryComboBoxItem) repoComboBox.getSelectedItem()).getRepository().getName();
			} else {
				return null;
			}
		}

		@Nullable
		@Override
		public State getState() {
			return null;
		}

		@Override
		public boolean isAllowReviewerToJoin() {
			return allowCheckBox.isSelected();
		}
	}


	protected abstract Review createReview(ServerData server, ReviewProvider reviewProvider)
			throws RemoteApiException,
			ServerPasswordNotProvidedException;

	@Override
	protected void doOKAction() {
		runCreateReviewTask(true);
		super.doOKAction();
	}

	protected void setReviewCreationTimeout(int reviewCreationTimeout) {
		this.reviewCreationTimeout = reviewCreationTimeout;
	}

	protected void runCreateReviewTask(final boolean runUntilSuccessful) {
		final ServerComboBoxItem selectedItem = (ServerComboBoxItem) crucibleServersComboBox.getSelectedItem();
		if (selectedItem != null) {
			final Date startDate = new Date();

			final ServerData server = selectedItem.getServer();

			Task.Backgroundable changesTask = new Task.Backgroundable(project, "Creating review...", runUntilSuccessful) {

				public boolean isCancelled = false;

				@Override
				public void run(@NotNull final ProgressIndicator indicator) {

					boolean submissionSuccess = false;
					do {
						indicator.setText("Attempting to create review... ");
						ModalityState modalityState = ModalityState
								.stateForComponent(CrucibleReviewCreateForm.this.getRootComponent());

                        Review newlyCreated = null;
						try {
							final Review draftReview = createReview(server, new ReviewProvider(server));
							if (draftReview == null) {
								EventQueue.invokeLater(new Runnable() {
									public void run() {
										Messages.showErrorDialog(
												project, "Review not created. Null returned.", PluginUtil.PRODUCT_NAME);
									}
								});
								return;
							}
							submissionSuccess = true;

							Set<String> users = new HashSet<String>();
							for (int i = 0; i < model.getSize(); ++i) {
								UserListItem item = (UserListItem) model.get(i);
								if (item.isSelected()) {
									users.add(item.getUser().getUserName());
								}
							}

							if (!users.isEmpty()) {
								crucibleServerFacade.addReviewers(server, draftReview.getPermId(), users);
							}

							if (!leaveAsDraftCheckBox.isSelected()) {
								try {
									Review newReview = crucibleServerFacade.getReview(server, draftReview.getPermId());
									if (newReview.getModerator().getUserName().equals(server.getUserName())) {
										if (newReview.getActions().contains(CrucibleAction.APPROVE)) {
											newlyCreated = crucibleServerFacade.approveReview(server, draftReview.getPermId());
										} else {
											Messages.showErrorDialog(project,
													newReview.getAuthor().getDisplayName() +
															" is authorized to approve review.\n"
															+ "Leaving review in draft state.", "Permission denied");
										}
									} else {
										if (newReview.getActions().contains(CrucibleAction.SUBMIT)) {
											newlyCreated = crucibleServerFacade.submitReview(server, draftReview.getPermId());
										} else {
											Messages.showErrorDialog(project,
													newReview.getAuthor().getDisplayName() + " is authorized submit review.\n"
															+ "Leaving review in draft state.", "Permission denied");
										}
									}
								} catch (ValueNotYetInitialized valueNotYetInitialized) {
									Messages.showErrorDialog(project,
											"Unable to change review state. Leaving review in draft state.",
											"Permission denied");
								}
							} else {
                                newlyCreated = draftReview;
                            }

                            final Review newRevewFinal = newlyCreated != null
                                    ? crucibleServerFacade.getReview(server, newlyCreated.getPermId()) : null;

							ApplicationManager.getApplication().invokeLater(new Runnable() {
								public void run() {
									final ReviewsToolWindowPanel panel = IdeaHelper.getReviewsToolWindowPanel(project);
									if (panel != null && newRevewFinal != null) {
										panel.refresh(UpdateReason.REFRESH);
                                        panel.openReview(new ReviewAdapter(newRevewFinal, server));
									}
								}
							}, modalityState);
						} catch (final Throwable e) {
							if (!runUntilSuccessful) {
								ApplicationManager.getApplication().invokeAndWait(new Runnable() {
									public void run() {
										String message = "Error creating review: " + server.getUrl();
										if (isUnknownChangeSetException(e)) {
											message
													+= "\nSpecified change set could not be found on server. Check selected repository";
										}
										DialogWithDetails.showExceptionDialog(project, message, e);
									}
								}, modalityState);
							} else {
								if (isUnknownChangeSetException(e)) {
									try {
										Date now = new Date();
										if (reviewCreationTimeout > 0
												&& now.getTime() - startDate.getTime() >
												reviewCreationTimeout * MILLISECONDS_IN_MINUTE) {
											SwingUtilities.invokeLater(new Runnable() {
												public void run() {
													Messages.showErrorDialog(project,
															"Creation of the review on server\n"
																	+ selectedItem.getServer().getName()
																	+ " timed out after "
																	+ reviewCreationTimeout + " minutes",
															"Review Creation Timeout");
												}
											});
											break;
										}
										indicator.setText("Waiting for Crucible to update to newest change set...");
										for (int i = 0; i < 10; ++i) {
											if (indicator.isCanceled()) {
												isCancelled = true;
												break;
											}
											Thread.sleep(1000);
										}
									} catch (InterruptedException e1) {
										// eeeem, now what?
									}
								} else {
									ApplicationManager.getApplication().invokeAndWait(new Runnable() {
										public void run() {
											DialogWithDetails.showExceptionDialog(project,
													"Error creating review: " + server.getUrl(), e);
										}
									}, modalityState);
									isCancelled = true;
								}
							}
						}
					} while (runUntilSuccessful && !submissionSuccess && !isCancelled && !indicator.isCanceled());
				}

				private boolean isUnknownChangeSetException(Throwable e) {
					return e != null
							&& e.getMessage() != null
							&& e.getMessage().contains("Specified change set id does not exist");

				}
			};
			ProgressManager.getInstance().run(changesTask);
		}
	}

	protected boolean isValid(ReviewProvider reviewProvider) {
		return true;
	}

	private boolean isValidForm() {
		if (crucibleServersComboBox.getSelectedItem() instanceof ServerComboBoxItem && titleText.getText().length() > 0
				&& projectsComboBox.getSelectedItem() instanceof ProjectComboBoxItem
				&& authorComboBox.getSelectedItem() instanceof UserComboBoxItem
				&& moderatorComboBox.getSelectedItem() instanceof UserComboBoxItem) {
			final ServerComboBoxItem selectedItem = (ServerComboBoxItem) crucibleServersComboBox.getSelectedItem();
			return isValid(new ReviewProvider(selectedItem.getServer()));
		} else {
			return false;
		}
	}

	private void createUIComponents() {
		model = new DefaultListModel();
		reviewersList = new JList(model);
		reviewersList.setCellRenderer(cellRenderer);
		reviewersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
}
  
