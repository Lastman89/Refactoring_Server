package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MultiServer {
    static ExecutorService executeIt = Executors.newFixedThreadPool(64);
    public static final Integer LOCALHOST_PORT = 9999;


    public static void main(String[] args){

        try {
            ServerSocket socket = new ServerSocket(LOCALHOST_PORT);

            //отправить
            //принять
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            // стартуем цикл при условии что серверный сокет не закрыт
            while (!socket.isClosed()) {

                // проверяем поступившие комманды из консоли сервера если такие
                // были
                if (in.ready()) {
                    System.out.println("Main Server found any messages in channel, let's look at them.");

                    // если команда - exit то инициализируем закрытие сервера и
                    // выход из цикла раздачии нитей монопоточных серверов
                    String serverCommand = in.readLine();
                }

                // если комманд от сервера нет то становимся в ожидание
                // подключения к сокету общения под именем - "clientSocket" на
                // серверной стороне
                Socket clientSocket = socket.accept();


                // после получения запроса на подключение сервер создаёт сокет
                // для общения с клиентом и отправляет его в отдельную нить
                // в Runnable
                // монопоточную нить = сервер - Server и тот
                // продолжает общение от лица сервера
                executeIt.execute(new Server(clientSocket));
                System.out.print("Connection accepted.");
            }

            // закрытие пула нитей после завершения работы всех нитей
            executeIt.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
