package com.mtrade.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProcessorApp {

    public static void main(String[] args) throws Exception {
        SpringApplication.run("classpath:/processor-context.xml", args);
    }
}
