package com.atlassian.theplugin.idea;

import java.lang.reflect.Field;

public abstract class PrivateFieldMapper {
	protected PrivateFieldMapper(Object original) throws Exception {
		for (Field f : getClass().getFields()) {
			String name = f.getName();
			Field originalField = original.getClass().getDeclaredField(name);
			originalField.setAccessible(true);
			f.setAccessible(true);
			f.set(this, originalField.get(original));
		}
	}
}
