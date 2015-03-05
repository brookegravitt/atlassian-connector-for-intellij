package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.jira.IntelliJJiraServerFacade;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModelImpl;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: pmaruszak
 * Date: Jul 2, 2009
 */
public class JIRAServerModelIdea extends JIRAServerModelImpl implements FrozenModel, ApplicationComponent {
    private boolean modelFrozen = false;
	private Collection<FrozenModelListener> frozenListeners = new ArrayList<FrozenModelListener>();
    
    public JIRAServerModelIdea() {
        super(null);
        IntelliJJiraServerFacade.setServerModel(this);
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

	public synchronized void addFrozenModelListener(FrozenModelListener listener) {
		frozenListeners.add(listener);
	}

	public synchronized void removeFrozenModelListener(FrozenModelListener listener) {
		frozenListeners.remove(listener);
	}

	private synchronized void fireModelFrozen() {
		for (FrozenModelListener listener : frozenListeners) {
			listener.modelFrozen(this, modelFrozen);
		}
	}

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return getClass().getCanonicalName();
    }
}
