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

	// cheating obviously but this seems to do the right thing, so whatever :)
	public Dimension getPreferredScrollableViewportSize() {
		return new Dimension(1, A_LOT);
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 1;
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 1;
	}

	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
}
