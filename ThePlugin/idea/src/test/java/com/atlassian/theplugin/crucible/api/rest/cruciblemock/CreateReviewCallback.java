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

package com.atlassian.theplugin.crucible.api.rest.cruciblemock;

import com.atlassian.theplugin.commons.crucible.api.model.PermIdBean;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.rest.CrucibleRestXmlHelper;
import com.atlassian.theplugin.commons.crucible.CrucibleChangeSet;
import com.atlassian.theplugin.commons.crucible.CrucibleChangeSetImpl;
import com.atlassian.theplugin.commons.configuration.ServerBean;
import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class CreateReviewCallback implements JettyMockServer.Callback {
    public static final String REPO_NAME = "AtlassianSVN";
    public static final String PERM_ID = "PR-1";

    public void onExpectedRequest(String target,
                                  HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        assertTrue(request.getPathInfo().endsWith("/rest-service/reviews-v1"));
        assertTrue("POST".equalsIgnoreCase(request.getMethod()));

        SAXBuilder builder = new SAXBuilder();
        Document req = builder.build(request.getInputStream());

        XPath xpath = XPath.newInstance("/createReview/reviewData");
        @SuppressWarnings("unchecked")
        List<Element> elements = xpath.selectNodes(req);

		ServerBean server = new ServerBean();
		Review reqReview = CrucibleRestXmlHelper.parseReviewNode(server, elements.get(0));

        CrucibleChangeSetImpl reviewData = null;
        if (elements != null && !elements.isEmpty()) {
            reviewData = (CrucibleChangeSetImpl) CrucibleRestXmlHelper.parseReviewNode(server, 
					elements.iterator().next());
            reviewData.setState(State.DRAFT);
            PermIdBean permId = new PermIdBean();
            permId.setId(PERM_ID);
            reviewData.setPermaId(permId);
            reviewData.setRepoName(REPO_NAME);
            reviewData.setAuthor(reqReview.getAuthor());
            reviewData.setCreator(reqReview.getCreator());
            reviewData.setModerator(reqReview.getModerator());
        }

        Document doc = CrucibleRestXmlHelper.prepareReviewNode(reviewData);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        outputter.output(doc, response.getOutputStream());
    }
}
