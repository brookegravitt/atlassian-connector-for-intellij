
package com.atlassian.theplugin.crucible.api.soap.xfire.review;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for reviewData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="reviewData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="author" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="creator" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="moderator" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="parentReview" type="{http://rpc.spi.crucible.atlassian.com/}permId" minOccurs="0"/>
 *         &lt;element name="permaId" type="{http://rpc.spi.crucible.atlassian.com/}permId" minOccurs="0"/>
 *         &lt;element name="projectKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="repoName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="state" type="{http://rpc.spi.crucible.atlassian.com/}state" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "reviewData", propOrder = {
    "author",
    "creator",
    "description",
    "moderator",
    "name",
    "parentReview",
    "permaId",
    "projectKey",
    "repoName",
    "state"
})
public class ReviewData {

    protected String author;
    protected String creator;
    protected String description;
    protected String moderator;
    protected String name;
    protected PermId parentReview;
    protected PermId permaId;
    protected String projectKey;
    protected String repoName;
    protected State state;

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
     *     {@link PermId }
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
     *     {@link PermId }
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
     *     {@link PermId }
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
     *     {@link PermId }
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
     *     {@link State }
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
     *     {@link State }
     *     
     */
    public void setState(State value) {
        this.state = value;
    }

}
