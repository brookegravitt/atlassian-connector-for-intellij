package com.atlassian.theplugin.idea.crucible.editor;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.idea.action.crucible.comment.gutter.EditAction;
import com.atlassian.theplugin.idea.action.crucible.comment.gutter.PublishAction;
import com.atlassian.theplugin.idea.action.crucible.comment.gutter.RemoveAction;
import com.atlassian.theplugin.idea.action.crucible.comment.gutter.ReplyAction;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class CrucibleGutterIconRenderer extends GutterIconRenderer {
	private static final Icon CRUCIBLE_ICON = IconLoader.getIcon("/icons/tab_crucible.png");

	private final ReviewAdapter review;
	private final CrucibleFileInfo fileInfo;
	private final VersionedComment comment;

	public CrucibleGutterIconRenderer(ReviewAdapter review, CrucibleFileInfo fileInfo, VersionedComment comment) {
		this.review = review;
		this.fileInfo = fileInfo;
		this.comment = comment;
	}

	@NotNull
	public Icon getIcon() {
		return CRUCIBLE_ICON;
	}

	@Override
	public String getTooltipText() {
		StringBuilder s = new StringBuilder();
		s.append("<html><b>" + comment.getAuthor().getDisplayName()
				+ ":</b><br> " + comment.getMessage().replace("\n", "<br>"));
		for (VersionedComment versionedComment : comment.getReplies()) {
			s.append("<br><b>" + versionedComment.getAuthor().getDisplayName()
					+ " replied:</b><br> " + versionedComment.getMessage().replace("\n", "<br>"));
		}
		s.append("</html>");
		return s.toString();
	}

	@Override
	public ActionGroup getPopupMenuActions() {
		final ActionManager actionManager = ActionManager.getInstance();
		DefaultActionGroup defaultactiongroup = new DefaultActionGroup();

		if (checkIfAuthorized()) {
			ReplyAction reply = (ReplyAction) actionManager.getAction("ThePlugin.Crucible.Comment.Gutter.Reply");
			reply.setReview(review);
			reply.setFile(fileInfo);
			reply.setComment(comment);
			defaultactiongroup.add(reply);
		}

		if (checkIfUserAnAuthor()) {
			EditAction edit = (EditAction) actionManager.getAction("ThePlugin.Crucible.Comment.Gutter.Edit");
			edit.setReview(review);
			edit.setFile(fileInfo);
			edit.setComment(comment);
			defaultactiongroup.add(edit);
		}

		if (checkIfUserAnAuthor()) {
			RemoveAction removeAction = (RemoveAction) actionManager.getAction("ThePlugin.Crucible.Comment.Gutter.Remove");
			removeAction.setReview(review);
			removeAction.setFile(fileInfo);
			removeAction.setComment(comment);
			defaultactiongroup.add(removeAction);
		}

		if (checkIfDraftAndAuthor()) {
			PublishAction publishAction = (PublishAction) actionManager.getAction("ThePlugin.Crucible.Comment.Gutter.Publish");
			publishAction.setReview(review);
			publishAction.setFile(fileInfo);
			publishAction.setComment(comment);
			defaultactiongroup.add(publishAction);
		}
		return defaultactiongroup;
	}

	@Override
	public AnAction getClickAction() {
		return new ClickAction();
	}

	private static final Color BACKGROUND_COLOR = new Color(253, 254, 226);

	private class ClickAction extends AnAction {

		public void actionPerformed(final AnActionEvent anActionEvent) {
/*
			final JEditorPane htmlView = new JEditorPane();
			JScrollPane scrollPane;
			htmlView.setEditable(false);
			htmlView.setContentType("text/html");
			htmlView.setBackground(BACKGROUND_COLOR);
			htmlView.addHyperlinkListener(new GenericHyperlinkListener());
			htmlView.setEditorKit(new ClasspathHTMLEditorKit());

			scrollPane = new JScrollPane(htmlView,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setWheelScrollingEnabled(true);
        	htmlView.setText(getTooltipText());
			htmlView.setCaretPosition(0);

			JBPopupFactory.getInstance().createComponentPopupBuilder(scrollPane, scrollPane)
					.setCancelOnClickOutside(true)
					.setMovable(true)
					.setTitle("Comment")
					.setResizable(true)
					.setAdText("Comment details")
					.setCancelKeyEnabled(true)
					.setModalContext(true)
					.createPopup().showInBestPositionFor(anActionEvent.getDataContext());
*/
		}
	}

	protected boolean checkIfDraftAndAuthor() {
		if (checkIfUserAnAuthor()
				&& comment.isDraft()) {
			return true;
		}
		return false;
	}

	protected boolean checkIfUserAnAuthor() {
		return review.getServer().getUsername().equals(comment.getAuthor().getUserName());
	}

	protected boolean checkIfAuthorized() {
		if (review == null) {
			return false;
		}
		try {
			if (!review.getActions().contains(CrucibleAction.COMMENT)) {
				return false;
			}
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			return false;
		}
		return true;
	}

}

