package com.atlassian.connector.intellij.util;

/**
 * @author pmaruszak
 * @date Jan 28, 2010
 */
public interface Computable<A, V> {
    V compute(A key) throws InterruptedException;    
}