package com.atlassian.theplugin.idea.crucible.comments;

import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.ListTableModel;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.ide.DataManager;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.ui.*;
import com.atlassian.theplugin.commons.crucible.CrucibleChangeSet;
import com.atlassian.theplugin.idea.crucible.events.ShowGeneralCommentEvent;
import com.atlassian.theplugin.idea.crucible.events.FocusOnGeneralCommentReplyEvent;
import com.atlassian.theplugin.idea.crucible.CrucibleConstants;
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
	private CrucibleReviewActionListener listener = new MyCrucibleReviewActionListener();


	protected ReviewCommentsPanel() {
		super();
		context = new UserTableContext();
		IdeaHelper.getReviewActionEventBroker().registerListener(listener);
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
									CrucibleReviewActionListener.I_WANT_THIS_MESSAGE_BACK,
									(CrucibleChangeSet)
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
							(CrucibleChangeSet) CrucibleConstants.CrucibleTableState.REVIEW_ADAPTER.getValue(context),
							selectedComment);
					d.show();
					d.toFront();
				}
				if (noClicks == 2) {
					IdeaHelper.getReviewActionEventBroker().trigger(
							new ShowGeneralCommentEvent(
									CrucibleReviewActionListener.I_WANT_THIS_MESSAGE_BACK,
									(CrucibleChangeSet)
											CrucibleConstants.CrucibleTableState.REVIEW_ADAPTER.getValue(context),
									selectedComment
							)
					);
				}
			}
		};
		return commentSelectedListener;
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
		private CrucibleChangeSet crucibleChangeSet;
		private final List<GeneralComment> generalComments;

		public CommentListChangedListener(CrucibleChangeSet crucibleChangeSet, List<GeneralComment> generalComments) {
			this.crucibleChangeSet = crucibleChangeSet;
			this.generalComments = generalComments;
		}

		public void run() {
			CrucibleConstants.CrucibleTableState.REVIEW_ADAPTER.setValue(context, crucibleChangeSet);
			getCommentTableModel().setItems(generalComments);
			getCommentTableModel().fireTableDataChanged();
			getCommentsTable().getHeaderLabel().setText("General comments to " + crucibleChangeSet);
//			dataPanelsHolder.moveToFront(commentsTable);
			switchToComments();
		}
	}

	private class CommentSelectedListener implements Runnable {
		private CrucibleChangeSet crucibleChangeSet;
		private GeneralComment comment;

		public CommentSelectedListener(CrucibleChangeSet crucibleChangeSet, GeneralComment comment) {
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


		public DDialog(DataContext dataContext, CrucibleChangeSet adapter, GeneralComment comment) {
			super(true);
			this.commentPanel = new CrucibleCommentPanel(adapter, comment);
			init();
		}

		@Nullable
		protected JComponent createCenterPanel() {
			return commentPanel.getRootPanel();
		}
	}

	private class MyCrucibleReviewActionListener extends CrucibleReviewActionListener {
		public void showReview(CrucibleChangeSet crucibleChangeSet) {
			try {
				getProgressAnimation().startProgressAnimation();
				final List<GeneralComment> generalComments = crucibleServerFacade.getGeneralComments(
						crucibleChangeSet.getServer(), crucibleChangeSet.getPermaId());
				EventQueue.invokeLater(new CommentListChangedListener(crucibleChangeSet, generalComments));
			} catch (RemoteApiException e) {
				LOGGER.warn("Error retrieving comments", e);
			} catch (ServerPasswordNotProvidedException e) {
				LOGGER.warn("Error retrieving comments", e);
			} finally {
				getProgressAnimation().stopProgressAnimation();
			}
		}

		public void showGeneralComment(CrucibleChangeSet crucibleChangeSet, GeneralComment comment) {
			EventQueue.invokeLater(new CommentSelectedListener(crucibleChangeSet, comment));
		}

	}
}
