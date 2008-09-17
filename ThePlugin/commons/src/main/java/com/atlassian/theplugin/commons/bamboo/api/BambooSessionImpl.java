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

package com.atlassian.theplugin.commons.bamboo.api;

import com.atlassian.theplugin.commons.BambooFileInfo;
import com.atlassian.theplugin.commons.BambooFileInfoImpl;
import com.atlassian.theplugin.commons.bamboo.*;
import com.atlassian.theplugin.commons.remoteapi.*;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import org.apache.commons.httpclient.HttpMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
@todo get logs based on url: BASE_URL/download/TP-TEST/build_logs/TP-TEST-341.log
 */

/**
 * Communication stub for Bamboo REST API.
 */
public class BambooSessionImpl extends AbstractHttpSession implements BambooSession {
    private static final String LOGIN_ACTION = "/api/rest/login.action";
    private static final String LOGOUT_ACTION = "/api/rest/logout.action";
    private static final String LIST_PROJECT_ACTION = "/api/rest/listProjectNames.action";
    private static final String LIST_PLAN_ACTION = "/api/rest/listBuildNames.action";
    private static final String LATEST_BUILD_FOR_PLAN_ACTION = "/api/rest/getLatestBuildResults.action";
    private static final String LATEST_USER_BUILDS_ACTION = "/api/rest/getLatestUserBuilds.action";
    private static final String GET_BUILD_DETAILS_ACTION = "/api/rest/getBuildResultsDetails.action";
    private static final String ADD_LABEL_ACTION = "/api/rest/addLabelToBuildResults.action";
    private static final String ADD_COMMENT_ACTION = "/api/rest/addCommentToBuildResults.action";
    private static final String EXECUTE_BUILD_ACTION = "/api/rest/executeBuild.action";
    private static final String GET_BAMBOO_BUILD_NUMBER_ACTION = "/api/rest/getBambooBuildNumber.action";

    private String authToken;

    private static final String AUTHENTICATION_ERROR_MESSAGE = "User not authenticated yet, or session timed out";

    /**
     * Public constructor for BambooSessionImpl.
     *
     * @param baseUrl base URL for Bamboo instance
     * @throws com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException
     *
     */
    public BambooSessionImpl(String baseUrl) throws RemoteApiMalformedUrlException {
        super(baseUrl);
    }


