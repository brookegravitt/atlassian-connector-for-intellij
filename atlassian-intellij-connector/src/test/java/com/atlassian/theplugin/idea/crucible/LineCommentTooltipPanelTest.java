package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfoImpl;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldBean;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PermIdBean;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewBean;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.UserBean;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedCommentBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
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
		final ReviewAdapter ra = new ReviewAdapter(rev, new CrucibleServerCfg("test", new ServerId()) {
			@Override
			public String getUsername() {
				return "zenon";
			}
		});
		final CrucibleFileInfo file = new CrucibleFileInfoImpl(null, null, new PermIdBean("reviewFile"));
		ra.setFacade(new MyNullFacade());
		final VersionedCommentBean comment = new VersionedCommentBean();
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

		SwingAppRunner.run(new LineCommentTooltipPanel(ra, file, comment, true) {
			@Override
			protected void addNewReply(VersionedComment parent, String text, boolean draft) {
				try {
					VersionedCommentBean reply = createReply(comment, text);
					reply.setDraft(draft);
					ra.addVersionedCommentReply(file, parent, reply);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			protected void updateComment(VersionedComment comment, String text) {
				try {
					VersionedCommentBean vcb = (VersionedCommentBean) comment;
					vcb.setMessage(text);
					ra.editVersionedComment(file, comment);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			protected void removeComment(VersionedComment comment) {
				try {
					ra.removeVersionedComment(comment, file);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			protected void publishComment(VersionedComment comment) {
				try {
					ra.publisVersionedComment(file, comment);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, "test cru tooltip", FRAME_WIDTH, FRAME_HEIGHT);
	}

	private static VersionedCommentBean createReply(VersionedComment parent, String txt) {
		VersionedCommentBean reply = new VersionedCommentBean();
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

		public VersionedComment addVersionedCommentReply(CrucibleServerCfg server, PermId id, PermId cId,
				VersionedComment comment) throws RemoteApiException, ServerPasswordNotProvidedException {
			VersionedCommentBean bean = (VersionedCommentBean) comment;
			bean.setAuthor(new UserBean("zenon", "Zenon User"));
			return bean;
		}
	}
}
