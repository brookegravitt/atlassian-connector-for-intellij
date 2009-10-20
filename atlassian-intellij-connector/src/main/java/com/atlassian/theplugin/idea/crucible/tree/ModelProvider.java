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

package com.atlassian.theplugin.idea.crucible.tree;

import com.atlassian.theplugin.idea.ui.tree.AtlassianClickAction;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeModel;
import com.atlassian.theplugin.idea.ui.tree.file.FolderNode;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 30, 2008
 * Time: 11:09:07 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ModelProvider {

	public abstract AtlassianTreeModel getModel(final AtlassianTreeWithToolbar.State state);

	public static final ModelProvider EMPTY_MODEL_PROVIDER = new ModelProvider() {

        private AtlassianTreeModel model = new AtlassianTreeModel(new FolderNode("/", AtlassianClickAction.EMPTY_ACTION));

        public AtlassianTreeModel getModel(final AtlassianTreeWithToolbar.State state) {
            return model;
        }

	};
}
