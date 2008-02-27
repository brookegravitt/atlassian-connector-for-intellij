package com.atlassian.theplugin.idea;

import java.lang.reflect.Field;

public abstract class PrivateFieldMapper {
	protected PrivateFieldMapper(Object original) throws Exception {
		for (Field f : getClass().getFields()) {
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
