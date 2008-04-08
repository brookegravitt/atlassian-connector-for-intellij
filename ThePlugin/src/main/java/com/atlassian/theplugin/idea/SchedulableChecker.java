package com.atlassian.theplugin.idea;

import java.util.TimerTask;

public interface SchedulableChecker {
	/**
	 * Create a new instance of TimerTask for this component so it can be used for (re)scheduling in
	 * {@link java.util.Timer}
	 * @return new instance of TimerTask
	 */
	TimerTask newTimerTask();

	/**
	 * Provides info whether the component should be scheduled - ie. if there is any work for it to be done on timer.
	 * @return true when this component wants to be scheduled.
	 */
	boolean canSchedule();

	/**
	 * Preferred scheduling interval in milliseconds.
	 * <p>
	 * Will be used for {@link java.util.Timer#schedule(java.util.TimerTask, long)}.
	 *  
	 * @return Preferred scheduling interval in milliseconds.
	 */
	long getInterval();

	void resetListeners();
}
