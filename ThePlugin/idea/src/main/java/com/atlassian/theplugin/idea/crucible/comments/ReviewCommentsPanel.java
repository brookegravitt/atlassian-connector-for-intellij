package com.atlassian.theplugin.idea.crucible.comments;

import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.ListTableModel;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.ide.DataManager;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.ui.*;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
import com.atlassian.theplugin.idea.crucible.events.ShowGeneralCommentEvent;
import com.atlassian.theplugin.idea.crucible.events.FocusOnGeneralCommentReplyEvent;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.util.PluginUtil;

import javax.swing.table.TableCellRenderer;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Collection;

import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 16, 2008
 * Time: 6:57:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReviewCommentsPanel extends AbstractCommentPanel {
	private static ReviewCommentsPanel instance = null;
	protected TableColumnProvider commentTableColumnProvider = new CommentColumnProvider();
	protected TableColumnProvider commentReplyTableColumnProvider = new CommentReplyColumnProvider();
	private CrucibleServerFacade crucibleServerFacade;
	public static final Logger LOGGER = PluginUtil.getLogger();
	private UserTableContext context;


	protected ReviewCommentsPanel() {
		super();
		context = new UserTableContext();
		IdeaHelper.getReviewActionEventBroker().registerListener(this);
		crucibleServerFacade = CrucibleServerFacadeImpl.getInstance();
		setCommentTableModel(new ListTableModel(getCommentTableColumnProvider().makeColumnInfo()));
		setCommentReplyTableModel(new ListTableModel(getCommentReplyTableColumnProvider().makeColumnInfo()));

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

	protected String getCommentToolbarActionGroup() {
		return "ThePlugin.EmptyToolBar";
		// 		return "ThePlugin.CrucibleGeneralCommentToolBar";
	}

	protected String getCommentReplyToolbarActionGroup() {
		return "ThePlugin.CrucibleGeneralCommentReplyToolBar";
	}

	protected TableColumnProvider getCommentTableColumnProvider() {
		return commentTableColumnProvider;
	}

	protected TableColumnProvider getCommentReplyTableColumnProvider() {
		return commentReplyTableColumnProvider;
	}

	public static ReviewCommentsPanel getInstance() {
		if (instance == null) {
			instance = new ReviewCommentsPanel();
		}
		return instance;
	}

	protected TableItemSelectedListener getCommentReplySelectedListener() {
		TableItemSelectedListener commentReplySelectedListener;
		commentReplySelectedListener = new TableItemSelectedListener() {
			public void itemSelected(Object item, int noClicks) {
				GeneralComment selectedComment = (GeneralComment) item;

				if (noClicks == 2) {
					IdeaHelper.getReviewActionEventBroker().trigger(
							new FocusOnGeneralCommentReplyEvent(
									I_WANT_THIS_MESSAGE_BACK,
									(ReviewDataInfoAdapter)
                                            CrucibleConstants.CrucibleTableState.REVIEW_ADAPTER.getValue(context),
                                    selectedComment
							)
					);
				}
			}
		};
		return commentReplySelectedListener;
	}

	protected TableItemSelectedListener getCommentSelectedListener() {
		TableItemSelectedListener commentSelectedListener;
		commentSelectedListener = new TableItemSelectedListener() {
			public void itemSelected(Object item, int noClicks) {
				GeneralComment selectedComment = (GeneralComment) item;
				if (noClicks == 1) {
					// GeneralComment server = ((GeneralCommentNode) selectedNode).getGeneralComment();
					DialogWrapper d = new DDialog(DataManager.getInstance().getDataContext(),
						 (ReviewDataInfoAdapter) CrucibleConstants.CrucibleTableState.REVIEW_ADAPTER.getValue(context),
						selectedComment);
					d.show();
					d.toFront();
				}
				if (noClicks == 2) {
					IdeaHelper.getReviewActionEventBroker().trigger(
							new ShowGeneralCommentEvent(
									I_WANT_THIS_MESSAGE_BACK,
									(ReviewDataInfoAdapter)
                                            CrucibleConstants.CrucibleTableState.REVIEW_ADAPTER.getValue(context),
									selectedComment
							)
					);
                }
			}
		};
		return commentSelectedListener;
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

	public void focusOnVersionedComment(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem,
            Collection<VersionedComment> versionedComments, VersionedComment versionedComment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void focusOnVersionedCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void showReview(ReviewDataInfoAdapter reviewDataInfoAdapter) {
		try {
			getProgressAnimation().startProgressAnimation();
			final List<GeneralComment> generalComments = crucibleServerFacade.getGeneralComments(
                    reviewDataInfoAdapter.getServer(), reviewDataInfoAdapter.getPermaId());
			EventQueue.invokeLater(new CommentListChangedListener(reviewDataInfoAdapter, generalComments));
		} catch (RemoteApiException e) {
			LOGGER.warn("Error retrieving comments", e);
		} catch (ServerPasswordNotProvidedException e) {
			LOGGER.warn("Error retrieving comments", e);
		} finally {
			getProgressAnimation().stopProgressAnimation();
		}
	}

	public void showReviewedFileItem(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem) {
	}

	public void showGeneralComment(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		EventQueue.invokeLater(new CommentSelectedListener(reviewDataInfoAdapter, comment));
	}

	public void showGeneralCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void showVersionedComment(ReviewDataInfoAdapter reviewDataInfoAdapter, final ReviewItem reviewItem,
            Collection<VersionedComment> versionedComments, final VersionedComment versionedComment) {
	}

	public void showVersionedCommentReply(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
		//To change body of implemented methods use File | Settings | File Templates.
	}




	public static class CommentColumnProvider implements TableColumnProvider {
		public TableColumnInfo[] makeColumnInfo() {
			return new TableColumnInfo[]{
					new CommentCreateDateColumn(),
					new CommentAuthorColumn(),
					new CommentSummaryColumn(),
					new CommentStateColumn(),
					new CommentRepliesColumn()
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

	public static class CommentReplyColumnProvider implements TableColumnProvider {
		public TableColumnInfo[] makeColumnInfo() {
			return new TableColumnInfo[]{
					new CommentCreateDateColumn(),
					new CommentAuthorColumn(),
					new CommentSummaryColumn(),
					new CommentStateColumn()
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


	private class CommentListChangedListener implements Runnable {
		private ReviewDataInfoAdapter reviewDataInfoAdapter;
		private final List<GeneralComment> generalComments;

		public CommentListChangedListener(ReviewDataInfoAdapter reviewDataInfoAdapter, List<GeneralComment> generalComments) {
			this.reviewDataInfoAdapter = reviewDataInfoAdapter;
			this.generalComments = generalComments;
		}

		public void run() {
			CrucibleConstants.CrucibleTableState.REVIEW_ADAPTER.setValue(context, reviewDataInfoAdapter);
			getCommentTableModel().setItems(generalComments);
			getCommentTableModel().fireTableDataChanged();
			getCommentsTable().getHeaderLabel().setText("General comments to " + reviewDataInfoAdapter);
//			dataPanelsHolder.moveToFront(commentsTable);
			switchToComments();
		}
	}

	private class CommentSelectedListener implements Runnable {
		private ReviewDataInfoAdapter reviewDataInfoAdapter;
		private GeneralComment comment;

		public CommentSelectedListener(ReviewDataInfoAdapter reviewDataInfoAdapter, GeneralComment comment) {
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
			buffer.append(CommentCreateDateColumn.FORMATTER.format(comment.getCreateDate()));
			buffer.append("]");
			getCommentReplyTable().setHeaderText(buffer.toString());
			getCommentReplyTable().getTable().revalidate();
			getCommentReplyTable().getTable().setEnabled(true);
			getCommentReplyTable().getTable().setForeground(UIUtil.getActiveTextColor());
//			dataPanelsHolder.moveToFront(replyCommentsTable);
			switchToCommentReplies();
		}
	}


	private class DDialog extends DialogWrapper {
	    private CrucibleCommentPanel commentPanel;


		public DDialog(DataContext dataContext, ReviewDataInfoAdapter adapter, GeneralComment comment) {
			super(true);			
			this.commentPanel = new CrucibleCommentPanel(adapter, comment);
			init();
		}

		@Nullable
		protected JComponent createCenterPanel() {
			return commentPanel.getRootPanel();
		}
	}

}
