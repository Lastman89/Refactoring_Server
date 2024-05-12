package org.example;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;


public class Server implements Runnable {

    static Socket serverSocket;
    BufferedReader in;
    BufferedOutputStream out;
    RequestParser requestParser;
    BufferedInputStream inn;
    Request request = new Request();

    final String GET = "GET";
    final String POST = "POST";
    List<String> validPaths = List.of("/index.html",
            "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html",
            "/classic.html", "/events.html", "/events.js", "/test.html");
    Map <String, List<String>> mapGet = new HashMap<>();
    Map <String, List<String>> mapPost = new HashMap<>();


    public Server(Socket serverSocket) throws IOException {
        Server.serverSocket = serverSocket;
        //отправить
        out = new BufferedOutputStream(serverSocket.getOutputStream());
        //принять
        in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        inn = new BufferedInputStream(serverSocket.getInputStream());
        requestParser = new RequestParser(serverSocket);
        mapGet.put(GET, validPaths);
        mapPost.put(POST, validPaths);

    }

    @Override
    public void run() {
        while (!serverSocket.isClosed()) {
            try {
                while (true) {

                    // read only request line for simplicity
                    // must be in form GET /path HTTP/1.1
                    final var requestLine = in.readLine();
                    //System.out.println(requestLine);

                    final var parts = requestLine.split(" ");

                    if (parts.length != 3) {
                        request.headers = requestParser.getHeaders(parts);
                        System.out.println(request.headers);
                        continue;

                    }

                    request.method = requestParser.getMethod(parts);
                    System.out.println(request.method);

                    final var filePath = Path.of(".", "public", requestParser.getPath(parts));

                    // special case for classic
                    if (requestParser.getPath(parts).equals("/classic.html")) {
                        final var template = Files.readString(filePath);
                        final var content = template.replace(
                                "{time}",
                                LocalDateTime.now().toString()
                        ).getBytes();
                        requestParser.getAnswer(requestParser.getPath(parts));
                        out.write(content);
                        out.flush();
                        continue;
                    }
                    //System.out.println(requestParser.getParam(requestParser.getPath(parts)));
                    //requestParser.getAnswer(requestParser.getPath(parts));
                    handler(request, requestParser.getPath(parts));

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void handler (Request request, String path) throws IOException {

        addHandler handlers = new addHandler() {
            @Override
            public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                if (request.method.equals(GET)) {
                    List<String> paths = mapGet.get(request.method);
                    for (int i = 0; i < paths.size(); i++) {
                        if (path.contains(paths.get(i))) {
                            requestParser.getAnswer(path);
                        }
                    }

                }
                if (request.method.equals(POST)) {
                    List<String> paths = mapPost.get(request.method);
                    for (int i = 0; i < paths.size(); i++) {
                        if (path.contains(paths.get(i))) {
                            requestParser.getAnswer(path);
                            requestParser.getBody(request.method, request.headers);

                        }
                    }

                }


            }
        };
        handlers.handle(request, out);
    }
    
}
