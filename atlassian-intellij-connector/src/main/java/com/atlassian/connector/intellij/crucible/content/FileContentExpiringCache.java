package com.atlassian.connector.intellij.crucible.content;

import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ReviewFileContent;
import com.atlassian.theplugin.util.PluginUtil;

/**
 * @user pmaruszak
 */
public final class FileContentExpiringCache extends ExpiringCache<String, ReviewFileContent> {
    private final static FileContentExpiringCache INSTANCE = new FileContentExpiringCache();

    private FileContentExpiringCache() {
    }


    public static FileContentExpiringCache getInstance() {
        return INSTANCE;         
    }

    public ReviewFileContent recover(VersionedVirtualFile virtualFile) {
        String key = ContentUtil.getKey(virtualFile);
        return recover(key);
    }


    @Override
    public ReviewFileContent recover(String key) {
          if (ContentDownloader.getInstance().isDownloadInProgress(key)) {
            Thread downloadingThread = ContentDownloader.getInstance().getDownloadingThread(key);
            if (downloadingThread != null) {
                try {
                    //wait for thread to finish downloading
                    downloadingThread.join();
                } catch (InterruptedException e) {
                    PluginUtil.getLogger().warn("Downloading file interrupted :" + key);
                }
            }
        }
        return super.recover(key);
    }
}
