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
package com.atlassian.theplugin.commons.fisheye.api.rest;

import com.atlassian.theplugin.api.AbstractSessionTest;
import com.atlassian.theplugin.commons.fisheye.api.rest.mock.FishEyeLoginCallback;
import com.atlassian.theplugin.commons.fisheye.api.rest.mock.FishEyeLogoutCallback;
import com.atlassian.theplugin.commons.remoteapi.ProductSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import org.ddsteps.mock.httpserver.JettyMockServer;

/**
 * FishEyeRestSession Tester.
 *
 * @author wseliga
 */
public class FishEyeRestSessionTest extends AbstractSessionTest {

    @Override
	public void setUp() throws Exception {
        super.setUp();
    }

    @Override
	public void tearDown() throws Exception {
        super.tearDown();
    }

	protected String getLoginUrl() {
		return "/api/rest/login";
	}

	protected ProductSession getProductSession(final String url) throws RemoteApiMalformedUrlException {
		return new FishEyeRestSession(url);
	}

	protected JettyMockServer.Callback getLoginCallback(final boolean isFail) {
		return new FishEyeLoginCallback(USER_NAME, PASSWORD, isFail);
	}

	public void xtestAdjustHttpHeader() {
        //TODO: wseliga implement it
        fail("unimplemented");
    }

    public void xtestPreprocessResult() {
        //TODO: wseliga implement it
        fail("unimplemented");
    }


    public void xtestGetDocument() {
        //TODO: wseliga implement it
        fail("unimplemented");
    }

    public void xtestGetLastModified() {
        //TODO: wseliga implement it
        fail("unimplemented");
    }

    public void xtestGetEtag() {
        //TODO: wseliga implement it
        fail("unimplemented");
    }

    public void testPlaceholder() {
    }

	public void testSuccessLoginURLWithSlash() throws Exception {
		mockServer.expect(FishEyeRestSession.LOGIN_ACTION, new FishEyeLoginCallback(USER_NAME, PASSWORD));
		mockServer.expect(FishEyeRestSession.LOGOUT_ACTION, new FishEyeLogoutCallback(FishEyeLoginCallback.AUTH_TOKEN));

		FishEyeRestSession apiHandler = new FishEyeRestSession(mockBaseUrl + "/");
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		assertTrue(apiHandler.isLoggedIn());
		apiHandler.logout();
		assertFalse(apiHandler.isLoggedIn());

		mockServer.verify();
	}

	public void testNullParamsLogin() throws Exception {
		try {
			FishEyeRestSession apiHandler = new FishEyeRestSession(null);
			apiHandler.login(null, null);
			fail();
		} catch (RemoteApiException ex) {
		}
	}

	public void testWrongParamsLogin() throws Exception {
		try {
			FishEyeRestSession apiHandler = new FishEyeRestSession("");
			apiHandler.login("", "".toCharArray());
			fail();
		} catch (RemoteApiException ex) {
		}
	}
}
