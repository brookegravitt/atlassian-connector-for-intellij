package com.atlassian.theplugin.idea.action.bitbucket;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.vcs.annotate.AnnotationGutterActionProvider;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import org.jetbrains.annotations.NotNull;

public class GitAnnotationProvider implements AnnotationGutterActionProvider
{

    @NotNull
    public AnAction createAction(final FileAnnotation fileAnnotation)
    {
        return new OpenCommitMessageInHipChat("Talk about this in HipChat");
    }

}
