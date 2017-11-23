package webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controller.Controller;
import http.HttpRequest;
import http.HttpResponse;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
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

			if (request.getCookies().getCookie("JSESSIONID") == null) {
				response.setHeader("Set-Cookie", "JSESSIONID=" + UUID.randomUUID());
			}

			if (request.getPath() == null || "".equals(request.getPath())) {
				return;
			}

			Controller controller = RequestMapping.getController(request.getPath());
			if (controller != null) {
				controller.service(request, response);
				return;
			}

			if (isStaticFile(request.getPath())) {
				byte[] body = IOUtils.htmlParser(request.getPath());
				response.response200Header(body.length, HttpRequestUtils.getContentType(request.getPath()));
				response.responseBody(body);
			}

		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private boolean isStaticFile(String path) {
		return path.endsWith(".html") || path.endsWith(".css") || path.endsWith(".js");
	}
}
