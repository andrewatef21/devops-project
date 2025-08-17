import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.ZonedDateTime;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", exchange -> {
            String response = "Hello from Java! Time: " + ZonedDateTime.now() + "\n";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(response.getBytes()); }
        });
        server.createContext("/health", exchange -> {
            String response = "ok";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(response.getBytes()); }
        });
        System.out.println("Java HTTP server listening on port " + port);
        server.start();
    }
}
