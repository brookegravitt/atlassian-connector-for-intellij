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

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.configuration.ProductServerConfiguration;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.crucible.api.*;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import static com.intellij.openapi.ui.Messages.showMessageDialog;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CruciblePatchUploadForm extends DialogWrapper {
	private JTextArea patchPreview;
	private JPanel rootComponent;
	private JTextField titleText;
	private JComboBox crucibleServersComboBox;
	private JTextArea statementArea;
	private JCheckBox openBrowserToCompleteCheckBox;
	private JComboBox repoComboBox;
	private JComboBox projectsComboBox;
	private String patchText;

	private CrucibleServerFacade crucibleServerFacade;

	protected CruciblePatchUploadForm(CrucibleServerFacade crucibleServerFacade, String commitMessage) {
		super(false);
		this.crucibleServerFacade = crucibleServerFacade;
		$$$setupUI$$$();
		init();
		titleText.setText(commitMessage);
		getOKAction().putValue(Action.NAME, "Create review...");
		crucibleServersComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (crucibleServersComboBox.getItemCount() > 0 && crucibleServersComboBox.getSelectedItem() != null && crucibleServersComboBox.getSelectedItem() instanceof ServerComboBoxItem) {
					fillServerRelatedCombos(((ServerComboBoxItem) crucibleServersComboBox.getSelectedItem()).getServer());
				}
			}
		});
		fillInCrucibleServers();
	}

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
		rootComponent.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		rootComponent.setMinimumSize(new Dimension(760, 505));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new GridLayoutManager(7, 6, new Insets(1, 1, 1, 1), -1, -1));
		rootComponent.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		crucibleServersComboBox = new JComboBox();
		panel1.add(crucibleServersComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		titleText = new JTextField();
		panel1.add(titleText, new GridConstraints(1, 1, 1, 5, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label1 = new JLabel();
		label1.setText("Server:");
		panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setInheritsPopupMenu(false);
		label2.setText("Project Key:");
		panel1.add(label2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label3 = new JLabel();
		label3.setText("Title:");
		panel1.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label4 = new JLabel();
		label4.setText("Statement of Objectives:");
		panel1.add(label4, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JScrollPane scrollPane1 = new JScrollPane();
		panel1.add(scrollPane1, new GridConstraints(5, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		patchPreview = new JTextArea();
		patchPreview.setEditable(false);
		patchPreview.setEnabled(true);
		patchPreview.setFont(new Font("Monospaced", patchPreview.getFont().getStyle(), patchPreview.getFont().getSize()));
		patchPreview.setLineWrap(true);
		patchPreview.setRows(5);
		patchPreview.setText("");
		scrollPane1.setViewportView(patchPreview);
		final JScrollPane scrollPane2 = new JScrollPane();
		panel1.add(scrollPane2, new GridConstraints(3, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
		statementArea = new JTextArea();
		statementArea.setLineWrap(true);
		statementArea.setRows(5);
		scrollPane2.setViewportView(statementArea);
		final JLabel label5 = new JLabel();
		label5.setText("Patch:");
		panel1.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		openBrowserToCompleteCheckBox = new JCheckBox();
		openBrowserToCompleteCheckBox.setSelected(true);
		openBrowserToCompleteCheckBox.setText("Open browser to complete review creation");
		panel1.add(openBrowserToCompleteCheckBox, new GridConstraints(6, 0, 1, 4, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label6 = new JLabel();
		label6.setText("Repository Name:");
		panel1.add(label6, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		repoComboBox = new JComboBox();
		panel1.add(repoComboBox, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		projectsComboBox = new JComboBox();
		panel1.add(projectsComboBox, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		label1.setLabelFor(crucibleServersComboBox);
		label3.setLabelFor(titleText);
		label4.setLabelFor(statementArea);
		label5.setLabelFor(patchPreview);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return rootComponent;
	}

	private static final class ServerComboBoxItem {
		private final Server server;

		private ServerComboBoxItem(Server server) {
			this.server = server;
		}

		public String toString() {
			return server.getName();
		}

		public Server getServer() {
			return server;
		}
	}

	private static final class ProjectComboBoxItem {
		private final ProjectData project;

		private ProjectComboBoxItem(ProjectData project) {
			this.project = project;
		}

		public String toString() {
			return project.getKey();
		}

		public ProjectData getProject() {
			return project;
		}
	}

	private static final class RepositoryComboBoxItem {
		private final RepositoryData repo;

		private RepositoryComboBoxItem(RepositoryData repo) {
			this.repo = repo;
		}

		public String toString() {
			return repo.getName();
		}

		public RepositoryData getRepository() {
			return repo;
		}
	}

	private void fillInCrucibleServers() {
		ProductServerConfiguration crucibleConfiguration =
				ConfigurationFactory.getConfiguration().getProductServers(ServerType.CRUCIBLE_SERVER);

		Collection<Server> enabledServers = crucibleConfiguration.transientgetEnabledServers();
		if (enabledServers.isEmpty()) {
			crucibleServersComboBox.setEnabled(false);
			crucibleServersComboBox.addItem("Enable a Crucible server first!");
			getOKAction().setEnabled(false);
		} else {
			for (Server server : enabledServers) {
				crucibleServersComboBox.addItem(new ServerComboBoxItem(server));
			}
		}
	}

	private void fillServerRelatedCombos(final Server server) {
		projectsComboBox.removeAllItems();
		repoComboBox.removeAllItems();
		getOKAction().setEnabled(false);

		new Thread(new Runnable() {
			public void run() {
				List<ProjectData> projects = new ArrayList<ProjectData>();
				List<RepositoryData> repositories = new ArrayList<RepositoryData>();
				try {
					projects = crucibleServerFacade.getProjects(server);
					repositories = crucibleServerFacade.getRepositories(server);
				} catch (RemoteApiException e) {
					// nothing can be done here
				} catch (ServerPasswordNotProvidedException e) {
					// nothing can be done here
				}
				final List<ProjectData> finalProjects = projects;
				final List<RepositoryData> finalRepositories = repositories;
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						updateServerRelatedCombos(finalProjects, finalRepositories);
					}
				});
			}
		}, "atlassian-idea-plugin crucible patch upload combos refresh").start();


	}

	private void updateServerRelatedCombos(List<ProjectData> projects, List<RepositoryData> repositories) {
		if (projects.isEmpty()) {
			projectsComboBox.setEnabled(false);
			projectsComboBox.addItem("No projects");
			getOKAction().setEnabled(false);
		} else {
			for (ProjectData project : projects) {
				projectsComboBox.addItem(new ProjectComboBoxItem(project));
			}
			getOKAction().setEnabled(true);
		}
		repoComboBox.addItem("");
		if (!repositories.isEmpty()) {
			for (RepositoryData repo : repositories) {
				repoComboBox.addItem(new RepositoryComboBoxItem(repo));
			}
			getOKAction().setEnabled(true);
		}
	}

	public JComponent getRootComponent() {
		return rootComponent;
	}

	public void setPatchPreview(String preview) {
		this.patchText = preview;
		patchPreview.setText(preview);
	}

	@Nullable
	protected JComponent createCenterPanel() {
		return getRootComponent();
	}

	private class ReviewDataProvider implements ReviewData {
		private final Server server;

		public ReviewDataProvider(Server server) {
			this.server = server;
		}

		public String getAuthor() {
			return server.getUserName();
		}

		public String getCreator() {
			return server.getUserName();
		}

		public String getDescription() {
			return statementArea.getText();
		}

		public String getModerator() {
			return null;
		}

		public String getName() {
			return titleText.getText();
		}

		public PermId getParentReview() {
			return null;
		}

		public PermId getPermaId() {
			return null;
		}

		public String getProjectKey() {
			return ((ProjectComboBoxItem) projectsComboBox.getSelectedItem()).getProject().getKey();
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

	}

	protected void doOKAction() {
		final ServerComboBoxItem selectedItem = (ServerComboBoxItem) crucibleServersComboBox.getSelectedItem();

		if (selectedItem != null) {
			final Server server = selectedItem.getServer();
			ReviewData reviewData = new ReviewDataProvider(server);

			try {
				ReviewData draftReviewData =
						crucibleServerFacade.createReviewFromPatch(
								server, reviewData, patchText);
				if (openBrowserToCompleteCheckBox.isSelected()) {
					BrowserUtil.launchBrowser(server.getUrlString()
							+ "/cru/"
							+ draftReviewData.getPermaId().getId());
				}
				super.doOKAction();
			} catch (RemoteApiException e) {
				showMessageDialog(e.getMessage(),
						"Error creating review: " + server.getUrlString(), Messages.getErrorIcon());
			} catch (ServerPasswordNotProvidedException e) {
				showMessageDialog(e.getMessage(), "Error creating review: " + server.getUrlString(), Messages.getErrorIcon());
			}


		}
	}

//	private boolean isValid() {
//
//		if (crucibleServersComboBox.getSelectedItem() == null ||
//				patchPreview.getText().length() == 0 ||
//				titleText.getText().length() == 0 ||
//				projectKeyText.getText().length() == 0 ||
//				statementArea.getText().length() == 0) {
//			return false;
//		}
//
//		return true;
//
//	}

	private void createUIComponents() {
		// TODO: place custom component creation code here
	}


}

