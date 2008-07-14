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

import com.atlassian.theplugin.idea.TableColumnInfo;
import com.atlassian.theplugin.idea.crucible.ReviewDataInfoAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;

import java.util.Comparator;
import java.util.Iterator;


public class ReviewReviewersColumn extends TableColumnInfo {
    private static final int COL_WIDTH = 200;

    public String getColumnName() {
        return "Reviewers completed";
    }

    public Object valueOf(Object o) {
        return o;
    }

    public Class getColumnClass() {
        return String.class;
    }

    public Comparator getComparator() {
        return new Comparator() {
            public int compare(Object o, Object o1) {
                String r = getReviewersAsText(o);
                String r1 = getReviewersAsText(o1);
                return r.compareTo(r1);
            }
        };
    }

	public static String getReviewersAsText(Object o) {
		StringBuffer sb = new StringBuffer();
		if (((ReviewDataInfoAdapter) o).getReviewers() != null) {
			for (Iterator<Reviewer> iterator = ((ReviewDataInfoAdapter) o).getReviewers().iterator(); iterator.hasNext();) {
				sb.append(iterator.next().getUserName());
				if (iterator.hasNext()) {
					sb.append(", ");
				}
			}
		}
		return sb.toString();
	}

	public int getPrefferedWidth() {
        return COL_WIDTH;
    }


}