package com.atlassian.theplugin.commons.crucible.api.model;



public class TransitionBean implements Transition {
    private State state;
    private State nextState;
    private String actionName;
    private String displayName;

    public TransitionBean() {
    }

    public TransitionBean(State state, State nextState, String actionName, String displayName) {
        this.state = state;
        this.nextState = nextState;
        this.actionName = actionName;
        this.displayName = displayName;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public State getNextState() {
        return nextState;
    }

    public void setNextState(State nextState) {
        this.nextState = nextState;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
