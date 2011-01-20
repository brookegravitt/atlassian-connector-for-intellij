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

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.api.model.BasicProject;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleVersionInfo;
import com.atlassian.theplugin.commons.crucible.api.model.PatchAnchorData;
import com.atlassian.theplugin.commons.crucible.api.model.PatchAnchorDataBean;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.exception.PatchCreateErrorException;
import com.atlassian.theplugin.idea.crucible.comboitems.RepositoryComboBoxItem;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsException;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static javax.swing.Action.NAME;


public abstract class CrucibleReviewCreateForm extends DialogWrapper {
	private JPanel rootComponent;
	private JTextField titleText;
	private JComboBox crucibleServersComboBox;
	private JTextArea statementArea;
	private JComboBox projectsComboBox;
	private JComboBox authorComboBox;
	private JComboBox moderatorComboBox;
	private JCheckBox leaveAsDraftCheckBox;
	private JPanel customComponentPanel;
	private JPanel anchorPanel;
	private JCheckBox includeAnchorDataCheckBox;

	private JComboBox anchorRepoComboBox;

	private final UserListCellRenderer cellRenderer = new UserListCellRenderer();

	protected Project project;
	protected final IntelliJCrucibleServerFacade crucibleServerFacade;
	private final ProjectCfgManager projectCfgManager;
	private int reviewCreationTimeout = -1;
	public static final int MILLISECONDS_IN_MINUTE = 1000 * 60;

	protected void setCustomComponent(JComponent component) {
		customComponentPanel.removeAll();
		if (component != null) {
			customComponentPanel.add(component);
			customComponentPanel.validate();
		}
	}
	//testing add revision to review

	protected abstract boolean isPatchForm();

