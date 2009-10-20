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
package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.jira.StatusBarPane;
import com.intellij.openapi.ui.Splitter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public abstract class ThreePanePanel extends JPanel {

	public static final float LEFT_PANEL_SPLIT_RATIO = 0.3f;
    public static final float RIGHT_PANEL_SPLIT_RATIO = 0.23f;
	protected static final float MANUAL_FILTER_PROPORTION_VISIBLE = 0.5f;
	protected static final float MANUAL_FILTER_PROPORTION_HIDDEN = 0.9f;

	//	private JPanel statusBarPane;
	private StatusBarPane statusBarPane;
	private final Splitter innerSplitPane = new Splitter(true, LEFT_PANEL_SPLIT_RATIO);
    private final Splitter outerSplitPane = new Splitter(true, 1 - RIGHT_PANEL_SPLIT_RATIO);

    private JScrollPane leftUpperScrollPane;
	private JScrollPane rightScrollPane;

    public ThreePanePanel() {
		super(new BorderLayout());

		statusBarPane = new StatusBarPane("");
		add(statusBarPane, BorderLayout.SOUTH);
		innerSplitPane.setShowDividerControls(false);
		innerSplitPane.setSecondComponent(createRightContent());
		innerSplitPane.setHonorComponentsMinimumSize(true);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				final Dimension dimension = e.getComponent().getSize();

				statusBarPane.validate();
				final boolean doVertical = dimension.getWidth() < dimension.getHeight();
				if (doVertical != innerSplitPane.getOrientation()) {
					innerSplitPane.setOrientation(doVertical);
				}
                if (doVertical != outerSplitPane.getOrientation()) {
                    outerSplitPane.setOrientation(doVertical);
                }

			}
		});

		add(outerSplitPane, BorderLayout.CENTER);
	}

    public void setLeftPaneVisible(boolean isVisible) {
		innerSplitPane.getFirstComponent().setVisible(isVisible);
		innerSplitPane.validate();
	}

	public void init() {
		innerSplitPane.setFirstComponent(createLeftContent());
		leftUpperScrollPane.setViewportBorder(BorderFactory.createEmptyBorder(0, Constants.DIALOG_MARGIN / 2, 0, 0));
		leftUpperScrollPane.setViewportView(getLeftPanel());
		rightScrollPane.setViewportView(getRightTree());

        JPanel p = new JPanel(new BorderLayout());
        p.add(getLeftToolBar(), BorderLayout.NORTH);
        p.add(innerSplitPane, BorderLayout.CENTER);
        outerSplitPane.setFirstComponent(p);
        outerSplitPane.setSecondComponent(createRightMostContent());
	}

    private JComponent createRightMostContent() {

        JPanel p = new JPanel(new BorderLayout());

        p.add(getRightMostToolBar(), BorderLayout.NORTH);
        p.add(getRightMostPanel(), BorderLayout.CENTER);

        return p;
    }

    public JComponent createLeftContent() {

		leftUpperScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		leftUpperScrollPane.setWheelScrollingEnabled(true);

		return leftUpperScrollPane;

	}

	public JScrollPane getRightScrollPane() {
		return rightScrollPane;
	}

	public void setStatusMessage(final String infoMessage) {
		statusBarPane.setInfoMessage(infoMessage, false);
	}

	public void setErrorMessage(final String errorMessage) {
		statusBarPane.setErrorMessage(errorMessage);
	}

	public void setErrorMessage(final String errorMessage, final Throwable exception) {
		statusBarPane.setErrorMessage(errorMessage, exception);
	}

	private JComponent createRightContent() {

		rightScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		rightScrollPane.setWheelScrollingEnabled(true);
		return rightScrollPane;

	}

	public void expandAllRightTreeNodes() {
		for (int i = 0; i < getRightTree().getRowCount(); i++) {
			getRightTree().expandRow(i);
		}
	}

	public void collapseAllRightTreeNodes() {
		for (int i = 0; i < getRightTree().getRowCount(); i++) {
			getRightTree().collapseRow(i);
		}
	}

	public JScrollPane getLeftScrollPane() {
		return leftUpperScrollPane;
	}

	protected abstract JTree getRightTree();

	protected abstract JComponent getLeftToolBar();

	protected abstract JComponent getLeftPanel();

    protected abstract JComponent getRightMostPanel();

    protected abstract JComponent getRightMostToolBar();
}
