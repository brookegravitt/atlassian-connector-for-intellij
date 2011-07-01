package com.atlassian.connector.intellij.remoteapi;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.intellij.util.HttpClientFactory;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.exception.HttpProxySettingsException;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallbackImpl;
import com.atlassian.theplugin.idea.BugReporting;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Wojciech Seliga, Piotr Maruszak
 */
public class IntelliJHttpSessionCallbackImpl extends HttpSessionCallbackImpl {
	private static final String USER_AGENT = "Atlassian Connector for IntelliJ/" + BugReporting.getVersionString();
    private final Map<String, HttpClient> httpClients =
            Collections.synchronizedMap(new HashMap<String, HttpClient>());

	public HttpClient getHttpClient(ConnectionCfg server) throws HttpProxySettingsException {
        if (httpClients.get(server.getId()) == null) {
		    final HttpClient client = HttpClientFactory.getClient();
            client.getParams().setParameter(HttpMethodParams.USER_AGENT, USER_AGENT);
            client.getParams().setParameter(HttpMethodParams.BUFFER_WARN_TRIGGER_LIMIT, 1048576);
            httpClients.put(server.getId(), client);
        }

		return httpClients.get(server.getId());
	}

    @Override
    public void configureHttpMethod(AbstractHttpSession session, HttpMethod method) {
        super.configureHttpMethod(session, method);
    }


    public void disposeClient(ConnectionCfg server) {
       httpClients.remove(server.getId());
    }

    public void disposeClient(ServerId serverId) {
       String toRemove = null;
       for (String id : httpClients.keySet()) {
           if (id.equals(serverId.toString())) {

               toRemove = id;
               break;
           }
       }

       if (toRemove != null) {
               httpClients.remove(toRemove);
        }
    }

    public Cookie[] getCookiesHeaders(ConnectionCfg server) {
        Cookie[] cookies = new Cookie[0];

        if (httpClients.containsKey(server.getId())) {
            cookies = httpClients.get(server.getId()).getState().getCookies();
        }

        return cookies;
    }
}
