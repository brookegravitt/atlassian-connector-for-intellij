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
package com.atlassian.theplugin.idea.bamboo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GroupByAction extends AbstractBambooComboBoxAction {

	@Override
	protected void execute(@NotNull final BambooToolWindowPanel panel, @Nullable final Object selectedItem) {
		if (selectedItem instanceof BambooGroupingType) {
			BambooGroupingType bambooGroupingType = (BambooGroupingType) selectedItem;
			panel.setGroupingType(bambooGroupingType);
		}
	}

	@Override
	protected DefaultComboBoxModel createComboBoxModel() {
		return new DefaultComboBoxModel(BambooGroupingType.values());
	}
}

