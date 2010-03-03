package com.atlassian.connector.intellij.crucible.content;

/**
 * @auuthor pmaruszak
 * @date Jan 26, 2010
 */

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ReviewFileContent;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.configuration.IdeaPluginConfigurationBean;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.apache.log4j.Category;
import org.apache.log4j.NDC;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ConcurrentModificationException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Thread safe
 */
public final class FileContentExpiringCache implements ProjectComponent {

    private static Category logger = Category.getInstance(FileContentExpiringCache.class);

    public static final long DEFAULT_TIME_TO_LIVE = 10 * 60 * 1000;
    public static final long DEFAULT_ACCESS_TIMEOUT = 5 * 60 * 1000;
    public static final long DEFAULT_TIMER_INTERVAL = 2 * 60 * 1000;
    private static final long DEFAULT_CACHE_SIZE_BYTES = 20 * 1024 * 1024;
    private long ttl = DEFAULT_TIME_TO_LIVE;
    private long ato = DEFAULT_ACCESS_TIMEOUT;
    private long tiv = DEFAULT_TIMER_INTERVAL;

    private static final AtomicLong NUMBER_OF_BYTES_DOWNLOADED = new AtomicLong(0);
    private final ConcurrentMap<String, Future<CachedObject>> cacheMap
            = new ConcurrentHashMap<String, Future<CachedObject>>();

    private Timer cacheManager;
    private static final int INITIAL_FILE_DOWNLOAD = 20;
    private final Project project;
    private static long cacheSize;


    protected void finalize() throws Throwable {
        if (cacheManager != null) {
            cacheManager.cancel();
        }
    }

    private FileContentExpiringCache(final Project project, final IdeaPluginConfigurationBean config) {
        this.project = project;

        cacheSize = config.getCrucibleConfigurationData().getReviewFileCacheSize() * 1024 * 1024;

        initialize();
    }


    public static Category getLogger() {
        return logger;
    }

