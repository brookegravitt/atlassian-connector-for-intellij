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

import com.atlassian.theplugin.eclipse.core.operation.AbstractNonLockingOperation;
import com.atlassian.theplugin.eclipse.core.operation.IUnprotectedOperation;
import com.atlassian.theplugin.eclipse.ui.bamboo.BambooServersView;
import com.atlassian.theplugin.eclipse.ui.bamboo.BambooTreeViewer;
import com.atlassian.theplugin.eclipse.ui.bamboo.IBambooTreeNode;

/**
 * Refresh repository location in the repository tree operation
 * 
 * @author Alexander Gurov
 */
public class RefreshBambooServersOperation extends AbstractNonLockingOperation {
	protected Object [] nodes;
	protected boolean deep;
	
	public RefreshBambooServersOperation(boolean deep) {
		this(null, deep);
	}

	public RefreshBambooServersOperation(Object [] nodes, boolean deep) {
		super("Operation.RefreshBambooServers");
		this.nodes = nodes;
		this.deep = deep;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		if (this.nodes == null) {
			BambooServersView.refreshRepositories(this.deep);
			return;
		}
		
		for (int i = 0; i < this.nodes.length; i++) {
			final Object current = this.nodes[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					BambooServersView.refresh(current, new BambooTreeViewer.IRefreshVisitor() {
						public void visit(Object data) {
							if (data instanceof IBambooTreeNode && RefreshBambooServersOperation.this.deep) {
								((IBambooTreeNode)data).refresh();
							}
						}
					});
				}
			}, monitor, this.nodes.length);
		}
	}
	
}
