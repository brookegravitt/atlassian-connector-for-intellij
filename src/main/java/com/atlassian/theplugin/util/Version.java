package com.atlassian.theplugin.util;

import com.atlassian.theplugin.exception.IncorrectVersionException;

import java.io.Serializable;
import java.util.Scanner;
import java.util.regex.MatchResult;

/**
 * Class encapsulating version descriptor of a form e.g. "0.3.0, SVN:14021"
 */
public class Version implements Serializable {
	public static final String SPECIAL_DEV_VERSION = "${project.version}, SVN:${buildNumber}";
	public static final Version NULL_VERSION = initNullVersion();
	private static final long serialVersionUID = 1846608052207718100L;

	private static Version initNullVersion() {
		Version result = null;
		try {
			result = new Version("0.0.0, SVN:0");
		} catch (IncorrectVersionException e) {
			PluginUtil.getLogger().error("God does not exist. Impossible has happened.", e);
		}
		return result;
	}

	private transient VersionNumber versionNumber;
	private transient Integer buildNo;

	private String version = null;

	public Version(String version) throws IncorrectVersionException {
		super();
		setVersion(version);
	}

	public Version() throws IncorrectVersionException {
		this(NULL_VERSION.getVersion());
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) throws IncorrectVersionException {
		this.version = version;
		if (!version.equals(SPECIAL_DEV_VERSION)) {
			tokenize();
		}
	}


	private static final String PATTERN = "^(\\d+)\\.(\\d+)\\.(\\d+)((-(SNAPSHOT))?+), SVN:(\\d+)$";
	//private static final String PATTERN = "^(\\d+)\\.(\\d+)\\.(\\d+)((-(ALPHA|BETA|SNAPSHOT))?+), SVN:(\\d+)$";
	private static final int MAJOR_TOKEN_GRP = 1;
	private static final int MINOR_TOKEN_GRP = 2;
	private static final int MICRO_TOKEN_GRP = 3;
	private static final int ALPHANUM_TOKEN_GRP = 6;
	private static final int BUILD_TOKEN_GRP = 7;

	private void tokenize() throws IncorrectVersionException {
		Scanner s = new Scanner(version);
		s.findInLine(PATTERN);
		try {
			MatchResult result = s.match();
			versionNumber = new VersionNumber(
					Integer.valueOf(result.group(MAJOR_TOKEN_GRP)),
					Integer.valueOf(result.group(MINOR_TOKEN_GRP)),
					Integer.valueOf(result.group(MICRO_TOKEN_GRP)),
					result.group(ALPHANUM_TOKEN_GRP)
			);
			buildNo = Integer.valueOf(result.group(BUILD_TOKEN_GRP));
		} catch (IllegalStateException ex) {
			throw new IncorrectVersionException("Version (" + version + ") does not match pattern (\"" + PATTERN
					+ "\")", ex);
		}
	}

	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}
		if (that == null || getClass() != that.getClass()) {
			return false;
		}

		Version thatVersion = (Version) that;

		if (version != null ? !version.equals(thatVersion.version) : thatVersion.version != null) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		return (version != null ? version.hashCode() : 0);
	}

	@Override
	public String toString() {
		return version;
	}

	public Integer getBuildNo() {
		return buildNo;
	}

	public boolean greater(Version other) {
		if (other.version.equals(SPECIAL_DEV_VERSION)) {
			return false;
		}
		if (version.equals(SPECIAL_DEV_VERSION)) {
			return true;
		}
		if (this.getVersionNumber().equals(other.getVersionNumber())) {
			return getBuildNo() > other.getBuildNo();
		}
		return this.getVersionNumber().greater(other.getVersionNumber());
	}

	private VersionNumber getVersionNumber() {
		return versionNumber;
	}

	private static class VersionNumber {

		private int major;
		private int minor;
		private int micro;
		private AlphaNum alphaNum;
		private static final int PRIME = 31;


		public enum AlphaNum {
			ALPHA, BETA, SNAPSHOT, NONE;
		}

		public VersionNumber(int major, int minor, int micro, String alphaNum) throws IncorrectVersionException {
			this.major = major;

			this.minor = minor;
			this.micro = micro;
			if (alphaNum == null) {
				this.alphaNum = AlphaNum.NONE;
			} else {
				try {
					this.alphaNum = AlphaNum.valueOf(alphaNum);
				} catch (IllegalArgumentException ex) {
					throw new IncorrectVersionException("Unknown version alphanum: " + alphaNum);
				}
			}
		}

		public boolean greater(VersionNumber other) {
			if (major > other.major) {
				return true;
			} else {
				if (major == other.major && minor > other.minor) {
					return true;
				} else {
					if (major == other.major && minor == other.minor && micro > other.micro) {
						return true;
					} else {
						if (major == other.major && minor == other.minor && micro == other.micro) {
							return alphaNum.ordinal() > other.alphaNum.ordinal();
						}
					}
				}
			}
			return false;
		}

		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			VersionNumber that = (VersionNumber) o;

			if (major != that.major) {
				return false;
			}
			if (micro != that.micro) {
				return false;
			}
			if (minor != that.minor) {
				return false;
			}
			if (alphaNum != that.alphaNum) {
				return false;
			}

			return true;
		}

		public int hashCode() {
			int result;
			result = major;
			result = PRIME * result + minor;
			result = PRIME * result + micro;
			result = PRIME * result + (alphaNum != null ? alphaNum.hashCode() : 0);
			return result;
		}

	}

}

