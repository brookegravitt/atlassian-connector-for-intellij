package com.atlassian.connector.intellij.stash.impl;

import com.atlassian.connector.intellij.stash.Comment;
import com.atlassian.connector.intellij.stash.StashSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

public class StashRestSession implements StashSession {

    String protocol = "http";
    String host = "localhost";//"192.168.157.159";
    Integer port = 7990;
    String baseUrl = protocol + "://" + host + ":" + port;
    private HttpClientContext context;
    private HttpClient client;

    private final Gson gson = new Gson();

    public StashRestSession()  {
        context = HttpClientContext.create();
        client = HttpClientBuilder.create().build();

        AuthCache authCache = new BasicAuthCache();

        HttpHost targetHost = new HttpHost(host, port, protocol);
        authCache.put(targetHost, new BasicScheme());
        context.setAuthCache(authCache);
    }

    public void login(String name, char[] aPassword) throws RemoteApiLoginException {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("blewandowski", "blewandowski"));

        context.setCredentialsProvider(credsProvider);
    }

    public void logout() {
        context.setCredentialsProvider(new BasicCredentialsProvider());
    }

    public boolean isLoggedIn() throws RemoteApiLoginException {
        return context.getCredentialsProvider().getCredentials(AuthScope.ANY) != null;
    }

    public String getPullRequests(String projectKey, String repo) throws IOException {
        String url = String.format("/rest/api/1.0/projects/%s/repos/%s/pull-requests", projectKey, repo);

        return executeGet(url);
    }

    public String getComments(String projectKey, String repo, String pullRequestId, String path) throws IOException {
        String url = String.format("/rest/api/1.0/projects/%s/repos/%s/pull-requests/%s/comments?path=%s", projectKey, repo, pullRequestId, path);

        return executeGet(url);
    }

    public void postComment(String projectKey, String repo, String pullRequestId, Comment comment) throws IOException {
        String url = String.format("/rest/api/1.0/projects/%s/repos/%s/pull-requests/%s/comments", projectKey, repo, pullRequestId);

        executePost(gson.toJson(comment), url);
    }

    public String getChangedFiles(String projectKey, String repo, String pullRequestId) throws IOException{
        String url = String.format("/rest/api/1.0/projects/%s/repos/%s/pull-requests/%s/changes", projectKey, repo, pullRequestId);

        return executeGet(url);
    }

    private String executeGet(String url) throws IOException {
        CloseableHttpResponse response = null;
        try {
            response = (CloseableHttpResponse) client.execute(new HttpGet(baseUrl + url), context);
            int statusCode = response.getStatusLine().getStatusCode();
            return IOUtils.toString(response.getEntity().getContent());
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private void executePost(String entityString, String url) throws IOException {
        HttpPost post = new HttpPost(baseUrl + url);

        StringEntity entity = new StringEntity(entityString);
        post.setEntity(entity);
        post.setHeader("Content-type", "application/json");
        CloseableHttpResponse response = null;

        try {
            response = (CloseableHttpResponse) client.execute(post, context);
            int statusCode = response.getStatusLine().getStatusCode();
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
