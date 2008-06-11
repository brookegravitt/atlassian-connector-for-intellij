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

package com.atlassian.theplugin.commons.crucible.api.model;

public class ReviewBean implements Review {

    private String author;
    private String creator;
    private String description;
    private String moderator;
    private String name;
    private PermId parentReview;
    private PermId permaId;
    private String projectKey;
    private String repoName;
    private State state;
    private int metricsVersion;

    /**
     * Gets the value of the author property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the value of the author property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAuthor(String value) {
        this.author = value;
    }

    /**
     * Gets the value of the creator property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Sets the value of the creator property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCreator(String value) {
        this.creator = value;
    }

    /**
     * Gets the value of the description property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the moderator property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getModerator() {
        return moderator;
    }

    /**
     * Sets the value of the moderator property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setModerator(String value) {
        this.moderator = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the parentReview property.
     *
     * @return
     *     possible object is
     *     {@link com.atlassian.theplugin.commons.crucible.api.model.PermId }
     *
     */
    public PermId getParentReview() {
        return parentReview;
    }

    /**
     * Sets the value of the parentReview property.
     *
     * @param value
     *     allowed object is
     *     {@link com.atlassian.theplugin.commons.crucible.api.model.PermId }
     *
     */
    public void setParentReview(PermId value) {
        this.parentReview = value;
    }

    /**
     * Gets the value of the permaId property.
     *
     * @return
     *     possible object is
     *     {@link com.atlassian.theplugin.commons.crucible.api.model.PermId }
     *
     */
    public PermId getPermaId() {
        return permaId;
    }

    /**
     * Sets the value of the permaId property.
     *
     * @param value
     *     allowed object is
     *     {@link com.atlassian.theplugin.commons.crucible.api.model.PermId }
     *
     */
    public void setPermaId(PermId value) {
        this.permaId = value;
    }

    /**
     * Gets the value of the projectKey property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getProjectKey() {
        return projectKey;
    }

    /**
     * Sets the value of the projectKey property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setProjectKey(String value) {
        this.projectKey = value;
    }

    /**
     * Gets the value of the repoName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getRepoName() {
        return repoName;
    }

    /**
     * Sets the value of the repoName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setRepoName(String value) {
        this.repoName = value;
    }

    /**
     * Gets the value of the state property.
     *
     * @return
     *     possible object is
     *     {@link com.atlassian.theplugin.commons.crucible.api.model.State }
     *
     */
    public State getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     *
     * @param value
     *     allowed object is
     *     {@link com.atlassian.theplugin.commons.crucible.api.model.State }
     *
     */
    public void setState(State value) {
        this.state = value;
    }

    public int getMetricsVersion() {
        return metricsVersion;
    }

    public void setMetricsVersion(int metricsVersion) {
        this.metricsVersion = metricsVersion;
    }
}