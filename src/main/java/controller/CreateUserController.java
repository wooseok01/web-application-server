package controller;

import java.io.IOException;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import util.Constants;

/**
 * Created by wooseokSong on 2017-11-20.
 */
public class CreateUserController implements Controller {

	@Override
	public void service(HttpRequest request, HttpResponse response) throws IOException {
		DataBase.addUser(new User(request.getHttpRequestParameters()));
		response.sendRedirect(Constants.INDEX_HTML);
	}
}
