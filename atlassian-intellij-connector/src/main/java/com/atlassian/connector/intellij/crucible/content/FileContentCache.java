/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.connector.intellij.crucible.content;

import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ReviewFileContent;
import com.atlassian.theplugin.commons.util.SimpleLruCache;

/**
 * @user pmaruszak
 * @date Dec 29, 2009
 */
final public class FileContentCache {
    private static FileContentCache instance;
    private static int cacheSize = 100;
    
    private static  SimpleLruCache<String, ReviewFileContent> cache;
    private static final long TIME_TO_WAIT = 30000;    

    private FileContentCache() {
        //not synchronized
        cache = new SimpleLruCache<String, ReviewFileContent>(cacheSize);
    }

    public ReviewFileContent getFileContent(VersionedVirtualFile virtualFile) {
        String key = ContentUtil.getKey(virtualFile);
        if (ContentDownloader.getInstance().isDownloadInProgress(key)) {
            Thread downloadingThread = ContentDownloader.getInstance().getDownloadingThread(key);
            if (downloadingThread != null) {
                try {
                    //wait for thread to finish downloading
                    downloadingThread.join(TIME_TO_WAIT);
                } catch (InterruptedException e) {
                }
            }
        }
        return cache.get(key);
    }

    public void clear() {
        cache.getMap().clear();
    }

    public static void setCacheSize(int cacheSize) {
        FileContentCache.cacheSize = cacheSize;
    }
    public static  FileContentCache getInstance() {
        if (instance == null) {
            instance = new FileContentCache();
        }

        return instance;

    }

    public void put(String fileKey, ReviewFileContent content) {
        cache.getMap().put(fileKey, content);
    }

    public static int getCacheSize() {
        return cacheSize;
    }
}
