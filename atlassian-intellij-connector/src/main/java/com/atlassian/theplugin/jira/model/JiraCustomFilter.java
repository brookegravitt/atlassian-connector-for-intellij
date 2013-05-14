package com.atlassian.theplugin.jira.model;

import com.atlassian.connector.commons.jira.beans.*;
import com.atlassian.connector.commons.jira.cache.CacheConstants;
import com.google.common.base.Function;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * User: pmaruszak
 */
public final class JiraCustomFilter implements JiraFilter {
	private static final int HASH_NUMBER = 31;

    public enum QueryElement {
		PROJECT("Project", "project"),
		ISSUE_TYPE("Issue Type", "type"),
		FIX_FOR("Fix For", "fixVersion"),
		COMPONENTS("Components", "component"),
		AFFECTS_VERSIONS("Affects Versions", "affectedVersion"),
		REPORTER("Reporter", "reporter"),
		ASSIGNEE("Assignee", "assignee"),
		STATUS("Status", "status"),
		RESOLUTIONS("Resolutions", "resolution"),
		PRIORITIES("Priorities", "priority"),
		UNKNOWN("Unknown", null);

		private String name;
        private String jqlName;

		QueryElement(final String name, final String jqlName) {
			this.name = name;
            this.jqlName = jqlName;
		}

		public String getName() {
			return name;
		}

        public String jqlName() {
            return jqlName;
        }
	}

    public boolean isEmpty() {
        return queryFragments == null || queryFragments.size() <= 0;
    }

	private List<JIRAQueryFragment> queryFragments = new ArrayList<JIRAQueryFragment>();

	private String name;
    private String uid;

    public JiraCustomFilter() {
         this.uid = UUID.randomUUID().toString();
         this.name = "Custom Filter";
    }

	JiraCustomFilter(final String uid, final String name, List<JIRAQueryFragment> queryFragments) {
        this.uid = uid;
        this.name = name;
		this.queryFragments = queryFragments;
	}

    public String getQueryStringFragment() {
        return null;
    }

    public long getId() {
        return 0;
    }

    public String getName() {
		return name;
	}

    public HashMap<String, String> getMap() {
        return null;
    }

    public JIRAQueryFragment getClone() {
        return null;
    }

    public void setName(final String name) {
		this.name = name;
	}


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<JIRAQueryFragment> getQueryFragments() {
        return queryFragments;
    }

    public void setQueryFragments(final List<JIRAQueryFragment> queryFragments) {
		this.queryFragments = queryFragments;
	}

	public Map<QueryElement, ArrayList<String>> groupBy(final boolean skipAnyValues) {
		TreeMap<QueryElement, ArrayList<String>> map = new TreeMap<QueryElement, ArrayList<String>>();

		for (JIRAQueryFragment fragment : queryFragments) {
            QueryElement qe = getQueryElementType(fragment);
			addValueToNameMap(map, qe, fragment, skipAnyValues);
		}
		return map;
	}

    public Map<QueryElement, ArrayList<JIRAQueryFragment>> groupFragmentsBy(final boolean skipAnyValues) {
        TreeMap<QueryElement, ArrayList<JIRAQueryFragment>> map = new TreeMap<QueryElement, ArrayList<JIRAQueryFragment>>();

        for (JIRAQueryFragment fragment : queryFragments) {
            QueryElement qe = getQueryElementType(fragment);
            addValueToQueryMap(map, qe, fragment, skipAnyValues);
        }
        return map;

    }

    private QueryElement getQueryElementType(JIRAQueryFragment fragment) {
        QueryElement qe = QueryElement.UNKNOWN;
        if (fragment instanceof JIRAProjectBean) {
            qe = QueryElement.PROJECT;
        } else if (fragment instanceof JIRAIssueTypeBean) {
            qe = QueryElement.ISSUE_TYPE;
        } else if (fragment instanceof JIRAStatusBean) {
            qe = QueryElement.STATUS;
        } else if (fragment instanceof JIRAPriorityBean) {
            qe = QueryElement.PRIORITIES;
        } else if (fragment instanceof JIRAResolutionBean) {
            qe = QueryElement.RESOLUTIONS;
        } else if (fragment instanceof JIRAFixForVersionBean) {
            qe = QueryElement.FIX_FOR;
        } else if (fragment instanceof JIRAComponentBean) {
            qe = QueryElement.COMPONENTS;
        } else if (fragment instanceof JIRAVersionBean) {
            qe = QueryElement.AFFECTS_VERSIONS;
        } else if (fragment instanceof JIRAAssigneeBean) {
            qe = QueryElement.ASSIGNEE;
        } else if (fragment instanceof JIRAReporterBean) {
            qe = QueryElement.REPORTER;
        }
        return qe;
    }

    private void addValueToNameMap(final TreeMap<QueryElement, ArrayList<String>> map, final QueryElement key,
                                   final JIRAQueryFragment fragment, final boolean skipAnyValues) {
		if (!skipAnyValues || fragment.getId() != CacheConstants.ANY_ID) {
			if (!map.containsKey(key)) {
				map.put(key, new ArrayList<String>());
			}
			map.get(key).add(fragment.getName());
		}
	}

