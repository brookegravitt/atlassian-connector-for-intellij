/**
 * Copyright 2006-2007, subject to LGPL version 3
 * User: garethc
 * Date: Mar 13, 2007
 * Time: 2:11:17 PM
 */
package org.veryquick.embweb;

import org.apache.log4j.Logger;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Ultra lightweight web server for embedding in applications
 * <p/>
 * Copyright 2006-2007, subject to LGPL version 3
 *
 * @author $Author:garethc$
 *         Last Modified: $Date:Mar 13, 2007$
 *         $Id: blah$
 */
public final class EmbeddedServer {

	/**
	 * Logger
	 */
	public static final Logger LOGGER = Logger.getLogger(EmbeddedServer.class);

	/**
	 * Port to serve clients on
	 */
	private int serverPort;

	/**
	 * Server lives as long as this is true
	 */
	private volatile boolean alive;


	/**
	 * Handler
	 */
	private HttpRequestHandler clientHandler;
	private boolean acceptOnlyLocal;

	/**
	 * New instance
	 *
	 * @param serverPort
	 * @param handler
	 * @param acceptOnlyLocal
	 * @return instance of http server
	 * @throws Exception
	 */
	public static EmbeddedServer createInstance(int serverPort, HttpRequestHandler handler, boolean acceptOnlyLocal)
			throws Exception {
		final EmbeddedServer server = new EmbeddedServer(serverPort, handler, acceptOnlyLocal);
		Thread thread = new Thread(
				new Runnable() {
					public void run() {
						try {
							server.start();
						} catch (IOException e) {
							LOGGER.warn("Failed to start server", e);
						}
					}
				}, "server thread"
		);
		thread.start();
		return server;
	}

	/**
	 * New instance
	 *
	 * @param serverPort
	 * @param handler
	 * @throws Exception
	 */
	private EmbeddedServer(int serverPort, HttpRequestHandler handler, boolean acceptOnlyLocal) throws Exception {
		this.serverPort = serverPort;
		this.clientHandler = handler;
		this.acceptOnlyLocal = acceptOnlyLocal;
	}

	/**
	 * Start the server
	 *
	 * @throws java.io.IOException
	 */
	private void start() throws IOException {
		this.alive = true;
		ServerSocket serverSocket;
		if (System.getProperty("javax.net.ssl.keyStore") != null) {
			ServerSocketFactory ssocketFactory = SSLServerSocketFactory.getDefault();
			serverSocket = ssocketFactory.createServerSocket(this.serverPort);
		} else {
			serverSocket = new ServerSocket(this.serverPort);
		}
		LOGGER.info("Server up on " + this.serverPort);
		while (alive) {
			Socket clientRequestSocket = serverSocket.accept();

			if (!acceptOnlyLocal || clientRequestSocket.getInetAddress().isLoopbackAddress()) {
				Thread thread = new Thread(
						new RequestHandler(clientRequestSocket),
						clientRequestSocket.getInetAddress().getCanonicalHostName()
				);
				thread.start();
			} else {
				clientRequestSocket.getOutputStream().close();
				clientRequestSocket.close();
			}
		}
	}

	/**
	 * Request handler
	 */
	private class RequestHandler implements Runnable {

		/**
		 * Socket to handle the request on
		 */
		private Socket clientRequestSocket;


		/**
		 * New handler
		 *
		 * @param clientRequestSocket Socket to handle the request on
		 */
		public RequestHandler(Socket clientRequestSocket) {
			this.clientRequestSocket = clientRequestSocket;
		}

		/**
		 * Handle the request
		 *
		 * @see Thread#run()
		 */
		public void run() {
			try {
				InputStream requestInputStream = clientRequestSocket.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(requestInputStream));
				String line = null;
				while ((line = reader.readLine()) != null) {
					LOGGER.debug(line);

					if (line.startsWith("GET") || line.startsWith("POST")) {
						StringTokenizer tokenizer = new StringTokenizer(line, " ");
						String requestType = tokenizer.nextToken();
						String url = tokenizer.nextToken();

						Map<String, String> parameters = new HashMap<String, String>();

						int indexOfQuestionMark = url.indexOf("?");
						if (indexOfQuestionMark >= 0) {
							// there are URL parameters
							String parametersToParse = url.substring(indexOfQuestionMark + 1);
							url = url.substring(0, indexOfQuestionMark);
							StringTokenizer parameterTokenizer = new StringTokenizer(parametersToParse, "&");
							while (parameterTokenizer.hasMoreTokens()) {
								String[] keyAndValue = parameterTokenizer.nextToken().split("=");
								String key = URLDecoder.decode(keyAndValue[0], "utf-8");
								String value = null;
								if (keyAndValue.length > 1) {
									value = URLDecoder.decode(keyAndValue[1], "utf-8");
								}
								parameters.put(key, value);
							}
						}

						HttpRequestHandler.Type type;
						if ("POST".equals(requestType)) {
							type = HttpRequestHandler.POST;
						} else {
							type = HttpRequestHandler.GET;
						}

						Response response = EmbeddedServer.this.clientHandler.handleRequest(
								type, url, parameters
						);
						OutputStream outputStream = clientRequestSocket.getOutputStream();
						response.writeToStream(outputStream);
						outputStream.close();
					}
				}
			} catch (SocketException e) {
				LOGGER.debug("Socket error", e);
			} catch (IOException e) {
				LOGGER.error("I/O Error", e);
			}
		}

	}

}
