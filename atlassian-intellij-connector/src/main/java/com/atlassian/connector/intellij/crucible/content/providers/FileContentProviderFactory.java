package com.atlassian.connector.intellij.crucible.content.providers;

import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.connector.intellij.crucible.content.ContentUtil;
import com.atlassian.connector.intellij.crucible.content.ReviewFileContentProvider;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.crucible.CrucibleVcsContentProvider;
import com.atlassian.theplugin.idea.crucible.CrucibleWebContentProvider;
import com.atlassian.theplugin.idea.crucible.CrucibleWebContentProviderForAddedAndDeletedFiles;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.*;

/**
 * @author pmaruszak
 * @date Jan 29, 2010
 */
public final class FileContentProviderFactory {
    private final ConcurrentMap<String, Future<ReviewFileContentProvider>> cache
        = new ConcurrentHashMap<String, Future<ReviewFileContentProvider>>();
    private static FileContentProviderFactory INSTANCE = new FileContentProviderFactory();

    private FileContentProviderFactory() {
    }

    public static FileContentProviderFactory getInstance() {
        return INSTANCE;
    }

     public ReviewFileContentProvider get(final Project project, final VersionedVirtualFile versionedVirtualFile,
                                                 final CrucibleFileInfo reviewItem, final ReviewAdapter review) throws InterruptedException {
        while (true) {
            String key = ContentUtil.getKey(versionedVirtualFile);
            Future<ReviewFileContentProvider> f = cache.get(key);
            if (f == null) {
                Callable<ReviewFileContentProvider> eval = new Callable<ReviewFileContentProvider>() {
                    public ReviewFileContentProvider call() throws InterruptedException {
                        return getContentProvider(project, versionedVirtualFile, reviewItem, review);
                    }
                };
                FutureTask<ReviewFileContentProvider> ft = new FutureTask<ReviewFileContentProvider>(eval);
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
    @Nullable
    private ReviewFileContentProvider getContentProvider(Project project, VersionedVirtualFile versionedVirtualFile,
                                                 CrucibleFileInfo reviewItem, ReviewAdapter review) throws InterruptedException {

        final PsiFile psiFile =  CodeNavigationUtil
                .guessCorrespondingPsiFile(project, versionedVirtualFile.getAbsoluteUrl());

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
                return new CrucibleWebContentProvider(reviewItem, psiFile.getVirtualFile());
            } else {
                return new CrucibleVcsContentProvider(project, reviewItem, psiFile.getVirtualFile());
            }

        } else if (contentUrlAvailable) {
            return new CrucibleWebContentProviderForAddedAndDeletedFiles(reviewItem);
        } else {
            throw new InterruptedException("This Crucible version does not support retrieving uploaded files");
        }

    }
}

