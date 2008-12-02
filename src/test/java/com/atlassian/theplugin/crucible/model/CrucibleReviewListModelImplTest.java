package com.atlassian.theplugin.crucible.model;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CrucibleReviewListModelImplTest extends TestCase {

	///CLOVER:OFF
	public void setUp() throws Exception {
		super.setUp();
	}

	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAddingReviewOnce() throws Exception {
		CrucibleReviewListModel model = new CrucibleReviewListModelImpl();

		model.addReview(new ReviewAdapter(null, null));

		assertEquals(1, model.getReviews().size());
	}

	public void testAddingReviewTwice() throws Exception {
		CrucibleReviewListModel model = new CrucibleReviewListModelImpl();


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
		CrucibleReviewListModel model = new CrucibleReviewListModelImpl();


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

	public void testAddingTworeviewsWithTheSamePermId() throws Exception {
		CrucibleReviewListModel model = new CrucibleReviewListModelImpl();


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
		CrucibleReviewListModel model = new CrucibleReviewListModelImpl();


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
		CrucibleReviewListModel model = new CrucibleReviewListModelImpl();


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
		CrucibleReviewListModel model = new CrucibleReviewListModelImpl();


		ServerId id = new ServerId();
		CrucibleServerCfg cfg = new CrucibleServerCfg("test", id);
		ReviewBean r1 = new ReviewBean("test");
		r1.setPermId(new PermIdBean("test1"));
		ReviewAdapter ra1 = new ReviewAdapter(r1, cfg);

		model.addReview(ra1);
		ReviewBean r2 = new ReviewBean("test");
		r2.setPermId(new PermIdBean("test2"));
		ReviewAdapter ra2 = new ReviewAdapter(r2, cfg);

		model.addReview(ra2);
		assertEquals(2, model.getReviews().size());
		model.removeAll();
		assertEquals(0, model.getReviews().size());
	}

	private boolean reviewAddedCalled;
	private boolean reviewRemovedCalled;
	private boolean reviewChangedCalled;

	public void testListeners() throws Exception {

		reviewAddedCalled = false;
		reviewChangedCalled = false;
		reviewRemovedCalled = false;

		CrucibleReviewListModelListener l = new CrucibleReviewListModelListener() {
			public void reviewAdded(ReviewAdapter review) {
				reviewAddedCalled = true;
			}

			public void reviewRemoved(ReviewAdapter review) {
				reviewRemovedCalled = true;
			}

			public void reviewChanged(ReviewAdapter review) {
				reviewChangedCalled = true;
			}
		};

		CrucibleReviewListModel model = new CrucibleReviewListModelImpl();
		model.addListener(l);

		ServerId id = new ServerId();
		CrucibleServerCfg cfg = new CrucibleServerCfg("test", id);
		ReviewBean r1 = new ReviewBean("test");
		r1.setPermId(new PermIdBean("test1"));
		ReviewAdapter ra1 = new ReviewAdapter(r1, cfg);
		ra1.setFacade(new MyFacade());
		ra1.setGeneralComments(new ArrayList<GeneralComment>());
		model.addReview(ra1);

		assertTrue(reviewAddedCalled);

		ra1.addGeneralComment(new GeneralCommentBean());

		assertTrue(reviewChangedCalled);

		model.removeReview(ra1);

		assertTrue(reviewRemovedCalled);
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

		public List<Project> getProjects(CrucibleServerCfg server)
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

		public ServerType getServerType() {
			return null;
		}
	}

}