    /**
     * Connects to Bamboo server instance. On successful login authentication token is returned from
     * server and stored in Bamboo session for subsequent calls.
     * <p/>
     * The exception returned may have the getCause() examined for to get the actual exception reason.<br>
     * If the exception is caused by a valid error response from the server (no IOEXception, UnknownHostException,
     * MalformedURLException or JDOMException), the
     * {@link com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginFailedException}
     * is actually thrown. This may be used as a hint that the password is invalid.
     *
     * @param name      username defined on Bamboo server instance
     * @param aPassword for username
     * @throws com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException
     *          on connection or authentication errors
     */
    public void login(String name, char[] aPassword) throws RemoteApiLoginException {
        String loginUrl;
        try {
            if (name == null || aPassword == null) {
                throw new RemoteApiLoginException("Corrupted configuration. Username or Password null");
            }
            String pass = String.valueOf(aPassword);
            loginUrl = baseUrl + LOGIN_ACTION + "?username=" + URLEncoder.encode(name, "UTF-8") + "&password="
                    + URLEncoder.encode(pass, "UTF-8") + "&os_username="
                    + URLEncoder.encode(name, "UTF-8") + "&os_password=" + URLEncoder.encode(pass, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoding problem: " + e.getMessage());
        }

        try {
            Document doc = retrieveGetResponse(loginUrl);
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                throw new RemoteApiLoginFailedException(exception);
            }

            @SuppressWarnings("unchecked")
            final List<Element> elements = XPath.newInstance("/response/auth").selectNodes(doc);
            if (elements == null || elements.size() == 0) {
                throw new RemoteApiLoginException("Server did not return any authentication token");
            }
            if (elements.size() != 1) {
                throw new RemoteApiLoginException("Server returned unexpected number of authentication tokens (" + elements.size() + ")");
            }
            this.authToken = elements.get(0).getText();
        } catch (MalformedURLException e) {
            throw new RemoteApiLoginException("Malformed server URL: " + baseUrl, e);
        } catch (UnknownHostException e) {
            throw new RemoteApiLoginException("Unknown host: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RemoteApiLoginException(e.getMessage(), e);
        } catch (JDOMException e) {
            throw new RemoteApiLoginException("Server returned malformed response", e);
        } catch (RemoteApiSessionExpiredException e) {
            throw new RemoteApiLoginException("Session expired", e);
        } catch (IllegalArgumentException e) {
            throw new RemoteApiLoginException("Malformed server URL: " + baseUrl, e);
        }

    }

    public void logout() {
        if (!isLoggedIn()) {
            return;
        }

        try {
            String logoutUrl = baseUrl + LOGOUT_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8");
            retrieveGetResponse(logoutUrl);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoding problem", e);
        } catch (IOException e) {
            /* ignore errors on logout */
        } catch (JDOMException e) {
            /* ignore errors on logout */
        } catch (RemoteApiSessionExpiredException e) {
            /* ignore errors on logout */
        }


        authToken = null;
        client = null;
    }

    public int getBamboBuildNumber() throws RemoteApiException {
        String queryUrl;
        try {
            queryUrl = baseUrl + GET_BAMBOO_BUILD_NUMBER_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoding problem: ", e);
        }

        try {
            Document doc = retrieveGetResponse(queryUrl);

            String exception = getExceptionMessages(doc);
            if (null != exception) {
                // error - method does nt exists (session errors handled in retrieveGetReponse
                return -1;
            }

            XPath xpath = XPath.newInstance("/response/bambooBuildNumber");
            @SuppressWarnings("unchecked")
            Element element = (Element) xpath.selectSingleNode(doc);
            if (element != null) {
                String bNo = element.getText();
                return Integer.parseInt(bNo);
            }
            return -1;
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    public List<BambooProject> listProjectNames() throws RemoteApiException {
        String buildResultUrl;
        try {
            buildResultUrl = baseUrl + LIST_PROJECT_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoding problem: ", e);
        }

        List<BambooProject> projects = new ArrayList<BambooProject>();
        try {
            Document doc = retrieveGetResponse(buildResultUrl);
            XPath xpath = XPath.newInstance("/response/project");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            if (elements != null) {
                for (Element element : elements) {
                    String name = element.getChild("name").getText();
                    String key = element.getChild("key").getText();
                    projects.add(new BambooProjectInfo(name, key));
                }
            }
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }

        return projects;
    }

    public List<BambooPlan> listPlanNames() throws RemoteApiException {
        String buildResultUrl;
        try {
            buildResultUrl = baseUrl + LIST_PLAN_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoding problem: ", e);
        }

        List<BambooPlan> plans = new ArrayList<BambooPlan>();
        try {
            Document doc = retrieveGetResponse(buildResultUrl);
            XPath xpath = XPath.newInstance("/response/build");
            @SuppressWarnings("unchecked")
            List<Element> elements = xpath.selectNodes(doc);
            if (elements != null) {
                for (Element element : elements) {
                    String enabledValue = element.getAttributeValue("enabled");
                    boolean enabled = true;
                    if (enabledValue != null) {
                        enabled = Boolean.parseBoolean(enabledValue);
                    }
                    String name = element.getChild("name").getText();
                    String key = element.getChild("key").getText();
                    BambooPlanData plan = new BambooPlanData(name, key);
                    plan.setEnabled(enabled);
                    plans.add(plan);
                }
            }
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }

        return plans;
    }

    /**
     * Returns a {@link com.atlassian.theplugin.commons.bamboo.BambooBuild} information about the latest build in a plan.
     * <p/>
     * Returned structure contains either the information about the build or an error message if the connection fails.
     *
     * @param planKey ID of the plan to get info about
     * @return Information about the last build or error message
     */
    public BambooBuild getLatestBuildForPlan(String planKey) throws RemoteApiSessionExpiredException {
        String buildResultUrl;
        try {
            buildResultUrl = baseUrl + LATEST_BUILD_FOR_PLAN_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8")
                    + "&buildKey=" + URLEncoder.encode(planKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoding problem: " + e.getMessage());
        }

        try {
            Document doc = retrieveGetResponse(buildResultUrl);
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                return constructBuildErrorInfo(planKey, exception, new Date());
            }

            @SuppressWarnings("unchecked")
            final List elements = XPath.newInstance("/response").selectNodes(doc);
            if (elements != null && !elements.isEmpty()) {
                Element e = (Element) elements.iterator().next();
                return constructBuildItem(e, new Date());
            } else {
                return constructBuildErrorInfo(planKey, "Malformed server reply: no response element", new Date());
            }
        } catch (IOException e) {
            return constructBuildErrorInfo(planKey, e.getMessage(), new Date());
        } catch (JDOMException e) {
            return constructBuildErrorInfo(planKey, "Server returned malformed response", new Date());
        } catch (RemoteApiException e) {
            return constructBuildErrorInfo(planKey, e.getMessage(), new Date());
        }
    }

    public List<String> getFavouriteUserPlans() throws RemoteApiSessionExpiredException {
        List<String> builds = new ArrayList<String>();
        String buildResultUrl;
        try {
            buildResultUrl = baseUrl + LATEST_USER_BUILDS_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoding problem: " + e.getMessage());
        }

        try {
            Document doc = retrieveGetResponse(buildResultUrl);
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                return builds;
            }

            final XPath xpath = XPath.newInstance("/response/build");
            @SuppressWarnings("unchecked")
            final List<Element> elements = xpath.selectNodes(doc);
            if (elements != null) {
                for (Element element : elements) {
                    builds.add(element.getChildText("key"));
                }
                return builds;
            } else {
                return builds;
            }
        } catch (IOException e) {
            return builds;
        } catch (JDOMException e) {
            return builds;
        }
    }

    public BuildDetails getBuildResultDetails(String buildKey, String buildNumber) throws RemoteApiException {
        String buildResultUrl;

        try {
            buildResultUrl = baseUrl + GET_BUILD_DETAILS_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8")
                    + "&buildKey=" + URLEncoder.encode(buildKey, "UTF-8")
                    + "&buildNumber=" + URLEncoder.encode(buildNumber, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoding problem: " + e.getMessage());
        }

        try {
            BuildDetailsInfo build = new BuildDetailsInfo();
            Document doc = retrieveGetResponse(buildResultUrl);
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                throw new RemoteApiException(exception);
            }

            @SuppressWarnings("unchecked")
            final List<Element> responseElements = XPath.newInstance("/response").selectNodes(doc);
            for (Element element : responseElements) {
                String vcsRevisionKey = element.getAttributeValue("vcsRevisionKey");
                if (vcsRevisionKey != null) {
                    build.setVcsRevisionKey(vcsRevisionKey);
                }
            }

            @SuppressWarnings("unchecked")
                final List<Element> commitElements = XPath.newInstance("/response/commits/commit").selectNodes(doc);
                if (!commitElements.isEmpty()) {
                    int i = 1;
                    for (Element element : commitElements) {
                        BambooChangeSetImpl cInfo = new BambooChangeSetImpl();
                        cInfo.setAuthor(element.getAttributeValue("author"));
                        cInfo.setCommitDate(parseCommitTime(element.getAttributeValue("date")));
                        cInfo.setComment(getChildText(element, "comment"));

                        String path = "/response/commits/commit[" + i++ + "]/files/file";
                        XPath filesPath = XPath.newInstance(path);
                        @SuppressWarnings("unchecked")
                        final List<Element> fileElements = filesPath.selectNodes(doc);
                        for (Element file : fileElements) {
                            BambooFileInfo fileInfo = new BambooFileInfoImpl(
                                    cInfo.getVirtualFileSystem(),
                                    file.getAttributeValue("name"),
                                    file.getAttributeValue("revision"));
                            cInfo.addCommitFile(fileInfo);
                        }
                        build.addCommitInfo(cInfo);
                    }
                }

            @SuppressWarnings("unchecked")
            final List<Element> sucTestResElements
                    = XPath.newInstance("/response/successfulTests/testResult").selectNodes(doc);
            for (Element element : sucTestResElements) {
                TestDetailsInfo tInfo = new TestDetailsInfo();
                tInfo.setTestClassName(element.getAttributeValue("testClass"));
                tInfo.setTestMethodName(element.getAttributeValue("testMethod"));
                double duration;
                try {
                    duration = Double.valueOf(element.getAttributeValue("duration"));
                } catch (NumberFormatException e) {
                    // leave 0
                    duration = 0;
                }
                tInfo.setTestDuration(duration);
                tInfo.setTestResult(TestResult.TEST_SUCCEED);
                build.addSuccessfulTest(tInfo);
            }

            @SuppressWarnings("unchecked")
            final List<Element> failedTestResElements = XPath.newInstance("/response/failedTests/testResult").selectNodes(doc);
            if (!failedTestResElements.isEmpty()) {
                int i = 1;
                for (Element element : failedTestResElements) {
                    TestDetailsInfo tInfo = new TestDetailsInfo();
                    tInfo.setTestClassName(element.getAttributeValue("testClass"));
                    tInfo.setTestMethodName(element.getAttributeValue("testMethod"));
                    double duration;
                    try {
                        duration = Double.valueOf(element.getAttributeValue("duration"));
                    } catch (NumberFormatException e) {
                        // leave 0
                        duration = 0;
                    }
                    tInfo.setTestDuration(duration);
                    tInfo.setTestResult(TestResult.TEST_FAILED);

                    String path = "/response/failedTests/testResult[" + i++ + "]/errors/error";
                    XPath errorPath = XPath.newInstance(path);
                    @SuppressWarnings("unchecked")
                    final List<Element> errorElements = errorPath.selectNodes(doc);
                    for (Element error : errorElements) {
                        tInfo.setTestErrors(error.getText());
                    }
                    build.addFailedTest(tInfo);
                }
            }

            return build;
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    public void addLabelToBuild(String buildKey, String buildNumber, String buildLabel) throws RemoteApiException {
        String buildResultUrl;
        try {
            buildResultUrl = baseUrl + ADD_LABEL_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8")
                    + "&buildKey=" + URLEncoder.encode(buildKey, "UTF-8")
                    + "&buildNumber=" + URLEncoder.encode(buildNumber, "UTF-8")
                    + "&label=" + URLEncoder.encode(buildLabel, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoding problem: ", e);
        }

        try {
            Document doc = retrieveGetResponse(buildResultUrl);
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                throw new RemoteApiException(exception);
            }
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    public void addCommentToBuild(String buildKey, String buildNumber, String buildComment) throws RemoteApiException {
        String buildResultUrl;
        try {
            buildResultUrl = baseUrl + ADD_COMMENT_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8")
                    + "&buildKey=" + URLEncoder.encode(buildKey, "UTF-8")
                    + "&buildNumber=" + URLEncoder.encode(buildNumber, "UTF-8")
                    + "&content=" + URLEncoder.encode(buildComment, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoding problem: ", e);
        }

        try {
            Document doc = retrieveGetResponse(buildResultUrl);
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                throw new RemoteApiException(exception);
            }
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    public void executeBuild(String buildKey) throws RemoteApiException {
        String buildResultUrl;
        try {
            buildResultUrl = baseUrl + EXECUTE_BUILD_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8")
                    + "&buildKey=" + URLEncoder.encode(buildKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoding problem: ", e);
        }

        try {
            Document doc = retrieveGetResponse(buildResultUrl);
            String exception = getExceptionMessages(doc);
            if (null != exception) {
                throw new RemoteApiException(exception);
            }
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    BambooBuild constructBuildErrorInfo(String planId, String message, Date lastPollingTime) {
        BambooBuildInfo buildInfo = new BambooBuildInfo();

        buildInfo.setServerUrl(baseUrl);
        buildInfo.setBuildKey(planId);
        buildInfo.setBuildState(BuildStatus.UNKNOWN.toString());
        buildInfo.setMessage(message);
        buildInfo.setPollingTime(lastPollingTime);

        return buildInfo;
    }

    private BambooBuildInfo constructBuildItem(Element buildItemNode, Date lastPollingTime) throws RemoteApiException {
        BambooBuildInfo buildInfo = new BambooBuildInfo();

        buildInfo.setServerUrl(baseUrl);

        buildInfo.setProjectName(getChildText(buildItemNode, "projectName"));
        buildInfo.setBuildName(getChildText(buildItemNode, "buildName"));
        buildInfo.setBuildKey(getChildText(buildItemNode, "buildKey"));
        buildInfo.setBuildState(getChildText(buildItemNode, "buildState"));
        buildInfo.setBuildNumber(getChildText(buildItemNode, "buildNumber"));
        buildInfo.setBuildReason(getChildText(buildItemNode, "buildReason"));
        buildInfo.setBuildDurationDescription(getChildText(buildItemNode, "buildDurationDescription"));
        buildInfo.setBuildTestSummary(getChildText(buildItemNode, "buildTestSummary"));
        buildInfo.setBuildCommitComment(getChildText(buildItemNode, "buildCommitComment"));
        buildInfo.setBuildRelativeBuildDate(getChildText(buildItemNode, "buildRelativeBuildDate"));
        try {
            buildInfo.setBuildTestsPassed(Integer.parseInt(getChildText(buildItemNode, "successfulTestCount")));
            buildInfo.setBuildTestsFailed(Integer.parseInt(getChildText(buildItemNode, "failedTestCount")));
        } catch (NumberFormatException ex) {
            throw new RemoteApiException("Invalid number", ex);
        }
        buildInfo.setBuildTime(parseBuildTime(getChildText(buildItemNode, "buildTime")));
        buildInfo.setPollingTime(lastPollingTime);

        return buildInfo;
    }

    private static DateTimeFormatter buildDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static DateTimeFormatter commitDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    private Date parseBuildTime(String date) {
        return buildDateFormat.parseDateTime(date).toDate();
    }

    private Date parseCommitTime(String date) {
        return commitDateFormat.parseDateTime(date).toDate();
    }

    private String getChildText(Element node, String childName) {
        try {
            return node.getChild(childName).getText();
        } catch (Exception e) {
            return "";
        }
    }

    private static String getExceptionMessages(Document doc) throws JDOMException {
        XPath xpath = XPath.newInstance("/errors/error");
        @SuppressWarnings("unchecked")
        List<Element> elements = xpath.selectNodes(doc);

        if (elements != null && elements.size() > 0) {
            StringBuffer exceptionMsg = new StringBuffer();
            for (Element e : elements) {
                exceptionMsg.append(e.getText());
                exceptionMsg.append("\n");
            }
            return exceptionMsg.toString();
        } else {
            /* no exception */
            return null;
        }
    }

    public boolean isLoggedIn() {
        return authToken != null;
    }

    public byte[] getBuildLogs(String buildKey, String buildNumber) throws RemoteApiException {
        String buildResultUrl;
        try {
            buildResultUrl = baseUrl + "/download/"
                    + URLEncoder.encode(buildKey, "UTF-8")
                    + "/build_logs/" + URLEncoder.encode(buildKey, "UTF-8")
                    + "-" + URLEncoder.encode(buildNumber, "UTF-8")
                    + ".log";
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoding problem: ", e);
        }

        try {
            return retrieveGetResponseAsBytes(buildResultUrl);
        } catch (JDOMException e) {
            throw new RemoteApiException("Server returned malformed response", e);
        } catch (IOException e) {
            throw new RemoteApiException(e.getMessage(), e);
        }
    }

    @Override
    protected void adjustHttpHeader(HttpMethod method) {
        // Bamboo does not require custom headers
    }

    @Override
    protected void preprocessResult(Document doc) throws JDOMException, RemoteApiSessionExpiredException {
        String error = getExceptionMessages(doc);
        if (error != null) {
            if (error.startsWith(AUTHENTICATION_ERROR_MESSAGE)) {
                throw new RemoteApiSessionExpiredException("Session expired.");
            }
        }
    }

}
