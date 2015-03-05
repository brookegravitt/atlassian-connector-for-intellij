package com.atlassian.connector.intellij.stash.impl;

import com.atlassian.connector.intellij.stash.Comment;
import com.atlassian.connector.intellij.stash.StashSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
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

        HttpResponse response = client.execute(new HttpGet(baseUrl + url), context);

        int statusCode = response.getStatusLine().getStatusCode();

        return IOUtils.toString(response.getEntity().getContent());
    }

    public String getComments(String projectKey, String repo, String pullRequestId, String path) throws IOException {
        String url = String.format("/rest/api/1.0/projects/%s/repos/%s/pull-requests/%s/comments?path=%s", projectKey, repo, pullRequestId, path);

        HttpResponse response = client.execute(new HttpGet(baseUrl + url), context);

        int statusCode = response.getStatusLine().getStatusCode();

        return IOUtils.toString(response.getEntity().getContent());
    }

    public void postComment(String projectKey, String repo, String pullRequestId, Comment comment) throws IOException {
        String url = String.format("/rest/api/1.0/projects/%s/repos/%s/pull-requests/%s/comments", projectKey, repo, pullRequestId);

        HttpPost post = new HttpPost(baseUrl + url);

        StringEntity entity = new StringEntity(gson.toJson(comment));
        post.setEntity(entity);
        post.setHeader("Content-type", "application/json");

        HttpResponse response = client.execute(post, context);

        int statusCode = response.getStatusLine().getStatusCode();


        //return IOUtils.toString(response.getEntity().getContent());
    }

    public String getChangedFiles(String projectKey, String repo, String pullRequestId) throws IOException{
        String url = String.format("/rest/api/1.0/projects/%s/repos/%s/pull-requests/%s/changes", projectKey, repo, pullRequestId);

        HttpResponse response = client.execute(new HttpGet(baseUrl + url), context);

        int statusCode = response.getStatusLine().getStatusCode();

        return IOUtils.toString(response.getEntity().getContent());
    }
}
