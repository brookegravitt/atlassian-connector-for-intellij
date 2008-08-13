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

package com.atlassian.theplugin.idea;

import java.lang.reflect.Field;

public abstract class PrivateFieldMapper {
	protected PrivateFieldMapper(Object original) throws Exception {
		for (Field f : getClass().getDeclaredFields()) {
			String name = f.getName();

			Field originalField;
			Class clazz = original.getClass();
			while (true) {
				try {
					originalField = clazz.getDeclaredField(name);
					originalField.setAccessible(true);
					break;
				} catch (NoSuchFieldException e) {
					clazz = clazz.getSuperclass();
					if (clazz == null) {
						throw e;
					}
				}
			}
			f.setAccessible(true);
			f.set(this, originalField.get(original));
		}
	}
}
