package com.atlassian.theplugin.bamboo;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * HtmlBambooStatusListener Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>01/30/2008</pre>
 */
public class HtmlBambooStatusListenerTest extends TestCase implements BambooStatusDisplay {

	public void setUp() throws Exception {
		super.setUp();
	}

	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testUpdateBuildStatuses() throws Exception {

	}


	public static Test suite() {
		return new TestSuite(HtmlBambooStatusListenerTest.class);
	}

	public void updateBambooStatus(BuildStatus generalBuildStatus, String htmlPage) {
		//TODO: implement method updateBambooStatus
		throw new UnsupportedOperationException("method updateBambooStatus not implemented");
	}
}
