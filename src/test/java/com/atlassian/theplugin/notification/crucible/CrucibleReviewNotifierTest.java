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

import com.atlassian.theplugin.commons.VersionedVirtualFile;
import com.atlassian.theplugin.commons.crucible.CrucibleFileInfoManager;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.atlassian.theplugin.idea.crucible.ReviewAdapter;
import com.atlassian.theplugin.idea.crucible.ReviewNotificationBean;
import com.atlassian.theplugin.idea.crucible.comments.ReviewActionEventBroker;
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
		project.putUserData(ThePluginProjectComponent.BROKER_KEY, new ReviewActionEventBroker(project));
		notifier = new CrucibleReviewNotifier(project);
	}

	private ReviewBean prepareReview() {
		return new ReviewBean("http://bogus");
	}

	private ReviewerBean prepareReviewer(String userName, String displayName, boolean completed) {
		ReviewerBean reviewer = new ReviewerBean();
		reviewer.setUserName(userName);
		reviewer.setDisplayName(displayName);
		reviewer.setCompleted(completed);

		return reviewer;
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

			public List<GeneralComment> getReplies() {
				List<GeneralComment> replies = new ArrayList<GeneralComment>();
				if (reply != null) {
					replies.add(reply);
				}
				return replies;
			}

			public Map<String, CustomField> getCustomFields() {
				return null;
			}

		};
	}

	private VersionedComment prepareVersionedComment(final PermId permId, final PermId itemId, final VersionedComment reply) {
		return new VersionedComment() {

			public PermId getPermId() {
				return permId;
			}

			public PermId getReviewItemId() {
				return itemId;
			}

			public boolean isToLineInfo() {
				return false;  //To change body of implemented methods use File | Settings | File Templates.
			}

			public int getToStartLine() {
				return 0;  //To change body of implemented methods use File | Settings | File Templates.
			}

			public int getToEndLine() {
				return 0;  //To change body of implemented methods use File | Settings | File Templates.
			}

			public boolean isFromLineInfo() {
				return false;  //To change body of implemented methods use File | Settings | File Templates.
			}

			public int getFromStartLine() {
				return 0;  //To change body of implemented methods use File | Settings | File Templates.
			}

			public int getFromEndLine() {
				return 0;  //To change body of implemented methods use File | Settings | File Templates.
			}

			public String getMessage() {
				return null;  //To change body of implemented methods use File | Settings | File Templates.
			}

			public boolean isDraft() {
				return false;  //To change body of implemented methods use File | Settings | File Templates.
			}

			public boolean isDeleted() {
				return false;  //To change body of implemented methods use File | Settings | File Templates.
			}

			public boolean isDefectRaised() {
				return false;  //To change body of implemented methods use File | Settings | File Templates.
			}

			public boolean isDefectApproved() {
				return false;  //To change body of implemented methods use File | Settings | File Templates.
			}

			public boolean isReply() {
				return false;  //To change body of implemented methods use File | Settings | File Templates.
			}

			public User getAuthor() {
				return null;  //To change body of implemented methods use File | Settings | File Templates.
			}

			public Date getCreateDate() {
				return null;  //To change body of implemented methods use File | Settings | File Templates.
			}

			public List<VersionedComment> getReplies() {
				List<VersionedComment> replies = new ArrayList<VersionedComment>();
				if (reply != null) {
					replies.add(reply);
				}
				return replies;
			}

			public Map<String, CustomField> getCustomFields() {
				return null;  //To change body of implemented methods use File | Settings | File Templates.
			}
		};
	}

	private CrucibleFileInfo prepareReviewItem(final PermId newItem) {
		return new CrucibleFileInfo() {
			private ArrayList<VersionedComment>
					comments = new ArrayList<VersionedComment>();

			public VersionedVirtualFile getOldFileDescriptor() {
				return null;
			}

//			public int getNumberOfComments() throws ValueNotYetInitialized {
//				return 0;
//			}
//
//			public int getNumberOfDefects() throws ValueNotYetInitialized {
//				return 0;
//			}
//
			public PermId getPermId() {
				return newItem;
			}

//			public List<VersionedComment> getVersionedComments() throws ValueNotYetInitialized {
//				return comments;
//			}

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

			public VersionedVirtualFile getFileDescriptor() {
				return null;
			}

			public CrucibleReviewItemInfo getItemInfo() {
				return new CrucibleReviewItemInfo(newItem);
			}
		};
	}

	private List<ReviewAdapter> prepareReviewData(State state) throws ValueNotYetInitialized {
		CrucibleFileInfoManager mgr = CrucibleFileInfoManager.getInstance();

		PermIdBean reviewId1 = new PermIdBean("CR-1");
		PermIdBean newItem = new PermIdBean("CRF:11");
		PermIdBean newCommentId = new PermIdBean("CMT:11");
		PermIdBean newVCommentId = new PermIdBean("CMT:12");

		PermIdBean reviewId2 = new PermIdBean("CR-2");
		PermIdBean newItem1 = new PermIdBean("CRF:21");
		PermIdBean newCommentId1 = new PermIdBean("CMT:21");
		PermIdBean newVCommentId1 = new PermIdBean("CMT:22");


		List<ReviewAdapter> reviews = new ArrayList<ReviewAdapter>();

		Reviewer reviewer1 = prepareReviewer("bob", "Bob", false);
		Reviewer reviewer2 = prepareReviewer("alice", "Alice", false);
		Reviewer reviewer3 = prepareReviewer("scott", "Scott", false);
		Reviewer reviewer4 = prepareReviewer("alice", "Alice", false);

		Review review1 = prepareReview();
		((ReviewBean) review1).setGeneralComments(new ArrayList<GeneralComment>());
//		((ReviewBean) review1).setVersionedComments(new ArrayList<VersionedComment>());
		((ReviewBean) review1).setPermId(reviewId1);
		((ReviewBean) review1).setState(state);
		((ReviewBean) review1).setReviewers(Arrays.asList(reviewer1, reviewer2));


		review1.getGeneralComments().add(prepareGeneralComment(newCommentId, null));
		CrucibleFileInfo file1 = prepareReviewItem(newItem);
		file1.getItemInfo().getComments().add(prepareVersionedComment(newVCommentId, newItem, null));
		ArrayList<CrucibleFileInfo> files1 = new ArrayList<CrucibleFileInfo>();
		files1.add(file1);
		mgr.setFiles(review1, files1);


		Review review2 = prepareReview();
		((ReviewBean) review2).setGeneralComments(new ArrayList<GeneralComment>());
//		((ReviewBean) review2).setVersionedComments(new ArrayList<VersionedComment>());
		((ReviewBean) review2).setPermId(reviewId2);
		((ReviewBean) review2).setState(state);
		((ReviewBean) review2).setReviewers(Arrays.asList(reviewer3, reviewer4));

		review2.getGeneralComments().add(prepareGeneralComment(newCommentId1, null));
		CrucibleFileInfo file2 = prepareReviewItem(newItem1);
		file2.getItemInfo().getComments().add(prepareVersionedComment(newVCommentId1, newItem1, null));
		ArrayList<CrucibleFileInfo> files2 = new ArrayList<CrucibleFileInfo>();
		files2.add(file2);
		mgr.setFiles(review2, files2);

		reviews.add(new ReviewAdapter(review1, null));
		reviews.add(new ReviewAdapter(review2, null));

		return reviews;
	}

	public void testNewReviews() throws ValueNotYetInitialized {
		List<ReviewAdapter> emptyReviews = new ArrayList<ReviewAdapter>();
		List<ReviewAdapter> reviews = prepareReviewData(State.REVIEW);
		ReviewNotificationBean bean = new ReviewNotificationBean();

		Map<PredefinedFilter, ReviewNotificationBean> map = new HashMap<PredefinedFilter, ReviewNotificationBean>();
		bean.setReviews(emptyReviews);
		map.put(PredefinedFilter.ToReview, bean);

		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
		assertEquals(0, notifier.getNotifications().size());

		bean.setReviews(reviews);
		map.put(PredefinedFilter.ToReview, bean);
		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
		assertEquals(reviews.size(), notifier.getNotifications().size());

		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
		assertEquals(0, notifier.getNotifications().size());
	}

	public void testResetStateReviews() throws ValueNotYetInitialized {
		List<ReviewAdapter> emptyReviews = new ArrayList<ReviewAdapter>();
		List<ReviewAdapter> reviews = prepareReviewData(State.REVIEW);
		ReviewNotificationBean bean = new ReviewNotificationBean();

		Map<PredefinedFilter, ReviewNotificationBean> map = new HashMap<PredefinedFilter, ReviewNotificationBean>();
		bean.setReviews(emptyReviews);
		map.put(PredefinedFilter.ToReview, bean);

		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
		assertEquals(0, notifier.getNotifications().size());

		bean.setReviews(reviews);
		map.put(PredefinedFilter.ToReview, bean);
		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
		assertEquals(2, notifier.getNotifications().size());
		assertTrue(notifier.getNotifications().get(0) instanceof NewReviewNotification);
		assertTrue(notifier.getNotifications().get(1) instanceof NewReviewNotification);

		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
		assertEquals(0, notifier.getNotifications().size());

		notifier.resetState();

		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
		assertEquals(2, notifier.getNotifications().size());
		assertTrue(notifier.getNotifications().get(0) instanceof NewReviewNotification);
		assertTrue(notifier.getNotifications().get(1) instanceof NewReviewNotification);
	}

	public void testStatusChange() throws ValueNotYetInitialized {
		List<ReviewAdapter> reviews = prepareReviewData(State.REVIEW);

		Map<PredefinedFilter, ReviewNotificationBean> map = new HashMap<PredefinedFilter, ReviewNotificationBean>();
		ReviewNotificationBean bean = new ReviewNotificationBean();

		bean.setReviews(reviews);
		map.put(PredefinedFilter.ToReview, bean);
		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
		assertEquals(reviews.size(), notifier.getNotifications().size());

		reviews = prepareReviewData(State.CLOSED);
		bean.setReviews(reviews);
		map.put(PredefinedFilter.ToReview, bean);
		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
		assertEquals(2, notifier.getNotifications().size());
		assertTrue(notifier.getNotifications().get(0) instanceof ReviewStateChangedNotification);
		assertTrue(notifier.getNotifications().get(1) instanceof ReviewStateChangedNotification);
	}

	public void testReviewerStatus() throws ValueNotYetInitialized {
		List<ReviewAdapter> reviews = prepareReviewData(State.REVIEW);
		ReviewNotificationBean bean = new ReviewNotificationBean();

		Map<PredefinedFilter, ReviewNotificationBean> map = new HashMap<PredefinedFilter, ReviewNotificationBean>();

		bean.setReviews(reviews);
		map.put(PredefinedFilter.ToReview, bean);
		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
		assertEquals(reviews.size(), notifier.getNotifications().size());

		reviews = prepareReviewData(State.REVIEW);
		((ReviewerBean) reviews.get(0).getReviewers().get(0)).setCompleted(true);
		bean.setReviews(reviews);
		map.put(PredefinedFilter.ToReview, bean);
		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
		assertEquals(1, notifier.getNotifications().size());
		assertTrue(notifier.getNotifications().get(0) instanceof ReviewerCompletedNotification);

		reviews = prepareReviewData(State.REVIEW);
		((ReviewerBean) reviews.get(0).getReviewers().get(0)).setCompleted(false);
		bean.setReviews(reviews);
		map.put(PredefinedFilter.ToReview, bean);
		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
		assertEquals(1, notifier.getNotifications().size());
		assertTrue(notifier.getNotifications().get(0) instanceof ReviewerCompletedNotification);

		reviews = prepareReviewData(State.REVIEW);
		((ReviewerBean) reviews.get(0).getReviewers().get(0)).setCompleted(true);
		((ReviewerBean) reviews.get(0).getReviewers().get(1)).setCompleted(true);
		bean.setReviews(reviews);
		map.put(PredefinedFilter.ToReview, bean);
		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
		assertEquals(3, notifier.getNotifications().size());
		assertTrue(notifier.getNotifications().get(0) instanceof ReviewerCompletedNotification);
		assertTrue(notifier.getNotifications().get(1) instanceof ReviewerCompletedNotification);
		assertTrue(notifier.getNotifications().get(2) instanceof ReviewCompletedNotification);
	}

	public void testNewItem() throws ValueNotYetInitialized {
		List<ReviewAdapter> reviews = prepareReviewData(State.REVIEW);
		ReviewNotificationBean bean = new ReviewNotificationBean();

		Map<PredefinedFilter, ReviewNotificationBean> map = new HashMap<PredefinedFilter, ReviewNotificationBean>();

		bean.setReviews(reviews);
		map.put(PredefinedFilter.ToReview, bean);
		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
		assertEquals(reviews.size(), notifier.getNotifications().size());

		reviews = prepareReviewData(State.REVIEW);

		final PermId newItem = new PermId() {

			public String getId() {
				return "CRF:2";
			}
		};
		reviews.get(0).getReviewItems().add(new CrucibleReviewItemInfo(newItem));
		CrucibleFileInfoManager.getInstance().getFiles(reviews.get(0).getInnerReviewObject()).add(new CrucibleFileInfo() {

			public VersionedVirtualFile getOldFileDescriptor() {
				return null;
			}

			public int getNumberOfComments() throws ValueNotYetInitialized {
				return 0;
			}

			public int getNumberOfDefects() throws ValueNotYetInitialized {
				return 0;
			}

			public PermId getPermId() {
				return newItem;
			}

			public List<VersionedComment> getVersionedComments() throws ValueNotYetInitialized {
				return new ArrayList<VersionedComment>();
			}

			public String getRepositoryName() {
				return null;
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

			public VersionedVirtualFile getFileDescriptor() {
				return null;
			}

			public CrucibleReviewItemInfo getItemInfo() {
				return new CrucibleReviewItemInfo(newItem);
			}
		});
		bean.setReviews(reviews);
		map.put(PredefinedFilter.ToReview, bean);
		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
		assertEquals(1, notifier.getNotifications().size());
		assertTrue(notifier.getNotifications().get(0) instanceof NewReviewItemNotification);
	}

	public void testNewGeneralComment() throws ValueNotYetInitialized {
		List<ReviewAdapter> reviews = prepareReviewData(State.REVIEW);
		ReviewNotificationBean bean = new ReviewNotificationBean();

		Map<PredefinedFilter, ReviewNotificationBean> map = new HashMap<PredefinedFilter, ReviewNotificationBean>();

		bean.setReviews(reviews);
		map.put(PredefinedFilter.ToReview, bean);
		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
		assertEquals(reviews.size(), notifier.getNotifications().size());

		reviews = prepareReviewData(State.REVIEW);

		final PermId newCommentId = new PermId() {

			public String getId() {
				return "CMT:2";
			}
		};
		reviews.get(0).getGeneralComments().add(new GeneralComment() {

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
		bean.setReviews(reviews);
		map.put(PredefinedFilter.ToReview, bean);
		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
		assertEquals(1, notifier.getNotifications().size());
		assertTrue(notifier.getNotifications().get(0) instanceof NewGeneralCommentNotification);
	}

	public void xtestNewVersionedComment() throws ValueNotYetInitialized {
		List<ReviewAdapter> reviews = prepareReviewData(State.REVIEW);
		ReviewNotificationBean bean = new ReviewNotificationBean();

		Map<PredefinedFilter, ReviewNotificationBean> map = new HashMap<PredefinedFilter, ReviewNotificationBean>();

		bean.setReviews(reviews);
		map.put(PredefinedFilter.ToReview, bean);
		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
		assertEquals(reviews.size(), notifier.getNotifications().size());

		reviews = prepareReviewData(State.REVIEW);

		final PermId newCommentId = new PermId() {

			public String getId() {
				return "CMT:3";
			}
		};
		final PermId newId = new PermId() {

			public String getId() {
				return "CRF:2";
			}
		};

		CrucibleFileInfoManager mgr = CrucibleFileInfoManager.getInstance();
		PermIdBean newPermlId = new PermIdBean("CMT:100");
		mgr.getFiles(reviews.get(0).getInnerReviewObject()).get(0).getItemInfo().getComments()
				.add(prepareVersionedComment(newPermlId, mgr.getFiles(reviews.get(0).getInnerReviewObject()).get(0).getItemInfo().getId(), null));
		bean.setReviews(reviews);
		map.put(PredefinedFilter.ToReview, bean);
		notifier.updateReviews(map, new HashMap<String, ReviewNotificationBean>());
		assertEquals(1, notifier.getNotifications().size());
		assertTrue(notifier.getNotifications().get(0) instanceof NewVersionedCommentNotification);
	}

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
}
