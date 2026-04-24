package ru.spbstu;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Application {
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            System.out.println("Application started.");
            // Можно получить бин и что-то сделать для теста
            // var facade = context.getBean(FormSolvingFacade.class);
            // facade.solve(123L, "https://forms.gle/...").subscribe(System.out::println);
            Thread.currentThread().join(); // чтобы реактор не завершился сразу
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}