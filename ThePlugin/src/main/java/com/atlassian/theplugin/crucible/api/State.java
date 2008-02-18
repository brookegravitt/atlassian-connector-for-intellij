
package com.atlassian.theplugin.crucible.api;

public enum State {
    APPROVAL("Approval"),
    CLOSED("Closed"),
    DEAD("Dead"),
    DRAFT("Draft"),
    REJECTED("Rejected"),
    REVIEW("Review"),
    SUMMARIZE("Summarize"),
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
        throw new IllegalArgumentException(v);
    }

}