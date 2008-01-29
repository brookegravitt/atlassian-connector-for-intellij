package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.exception.ThePluginException;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.util.IconLoader;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import static javax.swing.event.HyperlinkEvent.EventType.ACTIVATED;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-01-16
 * Time: 11:40:02
 * To change this template use File | Settings | File Templates.
 */
public class PluginStatusBarToolTip extends JFrame {

    private static final Category LOG = Logger.getInstance(PluginStatusBarToolTip.class);

    // htmlView panel shows HTML content
    private final JEditorPane htmlView = new JEditorPane();

    // title bar of the tooltip
    private final ToolTipTitleBar titleBar;

    private static final int SIZE_X = 600;
    private static final int SIZE_Y = 300;
    private static final int SIZE_TITLE_BAR_Y = 260;
    private static final Color BACKGROUND_COLOR = new Color(253, 254, 226);

    public PluginStatusBarToolTip() {

        // html view is the main view of the tooltip
        htmlView.setEditable(false);
        htmlView.setContentType("text/html");
        htmlView.setBackground(BACKGROUND_COLOR);
		htmlView.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType().equals(ACTIVATED)) {
                    BrowserUtil.launchBrowser(e.getURL().toString());
                }
                //System.out.println(e.getEventType().toString());
            }
        });

        // title bar of the tooltip
        try {
            titleBar = new ToolTipTitleBar(this);
        } catch (ThePluginException e) {
            throw new RuntimeException(e);
        }
        titleBar.setTitle(" " + "Bamboo builds status");
        titleBar.setSize(SIZE_X, SIZE_TITLE_BAR_Y);
        titleBar.setMaximumSize(new Dimension(SIZE_X, SIZE_TITLE_BAR_Y));
        titleBar.setMinimumSize(new Dimension(SIZE_X, SIZE_TITLE_BAR_Y));

		// add scroll facility to the html area
		JScrollPane scroll = new JScrollPane(htmlView,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setWheelScrollingEnabled(true);

		// put components on the main window
		this.setSize(SIZE_X, SIZE_Y);
        this.setUndecorated(true);
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(titleBar, BorderLayout.NORTH);
        //this.getContentPane().add(htmlView, BorderLayout.CENTER);
		this.getContentPane().add(scroll, BorderLayout.CENTER);

		// hide when focus lost
		this.addWindowFocusListener(new WindowAdapter() {
			public void windowLostFocus(WindowEvent e) {
				setVisible(false);
			}
		});
	}

	/**
     * Shows tooltip in the place where mouse pointer is located.
     *
     * @param xMouse current horizontal position of the mouse pointer (right corner of the tooltip)
     * @param yMouse current vertical position of the mouse pointer (bottom corner of the tooltip)
     */
    public void showToltip(int xMouse, int yMouse) {


		int startX = xMouse - SIZE_X;
		int startY = yMouse - SIZE_Y;

		if (startX < 0) {
			startX = xMouse;
		}

		if (startY < 0) {
			startY = yMouse;
		}

		this.setLocation(startX, startY);

		this.setVisible(true);
        this.requestFocus();
    }

    public void setHtmlContent(String html) {
        htmlView.setText(html);
    }

    private static class ToolTipTitleBar extends JPanel {

        private static Icon closeButtonIcon = IconLoader.getIcon("/icons/close_icon_30x15.png");
        private static Icon closeButtonIconHover = IconLoader.getIcon("/icons/close_icon_30x15_hover.png");

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

            // add title and close icon to title bar
            this.setLayout(new BorderLayout());
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
            MouseInputAdapterExt l = new MouseInputAdapterExt();
            titleLabel.addMouseMotionListener(l);
            titleLabel.addMouseListener(l);

        }

        public void setTitle(String title) {
            titleLabel.setText(title);
        }


    }

    private static class MouseInputAdapterExt extends MouseInputAdapter {
//			private int mousePressX = 0;
//			private int mousePressY = 0;

        // @todo find absolute position on the screen in java 1.5
//			public void mouseDragged(MouseEvent e) {
//				tooltipWindow.setLocation(e.getXOnScreen() - mousePressX, e.getYOnScreen() - mousePressY);
//			}
//
//			public void mousePressed(MouseEvent e) {
//				mousePressX = e.getX();
//				mousePressY = e.getY();
//			}
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
