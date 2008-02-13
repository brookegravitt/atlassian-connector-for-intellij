
package com.atlassian.theplugin.crucible.api.soap.xfire.review;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for action.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="action">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Recover"/>
 *     &lt;enumeration value="Reopen"/>
 *     &lt;enumeration value="Close"/>
 *     &lt;enumeration value="Summarize"/>
 *     &lt;enumeration value="Reject"/>
 *     &lt;enumeration value="Delete"/>
 *     &lt;enumeration value="Approve"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum Action {

    @XmlEnumValue("Approve")
    APPROVE("Approve"),
    @XmlEnumValue("Close")
    CLOSE("Close"),
    @XmlEnumValue("Delete")
    DELETE("Delete"),
    @XmlEnumValue("Recover")
    RECOVER("Recover"),
    @XmlEnumValue("Reject")
    REJECT("Reject"),
    @XmlEnumValue("Reopen")
    REOPEN("Reopen"),
    @XmlEnumValue("Summarize")
    SUMMARIZE("Summarize");
    private final String value;

    Action(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Action fromValue(String v) {
        for (Action c : Action.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
