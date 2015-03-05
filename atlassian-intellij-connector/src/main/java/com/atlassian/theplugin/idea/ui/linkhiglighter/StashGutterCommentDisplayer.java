package com.atlassian.theplugin.idea.ui.linkhiglighter;

import com.atlassian.connector.intellij.stash.*;
import com.atlassian.theplugin.idea.jira.IssueCommentDialog;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.impl.ApplicationImpl;
import com.intellij.openapi.components.ComponentManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.TextAnnotationGutterProvider;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

/**
 * Created by klopacinski on 2015-03-05.
 */
public class StashGutterCommentDisplayer {
    private Project project;
    private VirtualFile virtualFile;
    private PsiFile psiFile;
    private Editor editor;

    public StashGutterCommentDisplayer(@NotNull final Project project, final VirtualFile newFile,
                                       final PsiFile psiFile,
                                       final Editor editor) {
        this.project = project;
        this.virtualFile = newFile;
        this.psiFile = psiFile;
        this.editor = editor;

        registerTextAnnotationProvider(psiFile, editor);
    }

    private void registerTextAnnotationProvider(final PsiFile psiFile, Editor editor) {
        final StashArrayListBackedServerFacade stashFacade = StashArrayListBackedServerFacade.getInstance();

        final List<Comment> comments;
        try {
            comments = stashFacade.getComments(null, getRelativePath(psiFile));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        editor.getGutter().registerTextAnnotation(new TextAnnotationGutterProvider() {

            @Nullable
            public String getLineText(final int i, Editor editor) {
                return Joiner.on(", ").skipNulls().join(Iterables.transform(comments, new Function<Comment, Object>() {
                    public Object apply(@Nullable Comment comment) {
                        return comment.getAnchor().getLine() == i + 1 ? comment.getAuthor().getName() : null;
                    }
                }));
            }

            @Nullable
            public String getToolTip(final int i, Editor editor) {
                return Joiner.on("\n\n").skipNulls().join(Iterables.transform(comments, new Function<Comment, Object>() {
                    public Object apply(@Nullable Comment comment) {
                        return comment.getAnchor().getLine() == i + 1 ? comment.getText() : null;
                    }
                }));
            }

            public EditorFontType getStyle(int i, Editor editor) {
                return EditorFontType.BOLD;
            }

            @Nullable
            public ColorKey getColor(int i, Editor editor) {
                return ColorKey.createColorKey("", Color.black);
            }

            @Nullable
            public Color getBgColor(int i, Editor editor) {
                return Color.LIGHT_GRAY;
            }

            public List<AnAction> getPopupActions(final int i, final Editor editor) {
                List<AnAction> l = new ArrayList<AnAction>();
                l.add(new AnAction("an action!") {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                        final IssueCommentDialog issueCommentDialog = new IssueCommentDialog("line " + Integer.toString(i + 1));
                        issueCommentDialog.show();
                        if (issueCommentDialog.isOK()) {
                            Task.Backgroundable task = new Task.Backgroundable(project, "Adding comment to Stash", false) {

                                public void run(@NotNull ProgressIndicator progressIndicator) {
                                    Comment comment = new SimpleComment();
                                    try {
                                        stashFacade.addComment(new SimpleComment(issueCommentDialog.getComment(), "zbysiu", getRelativePath(psiFile), i + 1));
                                        reparseAll();
                                    } catch (URISyntaxException e) {
                                        e.printStackTrace();
                                        throw new RuntimeException(e);
                                    }
                                }
                            };

                            ProgressManager.getInstance().run(task);
                        }
                    }

                });
                return l;
            }

            public void gutterClosed() {

            }
        });
    }

    private String getRelativePath(PsiFile psiFile) throws URISyntaxException {
        return new URI(psiFile.getProject().getBaseDir().getUrl()).relativize(new URI(psiFile.getVirtualFile().getUrl())).getPath();
    }

    public void reparseAll() {

        ApplicationManager.getApplication().runReadAction(new Runnable() {
            public void run() {
                editor.getGutter().closeAllAnnotations();
                registerTextAnnotationProvider(psiFile, editor);
            }
        });
    }
}
