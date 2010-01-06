package com.atlassian.connector.intellij.crucible.content;

import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ReviewFileContent;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.crucible.CrucibleVcsContentProvider;
import com.atlassian.theplugin.idea.crucible.CrucibleWebContentProvider;
import com.atlassian.theplugin.idea.crucible.CrucibleWebContentProviderForAddedAndDeletedFiles;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;

/**
 * @author pmaruszak
 * @date Dec 30, 2009
 */
public final class ContentDownloader {
    private ThreadGroup group = new ThreadGroup("Crucible file content download");
    private static ContentDownloader instance = new ContentDownloader();

    private ContentDownloader() {
    }

    @SuppressWarnings("serial")
    private final Map<String, Thread> downloadInProgress = Collections.synchronizedMap(new HashMap<String, Thread>());

    public static ContentDownloader getInstance() {
        return instance;
    }


    private List<CrucibleFileInfo> getFileList(final ReviewAdapter review) {
        List<CrucibleFileInfo> list = new ArrayList<CrucibleFileInfo>();

        try {
            review.getFiles();
        } catch (ValueNotYetInitialized valueNotYetInitialized) {
            try {
                // get details for review (files and comments)
                IntelliJCrucibleServerFacade.getInstance().fillDetailsForReview(review);
            } catch (RemoteApiException e) {
                PluginUtil.getLogger().warn("Error when retrieving review details", e);
                return list;
            } catch (ServerPasswordNotProvidedException e) {
                PluginUtil.getLogger().warn("Missing password exception caught when retrieving review details", e);
                return list;
            }
        }

        try {
            for (CrucibleFileInfo item : review.getFiles()) {
                list.add(item);
            }
            return list;
        } catch (ValueNotYetInitialized valueNotYetInitialized) {
            return list;
        }
    }

    
    public synchronized void downloadFilesContent(@NotNull final Project project, final ReviewAdapter review) {
        downloadFileList(project, review, getFileList(review));
    }

    private ReviewFileContentProvider getContentProvider(@NotNull Project project, @NotNull final CrucibleFileInfo reviewItem,
                                                         @NotNull ReviewAdapter review,
                                                         final VersionedVirtualFile virtualFile) throws VcsException {

        final PsiFile psiFile = CodeNavigationUtil
                .guessCorrespondingPsiFile(project, virtualFile.getAbsoluteUrl());

        boolean contentUrlAvailable = false;
        try {
            contentUrlAvailable = IntelliJCrucibleServerFacade.getInstance().checkContentUrlAvailable(
                    review.getServerData());
        } catch (RemoteApiException e) {
            // unable to get version
        } catch (ServerPasswordNotProvidedException e) {
            // unable to get version
        }

        if (psiFile != null) {
            if (contentUrlAvailable) {
                return new CrucibleWebContentProvider(reviewItem, psiFile.getVirtualFile(), project);
            } else {
                return new CrucibleVcsContentProvider(project, reviewItem, psiFile.getVirtualFile());
            }

        } else if (contentUrlAvailable) {
            return new CrucibleWebContentProviderForAddedAndDeletedFiles(reviewItem, project);
        } else {
            throw new VcsException("This Crucible version does not support retrieving uploaded files");
        }

    }

    public boolean isDownloadInProgress(ReviewAdapter review) {
        for (String key : downloadInProgress.keySet()) {
            if (key.contains(review.getServerData().getUrl())) {
                return true;
            }
        }

        return false;
    }

    public boolean isDownloadInProgress(String key) {
        return downloadInProgress.containsKey(key);
    }

    private void downloadFileList(@NotNull final Project project, final ReviewAdapter review,
                                  final Collection<CrucibleFileInfo> reviewItems) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (CrucibleFileInfo reviewItem : reviewItems) {

                    try {
                        int numberOfFiles = 0;

                        switch (reviewItem.getCommitType()) {
                            case Added:
                                numberOfFiles += downloadSingleFile(project, review, reviewItem,
                                        reviewItem.getFileDescriptor());
                                break;
                            case Deleted:
                                numberOfFiles += downloadSingleFile(project, review, reviewItem,
                                        reviewItem.getOldFileDescriptor());
                                break;
                            case Modified:
                            case Moved:
                            case Copied:
                            case Unknown:
                            default:
                                numberOfFiles += downloadSingleFile(project, review, reviewItem,
                                        reviewItem.getFileDescriptor());
                                numberOfFiles += downloadSingleFile(project, review, reviewItem, 
                                        reviewItem.getOldFileDescriptor());

                        }

                        if (numberOfFiles >= FileContentCache.getCacheSize()) {
                            break;
                        }
                        //impossible or no files
                    } catch (VcsException e) {
                        //not important
                    }


                }
            }
        });
    }
    /**
     * Do not download file if already in cache or download in progress
    * */
    private int downloadSingleFile(@NotNull Project project, final ReviewAdapter review,
                                    final CrucibleFileInfo reviewItem, final VersionedVirtualFile virtualFile)
            throws VcsException {

        final String fileKey = ContentUtil.getKey(virtualFile);

        ReviewFileContentProvider provider = ContentProviderCache.getInstance().get(fileKey);

        if (downloadInProgress.containsKey(fileKey)
                || FileContentCache.getInstance().getFileContent(virtualFile) != null) {
            return 0;
        }

        if (provider == null) {
            provider = getContentProvider(project, reviewItem, review, virtualFile);
            if (provider != null) {
                ContentProviderCache.getInstance().put(fileKey, provider);
            }
            final ReviewFileContentProvider providerFinal = provider;
            //final String niceFileMessage = getNiceFileName(project, reviewItem);
            final Thread thread = new Thread(group, new Runnable() {
                public void run() {
                    if (providerFinal != null) {
                        ReviewFileContent content = null;
                        try {
                            content = providerFinal.getContent(review, virtualFile);
                        } catch (ReviewFileContentException e) {
                            //do nothing if file is not downloaded
                        }

                        FileContentCache.getInstance().put(fileKey, content);

                    }
                    downloadInProgress.remove(fileKey);
                }
            });

            downloadInProgress.put(fileKey, thread);
            thread.start();
        }

        return 1;
    }

    public Thread getDownloadingThread(String key) {
        return downloadInProgress.get(key);
    }

    public Thread getDownloadingThread(ReviewAdapter reviewItem) {
        for (String key : downloadInProgress.keySet()) {
            if (key.contains(reviewItem.getServerData().getUrl())) {
                return downloadInProgress.get(key);
            }
        }
        return null;
    }

    public synchronized ReviewFileContent getFileContent(Project project, ReviewAdapter review, VersionedVirtualFile fileInfo) {
        String key = ContentUtil.getKey(fileInfo);
        if (FileContentCache.getInstance().getFileContent(fileInfo) == null && !isDownloadInProgress(key)) {
            downloadFilesContent(project, review);
        }

        if (isDownloadInProgress(key)) {
            Thread thread = getDownloadingThread(key);
            if (thread != null) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    PluginUtil.getLogger().warn("File download interrupted: " + fileInfo.getAbsoluteUrl());
                }
            }
        }

        return FileContentCache.getInstance().getFileContent(fileInfo);
    }


}

