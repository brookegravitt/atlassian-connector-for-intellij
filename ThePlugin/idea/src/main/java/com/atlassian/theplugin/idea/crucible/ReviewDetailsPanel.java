package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.idea.crucible.comments.*;
import com.atlassian.theplugin.idea.crucible.events.FocusOnVersionedCommentEvent;
import com.atlassian.theplugin.idea.crucible.events.FocusOnRevisionCommentReplyEvent;
import com.atlassian.theplugin.idea.ui.TableItemSelectedListener;
import com.atlassian.theplugin.idea.ui.TableColumnProvider;
import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;

import javax.swing.table.TableCellRenderer;
import java.util.Collection;
import java.util.ArrayList;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 12, 2008
 * Time: 1:09:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReviewDetailsPanel extends AbstractCommentPanel {
	private ReviewDataInfoAdapter reviewDataInfoAdapter;
	private ReviewItem reviewItem;
	private Collection<VersionedComment> versionedComments;
	private ListTableModel rCommentTableModel;
	private ListTableModel rCommentReplyTableModel;
	private TableColumnProvider commentTableColumnProvider = new CommentColumnProvider();
	private VersionedComment selectedComment;
	private TableColumnProvider commentReplyTableColumnProvider = new ReviewCommentsPanel.CommentColumnProvider();

	public ReviewDetailsPanel(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem, Collection<VersionedComment> versionedComments) {
		super();
		IdeaHelper.getCurrentReviewActionEventBroker().registerListener(this);
		this.reviewDataInfoAdapter = reviewDataInfoAdapter;
		this.reviewItem = reviewItem;
		this.versionedComments = versionedComments;

		ListTableModel commentTableModel = new ListTableModel(getCommentTableColumnProvider().makeColumnInfo());
		commentTableModel.setItems(new ArrayList<VersionedComment>(versionedComments));
		setCommentTableModel(commentTableModel);

		ListTableModel replyTableModel = new ListTableModel(getCommentReplyTableColumnProvider().makeColumnInfo());
		setCommentReplyTableModel(replyTableModel);

		initialize();
	}

	public static ReviewDetailsPanel getInstance(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem, Collection<VersionedComment> versionedComments) {
		return new ReviewDetailsPanel(reviewDataInfoAdapter, reviewItem, versionedComments);
	}

	protected String getCommentToolbarPlace() {
		return "atlassian.toolwindow.serverToolBar";
	}

	protected String getCommentReplyToolbarPlace() {
		return "atlassian.toolwindow.serverToolBar";
	}

	protected TableColumnProvider getCommentTableColumnProvider() {
		return commentTableColumnProvider;
	}

	protected TableColumnProvider getCommentReplyTableColumnProvider() {
		return commentReplyTableColumnProvider;
	}

	protected TableItemSelectedListener getCommentReplySelectedListener() {
		TableItemSelectedListener commentReplySelectedListener;
		commentReplySelectedListener = new TableItemSelectedListener() {
			public void itemSelected(Object item, int noClicks) {
				GeneralComment selectedComment = (GeneralComment) item;
				if (noClicks == 2) {
					IdeaHelper.getCurrentReviewActionEventBroker().trigger(
							new FocusOnRevisionCommentReplyEvent(
									I_WANT_THIS_MESSAGE_BACK,
									reviewDataInfoAdapter,
									selectedComment
							)
					);
				}
			}
		};
		return commentReplySelectedListener;
	}

	protected TableItemSelectedListener getCommentSelectedListener() {
		TableItemSelectedListener commentReplySelectedListener;
		commentReplySelectedListener = new TableItemSelectedListener() {
			public void itemSelected(Object item, int noClicks) {
				VersionedComment selectedComment = (VersionedComment) item;
				if (noClicks == 2) {
					IdeaHelper.getCurrentReviewActionEventBroker().trigger(
							new FocusOnVersionedCommentEvent(
									I_WANT_THIS_MESSAGE_BACK,
									reviewDataInfoAdapter,
									selectedComment
							)
					);
				}
			}
		};
		return commentReplySelectedListener;
	}

	protected String getCommentToolbarActionGroup() {
		return "ThePlugin.CrucibleGeneralCommentToolBar";
	}

	protected String getCommentReplyToolbarActionGroup() {
		return "ThePlugin.CrucibleRevisionCommentReplyToolBar";
	}

	public void focusOnReview(ReviewDataInfoAdapter reviewDataInfoAdapter) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void focusOnFile(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void focusOnGeneralComment(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void focusOnGeneralCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void focusOnVersionedComment(ReviewDataInfoAdapter reviewDataInfoAdapter, VersionedComment versionedComment) {
		EventQueue.invokeLater(new CommentSelectedListener(reviewDataInfoAdapter, versionedComment));	}

	public void focusOnVersionedCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}


	private static class CommentColumnProvider implements TableColumnProvider {
		public TableColumnInfo[] makeColumnInfo() {
			return new TableColumnInfo[]{
					new VCommentCreateDateColumn(),
					new VCommentAuthorColumn(),
					new VCommentSummaryColumn(),
					new VCommentStateColumn()
			};
		}

		public TableCellRenderer[] makeRendererInfo() {
			return new TableCellRenderer[]{
					null,
					null,
					null,
					null
			};
		}
	}

	private class CommentSelectedListener implements Runnable {
		private ReviewDataInfoAdapter reviewDataInfoAdapter;
		private VersionedComment comment;

		public CommentSelectedListener(ReviewDataInfoAdapter reviewDataInfoAdapter, VersionedComment comment) {
			this.reviewDataInfoAdapter = reviewDataInfoAdapter;
			this.comment = comment;
		}

		public void run() {
			ReviewDetailsPanel.this.selectedComment = comment; // adapter changed to the new one
			getCommentReplyTableModel().setItems(comment.getReplies());
			getCommentReplyTableModel().fireTableDataChanged();
			StringBuffer buffer = new StringBuffer();
			buffer.append("Replies to a comment \"");
			buffer.append(comment.toString());
			buffer.append("\" made by ");
			buffer.append(comment.getDisplayUser());
			buffer.append(" [");
			buffer.append(VCommentCreateDateColumn.FORMATTER.format(comment.getCreateDate()));
			buffer.append("]");
			getReplyCommentsTable().setStatusText(buffer.toString());
			getReplyCommentsTable().getTable().revalidate();
			getReplyCommentsTable().getTable().setEnabled(true);
			getReplyCommentsTable().getTable().setForeground(UIUtil.getActiveTextColor());
//			dataPanelsHolder.moveToFront(replyCommentsTable);
			switchToCommentReplies();
		}
	}
}
