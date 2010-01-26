package com.atlassian.connector.intellij.crucible.content;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jan 26, 2010
 * Time: 10:57:32 AM
 * To change this template use File | Settings | File Templates.
 */
import java.util.*;
import org.apache.log4j.*;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.collections.MapIterator;
import org.jetbrains.annotations.Nullable;


/**
 * downloaded from : http://www.vipan.com/htdocs/cachehelp.html
This class is <b>not</b> meant to be thread-safe.  It does not appear to make
sense.  It is easy to do with Collections.synchronizedMap(cacheMap) and not
using the iterator (instead, copy keys to a different array and remove in
timerTask using that array).  Making operations synchronized will slow down
the operations.

*/
public class ExpiringCache<K,V> {

	static Category logger = Category.getInstance(ExpiringCache.class);

  public static final long DEFAULT_TIME_TO_LIVE = 10 * 60 * 1000;
  public static final long DEFAULT_ACCESS_TIMEOUT = 5 * 60 * 1000;
  public static final long DEFAULT_TIMER_INTERVAL = 2 * 60 * 1000;

  long ttl = DEFAULT_TIME_TO_LIVE;
  long ato = DEFAULT_ACCESS_TIMEOUT;
  long tiv = DEFAULT_TIMER_INTERVAL;

  LRUMap cacheMap;
  Timer cacheManager;


  protected void finalize() throws Throwable {
    if (cacheManager != null) cacheManager.cancel();
  }

  public ExpiringCache() {
    cacheMap = new LRUMap();
    initialize();
  }

  // All times in millisecs
  public ExpiringCache(long timeToLive, long accessTimeout,
    int maximumCachedQuantity, long timerInterval
  ) {
    ttl = timeToLive;
    ato = accessTimeout;
    cacheMap =  new LRUMap(maximumCachedQuantity);
    tiv = timerInterval;
    initialize();
  }


  public void setTimeToLive(long milliSecs) {
    ttl = milliSecs;
    initialize();
  }



  public void setAccessTimeout(long milliSecs) {
    ato = milliSecs;
    initialize();
  }



  public void setCleaningInterval(long milliSecs) {
    tiv = milliSecs;
    initialize();
  }


  public void initialize() {
    if (logger.isDebugEnabled()) logger.debug("initialize() started");
    if (cacheManager != null) cacheManager.cancel();
    cacheManager = new Timer(true);
    cacheManager.schedule(
      new TimerTask() {
        public void run() {
          NDC.push("TimerTask");
          long now = System.currentTimeMillis();
          try {
            MapIterator itr = cacheMap.mapIterator();
            while (itr.hasNext()) {
              K key = (K)itr.next();
              CachedObject cobj = (CachedObject) itr.getValue();
              if (cobj == null || cobj.hasExpired(now)) {
                if (logger.isDebugEnabled()) logger.debug(
                  "Removing " + key + ": Idle time=" +
                  (now - cobj.timeAccessedLast) + "; Stale time:" +
                  (now - cobj.timeCached));
                itr.remove();
                Thread.yield();
              }
            }
          }
          catch (ConcurrentModificationException cme) {
            /*
            Ignorable.  This is just a timer cleaning up.
            It will catchup on cleaning next time it runs.
            */
            if (logger.isDebugEnabled()) logger.debug(
              "Ignorable ConcurrentModificationException");
          }
          NDC.remove();
        }
      },
      0,
      tiv
    );
  }




  public int howManyObjects() {
    return cacheMap.size();
  }


  public void clear() {
    cacheMap.clear();
  }

	/**
  If the given key already maps to an existing object and the new object
  is not equal to the existing object, existing object is overwritten
  and the existing object is returned; otherwise null is returned.
  You may want to check the return value for null-ness to make sure you
  are not overwriting a previously cached object.  May be you can use a
  different key for your object if you do not intend to overwrite.
  */
  @Nullable
  public V admit(K key, V dataToCache) {
    //cacheMap.put(key, new CachedObject(dataToCache));
    //return null;

    CachedObject cobj = (CachedObject) cacheMap.get(key);
    if (cobj == null) {
      cacheMap.put(key, new CachedObject(dataToCache));
      return null;
    }
    else {
      V obj = cobj.getCachedData(key);
      if (obj == null) {
        if (dataToCache == null) {
          // Avoids creating unnecessary new cachedObject
          // Number of accesses is not reset because object is the same
          cobj.timeCached = cobj.timeAccessedLast = System.currentTimeMillis();
          return null;
        }
        else {
          cacheMap.put(key, new CachedObject(dataToCache));
          return null;
        }
      }
      else if (obj.equals(dataToCache)) {
        // Avoids creating unnecessary new cachedObject
        // Number of accesses is not reset because object is the same
        cobj.timeCached = cobj.timeAccessedLast = System.currentTimeMillis();
        return null;
      }
      else {
        cacheMap.put(key, new CachedObject(dataToCache));
        return obj;
      }
    }
  }


