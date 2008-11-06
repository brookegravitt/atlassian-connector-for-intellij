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

/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 13/03/2004
 * Time: 23:19:19
 */
package com.atlassian.theplugin.jira.api;

import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import static com.atlassian.theplugin.commons.util.UrlUtil.encodeUrl;
import com.atlassian.theplugin.jira.JIRAServer;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.httpclient.HttpMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class JIRARssClient extends AbstractHttpSession {
    private static final Logger LOGGER = Logger.getInstance(JIRARssClient.class.getName());

    public JIRARssClient(String url) throws RemoteApiMalformedUrlException {
		super(url);
    }

	protected void adjustHttpHeader(HttpMethod method) {
	}

	protected void preprocessResult(Document doc) throws JDOMException, RemoteApiSessionExpiredException {
	}

	public JIRARssClient(String url, String userName, String password) throws RemoteApiMalformedUrlException {
		super(url);
        this.userName = userName;
        this.password = password;
    }

    public List<JIRAIssue> getIssues(List<JIRAQueryFragment> fragments,
						  String sortBy,
						  String sortOrder, int start, int max) throws JIRAException {

        StringBuffer url = new StringBuffer(baseUrl + "/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?");


	    List<JIRAQueryFragment> fragmentsWithoutAnys = new ArrayList<JIRAQueryFragment>();
	    for (JIRAQueryFragment jiraQueryFragment : fragments) {
		    if (jiraQueryFragment.getId() != JIRAServer.ANY_ID) {
			    fragmentsWithoutAnys.add(jiraQueryFragment);
		    }
	    }

        for (JIRAQueryFragment fragment : fragmentsWithoutAnys) {
            if (fragment.getQueryStringFragment() != null) {
                url.append("&").append(fragment.getQueryStringFragment());
            }
        }

        url.append("&sorter/field=" + sortBy);
		url.append("&sorter/order=" + sortOrder);
		url.append("&pager/start=" + start);
		url.append("&tempMax=" + max);
        url.append(appendAuthentication());

		try {
            Document doc = retrieveGetResponse(url.toString());
            Element root = doc.getRootElement();
            Element channel = root.getChild("channel");
            if (channel != null && !channel.getChildren("item").isEmpty()) {
                return makeIssues(channel.getChildren("item"));
            }
            return Collections.emptyList();
        } catch (IOException e) {
            throw new JIRAException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new JIRAException(e.getMessage(), e);
        } catch (RemoteApiSessionExpiredException e) {
			throw new JIRAException(e.getMessage(), e);
		}

	}

    public List<JIRAIssue> getAssignedIssues(String assignee) throws JIRAException {
        String url = baseUrl + "/sr/jira.issueviews:searchrequest-xml"
                + "/temp/SearchRequest.xml?resolution=-1&assignee=" + encodeUrl(assignee)
                + "&sorter/field=updated&sorter/order=DESC&tempMax=100" + appendAuthentication();

        try {
            Document doc = retrieveGetResponse(url);
            Element root = doc.getRootElement();
            Element channel = root.getChild("channel");
            if (channel != null && !channel.getChildren("item").isEmpty()) {
                return makeIssues(channel.getChildren("item"));
            }
            

            return Collections.emptyList();
        } catch (IOException e) {
            throw new JIRAException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new JIRAException(e.getMessage(), e);
        } catch (RemoteApiSessionExpiredException e) {
            throw new JIRAException(e.getMessage(), e);
		}
	}

	public List<JIRAIssue> getSavedFilterIssues(JIRAQueryFragment fragment,
									 String sortBy,
									 String sortOrder,
									 int start, 
									 int max) throws JIRAException {

		StringBuffer url = new StringBuffer(baseUrl + "/sr/jira.issueviews:searchrequest-xml/");

		if (fragment.getQueryStringFragment() != null) {
			url.append(fragment.getQueryStringFragment())
					.append("/SearchRequest-")
					.append(fragment.getQueryStringFragment())
					.append(".xml");
		}

		url.append("?sorter/field=" + sortBy);
		url.append("&sorter/order=" + sortOrder);
		url.append("&pager/start=" + start);
		url.append("&tempMax=" + max);
			
		url.append(appendAuthentication());

		try {
			Document doc = retrieveGetResponse(url.toString());
			Element root = doc.getRootElement();
			Element channel = root.getChild("channel");
			if (channel != null && !channel.getChildren("item").isEmpty()) {
				return makeIssues(channel.getChildren("item"));
			}
			return Collections.emptyList();
		} catch (IOException e) {
			throw new JIRAException(e.getMessage(), e);
		} catch (JDOMException e) {
			throw new JIRAException(e.getMessage(), e);
		} catch (RemoteApiSessionExpiredException e) {
			throw new JIRAException(e.getMessage(), e);
		}

	}

	private List<JIRAIssue> makeIssues(List issueElements) {
        List<JIRAIssue> result = new ArrayList<JIRAIssue>(issueElements.size());
        for (Iterator iterator = issueElements.iterator(); iterator.hasNext();) {
            result.add(new JIRAIssueBean(baseUrl, (Element) iterator.next()));
        }
        return result;
    }

    private String appendAuthentication() {
        if (userName != null) {
            return "&os_username=" + encodeUrl(userName)
                    + "&os_password=" + encodeUrl(password);
        }
        return "";
    }
}