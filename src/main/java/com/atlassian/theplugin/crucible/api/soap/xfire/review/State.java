
package com.atlassian.theplugin.crucible.api.soap.xfire.review;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for state.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="state">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Unknown"/>
 *     &lt;enumeration value="Rejected"/>
 *     &lt;enumeration value="Dead"/>
 *     &lt;enumeration value="Closed"/>
 *     &lt;enumeration value="Summarize"/>
 *     &lt;enumeration value="Review"/>
 *     &lt;enumeration value="Approval"/>
 *     &lt;enumeration value="Draft"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum State {

    @XmlEnumValue("Approval")
    APPROVAL("Approval"),
    @XmlEnumValue("Closed")
    CLOSED("Closed"),
    @XmlEnumValue("Dead")
    DEAD("Dead"),
    @XmlEnumValue("Draft")
    DRAFT("Draft"),
    @XmlEnumValue("Rejected")
    REJECTED("Rejected"),
    @XmlEnumValue("Review")
    REVIEW("Review"),
    @XmlEnumValue("Summarize")
    SUMMARIZE("Summarize"),
    @XmlEnumValue("Unknown")
    UNKNOWN("Unknown");
    private final String value;

    State(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static State fromValue(String v) {
        for (State c : State.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