    private void addValueToQueryMap(final TreeMap<QueryElement, ArrayList<JIRAQueryFragment>> map, final QueryElement key,
                                   final JIRAQueryFragment fragment, final boolean skipAnyValues) {
        if (!skipAnyValues || fragment.getId() != CacheConstants.ANY_ID) {
            if (!map.containsKey(key)) {
                map.put(key, new ArrayList<JIRAQueryFragment>());
            }
            map.get(key).add(fragment);
        }
    }

    public String getJql() {
        StringBuilder sb = new StringBuilder();
        int cnt = 0;
        Map<QueryElement, ArrayList<JIRAQueryFragment>> groups = groupFragmentsBy(true);
        for (QueryElement key : groups.keySet()) {
            ArrayList<JIRAQueryFragment> group = groups.get(key);
            switch (key) {
                case REPORTER:
                    if (group.size() > 0) {
                        if (cnt++ > 0) {
                            sb.append(" and ");
                        }
                    }
                    String reporter = ((JIRAReporterBean) group.get(0)).getValue();
                    if (reporter.length() != 0) {
                        sb.append("reporter = \"").append(reporter).append("\"");
                    }
                    break;
                case ASSIGNEE:
                    if (group.size() > 0) {
                        if (cnt++ > 0) {
                            sb.append(" and ");
                        }
                        JIRAAssigneeBean a = (JIRAAssigneeBean) group.get(0);
                        String assignee = a.getValue();
                        if (assignee.equals("unassigned")) {
                            sb.append("assignee is EMPTY");
                        } else if (assignee.length() != 0) {
                            sb.append("assignee = \"").append(assignee).append("\"");
                        }
                    }
                    break;
                case ISSUE_TYPE:
                case AFFECTS_VERSIONS:
                case FIX_FOR:
                case COMPONENTS:
//                case STATUS:
//                case PRIORITIES:
                    cnt += joinJqlGroups(sb, key.jqlName(), groups.get(key), new CopyName());
                    break;
//                case RESOLUTIONS:
//                    cnt += joinJqlGroups(sb, key.jqlName(), groups.get(key), new CopyResolution());
//                    break;
                case STATUS:
                case PRIORITIES:
                    cnt += joinJqlGroups(sb, key.jqlName(), groups.get(key), new CopyId());
                    break;
                case RESOLUTIONS:
                    cnt += joinJqlGroups(sb, key.jqlName(), groups.get(key), new CopyResolution());
                    break;
                case PROJECT:
                    cnt += joinJqlGroups(sb, key.jqlName(), groups.get(key), new CopyProjectKey());
                    break;
                default:
                    break;
            }
        }
        return sb.toString();
    }

    private class CopyName implements Function<JIRAQueryFragment, String> {
        public String apply(@Nullable JIRAQueryFragment s) {
            return " = \"" + s.getName() + "\"";
        }
    }

    private class CopyProjectKey implements Function<JIRAQueryFragment, String> {
        public String apply(@Nullable JIRAQueryFragment s) {
            JIRAProjectBean p = (JIRAProjectBean) s;
            return " = " + p.getKey();
        }
    }

//    private class CopyResolution implements Function<JIRAQueryFragment, String> {
//        public String apply(@Nullable JIRAQueryFragment s) {
//            JIRAResolutionBean r = (JIRAResolutionBean) s;
//            if (-1 == r.getId()) return "Unresolved";
//            return r.getName();
//        }
//    }

    private class CopyId implements Function<JIRAQueryFragment, String> {
        public String apply(@Nullable JIRAQueryFragment s) {
            JIRAConstant r = (JIRAConstant) s;
            return " = " + r.getId();
        }
    }

    private class CopyResolution implements Function<JIRAQueryFragment, String> {
        public String apply(@Nullable JIRAQueryFragment s) {
            JIRAConstant r = (JIRAConstant) s;
            return r.getId() == -1 ? " is empty" : " = " + r.getId();
        }
    }

    private static int joinOptions(StringBuilder sb, String jqlName, List<JIRAQueryFragment> what, Function<JIRAQueryFragment, String> getValue) {
        int cnt = 0;
        for (JIRAQueryFragment s : what) {
            if (cnt++ > 0) {
                sb.append(" or ");
            }
            sb.append(jqlName).append(getValue.apply(s));
        }
        return cnt;
    }

    private static int joinJqlGroups(StringBuilder sb, String jqlName, List<JIRAQueryFragment> what, Function<JIRAQueryFragment, String> getValue) {
        if (what.size() == 0) return 0;

        if (sb.length() > 0) sb.append(" and ");
        sb.append("(");
        int cnt = joinOptions(sb, jqlName, what, getValue);
        sb.append(")");
        return cnt;
    }

    public String getOldStyleQueryString() {
        StringBuilder query = new StringBuilder();

        List<JIRAQueryFragment> fragmentsWithoutAnys = new ArrayList<JIRAQueryFragment>();
        for (JIRAQueryFragment jiraQueryFragment : getQueryFragments()) {
            if (jiraQueryFragment.getId() != CacheConstants.ANY_ID) {
                fragmentsWithoutAnys.add(jiraQueryFragment);
            }
        }

        for (JIRAQueryFragment fragment : fragmentsWithoutAnys) {
            if (fragment.getQueryStringFragment() != null) {
                query.append("&").append(fragment.getQueryStringFragment());
            }
        }

        return query.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JiraCustomFilter that = (JiraCustomFilter) o;

        if (uid != null ? !uid.equals(that.uid) : that.uid != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return uid != null ? uid.hashCode() : 0;
    }
}
