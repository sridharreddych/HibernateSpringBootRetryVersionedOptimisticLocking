package com.bookstore;

import com.bookstore.service.BookstoreService;
import com.vladmihalcea.concurrent.aop.OptimisticConcurrencyControlAspect;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class MainApplication {

    private final BookstoreService bookstoreService;

    public MainApplication(BookstoreService bookstoreService) {
        this.bookstoreService = bookstoreService;
    }

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @Bean
    public OptimisticConcurrencyControlAspect
            optimisticConcurrencyControlAspect() {

        return new OptimisticConcurrencyControlAspect();
    }

    @Bean
    public ApplicationRunner init() {
        return args -> {

            ExecutorService executor = Executors.newFixedThreadPool(2);
            executor.execute(bookstoreService);
            // Thread.sleep(2000); -> adding a sleep here will break the transactions concurrency
            executor.execute(bookstoreService); 

            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        };
    }
}


/*
 * 
 * 
 * 
 * 
 * How To Retry Transactions After OptimisticLockException Exception (@Version)

Note: Optimistic locking mechanism via @Version works for detached entities as well.

Description: This is a Spring Boot application that simulates a scenario that leads to an optimistic locking exception. When such exception occur, the application retry the corresponding transaction via db-util library developed by Vlad Mihalcea.

Key points:

for Maven, in pom.xml, add the db-util dependency
configure the OptimisticConcurrencyControlAspect bean
mark the method (not annotated with @Transactional) that is prone to throw (or that calls a method that is prone to throw (this method can be annotated with @Transactional)) an optimistic locking exception with @Retry(times = 10, on = OptimisticLockingFailureException.class)
 */
