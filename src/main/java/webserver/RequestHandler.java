package webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.HttpRequest;
import model.HttpResponse;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
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
			 OutputStream out = connection.getOutputStream()) {

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

		} else if (isStaticFile(request.getPath())) {
			byte[] body = IOUtils.htmlParser(request.getPath());
			response.response200Header(body.length, HttpRequestUtils.getContentType(request.getPath()));
			response.responseBody(body);

		} else if ("/user/list".equals(request.getPath())) {
			if (isLogin(request.getHeader("Cookie"))) {
				String listHtmlContents = IOUtils.appendUserList(USER_LIST_HTML, IOUtils.makeUserListHtml(), T_BODY_TAG);
				response.responseBody(listHtmlContents.getBytes());
				return;
			}

			response.sendRedirect(LOGIN_HTML);
		}
	}

	private boolean isStaticFile(String path) {
		return path.endsWith(".html") || path.endsWith(".css") || path.endsWith(".js");
	}

	private boolean isLogin(String cookieString) {
		if (cookieString.contains(COOKIE_LOGIN_KEY)) {
			String cookieValue = cookieString.split("=")[1];
			return Boolean.parseBoolean(cookieValue);
		}

		return false;
	}
}
