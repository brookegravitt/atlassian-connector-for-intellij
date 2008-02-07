package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.crucible.api.soap.axis.AuthSoapBindingStub;
import com.atlassian.theplugin.crucible.api.soap.xfire.Auth;
import com.atlassian.theplugin.crucible.api.soap.xfire.RpcAuthServiceName;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;

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
	private RpcAuthServiceName port;

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

		QName SERVICE_NAME = new QName("http://rpc.spi.crucible.atlassian.com/", "Auth");

		Auth ss = null;
		try {
			ss = new Auth(new URL("http://lech.atlassian.pl:8060/service/auth?wsdl"), SERVICE_NAME);
		} catch (MalformedURLException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		port = ss.getAuthPort();

	}

//		public CrucibleSessionImpl(String baseUrl) throws CrucibleException {
//		crucibleAuthUrl = baseUrl;
//
//		if (baseUrl.endsWith("/")) {
//			crucibleAuthUrl = baseUrl + SERVICE_AUTH_SUFFIX;
//		} else {
//			crucibleAuthUrl = baseUrl + "/" + SERVICE_AUTH_SUFFIX;
//		}
//
//		try {
//			service = (AuthSoapBindingStub) new AuthLocator().getAuthPort(new URL(crucibleAuthUrl));
//			//service.setTimeout(3000);
//		} catch (ServiceException e) {
//			throw new CrucibleException("Soap binding problem", e);
//		} catch (MalformedURLException e) {
//			throw new CrucibleException("Invalid URL", e);
//		}
//	}

	public void login(String userName, String password) throws CrucibleLoginException {
			authToken = port.login(userName, password);
			//authToken = service.login(userName, password);
	}

	public void logout() throws CrucibleLogoutException {
		if (authToken != null) {
				port.logout(authToken);
		}
		
		authToken = null;

	}
}
