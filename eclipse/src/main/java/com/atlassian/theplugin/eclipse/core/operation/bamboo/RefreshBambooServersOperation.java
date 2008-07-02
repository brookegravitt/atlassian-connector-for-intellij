/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.core.operation.bamboo;

import org.eclipse.core.runtime.IProgressMonitor;

import com.atlassian.theplugin.eclipse.core.bamboo.BambooServer;
import com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer;
import com.atlassian.theplugin.eclipse.core.operation.AbstractNonLockingOperation;
import com.atlassian.theplugin.eclipse.core.operation.IUnprotectedOperation;
import com.atlassian.theplugin.eclipse.ui.bamboo.BambooServersView;
import com.atlassian.theplugin.eclipse.ui.bamboo.BambooTreeViewer;

/**
 * Refresh repository location in the repository tree operation
 * 
 * @author Alexander Gurov
 */
public class RefreshBambooServersOperation extends AbstractNonLockingOperation {
	protected IBambooServer [] servers;
	protected boolean deep;
	
	public RefreshBambooServersOperation(boolean deep) {
		this(null, deep);
	}

	public RefreshBambooServersOperation(IBambooServer [] servers, boolean deep) {
		super("Operation.RefreshBambooServers");
		this.servers = servers;
		this.deep = deep;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		if (this.servers == null) {
			BambooServersView.refreshRepositories(this.deep);
			return;
		}
		
		for (int i = 0; i < this.servers.length; i++) {
			final IBambooServer current = this.servers[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					BambooServersView.refresh(current, new BambooTreeViewer.IRefreshVisitor() {
						public void visit(Object data) {
							if (data instanceof BambooServer && RefreshBambooServersOperation.this.deep) {
								//FIXME: ((BambooServer)data).refresh();
							}
						}
					});
				}
			}, monitor, this.servers.length);
		}
	}
	
}
