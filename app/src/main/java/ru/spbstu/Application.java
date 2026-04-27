package ru.spbstu;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.netty.http.server.HttpServer;

public class Application {

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {

            String host = context.getEnvironment().getProperty("server.host", "0.0.0.0");
            int port = Integer.parseInt(context.getEnvironment().getProperty("server.port", "8080"));

            // Создаём DispatcherHandler — главный обработчик запросов WebFlux
            DispatcherHandler dispatcherHandler = new DispatcherHandler(context);

            // Строим WebHttpHandler на основе dispatcherHandler
            var httpHandler = WebHttpHandlerBuilder.webHandler(dispatcherHandler).build();
            var adapter = new ReactorHttpHandlerAdapter(httpHandler);

            HttpServer.create()
                    .host(host)
                    .port(port)
                    .handle(adapter)
                    .bindNow()
                    .onDispose()
                    .block();

            System.out.println("✅ HTTP server started on http://" + host + ":" + port);
            System.out.println("✅ Healthcheck available at http://localhost:" + port + "/healthcheck");

            // Блокируем main, чтобы сервер не завершился
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}