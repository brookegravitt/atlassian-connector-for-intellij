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
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @author pmaruszak
 * @date Jan 29, 2010
 */
public final class FileContentProviderProxy implements ProjectComponent {
    private final ConcurrentMap<String, Future<ReviewFileContentProvider>> cache
        = new ConcurrentHashMap<String, Future<ReviewFileContentProvider>>();
    private final Project project;

    public FileContentProviderProxy(Project project) {
        this.project = project;
    }

    public ReviewFileContentProvider get(final VersionedVirtualFile versionedVirtualFile,
                                                 final CrucibleFileInfo reviewItem,
                                                 final ReviewAdapter review) throws InterruptedException {
        while (true) {
            String key = ContentUtil.getKey(versionedVirtualFile);
            Future<ReviewFileContentProvider> f = cache.get(key);
            if (f == null) {
                Callable<ReviewFileContentProvider> eval = new Callable<ReviewFileContentProvider>() {
                    public ReviewFileContentProvider call() throws InterruptedException {
                        return getContentProvider(versionedVirtualFile, reviewItem, review);
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
    private ReviewFileContentProvider getContentProvider(VersionedVirtualFile versionedVirtualFile,
                                                         CrucibleFileInfo reviewItem,
                                                         ReviewAdapter review) throws InterruptedException {

        GuessFileRunnable runnable = new GuessFileRunnable(project, versionedVirtualFile);
        try {
            EventQueue.invokeAndWait(runnable);
        } catch (InvocationTargetException e) {
            PluginUtil.getLogger().error("Cannot guess file ", e);
        }

        final PsiFile psiFile =  runnable.getPsiFile();
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

    public void projectOpened() {

    }

    public void projectClosed() {
        for (String key : cache.keySet()) {
            cache.get(key).cancel(true);
        }
    }

    @NotNull
    public String getComponentName() {
        return FileContentProviderProxy.class.getName();
    }

    public void initComponent() {

    }

    public void disposeComponent() {

    }

    public void clear() {
        cache.clear();        
    }

    class GuessFileRunnable implements Runnable {
        private PsiFile psiFile = null;
        private final Project project;
        private final VersionedVirtualFile versionedVirtualFile;

        GuessFileRunnable(Project project, VersionedVirtualFile versionedVirtualFile) {
            this.project = project;
            this.versionedVirtualFile = versionedVirtualFile;
        }

        public void run() {
            psiFile =  CodeNavigationUtil
                           .guessCorrespondingPsiFile(project, versionedVirtualFile.getAbsoluteUrl());

        }

        public PsiFile getPsiFile() {
            return psiFile;
        }
    }
}

