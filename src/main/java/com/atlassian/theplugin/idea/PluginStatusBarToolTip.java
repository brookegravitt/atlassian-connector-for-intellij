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

package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.commons.exception.ThePluginException;
import com.intellij.openapi.util.IconLoader;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PluginStatusBarToolTip extends Window {

    // htmlView panel shows HTML content
    private final JEditorPane htmlView = new JEditorPane();

    // title bar of the tooltip
    private final ToolTipTitleBar titleBar;

    private static final int INITIAL_SIZE_X = 400;
    private static final int INITIAL_SIZE_Y = 200;
    private static final int SIZE_TITLE_BAR_Y = 260;
    private static final Color BACKGROUND_COLOR = new Color(253, 254, 226);
	private JScrollPane scrollPane;

	public PluginStatusBarToolTip(JFrame frame) {

		super(frame);

		// html view is the main view of the tooltip
        htmlView.setEditable(false);
        htmlView.setContentType("text/html");
        htmlView.setBackground(BACKGROUND_COLOR);
		htmlView.addHyperlinkListener(new GenericHyperlinkListener());
        htmlView.setEditorKit(new ClasspathHTMLEditorKit());
        // title bar of the tooltip
        try {
            titleBar = new ToolTipTitleBar(this);
        } catch (ThePluginException e) {
            throw new RuntimeException(e);
        }
        titleBar.setTitle(" " + "Bamboo builds status");
        titleBar.setSize(INITIAL_SIZE_X, SIZE_TITLE_BAR_Y);
        titleBar.setMaximumSize(new Dimension(INITIAL_SIZE_X, SIZE_TITLE_BAR_Y));
        titleBar.setMinimumSize(new Dimension(INITIAL_SIZE_X, SIZE_TITLE_BAR_Y));

		// add scroll facility to the html area
		scrollPane = new JScrollPane(htmlView,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setWheelScrollingEnabled(true);

		// put components on the main window
		this.setSize(INITIAL_SIZE_X, INITIAL_SIZE_Y);
        this.setLayout(new BorderLayout());
        this.add(titleBar, BorderLayout.NORTH);
		this.add(scrollPane, BorderLayout.CENTER);

		// hide tooltip when focus lost
		this.addWindowFocusListener(new WindowAdapter() {
			public void windowLostFocus(WindowEvent e) {
				setVisible(false);
			}
		});

		// pas key events to main window
//		this.addKeyListener(new KeyAdapter() {
//			public void keyTyped(KeyEvent e) {
//
//				e.setSource(PluginStatusBarToolTip.this.getOwner());
//				java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(e);
//				PluginStatusBarToolTip.this.getOwner().dispatchEvent(e);
//			}
//		});
	}

	/**
     * Shows tooltip in the place where mouse pointer is located.
     *
     * @param xMouse current horizontal position of the mouse pointer (x of right-bottom corner of the tooltip)
     * @param yMouse current vertical position of the mouse pointer (y of right-bottom corner of the tooltip)
     */
    public void showToltip(int xMouse, int yMouse) {


		int startX = xMouse - (int) this.getSize().getWidth();
		int startY = yMouse - (int) this.getSize().getHeight();

		if (startX < 0) {
			startX = xMouse;
		}

		if (startY < 0) {
			startY = yMouse;
		}

		this.setLocation(startX, startY);

		this.setVisible(true);
        this.requestFocus();
		this.toFront();
	}

//	/**
//	 * Shows tooltip in the place where was visible before
//	 */
//	public void showTooltip() {
//		this.setVisible(true);
//        this.requestFocus();
//		this.toFront();
//	}
//
//	/**
//	 * Hides tooltip
//	 */
//	public void hideTooltip() {
//		this.setVisible(false);
//	}

	public void setHtmlContent(String html) {
        htmlView.setText(html);
		//scrollPane.getVerticalScrollBar().setValue(0);
		// move scroll bar to the top
		htmlView.setCaretPosition(0);
	}

	private static class ToolTipTitleBar extends JPanel {

        private Icon closeButtonIcon = IconLoader.getIcon("/icons/close_icon_30x15.png");
        private Icon closeButtonIconHover = IconLoader.getIcon("/icons/close_icon_30x15_hover.png");

		// resize label
		private final JLabel resizeLabel = new JLabel(" ");
		// title label
        private final GradientLabel titleLabel = new GradientLabel();
        // close icon container (top-right corner)
        private final JLabel closeIconLabel = new JLabel();
        // reference to the main tooltip window
        private PluginStatusBarToolTip tooltipWindow;

        /**
         * Creates titlebar in the tooltip with title and close button (icon).
         *
         * @param window reference to the main tooltip window to hide when close button clicked
         */
        ToolTipTitleBar(PluginStatusBarToolTip window) throws ThePluginException {

            if (window == null) {
                throw new ThePluginException("Null tooltip window reference passed to tooltip window title bar");
            }

            tooltipWindow = window;

            closeIconLabel.setIcon(closeButtonIcon);

            // add resize label, title and close icon to title bar
            this.setLayout(new BorderLayout());
			this.add(resizeLabel, BorderLayout.WEST);
			this.add(titleLabel, BorderLayout.CENTER);
            this.add(closeIconLabel, BorderLayout.EAST);

            // hide tooltip when clicked on close icon
            closeIconLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    tooltipWindow.setVisible(false);
                }

                public void mouseEntered(MouseEvent e) {
                    closeIconLabel.setIcon(closeButtonIconHover);
                }

                public void mouseExited(MouseEvent e) {
                    closeIconLabel.setIcon(closeButtonIcon);
                }
            });

            // move tooltip when draging title label
            ToolTipMoveHandler moveHandler = window.getNewMoveHandler();
            titleLabel.addMouseMotionListener(moveHandler);
            titleLabel.addMouseListener(moveHandler);

			// resize tooltip
			ToolTipResizeHandler resizeHandler = window.getNewResizeHandler();
			resizeLabel.addMouseListener(resizeHandler);
			resizeLabel.addMouseMotionListener(resizeHandler);

		}

        public void setTitle(String title) {
            titleLabel.setText(title);
        }

	}

	private ToolTipResizeHandler getNewResizeHandler() {
		return new ToolTipResizeHandler();
	}

	private class ToolTipResizeHandler extends MouseInputAdapter {
		private static final int MINIMUM_X_SIZE = 200;
		private static final int MINIMUM_Y_SIZE = 100;

		private int mousePressAbsoluteX;
		private int mousePressAbsoluteY;
		private int initialWindowXSize;
		private int initialWindowYSize;
		private int initialWindowXPosition;
		private int initialWindowYPosition;

		public void mouseEntered(MouseEvent e) {
			Cursor c = new Cursor(Cursor.NW_RESIZE_CURSOR);
			((JComponent) e.getSource()).setCursor(c);
		}

		public void mouseDragged(MouseEvent e) {

			int changedX = mousePressAbsoluteX - (int) (MouseInfo.getPointerInfo().getLocation().getX());
			int changedY = mousePressAbsoluteY - (int) (MouseInfo.getPointerInfo().getLocation().getY());

			int newXSize = initialWindowXSize + changedX;
			int newYSize = initialWindowYSize + changedY;

			if (newXSize > MINIMUM_X_SIZE) {
				int currentYPosition = (int) PluginStatusBarToolTip.this.getLocation().getY();
				int currentYSize = (int) PluginStatusBarToolTip.this.getSize().getHeight();

				PluginStatusBarToolTip.this.setSize(
						initialWindowXSize + changedX,
						currentYSize);

				PluginStatusBarToolTip.this.setLocation(
						initialWindowXPosition - changedX,
						currentYPosition);
			}
			if (newYSize > MINIMUM_Y_SIZE) {
				int currentXPosition = (int) PluginStatusBarToolTip.this.getLocation().getX();
				int currentXSize = (int) PluginStatusBarToolTip.this.getSize().getWidth();

				PluginStatusBarToolTip.this.setSize(
						currentXSize,
						initialWindowYSize + changedY);

				PluginStatusBarToolTip.this.setLocation(
						currentXPosition,
						initialWindowYPosition - changedY);
			}
		}

		public void mousePressed(MouseEvent e) {
			mousePressAbsoluteX = (int) (MouseInfo.getPointerInfo().getLocation().getX());
			mousePressAbsoluteY = (int) (MouseInfo.getPointerInfo().getLocation().getY());

			initialWindowXSize = (int) PluginStatusBarToolTip.this.getSize().getWidth();
			initialWindowYSize = (int) PluginStatusBarToolTip.this.getSize().getHeight();

			initialWindowXPosition = (int) PluginStatusBarToolTip.this.getLocation().getX();
			initialWindowYPosition = (int) PluginStatusBarToolTip.this.getLocation().getY(); 
		}
	}

	private ToolTipMoveHandler getNewMoveHandler() {
		return new ToolTipMoveHandler();
	}

	private class ToolTipMoveHandler extends MouseInputAdapter {
		private int mousePressRelativeX = 0;
		private int mousePressRelativeY = 0;

		public void mouseDragged(MouseEvent e) {
			PluginStatusBarToolTip.this.setLocation(
					(int) (MouseInfo.getPointerInfo().getLocation().getX() - mousePressRelativeX),
					(int) (MouseInfo.getPointerInfo().getLocation().getY() - mousePressRelativeY));
		}

		public void mousePressed(MouseEvent e) {
			mousePressRelativeX = e.getX();
			mousePressRelativeY = e.getY();
		}
	}

    private static class GradientLabel extends JLabel {

        private static final Color TITLE_BAR_COLOR1 = new Color(218, 236, 254); //255,255,255);
        private static final Color TITLE_BAR_COLOR2 = new Color(240, 240, 240); //255,255,100);

        protected void paintComponent(Graphics g) {
            final Graphics2D g2 = (Graphics2D) g;
            int w = getWidth();
            int h = getHeight();
            GradientPaint gradient = new GradientPaint(0, 0, TITLE_BAR_COLOR1, w, 0, TITLE_BAR_COLOR2, false);
            g2.setPaint(gradient);
            g2.fillRect(0, 0, w, h);

            super.paintComponent(g);
        }
    }
}
