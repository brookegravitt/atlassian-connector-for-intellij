package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.idea.ui.SwingAppRunner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
            protected void addNewReply(VersionedComment parent, String text) {
                try {
                    VersionedCommentBean reply = createReply(comment, text);
                    ra.addVersionedCommentReply(file, parent, reply);
                } catch (RemoteApiException e) {
                    e.printStackTrace();
                } catch (ServerPasswordNotProvidedException e) {
                    e.printStackTrace();
                }
            }

            protected void updateComment(VersionedComment comment, String text) {
				try {
					VersionedCommentBean vcb = (VersionedCommentBean) comment;
					vcb.setMessage(text);
					ra.editVersionedComment(file, comment);
				} catch (RemoteApiException e) {
					e.printStackTrace();
				} catch (ServerPasswordNotProvidedException e) {
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

    private static class MyNullFacade implements CrucibleServerFacade {
        public Review createReview(CrucibleServerCfg server, Review review)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public Review createReviewFromRevision(CrucibleServerCfg server, Review review, List<String> revisions)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public Review addRevisionsToReview(CrucibleServerCfg server, PermId permId, String repository,
                                           List<String> revisions)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public Review addPatchToReview(CrucibleServerCfg server, PermId permId, String repository, String patch)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public List<Reviewer> getReviewers(CrucibleServerCfg server, PermId permId)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public void addReviewers(CrucibleServerCfg server, PermId permId, Set<String> userName)
                throws RemoteApiException, ServerPasswordNotProvidedException {
        }

        public void removeReviewer(CrucibleServerCfg server, PermId permId, String userName)
                throws RemoteApiException, ServerPasswordNotProvidedException {
        }

        public Review approveReview(CrucibleServerCfg server, PermId permId)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public Review submitReview(CrucibleServerCfg server, PermId permId)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public Review summarizeReview(CrucibleServerCfg server, PermId permId)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public Review abandonReview(CrucibleServerCfg server, PermId permId)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public Review closeReview(CrucibleServerCfg server, PermId permId, String summary)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public Review recoverReview(CrucibleServerCfg server, PermId permId)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public Review reopenReview(CrucibleServerCfg server, PermId permId)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public void completeReview(CrucibleServerCfg server, PermId permId, boolean complete)
                throws RemoteApiException, ServerPasswordNotProvidedException {
        }

        public List<Review> getAllReviews(CrucibleServerCfg server)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public List<Review> getReviewsForFilter(CrucibleServerCfg server, PredefinedFilter filter)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public List<Review> getReviewsForCustomFilter(CrucibleServerCfg server, CustomFilter filter)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public Review getReview(CrucibleServerCfg server, PermId permId)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public Review createReviewFromPatch(CrucibleServerCfg server, Review review, String patch)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public Set<CrucibleFileInfo> getFiles(CrucibleServerCfg server, PermId permId)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public List<GeneralComment> getGeneralComments(CrucibleServerCfg server, PermId permId)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public List<VersionedComment> getVersionedComments(CrucibleServerCfg server, PermId permId)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public List<VersionedComment> getVersionedComments(CrucibleServerCfg server, PermId permId, PermId reviewItemId)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public GeneralComment addGeneralComment(CrucibleServerCfg server, PermId permId, GeneralComment comment)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public VersionedComment addVersionedComment(CrucibleServerCfg server, PermId permId, PermId riId,
                                                    VersionedComment comment)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public void updateComment(CrucibleServerCfg server, PermId id, Comment comment)
                throws RemoteApiException, ServerPasswordNotProvidedException {
        }

        public void publishComment(CrucibleServerCfg server, PermId reviewId, PermId commentId)
                throws RemoteApiException, ServerPasswordNotProvidedException {
        }

        public void publishAllCommentsForReview(CrucibleServerCfg server, PermId reviewId)
                throws RemoteApiException, ServerPasswordNotProvidedException {
        }

        public GeneralComment addGeneralCommentReply(CrucibleServerCfg server, PermId id,
                                                     PermId cId, GeneralComment comment)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public VersionedComment addVersionedCommentReply(CrucibleServerCfg server, PermId id, PermId cId,
                                                         VersionedComment comment)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            VersionedCommentBean bean = (VersionedCommentBean) comment;
            bean.setAuthor(new UserBean("zenon", "Zenon User"));
            return bean;
        }

        public void removeComment(CrucibleServerCfg server, PermId id, Comment comment)
                throws RemoteApiException, ServerPasswordNotProvidedException {
        }

        public List<User> getUsers(CrucibleServerCfg server)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public List<CrucibleProject> getProjects(CrucibleServerCfg server)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public List<Repository> getRepositories(CrucibleServerCfg server)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public SvnRepository getRepository(CrucibleServerCfg server, String repoName)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public List<CustomFieldDef> getMetrics(CrucibleServerCfg server, int version)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public void setCallback(HttpSessionCallback callback) {
        }

        @Nullable
        public String getDisplayName(@NotNull CrucibleServerCfg server, @NotNull String username) {
            return null;
        }

        @Nullable
        public CrucibleProject getProject(@NotNull CrucibleServerCfg server, @NotNull String projectKey)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public boolean checkContentUrlAvailable(CrucibleServerCfg server)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return false;
        }

        public Review createReviewFromUpload(CrucibleServerCfg server, Review review, Collection<UploadItem> uploadItems)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public Review addItemsToReview(CrucibleServerCfg server, PermId permId, Collection<UploadItem> items)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public String getFileContent(@NotNull CrucibleServerCfg server, @NotNull CrucibleFileInfo file,
                                     @NotNull ReviewItemContentType type)
                throws RemoteApiException, ServerPasswordNotProvidedException {
            return null;
        }

        public void testServerConnection(ServerCfg serverCfg) throws RemoteApiException {
        }

        public ServerType getServerType() {
            return null;
        }
    }
}
