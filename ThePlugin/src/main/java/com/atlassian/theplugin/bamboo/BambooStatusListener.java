package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.StatusListener;

import java.util.Collection;

public interface BambooStatusListener extends StatusListener {
	void updateBuildStatuses(Collection<BambooBuild> buildStatuses);

}
