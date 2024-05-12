package org.example;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RequestParser {

    BufferedOutputStream out;
    static Request request = new Request();
    static BufferedInputStream in;

    public RequestParser(Socket serverSocket) throws IOException {
        Server.serverSocket = serverSocket;
        //отправить
        out = new BufferedOutputStream(serverSocket.getOutputStream());
        //принять
        in = new BufferedInputStream(serverSocket.getInputStream());

    }

    public String getMethod(String[] method) {
        return method[0];
    }
    public String getPath (String[] path) {
        return path[1];
    }

    public String getHeaders(String[] partOfHeaders) {

        StringBuilder makeHeaders = new StringBuilder();

        for (int i = 0; i < partOfHeaders.length; i++) {
            if (partOfHeaders != null) {
                makeHeaders.append(partOfHeaders[i]);
            }
        }
        return makeHeaders.toString();
    }

    public static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }
    public static List<NameValuePair> getParam(String url) {

        List<NameValuePair> params = null;
        try {
            params = URLEncodedUtils.parse(new URI(url), Charset.forName("UTF-8"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        // Find first NameValuePair where the name equals initialURI
        Optional<NameValuePair> initialURI = params.stream()
                .filter(e -> e.getName().equals("/forms"))
                .findFirst();

        List<NameValuePair> initialParams = null;
        try {
            initialParams = URLEncodedUtils
                    .parse(new URI(initialURI.get().getValue()), Charset.forName("UTF-8"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        System.out.println(initialParams);
        return initialParams;
    }
    public static String getBody(String method, String headers) throws IOException {
        String body = null;
        if (!method.equals("GET")) {
            // вычитываем Content-Length, чтобы прочитать body
            final var contentLength = extractHeader(Arrays.asList(headers), "Content-Length");
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);
                body = new String(bodyBytes);
                System.out.println(body);

            }
        }
        return body;
    }

    public void getAnswer(String path) throws IOException {

        final var filePath = Path.of(".", "public", path);
        final var mimeType = Files.probeContentType(filePath);
        final var length = Files.size(filePath);

        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();

    }

    public void getError() throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }



}
