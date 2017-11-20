package controller;

import java.io.IOException;

import model.HttpRequest;
import model.HttpResponse;
import util.Constants;
import util.IOUtils;

/**
 * Created by wooseokSong on 2017-11-20.
 */
public class ListUserController implements Controller {

	@Override
	public void service(HttpRequest request, HttpResponse response) throws IOException {
		if (isLogin(request.getHeader("Cookie"))) {
			String listHtmlContents = IOUtils.appendUserList(Constants.USER_LIST_HTML, IOUtils.makeUserListHtml(),
				Constants.T_BODY_TAG);
			response.responseBody(listHtmlContents.getBytes());
			return;
		}

		response.sendRedirect(Constants.LOGIN_HTML);
	}

	private boolean isLogin(String cookieString) {
		if (cookieString.contains(Constants.COOKIE_LOGIN_KEY)) {
			String cookieValue = cookieString.split("=")[1];
			return Boolean.parseBoolean(cookieValue);
		}

		return false;
	}
}
