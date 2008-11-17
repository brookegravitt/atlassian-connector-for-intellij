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

package com.atlassian.theplugin.idea.ui;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;

public class CollapsiblePanel extends JPanel {
	private JButton myToggleCollapseButton;
	private JComponent myContent;
	private boolean myIsCollapsed;
	private final Collection<CollapsingListener> myListeners = new ArrayList<CollapsingListener>();
	private boolean myIsInitialized = false;
	private Icon myExpandIcon;
	private Icon myCollapseIcon;
	private JLabel myTitleLabel;
	private JPanel contentPanel;
	private JPanel labelPanel;

	public static final KeyStroke LEFT_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
	public static final KeyStroke RIGHT_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
	@NonNls
	public static final String EXPAND = "expand";
	@NonNls
	public static final String COLLAPSE = "collapse";


	public CollapsiblePanel(boolean collapseButtonAtLeft,
							boolean isCollapsed, Icon collapseIcon, Icon expandIcon,
							String title) {
		super(new GridBagLayout());
		setupComponents(expandIcon, collapseIcon, title, collapseButtonAtLeft, isCollapsed);
	}

	public CollapsiblePanel(JComponent content, boolean collapseButtonAtLeft,
							boolean isCollapsed, Icon collapseIcon, Icon expandIcon,
							String title) {
		super(new GridBagLayout());
		setupComponents(expandIcon, collapseIcon, title, collapseButtonAtLeft, isCollapsed);
		setContent(content);
	}


	public CollapsiblePanel(boolean collapseButtonAtLeft,
							boolean isCollapsed, String title) {
		super(new GridBagLayout());
		Icon collapseIcon = IconLoader.findIcon("/icons/navigate_down_10.gif");
		Icon expandIcon = IconLoader.findIcon("/icons/navigate_right_10.gif");
		setupComponents(expandIcon, collapseIcon, title, collapseButtonAtLeft, isCollapsed);
	}

	public void setTitle(String title) {
		myTitleLabel.setText(title);
	}


	private Dimension getButtonDimension() {
		if (myExpandIcon == null) {
			return new Dimension(9, 9);
		} else {
			return new Dimension((myExpandIcon.getIconWidth() > myCollapseIcon.getIconWidth() ? myExpandIcon.getIconWidth() : myCollapseIcon.getIconWidth()),
					myExpandIcon.getIconHeight() > myCollapseIcon.getIconHeight() ? myExpandIcon.getIconHeight() : myCollapseIcon.getIconHeight());
		}
	}

	public CollapsiblePanel(JComponent content, boolean collapseButtonAtLeft) {
		this(content, collapseButtonAtLeft, false, null, null, null);
	}

