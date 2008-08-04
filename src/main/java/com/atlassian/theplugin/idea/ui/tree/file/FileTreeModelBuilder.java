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
import com.atlassian.theplugin.commons.VersionedFileInfo;
import com.atlassian.theplugin.commons.bamboo.BambooChangeSet;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import com.atlassian.theplugin.idea.crucible.comments.CrucibleReviewActionListener;
import com.atlassian.theplugin.idea.crucible.comments.ReviewActionEventBroker;
import com.atlassian.theplugin.idea.crucible.events.FocusOnFileComments;
import com.atlassian.theplugin.idea.crucible.events.FocusOnGeneralComments;
import com.atlassian.theplugin.idea.crucible.events.ShowFileEvent;
import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class FileTreeModelBuilder {

    private FileTreeModelBuilder() {
        // this is a utility class
    }

	public static AtlassianTreeModel buildTreeModelFromBambooChangeSet(Project project, BambooChangeSet changeSet) {
        FileNode root = new FileNode("/", null);
        FileTreeModel model = new FileTreeModel(root);
        for (BambooFileInfo f : changeSet.getFiles()) {
            model.addFile(project, root, f);
        }
        model.compactModel(model.getRoot());
        return model;
    }

	@Nullable
    private static PsiFile guessCorrespondingPsiFile(final Project project, BambooFileInfo file) {
        final PsiFile[] psifiles = PsiManager.getInstance(project).getShortNamesCache().getFilesByName(
                file.getFileDescriptor().getName());
        return CodeNavigationUtil.guessMatchingFile(file.getFileDescriptor().getUrl(), psifiles, project.getBaseDir());
    }

	public static AtlassianTreeModel buildFlatTreeModelFromBambooChangeSet(final Project project, BambooChangeSet changeSet) {
        FileTreeModel model = new FileTreeModel(new FileNode("/", null));
        for (BambooFileInfo f : changeSet.getFiles()) {
            PsiFile psiFile = guessCorrespondingPsiFile(project, f);
            model.getRoot().addChild(new BambooFileNode(f, psiFile));
        }
        return model;
    }


	public static AtlassianTreeModel buildFlatModelFromCrucibleChangeSet(final ReviewData review,
            List<CrucibleFileInfo> files) {
        FileTreeModel model = new FileTreeModel(new CrucibleChangeSetTitleNode(review, new AtlassianClickAction() {
            public void execute(AtlassianTreeNode node, int noOfClicks) {
                switch (noOfClicks) {
                    case 1:
                    case 2:
                        ReviewActionEventBroker broker = IdeaHelper.getReviewActionEventBroker();
                        broker.trigger(new FocusOnGeneralComments(CrucibleReviewActionListener.ANONYMOUS, review));
                        break;
                    default:
                }
            }
        }));
        for (final CrucibleFileInfo file : files) {
            model.getRoot().addChild(new CrucibleFileNode(review, file, new AtlassianClickAction() {
                public void execute(com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode node, int noOfClicks) {
                    ReviewActionEventBroker broker = IdeaHelper.getReviewActionEventBroker();
                    switch (noOfClicks) {
                        case 1:
                            broker.trigger(new FocusOnFileComments(CrucibleReviewActionListener.ANONYMOUS, review, file));
                            break;
                        case 2:
                            broker.trigger(new ShowFileEvent(CrucibleReviewActionListener.ANONYMOUS, review, file));
                            break;
                        default:
                            break;
                    }
                }
            }));
        }
        return model;
    }

	public static AtlassianTreeModel buildTreeModelFromCrucibleChangeSet(final ReviewData review,
			final List<CrucibleFileInfo> files) {
		FileNode root = new CrucibleChangeSetTitleNode(review, new AtlassianClickAction() {
			public void execute(AtlassianTreeNode node, int noOfClicks) {
				switch (noOfClicks) {
					case 1:
					case 2:
						ReviewActionEventBroker broker = IdeaHelper.getReviewActionEventBroker();
						broker.trigger(new FocusOnGeneralComments(CrucibleReviewActionListener.ANONYMOUS, review));
						break;
					default:
				}
			}
		});
		FileTreeModel model = new FileTreeModel(root);
		for (CrucibleFileInfo f : files) {
			model.addFile(root, f, review);
		}
		model.compactModel(model.getRoot());
		return model;
	}

	private static class FileTreeModel extends AtlassianTreeModel {
        public FileTreeModel(FileNode root) {
            super(root);
        }

        @Override
        public FileNode getRoot() {
            return (FileNode) super.getRoot();
        }

        public void addFile(Project project, FileNode root, BambooFileInfo file) {
            PsiFile psiFile = guessCorrespondingPsiFile(project, file);
            FileNode node = createPlace(root, file);
            node.addChild(new BambooFileNode(file, AtlassianClickAction.EMPTY_ACTION, psiFile));
        }

        public void addFile(FileNode root, final CrucibleFileInfo file, final ReviewData review) {
            FileNode node = createPlace(root, file);
            // todo lguminski to avoid creation of a new object for each node
            node.addChild(new CrucibleFileNode(review, file, new AtlassianClickAction() {
                public void execute(com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode node, int noOfClicks) {
                    ReviewActionEventBroker broker = IdeaHelper.getReviewActionEventBroker();
                    switch (noOfClicks) {
                        case 1:
                            broker.trigger(new FocusOnFileComments(CrucibleReviewActionListener.ANONYMOUS, review, file));
                            break;
                        case 2:
                            broker.trigger(new ShowFileEvent(CrucibleReviewActionListener.ANONYMOUS, review, file));
                            break;
                        default:
                            break;
                    }
                }
            }));
        }


        private FileNode createPlace(FileNode root, VersionedFileInfo file) {
            int idx = 0;
            String fileName = file.getFileDescriptor().getUrl();
            FileNode node = root;
            do {
                int newIdx = fileName.indexOf('/', idx);
                if (newIdx != -1) {
                    String newNodeName = fileName.substring(idx, newIdx);
                    if (newNodeName.length() > 0) {
                        if (!node.hasNode(newNodeName)) {
                            FileNode newNode = new FileNode(newNodeName, null);
                            node.addChild(newNode);
                            node = newNode;
                        } else {
                            node = node.getNode(newNodeName);
                        }
                    }
                }
                idx = newIdx + 1;
            } while (idx > 0);
            return node;
        }

        private void compactModel(FileNode node) {
            if (node.isLeaf()) {
                return;
            }

            java.util.List<FileNode> ch = new ArrayList<FileNode>();

            for (FileNode n : node.getChildren().values()) {
                ch.add(n);
            }

            node.removeChildren();

            for (FileNode n : ch) {
                compactModel(n);
                if (n.getChildCount() == 1) {
                    FileNode cn = (FileNode) n.getFirstChild();
                    if (!cn.isLeaf()) {
                        String newName = n.getName() + "/" + cn.getName();
                        cn.setName(newName);
                        node.addChild(cn);
                    } else {
                        node.addChild(n);
                    }
                } else {
                    node.addChild(n);
                }
            }
        }

    }
}
