package ru.spbstu;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.netty.http.server.HttpServer;

public class Application {

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            System.out.println("Application started. HTTP server listening on http://localhost:8080");

            var httpHandler = WebHttpHandlerBuilder.applicationContext(context).build();
            var adapter = new ReactorHttpHandlerAdapter(httpHandler);

            HttpServer.create()
                    .host("0.0.0.0")
                    .port(8080)
                    .handle(adapter)
                    .bindNow()
                    .onDispose()
                    .block();

            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}