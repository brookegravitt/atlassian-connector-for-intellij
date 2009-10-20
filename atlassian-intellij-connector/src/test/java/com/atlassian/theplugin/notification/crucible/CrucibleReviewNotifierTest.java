/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.notification.crucible;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.components.ComponentConfig;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.PomModel;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.messages.MessageBus;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IMocksControl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.picocontainer.PicoContainer;

import java.util.*;

public class CrucibleReviewNotifierTest extends TestCase {
	CrucibleReviewNotifier notifier;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		final Project project = new DummysProject();
//		project.putUserData(ThePluginProjectComponent.BROKER_KEY, new ReviewActionEventBroker(project));
		notifier = new CrucibleReviewNotifier(project);
	}

	private Review prepareReview() {
		return new Review("http://bogus");
	}

	private GeneralComment prepareGeneralComment(final PermId permId, final GeneralComment reply) {
		return new GeneralComment() {

			public PermId getPermId() {
				return permId;
			}

			public String getMessage() {
				return "";
			}

			public boolean isDraft() {
				return false;
			}

			public boolean isDeleted() {
				return false;
			}

			public boolean isDefectRaised() {
				return false;
			}

			public boolean isDefectApproved() {
				return false;
			}

			public boolean isReply() {
				return false;
			}

			public User getAuthor() {
				return null;
			}

			public Date getCreateDate() {
				return null;
			}

			public List<Comment> getReplies() {
				List<Comment> replies = new ArrayList<Comment>();
				if (reply != null) {
					replies.add(reply);
				}
				return replies;
			}

			public Map<String, CustomField> getCustomFields() {
				return null;
			}

            public ReadState getReadState() {
                return ReadState.READ;
            }

            public List<GeneralComment> getReplies2() {
				return reply != null ? MiscUtil.buildArrayList(reply) : MiscUtil.<GeneralComment>buildArrayList();
			}
		};
	}

	private VersionedComment prepareVersionedComment(final PermId permId, final PermId itemId, final VersionedComment reply) {
		final IMocksControl niceControl = EasyMock.createNiceControl();
		final VersionedComment mock = niceControl.createMock(VersionedComment.class);
		EasyMock.expect(mock.getPermId()).andReturn(permId).anyTimes();
		EasyMock.expect(mock.getReviewItemId()).andReturn(itemId).anyTimes();
		EasyMock.expect(mock.getMessage()).andReturn("").anyTimes();
		EasyMock.expect(mock.getReplies()).andAnswer(new IAnswer<List<Comment>>() {
			public List<Comment> answer() throws Throwable {
				final List<Comment> replies = new ArrayList<Comment>();
				if (reply != null) {
					replies.add(reply);
				}
				return replies;
			}
		});

		niceControl.replay();
		return mock;
	}

	private CrucibleFileInfo prepareReviewItem(final PermId newItem) {
		return new CrucibleFileInfo() {
			private ArrayList<VersionedComment>
					versionedComments = new ArrayList<VersionedComment>();

			public VersionedVirtualFile getOldFileDescriptor() {
				return null;
			}

			public PermId getPermId() {
				return newItem;
			}

			public String getRepositoryName() {
				return null;  //To change body of implemented methods use File | Settings | File Templates.
			}

			public FileType getFileType() {
				return null;
			}

			public String getAuthorName() {
				return null;
			}

			public Date getCommitDate() {
				return null;
			}

			public CommitType getCommitType() {
				return null;
			}

			public void addComment(final VersionedComment comment) {

			}

			public List<VersionedComment> getVersionedComments() {
				return versionedComments;
			}

			public void setVersionedComments(final List<VersionedComment> versionedComments) {

			}

			public int getNumberOfCommentsDefects() {
				return 0;
			}

			public int getNumberOfCommentsDefects(final String userName) {
				return 0;
			}

			public int getNumberOfCommentsDrafts() {
				return 0;
			}

			public int getNumberOfCommentsDrafts(final String userName) {
				return 0;
			}

			public int getNumberOfLineComments() {
				return 0;
			}

			public RepositoryType getRepositoryType() {
				return null;
			}

			public int getNumberOfComments() {
				return 0;
			}

            public int getNumberOfUnreadComments() {
                return 0;
            }

            public int getNumberOfComments(final String userName) {
				return 0;
			}

			public VersionedVirtualFile getFileDescriptor() {
				return null;
			}
		};
	}

	private List<ReviewAdapter> prepareReviewData(State state) throws ValueNotYetInitialized {
//		CrucibleFileInfoManager mgr = CrucibleFileInfoManager.getInstance();

		PermId reviewId1 = new PermId("CR-1");
		PermId newItem = new PermId("CRF:11");
		PermId newCommentId = new PermId("CMT:11");
		PermId newVCommentId = new PermId("CMT:12");

		PermId reviewId2 = new PermId("CR-2");
		PermId newItem1 = new PermId("CRF:21");
		PermId newCommentId1 = new PermId("CMT:21");
		PermId newVCommentId1 = new PermId("CMT:22");


		List<ReviewAdapter> reviews = new ArrayList<ReviewAdapter>();

		Reviewer reviewer1 = new Reviewer("bob", "Bob", false);
		Reviewer reviewer2 = new Reviewer("alice", "Alice", false);
		Reviewer reviewer3 = new Reviewer("scott", "Scott", false);
		Reviewer reviewer4 = new Reviewer("alice", "Alice", false);

		Review review1 = prepareReview();
		((Review) review1).setGeneralComments(new ArrayList<GeneralComment>());
//		((Review) review1).setVersionedComments(new ArrayList<VersionedComment>());
		((Review) review1).setPermId(reviewId1);
		((Review) review1).setState(state);
		((Review) review1).setReviewers(new HashSet(Arrays.asList(reviewer1, reviewer2)));


		review1.getGeneralComments().add(prepareGeneralComment(newCommentId, null));
		CrucibleFileInfo file1 = prepareReviewItem(newItem);
		file1.getVersionedComments().add(prepareVersionedComment(newVCommentId, newItem, null));
		Set<CrucibleFileInfo> files1 = new HashSet<CrucibleFileInfo>();
		files1.add(file1);

		review1.setFilesAndVersionedComments(files1, null);

//		mgr.setFiles(review1, files1);


		Review review2 = prepareReview();
		review2.setGeneralComments(new ArrayList<GeneralComment>());
//		((Review) review2).setVersionedComments(new ArrayList<VersionedComment>());
		review2.setPermId(reviewId2);
		review2.setState(state);
		review2.setReviewers(new HashSet(Arrays.asList(reviewer3, reviewer4)));

		review2.getGeneralComments().add(prepareGeneralComment(newCommentId1, null));
		CrucibleFileInfo file2 = prepareReviewItem(newItem1);
		file2.getVersionedComments().add(prepareVersionedComment(newVCommentId1, newItem1, null));
		Set<CrucibleFileInfo> files2 = new HashSet<CrucibleFileInfo>();
		files2.add(file2);

		review2.setFilesAndVersionedComments(files2, null);

//		mgr.setFiles(review2, files2);

		reviews.add(new ReviewAdapter(review1, null, null));
		reviews.add(new ReviewAdapter(review2, null, null));

		return reviews;
	}

	/*
	  public void testNewReviews() throws ValueNotYetInitialized {
		  List<ReviewAdapter> reviews = prepareReviewData(State.REVIEW);
		  UpdateContext updateContext = new UpdateContext(UpdateReason.TIMER_FIRED, null, null);

		  notifier.reviewListUpdateStarted(updateContext);
		  assertEquals(0, notifier.getNotifications().size());

		  for (ReviewAdapter review : reviews) {
			  updateContext = new UpdateContext(UpdateReason.TIMER_FIRED, review, null);
			  notifier.reviewAdded(updateContext);
		  }

		  notifier.reviewListUpdateFinished(updateContext);
		  assertEquals(reviews.size(), notifier.getNotifications().size());

		  notifier.reviewListUpdateStarted(updateContext);
		  assertEquals(0, notifier.getNotifications().size());
	  }

	  public void testResetStateReviews() throws ValueNotYetInitialized {
		  UpdateContext updateContext = new UpdateContext(UpdateReason.TIMER_FIRED, null, null);
		  List<ReviewAdapter> reviews = prepareReviewData(State.REVIEW);

		  notifier.reviewListUpdateStarted(updateContext);
		  assertEquals(0, notifier.getNotifications().size());

		  for (ReviewAdapter review : reviews) {
			  updateContext = new UpdateContext(UpdateReason.TIMER_FIRED, review, null);
			  notifier.reviewAdded(updateContext);
		  }
		  assertEquals(2, notifier.getNotifications().size());
		  assertTrue(notifier.getNotifications().get(0) instanceof NewReviewNotification);
		  assertTrue(notifier.getNotifications().get(1) instanceof NewReviewNotification);
		  notifier.reviewListUpdateFinished(updateContext);


		  notifier.reviewListUpdateStarted(updateContext);
		  assertEquals(0, notifier.getNotifications().size());
		  notifier.reviewListUpdateFinished(updateContext);
	  }

	  public void testStatusChange() throws ValueNotYetInitialized {
		  UpdateContext updateContext = new UpdateContext(UpdateReason.TIMER_FIRED, null, null);
		  List<ReviewAdapter> reviews_review = prepareReviewData(State.REVIEW);
		  List<ReviewAdapter> reviews_closed = prepareReviewData(State.CLOSED);


		  notifier.reviewListUpdateStarted(updateContext);

		  updateContext = new UpdateContext(UpdateReason.TIMER_FIRED, reviews_closed.get(0), null);
		  updateContext.setOldReviewAdapter(reviews_review.get(0));
		  notifier.reviewChanged(updateContext);

		  updateContext = new UpdateContext(UpdateReason.TIMER_FIRED, reviews_closed.get(1), null);
		  updateContext.setOldReviewAdapter(reviews_review.get(1));
		  notifier.reviewChanged(updateContext);


		  assertEquals(2, notifier.getNotifications().size());

		  assertTrue(notifier.getNotifications().get(0) instanceof ReviewStateChangedNotification);
		  assertTrue(notifier.getNotifications().get(1) instanceof ReviewStateChangedNotification);
	  }

	  public void testReviewerStatus() throws ValueNotYetInitialized {
		  List<ReviewAdapter> reviews_review = prepareReviewData(State.REVIEW);
		  List<ReviewAdapter> reviews_review_final = prepareReviewData(State.REVIEW);
		  UpdateContext updateContext = new UpdateContext(UpdateReason.TIMER_FIRED, reviews_review_final.get(0), null);


		  notifier.reviewListUpdateStarted(updateContext);
		  assertEquals(0, notifier.getNotifications().size());

		  for (ReviewAdapter review : reviews_review_final) {
			  updateContext = new UpdateContext(UpdateReason.TIMER_FIRED, review, null);
			  notifier.reviewAdded(updateContext);
		  }

		  assertEquals(reviews_review.size(), notifier.getNotifications().size());
		  notifier.reviewListUpdateFinished(updateContext);


		  notifier.reviewListUpdateStarted(updateContext);
		  ((ReviewerBean) reviews_review.get(0).getReviewers().toArray()[0]).setCompleted(true);
		  updateContext = new UpdateContext(UpdateReason.TIMER_FIRED, reviews_review.get(0), null);
		  updateContext.setOldReviewAdapter(reviews_review_final.get(0));

		  notifier.reviewChangedWithoutFiles(updateContext);
		  assertEquals(1, notifier.getNotifications().size());
		  assertTrue(notifier.getNotifications().get(0) instanceof ReviewerCompletedNotification);
		  notifier.reviewListUpdateFinished(updateContext);


		  notifier.reviewListUpdateStarted(updateContext);
		  updateContext = new UpdateContext(UpdateReason.TIMER_FIRED, reviews_review.get(0), null);
		  updateContext.setOldReviewAdapter(reviews_review_final.get(0));
		  notifier.reviewChangedWithoutFiles(updateContext);
		  assertEquals(1, notifier.getNotifications().size());
		  assertTrue(notifier.getNotifications().get(0) instanceof ReviewerCompletedNotification);
		  notifier.reviewListUpdateFinished(updateContext);

		  notifier.reviewListUpdateStarted(updateContext);
		  reviews_review = prepareReviewData(State.REVIEW);
		  ((ReviewerBean) reviews_review.get(0).getReviewers().toArray()[0]).setCompleted(true);
		  ((ReviewerBean) reviews_review.get(0).getReviewers().toArray()[1]).setCompleted(true);

		  updateContext = new UpdateContext(UpdateReason.TIMER_FIRED, reviews_review.get(0), null);
		  updateContext.setOldReviewAdapter(reviews_review_final.get(0));
		  notifier.reviewChangedWithoutFiles(updateContext);
		  assertEquals(3, notifier.getNotifications().size());
		  assertTrue(notifier.getNotifications().get(0) instanceof ReviewerCompletedNotification);
		  assertTrue(notifier.getNotifications().get(1) instanceof ReviewerCompletedNotification);
		  assertTrue(notifier.getNotifications().get(2) instanceof ReviewCompletedNotification);
		  notifier.reviewListUpdateFinished(updateContext);
	  }
 */
	/*
	 public void testNewItem() throws ValueNotYetInitialized {
		 List<ReviewAdapter> reviews = prepareReviewData(State.REVIEW);
		 ReviewNotificationBean bean = new ReviewNotificationBean();

		 Map<PredefinedFilter, ReviewNotificationBean> map = new HashMap<PredefinedFilter, ReviewNotificationBean>();

		 bean.setReviews(reviews);
		 map.put(PredefinedFilter.ToReview, bean);
		 notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
		 assertEquals(reviews.size(), notifier.getNotifications().size());
 //
 //		reviews = prepareReviewData(State.REVIEW);
 //
 //		final PermId newItem = new PermId() {
 //
 //			public String getId() {
 //				return "CRF:2";
 //			}
 //		};
 //
 //		reviews.get(0).getInnerReviewObject().setPermId(newItem);
 //
 ////		reviews.get(0).getReviewItems().add(new CrucibleReviewItemInfo(newItem));
 //		CrucibleFileInfoManager.getInstance().getFiles(reviews.get(0).getInnerReviewObject()).add(new CrucibleFileInfo() {
 //
 //			public VersionedVirtualFile getOldFileDescriptor() {
 //				return null;
 //			}
 //
 //			public int getNumberOfComments() {
 //				return 0;
 //			}
 //
 //			public int getNumberOfComments(final String userName) {
 //				return 0;
 //			}
 //
 //			public int getNumberOfCommentsDefects() {
 //				return 0;
 //			}
 //
 //			public int getNumberOfCommentsDefects(final String userName) {
 //				return 0;
 //			}
 //
 //			public int getNumberOfCommentsDrafts() {
 //				return 0;
 //			}
 //
 //			public int getNumberOfCommentsDrafts(final String userName) {
 //				return 0;
 //			}
 //
 //			public PermId getPermId() {
 //				return newItem;
 //			}
 //
 //			public List<VersionedComment> getVersionedComments() {
 //				return new ArrayList<VersionedComment>();
 //			}
 //
 //			public void setVersionedComments(final List<VersionedComment> versionedComments) {
 //
 //			}
 //
 //			public String getRepositoryName() {
 //				return null;
 //			}
 //
 //			public FileType getFileType() {
 //				return null;
 //			}
 //
 //			public String getAuthorName() {
 //				return null;
 //			}
 //
 //			public Date getCommitDate() {
 //				return null;
 //			}
 //
 //			public CommitType getCommitType() {
 //				return null;
 //			}
 //
 //			public void addComment(final VersionedComment comment) {
 //
 //			}
 //
 //			public VersionedVirtualFile getFileDescriptor() {
 //				return null;
 //			}
 //		});
 //
 //		bean.setReviews(reviews);
 //		map.put(PredefinedFilter.ToReview, bean);
 //		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
 //		assertEquals(1, notifier.getNotifications().size());
 //		assertTrue(notifier.getNotifications().get(0) instanceof NewReviewNotification);
	 }

	 public void testNewGeneralComment() throws ValueNotYetInitialized {
		 List<ReviewAdapter> reviews_review = prepareReviewData(State.REVIEW);
	 List<ReviewAdapter> reviews_review_final = prepareReviewData(State.REVIEW);
		 UpdateContext updateContext = new UpdateContext(UpdateReason.TIMER_FIRED, reviews_review_final.get(0));


		 notifier.reviewListUpdateStarted(updateContext);
		 assertEquals(0, notifier.getNotifications().size());

		 for (ReviewAdapter review : reviews_review_final) {
			 updateContext = new UpdateContext(UpdateReason.TIMER_FIRED, review);
			 notifier.reviewAdded(updateContext);
		 }

		 assertEquals(reviews_review_final.size(), notifier.getNotifications().size());
		 notifier.reviewListUpdateFinished(updateContext);

		 final PermId newCommentId = new PermId() {

			 public String getId() {
				 return "CMT:2";
			 }
		 };
		 reviews_review.get(0).getGeneralComments().add(new GeneralComment() {

			 public PermId getPermId() {
				 return newCommentId;
			 }

			 public String getMessage() {
				 return "";
			 }

			 public boolean isDraft() {
				 return false;
			 }

			 public boolean isDeleted() {
				 return false;
			 }

			 public boolean isDefectRaised() {
				 return false;
			 }

			 public boolean isDefectApproved() {
				 return false;
			 }

			 public boolean isReply() {
				 return false;
			 }

			 public User getAuthor() {
				 return null;
			 }

			 public Date getCreateDate() {
				 return null;
			 }

			 public List<GeneralComment> getReplies() {
				 return new ArrayList<GeneralComment>();
			 }

			 public Map<String, CustomField> getCustomFields() {
				 return null;
			 }
		 });

		 notifier.reviewListUpdateStarted(updateContext);
		 updateContext = new UpdateContext(UpdateReason.TIMER_FIRED, reviews_review.get(0));
		 updateContext.setOldReviewAdapter(reviews_review_final.get(0));
		 notifier.reviewChangedWithoutFiles(updateContext);
		 assertEquals(1, notifier.getNotifications().size());
		 assertTrue(notifier.getNotifications().get(0) instanceof NewGeneralCommentNotification);
	 }

	 public void xtestNewVersionedComment() throws ValueNotYetInitialized {
 //		List<ReviewAdapter> reviews = prepareReviewData(State.REVIEW);
 //		ReviewNotificationBean bean = new ReviewNotificationBean();
 //
 //		Map<PredefinedFilter, ReviewNotificationBean> map = new HashMap<PredefinedFilter, ReviewNotificationBean>();
 //
 //		bean.setReviews(reviews);
 //		map.put(PredefinedFilter.ToReview, bean);
 //		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
 //		assertEquals(reviews.size(), notifier.getNotifications().size());
 //
 //		reviews = prepareReviewData(State.REVIEW);
 //
 //		final PermId newCommentId = new PermId() {
 //
 //			public String getId() {
 //				return "CMT:3";
 //			}
 //		};
 //		final PermId newId = new PermId() {
 //
 //			public String getId() {
 //				return "CRF:2";
 //			}
 //		};
 //
 //		CrucibleFileInfoManager mgr = CrucibleFileInfoManager.getInstance();
 //		PermId newPermlId = new PermId("CMT:100");
 //		mgr.getFiles(reviews.get(0).getInnerReviewObject()).get(0).getVersionedComments()
 //				.add(prepareVersionedComment(newPermlId, mgr.getFiles(reviews.get(0).getInnerReviewObject()).get(0).getPermId(), null));
 //		bean.setReviews(reviews);
 //		map.put(PredefinedFilter.ToReview, bean);
 //		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
 //		assertEquals(1, notifier.getNotifications().size());
 //		assertTrue(notifier.getNotifications().get(0) instanceof NewVersionedCommentNotification);
	 }
 */
	@SuppressWarnings("deprecation")
	private class DummysProject extends UserDataHolderBase implements Project {
		@Nullable
		public VirtualFile getProjectFile() {
			return null;
		}

		@Nullable
		public VirtualFile getWorkspaceFile() {
			return null;
		}

		@NotNull
		public String getProjectFilePath() {
			return null;
		}

		@Nullable
		public VirtualFile getBaseDir() {
			return null;
		}

		@NotNull
		@NonNls
		public String getName() {
			return null;
		}

		@Nullable
		@NonNls
		public String getPresentableUrl() {
			return null;
		}

		@NotNull
		@NonNls
		public String getLocationHash() {
			return null;
		}

		@NotNull
		@NonNls
		public String getLocation() {
			return null;
		}

		public void save() {

		}

		public BaseComponent getComponent(final String name) {
			return null;
		}

		public <T> T getComponent(final Class<T> interfaceClass) {
			return null;
		}

		public <T> T getComponent(final Class<T> interfaceClass, final T defaultImplementationIfAbsent) {
			return null;
		}

		@NotNull
		public Class[] getComponentInterfaces() {
			return new Class[0];
		}

		public boolean hasComponent(@NotNull final Class interfaceClass) {
			return false;
		}

		@NotNull
		public <T> T[] getComponents(final Class<T> baseClass) {
			return null;
		}

		@NotNull
		public PicoContainer getPicoContainer() {
			return null;
		}

		public MessageBus getMessageBus() {
			return null;
		}

		public boolean isDisposed() {
			return false;
		}

		@NotNull
		public ComponentConfig[] getComponentConfigurations() {
			return new ComponentConfig[0];
		}


		@Nullable
		public Object getComponent(final ComponentConfig componentConfig) {
			return null;
		}

		public <T> T[] getExtensions(final ExtensionPointName<T> extensionPointName) {
			return null;
		}

		public ComponentConfig getConfig(final Class componentImplementation) {
			return null;
		}

		public Condition getDisposed() {
			return null;
		}

		public boolean isOpen() {
			return false;
		}

		public boolean isInitialized() {
			return false;
		}

		public boolean isDefault() {
			return false;
		}

		@NotNull
		public PomModel getModel() {
			return null;
		}

		public GlobalSearchScope getAllScope() {
			return null;
		}

		public GlobalSearchScope getProjectScope() {
			return null;
		}

		public void dispose() {

		}
	}

	public void testUncommentTests() {
		assertTrue(true);
	}
}
