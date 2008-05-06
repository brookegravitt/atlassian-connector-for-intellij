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

import com.atlassian.theplugin.crucible.api.PermId;
import com.atlassian.theplugin.crucible.api.ReviewData;
import com.atlassian.theplugin.commons.Server;
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

    public void testGetReviewUrl() throws Exception {
		Server server = EasyMock.createMock(Server.class);

		server.getUrlString();
		EasyMock.expectLastCall()
				.andReturn("http://url")
				.andReturn("http://url/")
				.andReturn("http://url//")
				.andReturn("http://url/////");

		ReviewData rd = EasyMock.createMock(ReviewData.class);
		rd.getPermaId();
		EasyMock.expectLastCall().andReturn(new PermId() {
			public String getId() {
				return "ID";
			}
		}).anyTimes();

		EasyMock.replay(server);
		EasyMock.replay(rd);

		ReviewDataInfoImpl tested = new ReviewDataInfoImpl(rd, null, server);
		assertEquals("http://url/cru/ID", tested.getReviewUrl());
		assertEquals("http://url/cru/ID", tested.getReviewUrl());
		assertEquals("http://url/cru/ID", tested.getReviewUrl());
		assertEquals("http://url/cru/ID", tested.getReviewUrl());
	}

	public void testEqualsAndHashcode() throws Exception {
		Server server = EasyMock.createMock(Server.class);
		ReviewData rd1 = EasyMock.createMock(ReviewData.class);
		rd1.getPermaId();
		EasyMock.expectLastCall().andReturn(new PermId() {
			public String getId() {
				return new String("ID");
			}
		}).anyTimes();
		ReviewData rd2 = EasyMock.createMock(ReviewData.class);
		rd2.getPermaId();
		EasyMock.expectLastCall().andReturn(new PermId() {
			public String getId() {
				return "ID-2";
			}
		}).anyTimes();

		EasyMock.replay(server);
		EasyMock.replay(rd1);
		EasyMock.replay(rd2);

		ReviewDataInfoImpl r1 = new ReviewDataInfoImpl(rd1, null, server);
		ReviewDataInfoImpl r2 = new ReviewDataInfoImpl(rd2, null, server);

		assertEquals(r1, r1);

		assertFalse(r1.equals(null));
		assertFalse(r1.equals(r2));

		r2 = new ReviewDataInfoImpl(rd1, null, null);
		assertFalse(r1.equals(r2));

		r2 = new ReviewDataInfoImpl(rd1, null, server);
		assertEquals(r1, r2);
		assertEquals(r1.hashCode(), r2.hashCode());

		r1 = new ReviewDataInfoImpl(rd1, null, null);
		r2 = new ReviewDataInfoImpl(rd1, null, null);

		assertEquals(r1, r2);
		assertEquals(r1.hashCode(), r2.hashCode());

	}
	
	public static Test suite() {
        return new TestSuite(ReviewDataInfoImplTest.class);
    }
}
