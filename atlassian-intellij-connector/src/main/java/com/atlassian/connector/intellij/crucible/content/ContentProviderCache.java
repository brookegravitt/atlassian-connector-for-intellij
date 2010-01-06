package com.atlassian.connector.intellij.crucible.content;
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

import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.util.SimpleLruCache;

/**
 *@author pmaruszak
 * @date Dec 29, 2009
 */
public final class ContentProviderCache  {
    private static int cacheSize = 100;
    private static ContentProviderCache instance;
    private static SimpleLruCache<String, ReviewFileContentProvider> cache;

    private ContentProviderCache(int cacheSize) {
       ContentProviderCache.cacheSize = cacheSize;
        cache = new SimpleLruCache<String, ReviewFileContentProvider>(cacheSize);

    }

    public static ContentProviderCache getInstance() {
        if (instance == null) {
            instance = new ContentProviderCache(cacheSize);
        }
        return instance;
    }

    public static void setCacheSize(int cacheSize) {
        ContentProviderCache.cacheSize = cacheSize;
    }
    
    public void addContentProvider(ReviewFileContentProvider contentProvider) {
        String key = ContentUtil.getKey(contentProvider.getFileInfo().getFileDescriptor());
		String key2 = ContentUtil.getKey(contentProvider.getFileInfo().getOldFileDescriptor());
		if (!"".equals(key)) {
			cache.put(key, contentProvider);
		}
		if (!"".equals(key2)) {
			cache.put(key2, contentProvider);
		}
    }

    public boolean containsProvider(VersionedVirtualFile fileInfo) {
        String key = ContentUtil.getKey(fileInfo);
        return cache.getMap().containsKey(key);
    }

    public ReviewFileContentProvider getContentProvider(VersionedVirtualFile fileInfo) {
        String key = ContentUtil.getKey(fileInfo);
        return cache.getMap().get(key);
    }


    public void clear() {
        cache.getMap().clear();
    }

    public ReviewFileContentProvider get(String fileKey) {
        return cache.get(fileKey);
    }

    public void put(String fileKey, ReviewFileContentProvider provider) {
        cache.getMap().put(fileKey, provider);
    }
}
