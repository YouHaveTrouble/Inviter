package me.youhavetrouble.inviter.http;

import com.sun.net.httpserver.HttpServer;
import me.youhavetrouble.inviter.Main;
import me.youhavetrouble.inviter.http.endpoints.HandlerKernel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ApiServer {

    private final HttpServer server;

    public ApiServer(String hostname, int port) throws IllegalArgumentException, IOException {
        server = HttpServer.create(new InetSocketAddress(hostname, port), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.createContext("/", new HandlerKernel());
        server.start();
        Main.LOGGER.info("Http API server started on {}:{}", hostname, port);
    }

}
