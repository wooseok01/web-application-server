package webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.HttpRequest;
import model.HttpResponse;
import model.User;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	private static final String WEB_BASE_DIR = "./webapp";
	private static final String INDEX_HTML = "/index.html";
	private static final String LOGIN_HTML = "/user/login.html";
	private static final String LOGIN_FAIL_HTML = "/user/login_failed.html";
	private static final String USER_LIST_HTML = "/user/list.html";
	private static final String T_BODY_TAG = "<tbody>";
	private static final String COOKIE_LOGIN_KEY = "logined";

	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
			connection.getPort());

		try (InputStream in = connection.getInputStream();
			OutputStream out = connection.getOutputStream();) {

			HttpRequest request = new HttpRequest(in);
			HttpResponse response = new HttpResponse(out);

			if (request.getPath() == null || "".equals(request.getPath())) {
				return;
			}

			// get과 post를 먼저 나눈다.
			if ("GET".equals(request.getMethod())) {
				doGet(request, response);

			} else if ("POST".equals(request.getMethod())) {
				doPost(request, response);
			}

		} catch (IOException e) {
			log.error(e.getMessage());
			return;
		}
	}

	private void doPost(HttpRequest request, HttpResponse response) throws IOException {
		if ("/user/create".equals(request.getPath())) {
			User user = new User(request.getHttpRequestParameters());
			DataBase.addUser(user);
			response.sendRedirect(INDEX_HTML);

		} else if ("/user/login".equals(request.getPath())) {
			User user = new User(request.getHttpRequestParameters());

			if (DataBase.isValidToLogin(user)) {
				response.responseWithCookie(true);
				response.sendRedirect(INDEX_HTML);
				return;
			}

			response.responseWithCookie(false);
			response.sendRedirect(LOGIN_FAIL_HTML);
		}
	}

	private void doGet(HttpRequest request, HttpResponse response) throws IOException {
		if ("/user/create".equals(request.getPath())) {
			DataBase.addUser(new User(request.getHttpRequestParameters()));
			response.sendRedirect(INDEX_HTML);

		} else if (request.getPath().endsWith(".html") || request.getPath().endsWith(".css")) {
			byte[] body = htmlParser(request.getPath());
			response.response200Header(body.length, getContentType(request.getPath()));
			response.responseBody(body);

		} else if ("/user/list".equals(request.getPath())) {
			if (isLogin(request.getHeader("Cookie"))) {
				String listHtmlContents = readFile(USER_LIST_HTML, makeUserListHtml());
				response.responseBody(listHtmlContents.getBytes());
				return;
			}

			response.sendRedirect(LOGIN_HTML);
		}
	}

	private boolean isLogin(String cookieString) {
		if (cookieString.contains(COOKIE_LOGIN_KEY)) {
			String cookieValue = cookieString.split("=")[1];
			return Boolean.parseBoolean(cookieValue);
		}

		return false;
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

	private byte[] htmlParser(String httpHeader) throws IOException {
		File htmlFile = new File(new File(WEB_BASE_DIR), httpHeader);
		return Files.readAllBytes(htmlFile.toPath());
	}
}
