package com.atlassian.theplugin.remoteapi;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;

import java.util.LinkedList;
import java.util.Queue;

/**
 * User: kalamon
 * Date: Jun 3, 2009
 * Time: 12:00:45 PM
 */
public final class MissingPasswordHandlerQueue {

    public interface Handler {
        void go();
    }

    private static final Queue<Handler> HANDLERS = new LinkedList<Handler>();

    private MissingPasswordHandlerQueue() {
    }
    
    public static void addHandler(Handler handler) {
        synchronized (HANDLERS) {
            HANDLERS.offer(handler);
            if (HANDLERS.size() > 1) {
                return;
            }
        }

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                synchronized (HANDLERS) {
                    while (HANDLERS.size() > 0) {
                        Handler h = HANDLERS.peek();
                        h.go();
                        HANDLERS.remove();
                    }
                }
            }
        }, ModalityState.defaultModalityState());
    }
}
