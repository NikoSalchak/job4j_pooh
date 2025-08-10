package ru.job4j.pooh;

import java.io.*;
import java.net.Socket;

public class ConsumerClient {

    public static void main(String[] args) {
        try (var socket = new Socket("127.0.0.1", 9000)) {
            try (var out = new PrintWriter(socket.getOutputStream());
                 var input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                out.println("intro;queue;weather");
                out.flush();
                while (true) {
                    var text = input.readLine();
                    System.out.println(text);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
