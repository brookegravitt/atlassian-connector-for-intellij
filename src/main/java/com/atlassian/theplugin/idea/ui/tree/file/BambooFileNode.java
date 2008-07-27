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

package com.atlassian.theplugin.idea.ui.tree.file;

import com.atlassian.theplugin.commons.BambooFileInfo;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.psi.PsiFile;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.vcsUtil.VcsUtil;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;

public class BambooFileNode extends FileNode {

    private BambooFileInfo file;
    private static final TreeCellRenderer MY_RENDERER = new BambooFileNodeRenderer();
    /**
     * null when there is not corresponding PsiFile in currently open project
     */
    private PsiFile psiFile;

    public PsiFile getPsiFile() {
        return psiFile;
    }

    public BambooFileNode(BambooFileInfo file, AtlassianClickAction action, PsiFile psiFile) {
        super(FilenameUtils.getName(file.getFileDescriptor().getUrl()), action);
        this.file = file;
        this.psiFile = psiFile;
    }

    public BambooFileInfo getBambooFileInfo() {
        return file;
    }

    public String getRevision() {
        return file.getFileDescriptor().getRevision();
    }

    @Override
    public TreeCellRenderer getTreeCellRenderer() {
        return MY_RENDERER;
    }

    private static class BambooFileNodeRenderer extends ColoredTreeCellRenderer {

        @Override
        public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
            BambooFileNode node = (BambooFileNode) value;
            append(node.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

            StringBuilder txt = new StringBuilder();
            txt.append(" (rev: ");
            txt.append(node.getRevision());
            txt.append(",");
            append(txt.toString(), SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
            if (node.getPsiFile() != null) {
                try {
                    AbstractVcs vcs = VcsUtil.getVcsFor(IdeaHelper.getCurrentProject(), node.getPsiFile().getVirtualFile());
                    if (vcs == null) {
                        throw new NullPointerException("no VCS info");
                    }
                    String revision = vcs.getDiffProvider().getCurrentRevision(node.getPsiFile().getVirtualFile()).asString();
                    append(" loc rev: " + revision, SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
                } catch (NullPointerException e) {
                    append(e.getMessage(), SimpleTextAttributes.ERROR_ATTRIBUTES);
                }
            } else {
                append(" no corresponding file in the project", SimpleTextAttributes.ERROR_ATTRIBUTES);
            }

            append(")", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
            FileTypeManager mgr = FileTypeManager.getInstance();
            FileType type = mgr.getFileTypeByFileName(node.getName());
            setIcon(type.getIcon());
        }
    }
}
