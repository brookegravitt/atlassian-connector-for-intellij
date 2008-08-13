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

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

import com.spartez.util.junit3.TestUtil;
import com.spartez.util.junit3.IAction;

/**
 * StringUtil Tester.
 *
 * @author wseliga
 */
public class StringUtilTest extends TestCase {

    public void testEncodeDecode() {
		String test = "my-test-string";
		assertEquals(test, StringUtil.decode(StringUtil.encode(test)));
	}

	public void testEncodeNull() {
		TestUtil.assertThrows(NullPointerException.class, new IAction() {
			public void run() throws Throwable {
				StringUtil.encode(null);
			}
		});
	}

	public void testDecodeNull() {
		TestUtil.assertThrows(NullPointerException.class, new IAction() {
			public void run() throws Throwable {
				StringUtil.decode(null);
			}
		});
	}

	public void testDecodeUndecoded() {
		TestUtil.assertThrows(IllegalArgumentException.class, new IAction() {
			public void run() throws Throwable {
				StringUtil.decode(new String(new byte[] {1, 0, 2}));
			}
		});
	}


	public void testSlurp() throws IOException {
		String test = "fdsf%$%W$OAnotherString\nfdasfds__5w349584";
		InputStream in = new ByteArrayInputStream(test.getBytes());
		assertEquals(test, StringUtil.slurp(in));
		TestUtil.assertThrows(NullPointerException.class, new IAction() {
			public void run() throws Throwable {
				StringUtil.slurp(null);
			}
		});
	}


}
