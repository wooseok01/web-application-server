package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	private static final String WEB_BASE_DIR = System.getProperty("user.dir") + File.separator + "webapp";
	private static final String INDEX_HTML = "index.html";
	private static final String INDEX_JSP_PATH = WEB_BASE_DIR + File.separator + INDEX_HTML;
	private static final int URL_PATH = 1;
	private static final int METHOD_TYPE = 0;

	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
				connection.getPort());

		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

			InputStreamReader inputStreamReader = new InputStreamReader(in);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			String httpRequest = bufferedReader.readLine();
			String urlPath = httpRequest.split(" ")[URL_PATH];
			String httpMethod = httpRequest.split(" ")[METHOD_TYPE];

			DataOutputStream dos = new DataOutputStream(out);
			if (urlPath == null || "".equals(urlPath)) {
				return;
			}

			if (urlPath.startsWith("/user/create") && "GET".equals(httpMethod)) {
				DataBase.addUser(userParser(urlPath));
				redirectIndex(dos);
				return;
			}

			if (urlPath.endsWith(".html")) {
				byte[] body = htmlParser(urlPath);
				response200Header(dos, body.length);
				responseBody(dos, body);
				return;
			}

			if ("POST".equals(httpMethod) && urlPath.startsWith("/user/create")) {
				int contentLength = getContentLength(bufferedReader);
				String queryString = IOUtils.readData(bufferedReader, contentLength);

				User user = new User(HttpRequestUtils.parseQueryString(queryString));
				DataBase.addUser(user);
				redirectIndex(dos);
				return;
			}

		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void redirectIndex(DataOutputStream dos) throws IOException {
		byte[] body = Files.readAllBytes(new File(INDEX_JSP_PATH).toPath());
		response302Header(dos, body.length);
//		responseBody(dos, body);
	}

	private int getContentLength(BufferedReader bufferedReader) throws IOException {
		String line = "";
		while ((line = bufferedReader.readLine()).startsWith("Content-Length")) {
			return Integer.parseInt(line.split(" ")[1]);
		}

		return 0;
	}

	private User userParser(String httpHeader) {
		httpHeader = httpHeader.replace("/user/create", "").replace("?", "");
		return new User(HttpRequestUtils.parseQueryString(httpHeader));
	}

	private byte[] htmlParser(String httpHeader) throws IOException {
		String htmlFileName = httpHeader.replaceAll("/", File.separator);

		File htmlFile = new File(WEB_BASE_DIR + htmlFileName);
		return Files.readAllBytes(htmlFile.toPath());
	}

	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void response302Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 302 OK \r\n");
			dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("Location: " + "/" + INDEX_HTML + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
