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

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Splitter;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public abstract class TwoPanePanel extends JPanel {

	public static final float PANEL_SPLIT_RATIO = 0.3f;
	protected static final float MANUAL_FILTER_PROPORTION_VISIBLE = 0.5f;
	protected static final float MANUAL_FILTER_PROPORTION_HIDDEN = 0.9f;

	private JPanel statusBarPane;
	private final Splitter splitPane = new Splitter(true, PANEL_SPLIT_RATIO);
	private JScrollPane rightScrollPane;
	private JScrollPane leftUpperScrollPane;
	private JLabel statusBar;
	private JLabel hyperlinkLabel = new JLabel("<html><u>More</u>");

	private static final Color FAIL_COLOR = new Color(255, 100, 100);
	private String message;


	private static class JDialogX extends DialogWrapper {

		private JTextArea textArea;

		protected JDialogX(Component parent, boolean canBeParent, String text) {
			super(parent, canBeParent);
			setTitle("Detailed Status Information");
			textArea = new JTextArea(text);
			init();
			pack();
			
		}

		@Override
		protected Action[] createActions() {
		  return new Action[]{getOKAction()};
		}

		@Override
		protected JComponent createCenterPanel() {
			return new JScrollPane(textArea);
		}
	}

	public TwoPanePanel() {
		super(new BorderLayout());
		statusBar = new JLabel();
		statusBar.setMinimumSize(new Dimension(0, 0));

		statusBarPane = new JPanel(new FormLayout("2px, left:d:grow, right:pref, 2px", "4px, pref, 4px"));
		oldColor = statusBarPane.getBackground();
		CellConstraints cc = new CellConstraints();
		statusBarPane.add(statusBar, cc.xy(2, 2));
		statusBarPane.add(hyperlinkLabel, cc.xy(3, 2));
		hyperlinkLabel.setVisible(false);
		hyperlinkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		hyperlinkLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				new JDialogX(TwoPanePanel.this, false, getMessage()).show();
			}
		});

//		statusBarPane = new JScrollPane(statusBar, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
//				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
//			@Override
//			public Dimension getPreferredSize() {
//				Dimension dim = super.getPreferredSize();
//				dim.height = getToolBar().getPreferredSize().height;
//				return dim;
//			}
//		};
		// empty border is not transparent on Mac, so I need to use line border
//		statusBarPane.setBorder(BorderFactory.createLineBorder(oldColor, 2));
//		statusBar.setOpaque(true);
		add(statusBarPane, BorderLayout.SOUTH);
		splitPane.setShowDividerControls(false);
		splitPane.setSecondComponent(createRightContent());
		splitPane.setHonorComponentsMinimumSize(true);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				final Dimension dimension = e.getComponent().getSize();

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
		setStatusMessage(Collections.singleton(message), Collections.<String>emptySet());
	}

	public void setErrorMessage(final String message) {
		setStatusMessage(Collections.<String>emptySet(), Collections.singleton(message));
	}

	private final Color oldColor;

	/**
	 * It can be called from the non-UI thread
	 * @param msg info messages
	 * @param errors info error messages
	 */
	public void setStatusMessage(final Collection<String> msg, final Collection<String> errors) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				StringBuilder sb = new StringBuilder();
				for (String s : msg) {
					sb.append(s).append("\n");
				}
				for (String s : errors) {
					sb.append(s).append("\n");
				}
				if (msg.size() + errors.size() > 1) {
					hyperlinkLabel.setVisible(true);
				} else {
					hyperlinkLabel.setVisible(false);
				}

				String oneLiner = "";

				final Iterator<String> iterator = errors.iterator();
				if (iterator.hasNext()) {
					oneLiner = iterator.next();
				} else {
					final Iterator<String> it2 = msg.iterator();
					if (it2.hasNext()) {
						oneLiner = it2.next();
					}
				}

				message = sb.toString();
				statusBar.setText(oneLiner);
				statusBarPane.revalidate();
				statusBar.scrollRectToVisible(new Rectangle(1, statusBar.getPreferredSize().height, 1, 1));
				if (errors.size() > 0) {
					statusBarPane.setBackground(FAIL_COLOR);
					hyperlinkLabel.setBackground(FAIL_COLOR);
				} else {
					statusBarPane.setBackground(oldColor);
					hyperlinkLabel.setBackground(oldColor);
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


	private String getMessage() {
		return message;
	}

	protected abstract JTree getRightTree();
	protected abstract JComponent getToolBar();
	protected abstract JComponent getLeftPanel();

}
