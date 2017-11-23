package http;

import java.util.Map;

import util.HttpRequestUtils;

/**
 * Created by wooseokSong on 2017-11-24.
 */
public class HttpCookieImpl implements HttpCookie {
	private Map<String, String> cookies;

	public HttpCookieImpl(String cookieValue) {
		cookies = HttpRequestUtils.parseCookies(cookieValue);
	}

	@Override
	public String getCookie(String name) {
		return cookies.get(name);
	}
}
