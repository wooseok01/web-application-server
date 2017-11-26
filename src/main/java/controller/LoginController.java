package controller;

import java.io.IOException;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpSession;
import http.HttpSessions;
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
			HttpSession session = request.getSession();
			session.setAttribute("user", user);
//			response.setHeader("Set-Cookie", "logined=true; Path=/");
			response.sendRedirect(Constants.INDEX_HTML);
			return;
		}

//		response.setHeader("Set-Cookie","logined=false; Path=/");
		response.sendRedirect(Constants.LOGIN_FAIL_HTML);
	}
}
