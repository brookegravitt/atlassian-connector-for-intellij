package com.atlassian.theplugin.commons.fisheye.api.rest;

import com.atlassian.theplugin.commons.fisheye.FishEyeServerFacadeImpl;
import com.atlassian.theplugin.commons.fisheye.api.FishEyeSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;

/**
 * User: pmaruszak
 */
public class FishEyeServerFacadeImplUT extends FishEyeServerFacadeImpl {
	FishEyeSession mock;
	public FishEyeServerFacadeImplUT(final FishEyeSession fishEyeSessionMock) {
		this.mock = fishEyeSessionMock;

	}

	public FishEyeSession getSession(final String url) throws RemoteApiMalformedUrlException {
		return mock;
	}
}
