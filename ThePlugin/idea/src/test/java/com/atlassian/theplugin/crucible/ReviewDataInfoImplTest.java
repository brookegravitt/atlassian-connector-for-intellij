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

package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewBean;
import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.idea.crucible.ReviewData;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.easymock.EasyMock;

/**
 * RemoteReview Tester.
 */
public class ReviewDataInfoImplTest extends TestCase {
    public ReviewDataInfoImplTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSetGetReviewData() throws Exception {
        //TODO: Test goes here...
    }

    public void testSetGetReviewers() throws Exception {
        //TODO: Test goes here...
    }

    public void testGetServer() throws Exception {
        //TODO: Test goes here...
    }

	public void testNotEquals() throws Exception {
		Server server = EasyMock.createMock(Server.class);
		ReviewBean r1 = new ReviewBean();
        r1.setPermId(new PermId() {

            public String getId() {
                return new String("ID");
            }
        });
		ReviewBean r2 = new ReviewBean();
        r2.setPermId(new PermId() {

            public String getId() {
                return new String("ID-2");
            }
        });

		assertEquals(r1, r1);

		assertFalse(r1.equals(null));
		assertFalse(r1.equals(r2));
        assertFalse(r1.hashCode() == r2.hashCode());
    }
	
	public static Test suite() {
        return new TestSuite(ReviewDataInfoImplTest.class);
    }
}
