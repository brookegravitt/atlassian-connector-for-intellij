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

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public final class StringUtil {
	private static final int BUFFER_SIZE = 4096;

	private StringUtil() {
		// this is utility class
	}

	public static synchronized String decode(String str2decode) {
		try {
			Base64 base64 = new Base64();
			byte[] passwordBytes = base64.decode(str2decode.getBytes("UTF-8"));
			if (passwordBytes == null || passwordBytes.length == 0) {
				throw new IllegalArgumentException("Cannot decode string due to not supported "
						+ "characters or becuase it is not encoded");
			}

			return new String(passwordBytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			///CLOVER:OFF
			// cannot happen
			throw new RuntimeException("UTF-8 is not supported", e);
			///CLOVER:ON
		}
	}

	public static synchronized String encode(String str2encode) {
		try {
			Base64 base64 = new Base64();
			byte[] bytes = base64.encode(str2encode.getBytes("UTF-8"));
			return new String(bytes);
		} catch (UnsupportedEncodingException e) {
			///CLOVER:OFF
			// cannot happen
			throw new RuntimeException("UTF-8 is not supported", e);
			///CLOVER:ON
		}
	}

	public static String slurp(InputStream in) throws IOException {
		StringBuilder out = new StringBuilder();
		byte[] b = new byte[BUFFER_SIZE];
		for (int n = in.read(b); n != -1; n = in.read(b)) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}
}
