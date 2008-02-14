package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.PermId;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.easymock.EasyMock;

/**
 * RemoteReview Tester.
 */
public class RemoteReviewTest extends TestCase {
    public RemoteReviewTest(String name) {
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

		ReviewData rd = new ReviewData();
		PermId pid = new PermId();
		pid.setId("ID");
		rd.setPermaId(pid);

		EasyMock.replay(server);
		RemoteReview rr = new RemoteReview(rd, null, server);
		assertEquals("http://url/cru/ID", rr.getReviewUrl());
		assertEquals("http://url/cru/ID", rr.getReviewUrl());
		assertEquals("http://url/cru/ID", rr.getReviewUrl());
		assertEquals("http://url/cru/ID", rr.getReviewUrl());
	}

    public static Test suite() {
        return new TestSuite(RemoteReviewTest.class);
    }
}
