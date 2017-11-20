package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;

import db.DataBase;
import model.User;

public class IOUtils {
	private static final String WEB_BASE_DIR = "./webapp";

	/**
	 * @param BufferedReader는
	 *            Request Body를 시작하는 시점이어야
	 * @param contentLength는
	 *            Request Header의 Content-Length 값이다.
	 * @return
	 * @throws IOException
	 */
	public static String readData(BufferedReader br, int contentLength) throws IOException {
		char[] body = new char[contentLength];
		br.read(body, 0, contentLength);
		return String.copyValueOf(body);
	}

	public static String appendUserList(String userListHtml, String appendString, String targetStr) throws IOException {
		File file = new File(new File(WEB_BASE_DIR), userListHtml);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);

		String fileString = bufferedReader.readLine();
		StringBuilder stringBuilder = new StringBuilder();

		while (fileString != null) {
			stringBuilder.append(fileString);
			if (fileString.contains(targetStr)) {
				stringBuilder.append(appendString);
			}

			fileString = bufferedReader.readLine();
		}

		bufferedReader.close();

		return stringBuilder.toString();
	}

	public static String makeUserListHtml() {
		StringBuilder stringBuilder = new StringBuilder();
		Iterator<User> users = DataBase.findAll().iterator();
		int index = 1;

		while (users.hasNext()) {
			User user = users.next();
			stringBuilder.append("<tr>");
			stringBuilder.append("<th>" + index + "</th>");
			stringBuilder.append("<td>" + user.getUserId() + "</td>");
			stringBuilder.append("<td>" + user.getName() + "</td>");
			stringBuilder.append("<td>" + user.getEmail() + "</td>");
			stringBuilder.append("</tr>");
		}

		return stringBuilder.toString();
	}

	public static byte[] htmlParser(String httpHeader) throws IOException {
		File htmlFile = new File(new File(WEB_BASE_DIR), httpHeader);
		return Files.readAllBytes(htmlFile.toPath());
	}
}
