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

import com.atlassian.theplugin.commons.crucible.ValueNotYetInitialized;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.crucible.ReviewAdapter;
import static com.atlassian.theplugin.util.ReviewInfoUtil.getNumOfCompletedReviewers;

import javax.swing.*;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


public class ReviewReviewersColumn extends TableColumnInfo {
    private static final int COL_WIDTH = new JLabel("Reviewers").getPreferredSize().width;

    public String getColumnName() {
        return "Reviewers";
    }

    public Object valueOf(Object o) {
        return o;
    }

    public Class getColumnClass() {
        return String.class;
    }

    public Comparator getComparator() {
//        return new Comparator() {
//            public int compare(Object o, Object o1) {
//                String r = getReviewersAsText(o);
//                String r1 = getReviewersAsText(o1);
//                return r.compareTo(r1);
//            }
//        };

		 return new Comparator() {
            public int compare(Object o, Object o1) {
				ReviewAdapter review1 = (ReviewAdapter) o;
				ReviewAdapter review2 = (ReviewAdapter) o1;
				List<Reviewer> r1 = null;
				List<Reviewer> r2 = null;
				int r1s = 0;
				int r2s = 0;
				try {
					r1 = review1.getReviewers();
					r1s = r1.size();
				} catch (ValueNotYetInitialized valueNotYetInitialized) {
                    // TODO lguminsk check what to do or comment here
                }
				try {
					r2 = review2.getReviewers();
					r2s = r2.size();
				} catch (ValueNotYetInitialized valueNotYetInitialized) {
                    // TODO lguminsk check what to do or comment here
				}

				if (r1s == r2s) {
					return getNumOfCompletedReviewers(review1) - getNumOfCompletedReviewers(review2);
				} else {
					return r1s - r2s;
				}
            }
        };
	}

	public static String getReviewersAsText(Object o) {
		StringBuffer sb = new StringBuffer();
		try {
			if (((Review) o).getReviewers() != null) {
				for (Iterator<Reviewer> iterator = ((Review) o).getReviewers().iterator(); iterator.hasNext();) {
					sb.append(iterator.next().getUserName());
					if (iterator.hasNext()) {
						sb.append(", ");
					}
				}
			}
		} catch (ValueNotYetInitialized valueNotYetInitialized) {
			return "Error retrieving reviewers";
		}
		return sb.toString();
	}

	public int getPrefferedWidth() {
        return COL_WIDTH;
    }


}