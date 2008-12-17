package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.configuration.JiraFilterConfigurationBean;
import com.atlassian.theplugin.configuration.JiraFilterEntryBean;
import com.atlassian.theplugin.configuration.JiraProjectConfiguration;
import com.atlassian.theplugin.idea.ui.SwingAppRunner;
import com.atlassian.theplugin.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.jira.api.JIRASavedFilter;
import com.atlassian.theplugin.jira.model.FrozenModel;
import com.atlassian.theplugin.jira.model.FrozenModelListener;
import com.atlassian.theplugin.jira.model.JIRAFilterListModel;
import com.atlassian.theplugin.jira.model.JIRAFilterListModelListener;
import com.atlassian.theplugin.jira.model.JIRAManualFilter;
import com.atlassian.theplugin.jira.model.JIRAServerModel;
import com.intellij.openapi.project.Project;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * User: pmaruszak
 */
public class JiraManualFilterDetailsPanel extends JPanel {
	private JIRAFilterListModel listModel;
	private final JPanel manualFilterDetailsPanel = new JPanel();
	private final Project project;
	private final JiraProjectConfiguration jiraProjectCfg;
	private final JIRAServerModel jiraServerModel;
	private final JButton editButton = new JButton("Edit");
	private JScrollPane scrollPane;

	JiraManualFilterDetailsPanel(JIRAFilterListModel listModel, JiraProjectConfiguration jiraProjectCfg, Project project,
						  JIRAServerModel jiraServerModel) {
		super(new BorderLayout());
		this.jiraProjectCfg = jiraProjectCfg;
		this.listModel = listModel;
		this.project = project;
		this.jiraServerModel = jiraServerModel;
		createPanelContent();

		listModel.addModelListener(new JIRAFilterListModelListener() {

			public void modelChanged(JIRAFilterListModel aListModel) {
				JiraManualFilterDetailsPanel.this.listModel = aListModel;
			}

			public void selectedSavedFilter(JiraServerCfg jiraServer, JIRASavedFilter savedFilter, boolean isChanged) {
			}

			public void selectedManualFilter(JiraServerCfg jiraServer, java.util.List<JIRAQueryFragment> manualFilter,
											 boolean isChanged) {
			
			}

		});

		listModel.addFrozenModelListener(new FrozenModelListener() {
			public void modelFrozen(FrozenModel model, boolean frozen) {
						setEnabled(!frozen);
				editButton.setEnabled(!frozen);
			}
		});
		
	}


