package controller;

import java.io.IOException;

import db.DataBase;
import model.HttpRequest;
import model.HttpResponse;
import model.User;
import util.Constants;

/**
 * Created by wooseokSong on 2017-11-20.
 */
public class LoginController implements Controller {
	@Override
	public void service(HttpRequest request, HttpResponse response) throws IOException {
		User user = new User(request.getHttpRequestParameters());

		if (DataBase.isValidToLogin(user)) {
			response.responseWithCookie(true);
			response.sendRedirect(Constants.INDEX_HTML);
			return;
		}

		response.responseWithCookie(false);
		response.sendRedirect(Constants.LOGIN_FAIL_HTML);
	}
}
