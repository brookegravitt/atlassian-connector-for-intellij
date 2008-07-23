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

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 14, 2008
 * Time: 11:07:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleFileNode extends FileNode {

	private CrucibleFileInfo fileInfo;
    private static final ColoredTreeCellRenderer MY_RENDERER = new CrucibleFileNodeRenderer();

	public CrucibleFileNode(CrucibleFileInfo file, AtlassianClickAction action) {
		super(FilenameUtils.getName(file.getFileDescriptor().getUrl()), action);
		this.fileInfo = file;
    }

	@Override
	public TreeCellRenderer getTreeCellRenderer() {
		return MY_RENDERER;
	}

	public CrucibleFileInfo getFileInfo() {
		return fileInfo;
	}

	private static class CrucibleFileNodeRenderer extends ColoredTreeCellRenderer {
		private static final SimpleTextAttributes TEXT_ITALIC =
				new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, null);
		private static final SimpleTextAttributes RED_ITALIC =
				new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, Color.red);

		public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded,
										  boolean leaf, int row, boolean hasFocus) {
			CrucibleFileNode node = (CrucibleFileNode) value;
			append(node.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

			StringBuilder txt = new StringBuilder();
			txt.append(" (rev: ");
			txt.append(node.getFileInfo().getOldFileDescriptor().getRevision());
			txt.append("-");
			txt.append(node.getFileInfo().getFileDescriptor().getRevision());
			txt.append(")");
			append(txt.toString(), SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
			try {

				int noOfComments = node.getFileInfo().getNumberOfComments();
				if (noOfComments > 0) {
					int noOfDefects = node.getFileInfo().getNumberOfDefects();
					append(" ",
							TEXT_ITALIC);
					append(String.valueOf(noOfComments),
							TEXT_ITALIC);
					append(" comment", TEXT_ITALIC);
					if (noOfComments != 1) {
						append("s", TEXT_ITALIC);
					}

					if (noOfDefects > 0) {
						append(" (", TEXT_ITALIC);
						append(String.valueOf(noOfDefects),
								RED_ITALIC);
						append(" defect",
								RED_ITALIC);
						if (noOfDefects != 1) {
							append("s",
									RED_ITALIC);
						}
						append(")", TEXT_ITALIC);
					}
				}
			} catch (ValueNotYetInitialized valueNotYetInitialized) {
				valueNotYetInitialized.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}

			FileTypeManager mgr = FileTypeManager.getInstance();
			FileType type = mgr.getFileTypeByFileName(node.getName());
			setIcon(type.getIcon());
		}
	}

}
