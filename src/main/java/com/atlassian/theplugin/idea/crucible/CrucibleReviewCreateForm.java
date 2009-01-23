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
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewBean;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.UserBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import static com.intellij.openapi.ui.Messages.showMessageDialog;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import static java.lang.System.arraycopy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//enum ReviewCreationMode {
//	EMPTY,
//	REVISION,
//	PATCH
//}

public abstract class CrucibleReviewCreateForm extends DialogWrapper {
//	private ReviewCreationMode mode;

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
	private DefaultListModel model;
	private UserListCellRenderer cellRenderer = new UserListCellRenderer();

	protected Project project;
	protected CrucibleServerFacade crucibleServerFacade;
	private final CfgManager cfgManager;

	protected void setCustomComponent(JComponent component) {
		customComponentPanel.removeAll();
		if (component != null) {
			customComponentPanel.add(component);
		}
		customComponentPanel.setVisible(component != null);
		if (component == null) {
			customComponentPanel.setPreferredSize(new Dimension(0, 0));
		} else {
			customComponentPanel.setPreferredSize(null);
		}

		validate();
	}

	public CrucibleReviewCreateForm(Project project, CrucibleServerFacade crucibleServerFacade, String commitMessage, @NotNull final CfgManager cfgManager,
			@NotNull String dialogTitle) {
		super(false);
		this.project = project;
		this.crucibleServerFacade = crucibleServerFacade;
		this.cfgManager = cfgManager;
		setTitle(dialogTitle);

		$$$setupUI$$$();
		init();
		customComponentPanel.setLayout(new BorderLayout());
		titleText.setText(commitMessage);
		getOKAction().putValue(Action.NAME, "Create review...");
		crucibleServersComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (crucibleServersComboBox.getItemCount() > 0 && crucibleServersComboBox.getSelectedItem() != null && crucibleServersComboBox.getSelectedItem() instanceof ServerComboBoxItem) {
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
			}
		});

		reviewersList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					int index = reviewersList.getSelectedIndex();
					setCheckboxState(index);
					refreshUserModel();
				}
			}
		});

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
		titleText.addActionListener(enableOkActionListener);


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
			reviewersList.revalidate();
			reviewersList.repaint();
		}
		getOKAction().setEnabled(isValidForm());
	}

	private void setCheckboxState(int index) {
		if (index != -1) {
			UserListItem pi = (UserListItem) reviewersList.getModel().getElementAt(index);
			pi.setSelected(!pi.isSelected());
			setViewState(index, pi.isSelected());
			repaint();
		}
	}

	private void setViewState(int index, boolean newState) {
		int[] oldIdx = reviewersList.getSelectedIndices();
		int[] newIdx;
		if (newState) {
			newIdx = new int[oldIdx.length + 1];
			arraycopy(newIdx, 0, oldIdx, 0, oldIdx.length);
			newIdx[newIdx.length - 1] = index;
		} else {
			newIdx = new int[Math.max(0, oldIdx.length - 1)];
			int i = 0;
			for (int id : oldIdx) {
				if (id == index) {
					continue;
				}
				newIdx[i++] = id;
			}
		}
		reviewersList.setSelectedIndices(newIdx);
	}

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
				"center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,center:max(d;4px):noGrow,center:p:grow,top:3dlu:noGrow,center:d:grow,top:3dlu:noGrow,center:max(d;4px):noGrow"));
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
				"center:d:noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow"));
		rootComponent.add(panel1, cc.xy(1, 5));
		final JLabel label2 = new JLabel();
		label2.setText("Server:");
		panel1.add(label2, cc.xy(1, 1, CellConstraints.DEFAULT, CellConstraints.TOP));
		crucibleServersComboBox = new JComboBox();
		panel1.add(crucibleServersComboBox, cc.xy(3, 1));
		final JLabel label3 = new JLabel();
		label3.setInheritsPopupMenu(false);
		label3.setText("Project:");
		panel1.add(label3, cc.xy(1, 3, CellConstraints.DEFAULT, CellConstraints.TOP));
		projectsComboBox = new JComboBox();
		panel1.add(projectsComboBox, cc.xy(3, 3));
		final JLabel label4 = new JLabel();
		label4.setText("Repository:");
		panel1.add(label4, cc.xy(1, 5, CellConstraints.DEFAULT, CellConstraints.TOP));
		final JLabel label5 = new JLabel();
		label5.setText("Moderator:");
		panel1.add(label5, cc.xy(1, 7, CellConstraints.DEFAULT, CellConstraints.TOP));
		final JLabel label6 = new JLabel();
		label6.setText("Author:");
		panel1.add(label6, cc.xy(1, 9, CellConstraints.DEFAULT, CellConstraints.TOP));
		repoComboBox = new JComboBox();
		panel1.add(repoComboBox, cc.xy(3, 5));
		moderatorComboBox = new JComboBox();
		panel1.add(moderatorComboBox, cc.xy(3, 7));
		authorComboBox = new JComboBox();
		panel1.add(authorComboBox, cc.xy(3, 9));
		allowCheckBox = new JCheckBox();
		allowCheckBox.setEnabled(true);
		allowCheckBox.setText("Allow anyone to join");
		panel1.add(allowCheckBox, cc.xy(7, 9));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new BorderLayout(0, 0));
		panel1.add(panel2, cc.xywh(7, 1, 1, 7, CellConstraints.DEFAULT, CellConstraints.FILL));
		final JScrollPane scrollPane1 = new JScrollPane();
		panel2.add(scrollPane1, BorderLayout.CENTER);
		scrollPane1.setViewportView(reviewersList);
		final JLabel label7 = new JLabel();
		label7.setText("Reviewers:");
		panel1.add(label7, cc.xy(5, 1, CellConstraints.DEFAULT, CellConstraints.TOP));
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
		label6.setLabelFor(scrollPane1);
		label8.setLabelFor(statementArea);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootComponent;
	}

	private static final class ServerComboBoxItem {
		private final CrucibleServerCfg server;

		private ServerComboBoxItem(CrucibleServerCfg server) {
			this.server = server;
		}

		@Override
		public String toString() {
			return server.getName();
		}

		public CrucibleServerCfg getServer() {
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

	private static final class RepositoryComboBoxItem {
		private final Repository repo;

		private RepositoryComboBoxItem(@NotNull final Repository repo) {
			this.repo = repo;
		}

		@Override
		public String toString() {
			return repo.getName();
		}

		public Repository getRepository() {
			return repo;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			final RepositoryComboBoxItem that = (RepositoryComboBoxItem) o;

			//noinspection RedundantIfStatement
			if (!repo.equals(that.repo)) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return repo.hashCode();
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
		final Collection<CrucibleServerCfg> enabledServers = cfgManager.getAllEnabledCrucibleServers(CfgUtil.getProjectId(project));
		if (enabledServers.isEmpty()) {
			crucibleServersComboBox.setEnabled(false);
			crucibleServersComboBox.addItem("Enable a Crucible server first!");
			getOKAction().setEnabled(false);
		} else {
			for (CrucibleServerCfg server : enabledServers) {
				crucibleServersComboBox.addItem(new ServerComboBoxItem(server));
			}
			ProjectConfiguration prjCfg = cfgManager.getProjectConfiguration(CfgUtil.getProjectId(project));
			if (prjCfg != null) {
				final CrucibleServerCfg defCrucServer = prjCfg.getDefaultCrucibleServer();
				if (defCrucServer != null) {
					crucibleServersComboBox.setSelectedItem(new ServerComboBoxItem(defCrucServer));
				}
			}
		}
	}

	private void fillServerRelatedCombos(final CrucibleServerCfg server) {
		projectsComboBox.removeAllItems();
		repoComboBox.removeAllItems();
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
						repositories = crucibleServerFacade.getRepositories(server);
						users = crucibleServerFacade.getUsers(server);
					} catch (final Exception e) {
						if (CrucibleReviewCreateForm.this.getRootComponent().isShowing()) {
							ApplicationManager.getApplication().invokeAndWait(new Runnable() {
								public void run() {
									DialogWithDetails.showExceptionDialog(project, "Cannot retrieve data from Crucible server",
											e, "Error");
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

	private Map<ServerId, CrucibleServerData> crucibleData = MiscUtil.buildConcurrentHashMap(5);


	private void updateServerRelatedCombos(final CrucibleServerCfg server, final CrucibleServerData crucibleServerData) {

		final ServerComboBoxItem selectedItem = (ServerComboBoxItem) crucibleServersComboBox.getSelectedItem();
		if (selectedItem == null || selectedItem.getServer().equals(server) == false) {
			return;
		}

		// we are doing here once more, as it's executed by a separate thread and meantime
		// the combos could have been populated by another thread
		projectsComboBox.removeAllItems();
		repoComboBox.removeAllItems();
		authorComboBox.removeAllItems();
		moderatorComboBox.removeAllItems();
		model.removeAllElements();

		ProjectConfiguration prjCfg = cfgManager.getProjectConfiguration(CfgUtil.getProjectId(project));
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
		authorComboBox.addItem("");
		moderatorComboBox.addItem("");
		if (!crucibleServerData.getUsers().isEmpty()) {
			int indexToSelect = -1;
			int index = 0;
			for (User user : crucibleServerData.getUsers()) {
				authorComboBox.addItem(new UserComboBoxItem(user));
				moderatorComboBox.addItem(new UserComboBoxItem(user));
				if (user.getUserName().equals(server.getUsername())) {
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
		private final CrucibleServerCfg server;

		public ReviewProvider(CrucibleServerCfg server) {
			super(server.getUrl());
			this.server = server;
		}

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
			user.setUserName(server.getUsername());
			return user;
		}

		@Override
		public String getDescription() {
			return statementArea.getText();
		}

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

		@Override
		public PermId getParentReview() {
			return null;
		}

		@Override
		public PermId getPermId() {
			return null;
		}

		@Override
		public String getProjectKey() {
			return ((ProjectComboBoxItem) projectsComboBox.getSelectedItem()).getWrappedProject().getKey();
		}

		@Override
		public String getRepoName() {
			if (repoComboBox.getSelectedItem() instanceof RepositoryComboBoxItem) {
				return ((RepositoryComboBoxItem) repoComboBox.getSelectedItem()).getRepository().getName();
			} else {
				return null;
			}
		}

		@Override
		public State getState() {
			return null;
		}

		@Override
		public boolean isAllowReviewerToJoin() {
			return allowCheckBox.isSelected();
		}
	}


	protected abstract Review createReview(CrucibleServerCfg server, ReviewProvider reviewProvider) throws RemoteApiException,
			ServerPasswordNotProvidedException;

	@Override
	protected void doOKAction() {
		final ServerComboBoxItem selectedItem = (ServerComboBoxItem) crucibleServersComboBox.getSelectedItem();
		if (selectedItem != null) {
			final CrucibleServerCfg server = selectedItem.getServer();
			try {
				final Review draftReview = createReview(server, new ReviewProvider(server));
				if (draftReview == null) {
					return;
				}

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
						if (newReview.getModerator().getUserName().equals(server.getUsername())) {
							if (newReview.getActions()
									.contains(com.atlassian.theplugin.commons.crucible.api.model.Action.APPROVE)) {
								crucibleServerFacade.approveReview(server, draftReview.getPermId());
							} else {
								Messages.showErrorDialog(project,
										newReview.getAuthor().getDisplayName() + " is authorized to approve review.\n"
												+ "Leaving review in draft state.", "Permission denied");
							}
						} else {
							if (newReview.getActions()
									.contains(com.atlassian.theplugin.commons.crucible.api.model.Action.SUBMIT)) {
								crucibleServerFacade.submitReview(server, draftReview.getPermId());
							} else {
								Messages.showErrorDialog(project,
										newReview.getAuthor().getDisplayName() + " is authorized submit review.\n"
												+ "Leaving review in draft state.", "Permission denied");
							}
						}
					} catch (ValueNotYetInitialized valueNotYetInitialized) {
						Messages.showErrorDialog(project, "Unable to change review state. Leaving review in draft state.",
								"Permission denied");
					}
				}
				super.doOKAction();
			} catch (RemoteApiException e) {
				showMessageDialog(e.getMessage(), "Error creating review: " + server.getUrl(), Messages.getErrorIcon());
			} catch (ServerPasswordNotProvidedException e) {
				showMessageDialog(e.getMessage(), "Error creating review: " + server.getUrl(), Messages.getErrorIcon());
			}
		}
	}

	protected boolean isValid(ReviewProvider reviewProvider) {
		return true;
	}

	private boolean isValidForm() {

		if (crucibleServersComboBox.getSelectedItem() instanceof ServerComboBoxItem && titleText.getText().length() > 0
				&& projectsComboBox.getSelectedItem() instanceof ProjectComboBoxItem
				&& authorComboBox.getSelectedItem() instanceof UserComboBoxItem && moderatorComboBox.getSelectedItem() instanceof UserComboBoxItem) {
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

//	private boolean isReviewerSelected() {
//		for (int i = 0; i < model.size(); i++) {
//			if (((UserListItem) model.get(i)).isSelected()) {
//				return true;
//			}
//		}
//
//		return false;
//	}
}
  
