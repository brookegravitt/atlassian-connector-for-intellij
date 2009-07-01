package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.crucible.model.MockCrucibleFacadeAdapter;
import com.atlassian.theplugin.idea.ui.SwingAppRunner;

import java.util.Date;

/**
 * User: kalamon
 * Date: 2009-03-05
 * Time: 19:23:32
 */
public class LineCommentTooltipPanelTest {
	private static int replyCount = 0;

	private static final int FRAME_WIDTH = 600;
	private static final int FRAME_HEIGHT = 400;

	public static void main(String[] args) {
		ReviewBean rev = new ReviewBean("test");
		rev.setPermId(new PermIdBean("MyReview"));
		final ReviewAdapter ra = new ReviewAdapter(rev, new ServerData(new ServerCfg(true, "test", "", new ServerIdImpl()) {
			public ServerType getServerType() {
				return null;
			}

			public ServerCfg getClone() {
				return null;
			}
		}, "zenon", ""));
		final CrucibleFileInfo file = new CrucibleFileInfoImpl(null, null, new PermIdBean("reviewFile"));
		ra.setFacade(new MyNullFacade());
		final CommentBean comment = new VersionedCommentBean();
		User author = new UserBean("zenon", "Zenon User");

		comment.setAuthor(author);
		comment.setCreateDate(new Date());
		comment.setMessage(
				"Nice sizeable test message for you to look at"
						+ "Nice sizeable test message for you to look at"
						+ "Nice sizeable test message for you to look at"
						+ "Nice sizeable test message for you to look at"
						+ "Nice sizeable test message for you to look at"
						+ "Nice sizeable test message for you to look at");
		comment.setDefectRaised(true);
		comment.setDefectApproved(true);
		comment.setPermId(new PermIdBean("Parent Comment"));
		CustomFieldBean cf = new CustomFieldBean();
		cf.setValue("Total fubar");
		comment.getCustomFields().put("Defect class", cf);
		if (args.length > 0) {
			int replyNr = Integer.valueOf(args[0]);
			for (int i = 0; i < replyNr; ++i) {
				createReply(comment, "reply #" + (i + 1)
						+ "      - Nice sizeable test message for you to look at"
						+ "Nice sizeable test message for you to look at"
						+ "Nice sizeable test message for you to look at"
						+ "Nice sizeable test message for you to look at");
			}
		}

		SwingAppRunner.run(new CommentTooltipPanel(ra, file, comment, null, CommentTooltipPanel.Mode.SHOW, true) {

			protected void addNewComment(Comment comment, boolean draft) {
			}

			@Override
			protected void addNewReply(Comment parentComment, String text, boolean draft) {
				try {
					CommentBean reply = createReply(comment, text);
					reply.setDraft(draft);
					if (comment instanceof VersionedComment) {
						ra.addVersionedCommentReply(file, (VersionedComment) parentComment, (VersionedCommentBean) reply);
					} else {
						ra.addGeneralCommentReply((GeneralComment) parentComment, (GeneralCommentBean) reply);
					}
				} catch (Exception e) {
					e.printStackTrace();
					setStatusText(e.getMessage(), true);
				}
			}

			@Override
			protected void updateComment(Comment comment, String text) {
				try {
					CommentBean vcb = (CommentBean) comment;
					vcb.setMessage(text);

					if (comment instanceof VersionedComment) {
						ra.editVersionedComment(file, (VersionedComment) comment);
					} else {
						ra.editGeneralComment((GeneralComment) comment);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			protected void removeComment(Comment comment) {
				try {
					if (comment instanceof VersionedComment) {
						ra.removeVersionedComment((VersionedComment) comment, file);
					} else {
						ra.removeGeneralComment((GeneralComment) comment);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			protected void publishComment(Comment comment) {
				try {
					if (comment instanceof VersionedComment) {
						ra.publishVersionedComment(file, (VersionedComment) comment);
					} else {
						ra.publishGeneralComment((GeneralComment) comment);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, "test cru tooltip", FRAME_WIDTH, FRAME_HEIGHT);
	}

	private static CommentBean createReply(Comment parent, String txt) {
		CommentBean reply = new VersionedCommentBean();
		User replyAuthor = new UserBean("juzef", "Juzef Morda");
		reply.setAuthor(replyAuthor);
		reply.setMessage(txt);
		reply.setCreateDate(new Date());
		reply.setReply(true);
		parent.getReplies().add(reply);
		reply.setPermId(new PermIdBean("Reply #" + (++replyCount)));
		return reply;
	}

	private static class MyNullFacade extends MockCrucibleFacadeAdapter {

		public VersionedComment addVersionedCommentReply(ServerData server, PermId id, PermId cId,
				VersionedComment comment) throws RemoteApiException, ServerPasswordNotProvidedException {

			String throwException = System.getProperty("LCTPT.throw");

			if (throwException != null && throwException.equals("yes")) {
				throw new RemoteApiException(
						"Very Very Long Comment, Very Very Long Comment, "
								+ "Very Very Long Comment, Very Very Long Comment");
			} else {
				VersionedCommentBean bean = (VersionedCommentBean) comment;
				bean.setAuthor(new UserBean("zenon", "Zenon User"));
				return bean;
			}
		}
	}
}
