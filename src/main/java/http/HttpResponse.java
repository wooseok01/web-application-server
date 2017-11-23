package http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpResponse {
	private DataOutputStream dos = null;
	private Map<String, String> map;

	public HttpResponse(OutputStream os) throws IOException {
		this.map = new HashMap<>();
		this.dos = new DataOutputStream(os);
	}

	public void responseBody(byte[] body) throws IOException {
		dos.write(body, 0, body.length);
		dos.flush();
	}

	public void response200Header(int lengthOfBodyContent, String contentType) throws IOException {
		dos.writeBytes("HTTP/1.1 200 OK \r\n");
		dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
		dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
		makeHeader();
		dos.writeBytes("\r\n");
	}

	public void sendRedirect(String redirectUrl) throws IOException {
		dos.writeBytes("HTTP/1.1 302 Found \r\n");
		makeHeader();
		dos.writeBytes("Location: " + redirectUrl + "\r\n");
		dos.writeBytes("\r\n");
	}

	public void setHeader(String key, String value) {
		map.put(key, value);
	}

	private void makeHeader() throws IOException {
		Set<String> keys = map.keySet();
		System.out.println(keys);
		for (String key : keys) {
			dos.writeBytes(key + ": " + map.get(key) + "\r\n");
		}
	}
}
