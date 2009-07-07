package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.jira.cache.JIRAServerModelImpl;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: pmaruszak
 * Date: Jul 2, 2009
 */
public class JIRAServerModelIdea extends JIRAServerModelImpl implements FrozenModel  {
    private boolean modelFrozen = false;
	private Collection<FrozenModelListener> frozenListeners = new ArrayList<FrozenModelListener>();
    
    public JIRAServerModelIdea() {
        super(null);
    }

	public boolean isModelFrozen() {
		return this.modelFrozen;
	}

    public boolean isFrozen() {
        return isModelFrozen();
    }
	public synchronized void setModelFrozen(boolean frozen) {
		this.modelFrozen = frozen;
		setChanged(false);
		fireModelFrozen();
	}

	public void addFrozenModelListener(FrozenModelListener listener) {
		frozenListeners.add(listener);
	}

	public void removeFrozenModelListener(FrozenModelListener listener) {
		frozenListeners.remove(listener);
	}

	private void fireModelFrozen() {
		for (FrozenModelListener listener : frozenListeners) {
			listener.modelFrozen(this, modelFrozen);
		}
	}
}