    public void initialize() {
        if (logger.isDebugEnabled()) {
            logger.debug("initialize() started");
        }

        if (cacheManager != null) {
            cacheManager.cancel();
        }

        cacheManager = new Timer(true);
        cacheManager.schedule(
                new TimerTask() {
                    public void run() {
                        NDC.push("TimerTask");
                        long now = System.currentTimeMillis();
                        try {
                            for (String key : cacheMap.keySet()) {
                                FutureTask task = (FutureTask) cacheMap.get(key);
                                CachedObject cobj = task != null ? (CachedObject) task.get() : null;
                                if (task == null || cobj != null && cobj.hasExpired(now)) {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(
                                                "Removing " + key + ": Idle time="
                                                        + (now - cobj.timeAccessedLast) + "; Stale time:"
                                                        + (now - cobj.timeCached));
                                    }
                                    final Future<CachedObject> future = cacheMap.get(key);
                                    decrementMemoryConsumed(future.get().cachedData.getContent().length);
                                    cacheMap.remove(future);
                                    Thread.yield();
                                }
                            }
                        } catch (ConcurrentModificationException cme) {
                            /*
                            Ignorable.  This is just a timer cleaning up.
                            It will catchup on cleaning next time it runs.
                            */
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        "Ignorable ConcurrentModificationException");
                            }
                        } catch (InterruptedException e) {
                            PluginUtil.getLogger().error("Interrupoted download:" + e.getMessage());
                        } catch (ExecutionException e) {
                            PluginUtil.getLogger().error("Execution problem:" + e.getMessage());
                        }
                        NDC.remove();
                    }
                },
                0,
                tiv
        );
    }

    private void decrementMemoryConsumed(long numberOfBytes) {
        final long l = NUMBER_OF_BYTES_DOWNLOADED.addAndGet(-1L * numberOfBytes);


    }

    private void incrementMemoryConsumed(long numberOfBytes) {
        final long l = NUMBER_OF_BYTES_DOWNLOADED.addAndGet(numberOfBytes);

    }

    public void clear() {
        cacheMap.clear();
    }

    public void initDownload(final ReviewAdapter review) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ProgressManager.getInstance().run(
                        new Task.Backgroundable(project, "Prefetching files for review " + review.getPermId()) {
                            @Override
                            public void run(@NotNull ProgressIndicator progressIndicator) {                                
                                int c = 0;
                                for (CrucibleFileInfo file : review.getFiles()) {
                                    VersionedVirtualFile versionedVirtualFile = file.getFileDescriptor();
                                    ReviewFileContentProvider provider = null;
                                    if (versionedVirtualFile != null) {
                                        try {
                                            if (IdeaHelper.getFileContentProviderProxy(project) == null) {
                                                continue;
                                            }
                                            provider = IdeaHelper.getFileContentProviderProxy(project)
                                                            .get(versionedVirtualFile, file, review);
                                        } catch (InterruptedException e) {
                                            continue;
                                        }
                                        downloadFile(versionedVirtualFile, provider, review);
                                        c++;
                                    }                             
                                    if (c > INITIAL_FILE_DOWNLOAD) {
                                        break;
                                    }
                                }
                            }
                        });

            }
        });

    }

    boolean cacheLimitReached() {
        return NUMBER_OF_BYTES_DOWNLOADED.get() >= cacheSize;
    }
    private FutureTask<CachedObject> downloadFile(final VersionedVirtualFile versionedVirtualFile,
                                                  final ReviewFileContentProvider provider,
                                                  final ReviewAdapter review) {
        String key = ContentUtil.getKey(versionedVirtualFile);
        Callable<CachedObject> eval = new Callable<CachedObject>() {
            public CachedObject call() throws InterruptedException {
                ReviewFileContent fileContent = null;
                try {
                    //System.out.println("Downloading file:" + versionedVirtualFile.getAbsoluteUrl());
                    fileContent = provider.getContent(review, versionedVirtualFile);
                    incrementMemoryConsumed(fileContent.getContent().length);
                } catch (ReviewFileContentException e) {
                    throw new InterruptedException(e.getMessage());
                }
                return new CachedObject(fileContent);

            }
        };
        FutureTask<CachedObject> ft = new FutureTask<CachedObject>(eval);
        Future<CachedObject> f = cacheMap.putIfAbsent(key, ft);
        if (f == null) {
            f = ft;
            ft.run();

            try {
                if (ft.get().getCachedData().getContent() == null) {
                   cacheMap.remove(key);
                   return null;
                }
            } catch (Exception e) {
                PluginUtil.getLogger().error("Cannot download file:" + key, e);
                return null;
            }
        }

        return ft;
    }
    
    @Nullable
    public ReviewFileContent get(final VersionedVirtualFile versionedVirtualFile, final ReviewFileContentProvider provider,
                                 final ReviewAdapter review) throws InterruptedException {

        while (true) {
            String key = ContentUtil.getKey(versionedVirtualFile);
            Future<CachedObject> f = cacheMap.get(key);
            if (f == null) {
                f = downloadFile(versionedVirtualFile, provider, review);

            } else {
                CachedObject cobj = null;
                try {
                    cobj = f.get();
                } catch (ExecutionException e) {
                    throw new InterruptedException(e.getMessage());
                }

                cobj.timeAccessedLast = System.currentTimeMillis();
                cobj.timeCached = cobj.timeAccessedLast;
            }
            try {

                return f != null && f.get() != null && f.get().getCachedData(key).getContent() != null
                        ? f.get().getCachedData(key) : null;
            } catch (CancellationException e) {
                cacheMap.remove(key, f);
            } catch (ExecutionException e) {
                throw new InterruptedException(e.getMessage());
            }
        }
    }

    public void projectOpened() {
    }

    public void projectClosed() {
        for (String key : cacheMap.keySet()) {
            cacheMap.get(key).cancel(true);
        }
    }

    @NotNull
    public String getComponentName() {
        return FileContentExpiringCache.class.getName();
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }


    /**
     * A cached object, needed to store attributes such as the last time
     * it was accessed.
     */
    protected class CachedObject implements Comparable<CachedObject> {
        private ReviewFileContent cachedData;
        private long timeCached;
        private long timeAccessedLast;
        private int numberOfAccesses;
        private long objectTTL;
        private long objectIdleTimeout;
        private boolean userTimeouts;


        CachedObject(ReviewFileContent cachedData) {
            long now = System.currentTimeMillis();
            this.cachedData = cachedData;
            timeCached = now;
            timeAccessedLast = now;
            ++numberOfAccesses;
        }

        CachedObject(ReviewFileContent cachedData, long timeToLive, long idleTimeout) {
            long now = System.currentTimeMillis();
            this.cachedData = cachedData;
            objectTTL = timeToLive;
            objectIdleTimeout = idleTimeout;
            userTimeouts = true;
            timeCached = now;
            timeAccessedLast = now;
            ++numberOfAccesses;
        }


        ReviewFileContent getCachedData(String key) {
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
            long usedTTL = userTimeouts ? objectTTL : ttl;
            long usedATO = userTimeouts ? objectIdleTimeout : ato;

            return now > timeAccessedLast + usedATO
                    || now > timeCached + usedTTL || cacheLimitReached();
        }

        public ReviewFileContent getCachedData() {
            return cachedData;
        }

        public void setCachedData(ReviewFileContent cachedData) {
            this.cachedData = cachedData;
        }

        public long getTimeCached() {
            return timeCached;
        }

        public void setTimeCached(long timeCached) {
            this.timeCached = timeCached;
        }

        public long getTimeAccessedLast() {
            return timeAccessedLast;
        }

        public void setTimeAccessedLast(long timeAccessedLast) {
            this.timeAccessedLast = timeAccessedLast;
        }

        public int getNumberOfAccesses() {
            return numberOfAccesses;
        }

        public void setNumberOfAccesses(int numberOfAccesses) {
            this.numberOfAccesses = numberOfAccesses;
        }

        public long getObjectTTL() {
            return objectTTL;
        }

        public void setObjectTTL(long objectTTL) {
            this.objectTTL = objectTTL;
        }

        public long getObjectIdleTimeout() {
            return objectIdleTimeout;
        }

        public void setObjectIdleTimeout(long objectIdleTimeout) {
            this.objectIdleTimeout = objectIdleTimeout;
        }

        public boolean isUserTimeouts() {
            return userTimeouts;
        }

        public void setUserTimeouts(boolean userTimeouts) {
            this.userTimeouts = userTimeouts;
        }


        public int compareTo(CachedObject o) {
            int diff = (int) (timeAccessedLast - o.timeAccessedLast);

            if (diff != 0) {
                return diff;
            }
            return (int) (timeCached - o.timeCached);
        }
    }


