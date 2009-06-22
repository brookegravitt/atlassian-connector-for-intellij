package com.atlassian.theplugin.idea.util;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jun 22, 2009
 * Time: 11:21:05 AM
 * To change this template use File | Settings | File Templates.
 */

import java.lang.*;
import java.awt.*;
import java.awt.event.*;


public class FocusRequester implements Runnable {
    private static Component beanToFocusOn;
    private static FocusRequester theFocusRequester = null;
    private static WindowAdapter theWindowOpenListener = null;

    public static void requestFocus(Window win, Component bean) {
        if (theFocusRequester == null) {
            theFocusRequester = new FocusRequester();

// Create a WindowAdaptor which calls invokeLater on theFocusRequester when win is opened.
                    theWindowOpenListener = new WindowAdapter() {
                public void windowOpened(WindowEvent e) {
                    EventQueue.invokeLater(theFocusRequester);
                }
            };
        }

        beanToFocusOn = bean;

        if (win != null) {
            win.addWindowListener(theWindowOpenListener);
        }

// Call now in case window is already opened or win was passed as   null;
// normally the effective call is the
// one made in theWindowOpenListener.windowOpened()
        EventQueue.invokeLater(theFocusRequester);
    }

    public void run() {
        beanToFocusOn.requestFocus();
    }
}
