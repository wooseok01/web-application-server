package model;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class HttpResponse {
	private static final String WEB_BASE_DIR = "./webapp";

	private DataOutputStream dos = null;

	public HttpResponse(OutputStream os) throws IOException {
		this.dos = new DataOutputStream(os);
	}

	public void responseBody(byte[] body) throws IOException {
		dos.write(body, 0, body.length);
		dos.flush();
	}

	public void response302Header(String redirectUrl) throws IOException {
		dos.writeBytes("HTTP/1.1 302 OK \r\n");
		dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
		dos.writeBytes("Location: " + "localhost:8080" + redirectUrl + "\r\n");
		dos.writeBytes("\r\n");
	}

	public void response200Header(int lengthOfBodyContent, String contentType) throws IOException {
		dos.writeBytes("HTTP/1.1 200 OK \r\n");
		dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
		dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
		dos.writeBytes("\r\n");
	}

	public void responseWithCookie(boolean isLogin) throws IOException {
		dos.writeBytes("HTTP/1.1 200 OK \r\n");
		dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
		dos.writeBytes("Set-Cookie: logined=" + Boolean.toString(isLogin) + "\r\n");
		dos.writeBytes("\r\n");
	}

	public void sendRedirect(String htmlPath) throws IOException {
		byte[] body = Files.readAllBytes(new File(WEB_BASE_DIR + htmlPath).toPath());
		response302Header(htmlPath);
		responseBody(body);
	}
}
