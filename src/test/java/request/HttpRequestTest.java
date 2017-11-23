package request;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import http.HttpRequest;

public class HttpRequestTest {
	private static final String BASE_DIR = System.getProperty("user.dir");
	private static final String HTTP_GET_TXT = BASE_DIR + "/src/test/resources/Http_GET.txt";
	private static final String HTTP_POST_TXT = BASE_DIR + "/src/test/resources/Http_POST.txt";
	
	@Test
	public void request_GET() throws IOException {
		HttpRequest request = getHttpRequest(HTTP_GET_TXT);

		assertEquals("GET", request.getMethod());
		assertEquals("/user/create", request.getPath());
		assertEquals("keep-alive", request.getHeader("Connection"));
		assertEquals("javajigi", request.getParameter("userId"));
	}
	
	@Test
	public void request_POST() throws IOException {
		HttpRequest request = getHttpRequest(HTTP_POST_TXT);
		
		assertEquals("POST", request.getMethod());
		assertEquals("/user/create", request.getPath());
		assertEquals("keep-alive", request.getHeader("Connection"));
		assertEquals("javajigi", request.getParameter("userId"));
	}
	
	private HttpRequest getHttpRequest(String filePath) {
		File file = new File(filePath);
		try(InputStream is = new FileInputStream(file);){
			return new HttpRequest(is);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
