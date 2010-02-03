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

package com.atlassian.theplugin.idea.crucible;

import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.connector.intellij.crucible.content.FileContentExpiringCache;
import com.atlassian.connector.intellij.crucible.content.ReviewFileContentException;
import com.atlassian.connector.intellij.crucible.content.providers.FileContentProviderFactory;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.RepositoryType;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.idea.crucible.editor.CommentHighlighter;
import com.atlassian.theplugin.idea.crucible.editor.OpenCrucibleDiffToolAction;
import com.atlassian.theplugin.idea.crucible.editor.OpenDiffAction;
import com.atlassian.theplugin.idea.crucible.editor.OpenEditorDiffActionImpl;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.vcsUtil.VcsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

public final class CrucibleHelper {
    private CrucibleHelper() {
    }

    /**
     * Shows virtual file taken from repository in Idea Editor.
     * Higlights all versioned comments for given file
     * Adds StripeMark on the right side of file window with set tool tip text that corresponde
     * to VersionedComment.getMessage content
     * Note: must be run from event dispatch thread or inside read-action only!
     *
     * @param project    project
     * @param review     review data
     * @param reviewItem review item                                                                                         aaa
     */
    public static void showVirtualFileWithComments(final Project project,
                                                   final ReviewAdapter review,
                                                   final CrucibleFileInfo reviewItem) {
        openFileWithDiffs(project
                , true
                , review
                , reviewItem
                , 1
                , 1
                , new OpenEditorDiffActionImpl(project, review, reviewItem, true));
    }

    public static void showRevisionDiff(final Project project, final ReviewAdapter review, final CrucibleFileInfo reviewItem) {
        openFileWithDiffs(project
                , true
                , review
                , reviewItem
                , 1
                , 1
                , new OpenCrucibleDiffToolAction(project, review, reviewItem));
    }

    public static void openFileOnComment(final Project project, final ReviewAdapter review, final CrucibleFileInfo file,
                                         final VersionedComment comment) {

        ApplicationManager.getApplication().runReadAction(new Runnable() {
            public void run() {
                int line;
                switch (file.getCommitType()) {
                    case Deleted:
                        line = comment.getFromStartLine() - 1;
                        break;
                    default:
                        line = comment.getToStartLine() - 1;
                        break;
                }

                openFileWithDiffs(project
                        , true
                        , review
                        , file
                        , comment.getToStartLine()
                        , 0
                        , new OpenEditorDiffActionImpl(project, review, file, line, 0, true)
                );
            }
        });
    }

    // CHECKSTYLE:OFF

