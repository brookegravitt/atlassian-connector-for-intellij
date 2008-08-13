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
package com.atlassian.theplugin.commons.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MiscUtil {

	private MiscUtil() {
		// this is utility class
	}

	public static <K, V> HashMap<K, V> buildHashMap() {
        return new HashMap<K, V>();
    }

	public static <K, V> AbstractMap<K, V> buildConcurrentHashMap(int initialCapacity) {
		return new ConcurrentHashMap<K, V>(initialCapacity);
	}

	public static <T> boolean isModified(T a, T b) {
        return a != null ? !a.equals(b) : b != null;
    }

    public static <T> boolean isEqual(T a, T b) {
        return a != null ? a.equals(b) : b == null;
    }


    public static <T> Set<T> buildHashSet() {
        return new HashSet<T>();
    }

    public static <T> ArrayList<T> buildArrayList() {
        return new ArrayList<T>();
    }

	public static <T> ArrayList<T> buildArrayList(int initialCapacity) {
		return new ArrayList<T>(initialCapacity);
	}

	/**
     * Creates mutable {@link java.util.ArrayList} from given elements.
     * 
     * @param elements elements which will be included in the newly created ArrayList
     * @return newly created ArrayList
     */
    public static <T> ArrayList<T> buildArrayList(T ... elements) {
        final ArrayList<T> tmp = new ArrayList<T>();
        tmp.addAll(Arrays.asList(elements));
        return tmp;
    }

	public static <T> ArrayList<T> buildArrayList(Collection<T> collection) {
		return new ArrayList<T>(collection);
	}
}
