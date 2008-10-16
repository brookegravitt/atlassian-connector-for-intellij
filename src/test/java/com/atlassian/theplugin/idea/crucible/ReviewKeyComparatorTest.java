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

package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.*;
import com.atlassian.theplugin.idea.crucible.table.column.ReviewKeyComparator;
import com.atlassian.theplugin.commons.VirtualFileSystem;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import junit.framework.TestCase;

import java.util.List;
import java.util.Date;

public class ReviewKeyComparatorTest extends TestCase {

	public void testComparator() {
		ReviewKeyComparator comparator = new ReviewKeyComparator();

		assertEquals(0, comparator.compare(getReviewAdapter(null, null), getReviewAdapter(null, null)));
		assertEquals(0, comparator.compare(getReviewAdapter("CR", null), getReviewAdapter("CR", null)));
		assertEquals(0, comparator.compare(getReviewAdapter("CR", null), getReviewAdapter("CR", "")));
		assertEquals(0, comparator.compare(getReviewAdapter("CR", ""), getReviewAdapter("CR", null)));
		assertEquals(0, comparator.compare(getReviewAdapter("CR", ""), getReviewAdapter("CR", "")));
		assertEquals(-1, comparator.compare(getReviewAdapter("CR1", ""), getReviewAdapter("CR2", "")));
		assertEquals(-1, comparator.compare(getReviewAdapter("A", ""), getReviewAdapter("B", "")));
		assertEquals(0, comparator.compare(getReviewAdapter("CR", "freeText"), getReviewAdapter("CR", "freeText")));
		assertEquals(0, comparator.compare(getReviewAdapter("CR", "freeText1"), getReviewAdapter("CR", "freeText2")));
		assertEquals(-1, comparator.compare(getReviewAdapter("CR", "CR-1"), getReviewAdapter("CR", "CR-2")));
		assertEquals(1, comparator.compare(getReviewAdapter("CR", "CR-2"), getReviewAdapter("CR", "CR-1")));
		assertEquals(0, comparator.compare(getReviewAdapter("CR", "CR-FE-1"), getReviewAdapter("CR", "CR-FE-1")));
		assertEquals(-1, comparator.compare(getReviewAdapter("CR", "CR-FE-1"), getReviewAdapter("CR", "CR-FE-2")));
		assertEquals(1, comparator.compare(getReviewAdapter("CR", "CR-FE-3"), getReviewAdapter("CR", "CR-FE-1")));
		assertTrue(comparator.compare(getReviewAdapter("CR", "CR-1"), getReviewAdapter("CR-FE", "CR-FE-1")) < 0);
		assertTrue(comparator.compare(getReviewAdapter("CR-FE", "CR-FE-1"), getReviewAdapter("CR", "CR-1")) > 0);
		assertEquals(0, comparator.compare(getReviewAdapter("CR", "-1"), getReviewAdapter("CR", "-1")));		
	}

	private ReviewAdapter getReviewAdapter(final String projectKey, final String key) {
		Review review = new Review() {

			public List<Reviewer> getReviewers() {
				return null;
			}

			public List<GeneralComment> getGeneralComments() {
				return null;
			}

            public List<VersionedComment> getVersionedComments() throws ValueNotYetInitialized {
                return null;
            }

            public List<CrucibleFileInfo> getFiles() throws ValueNotYetInitialized {
				return null;
			}

			public List<Action> getTransitions() throws ValueNotYetInitialized {
				return null;
			}

            public List<Action> getActions() throws ValueNotYetInitialized {
                return null; 
            }

            public VirtualFileSystem getVirtualFileSystem() {
				return null;  
			}

			public CrucibleServerCfg getServer() {
				return null;
			}

			public String getReviewUrl() {
				return null;
			}

			public Review getInnerReviewObject() {
				return null;
			}

			public void setGeneralComments(final List<GeneralComment> generalComments) {
				// not implemented
			}

			public void removeGeneralComment(final GeneralComment comment) {
				// not implemented
			}

			public void removeVersionedComment(final VersionedComment versionedComment) {
				// not implemented
			}

			public User getAuthor() {
				return null;
			}

			public User getCreator() {
				return null;
			}

			public String getDescription() {
				return null;
			}

			public User getModerator() {
				return null;
			}

			public String getName() {
				return null;
			}

			public PermId getParentReview() {
				return null;
			}

			public PermId getPermId() {
				return new PermId() {

					public String getId() {
						return key;
					}
				};
			}

			public String getProjectKey() {
				return projectKey;
			}

			public String getRepoName() {
				return null;
			}

			public State getState() {
				return null;
			}

            public boolean isAllowReviewerToJoin() {
                return false;  
            }

            public int getMetricsVersion() {
                return 0;
            }

            public Date getCreateDate() {
                return null;
            }

            public Date getCloseDate() {
                return null;  
            }

            public String getSummary() {
                return null; 
            }

			public List<CrucibleReviewItemInfo> getReviewItems() {
				return null;
			}

			public CrucibleFileInfo getFileByPermId(PermId id) {
				return null;
			}

			public CrucibleFileInfo getFileByReviewInfo(CrucibleReviewItemInfo info) {
				return null;
			}

			public String getServerUrl() {
				return null;
			}
		};

		return new ReviewAdapter(review, null);
	}
}
