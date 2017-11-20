package controller;

import java.io.IOException;

import model.HttpRequest;
import model.HttpResponse;

/**
 * Created by wooseokSong on 2017-11-20.
 */
public interface Controller {
	void service(HttpRequest request, HttpResponse response) throws IOException;
}
