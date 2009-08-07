package com.atlassian.theplugin.idea.crucible.editor;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.Constants;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class UnreadCrucibleGutterIconRenderer extends AbstractCrucibleGutterIconRenderer {

    public UnreadCrucibleGutterIconRenderer(Editor editor, ReviewAdapter review,
                                            CrucibleFileInfo fileInfo, VersionedComment comment) {
        super(editor, review, fileInfo, comment);
    }

    @NotNull
    public Icon getIcon() {
        return Constants.CRUCIBLE_UNREAD_COMMENT_ICON;
    }
}

