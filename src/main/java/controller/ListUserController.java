package controller;

import java.io.IOException;

import http.HttpRequest;
import http.HttpResponse;
import http.HttpSession;
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
		HttpSession httpSession = request.getSession();
		if (httpSession.getAttribute("user") == null) {
			return false;
		}

		return true;
	}
}
