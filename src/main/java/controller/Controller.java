package controller;

import java.io.IOException;

import http.HttpRequest;
import http.HttpResponse;

/**
 * Created by wooseokSong on 2017-11-20.
 */
public interface Controller {
	void service(HttpRequest request, HttpResponse response) throws IOException;
}
