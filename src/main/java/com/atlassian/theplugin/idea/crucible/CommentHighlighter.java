package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;

import java.awt.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: marek
 * Date: Jul 30, 2008
 * Time: 1:57:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class CommentHighlighter {
    private static final Color VERSIONED_COMMENT_BACKGROUND_COLOR = Color.LIGHT_GRAY;
    private static final Color VERSIONED_COMMENT_STRIP_MARK_COLOR = Color.ORANGE;

    public static void highlightCommentsInEditor(Project project, Editor editor, List<VersionedComment> fileVersionedComments) {
        if (editor != null) {
            TextAttributes textAttributes = new TextAttributes();
            textAttributes.setBackgroundColor(VERSIONED_COMMENT_BACKGROUND_COLOR);
            for (VersionedComment comment : fileVersionedComments) {
                if (comment.getToStartLine() > 0 && comment.getToEndLine() > 0) {
                    for (int i = comment.getToStartLine() - 1; i < comment.getToEndLine(); i++) {
                        RangeHighlighter rh = editor.getDocument().getMarkupModel(project).addLineHighlighter(
                                i, HighlighterLayer.SELECTION, textAttributes);

                        rh.setErrorStripeTooltip(comment.getAuthor().getDisplayName() + ":" + comment.getMessage());
                        rh.setErrorStripeMarkColor(VERSIONED_COMMENT_STRIP_MARK_COLOR);
                    }
                }
            }
        }
    }
}