    public static void openFileWithDiffs(final Project project, final boolean modal,
                                         @NotNull final ReviewAdapter review, @NotNull final CrucibleFileInfo reviewItem,
                                         final int line, final int col, @Nullable final OpenDiffAction action) {
        // CHECKSTYLE:ON

        if (reviewItem.getRepositoryType() == RepositoryType.PATCH) {
            Messages.showErrorDialog(project, "Reviews based on patch upload are not supported", "Review not supported");
            return;
        }

        final PsiFile psiFile = CodeNavigationUtil
                .guessCorrespondingPsiFile(project, reviewItem.getFileDescriptor().getAbsoluteUrl());
        if (psiFile != null) {
            final VirtualFile vfl = psiFile.getVirtualFile();
            if (vfl != null) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        fetchAndOpenFileWithDiffs(project, modal, review, reviewItem, vfl, line, col, action);
                    }
                });
                return;
            }
        } else if (reviewItem.getRepositoryType() == RepositoryType.UPLOAD) {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                public void run() {
                    try {
                        fetchAndOpenAddedorDeletedFileWithDiffs(project, modal, review, reviewItem, line, col, action);
                    } catch (final VcsException e) {
                        ApplicationManager.getApplication().invokeLater(new Runnable() {
                            public void run() {
                                Messages.showErrorDialog(project, "Unable to retrieve the file "
                                        + reviewItem.getFileDescriptor().getAbsoluteUrl() + ": " + e.getMessage(),
                                        PluginUtil.PRODUCT_NAME);
                            }
                        });
                    }
                }
            });
            return;
        }

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                Messages.showErrorDialog(project,
                        "Your project does not contain requested file "
                                + reviewItem.getFileDescriptor().getAbsoluteUrl() + "."
                        , PluginUtil.PRODUCT_NAME);
            }
        });
    }

    public static Editor getEditorForCrucibleFile(Project project, ReviewAdapter review, CrucibleFileInfo file) {
        Editor[] editors = EditorFactory.getInstance().getAllEditors();
        for (Editor editor : editors) {
            if (!project.equals(editor.getProject())) {
                continue;
            }
            final Document document = editor.getDocument();
            final VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
            if (virtualFile != null) {
                final ReviewAdapter mr = virtualFile.getUserData(CommentHighlighter.REVIEW_DATA_KEY);
                final CrucibleFileInfo mf = virtualFile.getUserData(CommentHighlighter.REVIEWITEM_DATA_KEY);
                if (mr != null && mf != null) {
                    if (review.getPermId().equals(mr.getPermId()) && file.getPermId().equals(mf.getPermId())) {
                        return editor;
                    }
                }
            }
        }
        return null;
    }

    public static Collection<UploadItem> getUploadItemsFromChanges(final Project project, final Collection<Change> changes) {
        Collection<UploadItem> uploadItems = new ArrayList<UploadItem>();
        for (Change change : changes) {
            try {
                ContentRevision contentRevision;
                if (change.getBeforeRevision() != null) {
                    contentRevision = change.getBeforeRevision();
                } else {
                    //for added but not committed files after revision is available only
                    contentRevision = change.getAfterRevision();
                }
                // PL-1619
                if (contentRevision == null) {
                    continue;
                }

                String fileUrl = null;
                VirtualFile file = contentRevision.getFile().getVirtualFile();
                if (file == null) {
                    file = contentRevision.getFile().getVirtualFileParent();
                    if (file == null) {
                        continue;
                    }
                    fileUrl = VcsIdeaHelper.getRepositoryUrlForFile(project, file);
                    if (fileUrl != null) {
                        fileUrl = fileUrl + "/" + contentRevision.getFile().getName();
                    }
                } else {
                    fileUrl = VcsIdeaHelper.getRepositoryUrlForFile(project, file);
                }

                try {
                    URL url = new URL(fileUrl);
                    fileUrl = url.getPath();
                } catch (MalformedURLException e) {
                    String rootUrl = VcsIdeaHelper
                            .getRepositoryRootUrlForFile(project, contentRevision.getFile().getVirtualFile());
                    fileUrl = StringUtils.difference(rootUrl, fileUrl);
                }

                ContentRevision revOld = change.getBeforeRevision();
                ContentRevision revNew = change.getAfterRevision();

                byte[] byteOld = revOld != null && revOld.getContent() != null
                        ? revOld.getContent().getBytes() : null;
                byte[] byteNew = revNew != null && revNew.getContent() != null
                        ? revNew.getContent().getBytes() : null;

                // @todo implement it handling of binary files
                uploadItems.add(new UploadItem(fileUrl, byteOld, byteNew));

            } catch (VcsException e) {
                throw new RuntimeException(e);
            }
        }
        return uploadItems;
    }

    /**
     * Must be run from UI thread!
     *
     * @param project          project
     * @param modal            modal window or task in status bar
     * @param review           review
     * @param crucibleFileInfo review file
     * @param virtualFile      file to fetch from VCS
     * @param line             line to go to
     * @param column           column to go to
     * @param action           action to execute upon successful completion of the fetching
     */
    // CHECKSTYLE:OFF
    private static void fetchAndOpenFileWithDiffs(final Project project, final boolean modal,
                                                  @NotNull final ReviewAdapter review, @NotNull final CrucibleFileInfo crucibleFileInfo,
                                                  @NotNull final VirtualFile virtualFile,
                                                  final int line, final int column, @Nullable final OpenDiffAction action) {
        // CHECKSTYLE:ON

        final String fromRevision = crucibleFileInfo.getOldFileDescriptor().getRevision();
        final String toRevision = crucibleFileInfo.getFileDescriptor().getRevision();

        boolean contentUrlAvailable = false;
        try {
            contentUrlAvailable = IntelliJCrucibleServerFacade.getInstance().checkContentUrlAvailable(
                    review.getServerData());
        } catch (RemoteApiException e) {
            // unable to get version
        } catch (ServerPasswordNotProvidedException e) {
            // unable to get version
        }

        String sourceName = contentUrlAvailable ? "Crucible" : "VCS";
        String niceFileMessage;
        switch (crucibleFileInfo.getCommitType()) {
            case Added:
                niceFileMessage = " " + virtualFile.getName() + " (rev: " + toRevision + ") from " + sourceName;
                break;
            case Deleted:
                niceFileMessage = " " + virtualFile.getName() + " (rev: " + fromRevision + ") from " + sourceName;
                break;
            case Modified:
            case Moved:
            case Copied:
            case Unknown:
            default:
                niceFileMessage = "s " + virtualFile.getName() + " (rev: ";
                if (!StringUtils.isEmpty(fromRevision)) {
                    niceFileMessage += fromRevision;
                    if (!StringUtils.isEmpty(toRevision)) {
                        niceFileMessage += ", ";
                    }
                }
                if (!StringUtils.isEmpty(toRevision)) {
                    niceFileMessage += " " + toRevision;
                }
                niceFileMessage += ") from " + sourceName;
                break;
        }

        if (contentUrlAvailable) {
            review.addContentProvider(new CrucibleWebContentProvider(crucibleFileInfo, virtualFile));
        } else {
            review.addContentProvider(new CrucibleVcsContentProvider(project, crucibleFileInfo, virtualFile));
        }

        new FetchingTwoFilesTask(project, modal, niceFileMessage, review, crucibleFileInfo, line, column, action)
                .queue();
    }

    private static void fetchAndOpenAddedorDeletedFileWithDiffs(final Project project, final boolean modal,
                                                                @NotNull final ReviewAdapter review,
                                                                @NotNull final CrucibleFileInfo reviewItem,
                                                                final int line, final int col,
                                                                @Nullable final OpenDiffAction action) throws VcsException {

        final String fromRevision = reviewItem.getOldFileDescriptor().getRevision();
        final String toRevision = reviewItem.getFileDescriptor().getRevision();

        boolean contentUrlAvailable = false;
        try {
            contentUrlAvailable = IntelliJCrucibleServerFacade.getInstance().checkContentUrlAvailable(
                    review.getServerData());
        } catch (RemoteApiException e) {
            // unable to get version
        } catch (ServerPasswordNotProvidedException e) {
            // unable to get version
        }

        String sourceName = contentUrlAvailable ? "Crucible" : "VCS";
        String niceFileMessage;
        switch (reviewItem.getCommitType()) {
            case Added:
                niceFileMessage = " " + reviewItem.getFileDescriptor().getName()
                        + " (rev: " + toRevision + ") from " + sourceName;
                break;
            case Deleted:
                niceFileMessage = " " + reviewItem.getFileDescriptor().getName()
                        + " (rev: " + fromRevision + ") from " + sourceName;
                break;
            case Modified:
            case Moved:
            case Copied:
            case Unknown:
            default:
                niceFileMessage = "s " + reviewItem.getFileDescriptor().getName() + " (rev: ";
                if (!StringUtils.isEmpty(fromRevision)) {
                    niceFileMessage += fromRevision;
                    if (!StringUtils.isEmpty(toRevision)) {
                        niceFileMessage += ", ";
                    }
                }
                if (!StringUtils.isEmpty(toRevision)) {
                    niceFileMessage += " " + toRevision;
                }
                niceFileMessage += ") from " + sourceName;
                break;
        }

        if (contentUrlAvailable) {
            review.addContentProvider(new CrucibleWebContentProviderForAddedAndDeletedFiles(reviewItem));
        } else {
            throw new VcsException("This Crucible version does not support retrieving uploaded files");
        }

        new FetchingTwoFilesTask(project, modal, niceFileMessage, review, reviewItem, line, col, action).queue();
    }

    public static void selectVersionedComment(final Project project, final ReviewAdapter review,
                                              final CrucibleFileInfo file, final Comment comment) {
        // select comment
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                IdeaHelper.getReviewDetailsToolWindow(project).selectVersionedComment(review, file, comment);
            }
        });
    }

    public static void selectGeneralComment(final Project project, final ReviewAdapter review, final Comment comment) {
        // select comment
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                IdeaHelper.getReviewDetailsToolWindow(project).selectGeneralComment(review, comment);
            }
        });
    }

    public static void selectFile(final Project project, final ReviewAdapter review, final CrucibleFileInfo file) {
        // select comment
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                IdeaHelper.getReviewDetailsToolWindow(project).selectFile(review, file);
            }
        });
    }

    private static class FetchingTwoFilesTask extends Task.Backgroundable {
        private OpenFileDescriptor displayDescriptor;
        private VirtualFile referenceVirtualFile;

        private ReviewFileContentException exception;
        private final Project project;
        private final String niceFileMessage;
        private final ReviewAdapter review;
        private final CrucibleFileInfo crucibleFileInfo;
        private final VersionedVirtualFile oldFile;
        private final VersionedVirtualFile newFile;
        private final int line;
        private final int column;
        private final OpenDiffAction action;
        private final boolean modal;

        public FetchingTwoFilesTask(final Project project, final boolean modal, final String niceFileMessage,
                                    final ReviewAdapter review,
                                    final CrucibleFileInfo crucibleFileInfo, final int line, final int column,
                                    final OpenDiffAction action) {
            super(project, "Fetching file" + niceFileMessage, false);
            this.project = project;
            this.niceFileMessage = niceFileMessage;
            this.review = review;
            this.crucibleFileInfo = crucibleFileInfo;
            this.oldFile = crucibleFileInfo.getOldFileDescriptor();
            this.newFile = crucibleFileInfo.getFileDescriptor();
            this.line = line;
            this.column = column;
            this.action = action;
            this.modal = modal;
        }

        @Override
        public boolean shouldStartInBackground() {
            return !modal;
        }

        @Override
        public void run(@NotNull ProgressIndicator indicator) {
            indicator.setIndeterminate(false);
            VirtualFile displayVirtualFile = null;
            try {
                switch (crucibleFileInfo.getCommitType()) {
                    case Modified:
                    case Moved:
                    case Copied:
                        if (!StringUtils.isEmpty(oldFile.getRevision())) {
                            referenceVirtualFile = getVirtualFile(oldFile);
                        }
                        displayVirtualFile = getVirtualFile(newFile);
                        break;
                    case Added:
                        displayVirtualFile = getVirtualFile(newFile);
                        break;
                    case Deleted:
                        referenceVirtualFile = getVirtualFile(oldFile);
                        break;
                    default:
                        break;
                }
                if (displayVirtualFile != null) {
                    displayDescriptor = new OpenFileDescriptor(project, displayVirtualFile, line - 1, column);
                }
            } catch (ReviewFileContentException e) {
                exception = e;
            }
        }

        private VirtualFile getVirtualFile(VersionedVirtualFile fileInfo) throws ReviewFileContentException {

            // check if requested file is already opened
            // doesn't haeve to be in cache :)
            VirtualFile virtualFile = getEditorForRevisionFile(fileInfo);
            if (virtualFile != null) {
                return virtualFile;
            }

            IdeaReviewFileContentProvider provider = (IdeaReviewFileContentProvider) review.getContentProvider(fileInfo);
            IdeaReviewFileContent content = null;
            try {
                if (provider == null) {
                    provider = (IdeaReviewFileContentProvider) FileContentProviderFactory.getInstance().
                            get(project, fileInfo, crucibleFileInfo, review);
                }

                content =
                        (IdeaReviewFileContent) FileContentExpiringCache.getInstance().get(fileInfo, provider, review);

            } catch (Exception e) {
                throw new ReviewFileContentException(e);
            }


            //get local file if not dirty and has the same revision as remote
            if (provider != null && content != null /*&& !provider.isLocalFileDirty()*/
                    && provider.isContentIdentical(content.getContent())) {
                VirtualFile providerVirtualFile = provider.getVirtualFile();
                providerVirtualFile.putUserData(CommentHighlighter.REVIEW_FILE_URL, fileInfo.getAbsoluteUrl());
                providerVirtualFile.putUserData(CommentHighlighter.REVIEW_FILE_REVISION, fileInfo.getRevision());
                return providerVirtualFile;
            }

            if (content != null) {
                virtualFile = content.getVirtualFile();
                virtualFile.putUserData(CommentHighlighter.REVIEW_FILE_URL, fileInfo.getAbsoluteUrl());
                virtualFile.putUserData(CommentHighlighter.REVIEW_FILE_REVISION, fileInfo.getRevision());

                return virtualFile;
            } else {
                return null;
            }
        }

        //get non dirty file and priority has local file than remote

        private VirtualFile getEditorForRevisionFile(VersionedVirtualFile fileInfo) {
            VirtualFile foundFile = null;
            Editor[] editors = EditorFactory.getInstance().getAllEditors();
            for (Editor editor : editors) {
                if (!this.project.equals(editor.getProject())) {
                    continue;
                }

                final Document document = editor.getDocument();
                final VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
                if (virtualFile != null) {
                    final String fileUrl = virtualFile.getUserData(CommentHighlighter.REVIEW_FILE_URL);
                    final String fileRevision = virtualFile.getUserData(CommentHighlighter.REVIEW_FILE_REVISION);
                    if (fileUrl != null && fileRevision != null) {
                        if (fileInfo.getAbsoluteUrl().equals(fileUrl) && fileInfo.getRevision().equals(fileRevision)
                                && !FileDocumentManager.getInstance().isFileModified(virtualFile)) {

                            if (VcsUtil.getVcsFor(this.project, virtualFile) == null || foundFile == null) {
                                foundFile = virtualFile;
                            }
                        }
                    }
                }
            }
            return foundFile;
        }


        @Override
        public void onSuccess() {
            if (exception != null) {
                DialogWithDetails.showExceptionDialog(project, "The following error has occured while fetching file"
                        + niceFileMessage + ":\n" + exception.getMessage(), exception);
                return;
            }
            if (action != null) {
                action.run(displayDescriptor, referenceVirtualFile, crucibleFileInfo.getCommitType());
            }
        }
    }


    @Nullable
    public static String getCommentUrl(Project project, PsiElement psiElement) {
        VirtualFile vf = psiElement.getContainingFile().getVirtualFile();

        Document doc;
        if (vf != null) {
            doc = FileDocumentManager.getInstance().getDocument(vf);

            // binary file
            if (doc == null) {
                ReviewAdapter review = vf.getUserData(CommentHighlighter.REVIEW_DATA_KEY);
                return getVersionedFileUrl(review, vf.getUserData(CommentHighlighter.REVIEWITEM_DATA_KEY));
            }

            RangeHighlighter[] highlighters = doc.getMarkupModel(project).getAllHighlighters();

            Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            if (editor != null) {
                ReviewAdapter review = vf.getUserData(CommentHighlighter.REVIEW_DATA_KEY);

                int offset = editor.getCaretModel().getOffset();

                for (RangeHighlighter highlighter : highlighters) {
                    if (highlighter.getUserData(CommentHighlighter.COMMENT_DATA_KEY) != null) {

                        int start = highlighter.getStartOffset();
                        int end = highlighter.getEndOffset() + 1;


                        if (offset >= start && offset <= end) {
                            VersionedComment comment = highlighter.getUserData(CommentHighlighter.VERSIONED_COMMENT_DATA_KEY);
                            return getCommentUrl(review, comment);
                        }
                    }
                }

                return getVersionedFileUrl(review, vf.getUserData(CommentHighlighter.REVIEWITEM_DATA_KEY));
            }
        }
        return null;
    }

    @Nullable
    public static String getCommentUrl(final ReviewAdapter review, final Comment comment) {
        if (review != null && comment != null && comment.getPermId() != null) {
            String[] permTokens = comment.getPermId().getId().split(":");
            if (permTokens.length == 2) {
                return review.getServerData().getUrl() + "/cru/"
                        + review.getPermId().getId() + "/#c" + permTokens[1];
            }
        }
        return null;
    }

    public static String getVersionedFileUrl(final ReviewAdapter review, final CrucibleFileInfo file) {
        if (review != null && file != null && file.getPermId() != null) {
            return review.getServerData().getUrl() + "/cru/"
                    + review.getPermId().getId() + "/viewfile/" + file.getPermId().getId();
		}
		return null;
	}
}
