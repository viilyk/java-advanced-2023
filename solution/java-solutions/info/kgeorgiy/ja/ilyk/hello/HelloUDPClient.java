package info.kgeorgiy.ja.ilyk.hello;

import info.kgeorgiy.java.advanced.hello.*;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

public class HelloUDPClient implements HelloClient {
    private static final int RECEIVE_TIMEOUT = 100;

    private boolean check(String prefix, int a, int b, String receivedMessage) {
        String hello = "Hello, ";
        if (!receivedMessage.startsWith(hello)) {
            return false;
        }
        receivedMessage = receivedMessage.substring(hello.length());
        if (!receivedMessage.startsWith(prefix)) {
            return false;
        }
        receivedMessage = receivedMessage.substring(prefix.length());
        String[] array = receivedMessage.split("_");
        try {
            return Integer.parseInt(array[0]) == a && Integer.parseInt(array[1]) == b;
        } catch (NumberFormatException ignored) {
        }
        return false;
    }


    private void threadRequests(SocketAddress address, String prefix, int requests, int n, Phaser phaser) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(RECEIVE_TIMEOUT);
            int bufferSize = socket.getReceiveBufferSize();
            final DatagramPacket packet = new DatagramPacket(new byte[bufferSize], bufferSize, address);
            for (int i = 1; i <= requests; i++) {
                String requestedMessage = prefix + n + "_" + i;
                System.out.println("Requested: " + requestedMessage);
                while (!socket.isClosed() && !Thread.interrupted()) {
                    try {
                        packet.setData(requestedMessage.getBytes(StandardCharsets.UTF_8));
                        socket.send(packet);
                        packet.setData(new byte[bufferSize]);
                        try {
                            socket.receive(packet);
                        } catch (IOException e) {
                            System.err.println("Error occurred while receiving the packet. " + e.getMessage());
                        }
                        String receivedMessage = new String(packet.getData(), packet.getOffset(),
                                packet.getLength(), StandardCharsets.UTF_8);
                        if (check(prefix, n, i, receivedMessage)) {
                            System.out.println("Received: " + receivedMessage);
                            break;
                        }
                    } catch (PortUnreachableException e) {
                        System.err.println("Socket is connected to a currently unreachable destination. "
                                + e.getMessage());
                    } catch (IOException e) {
                        System.err.println("Error occurred while sending the packet. " + e.getMessage());
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Error creating or opening socket. " + e.getMessage());
        } finally {
            phaser.arrive();
        }
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        Phaser phaser = new Phaser(1);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        SocketAddress address = new InetSocketAddress(host, port);
        for (int i = 1; i <= threads; i++) {
            final int k = i;
            phaser.register();
            executor.submit(() -> threadRequests(address, prefix, requests, k, phaser));
        }
        phaser.arriveAndAwaitAdvance();
        executor.shutdown();
    }

    public static void main(String[] args) {
        if (args == null || args.length != 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Incorrect arguments, expected: <host> <port> <prefix> <threads> <requests>");
            return;
        }
        try {
            new HelloUDPClient().run(args[0], Integer.parseInt(args[1]), args[2],
                    Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        } catch (NumberFormatException e) {
            System.err.println("Parsing integer error; port, threads and requests must be integers. " + e.getMessage());
        }
    }

}
