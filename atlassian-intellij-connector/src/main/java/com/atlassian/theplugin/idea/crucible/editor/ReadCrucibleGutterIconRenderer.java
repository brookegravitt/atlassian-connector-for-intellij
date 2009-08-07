package com.atlassian.theplugin.idea.crucible.editor;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.crucible.CommentDateUtil;
import com.atlassian.theplugin.idea.crucible.CommentTooltipPanel;
import com.atlassian.theplugin.idea.crucible.CommentTooltipPanelWithRunners;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ReadCrucibleGutterIconRenderer extends AbstractCrucibleGutterIconRenderer {

    public ReadCrucibleGutterIconRenderer(Editor editor, ReviewAdapter review,
                                            CrucibleFileInfo fileInfo, VersionedComment comment) {
        super(editor, review, fileInfo, comment);
    }

    @NotNull
    public Icon getIcon() {
        return Constants.CRUCIBLE_REVIEW_PANEL_ICON;
    }
}