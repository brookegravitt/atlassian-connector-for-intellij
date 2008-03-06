package com.atlassian.theplugin.util;

import com.atlassian.theplugin.exception.VersionServiceException;
import com.atlassian.theplugin.exception.IncorrectVersionException;
import com.atlassian.theplugin.util.PluginUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.MatchResult;

public class InfoServer {
	private String serviceUrl;
	private long uid;

	public InfoServer(String serviceUrl, long uid) {
		this.serviceUrl = serviceUrl;
		this.uid = uid;
	}

	public VersionInfo getLatestPluginVersion() throws VersionServiceException {
		try {
			HttpClient client = new HttpClient();
			GetMethod method = new GetMethod(serviceUrl + "?uid=" + uid);
			try {
				client.executeMethod(method);
			} catch (IllegalArgumentException e) {
				throw new VersionServiceException("Connection error while retriving the latest plugin version", e);
			}
			InputStream is = method.getResponseBodyAsStream();
			SAXBuilder builder = new SAXBuilder();
			builder.setValidation(false);
			Document doc = builder.build(is);
			return new VersionInfo(doc, VersionInfo.Type.STABLE);
		} catch (IOException e) {
			throw new VersionServiceException("Connection error while retriving the latest plugin version", e);
		} catch (JDOMException e) {
			throw new VersionServiceException(
					"Error while parsing xml response from version service server at "
					+ serviceUrl
					+ "?uid="
					+ uid, e);
		}
	}


	public static class VersionInfo {
		public enum Type {
			STABLE,
			UNSTABLE
		}
		private Document doc;
		private Type type;
		private Version version;
		private String downloadUrl;

		public VersionInfo(Document doc, VersionInfo.Type type) {
			this.doc = doc;
			this.type = type;
		}

		public VersionInfo(Version version, String downloadUrl) {
			this.version = version;
			this.downloadUrl = downloadUrl;
		}

		public VersionInfo(String version, String downloadUrl) throws IncorrectVersionException {
			this(new Version(version), downloadUrl);
		}

		public Version getVersion() throws VersionServiceException, IncorrectVersionException {
			String path = "";
			if (version == null) {
				switch (type) {
					case STABLE:
						path = "/response/versions/stable/latestVersion";
						break;
					case UNSTABLE:
						path = "/response/versions/unstable/latestVersion";
						break;
					default:
						throw new VersionServiceException("neither stable nor unstable");
				}
				version = new Version(getValue(path));
			}
			return version;
		}

		private String getValue(String path) throws VersionServiceException {
			XPath xpath;
			Element element;
			try {
				xpath = XPath.newInstance(path);
				element = (Element) xpath.selectSingleNode(doc);
				if (element == null) {
					throw new VersionServiceException("Error while parsing " + PluginUtil.VERSION_INFO_URL);
				}
			} catch (JDOMException e) {
				throw new VersionServiceException("Error while parsing " + PluginUtil.VERSION_INFO_URL, e);
			}
			return element.getValue();
		}

		public String getDownloadUrl() throws VersionServiceException {
			String path = "";
			if (downloadUrl == null) {
				switch (type) {
					case STABLE:
						path = "/response/versions/stable/downloadUrl";
						break;
					case UNSTABLE:
						path = "/response/versions/unstable/downloadUrl";
						break;
					default:
						throw new VersionServiceException("neither stable nor unstable");
				}
				downloadUrl = getValue(path);
			}
			return downloadUrl;
		}
	}

	/**
	 * Class encapsulating version descriptor of a form e.g. "0.3.0, SVN:14021"
	 */
	public static class Version {
		public static final String SPECIAL_DEV_VERSION = "${project.version}, SVN:${buildNumber}";

		private static class VersionNumber {

			private int major;
			private int minor;
			private int micro;
			private AlphaNum alphaNum;
			private static final int PRIME = 31;


			public enum AlphaNum {
				NONE, ALPHA, BETA, SNAPSHOT;
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

		private VersionNumber versionNumber;
		private Integer buildNo;
		private String version;

		public Version(String version) throws IncorrectVersionException {
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

				try {
					buildNo = Integer.valueOf(result.group(BUILD_TOKEN_GRP));
				} catch (NumberFormatException ex) {
					throw new IncorrectVersionException("Invalid build number: \"" + result.group(BUILD_TOKEN_GRP)
							+ "\"", ex);
				}
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
	}
}
