package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Action;
import com.atlassian.theplugin.commons.crucible.api.model.Comment;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfoImpl;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleProject;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFieldDef;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralCommentBean;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.PermIdBean;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewBean;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.SvnRepository;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedCommentBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import junit.framework.TestCase;
import org.easymock.EasyMock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrucibleReviewListModelImplTest extends TestCase {
	private CrucibleReviewListModel model;
	private CrucibleServerCfg server1;
	private CrucibleServerCfg server2;
	private int addedReviews, changedReviews, removedReviews;
	private Date date = new Date();

	@Override
	public void setUp() throws Exception {
		super.setUp();
		model = new CrucibleReviewListModelImpl();
		server1 = createServer(1);
		server2 = createServer(2);
		addedReviews = 0;
		changedReviews = 0;
		removedReviews = 0;
	}

	public void testAddingReviewOnce() throws Exception {
		model = new CrucibleReviewListModelImpl();

		model.addReview(new ReviewAdapter(null, null));

		assertEquals(1, model.getReviews().size());
	}

	public void testAddingReviewTwice() throws Exception {
		model = new CrucibleReviewListModelImpl();


		ServerId id = new ServerId();
		CrucibleServerCfg cfg = new CrucibleServerCfg("test", id);
		ReviewBean r = new ReviewBean("test");
		r.setPermId(new PermIdBean("test"));
		ReviewAdapter ra = new ReviewAdapter(r, cfg);
		model.addReview(ra);
		model.addReview(ra);

		assertEquals(1, model.getReviews().size());
	}

	public void testAddingReviewWithDifferentPermId() throws Exception {
		model = new CrucibleReviewListModelImpl();


		ServerId id = new ServerId();
		CrucibleServerCfg cfg = new CrucibleServerCfg("test", id);
		ReviewBean r1 = new ReviewBean("test");
		r1.setPermId(new PermIdBean("test1"));
		ReviewBean r2 = new ReviewBean("test");
		r2.setPermId(new PermIdBean("test2"));
		ReviewAdapter ra1 = new ReviewAdapter(r1, cfg);
		ReviewAdapter ra2 = new ReviewAdapter(r2, cfg);
		model.addReview(ra1);
		model.addReview(ra2);

		assertEquals(2, model.getReviews().size());
	}

	public void testAddingTwoReviewsWithTheSamePermId() throws Exception {
		model = new CrucibleReviewListModelImpl();


		ServerId id = new ServerId();
		CrucibleServerCfg cfg = new CrucibleServerCfg("test", id);
		ReviewBean r = new ReviewBean("test");
		r.setPermId(new PermIdBean("test1"));
		ReviewAdapter ra1 = new ReviewAdapter(r, cfg);
		ReviewAdapter ra2 = new ReviewAdapter(r, cfg);
		model.addReview(ra1);
		model.addReview(ra2);

		assertEquals(1, model.getReviews().size());
	}

	public void testRemoveExistingReview() throws Exception {
		model = new CrucibleReviewListModelImpl();


		ServerId id = new ServerId();
		CrucibleServerCfg cfg = new CrucibleServerCfg("test", id);
		ReviewBean r1 = new ReviewBean("test");
		r1.setPermId(new PermIdBean("test1"));
		ReviewAdapter ra1 = new ReviewAdapter(r1, cfg);

		model.addReview(ra1);
		ReviewBean r2 = new ReviewBean("test");
		PermIdBean permId = new PermIdBean("test2");
		r2.setPermId(permId);
		ReviewAdapter ra2 = new ReviewAdapter(r2, cfg);

		model.addReview(ra2);
		assertEquals(2, model.getReviews().size());
		model.removeReview(ra1);
		assertEquals(1, model.getReviews().size());
		assertEquals(permId, model.getReviews().iterator().next().getPermId());
	}

	public void testRemoveNonExistingReview() throws Exception {
		model = new CrucibleReviewListModelImpl();


		ServerId id = new ServerId();
		CrucibleServerCfg cfg = new CrucibleServerCfg("test", id);
		ReviewBean r1 = new ReviewBean("test");
		PermIdBean permId1 = new PermIdBean("test1");
		r1.setPermId(permId1);
		ReviewAdapter ra1 = new ReviewAdapter(r1, cfg);

		model.addReview(ra1);
		ReviewBean r2 = new ReviewBean("test");
		PermIdBean permId2 = new PermIdBean("test2");
		r2.setPermId(permId2);
		ReviewAdapter ra2 = new ReviewAdapter(r2, cfg);

		model.removeReview(ra2);
		assertEquals(1, model.getReviews().size());
		assertEquals(permId1, model.getReviews().iterator().next().getPermId());
	}

	public void testRemoveAllReviews() throws Exception {
		CrucibleReviewListModel listModel = new CrucibleReviewListModelImpl();


		ServerId id = new ServerId();
		CrucibleServerCfg cfg = new CrucibleServerCfg("test", id);
		ReviewBean r1 = new ReviewBean("test");
		r1.setPermId(new PermIdBean("test1"));
		ReviewAdapter ra1 = new ReviewAdapter(r1, cfg);

		listModel.addReview(ra1);
		ReviewBean r2 = new ReviewBean("test");
		r2.setPermId(new PermIdBean("test2"));
		ReviewAdapter ra2 = new ReviewAdapter(r2, cfg);

		listModel.addReview(ra2);
		assertEquals(2, listModel.getReviews().size());
		listModel.removeAll();
		assertEquals(0, listModel.getReviews().size());
	}

	public void testListeners() throws Exception {

		ServerId id = new ServerId();
		CrucibleServerCfg cfg = new CrucibleServerCfg("test", id);

		ReviewBean reviewBean = new ReviewBean("test");
		reviewBean.setPermId(new PermIdBean("test1"));
		ReviewAdapter reviewAdapter = new ReviewAdapter(reviewBean, cfg);
		reviewAdapter.setFacade(new MyFacade());
		reviewAdapter.setGeneralComments(new ArrayList<GeneralComment>());

		CrucibleReviewListModelListener listener = EasyMock.createStrictMock(CrucibleReviewListModelListener.class);

		model = new CrucibleReviewListModelImpl();
		model.addListener(listener);

		listener.reviewAdded(reviewAdapter);
//		listener.reviewChanged(reviewAdapter);
		listener.reviewRemoved(reviewAdapter);

		EasyMock.replay(listener);

		model.addReview(reviewAdapter);
		reviewAdapter.addGeneralComment(new GeneralCommentBean());
		model.removeReview(reviewAdapter);

		EasyMock.verify(listener);
	}

	public void testListenersActionsDifferentOrder() throws Exception {

		int reviewId = 1;

		ServerId id = new ServerId();
		CrucibleServerCfg cfg = new CrucibleServerCfg("test", id);

		ReviewAdapter reviewAdapter_1 = createReviewAdapter(reviewId, cfg);
		ReviewAdapter reviewAdapter_2 = createReviewAdapter(reviewId, cfg);
		ReviewAdapter reviewAdapter_3 = createReviewAdapter(reviewId, cfg);

		CrucibleReviewListModelListener listener = EasyMock.createStrictMock(CrucibleReviewListModelListener.class);

		model = new CrucibleReviewListModelImpl();
		model.addListener(listener);

		// test 1 (add review)

		listener.reviewListUpdateStarted(cfg.getServerId());
		listener.reviewAdded(reviewAdapter_1);
		listener.reviewListUpdateFinished(cfg.getServerId());

		EasyMock.replay(listener);
		model.updateReviews(cfg, Arrays.asList(reviewAdapter_1));
		EasyMock.verify(listener);

		// test 2 (added actions)
		reviewAdapter_2.getActions().add(Action.ABANDON);
		reviewAdapter_2.getActions().add(Action.CLOSE);

		EasyMock.reset(listener);

		listener.reviewListUpdateStarted(cfg.getServerId());
		listener.reviewChangedWithoutFiles(reviewAdapter_2);
		listener.reviewListUpdateFinished(cfg.getServerId());

		EasyMock.replay(listener);
		model.updateReviews(cfg, Arrays.asList(reviewAdapter_2));
		EasyMock.verify(listener);

		// test 3 (actions different order)
		reviewAdapter_3.getActions().add(Action.CLOSE);
		reviewAdapter_3.getActions().add(Action.ABANDON);

		EasyMock.reset(listener);

		listener.reviewListUpdateStarted(cfg.getServerId());
		listener.reviewListUpdateFinished(cfg.getServerId());

		EasyMock.replay(listener);
		model.updateReviews(cfg, Arrays.asList(reviewAdapter_3));
		EasyMock.verify(listener);
	}

	public void testListenersAfterCrucibleStatusCheckerUpdate() throws Exception {

		int reviewId = 1;

		ServerId id = new ServerId();
		CrucibleServerCfg cfg = new CrucibleServerCfg("test", id);

		ReviewAdapter reviewAdapter_1 = createReviewAdapterWithComments(reviewId, cfg);
		ReviewAdapter reviewAdapter_2 = createReviewAdapterWithComments(reviewId, cfg);

		CrucibleReviewListModelListener listener = EasyMock.createStrictMock(CrucibleReviewListModelListener.class);

		model = new CrucibleReviewListModelImpl();
		model.addListener(listener);

		// test 1 (add review)

		listener.reviewListUpdateStarted(cfg.getServerId());
		listener.reviewAdded(reviewAdapter_1);
		listener.reviewListUpdateFinished(cfg.getServerId());

		EasyMock.replay(listener);
		model.updateReviews(cfg, Arrays.asList(reviewAdapter_1));
		EasyMock.verify(listener);

		// test 2 (the same review)

		EasyMock.reset(listener);

		listener.reviewListUpdateStarted(cfg.getServerId());
		listener.reviewListUpdateFinished(cfg.getServerId());

		EasyMock.replay(listener);
		model.updateReviews(cfg, Arrays.asList(reviewAdapter_2));
		EasyMock.verify(listener);

		// test 3 (change review)
		EasyMock.reset(listener);

		listener.reviewListUpdateStarted(cfg.getServerId());
		listener.reviewChangedWithoutFiles(reviewAdapter_1);
		listener.reviewListUpdateFinished(cfg.getServerId());

		EasyMock.replay(listener);
		reviewAdapter_2.getGeneralComments().add(new GeneralCommentBean());
		model.updateReviews(cfg, Arrays.asList(reviewAdapter_2));
		EasyMock.verify(listener);


		// test 4 (remove review)

		EasyMock.reset(listener);

		listener.reviewListUpdateStarted(cfg.getServerId());
		listener.reviewRemoved(reviewAdapter_1);
		listener.reviewListUpdateFinished(cfg.getServerId());

		EasyMock.replay(listener);
		model.updateReviews(cfg, Collections.<ReviewAdapter>emptyList());
	}

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
			public void reviewAdded(ReviewAdapter review) {
				addedReviews++;
			}

			@Override
			public void reviewRemoved(ReviewAdapter review) {
				removedReviews++;
			}

			@Override
			public void reviewChanged(ReviewAdapter review) {
				changedReviews++;
			}
		};
		model.addListener(l);
		
		model.updateReviews(server1, updatedServer1Reviews);

		assertEquals(3, addedReviews);
		assertEquals(0, removedReviews);
		assertEquals(0, changedReviews);

		model.updateReviews(server2, updatedServer2Reviews);

		assertEquals(6, addedReviews);
		assertEquals(0, removedReviews);
		assertEquals(0, changedReviews);

		updatedServer1Reviews.remove(ra13);
		updatedServer1Reviews.add(ra14);
		model.updateReviews(server1, updatedServer1Reviews);
		assertEquals(7, addedReviews);
		assertEquals(1, removedReviews);
		assertEquals(0, changedReviews);

		updatedServer2Reviews.clear();
		model.updateReviews(server2, updatedServer2Reviews);
		assertEquals(7, addedReviews);
		assertEquals(4, removedReviews);
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
			public void reviewAdded(ReviewAdapter review) {
				addedReviews++;
			}

			@Override
			public void reviewRemoved(ReviewAdapter review) {
				removedReviews++;
			}

			@Override
			public void reviewChanged(ReviewAdapter review) {
				changedReviews++;
			}
		};
		model.addListener(l);

		model.updateReviews(server1, updatedServer1);

		assertEquals(1, addedReviews);
		assertEquals(0, removedReviews);
		assertEquals(0, changedReviews);

		model.updateReviews(server2, updatedServer2);
		assertEquals(3, addedReviews);
		assertEquals(0, removedReviews);
		assertEquals(0, changedReviews);

		model.updateReviews(server1, updatedServer1);
		assertEquals(3, addedReviews);
		assertEquals(0, removedReviews);
		assertEquals(0, changedReviews);

		updatedServer1.clear();
		model.updateReviews(server1, updatedServer1);
		assertEquals(3, addedReviews);
		assertEquals(1, removedReviews);
		assertEquals(0, changedReviews);
	}


	private CrucibleServerCfg createServer(int id) {
		return new CrucibleServerCfg("server" + id, new ServerId());
	}

	private ReviewAdapter createReviewAdapter(int id, CrucibleServerCfg server) {
		ReviewBean rb = new ReviewBean("test_" + id);
		PermId pId = new PermIdBean("permId_" + id);
		rb.setPermId(pId);
		rb.setActions(new HashSet<Action>());

		return new ReviewAdapter(rb, server);
	}

	private ReviewAdapter createReviewAdapterWithComments(int id, CrucibleServerCfg server)
			throws RemoteApiException, ValueNotYetInitialized, ServerPasswordNotProvidedException {
		ReviewBean rb = new ReviewBean("test_" + id);
		PermId pId = new PermIdBean("permId_" + id);
		rb.setPermId(pId);

		// create review adapter
		ReviewAdapter adapter = new ReviewAdapter(rb, server);
		adapter.setFacade(new MyFacade());

		// add general comments
		adapter.setGeneralComments(new ArrayList<GeneralComment>());
		GeneralCommentBean generalComment = new GeneralCommentBean();
		generalComment.setCreateDate(date);
		adapter.addGeneralComment(generalComment);

		// add files and versioned comments
		List<CrucibleFileInfo> files = new ArrayList<CrucibleFileInfo>();
		CrucibleFileInfoImpl file = new CrucibleFileInfoImpl(
				new VersionedVirtualFile("", "", rb.getVirtualFileSystem()),
				new VersionedVirtualFile("", "", rb.getVirtualFileSystem()),
				new PermIdBean("file ID"));
		files.add(file);
		List<VersionedComment> versionedComments = new ArrayList<VersionedComment>();
		VersionedCommentBean versionedComment = new VersionedCommentBean();
		versionedComment.setReviewItemId(file.getPermId());
		versionedComment.setPermId(new PermIdBean("comment ID"));
		versionedComments.add(versionedComment);
		adapter.setFilesAndVersionedComments(files, versionedComments);

		return adapter;
	}

	private class MyFacade implements CrucibleServerFacade {
		public Review createReview(CrucibleServerCfg server, Review review)
				throws RemoteApiException, ServerPasswordNotProvidedException {
			return null;
		}

		public Review createReviewFromRevision(CrucibleServerCfg server, Review review, List<String> revisions)
				throws RemoteApiException, ServerPasswordNotProvidedException {
			return null;
		}

		public Review addRevisionsToReview(CrucibleServerCfg server, PermId permId,
										   String repository, List<String> revisions)
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

		public List<CrucibleFileInfo> getFiles(CrucibleServerCfg server, PermId permId)
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
			return comment;
		}

		public VersionedComment addVersionedComment(CrucibleServerCfg server, PermId permId,
													PermId riId, VersionedComment comment)
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

		public VersionedComment addVersionedCommentReply(CrucibleServerCfg server, PermId id,
														 PermId cId, VersionedComment comment)
				throws RemoteApiException, ServerPasswordNotProvidedException {
			return null;
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

		public void testServerConnection(String url, String userName, String password)
				throws RemoteApiException {
		}

		public void setCallback(final HttpSessionCallback callback) {
		}

		public void testServerConnection(final ServerCfg serverCfg) throws RemoteApiException {
		}

		public ServerType getServerType() {
			return null;
		}
	}

}
