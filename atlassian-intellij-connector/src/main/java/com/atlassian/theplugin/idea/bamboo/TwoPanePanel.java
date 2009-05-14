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
import com.jgoodies.forms.layout.CellConstraints;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public abstract class TwoPanePanel extends JPanel {

	public static final float PANEL_SPLIT_RATIO = 0.3f;
	protected static final float MANUAL_FILTER_PROPORTION_VISIBLE = 0.5f;
	protected static final float MANUAL_FILTER_PROPORTION_HIDDEN = 0.9f;

	//	private JPanel statusBarPane;
	private StatusBarPane statusBarPane;
	private final Splitter splitPane = new Splitter(true, PANEL_SPLIT_RATIO);
	private JScrollPane rightScrollPane;
	private JScrollPane leftUpperScrollPane;
//	private JLabel statusBar;
//	private JLabel hyperlinkLabel = new JLabel("<html><u>More</u>");

	private static final Color FAIL_COLOR = new Color(255, 100, 100);
	private String message;
	private String infos;
	private String errors;

	/*private static class JDialogX extends DialogWrapper {

		private JTextPane textPane;

		protected JDialogX(Component parent, boolean canBeParent, String infos, final String errors) {
			super(parent, canBeParent);
			setTitle("Detailed Status Information");
			textPane = new JTextPane();
			textPane.setText(infos + errors);

			// create text style "red"
			Style style = textPane.addStyle("Red", null);
			StyleConstants.setForeground(style, Color.red);

			// apply style "red"
			StyledDocument doc = textPane.getStyledDocument();
			doc.setCharacterAttributes(infos.length(), errors.length(), textPane.getStyle("Red"), true);

			textPane.setEditable(false);
			textPane.setBackground(Color.WHITE);
			init();
			pack();

		}

		@Override
		protected Action[] createActions() {
			return new Action[]{getOKAction()};
		}

		@Override
		protected JComponent createCenterPanel() {
			JScrollPane scroll = new JScrollPane(textPane);
			//CHECKSTYLE:MAGIC:OFF
			scroll.setMinimumSize(new Dimension(300, 100));
			//CHECKSTYLE:MAGIC:ON
			return scroll;
		}
	}*/

	public TwoPanePanel() {
		super(new BorderLayout());
//		statusBar = new JLabel();
//		statusBar.setMinimumSize(new Dimension(0, 0));

//		statusBarPane = new JPanel(new FormLayout("2px, left:d:grow, right:pref, 2px", "4px, pref, 4px"));
		statusBarPane = new StatusBarPane("");
		oldColor = statusBarPane.getBackground();
		CellConstraints cc = new CellConstraints();
		//CHECKSTYLE:MAGIC:OFF
//		statusBarPane.add(statusBar, cc.xy(2, 2));
//		statusBarPane.add(hyperlinkLabel, cc.xy(3, 2));
		//CHECKSTYLE:MAGIC:ON
//		hyperlinkLabel.setVisible(false);
//		hyperlinkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//		hyperlinkLabel.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseClicked(final MouseEvent e) {
//				new JDialogX(TwoPanePanel.this, false, infos, errors).show();
//			}
//		});

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
		leftUpperScrollPane.setViewportBorder(BorderFactory.createEmptyBorder(0, Constants.DIALOG_MARGIN / 2, 0, 0));
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

	public void setStatusMessage(final String infoMessage) {
		statusBarPane.setInfoMessage(infoMessage, false);
//		setStatusMessage(Collections.singleton(infoMessage), Collections.<String>emptySet());
	}

	public void setErrorMessage(final String errorMessage) {
		statusBarPane.setErrorMessage(errorMessage);
//		setStatusMessage(Collections.<String>emptySet(), Collections.singleton(errorMessage));
	}

	public void setErrorMessage(final String errorMessage, final Throwable exception) {
		statusBarPane.setErrorMessage(errorMessage, exception);
//		setStatusMessage(Collections.<String>emptySet(), Collections.singleton(errorMessage));
	}

	private final Color oldColor;

//	/**
//	 * It can be called from the non-UI thread
//	 *
//	 * @param msg info messages
//	 * @param aErrors info error messages
//	 */
//	public void setStatusMessage(final Collection<String> msg, final Collection<String> aErrors) {
//		EventQueue.invokeLater(new StatusMessageRunnable(msg, aErrors));
//	}

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

/*
	private String getMessage() {
		return message;
	}
*/

	protected abstract JTree getRightTree();

	protected abstract JComponent getToolBar();

	protected abstract JComponent getLeftPanel();

//	private class StatusMessageRunnable implements Runnable {
//		private final Collection<String> msg;
//		private final Collection<String> errors;
//
//		public StatusMessageRunnable(final Collection<String> msg, final Collection<String> errors) {
//			this.msg = msg;
//			this.errors = errors;
//		}
//
//		public void run() {
//			StringBuilder sb = new StringBuilder();
//			for (String s : msg) {
//				sb.append(s).append("\n");
//			}
//
//			TwoPanePanel.this.infos = sb.toString();
//
//			sb = new StringBuilder();
//			for (String s : errors) {
//				sb.append(s).append("\n");
//			}
//
//			TwoPanePanel.this.errors = sb.toString();
//
////			if (msg.size() + errors.size() > 1) {
////				hyperlinkLabel.setVisible(true);
////			} else {
////				hyperlinkLabel.setVisible(false);
////			}
//
//			String oneLiner = "";
//
//			final Iterator<String> iterator = errors.iterator();
//			if (iterator.hasNext()) {
//				oneLiner = iterator.next();
//			} else {
//				final Iterator<String> it2 = msg.iterator();
//				if (it2.hasNext()) {
//					oneLiner = it2.next();
//				}
//			}
//
//			message = TwoPanePanel.this.infos + TwoPanePanel.this.errors;
////			statusBar.setText(oneLiner);
//			statusBarPane.revalidate();
////			statusBar.scrollRectToVisible(new Rectangle(1, statusBar.getPreferredSize().height, 1, 1));
//			if (errors.size() > 0) {
//				statusBarPane.setBackground(FAIL_COLOR);
////				hyperlinkLabel.setBackground(FAIL_COLOR);
//			} else {
//				statusBarPane.setBackground(oldColor);
////				hyperlinkLabel.setBackground(oldColor);
//			}
//			repaint();
//		}
//	}
}
