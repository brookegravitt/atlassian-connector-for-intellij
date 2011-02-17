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

package com.atlassian.theplugin.idea.ui.tree.file;

import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;

import java.util.HashMap;
import java.util.Map;

public abstract class FileNode extends AtlassianTreeNode {

	private Map<String, FileNode> children;
	private String name;	

	public FileNode(String fullName, AtlassianClickAction action) {
		super(action);
		name = fullName;
		children = new HashMap<String, FileNode>();
	}

	public FileNode(final FileNode node) {
		super(node.getAtlassianClickAction());
		this.name = node.name;
		this.children = new HashMap<String, FileNode>();
		this.children.putAll(node.children);

	}

	public void addChild(FileNode child) {
//		if (!getChildren().containsKey(child.getName())) {
			getChildren().put(child.getName(), child);
			add(child);
//		}
	}

	public void removeChild(FileNode child) {
		if (getChildren().containsKey(child.getName())) {
			getChildren().remove(child.getName());
			remove(child);
		}
	}

	public void removeChildren() {
		getChildren().clear();
		removeAllChildren();
	}

	public boolean hasNode(String fullName) {
		return getChildren().containsKey(fullName);
	}

	public FileNode getNode(String fullName) {
		return getChildren().get(fullName);
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		name = newName;
	}

	public int compareTo(Object o) {
		if (o instanceof FileNode) {
			FileNode fn = (FileNode) o;
			return getName().compareTo(fn.getName());
		}
		return -1; // fixme?
	}

	@Override
	public String toString() {
		return getName();
	}

	public Map<String, FileNode> getChildren() {
		return children;
	}

	public boolean isCompactable() {
		return true;
	}
}
