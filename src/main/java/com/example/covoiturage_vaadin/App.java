package com.example.covoiturage_vaadin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.vaadin.flow.component.page.AppShellConfigurator;

@SpringBootApplication
@EnableTransactionManagement
public class App implements AppShellConfigurator{
    
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}