//      private List<CrucibleFileInfo> sortFilesToCache(final ReviewAdapter review) {
//        ArrayList<CrucibleFileInfoComparable> list = new ArrayList<CrucibleFileInfoComparable>();
//        for (CrucibleFileInfo fileInfo : review.getFiles()) {
//            list.add(new CrucibleFileInfoComparable(fileInfo));
//        }
//
//        Collections.sort(list);
//        return new ArrayList<CrucibleFileInfo>(list);
//
//    }
//    class CrucibleFileInfoComparable extends CrucibleFileInfo implements Comparable<CrucibleFileInfo> {
//        CrucibleFileInfoComparable(CrucibleFileInfo fileInfo) {
//            super(fileInfo.getFileDescriptor(), fileInfo.getOldFileDescriptor(), fileInfo.getPermId());
//        }
//
//        public int compareTo(CrucibleFileInfo crucibleFileInfo) {
//            if (getFileDescriptor() != null && crucibleFileInfo.getFileDescriptor() != null) {
//                return crucibleFileInfo.getFileDescriptor().getContentUrl().compareTo(getFileDescriptor().getContentUrl());
//            } else if (crucibleFileInfo.getOldFileDescriptor() != null) {
//                return crucibleFileInfo.getFileDescriptor().getContentUrl().compareTo(getOldFileDescriptor().getContentUrl());
//            }
//
//            if (getOldFileDescriptor() != null && crucibleFileInfo.getOldFileDescriptor() != null) {
//                return crucibleFileInfo.getFileDescriptor().getContentUrl().compareTo(getFileDescriptor().getContentUrl());
//            }
//            return 0;
//        }
//    }

}



