package org.example;

import java.io.BufferedOutputStream;
import java.io.IOException;

public interface addHandler {
    public void handle(Request request, BufferedOutputStream responseStream) throws IOException;
}
