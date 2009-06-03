package com.atlassian.theplugin.remoteapi;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;

import java.util.Queue;
import java.util.LinkedList;

/**
 * User: kalamon
 * Date: Jun 3, 2009
 * Time: 12:00:45 PM
 */
public final class MissingPasswordHandlerQueue {

    public interface Handler {
        void go();
    }

    private static final Queue<Handler> handlers = new LinkedList<Handler>();

    private MissingPasswordHandlerQueue() {
    }
    
    public static void addHandler(Handler handler) {
        synchronized (handlers) {
            handlers.offer(handler);
            if (handlers.size() > 1) {
                return;
            }
        }

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                synchronized (handlers) {
                    while (handlers.size() > 0) {
                        Handler h = handlers.peek();
                        h.go();
                        handlers.remove();
                    }
                }
            }
        }, ModalityState.defaultModalityState());
    }
}
