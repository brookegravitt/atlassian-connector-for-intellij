package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.crucible.api.soap.AuthLocator;
import com.atlassian.theplugin.crucible.api.soap.AuthSoapBindingStub;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-05
 * Time: 15:10:39
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleSessionImpl implements CrucibleSession {
	private String crucibleAuthUrl;
	private AuthSoapBindingStub service;

	private String authToken;

	private static final String SERVICE_AUTH_SUFFIX = "service/auth";

	/**
	 *
	 * @param baseUrl url to the Crucible installation (without /service/auth suffix)
	 * @throws CrucibleException if URL is invalid or SOAP binding failed
	 */
	public CrucibleSessionImpl(String baseUrl) throws CrucibleException {
		crucibleAuthUrl = baseUrl;

		if (baseUrl.endsWith("/")) {
			crucibleAuthUrl = baseUrl + SERVICE_AUTH_SUFFIX;
		} else {
			crucibleAuthUrl = baseUrl + "/" + SERVICE_AUTH_SUFFIX;
		}

		try {
			service = (AuthSoapBindingStub) new AuthLocator().getAuthPort(new URL(crucibleAuthUrl));
			//service.setTimeout(3000);
		} catch (ServiceException e) {
			throw new CrucibleException("Soap binding problem", e);
		} catch (MalformedURLException e) {
			throw new CrucibleException("Invalid URL", e);
		}
	}

	public void login(String userName, String password) throws CrucibleLoginException {
		try {
			authToken = service.login(userName, password);
		} catch (RemoteException e) {
			throw new CrucibleLoginException(e);
		}
	}

	public void logout() throws CrucibleLogoutException {
		if (authToken != null) {
			try {
				service.logout(authToken);
			} catch(RemoteException	e){
				throw new CrucibleLogoutException("Logout problem", e);
			}

			authToken = null;
		}

	}
}
