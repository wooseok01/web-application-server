package http;

import java.util.Map;

import util.HttpRequestUtils;

/**
 * Created by wooseokSong on 2017-11-24.
 */
public class HttpCookie {
	private Map<String, String> cookies;

	public HttpCookie(String cookieValue) {
		cookies = HttpRequestUtils.parseCookies(cookieValue);
	}

	public String getCookie(String name) {
		return cookies.get(name);
	}

	public void setCookie(String name, String value) {
		cookies.put(name, value);
	}
}
