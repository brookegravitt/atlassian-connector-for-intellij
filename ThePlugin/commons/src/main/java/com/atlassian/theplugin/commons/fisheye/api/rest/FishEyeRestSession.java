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

import com.atlassian.theplugin.commons.fisheye.api.FishEyeSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import org.apache.commons.httpclient.HttpMethod;
import org.jdom.Document;
import org.jdom.JDOMException;

public class FishEyeRestSession extends AbstractHttpSession implements FishEyeSession {
	/**
	 * Public constructor for AbstractHttpSession
	 *
	 * @param baseUrl base URL for server instance
	 * @throws com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException
	 *          for malformed url
	 */
	public FishEyeRestSession(String baseUrl) throws RemoteApiMalformedUrlException {
		super(baseUrl);
	}

	protected void adjustHttpHeader(final HttpMethod method) {
		// TODO wseliga implement it
		throw new UnsupportedOperationException("not yet implemented");
	}

	protected void preprocessResult(final Document doc) throws JDOMException, RemoteApiSessionExpiredException {
		// TODO wseliga implement it
		throw new UnsupportedOperationException("not yet implemented");
	}

	public void login(final String name, final char[] aPassword) throws RemoteApiLoginException {
		// TODO wseliga implement it
		throw new UnsupportedOperationException("not yet implemented");
	}

	public void logout() {
		// TODO wseliga implement it
		throw new UnsupportedOperationException("not yet implemented");
	}
}
