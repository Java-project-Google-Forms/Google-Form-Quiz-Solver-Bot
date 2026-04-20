package ru.spbstu;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Application {

    public static void main(String[] args) {
//        var context = new AnnotationConfigApplicationContext(AppConfig.class);
//
//        String host = context.getEnvironment().getProperty("server.host", "0.0.0.0");
//        int port = Integer.parseInt(context.getEnvironment().getProperty("server.port", "8080"));
//
//        HttpHandler httpHandler = WebHttpHandlerBuilder.applicationContext(context).build();
//        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
//
//        HttpServer.create()
//                .host(host)
//                .port(port)
//                .handle(adapter)
//                .bindNow()
//                .onDispose()
//                .block();


        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            System.out.println("Application started.");
        }
    }
}
