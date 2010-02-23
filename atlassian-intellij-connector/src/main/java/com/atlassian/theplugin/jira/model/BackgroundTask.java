package com.atlassian.theplugin.jira.model;

import com.intellij.openapi.application.ApplicationManager;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @author pmaruszak
 * @date: Feb 23, 2010
 */
public abstract class BackgroundTask<V> implements Runnable, Future<V> {
    //private final FutureTask<V> computation = new Computation();

    private class Computation extends FutureTask<V> {
        private Computation(Callable<V> vCallable) {
            super(vCallable);
        }

        protected final void done() {
            ApplicationManager.getApplication().invokeLater(new Runnable() {

                public void run() {
                    V value = null;
                    Throwable thrown = null;
                    boolean cancelled = false;
                    try {
                        value = get();
                    } catch (InterruptedException e) {

                    } catch (ExecutionException e) {
                        thrown = e.getCause();
                    } finally {
                        onCompletion(value, thrown, cancelled);
                    }
                }
            });
        }
    }

    //protected void setProgress(final int current )
    protected abstract V compute();
    protected abstract void onCompletion(V result, Throwable exception, boolean cancelled);
    
}
