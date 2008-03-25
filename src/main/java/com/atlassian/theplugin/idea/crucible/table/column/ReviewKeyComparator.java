package com.atlassian.theplugin.idea.crucible.table.column;

import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;

import java.io.Serializable;
import java.util.Comparator;

public class ReviewKeyComparator implements Comparator, Serializable {
	static final long serialVersionUID = 903490105978352608L;

	public int compare(Object o, Object o1) {
        ReviewDataInfoAdapter review = (ReviewDataInfoAdapter) o;
        String key = review.getPermaId().getId();
        ReviewDataInfoAdapter review1 = (ReviewDataInfoAdapter) o1;
        String key1 = review1.getPermaId().getId();

        // first, try to compare on projects
        if (!review.getProjectKey().equals(review1.getProjectKey())) {
            return review.getProjectKey().compareTo(review1.getProjectKey());
        }

        // otherwise, if the same project - sort on review ID
        Integer count = new Integer(key.substring(key.indexOf("-") + 1));
        Integer count1 = new Integer(key1.substring(key1.indexOf("-") + 1));

        return count.compareTo(count1);
    }
}