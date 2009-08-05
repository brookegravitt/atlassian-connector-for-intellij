package com.atlassian.theplugin.idea.action.crucible.comment;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.api.model.CommitType;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CrucibleHelper;
import com.atlassian.theplugin.idea.crucible.editor.ChangeViewer;
import com.atlassian.theplugin.idea.crucible.editor.CommentHighlighter;
import com.atlassian.theplugin.idea.crucible.editor.CrucibleDiffGutterRenderer;
import com.atlassian.theplugin.idea.crucible.editor.OpenEditorDiffActionImpl;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTree;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleFileNode;
import com.atlassian.theplugin.util.CodeNavigationUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ex.Range;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.Collection;

/**
 * User: jgorycki
 * Date: Mar 4, 2009
 * Time: 2:22:05 PM
 */
public abstract class AbstractDiffNavigationAction extends AbstractCommentAction {

	protected void selectNode(AtlassianTree tree, CrucibleFileNode node) {
		TreePath path = new TreePath(node.getPath());
		tree.scrollPathToVisible(path);
		tree.setSelectionPath(path);
	}

	protected void openFileNode(AnActionEvent e, CrucibleFileNode node, boolean goToLast) {
		Project p = IdeaHelper.getCurrentProject(e);
		if (p != null) {
			ReviewAdapter r = node.getReview();
			CrucibleFileInfo f = node.getFile();
			CrucibleHelper.openFileWithDiffs(p, true, r, f, 1, 0,
					goToLast ? new OpenEditorAndMoveToLastDiff(node, p, r, f, true)
							: new OpenEditorAndMoveToFirstDiff(node, p, r, f, true));
		}
	}

