package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.crucible.api.soap.xfire.auth.RpcAuthServiceName;

import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.RequestWrapper;
import javax.jws.WebResult;
import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-08
 * Time: 14:55:03
 * To change this template use File | Settings | File Templates.
 */
public class CxfAuthServiceMockImpl implements RpcAuthServiceName {

	private String token;
	public static final String INVALID_LOGIN = "invalidLogin";
	public static final String VALID_LOGIN = "validLogin";
	public static final String INVALID_PASSWORD = "invalidPassword";
	public static final String VALID_PASSWORD = "validPassword";
	public static final String VALID_URL = "http://localhost:9000";

	@ResponseWrapper(localName = "loginResponse", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.auth.LoginResponse")
	@RequestWrapper(localName = "login", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.auth.Login")
	@WebResult(name = "return", targetNamespace = "")
	@WebMethod
	public String login(@WebParam(name = "arg0", targetNamespace = "")
	String userName, @WebParam(name = "arg1", targetNamespace = "")
	String password) {

		if (userName.equals(INVALID_LOGIN) || password.equals(INVALID_PASSWORD)) {
			return null;
		} else if (userName.equals(VALID_LOGIN) && password.equals(VALID_PASSWORD)) {
			return "test token";
		} else {
			return null;
		}
	}

	@ResponseWrapper(localName = "logoutResponse", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.auth.LogoutResponse")
	@RequestWrapper(localName = "logout", targetNamespace = "http://rpc.spi.crucible.atlassian.com/", className = "com.atlassian.theplugin.crucible.api.soap.xfire.auth.Logout")
	@WebMethod
	public void logout(@WebParam(name = "arg0", targetNamespace = "")
	String arg0) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
