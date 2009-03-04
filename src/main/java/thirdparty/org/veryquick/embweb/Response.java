/**
 * Copyright 2006-2007, subject to LGPL version 3
 * User: garethc
 * Date: Apr 10, 2007
 * Time: 1:24:25 PM
 */
package org.veryquick.embweb;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Response container for the ultra light-weight web server
 * <p/>
 * Copyright 2006-2007, subject to LGPL version 3
 *
 * @author $Author:garethc$
 *         Last Modified: $Date:Apr 10, 2007$
 *         $Id: blah$
 */
public class Response {

	/**
	 * Logger
	 */
	public static final Logger LOGGER = Logger.getLogger(Response.class);

	/**
	 * The headers
	 */
	private StringBuilder headers;

	/**
	 * The body
	 */
	private StringBuilder body;

	/**
	 * Content type, defaulting to text/html
	 */
	private String contentType;

	/**
	 * Binary body
	 */
	private byte[] bytes;


	/**
	 * Create new response
	 */
	public Response() {
		this.headers = new StringBuilder();
		this.body = new StringBuilder();
		this.contentType = "text/html; charset=utf-8";
	}

	/**
	 * Set a redirect
	 *
	 * @param url url to redirect to
	 */
	public void setRedirect(String url) {
		headers.append("HTTP/1.1 307 Temporary Redirect\n");
		headers.append("Location: /\n");
	}

	/**
	 * Append the given string to the body so far
	 *
	 * @param content content
	 */
	public void addContent(String content) {
		this.body.append(content);
	}

	/**
	 * Mark the response as 200 OK. This also sets the content length, so the method
	 * should not be called until all content has been appended
	 */
	public void setOk() {
		headers.append("HTTP/1.1 200 OK\n");
		headers.append("Content-Type: ");
		headers.append(this.contentType);
		headers.append("\n");
		try {
			headers.append("Content-Length: ");
			if (this.bytes != null) {
				headers.append(bytes.length);
			} else {
				headers.append(body.toString().getBytes("utf-8").length);
			}
			headers.append("\n");
		}
		catch (UnsupportedEncodingException e) {
			LOGGER.error("failed to encode", e);
		}
	}

	/**
	 * Write the complete response to the given output stream
	 *
	 * @param outputStream stream
	 * @throws IOException on error
	 */
	public void writeToStream(OutputStream outputStream) throws IOException {
		headers.append("\n");
		outputStream.write(headers.toString().getBytes("iso-8859-1"));
		if (this.bytes != null) {
			outputStream.write(this.bytes);
		} else {
			outputStream.write(body.toString().getBytes("iso-8859-1"));
		}
	}

	/**
	 * Set a 500 error condition
	 *
	 * @param e the exception
	 */
	public void setError(Exception e) {
		headers.append("HTTP/1.1 500 ");
		headers.append(e.getMessage());
		headers.append("\n");
		body.append("<html><body><h1><p>500 Internal server error</p></h1>");
		body.append(e.getMessage());
		body.append("</body></html>");
	}

	/**
	 * Set a 404 not found
	 *
	 * @param url url
	 */
	public void setNotFound(String url) {
		headers.append("HTTP/1.1 404 Resource not found\n");
		body.append("<html><body><h1><p>404 Resource not found</p></h1>");
		body.append(url);
		body.append("</body></html>");
	}

	/**
	 * Set the content type (defaults to text/html utf-8)
	 *
	 * @param contentType
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Set binary content
	 *
	 * @param aBytes
	 */
	public void setBinaryContent(byte[] aBytes) {
		this.bytes = aBytes;
	}

	public void setNoContent() {
		headers.append("HTTP/1.1 204 No content\n");
	}
}
