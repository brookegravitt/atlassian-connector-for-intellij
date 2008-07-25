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

import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jun 18, 2008
 * Time: 6:06:47 PM
 * To change this template use File | Settings | File Templates.
 */
public final class CrucibleHelper {
    //private static Set<OpenFileDescriptor> openDescriptors = new Set<OpenFileDescriptor>();

    private static final Color VERSIONED_COMMENT_BACKGROUND_COLOR = Color.LIGHT_GRAY;
    private static final Color VERSIONED_COMMENT_STRIP_MARK_COLOR = Color.BLUE;

    private CrucibleHelper() {
    }


    public static void showVirtualFileWithComments(final Project project, final CrucibleFileInfo reviewItem,
            final Collection<VersionedComment> fileComments) {

        int line = 1;

        if (!fileComments.isEmpty()) {
            line = fileComments.iterator().next().getFromStartLine();
        }

        VcsIdeaHelper.openFile(project, reviewItem.getFileDescriptor().getAbsoluteUrl(),
                reviewItem.getFileDescriptor().getRevision(), line, 1, new VcsIdeaHelper.OpenFileDescriptorAction() {

            public void run(OpenFileDescriptor ofd) {
                FileEditorManager fem = FileEditorManager.getInstance(project);
                Editor editor = fem.openTextEditor(ofd, true);
                if (editor == null) {
                    return;
                }
                TextAttributes textAttributes = new TextAttributes();
                textAttributes.setBackgroundColor(VERSIONED_COMMENT_BACKGROUND_COLOR);
                highlightCommentsInEditor(project, editor, reviewItem, fileComments, textAttributes);
            }
        });
    }

//    @Nullable
//    private static Editor openFileInEditor(Project project, CrucibleFileInfo reviewItem, int line,
//            VcsIdeaHelper.OpenFileDescriptorAction action) {
//        try {
//            OpenFileDescriptor ofd = openFile(project, reviewItem.getFileDescriptor().getAbsoluteUrl(),
//                    reviewItem.getFileDescriptor().getRevision(), line, 1, action);
//            if (ofd == null) {
//                return null;
//            }
//            FileEditorManager fem = FileEditorManager.getInstance(project);
//            return fem.openTextEditor(ofd, true);
//
//        } catch (VcsException e) {
//            Messages.showErrorDialog(project, "The following error has occured while trying to open "
//                    + reviewItem.getFileDescriptor().getName() + " (rev: " + reviewItem.getFileDescriptor().getRevision()
//                    + "):\n" + e.getMessage(), "Error");
//            return null;
//        }
//    }

    /**
     * Shows virtual file taken from repository in Idea Editor.
     * Higlights all versioned comments for given file
     * Adds StripeMark on the right side of file window with set tool tip text that corresponde
     * to VersionedComment.getMessage content
     *
     * @param project       project
     * @param reviewAdapter adapter
     * @param reviewItem    review item
     */
    public static void showVirtualFileWithComments(Project project, final ReviewData reviewAdapter,
            final CrucibleFileInfo reviewItem) {

        try {
            Collection<VersionedComment> fileComments = CrucibleServerFacadeImpl.getInstance().getVersionedComments(reviewAdapter.getServer(),
                    reviewAdapter.getPermId(), reviewItem.getPermId());
            showVirtualFileWithComments(project, reviewItem, fileComments);
        } catch (RemoteApiException e) {
            PluginUtil.getLogger().error(e.getMessage());
        } catch (ServerPasswordNotProvidedException e) {
            PluginUtil.getLogger().error(e.getMessage());
        }
    }

    private static void highlightCommentsInEditor(Project project, Editor editor, CrucibleFileInfo reviewItem,
            Collection<VersionedComment> fileVersionedComments, TextAttributes textAttribute) {
        Collection<RangeHighlighter> ranges = new ArrayList<RangeHighlighter>();

        if (editor != null) {
            for (VersionedComment comment : fileVersionedComments) {
                //for (int i = comment.getFromStartLine(); i <= comment.getFromEndLine(); i++){
                RangeHighlighter rh = editor.getDocument().getMarkupModel(project).addLineHighlighter(
                        comment.getFromStartLine(), HighlighterLayer.SELECTION, textAttribute);
                rh.setErrorStripeTooltip(reviewItem.getPermId().getId() + ":" + comment.getMessage());
                rh.setErrorStripeMarkColor(VERSIONED_COMMENT_STRIP_MARK_COLOR);

//}
            }
        }
    }
}
