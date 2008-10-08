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
import com.atlassian.theplugin.commons.VirtualFileSystem;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleReviewItemInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.Project;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.UserBean;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import static com.intellij.openapi.ui.Messages.showMessageDialog;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

enum ReviewCreationMode {
	EMPTY,
	REVISION,
	PATCH
}

public class CrucibleReviewCreateForm extends DialogWrapper {
	private ReviewCreationMode mode;

	private JTextArea patchPreview;
	private JPanel rootComponent;
	private JTextField titleText;
	private JComboBox crucibleServersComboBox;
	private JTextArea statementArea;
	private JCheckBox openBrowserToCompleteCheckBox;
	private JComboBox repoComboBox;
	private JComboBox projectsComboBox;
	private JComboBox authorComboBox;
	private JComboBox moderatorComboBox;
	private JList reviewersList;
	private JCheckBox allowCheckBox;
	private JCheckBox leaveAsDraftCheckBox;
	private JScrollPane patchPanel;
	private JLabel patchLabel;
	private String patchText;
	private DefaultListModel model;
	private UserListCellRenderer cellRenderer = new UserListCellRenderer();

	private com.intellij.openapi.project.Project project;
	private CrucibleServerFacade crucibleServerFacade;
	private ChangeList[] changes;
	private final CfgManager cfgManager;

	public CrucibleReviewCreateForm(com.intellij.openapi.project.Project project, CrucibleServerFacade crucibleServerFacade,
			ChangeList[] changes, @NotNull final CfgManager cfgManager) {
		this(project, crucibleServerFacade, "", cfgManager);
		this.mode = ReviewCreationMode.REVISION;
		this.changes = changes;
		if (changes.length == 1) {
			titleText.setText(changes[0].getName());
		} else {
			titleText.setText("");
		}
		showPatchPanel(false);
		setTitle("Create Review");
	}

	public CrucibleReviewCreateForm(com.intellij.openapi.project.Project project, CrucibleServerFacade crucibleServerFacade,
			String commitMessage,
			String patch, @NotNull final CfgManager cfgManager) {
		this(project, crucibleServerFacade, commitMessage, cfgManager);
		this.mode = ReviewCreationMode.PATCH;
		setPatchPreview(patch);
		showPatchPanel(true);
		setTitle("Create Patch Review");
	}

	private CrucibleReviewCreateForm(com.intellij.openapi.project.Project project, CrucibleServerFacade crucibleServerFacade,
			String commitMessage, @NotNull final CfgManager cfgManager) {
		super(false);
		this.project = project;
		this.crucibleServerFacade = crucibleServerFacade;
		this.cfgManager = cfgManager;
		$$$setupUI$$$();
		init();
		titleText.setText(commitMessage);
		getOKAction().putValue(Action.NAME, "Create review...");
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
		User moderatorUser = ((UserComboBoxItem) (moderatorComboBox.getSelectedItem())).getUser();
		ArrayList<User> disabledUsers = new ArrayList<User>();
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
		getOKAction().setEnabled(isValidForm());
	}

