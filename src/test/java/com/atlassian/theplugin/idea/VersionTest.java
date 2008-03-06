package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.util.InfoServer;
import com.atlassian.theplugin.exception.IncorrectVersionException;
import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Mar 3, 2008
 * Time: 2:08:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class VersionTest extends TestCase {
	private InfoServer.Version version1;
	private InfoServer.Version version2;
	private InfoServer.Version version3;
	private InfoServer.Version version4;
	private InfoServer.Version version5;
	private InfoServer.Version version6;
	private InfoServer.Version version7;
	private InfoServer.Version versionSpecial;

	@Override
	protected void setUp() throws Exception {
		super.setUp();	//To change body of overridden methods use File | Settings | File Templates.
		version1 = new InfoServer.Version("0.1.1-SNAPSHOT, SVN:111");
		version2 = new InfoServer.Version("0.1.1-SNAPSHOT, SVN:111");
		version3 = new InfoServer.Version("0.17.1, SVN:222");
		version4 = new InfoServer.Version("0.17.1-SNAPSHOT, SVN:223");
		version5 = new InfoServer.Version("0.17.1-SNAPSHOT, SVN:224");
		version6 = new InfoServer.Version("0.18.0-SNAPSHOT, SVN:224");
		version7 = new InfoServer.Version("0.14.0-SNAPSHOT, SVN:999");
		versionSpecial = new InfoServer.Version(InfoServer.Version.SPECIAL_DEV_VERSION);
	}

	public void testEquals() {
		assertEquals(version1, version1);
		assertEquals(version1, version2);
	}

	public void testGreater() {
		assertFalse(version1.greater(version1));
		assertFalse(version2.greater(version2));

		assertTrue(version3.greater(version1));
		assertTrue(version4.greater(version3));
		assertTrue(version5.greater(version4));
		assertTrue(version5.greater(version3));
		assertTrue(version6.greater(version3)); // not realistic
		assertTrue(version6.greater(version7));


		assertFalse(version1.greater(version3));
		assertFalse(version3.greater(version4));
		assertFalse(version4.greater(version5));
		assertFalse(version3.greater(version5));
		assertFalse(version3.greater(version6)); // not realistic
		assertFalse(version7.greater(version6));
	}

	public void testExceptions() {
		String[] invalidArguments = {
				", SVN:222",
				"0.1., SVN:222",
				"0.1.1-ALPHA, SVN:222",
				"0.1, SVN:222",
				"0.1-BETA, SVN:222",
				"0.1, SVN:",
				"0.1, SVN:12a",
				"0.1.1.2, SVN:12",
		};
		for (String arg : invalidArguments) {
			try {
				InfoServer.Version v = new InfoServer.Version(arg);
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
