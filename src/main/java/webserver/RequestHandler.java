package webserver;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private static final String WEB_BASE_DIR = System.getProperty("user.dir") + File.separator + "webapp";

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream();
             OutputStream out = connection.getOutputStream()) {

            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String httpHeader = bufferedReader.readLine();
            if (httpHeader == null || "".equals(httpHeader)) {
                return;
            }

            if (httpHeader.contains("/user/create")) {
                User user = userParser(httpHeader);
            }

            if (httpHeader.contains(".html")) {
                DataOutputStream dos = new DataOutputStream(out);

                byte[] body = htmlParser(httpHeader);
                response200Header(dos, body.length);
                responseBody(dos, body);
                return;
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private User userParser(String httpHeader) {
        httpHeader = httpHeader.split(" ")[1].replace("/user/create", "").replace("?", "");
        return new User(HttpRequestUtils.parseQueryString(httpHeader));
    }

    private byte[] htmlParser(String httpHeader) throws IOException {
        String htmlFileName = httpHeader.split(" ")[1].replace("/", File.separator);

        File htmlFile = new File(WEB_BASE_DIR + htmlFileName);
        return Files.readAllBytes(htmlFile.toPath());
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
