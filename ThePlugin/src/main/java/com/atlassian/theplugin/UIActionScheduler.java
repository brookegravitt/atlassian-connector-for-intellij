package com.atlassian.theplugin;

/**
 * Interface for IDE-specific method to invoke methods in User Interface thread.
 */
public interface UIActionScheduler {
	void invokeLater(Runnable action);
}
