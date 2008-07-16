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

package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.commons.VirtualFileSystem;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.configuration.ServerBean;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.CrucibleSession;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.commons.crucible.api.model.GeneralComment;
import com.atlassian.theplugin.commons.crucible.api.model.PermId;
import com.atlassian.theplugin.commons.crucible.api.model.Project;
import com.atlassian.theplugin.commons.crucible.api.model.Repository;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewBean;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.Transition;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.crucible.api.model.UserBean;
import com.atlassian.theplugin.commons.crucible.api.model.VersionedComment;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.crucible.api.rest.cruciblemock.LoginCallback;
import junit.framework.TestCase;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import org.mortbay.jetty.Server;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CrucibleServerFacadeTest extends TestCase {
    private static final User VALID_LOGIN = new UserBean("validLogin");
    private static final String VALID_PASSWORD = "validPassword";
    private static final String VALID_URL = "http://localhost:9001";

    private CrucibleServerFacade facade;
    private CrucibleSession crucibleSessionMock;
    public static final String INVALID_PROJECT_KEY = "INVALID project key";

    @SuppressWarnings("unchecked")
    protected void setUp() {
        ConfigurationFactory.setConfiguration(new PluginConfigurationBean());

        crucibleSessionMock = createMock(CrucibleSession.class);

        facade = CrucibleServerFacadeImpl.getInstance();

        try {
            Field f = CrucibleServerFacadeImpl.class.getDeclaredField("sessions");
            f.setAccessible(true);

            ((Map<String, CrucibleSession>) f.get(facade)).put(VALID_URL + VALID_LOGIN.getUserName() + VALID_PASSWORD, crucibleSessionMock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void testConnectionTestFailed() throws Exception {

        Server server = new Server(0);
        server.start();

        String mockBaseUrl = "http://localhost:" + server.getConnectors()[0].getLocalPort();
        JettyMockServer mockServer = new JettyMockServer(server);
        mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(VALID_LOGIN.getUserName(), VALID_PASSWORD, LoginCallback.ALWAYS_FAIL));

        try {
            facade.testServerConnection(mockBaseUrl, VALID_LOGIN.getUserName(), VALID_PASSWORD);
            fail("testServerConnection failed");
        } catch (RemoteApiException e) {
            //
        }

        mockServer.verify();
        mockServer = null;
        server.stop();
    }

    public void testConnectionTestSucceed() throws Exception {
        Server server = new Server(0);
        server.start();

        String mockBaseUrl = "http://localhost:" + server.getConnectors()[0].getLocalPort();
        JettyMockServer mockServer = new JettyMockServer(server);
        mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(VALID_LOGIN.getUserName(), VALID_PASSWORD));

        try {
            facade.testServerConnection(mockBaseUrl, VALID_LOGIN.getUserName(), VALID_PASSWORD);
        } catch (RemoteApiException e) {
            fail("testServerConnection failed");
        }

        mockServer.verify();
        mockServer = null;
        server.stop();
    }

    public void testChangedCredentials() throws Exception {
        User validLogin2 = new UserBean(VALID_LOGIN.getUserName() + 2);
        String validPassword2 = VALID_PASSWORD + 2;
        try {
            Field f = CrucibleServerFacadeImpl.class.getDeclaredField("sessions");
            f.setAccessible(true);

            ((Map<String, CrucibleSession>) f.get(facade)).put(VALID_URL + validLogin2.getUserName() + validPassword2, crucibleSessionMock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        crucibleSessionMock.isLoggedIn();
        EasyMock.expectLastCall().andReturn(false);
        try {
            crucibleSessionMock.login(VALID_LOGIN.getUserName(), VALID_PASSWORD);
        } catch (RemoteApiLoginException e) {
            fail("recording mock failed for login");
        }

        PermId permId = new PermId() {
            public String getId() {
                return "permId";
            }
        };

        Review review = prepareReviewData(VALID_LOGIN, "name", State.DRAFT, permId);

        crucibleSessionMock.getAllReviews(true);
        EasyMock.expectLastCall().andReturn(Arrays.asList(review, review));

        crucibleSessionMock.isLoggedIn();
        EasyMock.expectLastCall().andReturn(false);
        try {
            crucibleSessionMock.login(validLogin2.getUserName(), validPassword2);
        } catch (RemoteApiLoginException e) {
            fail("recording mock failed for login");
        }

        Review review2 = prepareReviewData(validLogin2, "name", State.DRAFT, permId);
        crucibleSessionMock.getAllReviews(true);
        EasyMock.expectLastCall().andReturn(Arrays.asList(review2));

        replay(crucibleSessionMock);

        ServerBean server = prepareServerBean();
        List<Review> ret = facade.getAllReviews(server);
        assertEquals(2, ret.size());
        assertEquals(permId.getId(), ret.get(0).getPermId().getId());
        assertEquals("name", ret.get(0).getName());
        assertEquals(VALID_LOGIN, ret.get(0).getAuthor());
        assertEquals(VALID_LOGIN, ret.get(0).getCreator());
        assertEquals("Test description", ret.get(0).getDescription());
        assertEquals(VALID_LOGIN, ret.get(0).getModerator());
        assertEquals("TEST", ret.get(0).getProjectKey());
        assertEquals(null, ret.get(0).getRepoName());
        assertSame(State.DRAFT, ret.get(0).getState());
        assertNull(ret.get(0).getParentReview());

        server.setUserName(validLogin2.getUserName());
        server.transientSetPasswordString(validPassword2, false);
        ret = facade.getAllReviews(server);
        assertEquals(1, ret.size());
        assertEquals(permId.getId(), ret.get(0).getPermId().getId());
        assertEquals("name", ret.get(0).getName());
        assertEquals(validLogin2, ret.get(0).getAuthor());
        assertEquals(validLogin2, ret.get(0).getCreator());
        assertEquals("Test description", ret.get(0).getDescription());
        assertEquals(validLogin2, ret.get(0).getModerator());
        assertEquals("TEST", ret.get(0).getProjectKey());
        assertEquals(null, ret.get(0).getRepoName());
        assertSame(State.DRAFT, ret.get(0).getState());
        assertNull(ret.get(0).getParentReview());

        EasyMock.verify(crucibleSessionMock);
    }

    public void testCreateReview() throws Exception {
        crucibleSessionMock.isLoggedIn();
        EasyMock.expectLastCall().andReturn(false);
        try {
            crucibleSessionMock.login(VALID_LOGIN.getUserName(), VALID_PASSWORD);
        } catch (RemoteApiLoginException e) {
            fail("recording mock failed for login");
        }

        crucibleSessionMock.createReview(EasyMock.isA(Review.class));
        Review response = new ReviewBean();

        EasyMock.expectLastCall().andReturn(response);

        replay(crucibleSessionMock);

        ServerBean server = prepareServerBean();
        Review review = prepareReviewData("name", State.DRAFT);

        // test call
        Review ret = facade.createReview(server, review);
        assertSame(response, ret);

        EasyMock.verify(crucibleSessionMock);


    }

    public void testCreateReviewWithInvalidProjectKey() throws Exception {
        crucibleSessionMock.isLoggedIn();
        EasyMock.expectLastCall().andReturn(false);
        try {
            crucibleSessionMock.login(VALID_LOGIN.getUserName(), VALID_PASSWORD);
        } catch (RemoteApiLoginException e) {
            fail("recording mock failed for login");
        }

        crucibleSessionMock.createReview(EasyMock.isA(Review.class));

        EasyMock.expectLastCall().andThrow(new RemoteApiException("test"));

        replay(crucibleSessionMock);

        ServerBean server = prepareServerBean();
        Review review = prepareReviewData("name", State.DRAFT);

        try {
            // test call
            facade.createReview(server, review);
            fail("creating review with invalid key should throw an CrucibleException()");
        } catch (RemoteApiException e) {

        } finally {
            EasyMock.verify(crucibleSessionMock);
        }

    }

    public void testCreateReviewFromPatch() throws ServerPasswordNotProvidedException, RemoteApiException {
        crucibleSessionMock.isLoggedIn();
        EasyMock.expectLastCall().andReturn(false);
        try {
            crucibleSessionMock.login(VALID_LOGIN.getUserName(), VALID_PASSWORD);
        } catch (RemoteApiLoginException e) {
            fail("recording mock failed for login");
        }

        crucibleSessionMock.createReviewFromPatch(EasyMock.isA(Review.class), EasyMock.eq("some patch"));
        Review response = new ReviewBean();
        EasyMock.expectLastCall().andReturn(response);

        replay(crucibleSessionMock);

        ServerBean server = prepareServerBean();
        Review review = prepareReviewData("name", State.DRAFT);

        String patch = "some patch";

        // test call
        Review ret = facade.createReviewFromPatch(server, review, patch);
        assertSame(response, ret);

        EasyMock.verify(crucibleSessionMock);
    }

    public void testCreateReviewFromPatchWithInvalidProjectKey() throws Exception {
        crucibleSessionMock.isLoggedIn();
        EasyMock.expectLastCall().andReturn(false);
        try {
            crucibleSessionMock.login(VALID_LOGIN.getUserName(), VALID_PASSWORD);
        } catch (RemoteApiLoginException e) {
            fail("recording mock failed for login");
        }

        crucibleSessionMock.createReviewFromPatch(EasyMock.isA(Review.class), EasyMock.eq("some patch"));
        EasyMock.expectLastCall().andThrow(new RemoteApiException("test"));

        replay(crucibleSessionMock);

        ServerBean server = prepareServerBean();
        Review review = prepareReviewData("name", State.DRAFT);

        String patch = "some patch";

        try {
            facade.createReviewFromPatch(server, review, patch);
            fail("creating review with patch with invalid key should throw an RemoteApiException()");
        } catch (RemoteApiException e) {
            // ignored by design
        } finally {
            EasyMock.verify(crucibleSessionMock);
        }
    }

    public void testGetAllReviews() throws ServerPasswordNotProvidedException, RemoteApiException {
        crucibleSessionMock.isLoggedIn();
        EasyMock.expectLastCall().andReturn(false);
        try {
            crucibleSessionMock.login(VALID_LOGIN.getUserName(), VALID_PASSWORD);
        } catch (RemoteApiLoginException e) {
            fail("recording mock failed for login");
        }

        PermId permId = new PermId() {
            public String getId() {
                return "permId";
            }
        };

        Review review = prepareReviewData(VALID_LOGIN, "name", State.DRAFT, permId);

        crucibleSessionMock.getAllReviews(true);
        EasyMock.expectLastCall().andReturn(Arrays.asList(review, review));

        replay(crucibleSessionMock);

        ServerBean server = prepareServerBean();
        // test call
        List<Review> ret = facade.getAllReviews(server);
        assertEquals(2, ret.size());
        assertEquals(permId.getId(), ret.get(0).getPermId().getId());
        assertEquals("name", ret.get(0).getName());
        assertEquals(VALID_LOGIN, ret.get(0).getAuthor());
        assertEquals(VALID_LOGIN, ret.get(0).getCreator());
        assertEquals("Test description", ret.get(0).getDescription());
        assertEquals(VALID_LOGIN, ret.get(0).getModerator());
        assertEquals("TEST", ret.get(0).getProjectKey());
        assertEquals(null, ret.get(0).getRepoName());
        assertSame(State.DRAFT, ret.get(0).getState());
        assertNull(ret.get(0).getParentReview());

        EasyMock.verify(crucibleSessionMock);
    }

    public void testGetProjects() throws ServerPasswordNotProvidedException, RemoteApiException {
        crucibleSessionMock.isLoggedIn();
        EasyMock.expectLastCall().andReturn(false);
        try {
            crucibleSessionMock.login(VALID_LOGIN.getUserName(), VALID_PASSWORD);
        } catch (RemoteApiLoginException e) {
            fail("recording mock failed for login");
        }
        crucibleSessionMock.getProjects();
        EasyMock.expectLastCall().andReturn(Arrays.asList(prepareProjectData(0), prepareProjectData(1)));
        replay(crucibleSessionMock);

        ServerBean server = prepareServerBean();
        // test call
        List<Project> ret = facade.getProjects(server);
        assertEquals(2, ret.size());
        for (int i = 0; i < 2; i++) {
            String id = Integer.toString(i);
            assertEquals(id, ret.get(i).getId());
            assertEquals("CR" + id, ret.get(i).getKey());
            assertEquals("Name" + id, ret.get(i).getName());
        }
        EasyMock.verify(crucibleSessionMock);
    }

    public void testGetRepositories() throws ServerPasswordNotProvidedException, RemoteApiException {
        crucibleSessionMock.isLoggedIn();
        EasyMock.expectLastCall().andReturn(false);
        try {
            crucibleSessionMock.login(VALID_LOGIN.getUserName(), VALID_PASSWORD);
        } catch (RemoteApiLoginException e) {
            fail("recording mock failed for login");
        }
        crucibleSessionMock.getRepositories();
        EasyMock.expectLastCall().andReturn(Arrays.asList(prepareRepositoryData(0), prepareRepositoryData(1)));
        replay(crucibleSessionMock);

        ServerBean server = prepareServerBean();
        // test call
        List<Repository> ret = facade.getRepositories(server);
        assertEquals(2, ret.size());
        for (int i = 0; i < 2; i++) {
            String id = Integer.toString(i);
            assertEquals("RepoName" + id, ret.get(i).getName());
        }
        EasyMock.verify(crucibleSessionMock);
    }

    private Review prepareReviewData(final String name, final State state) {
        return new Review() {
            public User getAuthor() {
                return VALID_LOGIN;
            }

            public User getCreator() {
                return VALID_LOGIN;
            }

            public String getDescription() {
                return "Test description";
            }

            public User getModerator() {
                return VALID_LOGIN;
            }

            public String getName() {
                return name;
            }

            public PermId getParentReview() {
                return null;
            }

            public PermId getPermId() {
                return new PermId() {
                    public String getId() {
                        return "permId";
                    }
                };
            }

            public String getProjectKey() {
                return "TEST";
            }

            public String getRepoName() {
                return null;
            }

            public State getState() {
                return state;
            }

            public int getMetricsVersion() {
                return 0;
            }

            public Date getCreateDate() {
                return null;
            }

            public Date getCloseDate() {
                return null;
            }

            public List<Reviewer> getReviewers() throws ValueNotYetInitialized {
                return null;
            }

            public List<GeneralComment> getGeneralComments() throws ValueNotYetInitialized {
                return null;
            }

            public List<VersionedComment> getVersionedComments() throws ValueNotYetInitialized {
                return null;
            }

            public List<CrucibleFileInfo> getFiles() throws ValueNotYetInitialized {
                return null;
            }

            public List<Transition> getTransitions() throws ValueNotYetInitialized {
                return null;
            }

            public VirtualFileSystem getVirtualFileSystem() {
                return null;
            }
        };
    }

    private Review prepareReviewData(final User user, final String name, final State state, final PermId permId) {
        return new Review() {
            public User getAuthor() {
                return user;
            }

            public User getCreator() {
                return user;
            }

            public String getDescription() {
                return "Test description";
            }

            public User getModerator() {
                return user;
            }

            public String getName() {
                return name;
            }

            public PermId getParentReview() {
                return null;
            }

            public PermId getPermId() {
                return permId;
            }

            public String getProjectKey() {
                return "TEST";
            }

            public String getRepoName() {
                return null;
            }

            public State getState() {
                return state;
            }

            public int getMetricsVersion() {
                return 0;
            }

            public Date getCreateDate() {
                return null;
            }

            public Date getCloseDate() {
                return null;
            }

            public String getReviewUrl() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public List<Reviewer> getReviewers() {
                return null;
            }

            public com.atlassian.theplugin.commons.Server getServer() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public List<GeneralComment> getGeneralComments() {
                return null;
            }

            public List<VersionedComment> getVersionedComments() throws ValueNotYetInitialized {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public List<CrucibleFileInfo> getFiles() throws ValueNotYetInitialized {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public List<Transition> getTransitions() {
                return null;
            }

            public VirtualFileSystem getVirtualFileSystem() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }

    private ServerBean prepareServerBean() {
        ServerBean server = new ServerBean();
        server.setUrlString(VALID_URL);
        server.setUserName(VALID_LOGIN.getUserName());
        server.transientSetPasswordString(VALID_PASSWORD, false);
        return server;
    }

    private Project prepareProjectData(final int i) {
        return new Project() {
            public String getId() {
                return Integer.toString(i);
            }

            public String getKey() {
                return "CR" + Integer.toString(i);
            }

            public String getName() {
                return "Name" + Integer.toString(i);
            }
        };
    }

    private Repository prepareRepositoryData(final int i) {
        return new Repository() {
            public String getName() {
                return "RepoName" + Integer.toString(i);
            }

            public String getType() {
                return "svn";
            }

            public boolean isEnabled() {
                return false;
            }
        };
    }

    public void _testCreateReviewHardcoded() throws ServerPasswordNotProvidedException {

        //facade.setCrucibleSession(null);

        ServerBean server = new ServerBean();
        server.setUrlString("http://lech.atlassian.pl:8060");
        server.setUserName("test");
        server.transientSetPasswordString("test", false);

        Review review = prepareReviewData("test", State.DRAFT);

        Review ret;

        try {
            ret = facade.createReview(server, review);
            assertNotNull(ret);
            assertNotNull(ret.getPermId());
            assertNotNull(ret.getPermId().getId());
            assertTrue(ret.getPermId().getId().length() > 0);
        } catch (RemoteApiException e) {
            fail(e.getMessage());
        }
    }

    public void _testGetAllReviewsHardcoded() throws ServerPasswordNotProvidedException {
        //facade.setCrucibleSession(null);

        ServerBean server = new ServerBean();
        server.setUrlString("http://lech.atlassian.pl:8060");
        server.setUserName("test");
        server.transientSetPasswordString("test", false);

        try {
            List<Review> list = facade.getAllReviews(server);
            assertNotNull(list);
            assertTrue(list.size() > 0);
        } catch (RemoteApiException e) {
            fail(e.getMessage());
        }
    }
}