	protected void setCollapsed(boolean collapse) {
		try {

			if (collapse) {
				if (myIsInitialized) {
					remove(contentPanel);


					if (contentPanel != null) {
						contentPanel.setVisible(false);
						myContent.setVisible(false);
					}
					labelPanel.requestFocusInWindow();
				}

			} else {
				if (myContent != null) {
					add(contentPanel,
							new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(3, 0, 0, 0), 0, 0));
					contentPanel.setVisible(true);
					myContent.setVisible(true);

				}

			}
			myIsCollapsed = collapse;


			revalidate();
			repaint();

			Icon icon = getIcon();
			if (icon != null) {

				myToggleCollapseButton.setIcon(icon);
				myToggleCollapseButton.setBorder(null);
				myToggleCollapseButton.setBorderPainted(false);
				myToggleCollapseButton.setToolTipText(getToggleButtonToolTipText());
			}


			if (collapse) {
				setFocused(true);
				setSelected(true);

			} else if (myContent != null) {
				myContent.requestFocusInWindow();
			}


			notifyListners();

			revalidate();
			repaint();
		} finally {
			myIsInitialized = true;
		}
	}

	private String getToggleButtonToolTipText() {
		if (myIsCollapsed) {
			return "Collapsed";
		} else {
			return "Expanded";
		}
	}

	private Icon getIcon() {
		if (myIsCollapsed) {
			return myExpandIcon;
		} else {
			return myCollapseIcon;
		}
	}

	private void notifyListners() {
		CollapsingListener[] listeners = myListeners.toArray(new CollapsingListener[myListeners.size()]);
		for (CollapsingListener listener : listeners) {
			listener.onCollapsingChanged(this, isCollapsed());
		}
	}

	public JComponent getContent() {
		return myContent;
	}

	public void setContent(JComponent content) {
		setBackground(content.getBackground());
		this.myContent = content;
		contentPanel.setBackground(content.getBackground());
		contentPanel.add(content, BorderLayout.CENTER);
	}

	private void setupComponents(Icon expandIcon, Icon collapseIcon,
								 String title, boolean collapseButtonAtLeft,
								 boolean isCollapsed) {

		contentPanel = new JPanel(new BorderLayout());

		this.myToggleCollapseButton = new JButton();
		this.myExpandIcon = expandIcon;
		this.myCollapseIcon = collapseIcon;

		final Dimension buttonDimension = getButtonDimension();


		myToggleCollapseButton.setOpaque(false);
		myToggleCollapseButton.setBorderPainted(false);

		myToggleCollapseButton.setSize(buttonDimension);
		myToggleCollapseButton.setPreferredSize(buttonDimension);
		myToggleCollapseButton.setMinimumSize(buttonDimension);
		myToggleCollapseButton.setMaximumSize(buttonDimension);

		myToggleCollapseButton.setFocusable(true);


		myToggleCollapseButton.getActionMap().put(COLLAPSE, new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				collapse();
			}
		});

		myToggleCollapseButton.getActionMap().put(EXPAND, new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				expand();
			}
		});

		myToggleCollapseButton.getInputMap().put(LEFT_KEY_STROKE, COLLAPSE);
		myToggleCollapseButton.getInputMap().put(RIGHT_KEY_STROKE, EXPAND);

		myToggleCollapseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCollapsed(!myIsCollapsed);
			}
		});

		final int iconAnchor = collapseButtonAtLeft ? GridBagConstraints.WEST : GridBagConstraints.EAST;
		labelPanel = new JPanel(new BorderLayout(20, 0));

		labelPanel.add(myToggleCollapseButton, BorderLayout.LINE_START);
		labelPanel.setBackground(UIUtil.getTableSelectionBackground());

		myTitleLabel = new JLabel(title != null ? title : "");
		myTitleLabel.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD));

		labelPanel.add(myTitleLabel, BorderLayout.CENTER);
		revalidate();
		repaint();

		myTitleLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) { // on double click, just open the issue
				if (myIsCollapsed) {
					expand();
				} else {
					collapse();
				}
			}
		});

		add(labelPanel,
				new GridBagConstraints(0, 0, 1, 1, 1, 0.0,
						iconAnchor,
						GridBagConstraints.HORIZONTAL,
						new Insets(1, collapseButtonAtLeft ? 0 : 1, 0, collapseButtonAtLeft ? 1 : 0), 0,
						0));

		myIsCollapsed = isCollapsed;
		setCollapsed(isCollapsed);

	}


	public void addCollapsingListener(CollapsingListener listener) {
		myListeners.add(listener);
	}

	public void removeCollapsingListener(CollapsingListener listener) {
		myListeners.remove(listener);
	}

	public boolean isCollapsed() {
		return myIsCollapsed;
	}

	public void expand() {
		if (myIsCollapsed) {
			setCollapsed(false);
		}
	}

	public void collapse() {
		if (!myIsCollapsed) {
			setCollapsed(true);
		}
	}

	public void setFocused(boolean focused) {
		myToggleCollapseButton.requestFocusInWindow();
	}

	public void setSelected(boolean selected) {
		myToggleCollapseButton.setSelected(selected);
	}

	public ActionMap getCollapsibleActionMap() {
		return myToggleCollapseButton.getActionMap();
	}

	public InputMap getCollapsibleInputMap() {
		return myToggleCollapseButton.getInputMap();
	}

	protected void paintComponent(Graphics g) {
		updatePanel();
		super.paintComponent(g);
	}

	private void updatePanel() {
		setBackground(UIUtil.getTableSelectionBackground());
		//contentPanel.setPreferredSize(getCustomPreferredSize());

	}

	protected void paintChildren(Graphics g) {
		if (myTitleLabel != null) {
			updateTitle();
		}


		updateToggleButton();

		super.paintChildren(g);
	}

	private void updateToggleButton() {
		myToggleCollapseButton.setBackground(UIUtil.getTableSelectionBackground());

	}

	private void updateTitle() {

		myTitleLabel.setForeground(UIUtil.getTableSelectionForeground());
		myTitleLabel.setBackground(UIUtil.getTableSelectionBackground());
	}

	private boolean paintAsSelected() {
		return myToggleCollapseButton.hasFocus() && isCollapsed();
	}

}
