package com.atlassian.theplugin.idea.ui;

import javax.swing.*;
import java.awt.*;

/**
 * User: jgorycki
 * Date: Mar 5, 2009
 * Time: 11:47:52 AM
 */
public class ScrollablePanel extends JPanel implements Scrollable {
	public static final int A_LOT = 100000;
    // I say it is a decent approximation of how tall a font character is. I am too lazy to actually measure it
    private static final int LINE_SCROLL_INCREMENT = 20;

    // cheating obviously but this seems to do the right thing, so whatever :)
	public Dimension getPreferredScrollableViewportSize() {
		return new Dimension(1, A_LOT);
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return LINE_SCROLL_INCREMENT;
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return LINE_SCROLL_INCREMENT;
	}

	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
}
