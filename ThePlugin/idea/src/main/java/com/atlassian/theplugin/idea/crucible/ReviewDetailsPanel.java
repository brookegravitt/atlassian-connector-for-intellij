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
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.CrucibleChangeSet;
import com.atlassian.theplugin.crucible.CrucibleFileInfo;
import com.intellij.util.ui.ListTableModel;

import javax.swing.table.TableCellRenderer;
import java.util.*;
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
	private CrucibleChangeSet crucibleChangeSet;
	private CrucibleFileInfo reviewItem;
	private CrucibleReviewActionListener listener;

	public ReviewDetailsPanel(CrucibleChangeSet crucibleChangeSet, CrucibleFileInfo reviewItem,
            Collection<VersionedComment> versionedComments) {
		super();
		this.crucibleChangeSet = crucibleChangeSet;
		this.reviewItem = reviewItem;

		listener = new MyCrucibleReviewActionListener(this.reviewItem, this.crucibleChangeSet);
		IdeaHelper.getReviewActionEventBroker().registerListener(listener);
		context = new UserTableContext();
		CrucibleConstants.CrucibleTableState.REVIEW_ADAPTER.setValue(context, crucibleChangeSet);
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
        return new MyTableItemSelectedListener();
    }

    protected TableItemSelectedListener getCommentSelectedListener() {
		return new CommentReplySelectedListener();
	}

	protected String getCommentToolbarActionGroup() {
		return "ThePlugin.EmptyToolBar";
	}

	protected String getCommentReplyToolbarActionGroup() {
		return "ThePlugin.CrucibleRevisionCommentReplyToolBar";
	}


//	public void focusOnVersionedComment(CrucibleChangeSet crucibleChangeSet, final ReviewItem reviewItem,
//            Collection<VersionedComment> versionedComments, final VersionedComment versionedComment) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				CrucibleHelper.selectVersionedCommentLineInEditor(reviewItem, versionedComment);
//			}
//		});
//	}




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
		private CrucibleChangeSet crucibleChangeSet;
		private VersionedComment comment;

		public CommentSelectedListener(CrucibleChangeSet crucibleChangeSet, VersionedComment comment) {
			this.crucibleChangeSet = crucibleChangeSet;
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

//			dataPanelsHolder.moveToFront(replyCommentsTable);
			switchToCommentReplies();
		}
	}

    private class MyTableItemSelectedListener implements TableItemSelectedListener {
        public void itemSelected(Object item, int noClicks) {
            GeneralComment selectedComment = (GeneralComment) item;
            switch (noClicks) {
                case 1:
                    IdeaHelper.getReviewActionEventBroker().trigger(
                            new FocusOnRevisionCommentReplyEvent(
                                    CrucibleReviewActionListener.I_WANT_THIS_MESSAGE_BACK,
                                    (CrucibleChangeSet) CrucibleConstants.CrucibleTableState.REVIEW_ADAPTER.getValue(context),
                                    selectedComment
                            )
                    );
                    break;
                case 2:
                    IdeaHelper.getReviewActionEventBroker().trigger(
                            new ShowRevisionCommentReplyEvent(
									CrucibleReviewActionListener.I_WANT_THIS_MESSAGE_BACK,
                                    (CrucibleChangeSet) CrucibleConstants.CrucibleTableState.REVIEW_ADAPTER.getValue(context),
                                    selectedComment
                            )
                    );
                    break;
                default:
                    //
            }
        }
    }

    private class CommentReplySelectedListener implements TableItemSelectedListener {
        public void itemSelected(Object item, int noClicks) {
            VersionedComment selectedComment = (VersionedComment) item;
            CrucibleConstants.CrucibleTableState.SELECTED_VERSIONED_COMMENT.setValue(context, selectedComment);
            switch (noClicks) {
                case 1:
                    IdeaHelper.getReviewActionEventBroker().trigger(
                            new FocusOnVersionedCommentEvent(
                                    CrucibleReviewActionListener.I_WANT_THIS_MESSAGE_BACK,
                                    (CrucibleChangeSet) CrucibleConstants.CrucibleTableState.REVIEW_ADAPTER.getValue(context),
                                    (CrucibleFileInfo) CrucibleConstants.CrucibleTableState.REVIEW_ITEM.getValue(context),
                                    (Collection<VersionedComment>) CrucibleConstants.CrucibleTableState.VERSIONED_COMMENTS.getValue(context),
                                    (VersionedComment) CrucibleConstants.CrucibleTableState.SELECTED_VERSIONED_COMMENT.getValue(context)
                            )
                    );
                    break;
                case 2:
                    IdeaHelper.getReviewActionEventBroker().trigger(
                            new ShowVersionedCommentEvent(
                                    CrucibleReviewActionListener.I_WANT_THIS_MESSAGE_BACK,
                                    (CrucibleChangeSet) CrucibleConstants.CrucibleTableState.REVIEW_ADAPTER.getValue(context),
                                    (CrucibleFileInfo) CrucibleConstants.CrucibleTableState.REVIEW_ITEM.getValue(context),
                                    (Collection<VersionedComment>) CrucibleConstants.CrucibleTableState.VERSIONED_COMMENTS.getValue(context),
                                    (VersionedComment) CrucibleConstants.CrucibleTableState.SELECTED_VERSIONED_COMMENT.getValue(context)
                            )
                    );
                    break;
                default:
                    //
            }
        }
    }

	private class MyCrucibleReviewActionListener extends CrucibleReviewActionListener {
		private CrucibleChangeSet crucibleChangeSet;
		private CrucibleFileInfo reviewItem;

		public MyCrucibleReviewActionListener(CrucibleFileInfo reviewItem, CrucibleChangeSet crucibleChangeSet) {
			this.reviewItem = reviewItem;
			this.crucibleChangeSet = crucibleChangeSet;
		}


		public void showReviewedFileItem(CrucibleChangeSet crucibleChangeSet, CrucibleFileInfo reviewItem) {
			if (this.crucibleChangeSet.equals(crucibleChangeSet)
					&& this.reviewItem.equals(reviewItem)) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						switchToComments();
					}
				});
			}
		}

		public void showVersionedComment(CrucibleChangeSet crucibleChangeSet, final CrucibleFileInfo reviewItem,
				Collection<VersionedComment> versionedComments, final VersionedComment versionedComment) {
			EventQueue.invokeLater(new CommentSelectedListener(crucibleChangeSet, versionedComment));

		}

		public void showVersionedCommentReply(CrucibleChangeSet crucibleChangeSet, GeneralComment comment) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

	}
}
