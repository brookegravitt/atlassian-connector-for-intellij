package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.configuration.CrucibleViewConfigurationBean;
import com.atlassian.theplugin.idea.ui.KeyPressGobbler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class SearchReviewDialog extends DialogWrapper {
	private JPanel rootPane;
	private JTextField ctrlReviewSearch;
	private JPanel ctrlServersPanel;
	private JLabel ctrlLocalInfoLabel;
	private Collection<CrucibleServerCfg> selectedServers = new HashSet<CrucibleServerCfg>();
	private CrucibleViewConfigurationBean crucibleViewConfiguration;

	public Collection<CrucibleServerCfg> getSelectedServers() {
		return selectedServers;
	}

	public SearchReviewDialog(Project project, final Collection<CrucibleServerCfg> servers,
			final CrucibleViewConfigurationBean crucibleViewConfiguration) {
		super(project, true);
		this.crucibleViewConfiguration = crucibleViewConfiguration;

		$$$setupUI$$$();
		init();
		pack();

		setTitle("Search Review");
		getOKAction().putValue(Action.NAME, "Search");
		getOKAction().setEnabled(false);

		addServersCheckboxes(servers);
		if (selectedServers.size() != 0) {
			ctrlLocalInfoLabel.setVisible(false);
		}

		ctrlReviewSearch.addKeyListener(new KeyAdapter() {
			public void keyReleased(final KeyEvent e) {
				if (ctrlReviewSearch.getText().length() == 0) {
					getOKAction().setEnabled(false);
				} else {
					getOKAction().setEnabled(true);
				}
			}
		});
		KeyPressGobbler.gobbleKeyPress(ctrlReviewSearch);
		setOKActionEnabled(false);
	}

	protected void doOKAction() {

		if (crucibleViewConfiguration != null) {

			Collection<String> searchServers = new ArrayList<String>();

			for (CrucibleServerCfg server : selectedServers) {
				searchServers.add(server.getServerId().toString());
			}
			crucibleViewConfiguration.setSearchServers(searchServers);
		}

		super.doOKAction();
//		close(OK_EXIT_CODE);
	}

	public JComponent getPreferredFocusedComponent() {
		return ctrlReviewSearch;
	}

	private void addServersCheckboxes(final Collection<CrucibleServerCfg> servers) {

		ctrlServersPanel.setLayout(new BoxLayout(ctrlServersPanel, BoxLayout.Y_AXIS));

		if (servers != null) {
			for (final CrucibleServerCfg server : servers) {
				final CrucibleServerCheckbox checkbox = new CrucibleServerCheckbox(server);
				ctrlServersPanel.add(checkbox);

				if (crucibleViewConfiguration != null && crucibleViewConfiguration.getSearchServers() != null
						&& crucibleViewConfiguration.getSearchServers().contains(server.getServerId().toString())) {
					checkbox.setSelected(true);
					selectedServers.add(server);
				}

				checkbox.addActionListener(new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						if (checkbox.isSelected()) {
							selectedServers.add(server);
							ctrlLocalInfoLabel.setVisible(false);
						} else {
							selectedServers.remove(server);
							if (selectedServers.size() == 0) {
								ctrlLocalInfoLabel.setVisible(true);
								pack();
							}
						}
					}
				});
			}
		}
	}

	@Nullable
	protected JComponent createCenterPanel() {
		return getRootComponent();
	}

	private JComponent getRootComponent() {
		return rootPane;
	}

	public String getSearchKey() {
		return ctrlReviewSearch.getText();
	}


    private static class CrucibleServerCheckbox extends JCheckBox {
		private CrucibleServerCfg server;

		public CrucibleServerCheckbox(CrucibleServerCfg server) {
			super(server.getName());
			this.server = server;
		}

		public CrucibleServerCfg getServer() {
			return server;
		}
	}
}
