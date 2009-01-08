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

import com.intellij.openapi.ui.Splitter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public abstract class TwoPanePanel extends JPanel {

	public static final float PANEL_SPLIT_RATIO = 0.3f;
	protected static final float MANUAL_FILTER_PROPORTION_VISIBLE = 0.5f;
	protected static final float MANUAL_FILTER_PROPORTION_HIDDEN = 0.9f;

	private JScrollPane statusBarPane;
	private final Splitter splitPane = new Splitter(true, PANEL_SPLIT_RATIO);
	private JScrollPane rightScrollPane;
	private JScrollPane leftUpperScrollPane;
	private JLabel statusBar;

	private static final Color FAIL_COLOR = new Color(255, 100, 100);


	public TwoPanePanel() {
		super(new BorderLayout());
		statusBar = new JLabel();
		statusBarPane = new JScrollPane(statusBar, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
			@Override
			public Dimension getPreferredSize() {
				Dimension dim = super.getPreferredSize();
				dim.height = getToolBar().getPreferredSize().height;
				return dim; 
			}
		};
		statusBarPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//		statusBarPane.setBorder(BorderFactory.createEmptyBorder());
		statusBar.setOpaque(true);
		add(statusBarPane, BorderLayout.SOUTH);
		splitPane.setShowDividerControls(false);
		splitPane.setSecondComponent(createRightContent());
		splitPane.setHonorComponentsMinimumSize(true);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				final Dimension dimension = e.getComponent().getSize();
//				statusBarContainer.revalidate();
//				statusBarContainer.validate();

				statusBarPane.validate();
				final boolean doVertical = dimension.getWidth() < dimension.getHeight();
				if (doVertical != splitPane.getOrientation()) {
					splitPane.setOrientation(doVertical);
				}

			}
		});

		add(splitPane, BorderLayout.CENTER);
	}

	public void setLeftPaneVisible(boolean isVisible) {
		splitPane.getFirstComponent().setVisible(isVisible);
		splitPane.validate();
	}

	public void init() {

		splitPane.setFirstComponent(createLeftContent());
		leftUpperScrollPane.setViewportView(getLeftPanel());
		rightScrollPane.setViewportView(getRightTree());
		add(getToolBar(), BorderLayout.NORTH);
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

	public void setStatusMessage(final String message) {
		setStatusMessage(message, false);
	}

	private Color oldColor = new JLabel().getBackground();

	/**
	 * It can be called from the non-UI thread
	 * @param msg message
	 * @param isError error flag
	 */
	public void setStatusMessage(final String msg, final boolean isError) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				statusBar.setText("<html>" + msg);
				statusBarPane.revalidate();
				statusBar.scrollRectToVisible(new Rectangle(1, statusBar.getPreferredSize().height, 1, 1));
				if (isError) {
					statusBar.setBackground(FAIL_COLOR);
				} else {
					statusBar.setBackground(oldColor);
				}
				repaint();
			}
		});
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
	protected abstract JComponent getToolBar();
	protected abstract JComponent getLeftPanel();

}
