package com.atlassian.theplugin.crucible.model;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;

import java.util.Comparator;

/**
 * User: jgorycki
 * Date: Dec 23, 2008
 * Time: 11:15:51 AM
 */
public class ReviewKeyComparator implements Comparator<ReviewAdapter> {

	public int compare(ReviewAdapter review1, ReviewAdapter review2) {
		if (review1 == null || review2 == null) {
			return 0;
		}
		String key = review1.getPermId().getId();
		String key1 = review2.getPermId().getId();

		// first, try to compare on projects
		if (review1.getProjectKey() == null || review2.getProjectKey() == null) {
			return 0;
		}
		if (!review1.getProjectKey().equals(review2.getProjectKey())) {
			return review1.getProjectKey().compareTo(review2.getProjectKey());
		}

		// otherwise, if the same project - sort on review1 ID
		Integer count;
		try {
			count = new Integer(key.substring(key.lastIndexOf("-") + 1));
		} catch (Exception e) {
			// unable to compare
			return 0;
		}
		Integer count1;
		try {
			count1 = new Integer(key1.substring(key1.lastIndexOf("-") + 1));
		} catch (Exception e) {
			// unable to compare
			return 0;
		}

		return count.compareTo(count1);
	}
}