  @Nullable
  public V admit(K key, V dataToCache, long objectTimeToLive, long objectIdleTimeout) {
    CachedObject cobj = (CachedObject) cacheMap.get(key);
    if (cobj == null) {
      cacheMap.put(key, new CachedObject(dataToCache, objectTimeToLive, objectIdleTimeout));
      return null;
    }
    else {
      V obj = cobj.getCachedData(key);
      if (obj == null) {
        if (dataToCache == null) {
          // Avoids creating unnecessary new cachedObject
          // Number of accesses is not reset because object is the same
          cobj.timeCached = cobj.timeAccessedLast = System.currentTimeMillis();
          cobj.objectTTL = objectTimeToLive;
          cobj.objectIdleTimeout = objectIdleTimeout;
          cobj.userTimeouts = true;
          return null;
        }
        else {
          cacheMap.put(key, new CachedObject(dataToCache, objectTimeToLive, objectIdleTimeout));
          return null;
        }
      }
      else if (obj.equals(dataToCache)) {
        // Avoids creating unnecessary new cachedObject
        // Number of accesses is not reset because object is the same
        cobj.timeCached = cobj.timeAccessedLast = System.currentTimeMillis();
        cobj.objectTTL = objectTimeToLive;
        cobj.objectIdleTimeout = objectIdleTimeout;
        cobj.userTimeouts = true;
        return null;
      }
      else {
        cacheMap.put(key, new CachedObject(dataToCache, objectTimeToLive, objectIdleTimeout));
        return obj;
      }
    }
  }


  @Nullable
  public V recover(K key) {
    CachedObject cobj = (CachedObject) cacheMap.get(key);
    if (cobj == null) return null;
    else return cobj.getCachedData(key);
  }


  public void discard(K key) {
    cacheMap.remove(key);
  }


  public long whenCached(K key) {
    CachedObject cobj = (CachedObject) cacheMap.get(key);
    if (cobj == null) return 0;
    return cobj.timeCached;
  }


  public long whenLastAccessed(K key) {
    CachedObject cobj = (CachedObject) cacheMap.get(key);
    if (cobj == null) return 0;
    return cobj.timeAccessedLast;
  }


  public int howManyTimesAccessed(K key) {
    CachedObject cobj = (CachedObject) cacheMap.get(key);
    if (cobj == null) return 0;
    return cobj.numberOfAccesses;
  }


  /**
  A cached object, needed to store attributes such as the last time
  it was accessed.
  */
  protected class CachedObject {
    V cachedData;
    long timeCached;
    long timeAccessedLast;
    int numberOfAccesses;
    long objectTTL;
    long objectIdleTimeout;
    boolean userTimeouts;


    CachedObject(V cachedData) {
      long now = System.currentTimeMillis();
      this.cachedData = cachedData;
      timeCached = now;
      timeAccessedLast = now;
      ++numberOfAccesses;
    }

    CachedObject(V cachedData, long timeToLive, long idleTimeout) {
      long now = System.currentTimeMillis();
      this.cachedData = cachedData;
      objectTTL = timeToLive;
      objectIdleTimeout = idleTimeout;
      userTimeouts = true;
      timeCached = now;
      timeAccessedLast = now;
      ++numberOfAccesses;
    }


    V getCachedData(K key) {
      long now = System.currentTimeMillis();
      if (hasExpired(now)) {
				cachedData = null;
				cacheMap.remove(key);
				return null;
			}
      timeAccessedLast = now;
      ++numberOfAccesses;
      return cachedData;
    }

    boolean hasExpired(long now) {
      long usedTTL = userTimeouts?objectTTL:ttl;
      long usedATO = userTimeouts?objectIdleTimeout:ato;

      if (now > timeAccessedLast + usedATO ||
        now > timeCached + usedTTL
      ) {
        return true;
      }
      else return false;
    }


  }



} // END OF CLASS



