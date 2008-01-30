package com.atlassian.theplugin.bamboo;

import java.util.Collection;

public interface BambooStatusListener {
	void updateBuildStatuses(Collection<BambooBuild> buildStatuses);
}