	protected Editor getEditorForNode(CrucibleFileNode node) {
		if (node == null) {
			return null;
		}

		String ourUrl = node.getFile().getFileDescriptor().getAbsoluteUrl();

		for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
			Document document = editor.getDocument();

			final Project project = editor.getProject();
			if (project == null) {
				continue;
			}
			PsiFile psi = CodeNavigationUtil.guessCorrespondingPsiFile(project, ourUrl);

			VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
			if (virtualFile != null) {
				if (psi != null) {
					final String fileUrl = virtualFile.getUserData(CommentHighlighter.REVIEW_FILE_URL);
					final String fileRevision = virtualFile.getUserData(CommentHighlighter.REVIEW_FILE_REVISION);
					if (fileUrl != null && fileRevision != null) {
						VersionedVirtualFile file = node.getFile().getFileDescriptor();
						if (fileUrl.equals(file.getAbsoluteUrl()) && fileRevision.equals(file.getRevision())) {
							return editor;
						}
					}
				}
			}
		}
		return null;
	}

	protected CrucibleFileNode getNodeForFile(AnActionEvent e, VirtualFile file) {
		if (file == null) {
			return null;
		}
		CrucibleFileNode node = null;
		do {
			node = getNextFileNode((AtlassianTree) getTree(e), node, false);
			if (node != null) {
				String nodeUrl = node.getFile().getFileDescriptor().getAbsoluteUrl();
				PsiFile psi = CodeNavigationUtil.guessCorrespondingPsiFile(IdeaHelper.getCurrentProject(e), nodeUrl);
				if (psi != null) {
					return node;
				}
			} else {
				return null;
			}
		} while (true);
	}

	protected Range getFirstRange(Editor editor) {
		if (editor == null) {
			return null;
		}
		Collection<RangeHighlighter> ranges = editor.getDocument().getUserData(ChangeViewer.CRUCIBLE_RANGES);
		if (ranges != null) {
			for (RangeHighlighter rangeHighlighter : ranges) {
				CrucibleDiffGutterRenderer renderer
						= (CrucibleDiffGutterRenderer) rangeHighlighter.getLineMarkerRenderer();
				if (renderer != null) {
					return renderer.getNextRange(0);
				}
			}
		}
		return null;
	}

	protected Range getNextRange(Editor editor) {
		if (editor == null) {
			return null;
		}
		Collection<RangeHighlighter> ranges = editor.getDocument().getUserData(ChangeViewer.CRUCIBLE_RANGES);
		if (ranges != null) {
			for (RangeHighlighter rangeHighlighter : ranges) {
				CrucibleDiffGutterRenderer renderer
						= (CrucibleDiffGutterRenderer) rangeHighlighter.getLineMarkerRenderer();
				if (renderer != null) {
					int offs = editor.getCaretModel().getOffset();
					int line = editor.getDocument().getLineNumber(offs);
					Range r = renderer.getNextRange(0);
					do {
						int start = r.getOffset1();
						int end = r.getOffset2();
						if (end >= line) {
							if (start > line) {
								return r;
							}
							break;
						}
						r = renderer.getNextRange(r);
					} while (r != null);
					return renderer.getNextRange(r);
				}
			}
		}
		return null;
	}

	protected Range getPrevRange(Editor editor) {
		if (editor == null) {
			return null;
		}
		Collection<RangeHighlighter> ranges = editor.getDocument().getUserData(ChangeViewer.CRUCIBLE_RANGES);
		if (ranges != null) {
			for (RangeHighlighter rangeHighlighter : ranges) {
				CrucibleDiffGutterRenderer renderer
						= (CrucibleDiffGutterRenderer) rangeHighlighter.getLineMarkerRenderer();
				if (renderer != null) {
					int offs = editor.getCaretModel().getOffset();
					int line = editor.getDocument().getLineNumber(offs);
					Range r = renderer.getLastRange();
					do {
						int start = r.getOffset1();
						int end = r.getOffset2();
						if (end < line) {
							if (start <= line) {
								return r;
							}
							break;
						}
						r = renderer.getPrevRange(r);
					} while (r != null);
					return renderer.getPrevRange(r);
				}
			}
		}
		return null;
	}

	public void actionPerformed(final AnActionEvent e) {
		AtlassianTree tree = (AtlassianTree) getTree(e);
		if (tree != null) {
			CrucibleFileNode node = getSubsequentFileNode(tree, getSelectedNode(e), true);
			if (node != null) {
				selectNode(tree, node);
				Editor ed = getEditorForNode(node);
				if (ed == null) {
					openFileNode(e, node, wantLastNode());
				} else {
					Range r = getSubsequentRange(ed);
					if (r != null) {
						openFileAndSelectRange(e, node, r);
					} else {
						node = getSubsequentFileNode(tree, getSelectedNode(e), false);
						if (node != null) {
							selectNode(tree, node);
							openFileNode(e, node, wantLastNode());
						}
					}
				}
			}
		}
	}

	public void update(AnActionEvent e) {
		updateForTree(e);
		if (e.getData(DataKeys.EDITOR) != null) {
			e.getPresentation().setVisible(updateForEditor(e) && e.getPresentation().isEnabled());
		}
	}

	protected boolean updateForEditor(AnActionEvent e) {
		AtlassianTreeNode node = getSelectedNode(e);
		VirtualFile vf = getVirtualFile(e);
		CrucibleFileNode fileNode = null;
		if (vf != null && node != null && node instanceof CrucibleFileNode) {
			PsiFile psi = CodeNavigationUtil.guessCorrespondingPsiFile(IdeaHelper.getCurrentProject(e),
					((CrucibleFileNode) node).getFile().getFileDescriptor().getAbsoluteUrl());

			if (psi != null) {
				fileNode = getNodeForFile(e, vf);
			}
		}

		return fileNode != null;
	}

	private void updateForTree(final AnActionEvent e) {

		boolean enabled = false;

		AtlassianTreeNode node = getSelectedNode(e);
		CrucibleFileNode fileNode = getSubsequentFileNode((AtlassianTree) getTree(e), node, true);
		if (fileNode != null) {
			Editor ed = getEditorForNode(fileNode);
			if (ed != null) {
				Range r = getSubsequentRange(ed);
				enabled = r != null;
			}
			if (!enabled) {
				fileNode = getSubsequentFileNode((AtlassianTree) getTree(e), node, false);
				enabled = fileNode != null;
			}
		}
		e.getPresentation().setEnabled(enabled);
	}


	protected VirtualFile getVirtualFile(AnActionEvent e) {
		Project p = IdeaHelper.getCurrentProject(e);
		if (p == null) {
			return null;
		}
		FileEditorManager fem = FileEditorManager.getInstance(p);
		if (fem == null) {
			return null;
		}
		Editor editor = fem.getSelectedTextEditor();
		if (editor == null) {
			return null;
		}

		Document document = editor.getDocument();
		return FileDocumentManager.getInstance().getFile(document);
	}

	protected void openFileAndSelectRange(AnActionEvent e, CrucibleFileNode node, Range range) {
		Project p = IdeaHelper.getCurrentProject(e);
		if (p != null) {
			ReviewAdapter r = node.getReview();
			CrucibleFileInfo f = node.getFile();
			CrucibleHelper.openFileWithDiffs(p, true, r, f, 1, 1, new OpenEditorAndSelectRange(range, p, r, f, true));
		}
	}


	protected void selectFirstDiff(CrucibleFileNode node) {
		Editor e = getEditorForNode(node);
		if (e != null) {
			Range r = getFirstRange(e);
			selectRange(e, r);
		}
	}

	protected void selectNextDiff(CrucibleFileNode node) {
		Editor e = getEditorForNode(node);
		if (e != null) {
			Range r = getNextRange(e);
			selectRange(e, r);
		}
	}

	protected void selectPrevDiff(CrucibleFileNode node) {
		Editor e = getEditorForNode(node);
		if (e != null) {
			Range r = getPrevRange(e);
			selectRange(e, r);
		}
	}

	private void selectRange(Editor e, Range r) {
		if (e != null && r != null) {
			Collection<RangeHighlighter> ranges = e.getDocument().getUserData(ChangeViewer.CRUCIBLE_RANGES);
			if (ranges != null && !ranges.isEmpty()) {
				CrucibleDiffGutterRenderer renderer
						= (CrucibleDiffGutterRenderer) ranges.iterator().next().getLineMarkerRenderer();
				if (renderer != null) {
					renderer.moveToRange(r, e, e.getDocument(), false);
				}
			}
		}
	}

	protected CrucibleFileNode getNextFileNode(AtlassianTree tree, AtlassianTreeNode node, boolean alsoThis) {
		DefaultMutableTreeNode start = node;
		if (tree == null) {
			return null;
		}
		if (start == null) {
			start = (DefaultMutableTreeNode) tree.getModel().getRoot();
		}
		if (node != null && node instanceof CrucibleFileNode && alsoThis) {
			return (CrucibleFileNode) node;
		}
		if (start != null) {
			AtlassianTreeNode n = (AtlassianTreeNode) start.getNextNode();
			while (n != null) {
				if (n instanceof CrucibleFileNode) {
					return (CrucibleFileNode) n;
				}
				n = (AtlassianTreeNode) n.getNextNode();
			}
		}
		return null;
	}

	protected CrucibleFileNode getPrevFileNode(AtlassianTree tree, AtlassianTreeNode node, boolean alsoThis) {
		if (tree == null || node == null) {
			return null;
		}
		if (node instanceof CrucibleFileNode && alsoThis) {
			return (CrucibleFileNode) node;
		}
		AtlassianTreeNode n = (AtlassianTreeNode) node.getPreviousNode();
		while (n != null) {
			if (n instanceof CrucibleFileNode) {
				return (CrucibleFileNode) n;
			}
			n = (AtlassianTreeNode) n.getPreviousNode();
		}
		return null;
	}

	private class OpenEditorAndMoveToFirstDiff extends OpenEditorDiffActionImpl {
		private final CrucibleFileNode node;

		public OpenEditorAndMoveToFirstDiff(CrucibleFileNode n, Project p, ReviewAdapter r,
				CrucibleFileInfo f, boolean focusOnOpen) {
			super(p, r, f, focusOnOpen);
			this.node = n;
		}

		public void run(OpenFileDescriptor displayFile, VirtualFile referenceFile, CommitType commitType) {
			super.run(displayFile, referenceFile, commitType);

			selectFirstDiff(node);
		}
	}

	private class OpenEditorAndMoveToLastDiff extends OpenEditorDiffActionImpl {
		private final CrucibleFileNode node;

		public OpenEditorAndMoveToLastDiff(CrucibleFileNode n, Project p, ReviewAdapter r,
				CrucibleFileInfo f, boolean focusOnOpen) {
			super(p, r, f, focusOnOpen);
			this.node = n;
		}

		public void run(OpenFileDescriptor displayFile, VirtualFile referenceFile, CommitType commitType) {
			super.run(displayFile, referenceFile, commitType);

			while (getNextRange(getEditorForNode(node)) != null) {
				selectNextDiff(node);
			}
		}
	}

	private class OpenEditorAndSelectRange extends OpenEditorDiffActionImpl {
		private final Range range;

		public OpenEditorAndSelectRange(Range range, Project p, ReviewAdapter r,
				CrucibleFileInfo f, boolean focusOnOpen) {
			super(p, r, f, focusOnOpen);
			this.range = range;
		}

		public void run(OpenFileDescriptor displayFile, VirtualFile referenceFile, CommitType commitType) {
			super.run(displayFile, referenceFile, commitType);

			FileEditorManager fem = FileEditorManager.getInstance(getProject());
			Editor editor = fem.openTextEditor(displayFile, isFocusOnOpen());

			if (editor != null) {
				selectRange(editor, range);
			}
		}
	}

	public abstract void registerShortcutsInEditor(Editor editor);

	protected abstract boolean wantLastNode();

	protected abstract Range getSubsequentRange(Editor ed);

	protected abstract CrucibleFileNode getSubsequentFileNode(AtlassianTree tree,
			AtlassianTreeNode selectedNode, boolean alsoThis);

}
