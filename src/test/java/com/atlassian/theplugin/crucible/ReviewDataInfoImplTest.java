package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.crucible.api.PermId;
import com.atlassian.theplugin.crucible.api.ReviewData;
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

    public static Test suite() {
        return new TestSuite(ReviewDataInfoImplTest.class);
    }
}
