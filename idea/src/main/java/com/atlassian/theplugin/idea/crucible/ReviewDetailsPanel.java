package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.idea.crucible.comments.*;
import com.atlassian.theplugin.idea.crucible.events.ShowVersionedCommentEvent;
import com.atlassian.theplugin.idea.crucible.events.ShowRevisionCommentReplyEvent;
import com.atlassian.theplugin.idea.crucible.events.FocusOnRevisionCommentReplyEvent;
import com.atlassian.theplugin.idea.crucible.events.FocusOnVersionedCommentEvent;
import com.atlassian.theplugin.idea.ui.TableItemSelectedListener;
import com.atlassian.theplugin.idea.ui.TableColumnProvider;
import com.atlassian.theplugin.idea.ui.UserTableContext;
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
	private UserTableContext context;
	private TableColumnProvider commentTableColumnProvider = new CommentColumnProvider();
	private TableColumnProvider commentReplyTableColumnProvider = new ReviewCommentsPanel.CommentColumnProvider();

	public ReviewDetailsPanel(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem, Collection<VersionedComment> versionedComments) {
		super();
		IdeaHelper.getReviewActionEventBroker().registerListener(this);
		context = new UserTableContext();
		CrucibleConstants.CrucibleTableState.REVIEW_ADAPTER.setValue(context, reviewDataInfoAdapter);
		CrucibleConstants.CrucibleTableState.REVIEW_ITEM.setValue(context, reviewItem);
		CrucibleConstants.CrucibleTableState.VERSIONED_COMMENTS.setValue(context, versionedComments);

		ListTableModel commentTableModel = new ListTableModel(getCommentTableColumnProvider().makeColumnInfo());
		commentTableModel.setItems(new ArrayList<VersionedComment>(versionedComments));
		setCommentTableModel(commentTableModel);

		ListTableModel replyTableModel = new ListTableModel(getCommentReplyTableColumnProvider().makeColumnInfo());
		setCommentReplyTableModel(replyTableModel);

		initialize();

		getCommentsTable().getTable().setStateContext(context);
		getCommentReplyTable().getTable().setStateContext(context);

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
				switch (noClicks) {
					case 1:
						IdeaHelper.getReviewActionEventBroker().trigger(
								new FocusOnRevisionCommentReplyEvent(
										I_WANT_THIS_MESSAGE_BACK,
										(ReviewDataInfoAdapter) CrucibleConstants.CrucibleTableState.REVIEW_ADAPTER.getValue(context),
										selectedComment
								)
						);
						break;
					case 2:
						IdeaHelper.getReviewActionEventBroker().trigger(
								new ShowRevisionCommentReplyEvent(
										I_WANT_THIS_MESSAGE_BACK,
										(ReviewDataInfoAdapter) CrucibleConstants.CrucibleTableState.REVIEW_ADAPTER.getValue(context),
										selectedComment
								)
						);
						break;
					default:
						//
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
				CrucibleConstants.CrucibleTableState.SELECTED_VERSIONED_COMMENT.setValue(context, selectedComment);
				switch (noClicks) {
					case 1:
						IdeaHelper.getReviewActionEventBroker().trigger(
								new FocusOnVersionedCommentEvent(
										I_WANT_THIS_MESSAGE_BACK,
										(ReviewDataInfoAdapter) CrucibleConstants.CrucibleTableState.REVIEW_ADAPTER.getValue(context),
										(ReviewItem) CrucibleConstants.CrucibleTableState.REVIEW_ITEM.getValue(context),
										(Collection<VersionedComment>) CrucibleConstants.CrucibleTableState.VERSIONED_COMMENTS.getValue(context),
										(VersionedComment) CrucibleConstants.CrucibleTableState.SELECTED_VERSIONED_COMMENT.getValue(context)
								)
						);
						break;
					case 2:
						IdeaHelper.getReviewActionEventBroker().trigger(
								new ShowVersionedCommentEvent(
										I_WANT_THIS_MESSAGE_BACK,
										(ReviewDataInfoAdapter) CrucibleConstants.CrucibleTableState.REVIEW_ADAPTER.getValue(context),
										(ReviewItem) CrucibleConstants.CrucibleTableState.REVIEW_ITEM.getValue(context),
										(Collection<VersionedComment>) CrucibleConstants.CrucibleTableState.VERSIONED_COMMENTS.getValue(context),
										(VersionedComment) CrucibleConstants.CrucibleTableState.SELECTED_VERSIONED_COMMENT.getValue(context)
								)
						);
						break;
					default:
						//
				}
			}
		};
		return commentReplySelectedListener;
	}

	protected String getCommentToolbarActionGroup() {
		return "ThePlugin.EmptyToolBar";
	}

	protected String getCommentReplyToolbarActionGroup() {
		return "ThePlugin.CrucibleRevisionCommentReplyToolBar";
	}

	public void focusOnReview(ReviewDataInfoAdapter reviewDataInfoAdapter) {
	}

	public void focusOnFile(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem) {
	}

	public void focusOnGeneralComment(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
	}

	public void focusOnGeneralCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
	}

	public void focusOnVersionedComment(ReviewDataInfoAdapter reviewDataInfoAdapter, final ReviewItem reviewItem, Collection<VersionedComment> versionedComments, final VersionedComment versionedComment) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				CrucibleHelper.selectVersionedCommentLineInEditor(reviewItem, versionedComment);
//			}
//		});
	}

	public void focusOnVersionedCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
	}

	public void showReview(ReviewDataInfoAdapter reviewDataInfoAdapter) {
	}

	public void showReviewedFileItem(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem) {
	}

	public void showGeneralComment(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
	}

	public void showGeneralCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
	}

	public void showVersionedComment(ReviewDataInfoAdapter reviewDataInfoAdapter, final ReviewItem reviewItem,
									 Collection<VersionedComment> versionedComments, final VersionedComment versionedComment) {
//		EventQueue.invokeLater(new CommentSelectedListener(reviewDataInfoAdapter, versionedComment));
	}

	public void showVersionedCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}


	private static class CommentColumnProvider implements TableColumnProvider {
		public TableColumnInfo[] makeColumnInfo() {
			return new TableColumnInfo[]{
					new VCommentCreateDateColumn(),
					new VCommentAuthorColumn(),
					new VCommentSummaryColumn(),
					new VCommentStateColumn(),
					new VCommentRepliesColumn()
			};
		}

		public TableCellRenderer[] makeRendererInfo() {
			return new TableCellRenderer[]{
					null,
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
			getCommentReplyTable().getHeaderLabel().setText(buffer.toString());
			getCommentReplyTable().getTable().invalidate();
			getCommentReplyTable().getTable().revalidate();
			getCommentReplyTable().getTable().setEnabled(true);
			getCommentReplyTable().getTable().setForeground(UIUtil.getActiveTextColor());
			getCommentReplyTable().getTable().repaint();
//			dataPanelsHolder.moveToFront(replyCommentsTable);
			switchToCommentReplies();
		}
	}
}
