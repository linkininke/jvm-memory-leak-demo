package com.demo.leak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LeakDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeakDemoApplication.class, args);
    }
}
