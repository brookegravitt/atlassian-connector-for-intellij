package com.atlassian.theplugin.idea.crucible;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.commons.configuration.ServerBean;
import com.atlassian.theplugin.commons.configuration.ProductServerConfiguration;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.*;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: May 29, 2008
 * Time: 9:11:43 AM
 * To change this template use File | Settings | File Templates.
 */
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
	private CrucibleServerFacade crucibleServerFacade;

    private ProjectDataBean anyProject;
    private UserDataBean anyUser;

    CrucibleCustomFilterPanel() {
        anyProject = new ProjectDataBean();
        anyProject.setName("Any");
        anyUser = new UserDataBean();
        anyUser.setDisplayName("Any");

        crucibleServerFacade = CrucibleServerFacadeImpl.getInstance();
		serverComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (serverComboBox.getItemCount() > 0 && serverComboBox.getSelectedItem() != null && serverComboBox.getSelectedItem() instanceof ServerComboBoxItem) {
					fillServerRelatedCombos(((ServerComboBoxItem) serverComboBox.getSelectedItem()).getServer());
				}
			}
		});
	}

	public void fillinFilter() {
		fillInCrucibleServers();
	}

	private void fillServerRelatedCombos(final Server server) {
		projectComboBox.removeAllItems();
        projectComboBox.addItem(new ProjectComboBoxItem(anyProject));
        authorComboBox.removeAllItems();
        authorComboBox.addItem(new UserComboBoxItem(anyUser));
        moderatorComboBox.removeAllItems();
        moderatorComboBox.addItem(new UserComboBoxItem(anyUser));
        creatorComboBox.removeAllItems();
        creatorComboBox.addItem(new UserComboBoxItem(anyUser));
        reviewerStatusComboBox.removeAllItems();
		reviewerComboBox.removeAllItems();
        reviewerComboBox.addItem(new UserComboBoxItem(anyUser));


        new Thread(new Runnable() {
			public void run() {
				List<ProjectData> projects = new ArrayList<ProjectData>();
                List<UserData> users = new ArrayList<UserData>();
				try {
					projects = crucibleServerFacade.getProjects(server);
                    users = crucibleServerFacade.getUsers(server);
                } catch (RemoteApiException e) {
					// nothing can be done here
				} catch (ServerPasswordNotProvidedException e) {
					// nothing can be done here
				}
				final List<ProjectData> finalProjects = projects;
                final List<UserData> finalUsers = users;


                EventQueue.invokeLater(new Runnable() {
					public void run() {
						updateServerRelatedCombos(finalProjects, finalUsers);
					}
				});
			}
		}, "atlassian-idea-plugin crucible custom filter upload combos refresh").start();


	}

	private void updateServerRelatedCombos(List<ProjectData> projects, List<UserData> users) {
		if (projects.isEmpty()) {
			projectComboBox.setEnabled(false);
			projectComboBox.addItem("No projects");
			setEnabledApplyButton(false);
		} else {
			for (ProjectData project : projects) {
				projectComboBox.addItem(new ProjectComboBoxItem(project));
			}
			setEnabledApplyButton(true);
		}

		if (!users.isEmpty()) {
            for (UserData user : users) {
                authorComboBox.addItem(new UserComboBoxItem(user));
                moderatorComboBox.addItem(new UserComboBoxItem(user));
                creatorComboBox.addItem(new UserComboBoxItem(user));
                reviewerComboBox.addItem(new UserComboBoxItem(user));
            }
		}                 
    }

	private void setEnabledApplyButton(Boolean enabled) {
		ActionManager.getInstance().getAction("ThePlugin.Crucible.ShowFilter").getTemplatePresentation().setEnabled(enabled);
	}

	private void fillInCrucibleServers() {
		ProductServerConfiguration crucibleConfiguration =
				ConfigurationFactory.getConfiguration().getProductServers(ServerType.CRUCIBLE_SERVER);

		Collection<Server> enabledServers = crucibleConfiguration.transientgetEnabledServers();

		serverComboBox.removeAllItems();
		if (enabledServers.isEmpty()) {
			serverComboBox.setEnabled(false);
			serverComboBox.addItem("Enable a Crucible server first!");
			//@todo disable apply filter button in toolbar
		} else {
			for (Server server : enabledServers) {
				serverComboBox.addItem(new ServerComboBoxItem(server));
			}
		}


	}

	{
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
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
		rootPanel.setLayout(new FormLayout("fill:p:grow,fill:max(d;4px):noGrow", "top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:19px:noGrow,center:max(d;4px):noGrow,center:max(d;4px):noGrow,center:33px:noGrow,center:18px:noGrow,center:31px:noGrow,center:19px:noGrow,center:30px:noGrow,center:19px:noGrow,center:30px:noGrow,center:17px:noGrow,center:max(d;4px):noGrow,top:3dlu:noGrow,center:24px:noGrow,center:23px:noGrow,center:25px:noGrow,center:max(d;4px):noGrow,center:25px:noGrow,center:24px:noGrow,center:24px:noGrow,center:max(d;4px):noGrow"));
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
		rootPanel.add(draftCheckBox, cc.xy(1, 22));
		pendingApprovalCheckBox = new JCheckBox();
		pendingApprovalCheckBox.setText("Pending Approval");
		rootPanel.add(pendingApprovalCheckBox, cc.xy(1, 23));
		underReviewCheckBox = new JCheckBox();
		underReviewCheckBox.setText("Under Review");
		rootPanel.add(underReviewCheckBox, cc.xy(1, 24));
		summarizeCheckBox = new JCheckBox();
		summarizeCheckBox.setText("Summarize");
		rootPanel.add(summarizeCheckBox, cc.xy(1, 25));
		closedCheckBox = new JCheckBox();
		closedCheckBox.setText("Closed");
		rootPanel.add(closedCheckBox, cc.xy(1, 26));
		abandonedCheckBox = new JCheckBox();
		abandonedCheckBox.setText("Abandoned");
		rootPanel.add(abandonedCheckBox, cc.xy(1, 27));
		rejectedCheckBox = new JCheckBox();
		rejectedCheckBox.setText("Rejected");
		rootPanel.add(rejectedCheckBox, cc.xy(1, 28));
		reviewNeedsFixingCheckBox = new JCheckBox();
		reviewNeedsFixingCheckBox.setText("Review needs fixing");
		rootPanel.add(reviewNeedsFixingCheckBox, cc.xy(1, 29));
		serverComboBox = new JComboBox();
		rootPanel.add(serverComboBox, cc.xy(1, 4));
		final JLabel label8 = new JLabel();
		label8.setText("Crucible server:");
		rootPanel.add(label8, cc.xy(1, 2));
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
			return project.getName();
		}

		public ProjectData getProject() {
			return project;
		}
	}


    private class UserComboBoxItem extends Object {
        private final UserData user;

        public UserComboBoxItem(UserData user) {
            this.user = user;
        }

        public String toString() {
            return user.getDisplayName();
        }

        public UserData getUserData() {
            return user;
        }
    }
}

