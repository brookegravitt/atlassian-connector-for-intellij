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

import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.configuration.CrucibleProjectConfiguration;
import com.atlassian.theplugin.idea.jira.StatusBarIssuesPane;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * @author Jacek Jaroczynski
 */
public class ReviewsToolWindowPanel extends JPanel {
	private static final float REVIEWS_PANEL_SPLIT_RATIO = 0.3f;

	private final Project project;
	private final CfgManager cfgManager;
	private final CrucibleProjectConfiguration crucibleProjectCfg;

	private final Splitter splitPane = new Splitter(true, REVIEWS_PANEL_SPLIT_RATIO);
	// left panel
	private Splitter splitFilterPane;
	private final StatusBarIssuesPane messagePane;
	private JPanel serversPanel = new JPanel(new BorderLayout());
	private JTree serversTree;
	private JPanel manualFilterDetailsPanel;
	// right panel
	private JPanel reviewsPanel;
	private JScrollPane reviewTreescrollPane;
	private JTree reviewTree;

	public ReviewsToolWindowPanel(@NotNull final Project project,
			@NotNull final CrucibleProjectConfiguration crucibleProjectConfiguration, @NotNull final CfgManager cfgManager) {

		this.project = project;
		this.crucibleProjectCfg = crucibleProjectConfiguration;
		this.cfgManager = cfgManager;

		setLayout(new BorderLayout());
		this.messagePane = new StatusBarIssuesPane("Reviews panel");
		add(messagePane, BorderLayout.SOUTH);

		splitPane.setShowDividerControls(false);
		splitPane.setFirstComponent(createFilterContent());
		splitPane.setSecondComponent(createIssuesContent());
		splitPane.setHonorComponentsMinimumSize(true);

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				final Dimension dimension = e.getComponent().getSize();
				final boolean doVertical = dimension.getWidth() < dimension.getHeight();
				if (doVertical != splitPane.getOrientation()) {
					splitPane.setOrientation(doVertical);
				}

			}
		});

		add(splitPane, BorderLayout.CENTER);



	}

	private JComponent createFilterContent() {
		splitFilterPane = new Splitter(false, 1.0f);
		splitFilterPane.setOrientation(true);

		serversPanel = new JPanel(new BorderLayout());

		serversTree = new JTree();
		JScrollPane filterListScrollPane = new JScrollPane(serversTree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		filterListScrollPane.setWheelScrollingEnabled(true);

		manualFilterDetailsPanel = new JPanel();

		serversPanel.add(filterListScrollPane, BorderLayout.CENTER);
		serversPanel.add(createServersToolbar(), BorderLayout.NORTH);

		//create manual filter panel
		splitFilterPane.setFirstComponent(serversPanel);

		return splitFilterPane;
	}

	private JComponent createServersToolbar() {
		// todo create toolbar
		return new JLabel();
	}

	private JComponent createIssuesContent() {
		reviewsPanel = new JPanel(new BorderLayout());

		reviewTreescrollPane = new JScrollPane(createIssuesTree(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		reviewTreescrollPane.setWheelScrollingEnabled(true);

		reviewsPanel.add(reviewTreescrollPane, BorderLayout.CENTER);
		reviewsPanel.add(createReviewsToolbar(), BorderLayout.NORTH);
		return reviewsPanel;
	}

	private JTree createIssuesTree() {
		reviewTree = new JTree();
//		issueTreeBuilder.rebuild(reviewTree, issuesPanel);
		return reviewTree;
	}

	private JComponent createReviewsToolbar() {
		// todo create toolbar
		return new JLabel();
	}

}
