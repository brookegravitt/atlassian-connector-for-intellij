package com.atlassian.theplugin.bamboo.api.bamboomock;

import static junit.framework.Assert.fail;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class Util {

	private static final String RESOURCE_BASE = "/mock/bamboo/1_2_4/api/rest/";


	private Util() {
	}

	static void copyResource(OutputStream outputStream, String resource) {
		BufferedInputStream is = new BufferedInputStream(Util.class.getResourceAsStream(RESOURCE_BASE + resource));
		int c;
		try {
			while ((c = is.read()) != -1) {
				outputStream.write(c);
			}
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

}
