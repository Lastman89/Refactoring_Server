package org.example;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Server implements Runnable{

    static Socket serverSocket;
    BufferedReader in;
    BufferedOutputStream out;
    Request request;

    final List<String> validPaths = List.of("/index.html",
            "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html",
            "/classic.html", "/events.html", "/events.js");
    public Server(Socket serverSocket) throws IOException {
        Server.serverSocket = serverSocket;
        //отправить
        out = new BufferedOutputStream(serverSocket.getOutputStream());
        //принять
        in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        request = new Request(serverSocket);

    }

    @Override
    public void run(){
        while (!serverSocket.isClosed()) {
            try  {
                while (true) {
                        // read only request line for simplicity
                        // must be in form GET /path HTTP/1.1
                        final var requestLine = in.readLine();
                        final var parts = requestLine.split(" ");

                        if (parts.length != 3) {
                            // just close socket
                            continue;
                        }

                        final var path = parts[1];
                        if (!validPaths.contains(path)) {
                            request.getError();
                            continue;
                        }

                        final var filePath = Path.of(".", "public", path);

                        // special case for classic
                        if (path.equals("/classic.html")) {
                            final var template = Files.readString(filePath);
                            final var content = template.replace(
                                    "{time}",
                                    LocalDateTime.now().toString()
                            ).getBytes();
                            request.get(path);
                            out.write(content);
                            out.flush();
                            continue;
                        }
                        request.get(path);
                    /*final var mimeType = Files.probeContentType(filePath);

                    final var length = Files.size(filePath);
                    out.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    Files.copy(filePath, out);
                    out.flush();*/

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
