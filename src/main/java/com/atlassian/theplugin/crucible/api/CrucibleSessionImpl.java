package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.crucible.api.soap.xfire.RpcAuthServiceName;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-05
 * Time: 15:10:39
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleSessionImpl implements CrucibleSession {
	private String crucibleAuthUrl;


	private String authToken;
    RpcAuthServiceName authService;

    private static final String SERVICE_AUTH_SUFFIX = "service/auth";

	/**
	 *
	 * @param baseUrl url to the Crucible installation (without /service/auth suffix)
	 * @throws CrucibleException if URL is invalid or SOAP binding failed
	 */
	public CrucibleSessionImpl(String baseUrl) {
		crucibleAuthUrl = baseUrl;

		if (baseUrl.endsWith("/")) {
			crucibleAuthUrl = baseUrl + SERVICE_AUTH_SUFFIX;
		} else {
			crucibleAuthUrl = baseUrl + "/" + SERVICE_AUTH_SUFFIX;
		}

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(RpcAuthServiceName.class);
        factory.setAddress(crucibleAuthUrl);
        authService = (RpcAuthServiceName) factory.create();
	}

	public void login(String userName, String password) throws CrucibleLoginException {

        authToken = authService.login(userName, password);

	    if (authToken == null || authToken.length() == 0) {
			throw new CrucibleLoginException("Login failed");
		}
	}

	public void logout() {
		if (authToken != null) {
				authService.logout(authToken);
		}
		authToken = null;
	}
}
