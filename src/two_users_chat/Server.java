package two_users_chat;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private final Scanner CONSOLE = new Scanner(System.in);
    private final String host;
    private final int port;
    private Scanner in;
    private PrintWriter out;

    public Server(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        String message;
        try (
                ServerSocket server = serverInit();
                Socket client = clientInit(server);
                ) {
            inputOutputStreamsInit(client);
            Thread serverOutputThread = new ServerOutputThread(out);
            serverOutputThread.start();
            while (true) {
                message = receive();
                if (message.endsWith("exit")){
                    send("exit");
                    break;
                }
                System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ServerSocket serverInit() {
        System.out.println("server starting...");
        try {
            return new ServerSocket(port, 10, InetAddress.getByName(host));
        } catch (IOException e) {
            System.out.println("ошибка при инициализации серверного сокета");
            throw new RuntimeException(e);
        }
    }

    private Socket clientInit(ServerSocket server) {
        try {
            return server.accept();
        } catch (IOException e) {
            System.out.println("ошибка при подключении клиентского сокета");
            throw new RuntimeException(e);
        }
    }

    private void inputOutputStreamsInit (Socket client) throws IOException {
        in = inInit(client);
        out = outInit(client);
    }

    private PrintWriter outInit(Socket client) throws IOException {
        try {
            return new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    client.getOutputStream()
                            )
                    ), true
            );
        } catch (IOException e){
            System.out.println("Ошибка при инициализации потока вывода");
            throw new RuntimeException();
        }
    }

    private Scanner inInit(Socket client) throws IOException {
        try {
            return new Scanner(
                    new InputStreamReader(
                                    client.getInputStream()
                    )
            );
        } catch (IOException e){
            System.out.println("Ошибка при инициализации потока ввода");
            throw new RuntimeException();
        }
    }

    private String receive() {
        try {
            return in.nextLine();
        } catch (NoSuchElementException e){
            return "exit";
        }
    }

    private void sendMessage() {
        String message = CONSOLE.nextLine();
        send(message);
    }

    private void send(String message) {
        out.println("SERVER -> " + message);
    }



    class ServerOutputThread extends Thread {
        private final Scanner CONSOLE = new Scanner(System.in);
        private final PrintWriter out;


        public  ServerOutputThread(PrintWriter out) {
            this.out = out;
            setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                String message = CONSOLE.nextLine();
                out.println("SERVER> " + message);
                if (message.equals("exit")){
                    break;
                }
            }
            out.close();
        }
    }

        public static void main(String[] args) throws IOException {
        Server server = new Server("127.0.0.1", 9000);
        server.start();
    }

}
