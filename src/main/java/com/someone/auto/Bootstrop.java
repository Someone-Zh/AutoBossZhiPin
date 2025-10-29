package com.someone.auto;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.someone.auto.process.Delivery;

@Configuration
@ComponentScan()
public class Bootstrop {
    public static void main(String[] args) {
         ApplicationContext context = new AnnotationConfigApplicationContext(Bootstrop.class);
         Delivery delivery = context.getBean(Delivery.class);
         delivery.deliver();
    }
}
