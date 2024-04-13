package info.kgeorgiy.ja.ilyk.hello;

import info.kgeorgiy.java.advanced.hello.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPServer implements HelloServer {
    private DatagramSocket socket;
    private int bufferSize;
    private ExecutorService executor;
    private static final int RECEIVE_TIMEOUT = 100;
    private static final int AWAIT_TIMEOUT = 100;

    @Override
    public void start(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
            socket.setSoTimeout(RECEIVE_TIMEOUT);
            bufferSize = socket.getReceiveBufferSize();
            executor = Executors.newFixedThreadPool(threads);
            for (int i = 0; i < threads; i++) {
                executor.submit(this::run);
            }
        } catch (SocketException e) {
            System.err.println("Error creating or opening socket. " + e.getMessage());
        }
    }

    private void run() {
        DatagramPacket packet = new DatagramPacket(new byte[bufferSize], bufferSize);

        while (!socket.isClosed() && !Thread.interrupted()) {
            try {
                socket.receive(packet);
                String requestMessage = new String(packet.getData(), packet.getOffset(),
                        packet.getLength(), StandardCharsets.UTF_8);
                String message = "Hello, " + requestMessage;
                packet.setData(message.getBytes(StandardCharsets.UTF_8));
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    System.err.println("Error occurred while sending the packet. " + e.getMessage());
                }
            } catch (PortUnreachableException e) {
                System.err.println("Socket is connected to a currently unreachable destination. "
                        + e.getMessage());
            } catch (IOException e) {
                System.err.println("Error occurred while receiving the packet. " + e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        socket.close();
        executor.shutdown();
        while (true) {
            try {
                if (executor.awaitTermination(AWAIT_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    break;
                }
            } catch (InterruptedException ignored) {
            }
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Incorrect arguments, expected: <port> <threads>");
            return;
        }
        try {
            new HelloUDPServer().start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        } catch (NumberFormatException e) {
            System.err.println("Parsing integer error, arguments must be integers. " + e.getMessage());
        }
    }
}
