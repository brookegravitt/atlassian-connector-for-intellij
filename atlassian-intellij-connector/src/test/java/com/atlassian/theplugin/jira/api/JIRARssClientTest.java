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

package com.atlassian.theplugin.jira.api;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.jira.beans.JIRAProjectBean;
import com.atlassian.connector.commons.jira.beans.JIRAQueryFragment;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.connector.commons.jira.rss.JIRARssClient;
import com.atlassian.connector.intellij.remoteapi.IntelliJHttpSessionCallbackImpl;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.Server;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.exception.HttpProxySettingsException;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.jira.model.JiraCustomFilter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import junit.framework.TestCase;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JIRARssClientTest extends TestCase {
	private String mostRecentUrl;

	public void testAssignedIssues() throws Exception {
		JIRARssClient rss = getClasspathJIRARssClient("http://www.server.com", null, null, "/jira/assignedIssues.xml");
/*
        // first try unauthenticated and test the URL is correct
        rss.getAssignedIssues("anyone");
        assertEquals("http://www.server.com/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?resolution=-1&assignee=anyone&sorter/field=updated&sorter/order=DESC&tempMax=100", mostRecentUrl);

        // now try authenticated
        rss = getClasspathJIRARssClient("http://www.server.com", "user", "pass", "/jira/api/assignedIssues.xml");
        List list = rss.getAssignedIssues("anyone");
        assertEquals("http://www.server.com/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?resolution=-1&assignee=anyone&sorter/field=updated&sorter/order=DESC&tempMax=100&os_username=user&os_password=pass", mostRecentUrl);
        assertEquals(7, list.size());

        JIRAIssueBean firstIssue = new JIRAIssueBean();
        firstIssue.setServerUrl("http://www.server.com");
        firstIssue.setKey("PL-94");
        firstIssue.setSummary("NullPointerException on wrong URL to Bamboo server");
        assertEquals(firstIssue, list.get(0));
*/
	}

    public void testAuthenticationException_PL_1827() throws RemoteApiMalformedUrlException, JIRAException {
        Server srv = new Server() {
            ServerIdImpl serverId = new ServerIdImpl();
            public ServerIdImpl getServerId() {
                return serverId;
            }

            public String getName() {
                return "name";
            }

            public String getUrl() {
                return "http://atlassian.com";
            }

            public boolean isEnabled() {
                return true;
            }

            public boolean isUseDefaultCredentials() {
                return false;
            }

            public String getUsername() {
                return "userName";
            }

            public String getPassword() {
                return "password";
            }

            public ServerType getServerType() {
                return ServerType.JIRA_SERVER;
            }

            public boolean isDontUseBasicAuth() {
                return false;
            }

            public UserCfg getBasicHttpUser() {
                return null;
            }

            public boolean isShared() {
                return false;
            }

            public void setShared(boolean global) {
            }
        };

        final JiraServerData.Builder builder = new JiraServerData.Builder(srv);
        builder.useBasicUser(false);
        builder.defaultUser(new UserCfg("userName", "password"));

        JIRARssClientPublic mockRssClient = new JIRARssClientPublic(builder.build(), new HttpSessionCallback() {
            public HttpClient getHttpClient(ConnectionCfg server) throws HttpProxySettingsException {
                return new HttpClient();
            }
            public void configureHttpMethod(AbstractHttpSession session, HttpMethod method) {
            }

            public void disposeClient(ConnectionCfg server) {
            }

            public Cookie[] getCookiesHeaders(ConnectionCfg server) {
                return new Cookie[0]; 
            }
        });

//        List<JIRAQueryFragment> list = new ArrayList<JIRAQueryFragment>();
        JiraCustomFilter f = new JiraCustomFilter();
        try {
            mockRssClient.getIssues(f, "", "", 1, 100);
            fail();
        } catch (JIRAException e) {
            assertTrue(e.getMessage().startsWith("Connection error"));
        }


    }

		// for testing PL-2477
	public void testBugPl2477() throws Exception {
        JiraServerCfg serverCfg = new JiraServerCfg(true, "jira", "file://test", new ServerIdImpl(), true) {
			public ServerType getServerType() {
				return null;
			}

			public JiraServerCfg getClone() {
				return null;
			}
		};

		final JiraServerData server = new JiraServerData(serverCfg);

		JIRARssClient c = new JIRARssClient(server, new IntelliJHttpSessionCallbackImpl()) {
			@Override
			protected Document retrieveGetResponse(String urlString)
					throws IOException, JDOMException, RemoteApiSessionExpiredException {
				SAXBuilder builder = new SAXBuilder();
				InputStream is = JIRARssClientTest.class.getResourceAsStream("/jira/PL-2477.xml");
				Document doc = builder.build(is);
				preprocessResult(doc);
				return doc;
			}
		};
		List<JIRAQueryFragment> l = new ArrayList<JIRAQueryFragment>();
		l.add(new JIRAProjectBean());

		try {
            com.atlassian.connector.commons.jira.beans.JIRASavedFilter f = new com.atlassian.connector.commons.jira.beans.JIRASavedFilterBean(
                new JIRAQueryFragment() {
                    public String getQueryStringFragment() {
                        return "1001";
                    }

                    public long getId() {
                        return 0;
                    }

                    public String getName() {
                        return "name";  //To change body of implemented methods use File | Settings | File Templates.
                    }

                    public HashMap<String, String> getMap() {
                        return Maps.newHashMap(ImmutableMap.of("name", "x", "id", "1"));
                    }

                    public JIRAQueryFragment getClone() {
                        return null;
                    }
                }.getMap()
            );
			c.getSavedFilterIssues(f, "sortBy", "DESC", 0, 0);
		} catch (JIRAException e) {
			// I think it should stay here like this, as this is really unsolved on server side!
			System.out.println("PL-2477 not fixed: " + e.getMessage());
		}
	}
	// for testing PL-863
	public void testBugPl863() throws Exception {
        JiraServerCfg serverCfg = new JiraServerCfg(true, "jira", "file://test", new ServerIdImpl(), true) {
			public ServerType getServerType() {
				return null;
			}

			public JiraServerCfg getClone() {
				return null;
			}
		};

		final JiraServerData server = new JiraServerData(serverCfg);

		JIRARssClient c = new JIRARssClient(server, new IntelliJHttpSessionCallbackImpl()) {
			@Override
			protected Document retrieveGetResponse(String urlString)
					throws IOException, JDOMException, RemoteApiSessionExpiredException {
				SAXBuilder builder = new SAXBuilder();
				InputStream is = JIRARssClientTest.class.getResourceAsStream("/jira/PL-863.xml");
				Document doc = builder.build(is);
				preprocessResult(doc);
				return doc;
			}
		};
//		List<JIRAQueryFragment> l = new ArrayList<JIRAQueryFragment>();
//		l.add(new JIRAProjectBean());
        JiraCustomFilter f= new JiraCustomFilter();
        f.setQueryFragments(ImmutableList.of((JIRAQueryFragment) new JIRAProjectBean()));
		try {
			c.getIssues(f, "ASC", "prio", 0, 1);
		} catch (JIRAException e) {
			// I think it should stay here like this, as this is really unsolved on client side!
			System.out.println("PL-863 not fixed: " + e.getMessage());
		}
	}

	public void testBugPl941() throws Exception {
		final JiraServerData server =
                new JiraServerData(new JiraServerCfg(true, "jira", "file://test", new ServerIdImpl(), true) {
			public ServerType getServerType() {
				return null;
			}

			public JiraServerCfg getClone() {
				return null;
			}
		});

		JIRARssClient c = new JIRARssClient(server, new IntelliJHttpSessionCallbackImpl()) {
			@Override
			protected Document retrieveGetResponse(String urlString)
					throws IOException, JDOMException, RemoteApiSessionExpiredException {
				SAXBuilder builder = new SAXBuilder();
				InputStream is = JIRARssClientTest.class.getResourceAsStream("/jira/PL-941.xml");
				Document doc = builder.build(is);
				preprocessResult(doc);
				return doc;
			}
		};

		try {

			//if something wron with xml structure getIssue throws an exception so code has to be aware of that
			c.getIssue("PL-941");
			fail("PL-941 not fixed");

		} catch (JIRAException e) {
			assertTrue(e.getMessage().startsWith("Cannot parse response from JIRA:"));
		}

	}

	// make a simple mock rss client that overrides URL loading with loading from a file
	private JIRARssClient getClasspathJIRARssClient(String url, String userName, String password, final String file)
			throws RemoteApiMalformedUrlException {
        final JiraServerCfg jiraCfg = new JiraServerCfg(true, "jira", url, new ServerIdImpl(), true) {
			public ServerType getServerType() {
				return null;
			}

			public JiraServerCfg getClone() {
				return null;
			}
		};

		final JiraServerData.Builder builder = new JiraServerData.Builder(jiraCfg);

        builder.useDefaultUser(false);
        builder.defaultUser(new UserCfg(userName, password));
		return new JIRARssClient(builder.build(), new IntelliJHttpSessionCallbackImpl()) {
			// protected so that we can easily write tests by simply returning XML from a file instead of a URL!
			protected InputStream getUrlAsStream(String url) throws IOException {
				mostRecentUrl = url;
				return JIRARssClientTest.class.getResourceAsStream(file);
			}
		};
	}


    private class JIRARssClientPublic extends JIRARssClient {

        public JIRARssClientPublic(final JiraServerData server, final HttpSessionCallback callback) throws RemoteApiMalformedUrlException {
            super(server, callback);
}

        @Override
        public Document retrieveGetResponse(String urlString) throws IOException, JDOMException, RemoteApiSessionExpiredException {
            throw new AuthenticationException("ntlm authorization challenge expected, but not found: ntlm " +
                            "authorization challenge expected, but not found");
        }
    }


}
