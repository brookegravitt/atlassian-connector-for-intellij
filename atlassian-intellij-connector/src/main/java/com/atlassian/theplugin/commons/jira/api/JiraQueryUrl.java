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

package com.atlassian.theplugin.commons.jira.api;

import com.atlassian.theplugin.commons.jira.cache.JIRAServerCache;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pmaruszak
 */

public final class JiraQueryUrl {
    private static final String ISSUE_NAVIGATOR =
            "/secure/IssueNavigator.jspa?refreshFilter=false&reset=update&show=View+%3E%3E";
    private static final String ISSUE_RSS = "/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?";
    private String serverUrl = null;
    private List<JIRAQueryFragment> queryFragments = null;
    private String sortBy = null;
    private String sortOrder = null;
    private int start = -1;
    private int max = -1;
    private String userName = null;


    public static class Builder {
        private List<JIRAQueryFragment> queryFragments = null;
        private String sortBy = null;
        private String sortOrder = null;
        private int start = -1;
        private int max = -1;
        private String userName = null;
        private String serverUrl;

        public Builder serverUrl(String server) {
            this.serverUrl = server;
            return this;
        }

        public Builder queryFragments(List<JIRAQueryFragment> fragmentList) {
            this.queryFragments = fragmentList;
            return this;
        }

        public Builder sortBy(String sort) {
            this.sortBy = sort;
            return this;
        }

        public Builder sortOrder(String sortOdr) {
            this.sortOrder = sortOdr;
            return this;
        }

        public Builder start(int strt) {
            this.start = strt;
            return this;
        }

        public Builder max(int mx) {
            this.max = mx;
            return this;
        }

        public Builder userName(String username) {
            this.userName = username;
            return this;
        }

        public JiraQueryUrl build() {
            return new JiraQueryUrl(this);
        }
    }

    public String buildRssSearchUrl() {
        return buildUrl(ISSUE_RSS);        
    }

    public String buildIssueNavigatorUrl() {
        return buildUrl(ISSUE_NAVIGATOR);
    }

    
    private String buildUrl(String method) {

        StringBuilder sb = new StringBuilder();

        List<JIRAQueryFragment> fragmentsWithoutAnys = new ArrayList<JIRAQueryFragment>();
        for (JIRAQueryFragment jiraQueryFragment : queryFragments) {
            if (jiraQueryFragment.getId() != JIRAServerCache.ANY_ID) {
                fragmentsWithoutAnys.add(jiraQueryFragment);
            }
        }

        for (JIRAQueryFragment fragment : fragmentsWithoutAnys) {
            if (fragment.getQueryStringFragment() != null) {
                sb.append("&").append(fragment.getQueryStringFragment());
            }
        }

        if (sortBy != null) {
            sb.append("&sorter/field=").append(sortBy);
        }
        if (sortOrder != null) {
            sb.append("&sorter/order=").append(sortOrder);
        }
        if (start >= 0) {
            sb.append("&pager/start=").append(start);
        }

        if (max >= 0) {
            sb.append("&tempMax=").append(max);
        }

        if (userName != null) {
            sb.append(appendAuthentication(false, userName));
        }

        if (serverUrl != null) {
               sb.insert(0, method);
               sb.insert(0, serverUrl);
        }
        
        return sb.toString();
    }


    private static String appendAuthentication(boolean firstItem, String userName) {
        if (userName != null) {
            return (firstItem ? "?" : "&") + "os_authType=basic";
        }
        return "";
    }

    private JiraQueryUrl(Builder builder) {
        this.queryFragments = builder.queryFragments;
        this.sortBy = builder.sortBy;
        this.sortOrder = builder.sortOrder;
        this.start = builder.start;
        this.max = builder.max;
        this.userName = builder.userName;
        this.serverUrl = builder.serverUrl;
    }
}
