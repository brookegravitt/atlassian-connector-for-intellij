package com.atlassian.theplugin.idea.crucible.comments;

import com.intellij.openapi.project.Project;
import com.intellij.util.containers.WeakHashMap;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
import com.atlassian.theplugin.idea.crucible.tree.ReviewItemTreePanel;
import com.atlassian.theplugin.commons.crucible.api.model.ReviewItem;

import java.util.*;

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

	private ReviewActionEventBroker() {
		super();

		// IF YOU GET A COMPILATION ERROR HERE, SCROLL DOWN AND APPROPRIATE METHOD IN THE BROKER!!!
		// (this piece of code below is just to remember about that)
		CrucibleReviewActionListener defaultListener = new CrucibleReviewActionListener() {
			public void focusOnReview(ReviewDataInfoAdapter reviewItem) {
				//To change body of implemented methods use File | Settings | File Templates.
			}

			public void focusOnFile(ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem) {
				//To change body of implemented methods use File | Settings | File Templates.
			}
		};
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

	public void focusOnReview(CrucibleReviewActionListener caller, ReviewDataInfoAdapter reviewItem) {
		for (CrucibleReviewActionListener listener : listeners) {
			if (listener == caller) {
				continue;
			}
			listener.focusOnReview(reviewItem);
		}
	}

	public void focusOnFile(CrucibleReviewActionListener caller, ReviewDataInfoAdapter reviewDataInfoAdapter, ReviewItem reviewItem) {
		for (CrucibleReviewActionListener listener : listeners) {
			if (listener == caller) {
				continue;
			}
			listener.focusOnFile(reviewDataInfoAdapter, reviewItem);
		}
	}
}
