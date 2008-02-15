package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.crucible.api.soap.xfire.auth.RpcAuthServiceName;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.PermId;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.RpcReviewServiceName;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.State;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlType;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-05
 * Time: 15:10:39
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleSessionImpl implements CrucibleSession {
	private String crucibleAuthUrl;
	private String crucibleReviewUrl;


	private String authToken;
	private RpcAuthServiceName authService;
	private RpcReviewServiceName reviewService;

	private static final String SERVICE_AUTH_SUFFIX = "service/auth";
	private static final String SERVICE_REVIEW_SUFFIX = "service/reviewtmp";

	/**
	 * @param baseUrl url to the Crucible installation (without /service/auth suffix)
	 */
	public CrucibleSessionImpl(String baseUrl) {
		crucibleAuthUrl = baseUrl;

		if (baseUrl.endsWith("/")) {
			crucibleAuthUrl = baseUrl + SERVICE_AUTH_SUFFIX;
			crucibleReviewUrl = baseUrl + SERVICE_REVIEW_SUFFIX;
		} else {
			crucibleAuthUrl = baseUrl + "/" + SERVICE_AUTH_SUFFIX;
			crucibleReviewUrl = baseUrl + "/" + SERVICE_REVIEW_SUFFIX;
		}

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		Thread.currentThread().setContextClassLoader(factory.getClass().getClassLoader());
		factory.setServiceClass(RpcAuthServiceName.class);
		factory.setAddress(crucibleAuthUrl);
		authService = (RpcAuthServiceName) factory.create();

		JaxWsProxyFactoryBean reviewFactory = new JaxWsProxyFactoryBean();
		reviewFactory.setServiceClass(RpcReviewServiceName.class);
		reviewFactory.setAddress(crucibleReviewUrl);
		reviewService = (RpcReviewServiceName) reviewFactory.create();
	}

	public void login(String userName, String password) throws CrucibleLoginException {
		if (authToken != null) {
			throw new IllegalStateException("Calling login on already logged in session.");
		}
		try {
			authToken = authService.login(userName, password);
		} catch (SOAPFaultException e) {
			throw new CrucibleLoginException("Login failed", e);
		}

		if (authToken == null || getAuthToken().length() == 0) {
			authToken = null; // nullify when empty
			throw new CrucibleLoginException("Login failed");
		}
	}

	public void logout() {
		if (authToken != null) {
			authService.logout(authToken);
		}
		authToken = null;
	}

	public ReviewData createReview(ReviewData reviewData) throws CrucibleException {
		String token = getAuthToken();
		try {
			return reviewService.createReview(token, reviewData);
		} catch (RuntimeException e) {
			throw new CrucibleException("createReview", e);
		}
	}

	public ReviewData createReviewFromPatch(ReviewData reviewData, String patch) throws CrucibleException {
		String token = getAuthToken();
		try {
			return reviewService.createReviewFromPatch(token, reviewData, patch);
		} catch (RuntimeException e) {
			throw new CrucibleException("createReviewFromPatch", e);
		}

	}

	public List<ReviewData> getReviewsInStates(List<State> arg1) throws CrucibleException {
		String token = getAuthToken();
		try {
			return reviewService.getReviewsInStates(token, arg1);
		} catch (RuntimeException e) {
			throw new CrucibleException("getReviewInStates", e);
		}
	}

	public List<ReviewData> getAllReviews() throws CrucibleException {
		String token = getAuthToken();
		try {
			return reviewService.getAllReviews(token);
		} catch (RuntimeException e) {
			throw new CrucibleException("getAllReviews", e);
		}

	}

	public List<String> getReviewers(PermId arg1) throws CrucibleException {
		String token = getAuthToken();
		try {
			return reviewService.getReviewers(token, arg1);
		} catch (RuntimeException e) {
			throw new CrucibleException("getReviewers", e);
		}
	}

	private String getAuthToken() {
		if (authToken == null) {
			throw new IllegalStateException("Calling method without calling login() first");
		}
		return authToken;
	}

	/**
 * <p>Java class for removeReviewItem complex type.
	 *
	 * <p>The following schema fragment specifies the expected content contained within this class.
	 *
	 * <pre>
	 * &lt;complexType name="removeReviewItem">
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element name="token" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
	 *         &lt;element name="arg1" type="{http://rpc.spi.crucible.atlassian.com/}permId" minOccurs="0"/>
	 *         &lt;element name="arg2" type="{http://rpc.spi.crucible.atlassian.com/}permId" minOccurs="0"/>
	 *       &lt;/sequence>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 *
	 *
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "removeReviewItem", propOrder = {
		"token",
		"arg1",
		"arg2"
	})
	public static class RemoveReviewItem {

		protected String token;
		protected PermId arg1;
		protected PermId arg2;

		/**
		 * Gets the value of the token property.
		 *
		 * @return
		 *     possible object is
		 *     {@link String }
		 *
		 */
		public String getToken() {
			return token;
		}

		/**
		 * Sets the value of the token property.
		 *
		 * @param value
		 *     allowed object is
		 *     {@link String }
		 *
		 */
		public void setToken(String value) {
			this.token = value;
		}

		/**
		 * Gets the value of the arg1 property.
		 *
		 * @return
		 *     possible object is
		 *     {@link com.atlassian.theplugin.crucible.api.soap.xfire.review.PermId }
		 *
		 */
		public PermId getArg1() {
			return arg1;
		}

		/**
		 * Sets the value of the arg1 property.
		 *
		 * @param value
		 *     allowed object is
		 *     {@link com.atlassian.theplugin.crucible.api.soap.xfire.review.PermId }
		 *
		 */
		public void setArg1(PermId value) {
			this.arg1 = value;
		}

		/**
		 * Gets the value of the arg2 property.
		 *
		 * @return
		 *     possible object is
		 *     {@link com.atlassian.theplugin.crucible.api.soap.xfire.review.PermId }
		 *
		 */
		public PermId getArg2() {
			return arg2;
		}

		/**
		 * Sets the value of the arg2 property.
		 *
		 * @param value
		 *     allowed object is
		 *     {@link com.atlassian.theplugin.crucible.api.soap.xfire.review.PermId }
		 *
		 */
		public void setArg2(PermId value) {
			this.arg2 = value;
		}

	}
}