	private void createPanelContent() {

		scrollPane = new JScrollPane(manualFilterDetailsPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		scrollPane.getViewport().setBackground(manualFilterDetailsPanel.getBackground());

		scrollPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				manualFilterDetailsPanel.setPreferredSize(null);
				manualFilterDetailsPanel.setPreferredSize(new Dimension(e.getComponent().getWidth(),
						manualFilterDetailsPanel.getPreferredSize().height));
			}
		});


		scrollPane.setWheelScrollingEnabled(true);

		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		buttonPanel.add(editButton);

		this.add(buttonPanel, BorderLayout.SOUTH);
		this.add(scrollPane, BorderLayout.CENTER);
		final TitledBorder border = BorderFactory.createTitledBorder("Custom Filter");

		this.setBorder(border);
		editButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				JiraServerCfg jiraServer = listModel.getJiraSelectedServer();
				final JiraIssuesFilterPanel jiraIssuesFilterPanel
						= new JiraIssuesFilterPanel(project, jiraServerModel, listModel, jiraServer);

				if (jiraServer != null && listModel.getJiraSelectedManualFilter() != null) {
					final java.util.List<JIRAQueryFragment> listClone = new ArrayList<JIRAQueryFragment>();
					for (JIRAQueryFragment fragment :  listModel.getJiraSelectedManualFilter().getQueryFragment()) {
						listClone.add(fragment.getClone());
					}
					jiraIssuesFilterPanel.setFilter(listClone);
				}
				jiraIssuesFilterPanel.show();

				if (jiraIssuesFilterPanel.getExitCode() == 0) {
					JIRAManualFilter manualFilter = listModel.getJiraSelectedManualFilter();
					listModel.clearManualFilter(jiraServer);
					manualFilter.getQueryFragment().addAll(jiraIssuesFilterPanel.getFilter());
					listModel.setManualFilter(jiraServer, manualFilter);
					listModel.selectManualFilter(jiraServer, manualFilter, true);
					// store filter in project workspace
					jiraProjectCfg.getJiraFilterConfiguaration(listModel.getJiraSelectedServer().getServerId().toString())
							.setManualFilterForName(JiraFilterConfigurationBean.MANUAL_FILTER_LABEL,
									serializeFilter(jiraIssuesFilterPanel.getFilter()));
				}

			}
		});
	}

	public void setFilter(JIRAManualFilter jiraManualFilter) {
		manualFilterDetailsPanel.removeAll();
		if (jiraManualFilter == null) {
			manualFilterDetailsPanel.add(new JLabel("no filter defined"));
			return;
		}

		FormLayout layout = new FormLayout("4dlu, right:p, 4dlu, left:d, 4dlu");
		PanelBuilder builder = new PanelBuilder(layout, manualFilterDetailsPanel);

		final Map<JIRAManualFilter.QueryElement, ArrayList<String>> map = jiraManualFilter.groupBy();

		int row = 1;
		CellConstraints cc = new CellConstraints();
		for (JIRAManualFilter.QueryElement element : map.keySet()) {
			builder.appendRow("2dlu");
			builder.appendRow("p");
			cc.xy(2, row * 2).vAlign = CellConstraints.TOP;
			builder.addLabel(element.getName() + ":", cc);
			StringBuilder right = new StringBuilder();

			for (Iterator<String> it = map.get(element).iterator(); it.hasNext(); ) {
				right.append(it.next());
				if (it.hasNext()) {
					right.append(", ");
				}
			}
			builder.addLabel("<html>" + right.toString(), cc.xy(4, row * 2));
			row++;
		}
		// this two line (simulating JScrollPane resize)
		// are needed to more or less workaround the problem of revalidating whole scrollpane when more or fewer
		// rows could have been added. Without them you may end up with JScrollPane not completely showing its viewport
		manualFilterDetailsPanel.setPreferredSize(null);
		manualFilterDetailsPanel.setPreferredSize(new Dimension(scrollPane.getWidth(),
				manualFilterDetailsPanel.getPreferredSize().height));
	}

	private List<JiraFilterEntryBean> serializeFilter(List<JIRAQueryFragment> filter) {
		List<JiraFilterEntryBean> query = new ArrayList<JiraFilterEntryBean>();
		for (JIRAQueryFragment jiraQueryFragment : filter) {
			query.add(new JiraFilterEntryBean(jiraQueryFragment.getMap()));
		}
		return query;
	}


	private static class MyNewPanel extends JPanel {
		private MyNewPanel() {
			setBorder(new BevelBorder(BevelBorder.RAISED, Color.RED, Color.CYAN));
			FormLayout layout = new FormLayout("12dlu, right:p, 14dlu, left:d, 12dlu");
			PanelBuilder builder = new PanelBuilder(layout, this);
			CellConstraints cc = new CellConstraints();
			builder.appendRow("p");
			cc.xy(2, 1).vAlign = CellConstraints.TOP;
			builder.addLabel("My Label:", cc);
			final Component wrapLabel = new JLabel("<html>Very long text which should be wraaaaaaped wrap!");
//			final JTextArea wrapLabel = new JTextArea("Very long text which should be wraaaaaaped wrap!");
//			wrapLabel.setWrapStyleWord(false);
//			wrapLabel.setLineWrap(true);
			builder.add(wrapLabel, cc.xy(4, 1));
			builder.appendRow("p");
			cc.xy(2, 2).vAlign = CellConstraints.TOP;
			builder.addLabel("My Label:", cc);
			final Component wrapLabel2 = new JLabel("<html>Very long text fds kdsfkl kdsfh kl ldsfjlkjdfs"
					+ " ljdfs lksjdjfkldsjfklsd lfjwhich should be wraaaaaaped wrap!");
			builder.add(wrapLabel2, cc.xy(4, 2));
			add(wrapLabel);
		}
	}

//	private static class MyOuterPanel extends JPanel {
//		private MyOuterPanel() {
//			super(new BorderLayout());
//			add(new MyNewPanel());
//		}
//	}



	// just for testing bloody Swing layouts with JScrollPane
	public static void main(String[] args) {
		//SwingAppRunner.run(new MyNewPanel());
//		SwingAppRunner.run(new JScrollPane(new MyNewPanel()));
//		SwingAppRunner.run(new JScrollnew MyOuterPanel());
		final MyNewPanel panel = new MyNewPanel();
		final JScrollPane jScrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				panel.setPreferredSize(null);
				panel.setPreferredSize(new Dimension(e.getComponent().getWidth(), panel.getPreferredSize().height));
			}
		});
		SwingAppRunner.run(jScrollPane);
	}
}