	private void showPatchPanel(boolean visible) {
		this.patchPanel.setVisible(visible);
		this.patchLabel.setVisible(visible);
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
		rootComponent.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		rootComponent.setMinimumSize(new Dimension(760, 505));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(14, 6, new Insets(1, 1, 1, 1), -1, -1));
		rootComponent.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		crucibleServersComboBox = new JComboBox();
		panel1.add(crucibleServersComboBox, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST,
				GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
				null, null, 0, false));
		final JLabel label1 = new JLabel();
		label1.setText("Server:");
		panel1.add(label1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("Statement of Objectives:");
		panel1.add(label2, new GridConstraints(8, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		patchPanel = new JScrollPane();
		panel1.add(patchPanel, new GridConstraints(11, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		patchPreview = new JTextArea();
		patchPreview.setEditable(false);
		patchPreview.setEnabled(true);
		patchPreview.setFont(new Font("Monospaced", patchPreview.getFont().getStyle(), patchPreview.getFont().getSize()));
		patchPreview.setLineWrap(true);
		patchPreview.setRows(5);
		patchPreview.setText("");
		patchPanel.setViewportView(patchPreview);
		final JScrollPane scrollPane1 = new JScrollPane();
		panel1.add(scrollPane1, new GridConstraints(9, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		statementArea = new JTextArea();
		statementArea.setLineWrap(true);
		statementArea.setRows(5);
		scrollPane1.setViewportView(statementArea);
		patchLabel = new JLabel();
		patchLabel.setText("Patch:");
		panel1.add(patchLabel, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		openBrowserToCompleteCheckBox = new JCheckBox();
		openBrowserToCompleteCheckBox.setSelected(false);
		openBrowserToCompleteCheckBox.setText("Open browser to complete review creation");
		panel1.add(openBrowserToCompleteCheckBox, new GridConstraints(12, 0, 1, 4, GridConstraints.ANCHOR_WEST,
				GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label3 = new JLabel();
		label3.setText("Title:");
		panel1.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label4 = new JLabel();
		label4.setText("Moderator:");
		panel1.add(label4, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		moderatorComboBox = new JComboBox();
		panel1.add(moderatorComboBox, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST,
				GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
				null, null, 0, false));
		final JLabel label5 = new JLabel();
		label5.setInheritsPopupMenu(false);
		label5.setText("Project:");
		panel1.add(label5, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		projectsComboBox = new JComboBox();
		panel1.add(projectsComboBox, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST,
				GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null,
				null, null, 0, false));
		final JLabel label6 = new JLabel();
		label6.setText("Repository:");
		panel1.add(label6, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		repoComboBox = new JComboBox();
		panel1.add(repoComboBox, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label7 = new JLabel();
		label7.setText("Author:");
		panel1.add(label7, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		authorComboBox = new JComboBox();
		panel1.add(authorComboBox, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		titleText = new JTextField();
		panel1.add(titleText, new GridConstraints(1, 0, 1, 6, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
				GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
				false));
		final JScrollPane scrollPane2 = new JScrollPane();
		panel1.add(scrollPane2, new GridConstraints(2, 3, 5, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, 1,
				null, null, null, 0, false));
		scrollPane2.setViewportView(reviewersList);
		final JLabel label8 = new JLabel();
		label8.setText("Reviewers");
		panel1.add(label8, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		allowCheckBox = new JCheckBox();
		allowCheckBox.setEnabled(true);
		allowCheckBox.setText("Allow anyone to join");
		panel1.add(allowCheckBox, new GridConstraints(7, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
				GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
				null, null, null, 0, false));
		leaveAsDraftCheckBox = new JCheckBox();
		leaveAsDraftCheckBox.setText("Save review as Draft");
		panel1.add(leaveAsDraftCheckBox, new GridConstraints(13, 0, 1, 3, GridConstraints.ANCHOR_WEST,
				GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
				GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		label1.setLabelFor(crucibleServersComboBox);
		label2.setLabelFor(statementArea);
		patchLabel.setLabelFor(patchPreview);
		label3.setLabelFor(titleText);
		label7.setLabelFor(scrollPane2);
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
		private final Project wrappedProject;

		private ProjectComboBoxItem(@NotNull final Project project) {
			this.wrappedProject = project;
		}

		@Override
		public String toString() {
			return wrappedProject.getName();
		}

		public Project getWrappedProject() {
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
		final Collection<CrucibleServerCfg> enabledServers
				= cfgManager.getAllEnabledCrucibleServers(CfgUtil.getProjectId(project));
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

		new Thread(new Runnable() {
			public void run() {
				List<Project> projects = new ArrayList<Project>();
				List<Repository> repositories = new ArrayList<Repository>();
				List<User> users = new ArrayList<User>();

				try {
					projects = crucibleServerFacade.getProjects(server);
					repositories = crucibleServerFacade.getRepositories(server);
					users = crucibleServerFacade.getUsers(server);
				} catch (RemoteApiException e) {
					// nothing can be done here
				} catch (ServerPasswordNotProvidedException e) {
					// nothing can be done here
				}
				final List<Project> finalProjects = projects;
				final List<Repository> finalRepositories = repositories;
				final List<User> finalUsers = users;
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						updateServerRelatedCombos(server, finalProjects, finalRepositories, finalUsers);
					}
				});
			}
		}, "atlassian-idea-plugin crucible patch upload combos refresh").start();
	}

	private void updateServerRelatedCombos(CrucibleServerCfg server, List<Project> projects, List<Repository> repositories,
			List<User> users) {

		ProjectConfiguration prjCfg = cfgManager.getProjectConfiguration(CfgUtil.getProjectId(project));
		if (projects.isEmpty()) {
			projectsComboBox.setEnabled(false);
			projectsComboBox.addItem("No projects");
			getOKAction().setEnabled(false);
		} else {
			for (Project myProject : projects) {
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
		if (!repositories.isEmpty()) {
			for (Repository repo : repositories) {
				repoComboBox.addItem(new RepositoryComboBoxItem(repo));
			}

			// setting default repo if such is defined

			if (prjCfg != null) {
				final String defaultRepo = prjCfg.getDefaultCrucibleProject();
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
		if (this.mode == ReviewCreationMode.REVISION
				&& repositories.size() == 1) {
			repoComboBox.setSelectedIndex(repoComboBox.getItemCount() - 1);
		}
		authorComboBox.addItem("");
		moderatorComboBox.addItem("");
		if (!users.isEmpty()) {
			int indexToSelect = -1;
			int index = 0;
			for (User user : users) {
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

	public JComponent getRootComponent() {
		return rootComponent;
	}

	public void setPatchPreview(String preview) {
		this.patchText = preview;
		patchPreview.setText(preview);
	}

	@Override
	@Nullable
	protected JComponent createCenterPanel() {
		return getRootComponent();
	}

	private class ReviewProvider implements Review {
		private final CrucibleServerCfg server;

		public ReviewProvider(CrucibleServerCfg server) {
			this.server = server;
		}

		public User getAuthor() {
			if (authorComboBox.getSelectedItem() instanceof UserComboBoxItem) {
				return ((UserComboBoxItem) authorComboBox.getSelectedItem()).getUser();
			} else {
				return null;
			}
		}

		public User getCreator() {
			UserBean user = new UserBean();
			user.setUserName(server.getUsername());
			return user;
		}

		public String getDescription() {
			return statementArea.getText();
		}

		public User getModerator() {
			if (moderatorComboBox.getSelectedItem() instanceof UserComboBoxItem) {
				return ((UserComboBoxItem) moderatorComboBox.getSelectedItem()).getUser();
			} else {
				return null;
			}
		}

		public String getName() {
			return titleText.getText();
		}

		public PermId getParentReview() {
			return null;
		}

		public PermId getPermId() {
			return null;
		}

		public String getProjectKey() {
			return ((ProjectComboBoxItem) projectsComboBox.getSelectedItem()).getWrappedProject().getKey();
		}

		public String getRepoName() {
			if (repoComboBox.getSelectedItem() instanceof RepositoryComboBoxItem) {
				return ((RepositoryComboBoxItem) repoComboBox.getSelectedItem()).getRepository().getName();
			} else {
				return null;
			}
		}

		public State getState() {
			return null;
		}

		public boolean isAllowReviewerToJoin() {
			return allowCheckBox.isSelected();
		}

		public int getMetricsVersion() {
			return 0;
		}

		public Date getCreateDate() {
			return null;
		}

		public Date getCloseDate() {
			return null;
		}

		public String getSummary() {
			return null;
		}

		public List<Reviewer> getReviewers() throws ValueNotYetInitialized {
			return null;
		}

		public List<GeneralComment> getGeneralComments() throws ValueNotYetInitialized {
			return null;
		}

		public List<VersionedComment> getVersionedComments() throws ValueNotYetInitialized {
			return null;
		}

		public List<CrucibleFileInfo> getFiles() throws ValueNotYetInitialized {
			return null;
		}

		public List<com.atlassian.theplugin.commons.crucible.api.model.Action> getTransitions() throws ValueNotYetInitialized {
			return null;
		}

		public List<com.atlassian.theplugin.commons.crucible.api.model.Action> getActions() throws ValueNotYetInitialized {
			return null;
		}

		public VirtualFileSystem getVirtualFileSystem() {
			return null;
		}

		public CrucibleServerCfg getServer() {
			return null;
		}

		public String getReviewUrl() {
			return null;
		}

		public Review getInnerReviewObject() {
			return null;
		}

		public List<CrucibleReviewItemInfo> getReviewItems() {
			return null;
		}

		public CrucibleFileInfo getFileByPermId(PermId id) {
			return null;
		}

		public CrucibleFileInfo getFileByReviewInfo(CrucibleReviewItemInfo info) {
			return null;
		}

		public String getServerUrl() {
			return null;
		}
	}

	@Override
	protected void doOKAction() {

		final ServerComboBoxItem selectedItem = (ServerComboBoxItem) crucibleServersComboBox.getSelectedItem();

		if (selectedItem != null) {
			final CrucibleServerCfg server = selectedItem.getServer();
			Review review = new ReviewProvider(server);

			try {
				Review draftReview = null;
				switch (mode) {
					case PATCH:
						draftReview =
								crucibleServerFacade.createReviewFromPatch(
										server, review, patchText);
						break;
					case REVISION:
						if (mode == ReviewCreationMode.REVISION
								&& review.getRepoName() == null) {
							Messages.showErrorDialog(project,
									"Repository not selected. Unable to create review.\n"
									, "Repository required");
							return;
						}
						List<String> revisions = new ArrayList<String>();
						for (ChangeList change : changes) {
							for (Change change1 : change.getChanges()) {
								if (change1.getFileStatus().equals(FileStatus.DELETED)) {
									revisions.add(change1.getBeforeRevision().getRevisionNumber().asString());
								} else {
									revisions.add(change1.getAfterRevision().getRevisionNumber().asString());
								}
								break;
							}
						}
						draftReview =
								crucibleServerFacade.createReviewFromRevision(
										server, review, revisions);
						break;
					case EMPTY:
						break;
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
										newReview.getAuthor().getDisplayName() + " is authorized to approve review.\n" +
												"Leaving review in draft state."
										, "Permission denied");
							}
						} else {
							if (newReview.getActions()
									.contains(com.atlassian.theplugin.commons.crucible.api.model.Action.SUBMIT)) {
								crucibleServerFacade.submitReview(server, draftReview.getPermId());
							} else {
								Messages.showErrorDialog(project,
										newReview.getAuthor().getDisplayName() + " is authorized submit review.\n" +
												"Leaving review in draft state."
										, "Permission denied");
							}
						}
					} catch (ValueNotYetInitialized valueNotYetInitialized) {
						Messages.showErrorDialog(project,
								"Unable to change review state. Leaving review in draft state."
								, "Permission denied");
					}
				}
				if (openBrowserToCompleteCheckBox.isSelected()) {
					BrowserUtil.launchBrowser(server.getUrl()
							+ "/cru/"
							+ draftReview.getPermId().getId());
				}

				super.doOKAction();
			} catch (RemoteApiException e) {
				showMessageDialog(e.getMessage(),
						"Error creating review: " + server.getUrl(), Messages.getErrorIcon());
			} catch (ServerPasswordNotProvidedException e) {
				showMessageDialog(e.getMessage(), "Error creating review: " + server.getUrl(), Messages.getErrorIcon());
			}
		}
	}

	private boolean isValidForm() {

		if (crucibleServersComboBox.getSelectedItem() instanceof ServerComboBoxItem
				&& titleText.getText().length() > 0

				&& projectsComboBox.getSelectedItem() instanceof ProjectComboBoxItem
				&& authorComboBox.getSelectedItem() instanceof UserComboBoxItem
				&& moderatorComboBox.getSelectedItem() instanceof UserComboBoxItem) {
			if (mode != ReviewCreationMode.PATCH) {
				if (repoComboBox.getSelectedItem() instanceof RepositoryComboBoxItem) {
					return true;
				} else {
					return false;
				}
			}
			return true;
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

	private boolean isReviewerSelected() {
		for (int i = 0; i < model.size(); i++) {
			if (((UserListItem) model.get(i)).isSelected()) {
				return true;
			}
		}

		return false;

	}
}
  
