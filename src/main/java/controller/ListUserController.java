package controller;

import java.io.IOException;

import http.HttpCookie;
import http.HttpRequest;
import http.HttpResponse;
import util.Constants;
import util.IOUtils;

/**
 * Created by wooseokSong on 2017-11-20.
 */
public class ListUserController implements Controller {

	@Override
	public void service(HttpRequest request, HttpResponse response) throws IOException {
		if (isLogin(request)) {
			String listHtmlContents = IOUtils.appendUserList(Constants.USER_LIST_HTML, IOUtils.makeUserListHtml(),
				Constants.T_BODY_TAG);
			response.response200Header(listHtmlContents.getBytes().length, "text/html");
			response.responseBody(listHtmlContents.getBytes());
			return;
		}

		response.sendRedirect(Constants.LOGIN_HTML);
	}

	private boolean isLogin(HttpRequest request) {
		HttpCookie httpCookie = request.getCookies();
		String cookieValue = httpCookie.getCookie(Constants.COOKIE_LOGIN_KEY);
		if (cookieValue != null) {
			return Boolean.parseBoolean(cookieValue);
		}

		return false;
	}
}
