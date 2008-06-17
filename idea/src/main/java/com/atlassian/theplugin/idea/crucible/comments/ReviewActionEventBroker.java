package com.atlassian.theplugin.idea.crucible.comments;

import com.intellij.openapi.project.Project;
import com.intellij.util.containers.WeakHashMap;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
import com.atlassian.theplugin.idea.crucible.events.CrucibleEvent;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;
import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.util.PluginUtil;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 17, 2008
 * Time: 10:53:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class ReviewActionEventBroker {
	private static WeakHashMap<Project, ReviewActionEventBroker> brokers = new WeakHashMap<Project, ReviewActionEventBroker>();
	private Set<CrucibleReviewActionListener> listeners = new HashSet<CrucibleReviewActionListener>();
	private Queue<CrucibleEvent> events = new LinkedBlockingQueue<CrucibleEvent>();
	public static final Logger LOGGER = PluginUtil.getLogger();

	private ReviewActionEventBroker() {
		super();
		new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						CrucibleEvent event = ((LinkedBlockingQueue<CrucibleEvent>) events).take();
						event.run();
					}
				} catch (InterruptedException e) {
					//swallowed
				}
			}
		}, "atlassian-idea-plugin Crucible events processor"
		).start();
	}

	public static ReviewActionEventBroker getInstance(Project p) {
		ReviewActionEventBroker instance = brokers.get(p);
		if (instance == null) {
			instance = new ReviewActionEventBroker();
			brokers.put(p, instance);
		}
		return instance;
	}

	public void registerListener(CrucibleReviewActionListener listener) {
		listeners.add(listener);
	}

	public void unregisterListener(CrucibleReviewActionListener listener) {
		listeners.remove(listener);
	}

	public void trigger(CrucibleEvent event) {
		events.add(event);
	}

	public Iterable<? extends CrucibleReviewActionListener> getListeners() {
		return Collections.unmodifiableSet(listeners);
	}

}
