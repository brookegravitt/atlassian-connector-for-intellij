package com.atlassian.theplugin.idea.fisheye;

public class SourceCodeLinkParser {
	private String url;
	private String path = null;
	private String revision = null;
	private int line;

	public SourceCodeLinkParser(String url) {
		this.url = url;
	}

	public void parse() {
		String[] tokens = url.split("\\?");
		switch (tokens.length) {
			case 1:
				parsePath(tokens[0]);
				break;
			case 2:
				parsePath(tokens[0]);
				String[] locationTokens = tokens[1].split("#");
				switch (locationTokens.length) {
					case 1:
						parseRevision(locationTokens[0]);
						break;
					case 2:
						parseRevision(locationTokens[0]);
						String lineStr = locationTokens[1].substring(1);
						try {
							line = Integer.parseInt(lineStr) - 1;
						} catch (Exception ex) {
							line = 0;
						}
						break;
                    default:
                        // url has to be really ugly
                        break;
                }
				break;
            default:
                // url has to be really ugly
                break;
        }
	}

	private void parsePath(final String token) {
		path = token;
		if ("".equals(path)) {
			path = null;
		}
	}

	private void parseRevision(final String r) {
		String[] revTokens = r.split("=");
		if (revTokens.length == 2) {
			revision = revTokens[1];
		}
	}

	public String getPath() {
		return path;
	}

	public String getRevision() {
		return revision;
	}

	public int getLine() {
		return line;
	}
}
