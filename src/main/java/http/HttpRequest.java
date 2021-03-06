package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {
	private static final int URL_PATH = 1;
	private static final int METHOD_TYPE = 0;
	private static final String PATH = "path";
	private static final String METHOD = "method";

	private Map<String, String> httpRequestHeader;
	private Map<String, String> httpRequestParameters;
	private String requestHeader;
	private HttpCookie httpCookie;

	private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

	public HttpRequest(InputStream is) {
		this.httpRequestHeader = new HashMap<>();
		this.httpRequestParameters = new HashMap<>();

		try {
			InputStreamReader in = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(in);

			requestHeader = br.readLine();
			setRequestHeader(br);
			this.httpCookie = new HttpCookie(httpRequestHeader.get("Cookie"));

			httpRequestHeader.put(METHOD, requestHeader.split(" ")[METHOD_TYPE]);
			String path = requestHeader.split(" ")[URL_PATH];

			if ("GET".equals(httpRequestHeader.get(METHOD))) {
				if (path.contains("?") == false) {
					httpRequestHeader.put(PATH, path);
					return;
				}

				httpRequestHeader.put(PATH, path.split("\\?")[0]);

				String queryString = path.split("\\?")[1];
				setParameters(queryString);

			} else if ("POST".equals(httpRequestHeader.get(METHOD))) {
				httpRequestHeader.put(PATH, path);
				setParameters(IOUtils.readData(br, Integer.parseInt(httpRequestHeader.get("Content-Length"))));
			}
		} catch (IOException e) {
			log.error("ioException!");
		}

	}

	public HttpCookie getCookies() {
		return this.httpCookie;
	}

	private void setParameters(String queryString) {
		this.httpRequestParameters = HttpRequestUtils.parseQueryString(queryString);
	}

	private void setRequestHeader(BufferedReader br) throws IOException {
		String line = br.readLine();
		while (line != null && line.length() > 0) {
			String key = line.split(": ")[0].replaceAll(":", "");
			httpRequestHeader.put(key, line.split(": ")[1]);

			line = br.readLine();
		}
	}

	public String getMethod() {
		return httpRequestHeader.get(METHOD);
	}

	public String getPath() {
		return httpRequestHeader.get(PATH);
	}

	public String getHeader(String headerInfo) {
		return httpRequestHeader.get(headerInfo);
	}

	public String getParameter(String parameter) {
		return httpRequestParameters.get(parameter);
	}

	public Map<String, String> getHttpRequestParameters() {
		return httpRequestParameters;
	}

	public HttpSession getSession() {
		return HttpSessions.getSession(getCookies().getCookie("JSESSIONID"));
	}
}
