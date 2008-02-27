package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.configuration.Server;

import java.util.Collection;


public interface BambooPlanListener {
	void updatePlanNames(Server server, Collection<BambooPlan> plans);
}
