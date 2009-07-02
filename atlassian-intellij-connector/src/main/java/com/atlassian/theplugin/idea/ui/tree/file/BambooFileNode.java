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
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vfs.VirtualFile;
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

    public BambooFileNode(final BambooFileInfo f, final PsiFile psiFile) {
        this(f, AtlassianClickAction.EMPTY_ACTION, psiFile);
    }

	public BambooFileNode(final BambooFileNode node) {
		this(node.file, node.psiFile);
	}

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

	@Override
	public AtlassianTreeNode getClone() {
		return new BambooFileNode(this);
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
                AbstractVcs vcs = null;
                PsiFile psiFile = node.getPsiFile();
                VirtualFile vFile = null;
                if (psiFile != null) {
                    vFile = psiFile.getVirtualFile();
                    if (vFile != null) {
                        vcs = VcsUtil.getVcsFor(node.getPsiFile().getProject(), vFile);
                    }
                }
                if (vcs == null) {
                    appendNoVcsInfoString();
                } else {
                    DiffProvider diffProvider = vcs.getDiffProvider();
                    if (diffProvider != null) {
                        VcsRevisionNumber currentRevision = diffProvider.getCurrentRevision(vFile);
                        String revisionString = currentRevision != null ? currentRevision.asString() : "Unknown";
                        append(" loc rev: " + revisionString, SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
                    } else {
                        appendNoVcsInfoString();
                    }
                }
            } else {
                append(" no corresponding file in the project", SimpleTextAttributes.ERROR_ATTRIBUTES);
            }

            append(")", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
            FileTypeManager mgr = FileTypeManager.getInstance();
            FileType type = mgr.getFileTypeByFileName(node.getName());
            setIcon(type.getIcon());
        }

        private void appendNoVcsInfoString() {
            append("No VCS Info", SimpleTextAttributes.ERROR_ATTRIBUTES);
        }
    }
}
