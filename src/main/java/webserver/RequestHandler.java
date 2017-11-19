package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	private static final String WEB_BASE_DIR = "./webapp";
	private static final String INDEX_HTML = "/index.html";
	private static final String LOGIN_HTML = "/user/login.html";
	private static final String LOGIN_FAIL_HTML = "/user/login_failed.html";
	private static final String USER_LIST_HTML = "/user/list.html";
	private static final String T_BODY_TAG = "<tbody>";
	private static final int URL_PATH = 1;
	private static final int METHOD_TYPE = 0;
	private static final String COOKIE_LOGIN_KEY = "logined";

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

			// get과 post를 먼저 나눈다.
			if ("GET".equals(httpMethod)) {
				doGet(urlPath, bufferedReader, dos);
			} else if ("POST".equals(httpMethod)) {
				doPost(urlPath, bufferedReader, dos);
			}
		} catch (IOException e) {
			log.error(e.getMessage());
			return;
		}
	}

	private void doPost(String urlPath, BufferedReader bufferedReader, DataOutputStream dos) throws IOException {
		int contentLength = getContentLength(bufferedReader);
		String queryString = IOUtils.readData(bufferedReader, contentLength);

		if (urlPath.startsWith("/user/create")) {
			User user = new User(HttpRequestUtils.parseQueryString(queryString));
			DataBase.addUser(user);
			redirectUrl(dos, INDEX_HTML);

		} else if (urlPath.startsWith("/user/login")) {
			User user = new User(HttpRequestUtils.parseQueryString(queryString));

			if (DataBase.isValidToLogin(user)) {
				responseWithCookie(dos, true);
				redirectUrl(dos, INDEX_HTML);
				return;
			}

			responseWithCookie(dos, false);
			redirectUrl(dos, LOGIN_FAIL_HTML);
		}
	}

	private void doGet(String urlPath, BufferedReader bufferedReader, DataOutputStream dos) throws IOException {
		if (urlPath.startsWith("/user/create")) {
			DataBase.addUser(userParser(urlPath));
			redirectUrl(dos, INDEX_HTML);

		} else if (urlPath.endsWith(".html") || urlPath.endsWith(".css")) {
			byte[] body = htmlParser(urlPath);
			response200Header(dos, body.length, getContentType(urlPath));
			responseBody(dos, body);

		} else if (urlPath.startsWith("/user/list")) {
			if (loginCheck(bufferedReader)) {
				String listHtmlContents = readFile(USER_LIST_HTML, makeUserListHtml());
				responseBody(dos, listHtmlContents.getBytes());
				return;
			}

			redirectUrl(dos, LOGIN_HTML);
		}
	}

	private String getContentType(String urlPath) {
		if (urlPath.endsWith(".html")) {
			return "text/html";
		} else if (urlPath.endsWith(".css")) {
			return "text/css";
		}

		return "text/html";
	}

	private String readFile(String userListHtml, String appendString) throws IOException {
		File file = new File(new File(WEB_BASE_DIR), userListHtml);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		String fileString = bufferedReader.readLine();
		StringBuilder stringBuilder = new StringBuilder();

		while (fileString != null) {
			stringBuilder.append(fileString);
			if (fileString.equals(T_BODY_TAG)) {
				stringBuilder.append(appendString);
			}

			fileString = bufferedReader.readLine();
		}
		
		bufferedReader.close();

		return stringBuilder.toString();
	}

	private String makeUserListHtml() {
		StringBuilder stringBuilder = new StringBuilder();
		Iterator<User> users = DataBase.findAll().iterator();
		int index = 1;

		while (users.hasNext()) {
			User user = users.next();
			stringBuilder.append("<tr>");
			stringBuilder.append("<th>" + index + "</th>");
			stringBuilder.append("<td>" + user.getUserId() + "</td>");
			stringBuilder.append("<td>" + user.getName() + "</td>");
			stringBuilder.append("<td>" + user.getEmail() + "</td>");
			stringBuilder.append("</tr>");
		}

		return stringBuilder.toString();
	}

	private boolean loginCheck(BufferedReader bufferedReader) throws IOException {
		String line = bufferedReader.readLine();
		while (line != null && line.length() > 0) {
			if (line.startsWith("Cookie:")) {
				Map<String, String> cookie = HttpRequestUtils.parseCookies(line.split(" ")[1]);
				return Boolean.parseBoolean(cookie.get(COOKIE_LOGIN_KEY));
			}

			line = bufferedReader.readLine();
		}

		return false;
	}

	private void responseWithCookie(DataOutputStream dos, boolean logined) throws IOException {
		dos.writeBytes("HTTP/1.1 200 OK \r\n");
		dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
		dos.writeBytes("Set-Cookie: logined=" + Boolean.toString(logined) + "\r\n");
		dos.writeBytes("\r\n");
	}

	private void redirectUrl(DataOutputStream dos, String htmlPath) throws IOException {
		byte[] body = Files.readAllBytes(new File(WEB_BASE_DIR + htmlPath).toPath());
		response302Header(dos, body.length, htmlPath);
		responseBody(dos, htmlParser(htmlPath));
	}

	private int getContentLength(BufferedReader bufferedReader) throws IOException {
		String line = bufferedReader.readLine();
		int contentLength = 0;

		while (line != null && line.length() > 0) {
			if (line.startsWith("Content-Length")) {
				contentLength = Integer.parseInt(line.split(" ")[1]);
			}

			line = bufferedReader.readLine();
		}

		return contentLength;
	}

	private User userParser(String httpHeader) {
		httpHeader = httpHeader.replace("/user/create", "").replace("?", "");
		return new User(HttpRequestUtils.parseQueryString(httpHeader));
	}

	private byte[] htmlParser(String httpHeader) throws IOException {
		File htmlFile = new File(new File(WEB_BASE_DIR), httpHeader);
		return Files.readAllBytes(htmlFile.toPath());
	}

	private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType)
		throws IOException {
		dos.writeBytes("HTTP/1.1 200 OK \r\n");
		dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
		dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
		dos.writeBytes("\r\n");
	}

	private void response302Header(DataOutputStream dos, int lengthOfBodyContent, String redirectUrl)
		throws IOException {
		dos.writeBytes("HTTP/1.1 302 OK \r\n");
		dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
		//		dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
		dos.writeBytes("Location: " + "localhost:8080" + redirectUrl + "\r\n");
		dos.writeBytes("\r\n");
	}

	private void responseBody(DataOutputStream dos, byte[] body) throws IOException {
		dos.write(body, 0, body.length);
		dos.flush();
	}
}
