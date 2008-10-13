/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea.crucible.table.column;


import com.atlassian.theplugin.idea.crucible.ReviewDataImpl;

import java.io.Serializable;
import java.util.Comparator;

public class ReviewKeyComparator implements Comparator, Serializable {
    static final long serialVersionUID = 903490105978352608L;

    public int compare(Object o, Object o1) {
        if (o == null || o1 == null) {
            return 0;
        }
        ReviewDataImpl review = (ReviewDataImpl) o;
        String key = review.getPermId().getId();
        ReviewDataImpl review1 = (ReviewDataImpl) o1;
        String key1 = review1.getPermId().getId();

        // first, try to compare on projects
        if (review.getProjectKey() == null || review1.getProjectKey() == null) {
            return 0;
        }
        if (!review.getProjectKey().equals(review1.getProjectKey())) {
            return review.getProjectKey().compareTo(review1.getProjectKey());
        }

        // otherwise, if the same project - sort on review ID
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