	public CrucibleReviewCreateForm(Project project, IntelliJCrucibleServerFacade crucibleServerFacade, String commitMessage,
			@NotNull final ProjectCfgManager projectCfgManager, @NotNull String dialogTitle) {
		super(false);
		this.project = project;
		this.crucibleServerFacade = crucibleServerFacade;
		this.projectCfgManager = projectCfgManager;
		setTitle(dialogTitle);

		$$$setupUI$$$();
		init();


		includeAnchorDataCheckBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				anchorRepoComboBox.setEnabled(includeAnchorDataCheckBox.isSelected());
			}
		});

		customComponentPanel.setLayout(new BorderLayout());
		titleText.setText(commitMessage);
		getOKAction().putValue(NAME, "Create review...");
		crucibleServersComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (crucibleServersComboBox.getItemCount() > 0 && crucibleServersComboBox.getSelectedItem() != null &&
						crucibleServersComboBox.getSelectedItem() instanceof ServerComboBoxItem) {
					final ServerComboBoxItem boxItem = (ServerComboBoxItem) crucibleServersComboBox.getSelectedItem();
					try {
						CrucibleVersionInfo info = CrucibleReviewCreateForm.this.crucibleServerFacade
								.getServerVersion(boxItem.getServer());
						final boolean enable = info.isVersion24OrGrater() && isPatchForm();
						anchorPanel.setVisible(enable);
						includeAnchorDataCheckBox.setSelected(enable);

					} catch (final RemoteApiException e1) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								DialogWithDetails.showExceptionDialog(
										CrucibleReviewCreateForm.this.project,
										"Cannot determine Crucible version", e1.getCause());
							}
						});
					} catch (final ServerPasswordNotProvidedException e1) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								DialogWithDetails.showExceptionDialog(
										CrucibleReviewCreateForm.this.project,
										"Invalid password or user name", e1.getCause());
							}
						});
					}
					fillServerRelatedCombos(boxItem.getServer());
				}
			}
		});


		projectsComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				updateReviewersList();
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

		includeAnchorDataCheckBox.addActionListener(enableOkActionListener);

		authorComboBox.addActionListener(enableOkActionListener);
		anchorRepoComboBox.addActionListener(enableOkActionListener);
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

	private boolean isAnchorDataValid() {
		return !isPatchForm() || !isAnchorDataAvailable() || isAnchorRepoValid();
	}

	private boolean isAnchorRepoValid() {
		String repoName = getSelectedAnchorRepoName();
		return repoName.length() > 0;
	}


	public boolean isAnchorDataAvailable() {
		return includeAnchorDataCheckBox.isSelected();
	}

	public PatchAnchorData getPatchAnchorData() {
		return new PatchAnchorDataBean(getSelectedAnchorRepoName(), "",
				"");
	}

	private void refreshUserModel() {
		final Object selectedItem = moderatorComboBox.getSelectedItem();
		if (selectedItem instanceof UserComboBoxItem) {
			final UserComboBoxItem userComboBoxItem = (UserComboBoxItem) selectedItem;
			User moderatorUser = userComboBoxItem.getUser();
			final ArrayList<User> disabledUsers = new ArrayList<User>();
			disabledUsers.add(moderatorUser);
			cellRenderer.setDisabledUsers(disabledUsers);
//			for (int i = 0; i < userListModel.size(); i++) {
//				UserListItem reviewer = (UserListItem) userListModel.get(i);
//				if (reviewer.getUser() != null && reviewer.getUser().equals(moderatorUser)) {
//					reviewer.setSelected(false);
//				}
//			}
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
		final JLabel label4 = new JLabel();
		label4.setText("Moderator:");
		panel1.add(label4, cc.xy(1, 7, CellConstraints.DEFAULT, CellConstraints.CENTER));
		final JLabel label5 = new JLabel();
		label5.setText("Author:");
		panel1.add(label5, cc.xy(1, 9, CellConstraints.DEFAULT, CellConstraints.CENTER));
		moderatorComboBox = new JComboBox();
		panel1.add(moderatorComboBox, cc.xy(3, 7));
		authorComboBox = new JComboBox();
		panel1.add(authorComboBox, cc.xy(3, 9));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new BorderLayout(0, 0));
		panel1.add(panel2, cc.xywh(7, 1, 1, 7, CellConstraints.DEFAULT, CellConstraints.FILL));
		final JScrollPane scrollPane1 = new JScrollPane();
		panel2.add(scrollPane1, BorderLayout.CENTER);
		final JLabel label6 = new JLabel();
		label6.setText("Reviewers: ");
		panel1.add(label6, cc.xy(5, 1, CellConstraints.RIGHT, CellConstraints.TOP));
		final JLabel label7 = new JLabel();
		label7.setText("Selected: ");
		panel1.add(label7, cc.xy(5, 9, CellConstraints.RIGHT, CellConstraints.DEFAULT));
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
		private final BasicProject wrappedProject;

		private ProjectComboBoxItem(@NotNull final BasicProject project) {
			this.wrappedProject = project;
		}

		@Override
		public String toString() {
			return wrappedProject.getName();
		}

		public BasicProject getWrappedProject() {
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
		final Collection<ServerData> enabledServers = projectCfgManager.getAllEnabledCrucibleServerss();
		if (enabledServers.isEmpty()) {
			crucibleServersComboBox.setEnabled(false);
			crucibleServersComboBox.addItem("Enable a Crucible server first!");
			getOKAction().setEnabled(false);
		} else {
			for (ServerData server : enabledServers) {
				crucibleServersComboBox.addItem(new ServerComboBoxItem(server));
			}
			final ServerData defCrucServer = projectCfgManager.getDefaultCrucibleServer();
			if (defCrucServer != null) {
				crucibleServersComboBox.setSelectedItem(new ServerComboBoxItem(defCrucServer));
			}
		}
	}

	private void fillServerRelatedCombos(final ServerData server) {
		projectsComboBox.removeAllItems();
		authorComboBox.removeAllItems();
		moderatorComboBox.removeAllItems();
//		userListModel.removeAllElements();
		getOKAction().setEnabled(false);

		final CrucibleServerData data = crucibleData.get(server.getServerId());

		if (data == null) {
			new Thread(new Runnable() {
				public void run() {
					List<BasicProject> projects = new ArrayList<BasicProject>();
					List<Repository> repositories = new ArrayList<Repository>();
					List<User> users = new ArrayList<User>();

					try {
						projects = crucibleServerFacade.getProjects(server);
						if (shouldShowRepo() || isPatchForm()) {
							repositories = crucibleServerFacade.getRepositories(server);
						}
						users = crucibleServerFacade.getUsers(server);
					} catch (final Exception e) {
						if (CrucibleReviewCreateForm.this.getRootComponent().isShowing()) {
							ApplicationManager.getApplication().invokeAndWait(new Runnable() {
								public void run() {
									DialogWithDetails
											.showExceptionDialog(project, "Cannot retrieve data from Crucible server",
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
		private final List<BasicProject> projects;

		private final List<Repository> repositories;

		private final List<User> users;

		public CrucibleServerData(final List<Repository> repositories, final List<BasicProject> projects,
				final List<User> users) {
			this.repositories = repositories;
			this.projects = projects;
			this.users = users;
		}

		public List<BasicProject> getProjects() {
			return projects;
		}

		public List<Repository> getRepositories() {
			return repositories;
		}

		public List<User> getUsers() {
			return users;
		}
	}

	private final Map<ServerId, CrucibleServerData> crucibleData = MiscUtil.buildConcurrentHashMap(5);


	private void updateServerRelatedCombos(final ServerData server, final CrucibleServerData crucibleServerData) {

		final ServerComboBoxItem selectedItem = (ServerComboBoxItem) crucibleServersComboBox.getSelectedItem();
		if (selectedItem == null || !selectedItem.getServer().equals(server)) {
			return;
		}

		// we are doing here once more, as it's executed by a separate thread and meantime
		// the combos could have been populated by another thread
		projectsComboBox.removeAllItems();
		if (isPatchForm()) {
			anchorRepoComboBox.removeAllItems();
		}
		authorComboBox.removeAllItems();
		moderatorComboBox.removeAllItems();
//		userListModel.removeAllElements();

		if (crucibleServerData.getProjects().isEmpty()) {
			projectsComboBox.setEnabled(false);
			projectsComboBox.addItem("No projects");
			getOKAction().setEnabled(false);
		} else {
			projectsComboBox.setEnabled(true);
			for (BasicProject myProject : crucibleServerData.getProjects()) {
				projectsComboBox.addItem(new ProjectComboBoxItem(myProject));
			}

			// setting default project if such is defined
			if (projectCfgManager != null) {
				final String defaultProjectKey = projectCfgManager.getDefaultCrucibleProject();
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

		if (!crucibleServerData.getRepositories().isEmpty() && isPatchForm()) {
			for (Repository repo : crucibleServerData.getRepositories()) {
				anchorRepoComboBox
						.addItem(new com.atlassian.theplugin.idea.crucible.comboitems.RepositoryComboBoxItem(repo));

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
				if (user.getUsername().equals(server.getUsername())) {
					indexToSelect = index + 1;
				}

//				userListModel.addElement(new UserListItem(user, false));
				index++;
			}
			if (indexToSelect != -1) {
				authorComboBox.setSelectedIndex(indexToSelect);
				moderatorComboBox.setSelectedIndex(indexToSelect);
			}
		}

		updateReviewersList();
		getOKAction().setEnabled(isValidForm());

	}

	private void updateReviewersList() {
		ServerData server = null;
		if (crucibleServersComboBox.getSelectedItem() instanceof ServerComboBoxItem) {
			server = ((ServerComboBoxItem) crucibleServersComboBox.getSelectedItem()).getServer();

		}

		BasicProject selectedProject = null;
		if (projectsComboBox.getSelectedItem() instanceof ProjectComboBoxItem) {
			selectedProject = ((ProjectComboBoxItem) projectsComboBox.getSelectedItem()).getWrappedProject();
		}

		if (selectedProject != null && server != null) {
//			userListModel.removeAllElements();

			final ServerData finalServer = server;
			final BasicProject finalSelectedProject = selectedProject;
//            new Thread(new Runnable() {
//
//                public void run() {
			try {
				final List<User> reviewers = crucibleServerFacade
						.getAllowedReviewers(finalServer, finalSelectedProject.getKey());
//				if (reviewers != null) {
//					for (User user : reviewers) {
//						userListModel.addElement(new UserListItem(user, false));
//					}
//				}
			} catch (RemoteApiException e) {
				DialogWithDetails.showExceptionDialog(project, "Cannot fetch reviewvers from server", e);

			} catch (ServerPasswordNotProvidedException e) {
				DialogWithDetails.showExceptionDialog(project,
						"Incorrect server password for server " + finalServer.getName(), e.getMessage());
			}

//                }
//            }, "refreshing allowed reviewers").start();

		}
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

	@Nullable
	protected Review prepareReview(ServerData server) {
		User author = getSelectedAuthor();
		if (author == null) {
			return null;
		}

		final User creator = new User(server.getUsername());
		final String description = statementArea.getText();
		final String name = titleText.getText();
		final User moderator = getSelectedModerator();
		final String prjKey = getSelectedProjectKey();

		final Review review = new Review(server.getUrl(), prjKey, author, moderator);
		review.setCreator(creator);
		review.setDescription(description);
		review.setName(name);

		return review;
	}


	private String getSelectedAnchorRepoName() {
		if (anchorRepoComboBox.getSelectedItem() instanceof RepositoryComboBoxItem) {
			return ((RepositoryComboBoxItem) anchorRepoComboBox.getSelectedItem()).getRepository().getName();
		} else {
			return "";
		}
	}

	@Nullable
	private User getSelectedAuthor() {
		if (authorComboBox.getSelectedItem() instanceof UserComboBoxItem) {
			return ((UserComboBoxItem) authorComboBox.getSelectedItem()).getUser();
		} else {
			return null;
		}
	}

	@Nullable
	private User getSelectedModerator() {
		if (moderatorComboBox.getSelectedItem() instanceof UserComboBoxItem) {
			return ((UserComboBoxItem) moderatorComboBox.getSelectedItem()).getUser();
		} else {
			return null;
		}
	}

	@NotNull
	private String getSelectedProjectKey() {
		return ((ProjectComboBoxItem) projectsComboBox.getSelectedItem()).getWrappedProject().getKey();
	}

	protected abstract ReviewAdapter createReview(ServerData server, Review reviewBeingConstructed)
			throws RemoteApiException,
			ServerPasswordNotProvidedException, VcsException, IOException, PatchCreateErrorException;

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

			LoggerImpl.getInstance()
					.info("CrucibleReviewCreateForm.runCreateReviewTask() - starting review creation task");

			Task.Backgroundable changesTask = new Task.Backgroundable(project, "Creating review...",
					runUntilSuccessful) {

				public boolean isCancelled = false;

				@Nullable
				private String errorMessage;


				public void run(@NotNull final ProgressIndicator indicator) {

					boolean submissionSuccess = false;
					do {
						LoggerImpl.getInstance().info("runCreateReviewTask.run() - retrying review creation");

						indicator.setText("Attempting to create review... ");
						ModalityState modalityState = ModalityState
								.stateForComponent(CrucibleReviewCreateForm.this.getRootComponent());

						ReviewAdapter newlyCreated = null;
						try {

							LoggerImpl.getInstance().info("runCreateReviewTask.run() - before createReview()");
							Review reviewBeingCreated = prepareReview(server);
							if (reviewBeingCreated == null) {
								errorMessage = "Review not created: " + "Cannot prepare review data";
								return;
							}
							final ReviewAdapter draftReview = createReview(server, reviewBeingCreated);
							if (draftReview == null) {
								errorMessage = "Review not created: createReview returned null";
								return;
							}
							submissionSuccess = true;

//							Set<String> users = new HashSet<String>();
//							for (int i = 0; i < userListModel.getSize(); ++i) {
//								UserListItem item = (UserListItem) userListModel.get(i);
//								if (item.isSelected()) {
//									users.add(item.getUser().getUsername());
//								}
//							}

							LoggerImpl.getInstance().info("runCreateReviewTask.run() - before addReviewers()");

//							if (!users.isEmpty()) {
//								crucibleServerFacade.addReviewers(server, draftReview.getPermId(), users);
//							}

							if (!leaveAsDraftCheckBox.isSelected()) {
								ReviewAdapter newReview = crucibleServerFacade
										.getReview(server, draftReview.getPermId());
								if (newReview.getModerator() != null
										&& newReview.getModerator().getUsername().equals(server.getUsername())) {
									if (newReview.getActions().contains(CrucibleAction.APPROVE)) {
										LoggerImpl.getInstance()
												.info("runCreateReviewTask.run() - before approveReview()");
										Review tmpReview = crucibleServerFacade
												.changeReviewState(draftReview.getServerData(),
														draftReview.getPermId(), CrucibleAction.APPROVE);
										newlyCreated = new ReviewAdapter(tmpReview, draftReview.getServerData(),
												draftReview.getCrucibleProject());


									} else {
										errorMessage = "Permission denied: " + newReview.getAuthor().getDisplayName() +
												" is not authorized to approve review.\n"
												+ "Leaving review in draft state.";
										return;
									}
								} else {
									if (newReview.getActions().contains(CrucibleAction.SUBMIT)) {
										LoggerImpl.getInstance()
												.info("runCreateReviewTask.run() - before submitReview()");
										Review tmpReview = crucibleServerFacade
												.changeReviewState(draftReview.getServerData(),
														draftReview.getPermId(), CrucibleAction.SUBMIT);
										newlyCreated = new ReviewAdapter(tmpReview, draftReview.getServerData(),
												draftReview.getCrucibleProject());
									} else {
										errorMessage = "Permission denied: " + newReview.getAuthor().getDisplayName()
												+ " is not authorized submit review.\n"
												+ "Leaving review in draft state.";
									}
								}
							} else {
								newlyCreated = draftReview;
							}

							LoggerImpl.getInstance().info("runCreateReviewTask.run() - before getReview()");

							final ReviewAdapter newRevewFinal = newlyCreated != null
									? crucibleServerFacade.getReview(server, newlyCreated.getPermId()) : null;

							ApplicationManager.getApplication().invokeLater(new Runnable() {
								public void run() {
									if (newRevewFinal != null) {
										BrowserUtil.launchBrowser(newRevewFinal.getReviewUrl());
									}
								}
							}, modalityState);
						} catch (final Throwable e) {
							if (!runUntilSuccessful) {
								LoggerImpl.getInstance()
										.info("runCreateReviewTask.run() - review creation error - " + e.getMessage());
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
									LoggerImpl.getInstance().info("runCreateReviewTask.run() " +
											"- unknown changeset exception - fisheye does not know this review yet");
									try {
										Date now = new Date();
										if (reviewCreationTimeout > 0
												&& now.getTime() - startDate.getTime() >
												reviewCreationTimeout * MILLISECONDS_IN_MINUTE) {
											LoggerImpl.getInstance()
													.info("runCreateReviewTask.run() - review creation timed out");
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
										LoggerImpl.getInstance()
												.info("runCreateReviewTask.run() - sleeping for 10 seconds");
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
										LoggerImpl.getInstance().info("runCreateReviewTask.run() - sleep interrupted");
									}
								} else {
									LoggerImpl.getInstance().info(
											"runCreateReviewTask.run() - error creating review: " + e.getMessage());
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
							&& (e.getMessage().contains("Specified change set id does not exist") ||
							((e.getMessage().contains(" Change set id") || e.getMessage().contains("Changeset id"))
									&& e.getMessage().contains(" does not exist in repository")));


				}

				@Override
				public void onSuccess() {
					if (errorMessage != null) {
						Messages.showErrorDialog(
								project, errorMessage, PluginUtil.PRODUCT_NAME);

					}
				}

				;
			};
			ProgressManager.getInstance().run(changesTask);
		} else {
			LoggerImpl.getInstance().info("CrucibleReviewCreateForm.runCreateReviewTask() - sselectedItem == null");
		}
	}

	protected boolean isValid(Review review) {
		return true;
	}

	private boolean isValidForm() {
		if (crucibleServersComboBox.getSelectedItem() instanceof ServerComboBoxItem && titleText.getText().length() > 0
				&& projectsComboBox.getSelectedItem() instanceof ProjectComboBoxItem
				&& authorComboBox.getSelectedItem() instanceof UserComboBoxItem
				&& moderatorComboBox.getSelectedItem() instanceof UserComboBoxItem
				&& isAnchorDataValid()) {
			final ServerComboBoxItem selectedItem = (ServerComboBoxItem) crucibleServersComboBox.getSelectedItem();
			return isValid(prepareReview(selectedItem.getServer()));
		} else {
			return false;
		}
	}


}
  
