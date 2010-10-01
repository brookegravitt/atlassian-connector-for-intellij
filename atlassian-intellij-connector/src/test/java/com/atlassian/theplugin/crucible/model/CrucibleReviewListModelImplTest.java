package com.atlassian.theplugin.crucible.model;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleAction;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFilter;
import com.atlassian.theplugin.commons.crucible.api.model.ExtendedCrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.configuration.WorkspaceConfigurationBean;
import com.atlassian.theplugin.idea.crucible.ReviewNotificationBean;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CrucibleReviewListModelImplTest extends TestCase {
	private CrucibleReviewListModelImplAdapter model;
	private ServerData server1;
	private ServerData server2;
	private int addedReviews, changedReviews, removedReviews;
	private final Date date = new Date();
	private final User moderator = new User("moderator");
	private final User author = new User("author");
	private static final ExtendedCrucibleProject PROJECT_1 = new ExtendedCrucibleProject("My Id", "TEST", "My Test Project");

	@Override
	public void setUp() throws Exception {
		super.setUp();
		model = new CrucibleReviewListModelImplAdapter();
		server1 = createServer(1);
		server2 = createServer(2);
		addedReviews = 0;
		changedReviews = 0;
		removedReviews = 0;
	}

	public void testAddingReviewOnce() throws Exception {
		model = new CrucibleReviewListModelImplAdapter();

		model.addReview(new ReviewAdapter(null, null, null));

		assertEquals(1, model.getReviews().size());
	}

	public void testAddingReviewTwice() throws Exception {
		model = new CrucibleReviewListModelImplAdapter();


		ServerIdImpl id = new ServerIdImpl();
		ServerData cfg = createServerData(id);
		Review r = new Review("test", "TEST", author, moderator);
		r.setPermId(new PermId("test"));
		r.setState(State.REVIEW);
		ReviewAdapter ra = new ReviewAdapter(r, cfg, PROJECT_1);
		model.addReview(ra);
		model.addReview(ra);

		assertEquals(1, model.getReviews().size());
	}

	private ServerData createServerData(final ServerIdImpl id) {
		return new ServerData(new ServerCfg(true, "test", "", id) {
			@Override
			public ServerType getServerType() {
				return null;
			}

            public boolean isDontUseBasicAuth() {
                return false;
            }

            public UserCfg getBasicHttpUser() {
                return null;
            }

            @Override
			public ServerCfg getClone() {
				return null;
			}
		});
	}

	public void testAddingSingleReview() throws Exception {
		model = new CrucibleReviewListModelImplAdapter();

		model.openReview(new ReviewAdapter(null, null, null), UpdateReason.OPEN_IN_IDE);

		assertEquals(0, model.getReviews().size());
	}

	public void testAddingSingleReviewTwice() throws Exception {
		model = new CrucibleReviewListModelImplAdapter();

		ServerIdImpl id = new ServerIdImpl();
		ServerData cfg = createServerData(id);
		Review r = new Review("test", "TEST", author, moderator);
		r.setPermId(new PermId("test"));
		r.setState(State.REVIEW);
		ReviewAdapter ra = new ReviewAdapter(r, cfg, PROJECT_1);

		model.addReview(ra);
		model.openReview(ra, UpdateReason.OPEN_IN_IDE);

		assertEquals(1, model.getReviews().size());
	}

	public void testAddingTwoSingleReviews() throws Exception {
		model = new CrucibleReviewListModelImplAdapter();

		ServerIdImpl id = new ServerIdImpl();
		ServerData cfg = createServerData(id);
		Review r = new Review("test", "TEST", author, moderator);
		r.setPermId(new PermId("test"));
		r.setState(State.REVIEW);
		ReviewAdapter ra = new ReviewAdapter(r, cfg, PROJECT_1);

		// add standard review
		model.addReview(ra);
		assertEquals(1, model.getReviews().size());

		// add new review as open in ide
		model.openReview(new ReviewAdapter(null, null, null), UpdateReason.OPEN_IN_IDE);
		assertEquals(1, model.getReviews().size());

		// open the firsr review in ide (the review opened in ide before should disappear)
		// require rethink
		model.openReview(ra, UpdateReason.OPEN_IN_IDE);

		assertEquals(1, model.getReviews().size());
	}

	public void testAddingReviewWithDifferentPermId() throws Exception {
		model = new CrucibleReviewListModelImplAdapter();

		ServerIdImpl id = new ServerIdImpl();
		ServerData cfg = createServerData(id);
		Review r1 = new Review("test", "TEST", author, moderator);
		r1.setPermId(new PermId("test1"));
		Review r2 = new Review("test", "TEST", author, moderator);
		r2.setPermId(new PermId("test2"));
		ReviewAdapter ra1 = new ReviewAdapter(r1, cfg, PROJECT_1);
		ReviewAdapter ra2 = new ReviewAdapter(r2, cfg, PROJECT_1);
		model.addReview(ra1);
		model.addReview(ra2);

		assertEquals(2, model.getReviews().size());
	}

	public void testAddingTwoReviewsWithTheSamePermId() throws Exception {
		model = new CrucibleReviewListModelImplAdapter();

		ServerIdImpl id = new ServerIdImpl();
		ServerData cfg = createServerData(id);
		Review r = new Review("test", "TEST", author, moderator);
		r.setPermId(new PermId("test1"));
		r.setState(State.REVIEW);
		ReviewAdapter ra1 = new ReviewAdapter(r, cfg, PROJECT_1);
		ReviewAdapter ra2 = new ReviewAdapter(r, cfg, PROJECT_1);
		model.addReview(ra1);
		model.addReview(ra2);

		assertEquals(1, model.getReviews().size());
	}

	public void testRemoveExistingReview() throws Exception {
		model = new CrucibleReviewListModelImplAdapter();


		ServerIdImpl id = new ServerIdImpl();
		ServerData cfg = createServerData(id);
		Review r1 = new Review("test", "TEST", author, moderator);
		r1.setPermId(new PermId("test1"));
		ReviewAdapter ra1 = new ReviewAdapter(r1, cfg, PROJECT_1);

		model.addReview(ra1);
		Review r2 = new Review("test", "TEST", author, moderator);
		PermId permId = new PermId("test2");
		r2.setPermId(permId);
		ReviewAdapter ra2 = new ReviewAdapter(r2, cfg, PROJECT_1);

		model.addReview(ra2);
		assertEquals(2, model.getReviews().size());
		model.removeReview(ra1, UpdateReason.REFRESH);
		assertEquals(1, model.getReviews().size());
		assertEquals(permId, model.getReviews().iterator().next().getPermId());
	}

	public void testRemoveNonExistingReview() throws Exception {
		model = new CrucibleReviewListModelImplAdapter();


		ServerIdImpl id = new ServerIdImpl();
		ServerData cfg = createServerData(id);
		Review r1 = new Review("test", "TEST", author, moderator);
		PermId permId1 = new PermId("test1");
		r1.setPermId(permId1);
		ReviewAdapter ra1 = new ReviewAdapter(r1, cfg, PROJECT_1);

		model.addReview(ra1);
		Review r2 = new Review("test", "TEST", author, moderator);
		PermId permId2 = new PermId("test2");
		r2.setPermId(permId2);
		ReviewAdapter ra2 = new ReviewAdapter(r2, cfg, PROJECT_1);

		model.removeReview(ra2, UpdateReason.REFRESH);
		assertEquals(1, model.getReviews().size());
		assertEquals(permId1, model.getReviews().iterator().next().getPermId());
	}

	public void testRemoveAllReviews() throws Exception {
		CrucibleReviewListModelImplAdapter listModel = new CrucibleReviewListModelImplAdapter();


		ServerIdImpl id = new ServerIdImpl();
		ServerData cfg = createServerData(id);
		Review r1 = new Review("test", "TEST", author, moderator);
		r1.setPermId(new PermId("test1"));
		ReviewAdapter ra1 = new ReviewAdapter(r1, cfg, PROJECT_1);

		listModel.addReview(ra1);
		Review r2 = new Review("test", "TEST", author, moderator);
		r2.setPermId(new PermId("test2"));
		ReviewAdapter ra2 = new ReviewAdapter(r2, cfg, PROJECT_1);

		listModel.addReview(ra2);
		assertEquals(2, listModel.getReviews().size());
		Map<CrucibleFilter, ReviewNotificationBean> emptyResult = new HashMap<CrucibleFilter, ReviewNotificationBean>();
		listModel.updateReviews(0, emptyResult, UpdateReason.REFRESH); //.removeAll();
		assertEquals(0, listModel.getReviews().size());
	}

//	public void testListeners() throws Exception {
//
//		ServerId id = new ServerId();
//		CrucibleServerCfg cfg = new CrucibleServerCfg("test", id);
//
//		Review Review = new Review("test");
//		Review.setPermId(new PermId("test1"));
//		ReviewAdapter reviewAdapter = new ReviewAdapter(Review, cfg);
//		reviewAdapter.setFacade(new MyFacade());
//		reviewAdapter.setGeneralComments(new ArrayList<GeneralComment>());
//
//		CrucibleReviewListModelListener listener = EasyMock.createStrictMock(CrucibleReviewListModelListener.class);
//
//		model = new CrucibleReviewListModelImplAdapter();
//		model.addListener(listener);
//
//		listener.reviewAdded(new UpdateContext(UpdateReason.REFRESH, reviewAdapter));
////		listener.reviewChanged(reviewAdapter);
//		listener.reviewRemoved(new UpdateContext(UpdateReason.REFRESH, reviewAdapter));
//
//		EasyMock.replay(listener);
//
//		model.addReview(reviewAdapter);
//		reviewAdapter.addGeneralComment(new GeneralCommentBean());
//		model.removeReview(reviewAdapter, UpdateReason.REFRESH);
//
//		EasyMock.verify(listener);
//	}

//	public void testListenersActionsDifferentOrder() throws Exception {
//
//		int reviewId = 1;
//
//		ServerId id = new ServerId();
//		CrucibleServerCfg cfg = new CrucibleServerCfg("test", id);
//
//		ReviewAdapter reviewAdapter_1 = createReviewAdapter(reviewId, cfg);
//		ReviewAdapter reviewAdapter_2 = createReviewAdapter(reviewId, cfg);
//		ReviewAdapter reviewAdapter_3 = createReviewAdapter(reviewId, cfg);
//
//		CrucibleReviewListModelListener listener = EasyMock.createStrictMock(CrucibleReviewListModelListener.class);
//
//		model = new CrucibleReviewListModelImplAdapter();
//		model.addListener(listener);
//
//		// test 1 (add review)
//
//		listener.reviewListUpdateStarted(new UpdateContext(UpdateReason.REFRESH, null));
//		listener.reviewAdded(new UpdateContext(UpdateReason.REFRESH, reviewAdapter_1));
//		listener.reviewListUpdateFinished(new UpdateContext(UpdateReason.REFRESH, null));
//
//		EasyMock.replay(listener);
//		model.updateReviews(Arrays.asList(reviewAdapter_1));
//		EasyMock.verify(listener);
//
//		// test 2 (added actions)
//		reviewAdapter_2.getActions().add(CrucibleAction.ABANDON);
//		reviewAdapter_2.getActions().add(CrucibleAction.CLOSE);
//
//		EasyMock.reset(listener);
//
//		listener.reviewListUpdateStarted(new UpdateContext(UpdateReason.REFRESH, null));
//		listener.reviewChangedWithoutFiles(new UpdateContext(UpdateReason.REFRESH, reviewAdapter_2));
//		listener.reviewListUpdateFinished(new UpdateContext(UpdateReason.REFRESH, null));
//
//		EasyMock.replay(listener);
//		model.updateReviews(Arrays.asList(reviewAdapter_2));
//		EasyMock.verify(listener);
//
//		// test 3 (actions different order)
//		reviewAdapter_3.getActions().add(CrucibleAction.CLOSE);
//		reviewAdapter_3.getActions().add(CrucibleAction.ABANDON);
//
//		EasyMock.reset(listener);
//
//		listener.reviewListUpdateStarted(new UpdateContext(UpdateReason.REFRESH, null));
//		listener.reviewListUpdateFinished(new UpdateContext(UpdateReason.REFRESH, null));
//
//		EasyMock.replay(listener);
//		model.updateReviews(Arrays.asList(reviewAdapter_3));
//		EasyMock.verify(listener);
//	}

//	public void testListenersAfterCrucibleStatusCheckerUpdate() throws Exception {
//
//		int reviewId = 1;
//
//		ServerId id = new ServerId();
//		CrucibleServerCfg cfg = new CrucibleServerCfg("test", id);
//
//		ReviewAdapter reviewAdapter_1 = createReviewAdapterWithComments(reviewId, cfg);
//		ReviewAdapter reviewAdapter_2 = createReviewAdapterWithComments(reviewId, cfg);
//
//		CrucibleReviewListModelListener listener = EasyMock.createStrictMock(CrucibleReviewListModelListener.class);
//
//		model = new CrucibleReviewListModelImplAdapter();
//		model.addListener(listener);
//
//		// test 1 (add review)
//
//		listener.reviewListUpdateStarted(new UpdateContext(UpdateReason.REFRESH, null));
//		listener.reviewAdded(new UpdateContext(UpdateReason.REFRESH, reviewAdapter_1));
//		listener.reviewListUpdateFinished(new UpdateContext(UpdateReason.REFRESH, null));
//
//		EasyMock.replay(listener);
//		model.updateReviews(Arrays.asList(reviewAdapter_1));
//		EasyMock.verify(listener);
//
//		// test 2 (the same review)
//
//		EasyMock.reset(listener);
//
//		listener.reviewListUpdateStarted(new UpdateContext(UpdateReason.REFRESH, null));
//		listener.reviewListUpdateFinished(new UpdateContext(UpdateReason.REFRESH, null));
//
//		EasyMock.replay(listener);
//		model.updateReviews(Arrays.asList(reviewAdapter_2));
//		EasyMock.verify(listener);
//
//		// test 3 (change review)
//		EasyMock.reset(listener);
//
//		listener.reviewListUpdateStarted(new UpdateContext(UpdateReason.REFRESH, null));
//		listener.reviewChangedWithoutFiles(new UpdateContext(UpdateReason.REFRESH, reviewAdapter_1));
//		listener.reviewListUpdateFinished(new UpdateContext(UpdateReason.REFRESH, null));
//
//		EasyMock.replay(listener);
//		reviewAdapter_2.getGeneralComments().add(new GeneralCommentBean());
//		model.updateReviews(Arrays.asList(reviewAdapter_2));
//		EasyMock.verify(listener);
//
//
//		// test 4 (remove review)
//
//		EasyMock.reset(listener);
//
//		listener.reviewListUpdateStarted(new UpdateContext(UpdateReason.REFRESH, null));
//		listener.reviewRemoved(new UpdateContext(UpdateReason.REFRESH, reviewAdapter_1));
//		listener.reviewListUpdateFinished(new UpdateContext(UpdateReason.REFRESH, null));
//
//		EasyMock.replay(listener);
//		model.updateReviews(Collections.<ReviewAdapter>emptyList());
//	}

	public void testUpdateNonIntersectingList() {
		List<ReviewAdapter> updatedServer1Reviews = new ArrayList<ReviewAdapter>();
		List<ReviewAdapter> updatedServer2Reviews = new ArrayList<ReviewAdapter>();

		ReviewAdapter ra11 = createReviewAdapter(11, server1);
		ReviewAdapter ra12 = createReviewAdapter(12, server1);
		ReviewAdapter ra13 = createReviewAdapter(13, server1);
		ReviewAdapter ra14 = createReviewAdapter(14, server1);
		updatedServer1Reviews.add(ra11);
		updatedServer1Reviews.add(ra12);
		updatedServer1Reviews.add(ra13);

		ReviewAdapter ra21 = createReviewAdapter(21, server2);
		ReviewAdapter ra22 = createReviewAdapter(22, server2);
		ReviewAdapter ra23 = createReviewAdapter(23, server2);
		updatedServer2Reviews.add(ra21);
		updatedServer2Reviews.add(ra22);
		updatedServer2Reviews.add(ra23);

		CrucibleReviewListModelListener l = new CrucibleReviewListModelListenerAdapter() {
			@Override
			public void reviewAdded(UpdateContext updateContext) {
				addedReviews++;
			}

			@Override
			public void reviewRemoved(UpdateContext updateContext) {
				removedReviews++;
			}

			@Override
			public void reviewChanged(UpdateContext updateContext) {
				changedReviews++;
			}
		};
		model.addListener(l);

		model.updateReviews(updatedServer1Reviews);

		assertEquals(3, addedReviews);
		assertEquals(0, removedReviews);
		assertEquals(0, changedReviews);

		model.updateReviews(updatedServer2Reviews);

		assertEquals(6, addedReviews);
		assertEquals(3, removedReviews);
		assertEquals(0, changedReviews);

		updatedServer1Reviews.remove(ra13);
		updatedServer1Reviews.add(ra14);
		model.updateReviews(updatedServer1Reviews);
		assertEquals(9, addedReviews);
		assertEquals(6, removedReviews);
		assertEquals(0, changedReviews);

		updatedServer2Reviews.clear();
		model.updateReviews(updatedServer2Reviews);
		assertEquals(9, addedReviews);
		assertEquals(9, removedReviews);
		assertEquals(0, changedReviews);
	}

	public void testUpdateIntersectingSet() {
		List<ReviewAdapter> updatedServer1 = new ArrayList<ReviewAdapter>();
		List<ReviewAdapter> updatedServer2 = new ArrayList<ReviewAdapter>();

		ReviewAdapter ra11 = createReviewAdapter(11, server1);
		ReviewAdapter ra12 = createReviewAdapter(11, server1);
		ReviewAdapter ra13 = createReviewAdapter(11, server1);
		updatedServer1.add(ra11);
		updatedServer1.add(ra12);
		updatedServer1.add(ra13);

		ReviewAdapter ra21 = createReviewAdapter(21, server2);
		ReviewAdapter ra22 = createReviewAdapter(21, server2);
		ReviewAdapter ra23 = createReviewAdapter(23, server2);
		updatedServer2.add(ra21);
		updatedServer2.add(ra22);
		updatedServer2.add(ra23);


		CrucibleReviewListModelListener l = new CrucibleReviewListModelListenerAdapter() {
			@Override
			public void reviewAdded(UpdateContext updateContext) {
				addedReviews++;
			}

			@Override
			public void reviewRemoved(UpdateContext updateContext) {
				removedReviews++;
			}

			@Override
			public void reviewChanged(UpdateContext updateContext) {
				changedReviews++;
			}
		};
		model.addListener(l);

		model.updateReviews(updatedServer1);

		assertEquals(1, addedReviews);
		assertEquals(0, removedReviews);
		assertEquals(0, changedReviews);

		model.updateReviews(updatedServer2);
		assertEquals(3, addedReviews);
		assertEquals(1, removedReviews);
		assertEquals(0, changedReviews);

		model.updateReviews(updatedServer1);
		assertEquals(4, addedReviews);
		assertEquals(3, removedReviews);
		assertEquals(0, changedReviews);

		updatedServer1.clear();
		model.updateReviews(updatedServer1);
		assertEquals(4, addedReviews);
		assertEquals(4, removedReviews);
		assertEquals(0, changedReviews);
	}


	private ServerData createServer(int id) {
		return new ServerData(new ServerCfg(true, "server" + id, "", new ServerIdImpl()) {
			@Override
			public ServerType getServerType() {
				return null;
			}

            public boolean isDontUseBasicAuth() {
                return false;
            }

            public UserCfg getBasicHttpUser() {
                return null;
            }

            @Override
			public ServerCfg getClone() {
				return null;
			}
		});
	}

	private ReviewAdapter createReviewAdapter(int id, ServerData server) {
		Review review = new Review("test_" + id, "TEST", author, moderator);
		PermId pId = new PermId("permId_" + id);
		review.setPermId(pId);
		review.setState(State.REVIEW);
		review.setActions(new HashSet<CrucibleAction>());

		return new ReviewAdapter(review, server, PROJECT_1);
	}

	private ReviewAdapter createReviewAdapterWithComments(int id, ServerData server)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		Review review = new Review("test_" + id, "TEST", author, moderator);
		PermId pId = new PermId("permId_" + id);
		review.setPermId(pId);

		// create review adapter
		ReviewAdapter adapter = new ReviewAdapter(review, server, PROJECT_1);
		adapter.setFacade(new MyFacade());

		// add general comments
		adapter.setGeneralComments(new ArrayList<Comment>());
		GeneralComment generalComment = new GeneralComment(review, null);
		generalComment.setCreateDate(date);
		adapter.addGeneralComment(generalComment);

		// add files and versioned comments
		Set<CrucibleFileInfo> files = new HashSet<CrucibleFileInfo>();
		CrucibleFileInfo file = new CrucibleFileInfo(
				new VersionedVirtualFile("", ""),
				new VersionedVirtualFile("", ""),
				new PermId("file ID"));
		files.add(file);
		List<VersionedComment> versionedComments = new ArrayList<VersionedComment>();
		VersionedComment versionedComment = new VersionedComment(review, file);
		versionedComment.setReviewItemId(file.getPermId());
		versionedComment.setPermId(new PermId("comment ID"));
		versionedComments.add(versionedComment);
		adapter.setFilesAndVersionedComments(files, versionedComments);

		return adapter;
	}

	private class MyFacade extends MockCrucibleFacadeAdapter {

		@Override
		public Comment addGeneralComment(ServerData server, Review review, Comment comment)
				throws RemoteApiException, ServerPasswordNotProvidedException {
			return comment;
		}
	}

	private class CrucibleReviewListModelImplAdapter extends CrucibleReviewListModelImpl {
		Map<CrucibleFilter, ReviewNotificationBean> updatedReviews = new HashMap<CrucibleFilter, ReviewNotificationBean>();

		public CrucibleReviewListModelImplAdapter() {
			super(null, new WorkspaceConfigurationBean(), null, null);
			final ReviewNotificationBean bean = new ReviewNotificationBean();
			updatedReviews.put(PredefinedFilter.Open, bean);
			bean.setReviews(new ArrayList<ReviewAdapter>());
		}

		public void addReview(long epoch, CrucibleFilter crucibleFilter, ReviewAdapter review) {
			super.addReview(crucibleFilter, review, UpdateReason.REFRESH);
		}

		public void addReview(ReviewAdapter review) {

			super.addReview(PredefinedFilter.Open, review, UpdateReason.REFRESH);
		}

		public void updateReviews(Collection<ReviewAdapter> reviews) {
			updatedReviews.get(PredefinedFilter.Open).getReviews().clear();
			updatedReviews.get(PredefinedFilter.Open).getReviews().addAll(reviews);
			super.updateReviews(0, updatedReviews, UpdateReason.REFRESH);
		}
	}
}
