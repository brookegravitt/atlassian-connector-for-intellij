package com.atlassian.connector.intellij.util;



import java.util.concurrent.*;

/**
 * @user pmaruszak
 * @date Jan 28, 2010
 */
public class Memorizer<A, V> implements Computable<A, V> {
    private final ConcurrentMap<A, Future<V>> cache
        = new ConcurrentHashMap<A, Future<V>>();
    private final Computable<A, V> c;
    public Memorizer(Computable<A, V> c) {
        this.c = c;
    }

    public V compute(final A key) throws InterruptedException {
        while (true) {
            Future<V> f = cache.get(key);
            if (f == null) {
                Callable<V> eval = new Callable<V>() {
                    public V call() throws InterruptedException {
                        return c.compute(key);
                    }
                };
                FutureTask<V> ft = new FutureTask<V>(eval);
                f = cache.putIfAbsent(key, ft);
                if (f == null) { f = ft; ft.run(); }
            }
            try {
                return f.get();
            } catch (CancellationException e) {
                cache.remove(key, f);
            } catch (ExecutionException e) {
                throw new InterruptedException(e.getMessage());
            }
        }
    }
}