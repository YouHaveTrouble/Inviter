package me.youhavetrouble.inviter.http.endpoints;

import com.sun.net.httpserver.HttpExchange;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.regex.Pattern;

public interface EndpointHandler {

    @NotNull Pattern pathPattern();

    void handle(@NotNull HttpExchange exchange) throws IOException;

}
