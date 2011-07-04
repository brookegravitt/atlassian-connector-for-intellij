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

import com.atlassian.theplugin.commons.exception.IncorrectVersionException;
import com.atlassian.theplugin.commons.util.Version;
import junit.framework.TestCase;

/**
 * @author lguminski
 */
public class VersionTest extends TestCase {
	private Version version1;
	private Version version2;
	private Version version3;
	private Version version4;
	private Version version5;
	private Version version6;
	private Version version7;
	private Version versionSpecial;
	private Version versionAlpha;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		version1 = new Version("0.1.1-SNAPSHOT, SVN:111");
		version2 = new Version("0.1.1-SNAPSHOT, SVN:111");
		version4 = new Version("0.17.1-SNAPSHOT, SVN:223");
		version5 = new Version("0.17.1-SNAPSHOT, SVN:224");
		version3 = new Version("0.17.1, SVN:225");
		version6 = new Version("0.18.0-SNAPSHOT, SVN:224");
		version7 = new Version("0.14.0-SNAPSHOT, SVN:999");
		versionAlpha = new Version("3.15.4-ALPHA, SVN:999");
		versionSpecial = new Version(Version.SPECIAL_DEV_VERSION);
	}

	public void testNumberedBetaVersion() throws IncorrectVersionException {
		// we must assume that betas will have higher SVN revision nr than alphas :P
		final String versionStr = "3.15.4-beta-2, SVN:1000";
		final Version versionBeta2 = new Version(versionStr);
		assertEquals(versionStr, versionBeta2.getVersion());
		assertTrue(versionBeta2.greater(versionAlpha));
		assertFalse(versionAlpha.greater(versionBeta2));
		assertFalse(versionBeta2.greater(new Version("3.15.4-beta-3, SVN:1001")));

	}

	public void testEquals() {
		assertEquals(version1, version1);
		assertEquals(version1, version2);
	}

	public void testGreater() {
		assertFalse(version1.greater(version1));
		assertFalse(version2.greater(version2));

		assertTrue(version3.greater(version1));
		assertTrue(version3.greater(version4));
		assertTrue(version5.greater(version4));
		assertTrue(version3.greater(version5));
		assertTrue(version6.greater(version3)); // not realistic
		assertTrue(version6.greater(version7));


		assertFalse(version1.greater(version3));
		assertFalse(version4.greater(version3));
		assertFalse(version4.greater(version5));
		assertFalse(version5.greater(version3));
		assertFalse(version3.greater(version6)); // not realistic
		assertFalse(version7.greater(version6));

		assertTrue(versionSpecial.greater(versionAlpha));
	}

	public void testExceptions() {
		String[] invalidArguments = {
				", SVN:222",
				"0.1., SVN:222",
				"0.1.-ALPHA, SVN:222",
				"0.1, SVN:222",
				"0.1-BETA, SVN:222",
				"0.1, SVN:",
				"0.1, SVN:12a",
				"0.1.1.2, SVN:12",
		};
		for (String arg : invalidArguments) {
			try {
				new Version(arg);
				fail("Creation succeeded although it should fail (\"" + arg + "\")");
			} catch (IncorrectVersionException e) {
				// OK
			}
		}
		// all valid are actually testes during creation of this TestCase
	}

	/**
	 * We are testing here a special case, when plugin version is not resolved by maven. This
	 * happens during development in IDEA. We don't want to be bothered by false notifications
	 * on availability of newer versions 
	 */
	public void testSpecialCase() {
		assertTrue(versionSpecial.greater(version1));
		assertTrue(versionSpecial.greater(version2));
		assertTrue(versionSpecial.greater(version3));
		assertTrue(versionSpecial.greater(version4));
		assertTrue(versionSpecial.greater(version5));
		assertTrue(versionSpecial.greater(version6));
		assertTrue(versionSpecial.greater(version7));

		assertFalse(version1.greater(versionSpecial));
		assertFalse(version2.greater(versionSpecial));
		assertFalse(version3.greater(versionSpecial));
		assertFalse(version4.greater(versionSpecial));
		assertFalse(version5.greater(versionSpecial));
		assertFalse(version6.greater(versionSpecial));
		assertFalse(version7.greater(versionSpecial));
		assertFalse(versionSpecial.greater(versionSpecial));
	}

}
